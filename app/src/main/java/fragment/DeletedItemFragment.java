package fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.example.juniper.myerlistandroid.R;

import java.util.ArrayList;

import adapter.DeletedListAdapter;
import interfaces.IOnAddedToDo;
import interfaces.IOnReAddedToDo;
import model.ToDo;
import model.ToDoListHelper;


public class DeletedItemFragment extends Fragment
{

    private ArrayList<ToDo> mDeletedData;
    private RecyclerView mDeletedListRecyclerView;
    private com.getbase.floatingactionbutton.FloatingActionButton mFab;
    private IOnReAddedToDo mActivity;
    private LinearLayout mNoItemHintLayout;

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
                AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
                builder.setTitle(R.string.logout_title);
                builder.setMessage(R.string.deleteall_alert);
                builder.setPositiveButton(R.string.ok_btn, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i)
                    {
                        ((DeletedListAdapter) mDeletedListRecyclerView.getAdapter()).DeleteAll();
                    }
                });
                builder.setNegativeButton(R.string.cancel_btn, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i)
                    {
                        dialogInterface.dismiss();
                    }
                });
                builder.create().show();
            }
        });
        mNoItemHintLayout=(LinearLayout)view.findViewById(R.id.no_deleteditem_layout);
        mActivity.OnReCreatedToDo(true);

        return view;
    }


    public void SetUpData(ArrayList<ToDo> data)
    {
        ToDoListHelper.DeletedList=data;
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
            mActivity=(IOnReAddedToDo)activity;
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

}
