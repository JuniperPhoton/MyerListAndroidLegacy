package adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.chad.library.adapter.base.BaseItemDraggableAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.juniperphoton.jputils.SerializerHelper;
import com.juniperphoton.myerlistandroid.R;

import org.json.JSONObject;

import java.util.ArrayList;

import activity.MainActivity;
import api.CloudServices;
import fragment.DeletedItemFragment;
import interfaces.IRequestCallback;
import util.AppConfig;
import common.App;
import model.ToDo;
import util.GlobalListLocator;
import util.SerializationName;
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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_deleted, parent, false);
        return new DeleteItemViewHolder(view);
    }

    @Override
    protected void convert(BaseViewHolder baseViewHolder, final ToDo toDo) {

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

                SerializerHelper.serializeToFile(App.getInstance(), mData, SerializationName.DELETED_FILE_NAME);
            }
        });
        holder.mReDoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                GlobalListLocator.TodosList.add(toDo);

                //非离线模式下，同步
                if (AppConfig.canSync()) {
                    CloudServices.addToDo(AppConfig.getSid(),
                            AppConfig.getAccessToken(),
                            mData.get(position).getContent(),
                            toDo.getIsDone() ? "1" : "0",
                            toDo.getCate(),
                            new IRequestCallback() {
                                @Override
                                public void onResponse(JSONObject jsonObject) {
                                    ((MainActivity) mCurrentActivity).onReCreatedToDo(jsonObject);
                                }
                            });
                }

                mData.remove(toDo);
                notifyItemRemoved(position);

                if (mData.size() == 0) {
                    mDeletedItemFragment.showNoItemHint();
                }

                SerializerHelper.serializeToFile(App.getInstance(), mData, SerializationName.DELETED_FILE_NAME);
                SerializerHelper.serializeToFile(App.getInstance(), GlobalListLocator.TodosList, SerializationName.TODOS_FILE_NAME);
            }
        });
    }


    public void deleteAll() {
        try {
            notifyItemRangeRemoved(0, mData.size());
            mData.clear();
            mDeletedItemFragment.showNoItemHint();
            SerializerHelper.serializeToFile(App.getInstance(), mData, SerializationName.DELETED_FILE_NAME);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return mData != null ? mData.size() : 0;
    }
}
