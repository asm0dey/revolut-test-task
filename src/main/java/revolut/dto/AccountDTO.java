package revolut.dto;

import revolut.model.Account;

import java.math.BigDecimal;

public class AccountDTO {
    private final int accountId;
    private final BigDecimal moneyAmount;

    public static AccountDTO fromAccount(Account account) {
        return new AccountDTO(account.id, account.amount.get());
    }

    public AccountDTO(int accountId, BigDecimal moneyAmount) {
        this.accountId = accountId;
        this.moneyAmount = moneyAmount;
    }

    public int getAccountId() {
        return accountId;
    }

    public BigDecimal getMoneyAmount() {
        return moneyAmount;
    }
}
