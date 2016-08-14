package fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.google.gson.reflect.TypeToken;
import com.juniperphoton.jputils.LocalSettingHelper;
import com.juniperphoton.jputils.SerializerHelper;
import com.juniperphoton.myerlistandroid.R;
import com.orhanobut.logger.Logger;

import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;

import activity.MainActivity;
import api.CloudServices;
import interfaces.IRefresh;
import interfaces.IRequestCallback;
import util.AppUtil;
import util.ConfigHelper;
import common.AppExtension;
import adapter.ToDoListAdapter;
import util.GlobalListLocator;
import model.ToDo;
import util.SerializationName;

public class ToDoFragment extends Fragment implements IRefresh {
    private static String TAG = ToDoFragment.class.getName();

    private MainActivity mActivity;
    private RecyclerView mToDoRecyclerView;
    private SwipeRefreshLayout mRefreshLayout;
    private FloatingActionButton mAddingFab;

    private LinearLayout mNoItemLayout;

    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
        Log.d(ToDoFragment.class.getName(), "onAttach");
        try {
            if (activity instanceof MainActivity) {
                mActivity = (MainActivity) activity;
            }
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(ToDoFragment.class.getName(), "onCreate");
        Logger.init(TAG);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_to_do_list, container, false);

        Log.d(ToDoFragment.class.getName(), "onCreateView");

        mNoItemLayout = (LinearLayout) view.findViewById(R.id.fragment_todo_no_item_ll);
        mNoItemLayout.setVisibility(View.GONE);

        //设置下拉刷新控件
        mRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.fragment_todo_refresh_srl);
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (ConfigHelper.ISOFFLINEMODE) {
                    stopRefreshing();
                    return;
                }
                getAllSchedules();
            }
        });

        //设置 FAB
        mAddingFab = (FloatingActionButton) view.findViewById(R.id.fragment_todo_add_fab);
        mAddingFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                StringBuffer sb = new StringBuffer();
                for (ToDo todo : ((ToDoListAdapter)mToDoRecyclerView.getAdapter()).getData()) {
                    sb.append(todo.getContent()).append(",");
                }
                Logger.d(sb.toString());

                mActivity.showAddingPane(null);
            }
        });

        if (!LocalSettingHelper.getBoolean(AppExtension.getInstance(), "HandHobbit")) {
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
            layoutParams.setMargins(16, 0, 0, 16);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            mAddingFab.setLayoutParams(layoutParams);
        }

        onInit();

        initRV(view);

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(ToDoFragment.class.getName(), "onPause");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(ToDoFragment.class.getName(), "onDetach");
    }

    public void onInit() {
        try {
            Type type = new TypeToken<ArrayList<ToDo>>() {
            }.getType();
            ArrayList<ToDo> list = SerializerHelper.deSerializeFromFile(
                    type, AppExtension.getInstance(), SerializationName.TODOS_FILE_NAME);

            if (list != null) {
                GlobalListLocator.TodosList = list;
            }
            //已经登陆了
            if (!ConfigHelper.ISOFFLINEMODE) {
                updateData(GlobalListLocator.TodosList);
            }
            //离线模式
            else {
                updateData(GlobalListLocator.TodosList);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateNoItemUI() {
        if((mToDoRecyclerView.getAdapter())!=null){
            if (((ToDoListAdapter)mToDoRecyclerView.getAdapter()).getData().size() == 0) {
                mNoItemLayout.setVisibility(View.VISIBLE);
            } else {
                mNoItemLayout.setVisibility(View.GONE);
            }
        }
        else{
            mNoItemLayout.setVisibility(View.GONE);
        }
    }

    private void initRV(View view) {
        mToDoRecyclerView = (RecyclerView) view.findViewById(R.id.fragment_todo_rv);
        mToDoRecyclerView.setLayoutManager(new LinearLayoutManager(mActivity, LinearLayoutManager.VERTICAL, false));

        ToDoListAdapter adapter = new ToDoListAdapter(GlobalListLocator.TodosList, mActivity, this);
        mToDoRecyclerView.swapAdapter(adapter, true);
        updateNoItemUI();
    }

    public void updateData(ArrayList<ToDo> data) {
        if (mToDoRecyclerView != null) {
            mToDoRecyclerView.setAdapter(new ToDoListAdapter(data, mActivity, this));

            stopRefreshing();
            updateNoItemUI();
        }
    }

    public void showRefreshing() {
        if (mRefreshLayout != null) {
            mRefreshLayout.setRefreshing(true);
        }
    }

    public void stopRefreshing() {
        if (mRefreshLayout != null) {
            mRefreshLayout.setRefreshing(false);
        }
    }

    public void enableRefresh() {
        if (mRefreshLayout != null) {
            mRefreshLayout.setEnabled(true);
        }
    }

    public void disableRefresh() {
        if (mRefreshLayout != null) {
            mRefreshLayout.setEnabled(false);
        }
    }

    public int getFABRadius() {
        return mAddingFab.getWidth() / 2;
    }

    public int[] getFABPostion() {
        int[] position = new int[2];
        mAddingFab.getLocationOnScreen(position);
        return position;
    }

    public void getAllSchedules() {
        Logger.d(mActivity);
        showRefreshing();
        mActivity.syncCateAndList();

        if (!ConfigHelper.ISOFFLINEMODE && AppUtil.isNetworkAvailable(AppExtension.getInstance())) {
            if (GlobalListLocator.StagedList == null) return;
            mActivity.setIsAddStagedItems(true);
            for (ToDo todo : GlobalListLocator.StagedList) {
                CloudServices.addToDo(ConfigHelper.getSid(), ConfigHelper.getAccessToken(),
                        todo.getContent(), "0", todo.getCate(),
                        new IRequestCallback() {
                            @Override
                            public void onResponse(JSONObject jsonObject) {
                                if (jsonObject != null) Logger.d(jsonObject);
                                stopRefreshing();
                                mActivity.onAddedResponse(jsonObject);
                            }
                        });
            }
            GlobalListLocator.StagedList.clear();
            SerializerHelper.serializeToFile(AppExtension.getInstance(), GlobalListLocator.StagedList, SerializationName.STAGED_FILE_NAME);
        }
    }

    public ArrayList<ToDo> getData() {
        if ((mToDoRecyclerView.getAdapter()) != null) {
            return ((ToDoListAdapter)mToDoRecyclerView.getAdapter()).getData();
        } else {
            return null;
        }
    }

    public void updateContent(ToDo toDo) {
        if ((mToDoRecyclerView.getAdapter()) != null) {
            ((ToDoListAdapter)mToDoRecyclerView.getAdapter()).updateToDo(toDo);
        }
    }

    public void addToDo(ToDo todo) {
        if ((mToDoRecyclerView.getAdapter()) != null) {
            ((ToDoListAdapter)mToDoRecyclerView.getAdapter()).addToDo(todo);
        }
    }
}
