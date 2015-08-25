package activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;


import adapter.NavigationDrawerOtherCallbacks;
import fragment.DeletedItemFragment;
import fragment.NavigationDrawerFragment;
import com.example.juniper.myerlistandroid.R;
import com.umeng.analytics.MobclickAgent;


import fragment.ToDoFragment;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

import helper.AppHelper;
import helper.ConfigHelper;
import helper.ContextUtil;
import helper.GetScheduleAsyncTask;
import helper.PostHelper;
import helper.PostHelper.OnGetSchedulesCallback;
import adapter.NavigationDrawerCallbacks;
import adapter.ToDoListAdapter;
import helper.SerializerHelper;
import model.Schedule;
import model.ScheduleList;


public class MainActivity extends ActionBarActivity implements
        NavigationDrawerCallbacks,
        OnGetSchedulesCallback,
        PostHelper.OnLoginResponseCallback,
        PostHelper.OnAddedMemoCallback,
        PostHelper.OnSetOrderCallback,
        PostHelper.OnDoneCallback,
        PostHelper.OnDeleteCallback,
        NavigationDrawerFragment.DrawerStatusListener,
        NavigationDrawerOtherCallbacks,
        DeletedItemFragment.OnCreatedViewListener,
        ToDoFragment.OnCreatedTodoViewListener,
        PostHelper.OnUpdateContentCallback
{

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;
    private ToDoFragment mToDoFragment;
    private DeletedItemFragment mDeletedItemFragment;

    private Toolbar mToolbar;
    private MainActivity mInstance;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_main);

        mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(mToolbar);

        mNavigationDrawerFragment = (NavigationDrawerFragment) getFragmentManager().findFragmentById(R.id.fragment_drawer);
        mNavigationDrawerFragment.setup(R.id.fragment_drawer, (DrawerLayout) findViewById(R.id.drawer), mToolbar);

        mInstance=this;

        String access_token=ConfigHelper.getString(this,"access_token");
        boolean offline=ConfigHelper.getBoolean(this,"offline_mode");
        ConfigHelper.ISOFFLINEMODE=offline;
        if(!offline && access_token==null )
        {
            ConfigHelper.ISOFFLINEMODE=false;
            Intent intent=new Intent(this, StartActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                    Intent.FLAG_ACTIVITY_CLEAR_TASK |
                    Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
        else if(access_token!=null )
        {
            InitialFragment(savedInstanceState,true);
        }
        else
        {
             ConfigHelper.ISOFFLINEMODE=true;
             mNavigationDrawerFragment.SetupOfflineMode();
             InitialFragment(savedInstanceState, false);
        }
    }

    private void InitialFragment(Bundle savedInstanceState,boolean isLogined)
    {

        if (findViewById(R.id.fragment_container) != null)
        {
            if (savedInstanceState != null)
            {
                return;
            }

            mToDoFragment=new ToDoFragment();

            getFragmentManager().beginTransaction().replace(R.id.fragment_container,mToDoFragment).commit();

            if(isLogined)
            {
                mToDoFragment.ShowRefreshing();
                //PostHelper.GetOrderedSchedules(this, ConfigHelper.getString(this, "sid"), ConfigHelper.getString(this, "access_token"));
                try
                {
                    ArrayList list= new GetScheduleAsyncTask(this).execute().get();
                    if(list!=null)
                    {
                       OnGotScheduleResponse(list);
                    }
                }
                catch (Exception e)
                {

                }
            }
        }

    }

    @Override
    public void onResume()
    {
        super.onResume();
        MobclickAgent.onResume(this);
    }
    @Override

    public void onPause()
    {
        super.onPause();
        MobclickAgent.onPause(this);
    }


    @Override
    public void onNavigationDrawerItemSelected(int position)
    {
        switch (position)
        {
            case 0:
            {
                   try
                   {
                               if (mToDoFragment == null)
                               {
                                   mToDoFragment = new ToDoFragment();
                               }
                               // mToDoFragment.GetAllSchedules();
                               getFragmentManager().beginTransaction()
                                       .replace(R.id.fragment_container, mToDoFragment).commit();

                               getSupportActionBar().setTitle("MyerList");
                   }
                   catch (Exception E)
                   {
                        E.printStackTrace();
                   }

            };break;
            case 1:
            {
                if(mDeletedItemFragment==null)
                {
                    mDeletedItemFragment=new DeletedItemFragment();
                }

                getFragmentManager().beginTransaction().replace(R.id.fragment_container, mDeletedItemFragment).commit();
                getSupportActionBar().setTitle(getResources().getString(R.string.deleteditems));

            };break;
        }
    }


    @Override
    public void OnSelectedOther(int position)
    {
        switch (position)
        {
            case 0:
            {
                Intent intent=new Intent(getApplicationContext(),SettingActivity.class);
                startActivity(intent);
            };break;
            case 1:
            {
                Intent intent=new Intent(getApplicationContext(),AboutActivity.class);
                startActivity(intent);
            };break;
            case 2:
            {
                AlertDialog.Builder builder=new AlertDialog.Builder(this);
                builder.setTitle(R.string.logout_title);
                builder.setMessage(R.string.logout_content);
                builder.setPositiveButton(getResources().getString(R.string.ok_btn), new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i)
                    {
                        ConfigHelper.putBoolean(getApplicationContext(), "offline_mode", false);
                        ConfigHelper.DeleteKey(getApplicationContext(), "email");
                        ConfigHelper.DeleteKey(getApplicationContext(), "salt");
                        ConfigHelper.DeleteKey(getApplicationContext(),"access_token");
                        Intent intent = new Intent(getApplicationContext(), StartActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                });
                builder.setNegativeButton(getResources().getString(R.string.cancel_btn), new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i)
                    {
                        dialogInterface.dismiss();

                    }
                });
                builder.create().show();

            };break;
        }
    }

    @Override
    public void onBackPressed()
    {
        if (mNavigationDrawerFragment.isDrawerOpen())
        {
            mNavigationDrawerFragment.closeDrawer();
        }
        else if(mNavigationDrawerFragment.getCurrentSelectedPosition()==1)
        {
            mNavigationDrawerFragment.openDrawer();
        }
        else
        {
           super.onBackPressed();
        }

    }


    @Override
    public void OnGotScheduleResponse(ArrayList<Schedule> mytodosList)
    {
        if(mytodosList!=null)
        {
            ScheduleList.TodosList=mytodosList;
            mToDoFragment.SetUpData(mytodosList);
            mToDoFragment.StopRefreshing();

            SerializerHelper.SerializeToFile(ContextUtil.getInstance(), ScheduleList.TodosList, SerializerHelper.todosFileName);
        }
    }

    @Override
    public void OnLoginResponse(boolean value)
    {
        if(value)
        {
            try
            {
                ArrayList list=new GetScheduleAsyncTask(this).execute().get();
                OnGotScheduleResponse(list);
            }
           catch (Exception e)
           {

           }
            //PostHelper.GetOrderedSchedules(this, ConfigHelper.getString(this, "sid"), ConfigHelper.getString(this, "access_token"));
        }
        else
        {
            AppHelper.ShowShortToast("Fail to login.");
        }
    }

    @Override
    public void OnAddedResponse(boolean isSuccess, Schedule newTodo)
    {
        if(isSuccess)
        {
            ToDoListAdapter adapter=(ToDoListAdapter)mToDoFragment.mToDoRecyclerView.getAdapter();
            adapter.addToDos(newTodo);
            AppHelper.ShowShortToast(getResources().getString(R.string.add_success));

            PostHelper.SetListOrder(this,ConfigHelper.getString(this,"sid"),Schedule.getOrderString(adapter.getListSrc()));
        }
       else
        {
            AppHelper.ShowShortToast("Fail to add memo :-(");
        }
    }

    @Override
    public void OnSetOrderResponse(boolean isSuccess)
    {
        if(isSuccess)
        {

        }

    }

    @Override
    public void OnDoneResponse(boolean isSuccess)
    {

    }

    @Override
    public void OnDeleteResponse(boolean isSuccess)
    {

    }

    @Override
    public void OnDrawerStatusChanged(boolean isOpen)
    {
        ToDoListAdapter adapter=(ToDoListAdapter)mToDoFragment.mToDoRecyclerView.getAdapter();
        if(adapter!=null) adapter.SetCanOperate(isOpen);
    }

    @Override
    public void OnCreatedDeleted(boolean b)
    {
        mDeletedItemFragment.SetUpData(ScheduleList.DeletedList);
        if(ScheduleList.DeletedList.size()==0)
        {
            mDeletedItemFragment.ShowNoItemHint();
        }
        else mDeletedItemFragment.HideNoItemHint();
    }

    @Override
    public void OnCreatedToDo(boolean b)
    {
        ArrayList<Schedule> list= SerializerHelper.DeSerializeFromFile(this, SerializerHelper.todosFileName);
        if(list!=null)
        {
            ScheduleList.TodosList=list;
        }

        if(ConfigHelper.ISLOADLISTONCE)
        {
            mToDoFragment.SetUpData(ScheduleList.TodosList);
        }
        else
        {
            ConfigHelper.ISLOADLISTONCE=true;
            if(!ConfigHelper.ISOFFLINEMODE)
            {
                mToDoFragment.ShowRefreshing();

                try
                {
                    ArrayList newlist=new GetScheduleAsyncTask(this).execute().get();
                    OnGotScheduleResponse(newlist);
                    //PostHelper.GetOrderedSchedules(this, ConfigHelper.getString(this, "sid"), ConfigHelper.getString(this, "access_token"));
                }
                catch (Exception e)
                {

                }

            }
            else
            {
                mToDoFragment.SetUpData(ScheduleList.TodosList);
            }
        }
    }

    @Override
    public void OnUpdateContent(boolean isSuccess)
    {
    }
}
