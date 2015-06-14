package middle;

import android.app.Activity;
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

import helper.AppHelper;
import helper.ConfigHelper;
import helper.ContextUtil;
import helper.PostHelper;
import helper.SerializerHelper;
import model.Schedule;


public class ToDoListAdapter extends RecyclerView.Adapter<ToDoListAdapter.ViewHolder> implements View.OnTouchListener
{
    private Activity mCurrentActivity;
    private int mCurrentPosition=0;
    private ArrayList<Schedule> mMySchedules;
    private GestureDetector mGestureDetector;
    int lastX,lastY;

    private boolean mIsGreenOn=false;

    public ToDoListAdapter(ArrayList<Schedule> data,Activity activity)
    {
        mCurrentActivity=activity;
        mMySchedules=data;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View v= LayoutInflater.from(parent.getContext()).inflate(R.layout.row_todo,parent,false);
        ViewHolder viewHolder=new ViewHolder(v);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position)
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
    }

    public void addToDos(Schedule todoToAdd)
    {
        if(ConfigHelper.getBoolean(ContextUtil.getInstance(),"AddToBottom"))
        {
            mMySchedules.add(todoToAdd);
            notifyItemInserted(mMySchedules.size() - 1);
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
        mMySchedules.remove(index);
        notifyItemRemoved(index);

        Gson gson=new Gson();
        SerializerHelper.toStringAndSave(ContextUtil.getInstance(), mMySchedules, mMySchedules.getClass());
    }

    public  void markToDos()
    {

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

        int rollbackLeft=100;
        int left=0;
        String id=(String)v.getTag();

        switch(event.getAction())
        {
            case MotionEvent.ACTION_DOWN:

                lastX= (int)event.getRawX();
                Log.d("MOTION","down");
                break;

            case MotionEvent.ACTION_MOVE:
                //change value of X
                int dx=(int)event.getRawX()-lastX;

                //current left position relatively to parent
                left=v.getLeft()+dx;


                //set the position of view relative to parent layout
                v.scrollBy(-dx, 0);

                lastX=(int)event.getRawX();
                lastY=(int)event.getRawY();

                if(left<-20 && !mIsGreenOn)
                {
                    //SetGreenAnim((ImageView)root.findViewById(R.id.greenImageView));
                    //root.findViewById(R.id.greenImageView).setAlpha((float) 1);
                }

                Log.d("MOTION","move");
                break;
            case MotionEvent.ACTION_UP:

                left=v.getLeft();
                if(left<-60)
                {
                    Schedule currentS=null;
                    for (Schedule s:mMySchedules)
                    {
                        if(s.getID()==id)
                        {
                            currentS=s;
                            break;
                        }
                    }
                    if(currentS==null) break;

                    ImageView lineview=(ImageView)v.findViewById(R.id.lineView);
                    if(currentS.getIsDone())
                    {
                        lineview.setVisibility(View.GONE);
                        currentS.setIsDone(false);
                    }
                    else
                    {
                        lineview.setVisibility(View.VISIBLE);
                        currentS.setIsDone(true);
                    }

                    PostHelper.SetDone(mCurrentActivity,ConfigHelper.getString(ContextUtil.getInstance(),"sid"),id,currentS.getIsDone()?"1":"0");
                    //SetGreenAnim((ImageView)root.findViewById(R.id.greenImageView));
                    //root.findViewById(R.id.greenImageView).setAlpha((float) 1);
                }

                rollbackLeft=v.getScrollX();
                SetBackAnim(v,rollbackLeft);

                Log.d("MOTION", "up");
                break;
            case MotionEvent.ACTION_OUTSIDE:
            {
                Log.d("MOTION", "outside");
            };break;
            case MotionEvent.ACTION_CANCEL:
            {
                rollbackLeft=v.getScrollX();
                SetBackAnim(v,rollbackLeft);
                Log.d("MOTION", "cancel");
            };break;

        }

        return false;
    }

    private void SetBackAnim(final View v, final int left)
    {
        Log.d("MOTION", "anim");
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
                UnSetGreenAnim((ImageView)v.findViewById(R.id.greenImageView));
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

    private void SetGreenAnim(final ImageView v)
    {
        AnimationSet animationSet=new AnimationSet(false);
        AlphaAnimation alphaAnimation=new AlphaAnimation(0.1f, 1.0f);
        alphaAnimation.setFillAfter(true);
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

            }

            @Override
            public void onAnimationRepeat(Animation animation)
            {

            }
        });
        animationSet.addAnimation(alphaAnimation);
        v.startAnimation(animationSet);
        mIsGreenOn=true;
    }

    private void UnSetGreenAnim(final ImageView v)
    {
        AnimationSet animationSet=new AnimationSet(false);
        AlphaAnimation alphaAnimation=new AlphaAnimation(1.0f, 0.0f);
        alphaAnimation.setFillAfter(true);
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
                mIsGreenOn=false;
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
}
