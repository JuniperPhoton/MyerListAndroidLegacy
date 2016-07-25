package adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseItemDraggableAdapter;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.juniperphoton.myerlistandroid.R;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import activity.MainActivity;
import api.CloudServices;
import fragment.DeletedItemFragment;
import interfaces.IRequestCallback;
import util.AppUtil;
import util.ConfigHelper;
import util.AppExtension;
import util.SerializerHelper;
import model.ToDo;
import util.GlobalListLocator;
import viewholder.CateListViewHolder;
import viewholder.DeleteItemViewHolder;


public class DeletedListAdapter extends BaseItemDraggableAdapter<ToDo> {
    private Activity mCurrentActivity;
    private DeletedItemFragment mDeletedItemFragment;

    public DeletedListAdapter(ArrayList<ToDo> data) {
        super(R.layout.row_deleted, data);
    }

    public DeletedListAdapter(Activity activity, DeletedItemFragment deletedItemFragment, ArrayList<ToDo> data) {
        super(R.layout.row_todo, data);
        mDeletedItemFragment = deletedItemFragment;
        mCurrentActivity = activity;
    }

    @Override
    public DeleteItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_deleted, parent, false);
        return new DeleteItemViewHolder(v);
    }

    @Override
    protected void convert(BaseViewHolder baseViewHolder, final ToDo toDo) {
        if (toDo == null) return;

        DeleteItemViewHolder holder = (DeleteItemViewHolder) baseViewHolder;
        final int position = holder.getAdapterPosition();

        holder.mTextView.setText(toDo.getContent());
        holder.mDeleteView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mData.remove(toDo);
                notifyItemRemoved(position);

                if (mData.size() == 0) {
                    mDeletedItemFragment.showNoItemHint();
                }

                SerializerHelper.serializeToFile(AppExtension.getInstance(), mData, SerializerHelper.deletedFileName);
            }
        });
        holder.mReDoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //非离线模式下，同步
                if (!ConfigHelper.ISOFFLINEMODE && AppUtil.isNetworkAvailable(AppExtension.getInstance())) {
                    CloudServices.addToDo(ConfigHelper.getSid(),
                            ConfigHelper.getAccessToken(),
                            mData.get(position).getContent(),
                            "0",
                            0,
                            new IRequestCallback() {
                                @Override
                                public void onResponse(JSONObject jsonObject) {
                                    ((MainActivity) mCurrentActivity).onReCreatedToDo(jsonObject);
                                }
                            });
                } else {
                    GlobalListLocator.TodosList.add(GlobalListLocator.TodosList.size(), mData.get(position));
                }

                mData.remove(toDo);
                notifyItemRemoved(position);

                if (mData.size() == 0) {
                    mDeletedItemFragment.showNoItemHint();
                }

                SerializerHelper.serializeToFile(AppExtension.getInstance(), mData, SerializerHelper.deletedFileName);
                SerializerHelper.serializeToFile(AppExtension.getInstance(), GlobalListLocator.TodosList, SerializerHelper.todosFileName);
            }
        });
    }


    public void deleteAll() {
        try {
            notifyItemRangeRemoved(0, mData.size());
            mData.clear();
            mDeletedItemFragment.showNoItemHint();
            SerializerHelper.serializeToFile(AppExtension.getInstance(), mData, SerializerHelper.deletedFileName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return mData != null ? mData.size() : 0;
    }
}
