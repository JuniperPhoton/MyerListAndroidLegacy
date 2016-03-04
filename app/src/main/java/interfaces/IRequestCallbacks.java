package interfaces;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import model.ToDo;

/**
 * Created by dengw on 10/5/2015.
 */
public interface IRequestCallbacks {

    void onResponse(String responseString);

    void onCheckResponsenCheckResponse(boolean check);

    void onGetSaltResponse(String str);

    void onLoginResponse(boolean value);

    void onGotScheduleResponse(boolean value, ArrayList<ToDo> mytodosList);

    void onAddedResponse(boolean isSuccess, ToDo newTodo);

    void onSetOrderResponse(boolean isSuccess);

    void onRegisteredResponse(boolean isSuccess, String salt);

    void onDoneResponse(boolean isSuccess);

    void onDeleteResponse(boolean isSuccess);

    void onUpdateContent(boolean isSuccess);
}
