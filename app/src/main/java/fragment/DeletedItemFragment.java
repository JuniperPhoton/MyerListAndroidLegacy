package fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.juniperphoton.myerlistandroid.R;

import java.util.ArrayList;

import activity.MainActivity;
import adapter.DeletedListAdapter;
import model.ToDo;
import util.GlobalListLocator;

public class DeletedItemFragment extends Fragment {

    private ArrayList<ToDo> mDeletedToDos;
    private com.getbase.floatingactionbutton.FloatingActionButton mFab;
    private MainActivity mActivity;
    private LinearLayout mNoItemHintLayout;

    private RecyclerView mRecyclerView;
    private DeletedListAdapter mAdapter;

    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
        try {
            mActivity = (MainActivity) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_deleted_list, container, false);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.fragment_delete_rv);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);

        mFab = (com.getbase.floatingactionbutton.FloatingActionButton) view.findViewById(R.id.fragment_delete_delete_all_fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(R.string.logout_title);
                builder.setMessage(R.string.deleteall_alert);
                builder.setPositiveButton(R.string.ok_btn, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ((DeletedListAdapter) mRecyclerView.getAdapter()).deleteAll();
                    }
                });
                builder.setNegativeButton(R.string.cancel_btn, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                builder.create().show();
            }
        });
        mNoItemHintLayout = (LinearLayout) view.findViewById(R.id.fragment_delete_noitem_ll);
        setAdapter(GlobalListLocator.DeletedList);

        return view;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public DeletedItemFragment() {

    }

    public void setAdapter(ArrayList<ToDo> data) {
        GlobalListLocator.DeletedList = data;
        mDeletedToDos = data;

        mAdapter = new DeletedListAdapter(mActivity, this, data);

        mRecyclerView.setAdapter(mAdapter);

        if (GlobalListLocator.DeletedList.size() == 0) {
            this.showNoItemHint();
        } else {
            this.hideNoItemHint();
        }
    }

    public void showNoItemHint() {
        mNoItemHintLayout.setVisibility(View.VISIBLE);
    }

    public void hideNoItemHint() {
        mNoItemHintLayout.setVisibility(View.GONE);
    }

}
