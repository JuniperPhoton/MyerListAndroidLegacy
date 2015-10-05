package adapter;


import android.content.res.Configuration;
import android.content.res.Resources;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.TextPaint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.juniper.myerlistandroid.R;

import java.util.List;
import java.util.Locale;

import helper.ContextUtil;
import interfaces.INavigationDrawerCallbacks;
import interfaces.INavigationDrawerCateCallbacks;
import interfaces.INavigationDrawerOtherCallbacks;
import model.NavigationItem;


public class NavigationDrawerAdapter extends RecyclerView.Adapter<NavigationDrawerAdapter.ViewHolder>
{

    private List<NavigationItem> mData;
    private INavigationDrawerCallbacks mINavigationDrawerCallbacks;
    private INavigationDrawerOtherCallbacks mINavigationDrawerOtherCallbacks;
    private INavigationDrawerCateCallbacks mINavigationDrawerCateCallbacks;
    private View mSelectedView;
    private int mSelectedPosition;

    public NavigationDrawerAdapter(List<NavigationItem> data)
    {
        mData = data;
    }

    public INavigationDrawerCallbacks getNavigationDrawerCallbacks()
    {
        return mINavigationDrawerCallbacks;
    }

    public INavigationDrawerOtherCallbacks getOtherNavigationDrawerCallbacks()
    {
        return mINavigationDrawerOtherCallbacks;
    }

    public INavigationDrawerCateCallbacks getCateNavigationDrawerCallbacks()
    {
        return mINavigationDrawerCateCallbacks;
    }

    public void setNavigationDrawerCallbacks(INavigationDrawerCallbacks INavigationDrawerCallbacks)
    {
        mINavigationDrawerCallbacks = INavigationDrawerCallbacks;
    }

    public void setOtherNavigationDrawerCallbacks(INavigationDrawerOtherCallbacks navigationDrawerCallbacks)
    {
        mINavigationDrawerOtherCallbacks = navigationDrawerCallbacks;
    }

    public void setCateNavigationDrawerCallbacks(INavigationDrawerCateCallbacks navigationDrawerCallbacks)
    {
        mINavigationDrawerCateCallbacks = navigationDrawerCallbacks;
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
                                                           mSelectedPosition = viewHolder.getPosition();

                                                           if (mSelectedView != null)
                                                           {
                                                               ((CardView) mSelectedView).setCardBackgroundColor(ContextUtil.getInstance().getResources().getColor(R.color.myDrawerBackground));
                                                               ((ImageView) mSelectedView.findViewById(R.id.item_select)).setVisibility(View.INVISIBLE);
                                                           }


                                                           //v.setSelected(true);

                                                           mSelectedView = (CardView) v.getParent();

                                                           if (mINavigationDrawerCallbacks != null)
                                                           {
                                                               mINavigationDrawerCallbacks.onNavigationDrawerItemSelected(viewHolder.getPosition());
                                                           }
                                                           if (mINavigationDrawerOtherCallbacks != null)
                                                           {
                                                               mINavigationDrawerOtherCallbacks.OnSelectedOther(viewHolder.getPosition());
                                                           }
                                                       }
                                                   }
        );
        //viewHolder.cardView.setCardBackgroundColor(R.drawable.row_selector);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(NavigationDrawerAdapter.ViewHolder viewHolder, int i)
    {
        //设置抽屉每一项的 UI
        viewHolder.textView.setText(mData.get(i).getText());

        Resources resources = ContextUtil.getInstance().getResources();
        Configuration config = resources.getConfiguration();

        //中文字体下加粗
        if (config.locale == Locale.SIMPLIFIED_CHINESE)
        {
            TextPaint textPaint = viewHolder.textView.getPaint();
            textPaint.setFakeBoldText(false);
        }

        viewHolder.imageView.setImageDrawable(mData.get(i).getDrawable());

        if (mData.get(i).getText().equals(""))
        {
            viewHolder.selectRectImage.setVisibility(View.INVISIBLE);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(80, LinearLayout.LayoutParams.MATCH_PARENT);
            layoutParams.setMargins(0, 0, 0, 0);
            viewHolder.imageView.setLayoutParams(layoutParams);
        }

        if (mSelectedPosition == i && mINavigationDrawerCallbacks != null)
        {
            if (mSelectedView != null)
            {
                ((CardView) mSelectedView).setCardBackgroundColor(ContextUtil.getInstance().getResources().getColor(R.color.myDrawerBackground));
            }
            mSelectedPosition = i;
            mSelectedView = viewHolder.cardView;
            ((CardView) mSelectedView).setCardBackgroundColor(ContextUtil.getInstance().getResources().getColor(R.color.MyerListGray));
            ((ImageView) mSelectedView.findViewById(R.id.item_select)).setVisibility(View.VISIBLE);
        }
    }


    public void selectPosition(int position)
    {
        mSelectedPosition = position;
        notifyItemChanged(position);
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
        public ImageView selectRectImage;

        public ViewHolder(View itemView)
        {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.item_name);
            imageView = (ImageView) itemView.findViewById(R.id.item_icon);
            cardView = (CardView) itemView.findViewById(R.id.navigation_card_view);
            linearLayout = (LinearLayout) cardView.findViewById(R.id.navigationitem_layout);
            selectRectImage = (ImageView) itemView.findViewById(R.id.item_select);
        }
    }

}