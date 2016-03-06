package interfaces;

import org.json.JSONObject;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import model.ToDo;

/**
 * Created by dengw on 10/5/2015.
 */
public interface IRequestCallback {

    void onResponse(JSONObject jsonObject);
}
