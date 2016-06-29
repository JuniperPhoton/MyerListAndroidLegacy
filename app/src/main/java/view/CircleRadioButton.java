package view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.juniperphoton.myerlistandroid.R;

import java.util.jar.Attributes;

import util.ColorUtil;

/**
 * Created by dengw on 2016-06-29.
 */
public class CircleRadioButton extends RadioButton {
    private int mColor;

    public CircleRadioButton(Context context) {
        super(context);
        init();
    }

    public CircleRadioButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                invalidate();
            }
        });
    }

    public void setCircleColor(int color) {
        mColor = color;
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        Paint paint = new Paint();
        paint.setColor(mColor);

        Paint darkPaint = new Paint();
        darkPaint.setColor(ColorUtil.MakeColorDarker(mColor));

        canvas.drawCircle(getWidth() / 2, getHeight() / 2, getWidth() / 2 - 12, darkPaint);
        canvas.drawCircle(getWidth() / 2, getHeight() / 2, getWidth() / 2 - 20, paint);

        if (isChecked()) {
            Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.white_tick);
            Matrix matrix = new Matrix();
            float scaleWidth = (float) getWidth() / bmp.getWidth();
            float scaleHeight = (float) getHeight() / bmp.getHeight();
            matrix.postScale(scaleWidth, scaleHeight);
            Bitmap destBmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
            canvas.drawBitmap(destBmp, 0, 0, null);
        }
    }
}
