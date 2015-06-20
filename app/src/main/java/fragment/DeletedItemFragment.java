package fragment;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.example.juniper.myerlistandroid.R;

import java.nio.channels.SelectableChannel;
import java.util.ArrayList;

import adapter.DeletedListAdapter;
import model.Schedule;
import model.ScheduleList;


public class DeletedItemFragment extends Fragment
{

    private ArrayList<Schedule> mDeletedData;
    private RecyclerView mDeletedListRecyclerView;
    private com.getbase.floatingactionbutton.FloatingActionButton mFab;
    private OnCreatedViewListener mActivity;
    private LinearLayout mNoItemHintLayout;

    public static DeletedItemFragment newInstance(String param1, String param2)
    {
        DeletedItemFragment fragment = new DeletedItemFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public DeletedItemFragment()
    {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View view=inflater.inflate(R.layout.fragment_deleted_item,container,false);
        mDeletedListRecyclerView =(RecyclerView)view.findViewById(R.id.deletedList);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mDeletedListRecyclerView.setLayoutManager(layoutManager);
        mDeletedListRecyclerView.setHasFixedSize(true);
        mFab=(com.getbase.floatingactionbutton.FloatingActionButton)view.findViewById(R.id.delete_all_fab);
        mFab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                ((DeletedListAdapter)mDeletedListRecyclerView.getAdapter()).DeleteAll();
            }
        });
        mNoItemHintLayout=(LinearLayout)view.findViewById(R.id.no_deleteditem_layout);
        mActivity.OnCreated(true);

        return view;
    }


    public void SetUpData(ArrayList<Schedule> data)
    {
        ScheduleList.DeletedList=data;
        mDeletedData=data;
        DeletedListAdapter deletedListAdapter=new DeletedListAdapter(getActivity(),this,data);

        mDeletedListRecyclerView.setAdapter(deletedListAdapter);

        if(data.size()==0)
        {
            ShowNoItemHint();
        }
    }

    public void ShowNoItemHint()
    {
        mNoItemHintLayout.setVisibility(View.VISIBLE);
    }

    public void HideNoItemHint()
    {
        mNoItemHintLayout.setVisibility(View.GONE);
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        try
        {
            mActivity=(OnCreatedViewListener)activity;
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
    }

    public interface OnCreatedViewListener
    {
        void OnCreated(boolean b);
    }
}
