package viewholder;

import android.text.Editable;
import android.text.TextWatcher;

import model.ToDoCategory;

/**
 * Created by chao on 8/7/2016.
 */
public class CateTextWatcher implements TextWatcher {

    private ToDoCategory mToDocategory;

    public CateTextWatcher(ToDoCategory toDoCategory) {
        mToDocategory = toDoCategory;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        mToDocategory.setName(s.toString());
    }
}
