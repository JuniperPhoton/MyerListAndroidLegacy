package exception;

/**
 * Created by dengw on 3/4/2016.
 */
public class APIException extends Exception {

    private String error;

    public APIException(String string) {
        this.error = string;
    }

    public APIException() {

    }

    public String getError() {
        return error;
    }
}
