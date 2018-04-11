package revolut;

import org.jooby.Jooby;
import org.jooby.Results;
import org.jooby.apitool.ApiTool;
import org.jooby.json.Jackson;
import revolut.controller.AccountController;

/**
 * @author jooby generator
 */
public class App extends Jooby {

    {
        use(new Jackson());

        use(AccountController.class);

        use(new ApiTool()
                .disableTryIt()
                .raml()
        );

        get("/", () -> Results.redirect("/raml"));
    }

    public static void main(final String[] args) {
        run(App::new, args);
    }

}
