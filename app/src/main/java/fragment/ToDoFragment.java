package fragment;

import android.app.Activity;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.juniperphoton.myerlistandroid.R;

import org.json.JSONObject;

import java.util.ArrayList;

import activity.MainActivity;
import api.CloudServices;
import interfaces.IRequestCallback;
import util.AppUtil;
import util.ConfigHelper;
import util.AppExtension;
import adapter.ToDoListAdapter;
import util.SerializerHelper;
import util.ToDoListGlobalLocator;
import model.ToDo;

public class ToDoFragment extends Fragment {
    private Activity mActivity;
    public RecyclerView mToDoRecyclerView;
    private ArrayList<ToDo> mMyToDos;
    private SwipeRefreshLayout mRefreshLayout;
    private FloatingActionButton mAddingFab;

    private LinearLayout mNoItemLayout;
    private LinearLayout mAddingPaneLayout;

    public ToDoFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_to_do, container, false);

        //拿到主列表控件
        mToDoRecyclerView = (RecyclerView) view.findViewById(R.id.todoList);

        mNoItemLayout = (LinearLayout) view.findViewById(R.id.no_item_layout);
        mAddingPaneLayout = (LinearLayout) view.findViewById(R.id.fragment_todo_adding_pane);

        //设置布局
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        mToDoRecyclerView.setLayoutManager(layoutManager);
        mToDoRecyclerView.setHasFixedSize(true);

        //设置下拉刷新控件
        mRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swiperefresh);
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (ConfigHelper.ISOFFLINEMODE) {
                    mRefreshLayout.setRefreshing(false);
                    return;
                }
                getAllSchedules();
            }
        });

        //设置 FAB
        mAddingFab = (FloatingActionButton) view.findViewById(R.id.pink_icon);
        mAddingFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity) mActivity).showAddingPane();
            }
        });
        if (!ConfigHelper.getBoolean(AppExtension.getInstance(), "HandHobbit")) {
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
            layoutParams.setMargins(16, 0, 0, 16);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            mAddingFab.setLayoutParams(layoutParams);
        }

        ((MainActivity) mActivity).onInitial();

        return view;
    }

    public void showNoItemHint(boolean show) {
        if (show) {
            mNoItemLayout.setVisibility(View.VISIBLE);
        }
        else {
            mNoItemLayout.setVisibility(View.GONE);
        }
    }

    public void updateData(ArrayList<ToDo> data) {
        mMyToDos = data;
        if (mToDoRecyclerView != null) {
            mToDoRecyclerView.setAdapter(new ToDoListAdapter(mMyToDos, mActivity, this));
            stopRefreshing();
            if (data.size() == 0)
                showNoItemHint(true);
            else
                showNoItemHint(false);
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

    public void getAllSchedules() {
        ((MainActivity) mActivity).syncCateAndList();

        if (!ConfigHelper.ISOFFLINEMODE && AppUtil.isNetworkAvailable(AppExtension.getInstance())) {
            if (ToDoListGlobalLocator.StagedList == null) return;
            ((MainActivity) mActivity).setIsAddStagedItems(true);
            for (ToDo todo : ToDoListGlobalLocator.StagedList) {
                CloudServices.addToDo(
                        ConfigHelper.getString(AppExtension.getInstance(), "sid"),
                        ConfigHelper.getString(AppExtension.getInstance(), "access_token"),
                        todo.getContent(),
                        "0",
                        todo.getCate(),
                        new IRequestCallback() {
                            @Override
                            public void onResponse(JSONObject jsonObject) {
                                ((MainActivity) mActivity).onAddedResponse(jsonObject);
                            }
                        });
            }
            ToDoListGlobalLocator.StagedList.clear();
            SerializerHelper.serializeToFile(AppExtension.getInstance(), ToDoListGlobalLocator.StagedList, SerializerHelper.stagedFileName);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mActivity = activity;
        }
        catch (ClassCastException e) {

        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

}
