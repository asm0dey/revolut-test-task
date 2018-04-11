package revolut.model;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicReference;

public class Account {
    public final int id;
    public final AtomicReference<BigDecimal> amount;

    public Account(int id, AtomicReference<BigDecimal> amount) {
        this.id = id;
        this.amount = amount;
    }
}
