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

import java.util.ArrayList;

import fragment.ToDoFragment;
import util.ConfigHelper;
import util.AppExtension;
import util.FindRadioBtnHelper;
import util.PostHelper;
import util.SerializerHelper;
import model.ToDo;
import util.ToDoListRef;


public class ToDoListAdapter extends RecyclerView.Adapter<ToDoListAdapter.ToDoItemViewHolder> implements View.OnTouchListener {
    //能否操作列表项目
    private boolean mIsEnable = true;
    private boolean mCanChangeCate = true;

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
        holder.textView.setText(mToDosToDisplay.get(position).getContent());
        holder.id = mToDosToDisplay.get(position).getID();

        //设置类别
        final int cate = mToDosToDisplay.get(position).getCate();
        switch (cate) {
            case 0:
                holder.cateImage.setImageResource(R.drawable.cate_default);
                break;
            case 1:
                holder.cateImage.setImageResource(R.drawable.cate_work);
                break;
            case 2:
                holder.cateImage.setImageResource(R.drawable.cate_life);
                break;
            case 3:
                holder.cateImage.setImageResource(R.drawable.cate_family);
                break;
            case 4:
                holder.cateImage.setImageResource(R.drawable.cate_enter);
                break;
        }

        holder.cateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!mCanChangeCate) return;

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

                ToDo currentItem = mToDosToDisplay.get(index);
                int cate = currentItem.getCate();
                currentItem.setCate(++cate);
                if (cate >= 5)
                    currentItem.setCate(0);
                switch (currentItem.getCate()) {
                    case 0:
                        holder.cateImage.setImageResource(R.drawable.cate_default);
                        break;
                    case 1:
                        holder.cateImage.setImageResource(R.drawable.cate_work);
                        break;
                    case 2:
                        holder.cateImage.setImageResource(R.drawable.cate_life);
                        break;
                    case 3:
                        holder.cateImage.setImageResource(R.drawable.cate_family);
                        break;
                    case 4:
                        holder.cateImage.setImageResource(R.drawable.cate_enter);
                        break;
                }

                //要notify UI 才会更新
                notifyItemChanged(index);

                if (!ConfigHelper.ISOFFLINEMODE) {
                    PostHelper.UpdateContent(mCurrentActivity, ConfigHelper.getString(mCurrentActivity, "sid"), targetID, currentItem.getContent(), cate);
                }
                else {
                    SerializerHelper.SerializeToFile(mCurrentActivity, mToDosToDisplay, SerializerHelper.todosFileName);
                }
            }
        });

        holder.setID(mToDosToDisplay.get(position).getID());

        //设置是否完成
        if (!mToDosToDisplay.get(position).getIsDone()) {
            holder.lineView.setVisibility(View.GONE);
        }

        //设置删除
        holder.deleteView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DeleteToDo(holder.getID());
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
                        int index = FindRadioBtnHelper.GetCateByRadioBtnID(i);
                        cateAboutToModify = index;
                    }
                });
                int currentBtnID = FindRadioBtnHelper.GetRadioBtnIDByCate(cate);
                if (currentBtnID != 0) {
                    RadioButton btn = (RadioButton) radioGroup.findViewById(currentBtnID);
                    if (btn != null) radioGroup.check((currentBtnID));
                }

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
                            PostHelper.UpdateContent(mCurrentActivity, ConfigHelper.getString(mCurrentActivity, "sid"), targetID, mNewMemoText.getText().toString(), currentItem.getCate());
                        }
                        else {
                            SerializerHelper.SerializeToFile(mCurrentActivity, mToDosToDisplay, SerializerHelper.todosFileName);
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

    public void SetCanChangeCate(boolean canChange) {
        mCanChangeCate = canChange;
    }

    public void AddToDo(ToDo todoToAdd) {
        if (todoToAdd == null) return;

        if (ConfigHelper.getBoolean(AppExtension.getInstance(), "AddToBottom")) {
            //mToDosToDisplay.add(todoToAdd);
            notifyItemInserted(mToDosToDisplay.size() - 1);
            ToDoListRef.TodosList.add(todoToAdd);
        }
        else {
            //mToDosToDisplay.add(0, todoToAdd);
            notifyItemInserted(0);
            ToDoListRef.TodosList.add(0, todoToAdd);
        }
        SerializerHelper.SerializeToFile(mCurrentActivity, mToDosToDisplay, SerializerHelper.todosFileName);
    }

    public void DeleteToDo(String id) {
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
            DeleteToDo(index, todoToDelete);
    }

    private void DeleteToDo(int index, ToDo todoToDelete) {
        notifyItemRemoved(index);
        mToDosToDisplay.remove(todoToDelete);

        ToDoListRef.DeletedList.add(0, todoToDelete);
        SerializerHelper.SerializeToFile(AppExtension.getInstance(), ToDoListRef.DeletedList, SerializerHelper.deletedFileName);

        if (ConfigHelper.ISOFFLINEMODE) {
            SerializerHelper.SerializeToFile(mCurrentActivity, mToDosToDisplay, SerializerHelper.todosFileName);
        }
        else
            PostHelper.SetDelete(mCurrentActivity, ConfigHelper.getString(AppExtension.getInstance(), "sid"), todoToDelete.getID());
    }

    @Override
    public int getItemCount() {
        return mToDosToDisplay != null ? mToDosToDisplay.size() : 0;
    }

    public ArrayList<ToDo> GetListSrc() {
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
                    mCurrentFragment.DisableRefresh();
                }

                lastX = (int) event.getRawX();

                if (scrollLeft < -150 && !mIsInGreen) {
                    PlayColorChangeAnimation((ImageView) root.findViewById(R.id.greenImageView), true);
                }
                else if (scrollLeft > 150 && !mIsInRed) {
                    PlayColorChangeAnimation((ImageView) root.findViewById(R.id.redImageView), false);
                }

                break;
            case MotionEvent.ACTION_UP:

                OnMoveComplete(v, v.getScrollX(), id);

                break;

            case MotionEvent.ACTION_CANCEL:

                OnMoveComplete(v, v.getScrollX(), id);
                break;
        }

        return false;
    }

    private void OnMoveComplete(View v, float scrollLeft, String id) {
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
                PostHelper.SetDone(mCurrentActivity, ConfigHelper.getString(AppExtension.getInstance(), "sid"), id, mCurrentToDo.getIsDone() ? "1" : "0");
            }
        }
        //Delete
        else if (scrollLeft > 150) {
            DeleteToDo(mCurrentToDo.getID());
        }

        if (mIsInGreen) {
            PlayFadebackAnimation((ImageView) v.findViewById(R.id.greenImageView), true);
        }
        else if (mIsInRed) {
            PlayFadebackAnimation((ImageView) v.findViewById(R.id.redImageView), false);
        }

        PlayGoBackAnimation(v, scrollLeft);
        SerializerHelper.SerializeToFile(AppExtension.getInstance(), mToDosToDisplay, SerializerHelper.todosFileName);
    }

    private void PlayGoBackAnimation(final View v, final float left) {
        ValueAnimator valueAnimator = ValueAnimator.ofInt((int) left, 0);
        valueAnimator.setDuration(700);
        valueAnimator.setInterpolator(new DecelerateInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                v.scrollTo((int) valueAnimator.getAnimatedValue(), 0);
                if (Math.abs((int) valueAnimator.getAnimatedValue()) < 10) {
                    mCurrentFragment.EnableRefresh();
                    mIsSwiping = false;
                }
            }
        });
        valueAnimator.start();
    }

    private void PlayColorChangeAnimation(final ImageView v, boolean isGreen) {
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

    private void PlayFadebackAnimation(final ImageView v, final boolean isGreen) {
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
        public ImageView cateImage;
        public RelativeLayout cateBtn;

        public ToDoItemViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.todoBlock);
            lineView = (ImageView) itemView.findViewById(R.id.lineView);
            greenImageView = (ImageView) itemView.findViewById(R.id.greenImageView);
            redImageView = (ImageView) itemView.findViewById(R.id.redImageView);
            deleteView = (ImageView) itemView.findViewById(R.id.deleteView);
            relativeLayout = (RelativeLayout) itemView.findViewById(R.id.todo_layout);
            cateBtn = (RelativeLayout) itemView.findViewById(R.id.cateBtn);
            cateImage = (ImageView) itemView.findViewById(R.id.cateImage);
        }

        public String getID() {
            return id;
        }

        public void setID(String id) {
            this.id = id;
        }
    }

}
