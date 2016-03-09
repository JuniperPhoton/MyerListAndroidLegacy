package util;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.juniperphoton.myerlistandroid.R;

/**
 * Created by dengw on 1/8/2016.
 */
public class ToastService {
    public static void showShortToast(String str) {
        LayoutInflater inflater = LayoutInflater.from(AppExtension.getInstance());
        View view = inflater.inflate(R.layout.custom_toast, null);

        TextView textView = (TextView) view.findViewById(R.id.toast_textView);
        textView.setText(str);

        Toast toast = new Toast(AppExtension.getInstance());
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(view);
        toast.setGravity(Gravity.BOTTOM, 0, 100);
        toast.show();
    }
}
