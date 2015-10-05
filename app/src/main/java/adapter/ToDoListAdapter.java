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
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.juniper.myerlistandroid.R;

import java.util.ArrayList;

import fragment.ToDoFragment;
import helper.ConfigHelper;
import helper.ContextUtil;
import helper.PostHelper;
import helper.SerializerHelper;
import model.ToDo;
import model.ToDoListHelper;


public class ToDoListAdapter extends RecyclerView.Adapter<ToDoListAdapter.ViewHolder> implements View.OnTouchListener
{
    //能否操作列表项目
    private boolean mCanOperate=true;

    //当前所在的 Activity
    private Activity mCurrentActivity;

    //当前所在的 Fragment
    private ToDoFragment mCurrentFragment;

    //表示的列表
    private ArrayList<ToDo> mMyToDos;

    //当前操控的项目
    private ToDo mCurrentToDo =null;

    int lastX;

    private boolean mIsInGreen =false;
    private boolean mIsInRed =false;
    private boolean mIsSwiping =false;

    //修改的时候弹出的对话框
    private android.support.v7.app.AlertDialog mDialog;

    //修改的时候文本框
    private EditText mNewMemoText;

    //构造函数
    //传入当前的列表
    public ToDoListAdapter(ArrayList<ToDo> data,Activity activity,ToDoFragment fragment)
    {
        mCurrentActivity=activity;
        mMyToDos =data;
        mCurrentFragment=fragment;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View v= LayoutInflater.from(parent.getContext()).inflate(R.layout.row_todo,parent,false);
        ViewHolder viewHolder=new ViewHolder(v);

        return viewHolder;
    }

