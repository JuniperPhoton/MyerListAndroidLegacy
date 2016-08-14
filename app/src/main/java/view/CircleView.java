package view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;

public class CircleView extends ColorView {

    public CircleView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Paint paint = new Paint();
        paint.setColor(getColor());

        canvas.drawCircle(getWidth() / 2, getHeight() / 2, getWidth() / 2, paint);
    }
}
