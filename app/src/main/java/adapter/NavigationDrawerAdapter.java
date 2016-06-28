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

import interfaces.INavigationDrawerCallback;
import model.ToDoCategory;
import util.AppExtension;
import util.ToDoListGlobalLocator;
import view.DrawView;


public class NavigationDrawerAdapter extends RecyclerView.Adapter<NavigationDrawerAdapter.DrawerViewHolder> {

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
    public DrawerViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_drawer, viewGroup, false);
        final DrawerViewHolder drawerViewHolder = new DrawerViewHolder(v);

        //点击选中的时候发生
        drawerViewHolder.rootLayout.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mSelectedPosition = drawerViewHolder.getAdapterPosition();

                        //上一个选中的变为透明色
                        if (mSelectedCardView != null) {
                            ((CardView) mSelectedCardView).setCardBackgroundColor(Color.TRANSPARENT);
                        }

                        mSelectedCardView = (CardView) view.getParent();
                        mSelectedCardView.setCardBackgroundColor(AppExtension.getInstance().getResources().
                                getColor(R.color.DrawerSelectedBackground));

                        if (mINavigationDrawerCallback != null) {
                            ToDoCategory category = ToDoListGlobalLocator.CategoryList.
                                    get(drawerViewHolder.getAdapterPosition());
                            mINavigationDrawerCallback.onDrawerMainItemSelected(category.getID());
                        }
                    }
                }
        );
        return drawerViewHolder;
    }

    @Override
    public void onBindViewHolder(DrawerViewHolder drawerViewHolder, int i) {
        //设置抽屉每一项的 UI
        drawerViewHolder.textView.setText(mData.get(i).getName());
        drawerViewHolder.cateView.setEllipseColor(mData.get(i).getColor());

//        if (mSelectedPosition == i && mINavigationDrawerCallback != null) {
//            mSelectedPosition = i;
//            mSelectedCardView = drawerViewHolder.cardView;
//            ((CardView) mSelectedCardView).setCardBackgroundColor(
//                    AppExtension.getInstance().getResources().
//                            getColor(R.color.DrawerSelectedBackground));
//        }
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
        public DrawView cateView;
        public CardView cardView;
        public LinearLayout rootLayout;

        public DrawerViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.item_name);
            cateView = (DrawView) itemView.findViewById(R.id.item_icon);
            cardView = (CardView) itemView.findViewById(R.id.navigation_card_view);
            rootLayout = (LinearLayout) cardView.findViewById(R.id.navigationitem_layout);
        }
    }
}