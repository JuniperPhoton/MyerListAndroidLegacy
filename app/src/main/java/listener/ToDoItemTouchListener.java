package listener;

import android.animation.ValueAnimator;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

import com.juniperphoton.myerlistandroid.R;

/**
 * Created by chao on 8/9/2016.
 */
public class ToDoItemTouchListener implements RecyclerView.OnItemTouchListener {

    private View childView;
    private RecyclerView touchView;
    public ToDoItemTouchListener() {

    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
        childView = rv.findChildViewUnder(e.getX(), e.getY());
        touchView = rv;
        return true;
    }

    private boolean mTurnGreen = false;
    private boolean mTurnRed = false;
    private boolean mIsSwiping = false;
    private int startingX;

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent event) {
        //String id = (String) view.getTag();
        //findDataById(id);

        int scrollingX;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:

                startingX = (int) event.getRawX();
                break;

            case MotionEvent.ACTION_MOVE:
                mIsSwiping = true;

                scrollingX = childView.getScrollX();
                int dx = (int) event.getRawX() - startingX;


                childView.scrollBy(-dx, 0);

//                if (scrollingX < -20) {
//                    if (mIRefreshCallback != null) {
//                        mIRefreshCallback.disableRefresh();
//                    }
//                }

                startingX = (int) event.getRawX();

                if (scrollingX < -150 && !mTurnGreen) {
                    playColorChangeAnimation((ImageView) childView.findViewById(R.id.greenImageView), true);
                } else if (scrollingX > 150 && !mTurnRed) {
                    playColorChangeAnimation((ImageView) childView.findViewById(R.id.redImageView), false);
                }

                break;
            case MotionEvent.ACTION_UP:

                //onMoveComplete(childView, childView.getScrollX());

                break;

            case MotionEvent.ACTION_CANCEL:

                //onMoveComplete(childView, childView.getScrollX());

                break;
        }

    }

    private void onMoveComplete(View v, float scrollLeft) {
//        if (mCurrentToDo == null)
//            return;
//
//        //Finish
//        if (scrollLeft < -150) {
//
//            ImageView lineview = (ImageView) v.findViewById(R.id.lineView);
//            if (mCurrentToDo.getIsDone()) {
//                lineview.setVisibility(View.GONE);
//                mCurrentToDo.setIsDone(false);
//            } else {
//                lineview.setVisibility(View.VISIBLE);
//                mCurrentToDo.setIsDone(true);
//            }
//
//            if (!ConfigHelper.ISOFFLINEMODE) {
//                CloudServices.setDone(LocalSettingHelper.getString(AppExtension.getInstance(), "sid"),
//                        LocalSettingHelper.getString(AppExtension.getInstance(), "access_token"), mCurrentToDo.getID(),
//                        mCurrentToDo.getIsDone() ? "1" : "0",
//                        new IRequestCallback() {
//                            @Override
//                            public void onResponse(JSONObject jsonObject) {
//                                //mCurrentActivity.onSetDone(jsonObject);
//                            }
//                        });
//            }
//        }
//        //Delete
//        else if (scrollLeft > 150) {
//            deleteToDo(mCurrentToDo.getID());
//        }
//
//        if (mTurnGreen) {
//            playFadebackAnimation((ImageView) v.findViewById(R.id.greenImageView), true);
//        } else if (mTurnRed) {
//            playFadebackAnimation((ImageView) v.findViewById(R.id.redImageView), false);
//        }
//
//        playGoBackAnimation(v, scrollLeft);
//        SerializerHelper.serializeToFile(AppExtension.getInstance(), mData, SerializationName.TODOS_FILE_NAME);
    }

    private void playGoBackAnimation(final View v, final float left) {
        ValueAnimator valueAnimator = ValueAnimator.ofInt((int) left, 0);
        valueAnimator.setDuration(700);
        valueAnimator.setInterpolator(new DecelerateInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                v.scrollTo((int) valueAnimator.getAnimatedValue(), 0);
                if (Math.abs((int) valueAnimator.getAnimatedValue()) < 10) {
//                    if (mIRefreshCallback != null) {
//                        mIRefreshCallback.enableRefresh();
//                    }
                    mIsSwiping = false;
                }
            }
        });
        valueAnimator.start();
    }

    private void playColorChangeAnimation(final ImageView v, boolean isGreen) {
        v.setAlpha(1f);
        AnimationSet animationSet = new AnimationSet(false);

        AlphaAnimation alphaAnimation = new AlphaAnimation(0.1f, 1.0f);
        alphaAnimation.setDuration(700);
        alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                v.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        animationSet.addAnimation(alphaAnimation);
        v.startAnimation(animationSet);

        if (isGreen)
            mTurnGreen = true;
        else
            mTurnRed = true;
    }

    private void playFadebackAnimation(final ImageView v, final boolean isGreen) {
        AnimationSet animationSet = new AnimationSet(false);
        AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, 0.0f);
        alphaAnimation.setDuration(700);
        alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                v.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                v.setVisibility(View.INVISIBLE);
                if (isGreen)
                    mTurnGreen = false;
                else
                    mTurnRed = false;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        animationSet.addAnimation(alphaAnimation);
        v.startAnimation(animationSet);
    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

    }
}
