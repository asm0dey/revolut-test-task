package revolut.exceptions;

public class NotEnoughMoneyException extends Exception {
    public NotEnoughMoneyException(String s) {
        super(s);
    }
}
