package interfaces;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import model.ToDo;

/**
 * Created by dengw on 10/5/2015.
 */
public interface IRequestCallbacks
{
    void OnCheckResponse(boolean check);
    void OnGetSaltResponse(String str);
    void OnLoginResponse(boolean value);
    void OnGotScheduleResponse(ArrayList<ToDo> mytodosList);
    void OnAddedResponse(boolean isSuccess, ToDo newTodo);
    void OnSetOrderResponse(boolean isSuccess);
    void OnRegisteredResponse(boolean isSuccess,String salt);
    void OnDoneResponse(boolean isSuccess);
    void OnDeleteResponse(boolean isSuccess);
    void OnUpdateContent(boolean isSuccess);

}
