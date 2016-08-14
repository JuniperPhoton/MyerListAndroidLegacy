package view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;

/**
 * Created by JuniperPhoton on 2016-07-31.
 */
public class RectangleView extends ColorView {

    public RectangleView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Paint paint = new Paint();
        paint.setColor(getColor());

        canvas.drawRect(0,0,getWidth(),getWidth(),paint);
    }
}
