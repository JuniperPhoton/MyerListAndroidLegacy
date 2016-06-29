package util;

import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.juniperphoton.myerlistandroid.R;

/**
 * Created by dengw on 2016-06-30.
 */
public class RadioGroupUtil {
    public static RadioButton findButtonByIdInGroup(RadioGroup group,int id){
        RadioButton button=(RadioButton)group.findViewById(id);
        return button;
    }
}
