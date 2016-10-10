package view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.CompoundButton;
import android.widget.RadioButton;

import com.juniperphoton.jputils.ColorUtil;
import com.juniperphoton.myerlistandroid.R;

public class CircleRadioButton extends RadioButton {
    private int mCircleColor;

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
        mCircleColor = color;
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        Paint paint = new Paint();
        paint.setColor(mCircleColor);

        Paint darkPaint = new Paint();
        darkPaint.setColor(ColorUtil.makeColorDarker(mCircleColor));

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
