package adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.juniper.myerlistandroid.R;

import java.util.ArrayList;

import fragment.DeletedItemFragment;
import helper.ConfigHelper;
import helper.ContextUtil;
import helper.PostHelper;
import helper.SerializerHelper;
import model.Schedule;
import model.ScheduleList;


public class DeletedListAdapter  extends RecyclerView.Adapter<DeletedListAdapter.ViewHolder>
{
    private Activity mCurrentActivity;
    private DeletedItemFragment mDeletedItemFragment;
    private ArrayList<Schedule> mDeleteSchedules;

    public DeletedListAdapter(Activity activity, DeletedItemFragment deletedItemFragment,ArrayList<Schedule> deletedList)
    {
        mDeletedItemFragment=deletedItemFragment;
        mCurrentActivity=activity;
        mDeleteSchedules=deletedList;
    }

    @Override
    public DeletedListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View v= LayoutInflater.from(parent.getContext()).inflate(R.layout.row_deleted,parent,false);
        ViewHolder viewHolder=new ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final DeletedListAdapter.ViewHolder holder, final int position)
    {
        holder.mTextView.setText(mDeleteSchedules.get(position).getContent());
        holder.mDeleteView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {

                Schedule sToDelete=mDeleteSchedules.get(position);
                mDeleteSchedules.remove(sToDelete);
                notifyItemRemoved(position);

                if(mDeleteSchedules.size()==0)
                {
                    mDeletedItemFragment.ShowNoItemHint();
                }

                SerializerHelper.SerializeToFile(ContextUtil.getInstance(), mDeleteSchedules, SerializerHelper.deletedFileName);
            }
        });
        holder.mReDoView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(!ConfigHelper.ISOFFLINEMODE)
                {
                    PostHelper.AddMemo(mCurrentActivity, ConfigHelper.getString(ContextUtil.getInstance(),"sid"),mDeleteSchedules.get(position).getContent(),"0");
                }
                else
                {
                    ScheduleList.TodosList.add(ScheduleList.TodosList.size(),mDeleteSchedules.get(position));
                }

                Schedule sToDelete=mDeleteSchedules.get(position);
                mDeleteSchedules.remove(sToDelete);
                notifyItemRemoved(position);

                if(mDeleteSchedules.size()==0)
                {
                    mDeletedItemFragment.ShowNoItemHint();
                }

                SerializerHelper.SerializeToFile(ContextUtil.getInstance(), mDeleteSchedules, SerializerHelper.deletedFileName);
                SerializerHelper.SerializeToFile(ContextUtil.getInstance(), ScheduleList.TodosList, SerializerHelper.todosFileName);
            }
        });
    }


    public void DeleteAll()
    {
        notifyItemRangeChanged(0, mDeleteSchedules.size());
        mDeleteSchedules.clear();
        mDeletedItemFragment.ShowNoItemHint();
        SerializerHelper.SerializeToFile(ContextUtil.getInstance(),mDeleteSchedules,SerializerHelper.deletedFileName);
    }

    @Override
    public int getItemCount()
    {
        return mDeleteSchedules!=null?mDeleteSchedules.size():0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder
    {
        private TextView mTextView;
        private ImageView mReDoView;
        private ImageView mDeleteView;

        public ViewHolder(View itemView)
        {
            super(itemView);
            mTextView=(TextView)itemView.findViewById(R.id.deletedBlock);
            mReDoView=(ImageView)itemView.findViewById(R.id.redoView);
            mDeleteView=(ImageView)itemView.findViewById(R.id.deleteView);
        }
    }
}
