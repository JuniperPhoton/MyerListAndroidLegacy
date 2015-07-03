package adapter;

import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.juniper.myerlistandroid.R;

import java.util.ArrayList;

import activity.MainActivity;
import activity.StartActivity;
import fragment.ToDoFragment;
import helper.ConfigHelper;
import helper.ContextUtil;
import helper.PostHelper;
import helper.SerializerHelper;
import model.Schedule;
import model.ScheduleList;


public class ToDoListAdapter extends RecyclerView.Adapter<ToDoListAdapter.ViewHolder> implements View.OnTouchListener
{
    private boolean mCanOperate=true;
    private Activity mCurrentActivity;
    private ToDoFragment mCurrentFragment;
    private ArrayList<Schedule> mMySchedules;
    private Schedule mCurrentSchedule=null;
    int lastX;

    private boolean mIsGreenOn=false;
    private boolean mIsRedOn=false;

    private boolean mIsInSwipe=false;

    private android.support.v7.app.AlertDialog mDialog;
    private EditText mNewMemoText;

    public ToDoListAdapter(ArrayList<Schedule> data,Activity activity,ToDoFragment fragment)
    {
        mCurrentActivity=activity;
        mMySchedules=data;
        mCurrentFragment=fragment;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View v= LayoutInflater.from(parent.getContext()).inflate(R.layout.row_todo,parent,false);
        ViewHolder viewHolder=new ViewHolder(v);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position)
    {
        //bind data to UI component
        holder.textView.setText(mMySchedules.get(position).getContent());
        holder.setID(mMySchedules.get(position).getID());
        if(!mMySchedules.get(position).getIsDone())
        {
            holder.lineView.setVisibility(View.GONE);
        }
        holder.deleteView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                deleteToDos(mMySchedules.get(position));
            }
        });

        holder.relativeLayout.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(mIsInSwipe)
                {
                    return;
                }
                View dialogView=(View)LayoutInflater.from(mCurrentActivity).inflate(R.layout.add_todo_dialog, (ViewGroup) mCurrentActivity.findViewById(R.id.dialog_title));

                TextView titleText=(TextView)dialogView.findViewById(R.id.dialog_title_text);
                titleText.setText(mCurrentActivity.getResources().getString(R.string.modify_memo_title));

                mNewMemoText=(EditText)dialogView.findViewById(R.id.newMemoEdit);
                mNewMemoText.setHint(R.string.new_memo_hint);
                mNewMemoText.setText(holder.textView.getText().toString());

                Button okBtn=(Button)dialogView.findViewById(R.id.add_ok_btn);
                okBtn.setText(R.string.ok_btn);
                okBtn.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        mDialog.dismiss();
                        String targetID=holder.getID();
                        int index=0;
                        for(int i=0;i<mMySchedules.size();i++)
                        {
                            Schedule s=mMySchedules.get(i);
                            if(s.getID().equals(targetID))
                            {
                                index=i;
                                break;
                            }
                        }
                        mMySchedules.get(index).setContent(mNewMemoText.getText().toString());
                        notifyItemChanged(index);

                        if(!ConfigHelper.ISOFFLINEMODE)
                        {
                            PostHelper.UpdateContent(mCurrentActivity,ConfigHelper.getString(mCurrentActivity,"sid"),targetID,mNewMemoText.getText().toString());
                        }
                        else
                        {
                            SerializerHelper.SerializeToFile(mCurrentActivity,mMySchedules,SerializerHelper.todosFileName);
                        }

                    }
                });

                Button cancelBtn=(Button)dialogView.findViewById(R.id.add_cancel_btn);
                cancelBtn.setText(R.string.cancel_btn);
                cancelBtn.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        if (mDialog != null)
                        {
                            mDialog.dismiss();
                        }
                    }
                });

                if(!ConfigHelper.getBoolean(ContextUtil.getInstance(),"HandHobbit"))
                {
                    LinearLayout linearLayout=(LinearLayout)dialogView.findViewById(R.id.dialog_btn_layout);

                    RelativeLayout.LayoutParams layoutParams=new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT);
                    layoutParams.setMargins(20,0,0,0);
                    layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                    linearLayout.setLayoutParams(layoutParams);
                }

                android.support.v7.app.AlertDialog.Builder builder=new android.support.v7.app.AlertDialog.Builder(mCurrentActivity);
                mDialog=builder.setView((dialogView)).show();

            }
        });

        holder.relativeLayout.setOnTouchListener(this);
        holder.relativeLayout.setTag(holder.getID());

        holder.greenImageView.setAlpha(1f);
        holder.redImageView.setAlpha(1f);
        holder.greenImageView.setVisibility(View.INVISIBLE);
        holder.redImageView.setVisibility(View.INVISIBLE);
        holder.relativeLayout.scrollTo(0,0);
    }

    public void SetCanOperate(boolean isDraweOpen)
    {
        mCanOperate=!isDraweOpen;
    }

    public void addToDos(Schedule todoToAdd)
    {
        if(ConfigHelper.getBoolean(ContextUtil.getInstance(),"AddToBottom"))
        {
            mMySchedules.add(todoToAdd);
            notifyItemInserted(mMySchedules.size()-1);
        }
        else
        {
            mMySchedules.add(0, todoToAdd);
            notifyItemInserted(0);
        }
        SerializerHelper.SerializeToFile(mCurrentActivity, mMySchedules, SerializerHelper.todosFileName);
    }

    public void deleteToDos(Schedule todoToDelete)
    {
        int index=0;
        for(int i=0;i<mMySchedules.size();i++)
        {
           Schedule s=mMySchedules.get(i);
            if(s.getID().equals(todoToDelete.getID()))
            {
                index=i;
                break;
            }
        }
        notifyItemRemoved(index);
        mMySchedules.remove(todoToDelete);

        ScheduleList.DeletedList.add(0, todoToDelete);
        SerializerHelper.SerializeToFile(ContextUtil.getInstance(), ScheduleList.DeletedList, SerializerHelper.deletedFileName);

        if(ConfigHelper.ISOFFLINEMODE)
        {
            SerializerHelper.SerializeToFile(mCurrentActivity, mMySchedules, SerializerHelper.todosFileName);
        }
        else PostHelper.SetDelete(mCurrentActivity, ConfigHelper.getString(ContextUtil.getInstance(), "sid"), todoToDelete.getID());

    }

    @Override
    public int getItemCount()
    {
        return mMySchedules!=null?mMySchedules.size():0;
    }

    public ArrayList<Schedule> getListSrc()
    {
        return mMySchedules;
    }


    //@Override
    public boolean onTouch(final View v, MotionEvent event)
    {
        RelativeLayout root=(RelativeLayout)v;

        int scrollleft=0;
        String id=(String)v.getTag();

        switch(event.getAction())
        {
            case MotionEvent.ACTION_DOWN:

                lastX= (int)event.getRawX();
                break;

            case MotionEvent.ACTION_MOVE:

                if(!mCanOperate) break;

                mIsInSwipe=true;

                int dx=(int)event.getRawX()-lastX;

                scrollleft=v.getScrollX();

                v.scrollBy(-dx, 0);

                if(scrollleft<-20)
                {
                    mCurrentFragment.DisableRefresh();
                }

                lastX=(int)event.getRawX();

                if(scrollleft<-150 && !mIsGreenOn)
                {
                    SetColorAnim((ImageView) root.findViewById(R.id.greenImageView),true);
                }
                else if(scrollleft>150 && !mIsRedOn)
                {
                    SetColorAnim((ImageView) root.findViewById(R.id.redImageView),false);
                }


                break;
            case MotionEvent.ACTION_UP:

                SetMoveComplete(v,v.getScrollX(),id);

                break;

            case MotionEvent.ACTION_CANCEL:
            {
                SetMoveComplete(v, v.getScrollX(), id);

            };break;

        }

        return false;
    }

    private void SetMoveComplete(View v,float scrollLeft,String id)
    {
        //Find the current schedule

        for (Schedule s:mMySchedules)
        {
            if(s.getID().equals(id))
            {
                mCurrentSchedule=s;
                break;
            }
        }
        if(mCurrentSchedule==null) return;

        //Finish
        if(scrollLeft<-150)
        {

            ImageView lineview=(ImageView)v.findViewById(R.id.lineView);
            if(mCurrentSchedule.getIsDone())
            {
                lineview.setVisibility(View.GONE);
                mCurrentSchedule.setIsDone(false);
            }
            else
            {
                lineview.setVisibility(View.VISIBLE);
                mCurrentSchedule.setIsDone(true);
            }

            if(!ConfigHelper.ISOFFLINEMODE)
            {
                PostHelper.SetDone(mCurrentActivity, ConfigHelper.getString(ContextUtil.getInstance(), "sid"), id, mCurrentSchedule.getIsDone() ? "1" : "0");
            }


        }
        //Delete
        else if(scrollLeft>150)
        {
            deleteToDos(mCurrentSchedule);
        }

        if(mIsGreenOn)
        {
            UnSetColorAnim((ImageView) v.findViewById(R.id.greenImageView),true);
        }
        else if(mIsRedOn)
        {
            UnSetColorAnim((ImageView)v.findViewById(R.id.redImageView),false);
        }

        SetBackAnim(v, scrollLeft);
        SerializerHelper.SerializeToFile(ContextUtil.getInstance(), mMySchedules, SerializerHelper.todosFileName);
    }

    private void SetBackAnim(final View v, final float left)
    {
        ValueAnimator valueAnimator=ValueAnimator.ofInt((int)left,0);
        valueAnimator.setDuration(700);
        valueAnimator.setInterpolator(new DecelerateInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener()
                {
                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator)
                    {
                        v.scrollTo((int) valueAnimator.getAnimatedValue(), 0);
                        if (Math.abs((int) valueAnimator.getAnimatedValue()) < 10)
                        {
                            mCurrentFragment.EnableRefresh();
                            mIsInSwipe = false;
                        }
                    }
                });
        valueAnimator.start();
    }

    private void SetColorAnim(final ImageView v,boolean isGreen)
    {
        v.setAlpha(1f);
        AnimationSet animationSet=new AnimationSet(false);

        AlphaAnimation alphaAnimation=new AlphaAnimation(0.1f, 1.0f);
        alphaAnimation.setDuration(700);
        alphaAnimation.setAnimationListener(new Animation.AnimationListener()
        {
            @Override
            public void onAnimationStart(Animation animation)
            {
                v.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation)
            {

            }

            @Override
            public void onAnimationRepeat(Animation animation)
            {

            }
        });
        animationSet.addAnimation(alphaAnimation);
        v.startAnimation(animationSet);

        if(isGreen) mIsGreenOn=true;
        else mIsRedOn=true;
    }

    private void UnSetColorAnim(final ImageView v, final boolean isGreen)
    {
        AnimationSet animationSet=new AnimationSet(false);
        AlphaAnimation alphaAnimation=new AlphaAnimation(1.0f, 0.0f);
        alphaAnimation.setDuration(700);
        alphaAnimation.setAnimationListener(new Animation.AnimationListener()
        {
            @Override
            public void onAnimationStart(Animation animation)
            {
                v.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation)
            {
                v.setVisibility(View.INVISIBLE);
                if(isGreen) mIsGreenOn=false;
                else mIsRedOn=false;
            }

            @Override
            public void onAnimationRepeat(Animation animation)
            {

            }
        });
        animationSet.addAnimation(alphaAnimation);
        v.startAnimation(animationSet);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder
    {
        private String id;
        public TextView textView;
        public ImageView lineView;
        public ImageView deleteView;
        public RelativeLayout relativeLayout;
        public ImageView greenImageView;
        public ImageView redImageView;

        public ViewHolder(View itemView)
        {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.todoBlock);
            lineView=(ImageView)itemView.findViewById(R.id.lineView);
            greenImageView=(ImageView) itemView.findViewById(R.id.greenImageView);
            redImageView=(ImageView)itemView.findViewById(R.id.redImageView);
            deleteView=(ImageView)itemView.findViewById(R.id.deleteView);
            relativeLayout=(RelativeLayout)itemView.findViewById(R.id.todo_layout);
        }

        public String getID()
        {
            return id;
        }
        public void setID(String id)
        {
            this.id=id;
        }
    }

    public interface OnDeletedItem
    {
        void OnDeletedItem();
    }
}
