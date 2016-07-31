package adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.chad.library.adapter.base.BaseItemDraggableAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.juniperphoton.myerlistandroid.R;

import java.util.List;

import model.ToDo;
import model.ToDoCategory;
import util.AppExtension;
import view.CircleView;
import viewholder.CateListViewHolder;

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
    protected void convert(BaseViewHolder helper, final ToDoCategory item) {
        CateListViewHolder cateListViewHolder = (CateListViewHolder) helper;
        if (cateListViewHolder.getItemViewType() == IS_NORMAL) {
            cateListViewHolder.setCateName(item.getName());
            cateListViewHolder.setCircleColor(item.getColor());
            cateListViewHolder.getDeleteView().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteById(item.getID());
                }
            });
        } else if (cateListViewHolder.getItemViewType() == IS_HEADER) {
            cateListViewHolder.getRootView().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addNewCate();
                }
            });
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

    private int getNewId() {
        int maxId = 0;
        if (mData != null && mData.size() > 0) {
            for (int i = 0; i < mData.size(); i++) {
                maxId = mData.get(i).getID() > maxId ? mData.get(i).getID() : maxId;
            }
        }
        return ++maxId;
    }

    private void addNewCate() {
        ToDoCategory newCategory = new ToDoCategory(AppExtension.getInstance().getResources().getString(R.string.new_cate_name),
                getNewId(),
                ContextCompat.getColor(AppExtension.getInstance(), R.color.MyerListBlue));
        mData.add(newCategory);
        notifyItemInserted(mData.size());
    }

    private void deleteById(int id) {
        int location = -1;
        for (int i = 0; i < mData.size(); i++) {
            ToDoCategory cate = mData.get(i);
            if (cate.getID() == id) {
                location = i;
            }
        }
        if (location != -1) {
            mData.remove(location);
            notifyItemRemoved(location);
        }
    }
}


