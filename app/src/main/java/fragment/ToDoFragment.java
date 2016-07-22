package fragment;

import android.content.Context;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.chad.library.adapter.base.callback.ItemDragAndSwipeCallback;
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
import util.GlobalListLocator;
import model.ToDo;

public class ToDoFragment extends Fragment {
    private MainActivity mActivity;
    public RecyclerView mToDoRecyclerView;
    private ArrayList<ToDo> mMyToDos;
    private SwipeRefreshLayout mRefreshLayout;
    private FloatingActionButton mAddingFab;

    private LinearLayout mNoItemLayout;
    private RelativeLayout mAddingPaneLayout;

    private ToDoListAdapter mAdapter;
    private ItemTouchHelper mItemTouchHelper;
    private ItemDragAndSwipeCallback mItemDragAndSwipeCallback;

    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
        try {
            if (activity instanceof MainActivity) {
                mActivity = (MainActivity) activity;
            }
        } catch (ClassCastException e) {

        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_to_do, container, false);

        mNoItemLayout = (LinearLayout) view.findViewById(R.id.fragment_todo_no_item_ll);
        mAddingPaneLayout = (RelativeLayout) view.findViewById(R.id.fragment_adding_pane_root_rl);

        mToDoRecyclerView = (RecyclerView) view.findViewById(R.id.fragment_todo_rv);
        mToDoRecyclerView.setLayoutManager(new GridLayoutManager(AppExtension.getInstance(), 1));

        mAdapter = new ToDoListAdapter(new ArrayList<ToDo>(), mActivity, this);
        mItemDragAndSwipeCallback = new ItemDragAndSwipeCallback(mAdapter);
        mItemTouchHelper = new ItemTouchHelper(mItemDragAndSwipeCallback);
        mItemTouchHelper.attachToRecyclerView(mToDoRecyclerView);

        mItemDragAndSwipeCallback.setSwipeMoveFlags(ItemTouchHelper.START | ItemTouchHelper.END);
        mAdapter.enableDragItem(mItemTouchHelper);

        mToDoRecyclerView.setAdapter(mAdapter);

        //设置下拉刷新控件
        mRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.fragment_todo_refresh_srl);
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
        mAddingFab = (FloatingActionButton) view.findViewById(R.id.fragment_todo_add_fab);
        mAddingFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mActivity.showAddingPane(null);
            }
        });

        if (!ConfigHelper.getBoolean(AppExtension.getInstance(), "HandHobbit")) {
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
            layoutParams.setMargins(16, 0, 0, 16);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            mAddingFab.setLayoutParams(layoutParams);
        }

        mActivity.onInit();

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public void showNoItemHint(boolean show) {
        if (show) {
            mNoItemLayout.setVisibility(View.VISIBLE);
        } else {
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

    public int getFABRadius() {
        return mAddingFab.getWidth() / 2;
    }

    public int[] getFABPostion() {
        int[] position = new int[2];
        mAddingFab.getLocationOnScreen(position);
        return position;
    }

    public void getAllSchedules() {
        mActivity.syncCateAndList();

        if (!ConfigHelper.ISOFFLINEMODE && AppUtil.isNetworkAvailable(AppExtension.getInstance())) {
            if (GlobalListLocator.StagedList == null) return;
            mActivity.setIsAddStagedItems(true);
            for (ToDo todo : GlobalListLocator.StagedList) {
                CloudServices.addToDo(
                        ConfigHelper.getSid(),
                        ConfigHelper.getAccessToken(),
                        todo.getContent(),
                        "0",
                        todo.getCate(),
                        new IRequestCallback() {
                            @Override
                            public void onResponse(JSONObject jsonObject) {
                                mActivity.onAddedResponse(jsonObject);
                            }
                        });
            }
            GlobalListLocator.StagedList.clear();
            SerializerHelper.serializeToFile(AppExtension.getInstance(), GlobalListLocator.StagedList, SerializerHelper.stagedFileName);
        }
    }

}
