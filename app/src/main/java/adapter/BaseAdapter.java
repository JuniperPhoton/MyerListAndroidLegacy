package adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.ArrayList;

/**
 * Created by JuniperPhoton on 2016-07-19.
 */
public abstract class BaseAdapter<T, T2 extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<T2> {

    protected ArrayList<T> mData;
    protected Context mContext;

    public BaseAdapter(Context context) {
        mContext = context;
    }

    public BaseAdapter(Context context, ArrayList<T> data) {
        mContext = context;
        mData = data;
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    public void addItem(T item) {
        mData.add(item);
        notifyItemInserted(mData.size()-1);
    }

    public T getItem(int index) {
        return mData.get(index);
    }

    public void refreshData(ArrayList<T> data) {
        mData = data;
    }
}