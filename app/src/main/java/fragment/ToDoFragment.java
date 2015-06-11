package fragment;

import android.app.Activity;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.example.juniper.myerlistandroid.R;

import java.util.ArrayList;
import java.util.List;

import helper.ConfigHelper;
import helper.PostHelper;
import middle.ToDoListAdapter;
import model.Schedule;

public class ToDoFragment extends Fragment
{

    public RecyclerView mToDoRecyclerView;
    private View mFragmentContainerView;
    private ArrayList<Schedule> mMySchedules;
    private SwipeRefreshLayout mRefreshLayout;

    public ToDoFragment()
    {
        // Required empty public constructor
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_to_do, container, false);
        mToDoRecyclerView =(RecyclerView)view.findViewById(R.id.todoList);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        mToDoRecyclerView.setLayoutManager(layoutManager);
        mToDoRecyclerView.setHasFixedSize(true);

        mRefreshLayout=(SwipeRefreshLayout)view.findViewById(R.id.swiperefresh);
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener()
        {
            @Override
            public void onRefresh()
            {
                GetAllSchedules();
            }
        });


        return view;
    }

    public void SetUpData(ArrayList<Schedule> data)
    {
        mMySchedules=data;
        mToDoRecyclerView.setAdapter(new ToDoListAdapter(mMySchedules));

        StopRefreshing();
    }

    public void ShowRefreshing()
    {
        if(mRefreshLayout!=null)
        {
            mRefreshLayout.setRefreshing(true);

        }
    }

    public void StopRefreshing()
    {
        if(mRefreshLayout!=null)
        {
            mRefreshLayout.setRefreshing(false);
        }
    }

    public void GetAllSchedules()
    {
        PostHelper.GetOrderedSchedules(getActivity(), ConfigHelper.getString(getActivity(), "sid"),ConfigHelper.getString(getActivity(),"access_token"));
    }


    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        try
        {
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
