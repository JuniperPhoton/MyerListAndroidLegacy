package adapter;

import android.animation.ValueAnimator;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.juniperphoton.myerlistandroid.R;

import org.json.JSONObject;

import java.util.ArrayList;

import activity.MainActivity;
import api.CloudServices;
import fragment.ToDoFragment;
import interfaces.IRequestCallback;
import model.ToDoCategory;
import util.ConfigHelper;
import util.AppExtension;
import util.SerializerHelper;
import model.ToDo;
import util.GlobalListLocator;
import view.CircleView;


public class ToDoListAdapter extends RecyclerView.Adapter<ToDoListAdapter.ToDoItemViewHolder> implements View.OnTouchListener {
    //能否操作列表项目
    private boolean mIsEnable = true;

    //当前所在的 Activity
    private MainActivity mCurrentActivity;

    //当前所在的 Fragment
    private ToDoFragment mCurrentFragment;

    //表示的列表
    private ArrayList<ToDo> mToDosToDisplay;

    //当前操控的项目
    private ToDo mCurrentToDo = null;

    int lastX;

    private boolean mIsInGreen = false;
    private boolean mIsInRed = false;
    private boolean mIsSwiping = false;

    //修改的时候弹出的对话框
    private AlertDialog mDialog;

    //修改的时候文本框
    private EditText mNewMemoText;

    private int cateAboutToModify = 0;

    private int deletedItemsCount = 0;

    //构造函数
    //传入当前的列表
    public ToDoListAdapter(ArrayList<ToDo> data, MainActivity activity, ToDoFragment fragment) {
        mCurrentActivity = activity;
        mToDosToDisplay = data;
        mCurrentFragment = fragment;
    }

