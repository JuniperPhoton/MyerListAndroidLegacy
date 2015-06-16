package middle;

import android.app.Activity;
import android.app.Fragment;
import android.media.Image;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.juniper.myerlistandroid.R;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import fragment.ToDoFragment;
import helper.AppHelper;
import helper.ConfigHelper;
import helper.ContextUtil;
import helper.PostHelper;
import helper.SerializerHelper;
import model.Schedule;


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

        //mGestureDetector=new GestureDetector(ContextUtil.getInstance(),new OnGestureListener(holder.relativeLayout));
        holder.relativeLayout.setOnTouchListener(this);
        holder.relativeLayout.setTag(holder.getID());

        holder.greenImageView.setAlpha(0f);
        holder.redImageView.setAlpha(0f);
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

        Gson gson=new Gson();
        SerializerHelper.toStringAndSave(ContextUtil.getInstance(), mMySchedules, mMySchedules.getClass());
        //ArrayList<Schedule> content=SerializerHelper.readFromFile(mMySchedules.getClass(),ContextUtil.getInstance());
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


        PostHelper.SetDelete(mCurrentActivity, ConfigHelper.getString(ContextUtil.getInstance(), "sid"), todoToDelete.getID());

        Gson gson=new Gson();
        SerializerHelper.toStringAndSave(ContextUtil.getInstance(), mMySchedules, mMySchedules.getClass());


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

            PostHelper.SetDone(mCurrentActivity, ConfigHelper.getString(ContextUtil.getInstance(), "sid"), id, mCurrentSchedule.getIsDone() ? "1" : "0");
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

    }

    private void SetBackAnim(final View v, final float left)
    {
        AnimationSet animationSet=new AnimationSet(true);

        TranslateAnimation translateAnimation=new TranslateAnimation(0,left,0,0);
        translateAnimation.setDuration(700);
        translateAnimation.setAnimationListener(new Animation.AnimationListener()
        {
            @Override
            public void onAnimationStart(Animation animation)
            {

            }

            @Override
            public void onAnimationEnd(Animation animation)
            {

                mCurrentFragment.EnableRefresh();
                v.clearAnimation();
                v.scrollTo(0, 0);
            }

            @Override
            public void onAnimationRepeat(Animation animation)
            {

            }
        });
        animationSet.addAnimation(translateAnimation);

        v.startAnimation(animationSet);
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

            }

            @Override
            public void onAnimationEnd(Animation animation)
            {
                v.setAlpha(1.0f);
                v.clearAnimation();

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

            }

            @Override
            public void onAnimationEnd(Animation animation)
            {
                v.setAlpha(0.0f);
                v.clearAnimation();
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

    public interface OnMoveListener
    {
        void OnMove(float getRawX,float getRawY,float getLeft);
    }
}
