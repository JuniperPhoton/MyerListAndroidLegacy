package fragment;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.textservice.SpellCheckerInfo;

import com.example.juniper.myerlistandroid.R;
import com.example.juniper.myerlistandroid.ToDoListAdapter;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import helper.ConfigHelper;
import helper.PostHelper;
import model.Schedule;

public class ToDoFragment extends Fragment
{

    private RecyclerView mToDoList;
    private View mFragmentContainerView;
    private OnFragmentInteractionListener mListener;
    private List<Schedule> mMySchedules;
    private SwipeRefreshLayout mRefreshLayout;

    public ToDoFragment()
    {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (getArguments() != null)
        {
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_to_do, container, false);
        mToDoList=(RecyclerView)view.findViewById(R.id.todoList);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        mToDoList.setLayoutManager(layoutManager);
        mToDoList.setHasFixedSize(true);

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

    public void SetUpData(List<Schedule> data)
    {
        mMySchedules=data;
        mToDoList.setAdapter(new ToDoListAdapter(mMySchedules));
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
        PostHelper.GetAllSchedules(getActivity(), ConfigHelper.getString(getActivity(), "sid"),ConfigHelper.getString(getActivity(),"access_token"));
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri)
    {
        if (mListener != null)
        {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        try
        {
            mListener = (OnFragmentInteractionListener) activity;
        }
        catch (ClassCastException e)
        {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener
    {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

}
