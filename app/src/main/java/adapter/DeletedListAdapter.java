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
import util.ConfigHelper;
import util.AppExtension;
import util.SerializerHelper;
import model.ToDo;
import util.ToDoListRef;


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
                    mDeletedItemFragment.ShowNoItemHint();
                }

                SerializerHelper.SerializeToFile(AppExtension.getInstance(), mDeleteToDos, SerializerHelper.deletedFileName);
            }
        });
        holder.mReDoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!ConfigHelper.ISOFFLINEMODE) {
                    CloudServices.AddToDo(ConfigHelper.getString(AppExtension.getInstance(), "sid"),
                            ConfigHelper.getString(AppExtension.getInstance(), "access_token"),
                            mDeleteToDos.get(position).getContent(), "0", 0,
                            new IRequestCallback() {
                                @Override
                                public void onResponse(JSONObject jsonObject) {
                                    ((MainActivity)mCurrentActivity).OnReCreatedToDo(jsonObject);
                                }
                            });
                }
                else {
                    ToDoListRef.TodosList.add(ToDoListRef.TodosList.size(), mDeleteToDos.get(position));
                }

                mDeleteToDos.remove(currentToDo);
                notifyItemRemoved(position);

                if (mDeleteToDos.size() == 0) {
                    mDeletedItemFragment.ShowNoItemHint();
                }

                SerializerHelper.SerializeToFile(AppExtension.getInstance(), mDeleteToDos, SerializerHelper.deletedFileName);
                SerializerHelper.SerializeToFile(AppExtension.getInstance(), ToDoListRef.TodosList, SerializerHelper.todosFileName);
            }
        });
    }


    public void DeleteAll() {
        try {
            //notifyItemRangeChanged(0, mDeleteToDos.size());
            notifyItemRangeRemoved(0, mDeleteToDos.size());
            mDeleteToDos.clear();
            mDeletedItemFragment.ShowNoItemHint();
            SerializerHelper.SerializeToFile(AppExtension.getInstance(), mDeleteToDos, SerializerHelper.deletedFileName);
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
            mTextView = (TextView) itemView.findViewById(R.id.deletedBlock);
            mReDoView = (ImageView) itemView.findViewById(R.id.redoView);
            mDeleteView = (ImageView) itemView.findViewById(R.id.deleteView);
        }
    }
}
