package api;

import org.apache.http.HttpStatus;

/**
 * Created by dengw on 3/4/2016.
 */
public class StringResponse {
    private String mResponse = null;
    private int mStatusCode = HttpStatus.SC_OK;

    public StringResponse(int statusCode, String response) {
        mStatusCode = statusCode;
        mResponse = response;
    }

    public String getResponse() {
        return mResponse;
    }

    public int getStatusCode() {
        return mStatusCode;
    }

    public boolean isSuccess() {
        return (199 < mStatusCode) && (mStatusCode < 300);
    }
}