    @Override
    public ToDoItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_todo, parent, false);
        return new ToDoItemViewHolder(v);
    }

    //创建每一项的布局
    @Override
    public void onBindViewHolder(final ToDoItemViewHolder holder, final int position) {
        //设置文字
        final ToDo currentToDoItem = mToDosToDisplay.get(position);
        if (currentToDoItem == null) return;

        holder.textView.setText(currentToDoItem.getContent());
        holder.id = currentToDoItem.getID();

        //设置类别
        final int cateID = currentToDoItem.getCate();
        ToDoCategory category = GlobalListLocator.GetCategoryByCateID(cateID);

        if (cateID == 0) {
            holder.cateCircle.setEllipseColor(AppExtension.getInstance().
                    getResources().getColor(R.color.MyerListBlue));
        } else {
            if (category != null) {
                holder.cateCircle.setEllipseColor(category.getColor());
            } else {
                holder.cateCircle.setEllipseColor(AppExtension.getInstance().
                        getResources().getColor(R.color.MyerListBlue));
            }
        }

        holder.setID(mToDosToDisplay.get(position).getID());

        //设置是否完成
        if (!mToDosToDisplay.get(position).getIsDone()) {
            holder.lineView.setVisibility(View.GONE);
        }

        //设置删除
        holder.deleteView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteToDo(holder.getID());
            }
        });

        //设置点击修改
        holder.relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mIsSwiping) {
                    return;
                }
                mCurrentActivity.setupAddingPaneForModify(currentToDoItem);
            }
        });

        holder.relativeLayout.setOnTouchListener(this);
        holder.relativeLayout.setTag(holder.getID());

        holder.greenImageView.setAlpha(1f);
        holder.redImageView.setAlpha(1f);
        holder.greenImageView.setVisibility(View.INVISIBLE);
        holder.redImageView.setVisibility(View.INVISIBLE);
        holder.relativeLayout.scrollTo(0, 0);
    }

    public void addToDo(ToDo todoToAdd) {
        if (todoToAdd == null) return;

        if (ConfigHelper.getBoolean(AppExtension.getInstance(), "AddToBottom")) {
            //mToDosToDisplay.add(todoToAdd);
            notifyItemInserted(mToDosToDisplay.size() - 1);
            GlobalListLocator.TodosList.add(todoToAdd);
        } else {
            //mToDosToDisplay.add(0, todoToAdd);
            notifyItemInserted(0);
            GlobalListLocator.TodosList.add(0, todoToAdd);
        }
        SerializerHelper.serializeToFile(mCurrentActivity, mToDosToDisplay, SerializerHelper.todosFileName);
    }

    public void deleteToDo(String id) {
        int index = 0;
        ToDo todoToDelete = null;
        for (int i = 0; i < mToDosToDisplay.size(); i++) {
            ToDo s = mToDosToDisplay.get(i);
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
        mToDosToDisplay.remove(todoToDelete);

        GlobalListLocator.DeletedList.add(0, todoToDelete);
        SerializerHelper.serializeToFile(AppExtension.getInstance(), GlobalListLocator.DeletedList, SerializerHelper.deletedFileName);

        if (ConfigHelper.ISOFFLINEMODE) {
            SerializerHelper.serializeToFile(mCurrentActivity, mToDosToDisplay, SerializerHelper.todosFileName);
        } else
            CloudServices.setDelete(ConfigHelper.getString(AppExtension.getInstance(), "sid"),
                    ConfigHelper.getString(AppExtension.getInstance(), "access_token"),
                    todoToDelete.getID(),
                    new IRequestCallback() {
                        @Override
                        public void onResponse(JSONObject jsonObject) {
                            ((MainActivity) mCurrentActivity).onDelete(jsonObject);
                        }
                    });
    }

    public void updateContent(ToDo toDo) {
        String targetID = toDo.getID();
        int index = 0;

        //根据ID 找到项目
        for (int i = 0; i < mToDosToDisplay.size(); i++) {
            ToDo s = mToDosToDisplay.get(i);
            if (s.getID().equals(targetID)) {
                index = i;
                break;
            }
        }

        //更新项目
        ToDo currentItem = mToDosToDisplay.get(index);
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
                            mCurrentActivity.onUpdateContent(jsonObject);
                        }
                    });
        } else {
            SerializerHelper.serializeToFile(mCurrentActivity, mToDosToDisplay, SerializerHelper.todosFileName);
        }
    }

    @Override
    public int getItemCount() {
        return mToDosToDisplay != null ? mToDosToDisplay.size() : 0;
    }

    public ArrayList<ToDo> getListSrc() {
        return mToDosToDisplay;
    }

    public boolean onTouch(final View v, MotionEvent event) {
        RelativeLayout root = (RelativeLayout) v;

        int scrollLeft;
        String id = (String) v.getTag();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:

                lastX = (int) event.getRawX();
                break;

            case MotionEvent.ACTION_MOVE:

                if (!mIsEnable)
                    break;

                mIsSwiping = true;

                int dx = (int) event.getRawX() - lastX;

                scrollLeft = v.getScrollX();

                v.scrollBy(-dx, 0);

                if (scrollLeft < -20) {
                    mCurrentFragment.disableRefresh();
                }

                lastX = (int) event.getRawX();

                if (scrollLeft < -150 && !mIsInGreen) {
                    playColorChangeAnimation((ImageView) root.findViewById(R.id.greenImageView), true);
                } else if (scrollLeft > 150 && !mIsInRed) {
                    playColorChangeAnimation((ImageView) root.findViewById(R.id.redImageView), false);
                }

                break;
            case MotionEvent.ACTION_UP:

                onMoveComplete(v, v.getScrollX(), id);

                break;

            case MotionEvent.ACTION_CANCEL:

                onMoveComplete(v, v.getScrollX(), id);
                break;
        }

        return false;
    }

    private void onMoveComplete(View v, float scrollLeft, String id) {
        int index = 0;
        for (int i = 0; i < mToDosToDisplay.size(); i++) {
            ToDo s = mToDosToDisplay.get(i);
            if (s.getID().equals(id)) {
                mCurrentToDo = s;
                index = i;
                break;
            }
        }
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
                CloudServices.setDone(ConfigHelper.getString(AppExtension.getInstance(), "sid"),
                        ConfigHelper.getString(AppExtension.getInstance(), "access_token"),
                        id,
                        mCurrentToDo.getIsDone() ? "1" : "0",
                        new IRequestCallback() {
                            @Override
                            public void onResponse(JSONObject jsonObject) {
                                mCurrentActivity.onSetDone(jsonObject);
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
        SerializerHelper.serializeToFile(AppExtension.getInstance(), mToDosToDisplay, SerializerHelper.todosFileName);
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
                    mCurrentFragment.enableRefresh();
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

    public static class ToDoItemViewHolder extends RecyclerView.ViewHolder {
        private String id;
        public TextView textView;
        public ImageView lineView;
        public ImageView deleteView;
        public RelativeLayout relativeLayout;
        public ImageView greenImageView;
        public ImageView redImageView;
        public CircleView cateCircle;

        public ToDoItemViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.todoBlock);
            lineView = (ImageView) itemView.findViewById(R.id.lineView);
            greenImageView = (ImageView) itemView.findViewById(R.id.greenImageView);
            redImageView = (ImageView) itemView.findViewById(R.id.redImageView);
            deleteView = (ImageView) itemView.findViewById(R.id.deleteView);
            relativeLayout = (RelativeLayout) itemView.findViewById(R.id.todo_layout);
            cateCircle = (CircleView) itemView.findViewById(R.id.cateCircle);
        }

        public String getID() {
            return id;
        }

        public void setID(String id) {
            this.id = id;
        }
    }

}
