package adapter;

import android.animation.ValueAnimator;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.chad.library.adapter.base.BaseItemDraggableAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.juniperphoton.jputils.LocalSettingHelper;
import com.juniperphoton.jputils.SerializerHelper;
import com.juniperphoton.myerlistandroid.R;

import org.json.JSONObject;

import java.util.ArrayList;

import activity.MainActivity;
import api.CloudServices;
import fragment.ToDoFragment;
import interfaces.IRefresh;
import interfaces.IRequestCallback;
import model.ToDoCategory;
import util.ConfigHelper;
import util.AppExtension;
import model.ToDo;
import util.GlobalListLocator;
import util.SerializationName;
import viewholder.ToDoItemViewHolder;


public class ToDoListAdapter extends BaseItemDraggableAdapter<ToDo> implements View.OnTouchListener {
    private boolean mIsEnable = true;

    private MainActivity mCurrentActivity;
    private IRefresh mIRefreshCallback;

    private ToDo mCurrentToDo = null;

    private int lastX;

    private boolean mIsInGreen = false;
    private boolean mIsInRed = false;
    private boolean mIsSwiping = false;

    public ToDoListAdapter(ArrayList<ToDo> data) {
        super(R.layout.row_todo, data);
    }

    public ToDoListAdapter(ArrayList<ToDo> data, MainActivity activity, ToDoFragment fragment) {
        super(R.layout.row_todo, data);
        mCurrentActivity = activity;
        mIRefreshCallback = (IRefresh) fragment;
        mContext = activity;
    }

