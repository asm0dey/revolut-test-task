package revolut;

import io.restassured.response.ValidatableResponse;
import org.jooby.Status;
import org.jooby.test.JoobyRule;
import org.junit.ClassRule;
import org.junit.Test;

import java.math.BigDecimal;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.equalTo;

public class ApiTests {

    @ClassRule
    public static JoobyRule app = new JoobyRule(new App());

    @Test
    public void onAccountCreationICanObtainSequentialId() {
        Integer currentId = post("/account")
                .thenReturn()
                .as(Integer.class);
        post("/account")
                .then()
                .assertThat()
                .body(equalTo(String.valueOf(currentId + 1)))
                .statusCode(200);
        post("/account")
                .then()
                .assertThat()
                .body(equalTo(String.valueOf(currentId + 2)))
                .statusCode(200);
    }

    @Test
    public void onAccountDeletionIGet204Status() {
        Integer generatedId = post("/account")
                .thenReturn()
                .as(Integer.class);
        delete("/account/" + generatedId)
                .then()
                .assertThat()
                .statusCode(204);
    }

    @Test
    public void onDeletionOfNonExistentAccountIGet404() {
        Integer generatedId = post("/account")
                .thenReturn()
                .as(Integer.class);
        delete("/account/" + (generatedId + 1))
                .then()
                .assertThat()
                .statusCode(404);
    }

    @Test
    public void iCanFullfillAccountWithPatch() {
        Integer generatedId = post("/account")
                .thenReturn()
                .as(Integer.class);
        fullfill(generatedId, BigDecimal.ONE)
                .body("accountId", equalTo(generatedId))
                .body("moneyAmount", equalTo(1));
        fullfill(generatedId, BigDecimal.ONE)
                .body("accountId", equalTo(generatedId))
                .body("moneyAmount", equalTo(2));

        fullfill(generatedId, new BigDecimal("1.7777777777777777777777777"))
                .body("accountId", equalTo(generatedId))
                .body("moneyAmount", equalTo(3.7777777777777777777777777f));
    }

    private ValidatableResponse fullfill(Integer id, BigDecimal amount) {
        return patch("/account/" + id + "/fullfill/" + amount)
                .then();
    }

    @Test
    public void iCantFullfillUnexistentAccountAndGet404() {
        Integer generatedId = post("/account")
                .thenReturn()
                .as(Integer.class);
        patch("/account/" + (generatedId + 1) + "/fullfill/1")
                .then()
                .statusCode(404);
    }

    @Test
    public void iCanTransferMoneyFromOneAccountToAnother() {
        Integer generatedId1 = post("/account")
                .thenReturn()
                .as(Integer.class);
        Integer generatedId2 = post("/account")
                .thenReturn()
                .as(Integer.class);
        fullfill(generatedId1, new BigDecimal(5));
        fullfill(generatedId2, new BigDecimal(5));
        patch("/account/" + generatedId1 + "/transfer/" + generatedId2 + "/2")
                .then()
                .body("[0].accountId", equalTo(generatedId1))
                .body("[0].moneyAmount", equalTo(3))
                .body("[1].accountId", equalTo(generatedId2))
                .body("[1].moneyAmount", equalTo(7));
    }

    @Test
    public void whenITryToTransferMoneyFromNonExistentAccountIGet404() {
        Integer generatedId1 = post("/account")
                .thenReturn()
                .as(Integer.class);
        Integer generatedId2 = post("/account")
                .thenReturn()
                .as(Integer.class);
        fullfill(generatedId1, new BigDecimal(5));
        fullfill(generatedId2, new BigDecimal(5));
        patch("/account/" + (generatedId1 + 2) + "/transfer/" + generatedId2 + "/" + 2)
                .then()
                .statusCode(404);
    }

    @Test
    public void whenITryToTransferMoneyToNonExistentAccountIGet404() {
        Integer generatedId1 = post("/account")
                .thenReturn()
                .as(Integer.class);
        Integer generatedId2 = post("/account")
                .thenReturn()
                .as(Integer.class);
        fullfill(generatedId1, new BigDecimal(5));
        fullfill(generatedId2, new BigDecimal(5));
        patch("/account/" + generatedId1 + "/transfer/" + (generatedId2 + 2) + "/" + 2)
                .then()
                .statusCode(404);
    }

    @Test
    public void whenImTryingToTransferMoreMoneyThenFirstUserHasIGetError() {
        Integer generatedId1 = post("/account")
                .thenReturn()
                .as(Integer.class);
        Integer generatedId2 = post("/account")
                .thenReturn()
                .as(Integer.class);
        fullfill(generatedId2, new BigDecimal(5));
        patch("/account/" + generatedId1 + "/transfer/" + generatedId2 + "/" + 2)
                .then()
                .statusCode(412);
    }

    @Test
    public void whenImTryingToTransferMoreMoneyFromAccountToItselfIGain400Error() {
        Integer generatedId1 = post("/account")
                .thenReturn()
                .as(Integer.class);
        patch("/account/" + generatedId1 + "/transfer/" + generatedId1 + "/" + 2)
                .then()
                .statusCode(400);
    }

    @Test
    public void imRedirectedToRamlWhenImOpeningIndex() {
        given()
                .redirects()
                .follow(false)
                .when()
                .get("/")
                .then()
                .statusCode(302);
    }


}
