package adapter;

import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseItemDraggableAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.juniperphoton.jputils.LocalSettingHelper;
import com.juniperphoton.jputils.SerializerHelper;
import com.juniperphoton.myerlistandroid.R;
import com.orhanobut.logger.Logger;

import org.json.JSONObject;

import java.util.ArrayList;

import activity.MainActivity;
import api.CloudServices;
import fragment.ToDoFragment;
import interfaces.IRequestCallback;
import model.ToDoCategory;
import util.AppConfig;
import common.AppExtension;
import model.ToDo;
import util.GlobalListLocator;
import util.SerializationName;
import view.CircleView;


public class ToDoListAdapter extends BaseItemDraggableAdapter<ToDo>{
    private static String TAG = ToDoListAdapter.class.getName();

    private MainActivity mActivity;
    private ToDoFragment mFragment;

    private ArrayList<ToDo> mData;

    public ToDoListAdapter(ArrayList<ToDo> data, MainActivity activity, ToDoFragment fragment) {
        super(data);
        mActivity = activity;
        mFragment = fragment;
        mData = data;
    }

    @Override
    public ToDoItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_todo, parent, false);
        return new ToDoItemViewHolder(v);
    }

    @Override
    public void convert(BaseViewHolder helper, final ToDo currentToDoItem) {

        final ToDoItemViewHolder holder = (ToDoItemViewHolder) helper;
        final ToDo toDoItem = mData.get(holder.getAdapterPosition());

        holder.mTextView.setText(toDoItem.getContent());
        holder.setID(toDoItem.getID());

        final int cateID = toDoItem.getCate();
        ToDoCategory category = GlobalListLocator.getCategoryByCateID(cateID);

        if (cateID == 0) {
            holder.mCateCircle.setColor(ContextCompat.getColor(mActivity, R.color.MyerListBlue));
        } else {
            if (category != null) {
                holder.mCateCircle.setColor(category.getColor());
            } else {
                holder.mCateCircle.setColor(ContextCompat.getColor(mActivity, R.color.MyerListBlue));
            }
        }

        if (!toDoItem.getIsDone()) {
            holder.mLineView.setVisibility(View.GONE);
        }

        holder.mRelativeLayout.setTag(currentToDoItem.getID());
    }

    @Override
    public int getItemCount() {
        return mData != null ? mData.size() : 0;
    }

    private void deleteToDoInternal(int index) {

        ToDo todoToDelete = mData.get(index);

        mData.remove(index);
        notifyItemRemoved(index);

        GlobalListLocator.deleteToDo(todoToDelete.getID());
        GlobalListLocator.DeletedList.add(0, todoToDelete);
        GlobalListLocator.saveData();

        if (AppConfig.canSync()) {
            CloudServices.setDelete(LocalSettingHelper.getString(AppExtension.getInstance(), "sid"),
                    LocalSettingHelper.getString(AppExtension.getInstance(), "access_token"),
                    todoToDelete.getID(),
                    new IRequestCallback() {
                        @Override
                        public void onResponse(JSONObject jsonObject) {
                            //((MainActivity) mActivity).onDelete(jsonObject);
                        }
                    });
        }
    }

    public void addToDo(ToDo todoToAdd) {
        if (todoToAdd == null) return;

        if (LocalSettingHelper.getBoolean(AppExtension.getInstance(), "AddToBottom")) {
            notifyItemInserted(mData.size() - 1);
            GlobalListLocator.TodosList.add(todoToAdd);
        } else {
            notifyItemInserted(0);
            GlobalListLocator.TodosList.add(0, todoToAdd);
        }
        SerializerHelper.serializeToFile(mActivity, mData, SerializationName.TODOS_FILE_NAME);
    }

    public void deleteToDo(String id) {
        int index = 0;
        ToDo todoToDelete = null;
        for (int i = 0; i < mData.size(); i++) {
            ToDo s = mData.get(i);
            if (s.getID().equals(id)) {
                todoToDelete = s;
                index = i;
                break;
            }
        }
        if (todoToDelete != null) {
            deleteToDoInternal(index);
        }
    }

    public void updateToDo(ToDo toDo) {
        GlobalListLocator.updateContent(toDo);

        int position = findToDoInData(toDo.getID());

        mData.get(position).setContent(toDo.getContent());
        mData.get(position).setCate(toDo.getCate());

        notifyItemChanged(position);

        StringBuffer sb = new StringBuffer();
        for (ToDo todo : mData) {
            sb.append(todo.getContent()).append(",");
        }
        Logger.d(sb.toString());

        if (!AppConfig.ISOFFLINEMODE) {
            CloudServices.updateContent(
                    AppConfig.getSid(),
                    AppConfig.getAccessToken(),
                    toDo.getID(),
                    toDo.getContent(),
                    toDo.getCate(),
                    new IRequestCallback() {
                        @Override
                        public void onResponse(JSONObject jsonObject) {
                            mActivity.onUpdateContent(jsonObject);
                        }
                    });
        } else {
            SerializerHelper.serializeToFile(mActivity, mData, SerializationName.TODOS_FILE_NAME);
        }
    }

    private int findToDoInData(String id) {
        int index = 0;
        for (int i = 0; i < mData.size(); i++) {
            if (mData.get(i).getID().equals(id)) {
                index = i;
                break;
            }
        }
        return index;
    }

    public ArrayList<ToDo> getData() {
        return mData;
    }

    public class ToDoItemViewHolder extends BaseViewHolder {

        private String mId;
        public TextView mTextView;
        public ImageView mLineView;
        public RelativeLayout mRelativeLayout;
        public ImageView mGreenImageView;
        public ImageView mRedImageView;
        public CircleView mCateCircle;

        public ToDoItemViewHolder(View itemView) {
            super(itemView);
            mTextView = (TextView) itemView.findViewById(R.id.todoBlock);
            mLineView = (ImageView) itemView.findViewById(R.id.lineView);
            mGreenImageView = (ImageView) itemView.findViewById(R.id.greenImageView);
            mRedImageView = (ImageView) itemView.findViewById(R.id.redImageView);
            mRelativeLayout = (RelativeLayout) itemView.findViewById(R.id.todo_layout);
            mCateCircle = (CircleView) itemView.findViewById(R.id.cateCircle);
        }

        public String getID() {
            return mId;
        }

        public void setID(String id) {
            this.mId = id;
        }
    }
}
