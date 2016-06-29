package adapter;

import android.animation.ValueAnimator;
import android.app.Activity;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
import util.ToDoListGlobalLocator;
import view.DrawView;


public class ToDoListAdapter extends RecyclerView.Adapter<ToDoListAdapter.ToDoItemViewHolder> implements View.OnTouchListener {
    //能否操作列表项目
    private boolean mIsEnable = true;

    //当前所在的 Activity
    private Activity mCurrentActivity;

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
    public ToDoListAdapter(ArrayList<ToDo> data, Activity activity, ToDoFragment fragment) {
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
        ToDo currentToDo = mToDosToDisplay.get(position);
        if (currentToDo == null) return;

        holder.textView.setText(currentToDo.getContent());
        holder.id = currentToDo.getID();

        //设置类别
        final int cateID = currentToDo.getCate();
        ToDoCategory category = ToDoListGlobalLocator.GetCategoryByID(cateID);

        if(cateID==0){
            holder.cateCircle.setEllipseColor(AppExtension.getInstance().
                    getResources().getColor(R.color.MyerListBlue));
        }
        else {
            if(category!=null){
                holder.cateCircle.setEllipseColor(category.getColor());
            }
            else{
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
                View dialogView = LayoutInflater.from(mCurrentActivity).inflate(R.layout.dialog_adding_pane, (ViewGroup) mCurrentActivity.findViewById(R.id.dialog_title));

                TextView titleText = (TextView) dialogView.findViewById(R.id.dialog_title_text);
                titleText.setText(mCurrentActivity.getResources().getString(R.string.modify_memo_title));

                mNewMemoText = (EditText) dialogView.findViewById(R.id.newMemoEdit);
                mNewMemoText.setHint(R.string.new_memo_hint);
                mNewMemoText.setText(holder.textView.getText().toString());
                mNewMemoText.setSelection(holder.textView.getText().length());

                RadioGroup radioGroup = (RadioGroup) dialogView.findViewById(R.id.add_pane_radio_legacy);
                radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup radioGroup, int i) {
//                        int index = FindRadioBtnHelper.getCateByRadioBtnID(i);
//                        cateAboutToModify = index;
                    }
                });
//                int currentBtnID = FindRadioBtnHelper.getRadioBtnIDByCate(cateID);
//                if (currentBtnID != 0) {
//                    RadioButton btn = (RadioButton) radioGroup.findViewById(currentBtnID);
//                    if (btn != null) radioGroup.check((currentBtnID));
//                }

                Button okBtn = (Button) dialogView.findViewById(R.id.add_ok_btn);
                okBtn.setText(R.string.ok_btn);
                okBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mDialog.dismiss();
                        String targetID = holder.getID();
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
                        currentItem.setContent(mNewMemoText.getText().toString());
                        currentItem.setCate(cateAboutToModify);

                        //要notify UI 才会更新
                        notifyItemChanged(index);

                        if (!ConfigHelper.ISOFFLINEMODE) {
                            CloudServices.updateContent(
                                    ConfigHelper.getString(mCurrentActivity, "sid"),
                                    ConfigHelper.getString(mCurrentActivity, "access_token"),
                                    targetID, mNewMemoText.getText().toString(),
                                    currentItem.getCate(),
                                    new IRequestCallback() {
                                        @Override
                                        public void onResponse(JSONObject jsonObject) {
                                            ((MainActivity) mCurrentActivity).onUpdateContent(jsonObject);
                                        }
                                    });
                        }
                        else {
                            SerializerHelper.serializeToFile(mCurrentActivity, mToDosToDisplay, SerializerHelper.todosFileName);
                        }
                    }
                });

                Button cancelBtn = (Button) dialogView.findViewById(R.id.add_cancel_btn);
                cancelBtn.setText(R.string.cancel_btn);
                cancelBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (mDialog != null) {
                            mDialog.dismiss();
                        }
                    }
                });

                if (!ConfigHelper.getBoolean(AppExtension.getInstance(), "HandHobbit")) {
                    LinearLayout linearLayout = (LinearLayout) dialogView.findViewById(R.id.dialog_btn_layout);

                    RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
                    layoutParams.setMargins(20, 0, 0, 0);
                    layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                    linearLayout.setLayoutParams(layoutParams);
                }

                android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(mCurrentActivity);
                mDialog = builder.setView((dialogView)).show();
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
            ToDoListGlobalLocator.TodosList.add(todoToAdd);
        }
        else {
            //mToDosToDisplay.add(0, todoToAdd);
            notifyItemInserted(0);
            ToDoListGlobalLocator.TodosList.add(0, todoToAdd);
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

        ToDoListGlobalLocator.DeletedList.add(0, todoToDelete);
        SerializerHelper.serializeToFile(AppExtension.getInstance(), ToDoListGlobalLocator.DeletedList, SerializerHelper.deletedFileName);

        if (ConfigHelper.ISOFFLINEMODE) {
            SerializerHelper.serializeToFile(mCurrentActivity, mToDosToDisplay, SerializerHelper.todosFileName);
        }
        else
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
                }
                else if (scrollLeft > 150 && !mIsInRed) {
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
            }
            else {
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
                                ((MainActivity) mCurrentActivity).onSetDone(jsonObject);
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
        }
        else if (mIsInRed) {
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
        public DrawView cateCircle;

        public ToDoItemViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.todoBlock);
            lineView = (ImageView) itemView.findViewById(R.id.lineView);
            greenImageView = (ImageView) itemView.findViewById(R.id.greenImageView);
            redImageView = (ImageView) itemView.findViewById(R.id.redImageView);
            deleteView = (ImageView) itemView.findViewById(R.id.deleteView);
            relativeLayout = (RelativeLayout) itemView.findViewById(R.id.todo_layout);
            cateCircle = (DrawView) itemView.findViewById(R.id.cateCircle);
        }

        public String getID() {
            return id;
        }

        public void setID(String id) {
            this.id = id;
        }
    }

}
