package revolut;

import org.junit.Test;
import revolut.exceptions.InvalidTransferException;
import revolut.exceptions.NotEnoughMoneyException;
import revolut.repo.AccountRepo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.LongAdder;

import static org.junit.Assert.assertEquals;

public class DeadlockTest {
    AccountRepo repo = new AccountRepo();

    /**
     * This test isn't perfect and will fail on really slow machines or on strange architectures. Also it can fail if the code is written wrong,
     * The wole idea is we're trying to create so many concurrent transfers that chance of deadlock should be very high. And we're trying to execute them on CommonPool, which size is pretty big
     * If the deadlock will occur — we won't succeed to complete this test in 100 seconds and test will fail
     */
    @Test(timeout = 100_000L)
    public void tryToCreateDeadlock() {
        int[] ids = new int[]{repo.createAccount(), repo.createAccount(), repo.createAccount()};
        for (int id : ids) {
            repo.fullfill(id, new BigDecimal("10000000"));
        }

        LongAdder counter = new LongAdder();
        List<Runnable> transfers = new ArrayList<>(12_000_000);
        for (int i = 0; i < 2_000_000; i++) {
            transfers.add(transactionRequest(counter, ids[0], ids[1]));
            transfers.add(transactionRequest(counter, ids[1], ids[2]));
            transfers.add(transactionRequest(counter, ids[2], ids[0]));
            transfers.add(transactionRequest(counter, ids[0], ids[2]));
            transfers.add(transactionRequest(counter, ids[2], ids[1]));
            transfers.add(transactionRequest(counter, ids[1], ids[0]));
        }
        transfers
                .parallelStream()
                .forEach(Runnable::run);
        // check we have really called transfer 12 million times
        assertEquals(12_000_000L, counter.sum());
        // check we haven't losed any money
        assertEquals(0, repo
                .getUsers()
                .values()
                .stream()
                .map(it -> it.amount.get())
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO).compareTo(new BigDecimal("30000000")));
    }

    private Runnable transactionRequest(LongAdder adder, int id1, int id2) {
        return () -> {
            try {
                repo.transfer(id1, id2, BigDecimal.ONE);
                adder.increment();
            } catch (NotEnoughMoneyException | InvalidTransferException ignored) {
                // in this test we don't care about exceptions, we're looking for deadlocks
            }
        };
    }
}
