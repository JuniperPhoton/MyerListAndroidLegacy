package adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.juniperphoton.myerlistandroid.R;

import org.json.JSONObject;

import java.util.ArrayList;

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


public class DeletedListAdapter extends RecyclerView.Adapter<DeletedListAdapter.DeleteItemViewHolder> {
    private Activity mCurrentActivity;
    private DeletedItemFragment mDeletedItemFragment;
    private ArrayList<ToDo> mDeleteToDos;

    public DeletedListAdapter(Activity activity, DeletedItemFragment deletedItemFragment, ArrayList<ToDo> deletedList) {
        mDeletedItemFragment = deletedItemFragment;
        mCurrentActivity = activity;
        mDeleteToDos = deletedList;
    }

    @Override
    public DeleteItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_deleted, parent, false);
        DeleteItemViewHolder deleteItemViewHolder = new DeleteItemViewHolder(v);
        return deleteItemViewHolder;
    }

    @Override
    public void onBindViewHolder(final DeleteItemViewHolder holder, final int position) {
        final ToDo currentToDo = mDeleteToDos.get(position);
        if (currentToDo == null) return;

        holder.mTextView.setText(currentToDo.getContent());
        holder.mDeleteView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mDeleteToDos.remove(currentToDo);
                notifyItemRemoved(position);

                if (mDeleteToDos.size() == 0) {
                    mDeletedItemFragment.showNoItemHint();
                }

                SerializerHelper.serializeToFile(AppExtension.getInstance(), mDeleteToDos, SerializerHelper.deletedFileName);
            }
        });
        holder.mReDoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //非离线模式下，同步
                if (!ConfigHelper.ISOFFLINEMODE && AppUtil.isNetworkAvailable(AppExtension.getInstance())) {
                    CloudServices.addToDo(ConfigHelper.getSid(),
                            ConfigHelper.getAccessToken(),
                            mDeleteToDos.get(position).getContent(),
                            "0",
                            0,
                            new IRequestCallback() {
                                @Override
                                public void onResponse(JSONObject jsonObject) {
                                    ((MainActivity) mCurrentActivity).onReCreatedToDo(jsonObject);
                                }
                            });
                }
                else {
                    GlobalListLocator.TodosList.add(GlobalListLocator.TodosList.size(), mDeleteToDos.get(position));
                }

                mDeleteToDos.remove(currentToDo);
                notifyItemRemoved(position);

                if (mDeleteToDos.size() == 0) {
                    mDeletedItemFragment.showNoItemHint();
                }

                SerializerHelper.serializeToFile(AppExtension.getInstance(), mDeleteToDos, SerializerHelper.deletedFileName);
                SerializerHelper.serializeToFile(AppExtension.getInstance(), GlobalListLocator.TodosList, SerializerHelper.todosFileName);
            }
        });
    }


    public void deleteAll() {
        try {
            notifyItemRangeRemoved(0, mDeleteToDos.size());
            mDeleteToDos.clear();
            mDeletedItemFragment.showNoItemHint();
            SerializerHelper.serializeToFile(AppExtension.getInstance(), mDeleteToDos, SerializerHelper.deletedFileName);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return mDeleteToDos != null ? mDeleteToDos.size() : 0;
    }

    public static class DeleteItemViewHolder extends RecyclerView.ViewHolder {
        private TextView mTextView;
        private ImageView mReDoView;
        private ImageView mDeleteView;

        public DeleteItemViewHolder(View itemView) {
            super(itemView);
            mTextView = (TextView) itemView.findViewById(R.id.row_deleted_tv);
            mReDoView = (ImageView) itemView.findViewById(R.id.row_deleted_reAdd_iv);
            mDeleteView = (ImageView) itemView.findViewById(R.id.row_deleted_btn_iv);
        }
    }
}
