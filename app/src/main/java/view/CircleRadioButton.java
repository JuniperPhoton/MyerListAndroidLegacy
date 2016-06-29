package view;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.RadioButton;
import com.juniperphoton.myerlistandroid.R;

import java.util.jar.Attributes;

/**
 * Created by dengw on 2016-06-29.
 */
public class CircleRadioButton extends RadioButton {
    public CircleRadioButton(Context context, AttributeSet attrs){
        super(context,attrs);
    }



    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        canvas.drawBitmap(getResources().getDrawable(R.drawable.ok));
    }
}
