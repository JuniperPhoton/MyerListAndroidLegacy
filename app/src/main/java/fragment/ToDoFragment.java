package fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.juniper.myerlistandroid.R;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import activity.MainActivity;
import helper.ConfigHelper;
import helper.ContextUtil;
import helper.PostHelper;
import adapter.ToDoListAdapter;
import model.Schedule;

public class ToDoFragment extends Fragment
{
    private Activity mActivity;
    public RecyclerView mToDoRecyclerView;
    private View mFragmentContainerView;
    private ArrayList<Schedule> mMySchedules;
    private SwipeRefreshLayout mRefreshLayout;
    private com.getbase.floatingactionbutton.FloatingActionButton add_fab;

    private AlertDialog mDialog;
    private EditText mNewMemoText;

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
                if(ConfigHelper.ISOFFLINEMODE)
                {
                    mRefreshLayout.setRefreshing(false);
                    return;
                }
                GetAllSchedules();
            }
        });


        add_fab=(com.getbase.floatingactionbutton.FloatingActionButton)view.findViewById(R.id.pink_icon);
        add_fab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                    View dialogView=(View)LayoutInflater.from(getActivity()).inflate(R.layout.add_todo_dialog, (ViewGroup)getActivity().findViewById(R.id.dialog_title));

                    TextView titleText=(TextView)dialogView.findViewById(R.id.dialog_title_text);
                    titleText.setText(getResources().getString(R.string.new_memo_title));

                    mNewMemoText=(EditText)dialogView.findViewById(R.id.newMemoEdit);
                    mNewMemoText.setHint(R.string.new_memo_hint);

                    Button okBtn=(Button)dialogView.findViewById(R.id.add_ok_btn);
                    okBtn.setText(R.string.ok_btn);
                    okBtn.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View view)
                        {
                            mDialog.dismiss();
                            if(ConfigHelper.ISOFFLINEMODE)
                            {
                                Schedule newToAdd=new Schedule();
                                newToAdd.setContent(mNewMemoText.getText().toString());
                                newToAdd.setIsDone(false);
                                newToAdd.setID(java.util.UUID.randomUUID().toString());
                                ((MainActivity)getActivity()).OnAddedResponse(true, newToAdd);
                            }
                            else
                            {
                                PostHelper.AddMemo(getActivity(), ConfigHelper.getString(getActivity(), "sid"), mNewMemoText.getText().toString(), "0");
                            }

                        }
                    });

                    Button cancelBtn=(Button)dialogView.findViewById(R.id.add_cancel_btn);
                    cancelBtn.setText(R.string.cancel_btn);
                    cancelBtn.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View view)
                        {
                            if (mDialog != null)
                            {
                                mDialog.dismiss();
                            }
                        }
                    });

                    if(!ConfigHelper.getBoolean(ContextUtil.getInstance(),"HandHobbit"))
                    {
                        LinearLayout linearLayout=(LinearLayout)dialogView.findViewById(R.id.dialog_btn_layout);

                        RelativeLayout.LayoutParams layoutParams=new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT);
                        layoutParams.setMargins(20,0,0,0);
                        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                        linearLayout.setLayoutParams(layoutParams);
                    }

                    AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
                    mDialog=builder.setView((dialogView)).show();

                    if(ConfigHelper.getBoolean(ContextUtil.getInstance(),"ShowKeyboard"))
                    {
                        mNewMemoText.requestFocus();
                        Timer timer=new Timer();
                        timer.schedule(new TimerTask()
                        {
                            @Override
                            public void run()
                            {
                                InputMethodManager inputMethodManager = (InputMethodManager) mNewMemoText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                                inputMethodManager.showSoftInput(mNewMemoText, 0);
                            }
                        },333);
                    }
            }
        });
        if(!ConfigHelper.getBoolean(ContextUtil.getInstance(),"HandHobbit"))
        {
           RelativeLayout.LayoutParams layoutParams=new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT);
            layoutParams.setMargins(16,0,0,16);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            add_fab.setLayoutParams(layoutParams);
        }

        ((OnCreatedTodoViewListener)mActivity).OnCreatedToDo(true);

        return view;
    }

    public void SetUpData(ArrayList<Schedule> data)
    {
        mMySchedules=data;
        if(mToDoRecyclerView!=null)
        {
            mToDoRecyclerView.setAdapter(new ToDoListAdapter(mMySchedules,mActivity,this));
            StopRefreshing();
        }
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

    public void EnableRefresh()
    {
        if(mRefreshLayout!=null)
        {
            mRefreshLayout.setEnabled(true);
        }
    }

    public void DisableRefresh()
    {
        if(mRefreshLayout!=null)
        {
            mRefreshLayout.setEnabled(false);
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
            mActivity=activity;
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

    public interface OnCreatedTodoViewListener
    {
        void OnCreatedToDo(boolean b);
    }

}
