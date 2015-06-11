package com.example.juniper.myerlistandroid;


import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import helper.AppHelper;
import helper.ContextUtil;
import model.NavigationItem;
import model.Schedule;


public class NavigationDrawerAdapter extends RecyclerView.Adapter<NavigationDrawerAdapter.ViewHolder>
{

    private List<NavigationItem> mData;
    private NavigationDrawerCallbacks mNavigationDrawerCallbacks;
    private View mSelectedView;
    private int mSelectedPosition;

    public NavigationDrawerAdapter(List<NavigationItem> data)
    {
        mData = data;
    }

    public NavigationDrawerCallbacks getNavigationDrawerCallbacks()
    {
        return mNavigationDrawerCallbacks;
    }

    public void setNavigationDrawerCallbacks(NavigationDrawerCallbacks navigationDrawerCallbacks)
    {
        mNavigationDrawerCallbacks = navigationDrawerCallbacks;
    }

    @Override
    public NavigationDrawerAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i)
    {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_drawer, viewGroup, false);
        final ViewHolder viewHolder = new ViewHolder(v);
        viewHolder.linearLayout.setOnClickListener(new View.OnClickListener()
                                                   {
                                                       @Override
                                                       public void onClick(View v)
                                                       {
                                                           if (mSelectedView != null)
                                                           {
                                                               ((CardView)mSelectedView).setCardBackgroundColor(ContextUtil.getInstance().getResources().getColor(R.color.myDrawerBackground));
                                                           }
                                                           mSelectedPosition = viewHolder.getPosition();

                                                           //v.setSelected(true);

                                                           mSelectedView = (CardView)v.getParent();

                                                           if (mNavigationDrawerCallbacks != null)
                                                               mNavigationDrawerCallbacks.onNavigationDrawerItemSelected(viewHolder.getPosition());
                                                       }
                                                   }
        );
        //viewHolder.cardView.setCardBackgroundColor(R.drawable.row_selector);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(NavigationDrawerAdapter.ViewHolder viewHolder, int i)
    {
        viewHolder.textView.setText(mData.get(i).getText());
        viewHolder.imageView.setImageDrawable(mData.get(i).getDrawable());
        if (mSelectedPosition == i && i!=4 && i!=3 && i!=2)
        {
            if (mSelectedView != null)
            {
                ((CardView)mSelectedView).setCardBackgroundColor(ContextUtil.getInstance().getResources().getColor(R.color.myDrawerBackground));
            }
            mSelectedPosition = i;
            mSelectedView = viewHolder.cardView;
            ((CardView)mSelectedView).setCardBackgroundColor(ContextUtil.getInstance().getResources().getColor(R.color.MyerListBlueLight));
            //mSelectedView.setSelected(true);
        }
    }


    public void selectPosition(int position)
    {
        mSelectedPosition = position;
        notifyItemChanged(position);
    }

    public void deleteToDo(String id)
    {

    }



    @Override
    public int getItemCount()
    {
        return mData != null ? mData.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder
    {
        public TextView textView;
        public ImageView imageView;
        public CardView cardView;
        public LinearLayout linearLayout;

        public ViewHolder(View itemView)
        {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.item_name);
            imageView=(ImageView) itemView.findViewById(R.id.item_icon);
            cardView=(CardView)itemView.findViewById(R.id.navigation_card_view);
            linearLayout=(LinearLayout) cardView.findViewById(R.id.navigationitem_layout);
        }
    }
}