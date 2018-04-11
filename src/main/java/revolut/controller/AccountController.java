package revolut.controller;

import com.google.inject.Inject;
import org.jooby.Err;
import org.jooby.Status;
import org.jooby.mvc.DELETE;
import org.jooby.mvc.PATCH;
import org.jooby.mvc.POST;
import org.jooby.mvc.Path;
import revolut.dto.AccountDTO;
import revolut.exceptions.InvalidTransferException;
import revolut.exceptions.NotEnoughMoneyException;
import revolut.model.Account;
import revolut.repo.AccountRepo;

import java.math.BigDecimal;
import java.util.List;

@Path("/account")
public class AccountController {
    private AccountRepo repo;

    @Inject
    public AccountController(AccountRepo repo) {
        this.repo = repo;
    }

    @POST
    public Integer createAccount() {
        return repo.createAccount();
    }

    @DELETE
    @Path("/:id")
    public void deleteAccount(Integer id) {
        Account userInfo = repo.deleteAccount(id);
        if (userInfo == null) throw new Err(Status.NOT_FOUND, "No user with id " + id + " found");
    }

    @PATCH
    @Path("/:id/fullfill/:amount")
    public AccountDTO fullfill(int id, BigDecimal amount) {
        AccountDTO accountDTO = repo.fullfill(id, amount);
        if (accountDTO == null) throw new Err(404, "Account " + id + " not found");
        return accountDTO;
    }

    @PATCH
    @Path("/:from/transfer/:to/:amount")
    public List<AccountDTO> transfer(int from, int to, BigDecimal amount) {
        try {
            List<AccountDTO> transfer = repo.transfer(from, to, amount);
            if (transfer == null) throw new Err(404, "One of accounts not found");
            return transfer;
        } catch (NotEnoughMoneyException e) {
            throw new Err(Status.PRECONDITION_FAILED, "User with id " + from + " has no enough money", e);
        } catch (InvalidTransferException e) {
            throw new Err(Status.BAD_REQUEST, "You can't transfer money from account to itself");
        }
    }

}