    @Override
    public ToDoItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_todo, parent, false);
        return new ToDoItemViewHolder(v, viewType);
    }

    @Override
    protected void convert(BaseViewHolder baseViewHolder, final ToDo currentToDoItem) {
        final ToDoItemViewHolder holder = (ToDoItemViewHolder) baseViewHolder;

        holder.mTextView.setText(currentToDoItem.getContent());
        holder.setID(currentToDoItem.getID());

        final int cateID = currentToDoItem.getCate();
        ToDoCategory category = GlobalListLocator.GetCategoryByCateID(cateID);

        if (cateID == 0) {
            holder.mCateCircle.setColor(ContextCompat.getColor(mContext, R.color.MyerListBlue));
        } else {
            if (category != null) {
                holder.mCateCircle.setColor(category.getColor());
            } else {
                holder.mCateCircle.setColor(ContextCompat.getColor(mContext, R.color.MyerListBlue));
            }
        }

        if (!currentToDoItem.getIsDone()) {
            holder.mLineView.setVisibility(View.GONE);
        }

        holder.mRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mIsSwiping) {
                    return;
                }
                int[] location = new int[2];
                holder.mCateCircle.getLocationOnScreen(location);
                mCurrentActivity.setupAddingPaneForModifyAndShow(currentToDoItem,
                        new int[]{location[0] + holder.mCateCircle.getWidth() / 2, location[1] + holder.mCateCircle.getHeight() / 2});
            }
        });

        holder.mRelativeLayout.setOnTouchListener(this);
        holder.mRelativeLayout.setTag(holder.getID());
        holder.mGreenImageView.setAlpha(1f);
        holder.mRedImageView.setAlpha(1f);
        holder.mGreenImageView.setVisibility(View.INVISIBLE);
        holder.mRedImageView.setVisibility(View.INVISIBLE);
        holder.mRelativeLayout.scrollTo(0, 0);
    }

    @Override
    public int getItemCount() {
        return mData != null ? mData.size() : 0;
    }

    public void addToDo(ToDo todoToAdd) {
        if (todoToAdd == null) return;

        if (LocalSettingHelper.getBoolean(AppExtension.getInstance(), "AddToBottom")) {
            //mToDosToDisplay.add(todoToAdd);
            notifyItemInserted(mData.size() - 1);
            GlobalListLocator.TodosList.add(todoToAdd);
        } else {
            //mToDosToDisplay.add(0, todoToAdd);
            notifyItemInserted(0);
            GlobalListLocator.TodosList.add(0, todoToAdd);
        }
        SerializerHelper.serializeToFile(mContext, mData, SerializationName.TODOS_FILE_NAME);
    }

    public void deleteToDo(String id) {
        int index = 0;
        ToDo todoToDelete = null;
        for (int i = 0; i < mData.size(); i++) {
            ToDo s = mData.get(i);
            if (s.getID().equals(id)) {
                todoToDelete = s;
                index = i;
                break;
            }
        }
        if (todoToDelete != null)
            deleteToDo(index, todoToDelete);
    }

    private void deleteToDo(int index, ToDo todoToDelete) {
        notifyItemRemoved(index);
        mData.remove(todoToDelete);

        GlobalListLocator.DeletedList.add(0, todoToDelete);
        SerializerHelper.serializeToFile(AppExtension.getInstance(), GlobalListLocator.DeletedList, SerializationName.DELETED_FILE_NAME);

        if (ConfigHelper.ISOFFLINEMODE) {
            SerializerHelper.serializeToFile(mContext, mData, SerializationName.TODOS_FILE_NAME);
        } else
            CloudServices.setDelete(LocalSettingHelper.getString(AppExtension.getInstance(), "sid"),
                    LocalSettingHelper.getString(AppExtension.getInstance(), "access_token"),
                    todoToDelete.getID(),
                    new IRequestCallback() {
                        @Override
                        public void onResponse(JSONObject jsonObject) {
                            //((MainActivity) mCurrentActivity).onDelete(jsonObject);
                        }
                    });
    }

    public void updateContent(ToDo toDo) {
        String targetID = toDo.getID();
        int index = 0;

        //根据ID 找到项目
        for (int i = 0; i < mData.size(); i++) {
            ToDo s = mData.get(i);
            if (s.getID().equals(targetID)) {
                index = i;
                break;
            }
        }

        //更新项目
        ToDo currentItem = mData.get(index);
        currentItem.setContent(toDo.getContent());
        currentItem.setCate(toDo.getCate());

        //要notify UI 才会更新
        notifyItemChanged(index);

        if (!ConfigHelper.ISOFFLINEMODE) {
            CloudServices.updateContent(
                    ConfigHelper.getSid(),
                    ConfigHelper.getAccessToken(),
                    targetID,
                    currentItem.getContent(),
                    currentItem.getCate(),
                    new IRequestCallback() {
                        @Override
                        public void onResponse(JSONObject jsonObject) {
                            //mCurrentActivity.onUpdateContent(jsonObject);
                        }
                    });
        } else {
            SerializerHelper.serializeToFile(mContext, mData, SerializationName.TODOS_FILE_NAME);
        }
    }

    public boolean onTouch(final View view, MotionEvent event) {
        RelativeLayout rootLayout = (RelativeLayout) view;

        int scrollLeft;
        String id = (String) view.getTag();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:

                lastX = (int) event.getRawX();
                break;

            case MotionEvent.ACTION_MOVE:

                if (!mIsEnable)
                    break;

                mIsSwiping = true;

                int dx = (int) event.getRawX() - lastX;

                scrollLeft = view.getScrollX();

                view.scrollBy(-dx, 0);

                if (scrollLeft < -20) {
                    if (mIRefreshCallback != null) {
                        mIRefreshCallback.disableRefresh();
                    }
                }

                lastX = (int) event.getRawX();

                if (scrollLeft < -150 && !mIsInGreen) {
                    playColorChangeAnimation((ImageView) rootLayout.findViewById(R.id.greenImageView), true);
                } else if (scrollLeft > 150 && !mIsInRed) {
                    playColorChangeAnimation((ImageView) rootLayout.findViewById(R.id.redImageView), false);
                }

                break;
            case MotionEvent.ACTION_UP:

                onMoveComplete(view, view.getScrollX(), id);

                break;

            case MotionEvent.ACTION_CANCEL:

                onMoveComplete(view, view.getScrollX(), id);
                break;
        }

        return false;
    }

    private void onMoveComplete(View v, float scrollLeft, String id) {
        if (mCurrentToDo == null)
            return;

        //Finish
        if (scrollLeft < -150) {

            ImageView lineview = (ImageView) v.findViewById(R.id.lineView);
            if (mCurrentToDo.getIsDone()) {
                lineview.setVisibility(View.GONE);
                mCurrentToDo.setIsDone(false);
            } else {
                lineview.setVisibility(View.VISIBLE);
                mCurrentToDo.setIsDone(true);
            }

            if (!ConfigHelper.ISOFFLINEMODE) {
                CloudServices.setDone(LocalSettingHelper.getString(AppExtension.getInstance(), "sid"),
                        LocalSettingHelper.getString(AppExtension.getInstance(), "access_token"),
                        id,
                        mCurrentToDo.getIsDone() ? "1" : "0",
                        new IRequestCallback() {
                            @Override
                            public void onResponse(JSONObject jsonObject) {
                                //mCurrentActivity.onSetDone(jsonObject);
                            }
                        });
            }
        }
        //Delete
        else if (scrollLeft > 150) {
            deleteToDo(mCurrentToDo.getID());
        }

        if (mIsInGreen) {
            playFadebackAnimation((ImageView) v.findViewById(R.id.greenImageView), true);
        } else if (mIsInRed) {
            playFadebackAnimation((ImageView) v.findViewById(R.id.redImageView), false);
        }

        playGoBackAnimation(v, scrollLeft);
        SerializerHelper.serializeToFile(AppExtension.getInstance(), mData, SerializationName.TODOS_FILE_NAME);
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
                    if (mIRefreshCallback != null) {
                        mIRefreshCallback.enableRefresh();
                    }
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
            mIsInGreen = true;
        else
            mIsInRed = true;
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
                    mIsInGreen = false;
                else
                    mIsInRed = false;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        animationSet.addAnimation(alphaAnimation);
        v.startAnimation(animationSet);
    }
}
