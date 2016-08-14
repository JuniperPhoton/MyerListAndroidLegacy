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
import android.widget.TextView;

import com.chad.library.adapter.base.BaseItemDraggableAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.juniperphoton.jputils.LocalSettingHelper;
import com.juniperphoton.jputils.SerializerHelper;
import com.juniperphoton.myerlistandroid.R;
import com.orhanobut.logger.Logger;

import org.json.JSONObject;

import java.util.ArrayList;

import activity.MainActivity;
import api.CloudServices;
import fragment.ToDoFragment;
import interfaces.IRequestCallback;
import model.ToDoCategory;
import util.ConfigHelper;
import common.AppExtension;
import model.ToDo;
import util.GlobalListLocator;
import util.SerializationName;
import view.CircleView;


public class ToDoListAdapter extends BaseItemDraggableAdapter<ToDo> implements View.OnTouchListener {
    private static String TAG = ToDoListAdapter.class.getName();

    private MainActivity mActivity;
    private ToDoFragment mFragment;

    private ToDo mCurrentToDo = null;
    private ArrayList<ToDo> mData;

    private int lastX;

    private boolean mTurnGreen = false;
    private boolean mTurnRed = false;
    private boolean mIsSwiping = false;

    public ToDoListAdapter(ArrayList<ToDo> data, MainActivity activity, ToDoFragment fragment) {
        super(data);
        mActivity = activity;
        mFragment = fragment;
        mData = data;
    }

