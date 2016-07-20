package adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.chad.library.adapter.base.BaseItemDraggableAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.juniperphoton.myerlistandroid.R;

import java.util.List;

import model.ToDoCategory;
import view.CircleView;

/**
 * Created by JuniperPhoton on 2016-07-19.
 */
public class CateListAdapter extends BaseItemDraggableAdapter<ToDoCategory> {

    private final int IS_NORMAL = 0;
    private final int IS_HEADER = 1;

    public CateListAdapter(List data) {
        super(R.layout.row_cate_per_item, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, ToDoCategory item) {
        CateListViewHolder cateListViewHolder=(CateListViewHolder)helper;
        if (cateListViewHolder.getItemViewType() == IS_NORMAL) {
            cateListViewHolder.setCateName(item.getName());
            cateListViewHolder.setCircleColor(item.getColor());
        }
    }

    @Override
    public CateListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == IS_NORMAL) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_cate_per_item, parent, false);
            return new CateListViewHolder(view, viewType);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_cate_per_first_add, parent, false);
            return new CateListViewHolder(view, viewType);
        }
    }

//    @Override
//    public void onBindViewHolder(CateListViewHolder holder, int position) {
//
//    }

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

    public class CateListViewHolder extends BaseViewHolder {

        private CircleView mCateCircle;
        private EditText mTextView;

        public CateListViewHolder(View view, int viewType) {
            super(view);
            if (viewType == IS_NORMAL) {
                mCateCircle = (CircleView) view.findViewById(R.id.raw_cate_per_cv);
                mTextView = (EditText) view.findViewById(R.id.raw_cate_per_tv);
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
