package adapter;

import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.juniperphoton.myerlistandroid.R;

import java.util.List;

import interfaces.INavigationDrawerCallback;
import util.AppExtension;
import model.NavigationItemWithIcon;


public class NavigationDrawerAdapter extends RecyclerView.Adapter<NavigationDrawerAdapter.DrawerViewHolder> {

    private List<NavigationItemWithIcon> mData;

    //这些是 MainActivity
    private INavigationDrawerCallback mINavigationDrawerCallback;

    //选中的 View
    private View mSelectedView;

    //选中的项
    private int mSelectedPosition;

    public NavigationDrawerAdapter(List<NavigationItemWithIcon> data) {
        mData = data;
    }

    //设置回调
    public void setNavigationDrawerCallbacks(INavigationDrawerCallback INavigationDrawerCallback) {
        mINavigationDrawerCallback = INavigationDrawerCallback;
    }

    //创建每一项的容器
    @Override
    public DrawerViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_drawer, viewGroup, false);
        final DrawerViewHolder drawerViewHolder = new DrawerViewHolder(v);

        //点击选中的时候发生
        drawerViewHolder.linearLayout.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mSelectedPosition = drawerViewHolder.getPosition();

                        //上一个选中的变为透明色
                        if (mSelectedView != null) {
                            ((CardView) mSelectedView).setCardBackgroundColor(Color.TRANSPARENT);
                        }

                        mSelectedView = (CardView) v.getParent();

                        if (mINavigationDrawerCallback != null) {
                            mINavigationDrawerCallback.onDrawerMainItemSelected(drawerViewHolder.getPosition());
                        }
                    }
                }
        );
        return drawerViewHolder;
    }

    @Override
    public void onBindViewHolder(DrawerViewHolder drawerViewHolder, int i) {
        //设置抽屉每一项的 UI
        drawerViewHolder.textView.setText(mData.get(i).getText());

        drawerViewHolder.imageView.setImageDrawable(mData.get(i).getDrawable());

        if (mSelectedPosition == i && mINavigationDrawerCallback != null) {
            mSelectedPosition = i;
            mSelectedView = drawerViewHolder.cardView;
            ((CardView) mSelectedView).setCardBackgroundColor(
                    AppExtension.getInstance().getResources().
                            getColor(R.color.DrawerSelectedBackground));
        }
    }


    public void selectPosition(int position) {
        mSelectedPosition = position;
        notifyItemChanged(position);
    }

    @Override
    public int getItemCount() {
        return mData != null ? mData.size() : 0;
    }

    //表示 Recycler 里的每一项的容器
    public static class DrawerViewHolder extends RecyclerView.ViewHolder {
        public TextView textView;
        public ImageView imageView;
        public CardView cardView;
        public LinearLayout linearLayout;
        public ImageView selectRectImage;

        public DrawerViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.item_name);
            imageView = (ImageView) itemView.findViewById(R.id.item_icon);
            cardView = (CardView) itemView.findViewById(R.id.navigation_card_view);
            linearLayout = (LinearLayout) cardView.findViewById(R.id.navigationitem_layout);
            selectRectImage = (ImageView) itemView.findViewById(R.id.item_select);
        }
    }

}