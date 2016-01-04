package helper;

import com.example.juniper.myerlistandroid.R;

/**
 * Created by dengw on 1/5/2016.
 */
public class FindRadioBtnHelper
{
    public static int GetCateByRadioBtnID(int i)
    {
        int index = 0;
        if (i == R.id.radio_default_btn) index = 0;
        else if (i == R.id.radio_work_btn) index = 1;
        else if (i == R.id.radio_life_btn) index = 2;
        else if (i == R.id.radio_family_btn) index = 3;
        else if (i == R.id.radio_enter_btn) index = 4;

        return index;
    }

    public static int GetRadioBtnIDByCate(int cate)
    {
        int id = 0;
        if (cate == 0) id = R.id.radio_default_btn;
        else if (cate == 1) id = R.id.radio_work_btn;
        else if (cate == 2) id = R.id.radio_life_btn;
        else if (cate == 3) id = R.id.radio_family_btn;
        else if (cate == 4) id = R.id.radio_enter_btn;
        return id;
    }
}
