package api;

import org.apache.http.HttpStatus;

/**
 * Created by dengw on 3/4/2016.
 */
public class Response {

    private int statusCode;

    private int errorCode;
    private String errorMsg;
    private String data;

    public Response() {
        statusCode = HttpStatus.SC_OK;
    }

    public Response(int statusCode) {
        this.statusCode = statusCode;
    }

    public boolean isSuccess() {
        return (199 < statusCode) && (statusCode < 300) && errorCode == 0;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int mStatusCode) {
        this.statusCode = mStatusCode;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
