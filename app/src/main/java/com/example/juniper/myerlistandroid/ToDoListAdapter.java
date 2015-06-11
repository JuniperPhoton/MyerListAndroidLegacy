package com.example.juniper.myerlistandroid;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import helper.ContextUtil;
import helper.SerializerHelper;
import model.Schedule;


public class ToDoListAdapter extends RecyclerView.Adapter<ToDoListAdapter.ViewHolder>
{
    private ArrayList<Schedule> mMySchedules;

    public ToDoListAdapter(ArrayList<Schedule> data)
    {
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
        if(!mMySchedules.get(position).getIsDone())
        {
            holder.lineView.setVisibility(View.GONE);
        }
        holder.deleteView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                mMySchedules.remove(position);
                notifyItemRemoved(position);
            }
        });
    }

    public void addToDos(Schedule todoToAdd)
    {
        mMySchedules.add(todoToAdd);
        notifyItemInserted(mMySchedules.size() - 1);

        Gson gson=new Gson();
        SerializerHelper.toStringAndSave(ContextUtil.getInstance(),mMySchedules,mMySchedules.getClass());
        ArrayList<Schedule> content=SerializerHelper.readFromFile(mMySchedules.getClass(),ContextUtil.getInstance());
    }

    public void deleteToDos(Schedule todoToDelete)
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

    public static class ViewHolder extends RecyclerView.ViewHolder
    {
        public TextView textView;
        public ImageView lineView;
        public ImageView deleteView;

        public ViewHolder(View itemView)
        {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.todoBlock);
            lineView=(ImageView) itemView.findViewById(R.id.lineView);
            deleteView=(ImageView)itemView.findViewById(R.id.deleteView);
        }
    }
}
