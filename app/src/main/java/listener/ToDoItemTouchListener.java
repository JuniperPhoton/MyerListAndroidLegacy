package listener;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

/**
 * Created by chao on 8/9/2016.
 */
public class ToDoItemTouchListener implements RecyclerView.OnItemTouchListener {

    private static String TAG = ToDoItemTouchListener.class.getName();

    private int mLastDownX, mLastDownY;

    private int touchSlop;
    private OnItemClickListener mListener;

    private boolean isSingleTapUp = false;

    private boolean isLongPressUp = false;

    private boolean isMove = false;
    private boolean moveCompleted = false;
    private long mDownTime;
    private View mChildView;

    public interface OnItemClickListener {
        void onPointerDown(View view, int position);

        void onPointerUp(View view, int position);

        void onItemClick(View view, int position);

        void onItemLongClick(View view, int position);

        void onMovingItem(View view, int position, float dx, float dy);

        void onMoveCompleted(View view, int position);
    }

    public ToDoItemTouchListener(Context context, OnItemClickListener listener) {
        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mListener = listener;
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
        int x = (int) e.getX();
        int y = (int) e.getY();
        if (mChildView == null) {
            mChildView = rv.findChildViewUnder(e.getX(), e.getY());
        }

        Log.d(TAG, "CURRENT ACTION:" + String.valueOf(MotionEvent.actionToString(e.getAction())));

        switch (e.getAction()) {
            /**
             *  如果是ACTION_DOWN事件，那么记录当前按下的位置，
             *  记录当前的系统时间。
             */
            case MotionEvent.ACTION_DOWN:
                mLastDownX = x;
                mLastDownY = y;
                mDownTime = System.currentTimeMillis();
                isMove = false;
                if (mListener != null) {
                    mListener.onPointerDown(mChildView, rv.getChildLayoutPosition(mChildView));
                }
                break;
            /**
             *  如果是ACTION_MOVE事件，此时根据TouchSlop判断用户在按下的时候是否滑动了，
             *  如果滑动了，那么接下来将不处理点击事件
             */
            case MotionEvent.ACTION_MOVE:
                if (Math.abs(x - mLastDownX) > touchSlop || Math.abs(y - mLastDownY) > touchSlop) {
                    isMove = true;
                    Log.d(TAG, "IS MOVING:" + String.valueOf(isMove));
                }
                break;
            /**
             *  如果是ACTION_UP事件，那么根据isMove标志位来判断是否需要处理点击事件；
             *  根据系统时间的差值来判断是哪种事件，如果按下事件超过1ms，则认为是长按事件，
             *  否则是单击事件。
             */
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (mListener != null) {
                    mListener.onPointerUp(mChildView, rv.getChildLayoutPosition(mChildView));
                }
                //Log.d(TAG, "ACTION_UP onInterceptTouchEvent");
                //Log.d(TAG, "IS MOVING:" + String.valueOf(isMove));
                if (isMove) {
                    moveCompleted = true;
                    break;
                }
                if (e.getAction() == MotionEvent.ACTION_UP) {
                    if (System.currentTimeMillis() - mDownTime > 1000) {
                        isLongPressUp = true;
                    } else {
                        isSingleTapUp = true;
                    }
                    break;
                }

        }
        if (isSingleTapUp) {
            //根据触摸坐标来获取childView
            isSingleTapUp = false;
            if (mChildView != null) {
                //回调mListener#onItemClick方法
                mListener.onItemClick(mChildView, rv.getChildLayoutPosition(mChildView));
                mChildView = null;
                return true;
            }
            return false;
        }
        if (isLongPressUp) {
            isLongPressUp = false;
            if (mChildView != null) {
                mListener.onItemLongClick(mChildView, rv.getChildLayoutPosition(mChildView));
                mChildView = null;
                return true;
            }
            return false;
        }
        if (isMove) {
            if (!moveCompleted) {
                if (mChildView != null) {
                    mListener.onMovingItem(mChildView, rv.getChildLayoutPosition(mChildView), x - mLastDownX, y - mLastDownY);
                }
            } else {
                isMove = false;
                moveCompleted = false;
                //Log.d(TAG, "isMove,and completed");
                if (mChildView != null) {
                    mListener.onMoveCompleted(mChildView, rv.getChildLayoutPosition(mChildView));
                    mChildView = null;
                }
            }
        }
        return false;
    }

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {
    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
    }
}
