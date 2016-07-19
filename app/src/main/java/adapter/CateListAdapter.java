package adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.juniperphoton.myerlistandroid.R;

import java.util.ArrayList;

import model.ToDoCategory;
import view.CircleView;

/**
 * Created by JuniperPhoton on 2016-07-19.
 */
public class CateListAdapter extends BaseAdapter<ToDoCategory, CateListAdapter.CateListViewHolder> {

    private final int IS_NORMAL = 0;
    private final int IS_HEADER = 1;

    public CateListAdapter(Context context) {
        super(context);
    }

    public CateListAdapter(Context context, ArrayList<ToDoCategory> data) {
        super(context, data);
    }

    @Override
    public CateListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == IS_NORMAL) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.raw_cate_per_item, parent, false);
            return new CateListViewHolder(view, viewType);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.raw_cate_per_first_add, parent, false);
            return new CateListViewHolder(view, viewType);
        }
    }

    @Override
    public void onBindViewHolder(CateListViewHolder holder, int position) {
        if (holder.getItemViewType() == IS_NORMAL) {
            ToDoCategory category = getItem(position);
            holder.setCateName(category.getName());
            holder.setCircleColor(category.getColor());
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return IS_HEADER;
        } else return IS_NORMAL;
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }



    public class CateListViewHolder extends RecyclerView.ViewHolder {

        private CircleView mCateCircle;
        private EditText mTextView;

        public CateListViewHolder(View view, int viewType) {
            super(view);
            if (viewType == IS_NORMAL) {
                mCateCircle = (CircleView) view.findViewById(R.id.cateitem_circleView);
                mTextView = (EditText) view.findViewById(R.id.cateitem_textView);
            }
        }

        public void setCircleColor(int color) {
            mCateCircle.setEllipseColor(color);
        }

        public void setCateName(String name) {
            mTextView.setText(name);
        }
    }
}
