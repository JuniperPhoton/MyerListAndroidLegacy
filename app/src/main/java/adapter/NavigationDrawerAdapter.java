package adapter;

import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.juniperphoton.myerlistandroid.R;

import java.util.List;
import java.util.Queue;

import interfaces.INavigationDrawerCallback;
import model.ToDoCategory;
import util.AppExtension;
import view.CircleView;


public class NavigationDrawerAdapter extends RecyclerView.Adapter<NavigationDrawerAdapter.DrawerViewHolder> {

    private static final int IS_FOOTER = 3;
    private static final int IS_NORMAL = 1;

    private List<ToDoCategory> mData;

    //这些是 MainActivity
    private INavigationDrawerCallback mINavigationDrawerCallback;

    //选中的 View
    private CardView mSelectedCardView;

    //选中的项
    private int mSelectedPosition;

    public NavigationDrawerAdapter(List<ToDoCategory> data) {
        mData = data;
    }

    //设置回调
    public void setNavigationDrawerCallbacks(INavigationDrawerCallback INavigationDrawerCallback) {
        mINavigationDrawerCallback = INavigationDrawerCallback;
    }

    //创建每一项的容器
    @Override
    public DrawerViewHolder onCreateViewHolder(ViewGroup viewGroup, final int viewType) {

        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_drawer, viewGroup, false);
        final DrawerViewHolder drawerViewHolder = new DrawerViewHolder(v, viewType);

        //点击选中的时候发生
        drawerViewHolder.rootLayout.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mSelectedPosition = drawerViewHolder.getAdapterPosition();

                        if (viewType == IS_FOOTER) {
                            if (mINavigationDrawerCallback != null) {
                                mINavigationDrawerCallback.onFooterSelected();
                                return;
                            }
                        }

                        //上一个选中的变为透明色
                        if (mSelectedCardView != null) {
                            mSelectedCardView.setCardBackgroundColor(Color.TRANSPARENT);
                        }

                        mSelectedCardView = (CardView) view.getParent();

                        if (mINavigationDrawerCallback != null) {
                            mINavigationDrawerCallback.onDrawerMainItemSelected(mSelectedPosition);
                        }
                    }
                }
        );
        return drawerViewHolder;
    }

    @Override
    public void onBindViewHolder(DrawerViewHolder drawerViewHolder, int position) {
        //设置抽屉每一项的 UI
        drawerViewHolder.textView.setText(mData.get(position).getName());
        drawerViewHolder.cateView.setEllipseColor(mData.get(position).getColor());

        if (mSelectedPosition == position && mINavigationDrawerCallback != null) {
            mSelectedCardView = drawerViewHolder.cardView;
            mSelectedCardView.setCardBackgroundColor(
                    AppExtension.getInstance().getResources().
                            getColor(R.color.DrawerSelectedBackground));
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == mData.size() - 1) {
            return IS_FOOTER;
        } else return IS_NORMAL;
    }

    /**
     * @param position
     */
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
        public CircleView cateView;
        public CardView cardView;
        public LinearLayout rootLayout;

        public int viewType;

        public DrawerViewHolder(View itemView, int type) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.item_name);
            cateView = (CircleView) itemView.findViewById(R.id.item_icon);
            cardView = (CardView) itemView.findViewById(R.id.navigation_card_view);
            rootLayout = (LinearLayout) cardView.findViewById(R.id.navigationitem_layout);
            viewType = type;
        }
    }
}