    @Override
    public ToDoItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_todo, parent, false);
        return new ToDoItemViewHolder(v);
    }

    @Override
    public void convert(BaseViewHolder helper, final ToDo currentToDoItem) {

        final ToDoItemViewHolder holder = (ToDoItemViewHolder) helper;
        final ToDo toDoItem = mData.get(holder.getAdapterPosition());

        holder.mTextView.setText(toDoItem.getContent());
        holder.setID(toDoItem.getID());

        final int cateID = toDoItem.getCate();
        ToDoCategory category = GlobalListLocator.getCategoryByCateID(cateID);

        if (cateID == 0) {
            holder.mCateCircle.setColor(ContextCompat.getColor(mActivity, R.color.MyerListBlue));
        } else {
            if (category != null) {
                holder.mCateCircle.setColor(category.getColor());
            } else {
                holder.mCateCircle.setColor(ContextCompat.getColor(mActivity, R.color.MyerListBlue));
            }
        }

        if (!toDoItem.getIsDone()) {
            holder.mLineView.setVisibility(View.GONE);
        }

        holder.mRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mIsSwiping) {
                    return;
                }

                StringBuffer sb = new StringBuffer();
                for (ToDo todo : mData) {
                    sb.append(todo.getContent()).append(",");
                }
                Logger.d(sb.toString());

                int[] location = new int[2];
                holder.mCateCircle.getLocationOnScreen(location);
                mActivity.setupAddingPaneForModifyAndShow(toDoItem,
                        new int[]{location[0] + holder.mCateCircle.getWidth() / 2, location[1] + holder.mCateCircle.getHeight() / 2});
            }
        });

        holder.mRelativeLayout.setOnTouchListener(this);
        holder.mRelativeLayout.setTag(holder.getID());
    }

    @Override
    public int getItemCount() {
        return mData != null ? mData.size() : 0;
    }

    public boolean onTouch(final View view, MotionEvent event) {
        RelativeLayout rootLayout = (RelativeLayout) view;

        String id = (String) view.getTag();
        findDataById(id);

        int scrollingX;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:

                lastX = (int) event.getRawX();
                break;
            case MotionEvent.ACTION_MOVE:
                mIsSwiping = true;

                int dx = (int) event.getRawX() - lastX;

                scrollingX = view.getScrollX();

                view.scrollBy(-dx, 0);

                if (scrollingX < -20) {
                    if (mFragment != null) {
                        mFragment.disableRefresh();
                    }
                }

                lastX = (int) event.getRawX();

                if (scrollingX < -150 && !mTurnGreen) {
                    playColorChangeAnimation((ImageView) rootLayout.findViewById(R.id.greenImageView), true);
                } else if (scrollingX > 150 && !mTurnRed) {
                    playColorChangeAnimation((ImageView) rootLayout.findViewById(R.id.redImageView), false);
                }

                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_OUTSIDE:
            case MotionEvent.ACTION_UP:

                onMoveComplete(view, view.getScrollX());

                break;
        }

        return false;
    }

    private void onMoveComplete(View v, float scrollLeft) {
        if (mCurrentToDo == null)
            return;

        if (mTurnGreen) {
            playFadebackAnimation((ImageView) v.findViewById(R.id.greenImageView), true);
        } else if (mTurnRed) {
            playFadebackAnimation((ImageView) v.findViewById(R.id.redImageView), false);
        }

        playGoBackAnimation(v, scrollLeft);

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
                        LocalSettingHelper.getString(AppExtension.getInstance(), "access_token"), mCurrentToDo.getID(),
                        mCurrentToDo.getIsDone() ? "1" : "0",
                        new IRequestCallback() {
                            @Override
                            public void onResponse(JSONObject jsonObject) {
                                //mActivity.onSetDone(jsonObject);
                            }
                        });
            }
        }
        //Delete
        else if (scrollLeft > 150) {
            deleteToDo(mCurrentToDo.getID());
        }

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
                    if (mFragment != null) {
                        mFragment.enableRefresh();
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

    private void findDataById(String id) {
        int location = -1;
        for (int i = 0; i < mData.size(); i++) {
            if (mData.get(i).getID().equals(id)) {
                location = i;
            }
        }
        if (location != -1) {
            mCurrentToDo = mData.get(location);
        }
    }

    private void deleteToDoInternal(int index) {

        ToDo todoToDelete = mData.get(index);

        mData.remove(index);
        notifyItemRemoved(index);

        GlobalListLocator.deleteToDo(todoToDelete.getID());
        GlobalListLocator.DeletedList.add(0, todoToDelete);
        GlobalListLocator.saveData();

        if (!ConfigHelper.ISOFFLINEMODE) {
            CloudServices.setDelete(LocalSettingHelper.getString(AppExtension.getInstance(), "sid"),
                    LocalSettingHelper.getString(AppExtension.getInstance(), "access_token"),
                    todoToDelete.getID(),
                    new IRequestCallback() {
                        @Override
                        public void onResponse(JSONObject jsonObject) {
                            //((MainActivity) mActivity).onDelete(jsonObject);
                        }
                    });
        }
    }

    public void addToDo(ToDo todoToAdd) {
        if (todoToAdd == null) return;

        if (LocalSettingHelper.getBoolean(AppExtension.getInstance(), "AddToBottom")) {
            notifyItemInserted(mData.size() - 1);
            GlobalListLocator.TodosList.add(todoToAdd);
        } else {
            notifyItemInserted(0);
            GlobalListLocator.TodosList.add(0, todoToAdd);
        }
        SerializerHelper.serializeToFile(mActivity, mData, SerializationName.TODOS_FILE_NAME);
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
        if (todoToDelete != null) {
            deleteToDoInternal(index);
        }
    }

    public void updateToDo(ToDo toDo) {
        GlobalListLocator.updateContent(toDo);

        int position = findToDoInData(toDo.getID());

        mData.get(position).setContent(toDo.getContent());
        mData.get(position).setCate(toDo.getCate());

        notifyItemChanged(position);

        StringBuffer sb = new StringBuffer();
        for (ToDo todo : mData) {
            sb.append(todo.getContent()).append(",");
        }
        Logger.d(sb.toString());

        if (!ConfigHelper.ISOFFLINEMODE) {
            CloudServices.updateContent(
                    ConfigHelper.getSid(),
                    ConfigHelper.getAccessToken(),
                    toDo.getID(),
                    toDo.getContent(),
                    toDo.getCate(),
                    new IRequestCallback() {
                        @Override
                        public void onResponse(JSONObject jsonObject) {
                            mActivity.onUpdateContent(jsonObject);
                        }
                    });
        } else {
            SerializerHelper.serializeToFile(mActivity, mData, SerializationName.TODOS_FILE_NAME);
        }

        //mActivity.updateListByCategory();
    }

    private int findToDoInData(String id) {
        int index = 0;
        for (int i = 0; i < mData.size(); i++) {
            if (mData.get(i).getID().equals(id)) {
                index = i;
                break;
            }
        }
        return index;
    }

    public ArrayList<ToDo> getData() {
        return mData;
    }

    public class ToDoItemViewHolder extends BaseViewHolder {

        private String mId;
        public TextView mTextView;
        public ImageView mLineView;
        public RelativeLayout mRelativeLayout;
        public ImageView mGreenImageView;
        public ImageView mRedImageView;
        public CircleView mCateCircle;

        public ToDoItemViewHolder(View itemView) {
            super(itemView);
            mTextView = (TextView) itemView.findViewById(R.id.todoBlock);
            mLineView = (ImageView) itemView.findViewById(R.id.lineView);
            mGreenImageView = (ImageView) itemView.findViewById(R.id.greenImageView);
            mRedImageView = (ImageView) itemView.findViewById(R.id.redImageView);
            mRelativeLayout = (RelativeLayout) itemView.findViewById(R.id.todo_layout);
            mCateCircle = (CircleView) itemView.findViewById(R.id.cateCircle);
        }

        public String getID() {
            return mId;
        }

        public void setID(String id) {
            this.mId = id;
        }
    }
}
