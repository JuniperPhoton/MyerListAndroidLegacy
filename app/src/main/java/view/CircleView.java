package view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.juniperphoton.myerlistandroid.R;

public class CircleView extends View {

    private int mcolor;

    public CircleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mcolor = getResources().getColor(R.color.MyerListBlue);
    }

    public int getColor() {
        return mcolor;
    }

    public void setEllipseColor(int color) {
        mcolor = color;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Paint paint = new Paint();
        paint.setColor(mcolor);

        canvas.drawCircle(getWidth() / 2, getHeight() / 2, getWidth() / 2, paint);
    }
}
