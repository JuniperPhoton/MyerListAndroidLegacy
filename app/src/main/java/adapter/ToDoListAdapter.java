package adapter;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.juniper.myerlistandroid.R;

import java.util.ArrayList;

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

                int dx=(int)event.getRawX()-lastX;

                scrollleft=v.getScrollX();

                v.scrollBy(-dx, 0);

                if(scrollleft<-20)
                {
                    mCurrentFragment.DisableRefresh();
                }

                lastX=(int)event.getRawX();

                if(scrollleft<-100 && !mIsGreenOn)
                {
                    SetColorAnim((ImageView) root.findViewById(R.id.greenImageView),true);
                }
                else if(scrollleft>100 && !mIsRedOn)
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
        if(scrollLeft<-100)
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
        else if(scrollLeft>100)
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
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener()
        {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator)
            {
                v.scrollTo((int) valueAnimator.getAnimatedValue(), 0);
                if(((int)valueAnimator.getAnimatedValue()-left)<10)
                {
                    mCurrentFragment.EnableRefresh();
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
