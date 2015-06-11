package middle;

import android.content.Context;
import android.renderscript.RenderScript;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.RelativeLayout;
import android.widget.Toast;

import helper.AppHelper;
import helper.ContextUtil;

public class OnGestureListener implements GestureDetector.OnGestureListener
{
    private RelativeLayout mRootLayout;

    public OnGestureListener(RelativeLayout r)
    {
        mRootLayout=r;
    }


    @Override
    public boolean onDown(MotionEvent motionEvent)
    {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent motionEvent)
    {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent)
    {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1)
    {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent motionEvent)
    {

    }

    @Override
    public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1)
    {
        mRootLayout.scrollBy(-(int)motionEvent1.getRawX(),0);
        return false;
    }
}