    //创建每一项的布局
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position)
    {
        //设置文字
        holder.textView.setText(mMyToDos.get(position).getContent());

        //设置类别
        int cate= mMyToDos.get(position).getCate();
        switch (cate)
        {
            case 0:holder.cateImageView.setImageResource(R.drawable.cate_default);break;
            case 1:holder.cateImageView.setImageResource(R.drawable.cate_work);break;
            case 2:holder.cateImageView.setImageResource(R.drawable.cate_life);break;
            case 3:holder.cateImageView.setImageResource(R.drawable.cate_family);break;
            case 4:holder.cateImageView.setImageResource(R.drawable.cate_enter);break;
        }

        holder.cateImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                String targetID=holder.getID();
                int index=0;

                //根据ID 找到项目
                for(int i=0;i< mMyToDos.size();i++)
                {
                    ToDo s= mMyToDos.get(i);
                    if(s.getID().equals(targetID))
                    {
                        index=i;
                        break;
                    }
                }

                ToDo currentItem=mMyToDos.get(index);
                int cate=currentItem.getCate();
                currentItem.setCate(++cate);
                if(cate>=5) currentItem.setCate(0);
                switch (currentItem.getCate())
                {
                    case 0:holder.cateImageView.setImageResource(R.drawable.cate_default);break;
                    case 1:holder.cateImageView.setImageResource(R.drawable.cate_work);break;
                    case 2:holder.cateImageView.setImageResource(R.drawable.cate_life);break;
                    case 3:holder.cateImageView.setImageResource(R.drawable.cate_family);break;
                    case 4:holder.cateImageView.setImageResource(R.drawable.cate_enter);break;
                }

                //要notify UI 才会更新
                notifyItemChanged(index);

                if(!ConfigHelper.ISOFFLINEMODE)
                {
                    PostHelper.UpdateContent(mCurrentActivity,ConfigHelper.getString(mCurrentActivity, "sid"),targetID,currentItem.getContent(),cate);
                }
                else
                {
                    SerializerHelper.SerializeToFile(mCurrentActivity, mMyToDos,SerializerHelper.todosFileName);
                }
            }
        });

        holder.setID(mMyToDos.get(position).getID());

        //设置是否完成
        if(!mMyToDos.get(position).getIsDone())
        {
            holder.lineView.setVisibility(View.GONE);
        }

        //设置删除
        holder.deleteView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                deleteToDos(mMyToDos.get(position));
            }
        });

        //设置点击修改
        holder.relativeLayout.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(mIsSwiping)
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

                        //根据ID 找到项目
                        for(int i=0;i< mMyToDos.size();i++)
                        {
                            ToDo s= mMyToDos.get(i);
                            if(s.getID().equals(targetID))
                            {
                                index=i;
                                break;
                            }
                        }

                        //更新项目
                        ToDo currentItem=mMyToDos.get(index);
                        currentItem.setContent(mNewMemoText.getText().toString());

                        //要notify UI 才会更新
                        notifyItemChanged(index);

                        if(!ConfigHelper.ISOFFLINEMODE)
                        {
                            PostHelper.UpdateContent(mCurrentActivity,ConfigHelper.getString(mCurrentActivity,"sid"),targetID,mNewMemoText.getText().toString(),currentItem.getCate());
                        }
                        else
                        {
                            SerializerHelper.SerializeToFile(mCurrentActivity, mMyToDos,SerializerHelper.todosFileName);
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

    public void addToDos(ToDo todoToAdd)
    {
        if(todoToAdd==null) return;
        if(ConfigHelper.getBoolean(ContextUtil.getInstance(),"AddToBottom"))
        {
            mMyToDos.add(todoToAdd);
            notifyItemInserted(mMyToDos.size()-1);
        }
        else
        {
            mMyToDos.add(0, todoToAdd);
            notifyItemInserted(0);
        }
        SerializerHelper.SerializeToFile(mCurrentActivity, mMyToDos, SerializerHelper.todosFileName);
    }

    public void deleteToDos(ToDo todoToDelete)
    {
        int index=0;
        for(int i=0;i< mMyToDos.size();i++)
        {
           ToDo s= mMyToDos.get(i);
            if(s.getID().equals(todoToDelete.getID()))
            {
                index=i;
                break;
            }
        }
        notifyItemRemoved(index);
        mMyToDos.remove(todoToDelete);

        ToDoListHelper.DeletedList.add(0, todoToDelete);
        SerializerHelper.SerializeToFile(ContextUtil.getInstance(), ToDoListHelper.DeletedList, SerializerHelper.deletedFileName);

        if(ConfigHelper.ISOFFLINEMODE)
        {
            SerializerHelper.SerializeToFile(mCurrentActivity, mMyToDos, SerializerHelper.todosFileName);
        }
        else PostHelper.SetDelete(mCurrentActivity, ConfigHelper.getString(ContextUtil.getInstance(), "sid"), todoToDelete.getID());

    }

    @Override
    public int getItemCount()
    {
        return mMyToDos !=null? mMyToDos.size():0;
    }

    public ArrayList<ToDo> getListSrc()
    {
        return mMyToDos;
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

                mIsSwiping =true;

                int dx=(int)event.getRawX()-lastX;

                scrollleft=v.getScrollX();

                v.scrollBy(-dx, 0);

                if(scrollleft<-20)
                {
                    mCurrentFragment.DisableRefresh();
                }

                lastX=(int)event.getRawX();

                if(scrollleft<-150 && !mIsInGreen)
                {
                    SetColorAnim((ImageView) root.findViewById(R.id.greenImageView),true);
                }
                else if(scrollleft>150 && !mIsInRed)
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

        for (ToDo s: mMyToDos)
        {
            if(s.getID().equals(id))
            {
                mCurrentToDo =s;
                break;
            }
        }
        if(mCurrentToDo ==null) return;

        //Finish
        if(scrollLeft<-150)
        {

            ImageView lineview=(ImageView)v.findViewById(R.id.lineView);
            if(mCurrentToDo.getIsDone())
            {
                lineview.setVisibility(View.GONE);
                mCurrentToDo.setIsDone(false);
            }
            else
            {
                lineview.setVisibility(View.VISIBLE);
                mCurrentToDo.setIsDone(true);
            }

            if(!ConfigHelper.ISOFFLINEMODE)
            {
                PostHelper.SetDone(mCurrentActivity, ConfigHelper.getString(ContextUtil.getInstance(), "sid"), id, mCurrentToDo.getIsDone() ? "1" : "0");
            }


        }
        //Delete
        else if(scrollLeft>150)
        {
            deleteToDos(mCurrentToDo);
        }

        if(mIsInGreen)
        {
            UnSetColorAnim((ImageView) v.findViewById(R.id.greenImageView),true);
        }
        else if(mIsInRed)
        {
            UnSetColorAnim((ImageView)v.findViewById(R.id.redImageView),false);
        }

        SetBackAnim(v, scrollLeft);
        SerializerHelper.SerializeToFile(ContextUtil.getInstance(), mMyToDos, SerializerHelper.todosFileName);
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
                            mIsSwiping = false;
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

        if(isGreen) mIsInGreen =true;
        else mIsInRed =true;
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
                if(isGreen) mIsInGreen =false;
                else mIsInRed =false;
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
        public ImageView cateImageView;

        public ViewHolder(View itemView)
        {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.todoBlock);
            lineView=(ImageView)itemView.findViewById(R.id.lineView);
            greenImageView=(ImageView) itemView.findViewById(R.id.greenImageView);
            redImageView=(ImageView)itemView.findViewById(R.id.redImageView);
            deleteView=(ImageView)itemView.findViewById(R.id.deleteView);
            relativeLayout=(RelativeLayout)itemView.findViewById(R.id.todo_layout);
            cateImageView=(ImageView)itemView.findViewById(R.id.cateCircleImage);
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
