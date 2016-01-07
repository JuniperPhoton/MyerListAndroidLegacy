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

import com.example.juniper.myerlistandroid.R;
import com.getbase.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

import activity.MainActivity;
import util.AppUtil;
import util.ConfigHelper;
import util.ContextUtil;
import util.PostHelper;
import adapter.ToDoListAdapter;
import util.SerializerHelper;
import util.ToDoListRef;
import model.ToDo;

public class ToDoFragment extends Fragment
{
    private Activity mActivity;
    public RecyclerView mToDoRecyclerView;
    private ArrayList<ToDo> mMyToDos;
    private SwipeRefreshLayout mRefreshLayout;
    private FloatingActionButton mAddingFab;

    private LinearLayout mNoItemLayout;
    private LinearLayout mAddingPaneLayout;

    public ToDoFragment()
    {

    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onPause()
    {
        super.onPause();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
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
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener()
        {
            @Override
            public void onRefresh()
            {
                if (ConfigHelper.ISOFFLINEMODE)
                {
                    mRefreshLayout.setRefreshing(false);
                    return;
                }
                GetAllSchedules();
            }
        });

        //设置 FAB
        mAddingFab = (FloatingActionButton) view.findViewById(R.id.pink_icon);
        mAddingFab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                ((MainActivity) mActivity).ShowAddingPane();
            }
        });
        if (!ConfigHelper.getBoolean(ContextUtil.getInstance(), "HandHobbit"))
        {
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
            layoutParams.setMargins(16, 0, 0, 16);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            mAddingFab.setLayoutParams(layoutParams);
        }

        ((MainActivity) mActivity).OnInitial(true);

        return view;
    }

    public void ShowNoItemHint(boolean show)
    {
        if (show)
        {
            mNoItemLayout.setVisibility(View.VISIBLE);
        } else
        {
            mNoItemLayout.setVisibility(View.GONE);
        }
    }

    public void UpdateData(ArrayList<ToDo> data)
    {
        mMyToDos = data;
        if (mToDoRecyclerView != null)
        {
            mToDoRecyclerView.setAdapter(new ToDoListAdapter(mMyToDos, mActivity, this));
            StopRefreshing();
            if (data.size() == 0)
                ShowNoItemHint(true);
            else
                ShowNoItemHint(false);
        }
    }


    public void ShowRefreshing()
    {
        if (mRefreshLayout != null)
        {
            mRefreshLayout.setRefreshing(true);
        }
    }

    public void StopRefreshing()
    {
        if (mRefreshLayout != null)
        {
            mRefreshLayout.setRefreshing(false);
        }
    }

    public void EnableRefresh()
    {
        if (mRefreshLayout != null)
        {
            mRefreshLayout.setEnabled(true);
        }
    }

    public void DisableRefresh()
    {
        if (mRefreshLayout != null)
        {
            mRefreshLayout.setEnabled(false);
        }
    }

    public void GetAllSchedules()
    {
        PostHelper.GetOrderedSchedules(getActivity(), ConfigHelper.getString(getActivity(), "sid"), ConfigHelper.getString(getActivity(), "access_token"));

        if (!ConfigHelper.ISOFFLINEMODE && AppUtil.isNetworkAvailable(ContextUtil.getInstance()))
        {
            if (ToDoListRef.StagedList == null) return;
            ((MainActivity) mActivity).SetIsAddStagedItems(true);
            for (ToDo todo : ToDoListRef.StagedList)
            {
                PostHelper.AddToDo(getActivity(), ConfigHelper.getString(ContextUtil.getInstance(), "sid"), todo.getContent(), "0", todo.getCate());
            }
            ToDoListRef.StagedList.clear();
            SerializerHelper.SerializeToFile(ContextUtil.getInstance(), ToDoListRef.StagedList, SerializerHelper.stagedFileName);
        }
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        try
        {
            mActivity = activity;
        }
        catch (ClassCastException e)
        {

        }
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
    }

}
