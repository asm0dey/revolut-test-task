package revolut.repo;

import com.google.common.annotations.VisibleForTesting;
import revolut.dto.AccountDTO;
import revolut.exceptions.InvalidTransferException;
import revolut.exceptions.NotEnoughMoneyException;
import revolut.model.Account;

import javax.inject.Singleton;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static com.google.common.collect.Lists.newArrayList;

@Singleton
public class AccountRepo {
    private AtomicInteger userCounter = new AtomicInteger(0);
    private Map<Integer, Account> users = new ConcurrentHashMap<>();
    private Map<Integer, Object> locks = new ConcurrentHashMap<>();

    public int createAccount() {
        int id = userCounter.incrementAndGet();
        users.put(id, new Account(id, new AtomicReference<>(BigDecimal.ZERO)));
        return id;
    }

    public Account deleteAccount(int id) {
        return users.remove(id);
    }

    public AccountDTO fullfill(int id, BigDecimal amount) {
        Account account = users.get(id);
        if (account == null) return null;
        account.amount.updateAndGet(current -> current.add(amount));
        return AccountDTO.fromAccount(account);
    }

    public List<AccountDTO> transfer(Integer from, Integer to, BigDecimal amount) throws NotEnoughMoneyException, InvalidTransferException {
        if (from == null || to == null) return null;
        if (Objects.equals(from,to)) throw new InvalidTransferException();
        Account accountFrom = users.get(from);
        Account accountTo = users.get(to);
        if (accountFrom == null || accountTo == null) return null;
        synchronized (locks.computeIfAbsent(Math.min(from, to), integer -> new Object())) {
            synchronized (locks.computeIfAbsent(Math.max(from, to), integer -> new Object())) {
                if (accountFrom.amount.get().compareTo(amount) < 0)
                    throw new NotEnoughMoneyException("User with id " + from + " has not enough money to transfer to user with id " + to);
                accountFrom.amount.updateAndGet(current -> current.subtract(amount));
                accountTo.amount.updateAndGet(current -> current.add(amount));
                return newArrayList(AccountDTO.fromAccount(accountFrom), AccountDTO.fromAccount(accountTo));

            }
        }

    }

    @VisibleForTesting
    public Map<Integer, Account> getUsers() {
        return users;
    }
}
