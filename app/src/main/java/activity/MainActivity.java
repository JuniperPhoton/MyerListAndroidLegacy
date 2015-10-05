package activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;

import interfaces.INavigationDrawerOtherCallbacks;
import fragment.DeletedItemFragment;
import fragment.IINavigationDrawerFragment;

import com.example.juniper.myerlistandroid.R;
import com.umeng.analytics.MobclickAgent;

import fragment.ToDoFragment;

import java.util.ArrayList;

import helper.AppHelper;
import helper.ConfigHelper;
import helper.ContextUtil;
import helper.PostHelper;
import interfaces.INavigationDrawerCallbacks;
import adapter.ToDoListAdapter;
import helper.SerializerHelper;
import interfaces.IRequestCallbacks;
import model.ToDo;
import model.ToDoListHelper;
import moe.feng.material.statusbar.StatusBarCompat;


public class MainActivity extends ActionBarActivity implements
        INavigationDrawerCallbacks,
        IRequestCallbacks,
        IINavigationDrawerFragment.DrawerStatusListener,
        INavigationDrawerOtherCallbacks,
        DeletedItemFragment.OnCreatedViewListener,
        ToDoFragment.OnCreatedTodoViewListener
{


    private IINavigationDrawerFragment mNavigationDrawerFragment;
    private ToDoFragment mToDoFragment;
    private DeletedItemFragment mDeletedItemFragment;

    private Toolbar mToolbar;
    private MainActivity mInstance;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        StatusBarCompat.setUpActivity(this);

        setContentView(R.layout.activity_main);

        mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(mToolbar);

        mNavigationDrawerFragment = (IINavigationDrawerFragment) getFragmentManager().findFragmentById(R.id.fragment_drawer);
        mNavigationDrawerFragment.setup(R.id.fragment_drawer, (DrawerLayout) findViewById(R.id.drawer), mToolbar);

        mInstance = this;

        String access_token = ConfigHelper.getString(this, "access_token");
        boolean offline = ConfigHelper.getBoolean(this, "offline_mode");
        ConfigHelper.ISOFFLINEMODE = offline;
        if (!offline && access_token == null)
        {
            ConfigHelper.ISOFFLINEMODE = false;
            Intent intent = new Intent(this, StartActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                    Intent.FLAG_ACTIVITY_CLEAR_TASK |
                    Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else if (access_token != null)
        {
            InitialFragment(savedInstanceState, true);
        } else
        {
            ConfigHelper.ISOFFLINEMODE = true;
            mNavigationDrawerFragment.SetupOfflineMode();
            InitialFragment(savedInstanceState, false);
        }
    }

    private void InitialFragment(Bundle savedInstanceState, boolean isLogined)
    {
        if (findViewById(R.id.fragment_container) != null)
        {
            // However, if we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
            if (savedInstanceState != null)
            {
                return;
            }

            mToDoFragment = new ToDoFragment();

            getFragmentManager().beginTransaction().replace(R.id.fragment_container, mToDoFragment).commit();

            if (isLogined)
            {
                mToDoFragment.ShowRefreshing();
                PostHelper.GetOrderedSchedules(this, ConfigHelper.getString(this, "sid"), ConfigHelper.getString(this, "access_token"));
            }
        }

    }

    @Override
    public void onResume()
    {
        super.onResume();
        mToDoFragment.GetAllSchedules();
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
                } catch (Exception E)
                {
                    E.printStackTrace();
                }

            } ; break;
            case 1:
            {
                if (mDeletedItemFragment == null)
                {
                    mDeletedItemFragment = new DeletedItemFragment();
                }

                getFragmentManager().beginTransaction().replace(R.id.fragment_container, mDeletedItemFragment).commit();
                getSupportActionBar().setTitle(getResources().getString(R.string.deleteditems));

            } ; break;
        }
    }


    @Override
    public void OnSelectedOther(int position)
    {
        switch (position)
        {
            case 0:
            {
                Intent intent = new Intent(getApplicationContext(), SettingActivity.class);
                startActivity(intent);
            } ; break;
            case 1:
            {
                Intent intent = new Intent(getApplicationContext(), AboutActivity.class);
                startActivity(intent);
            } ; break;
            case 2:
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
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
                        ConfigHelper.DeleteKey(getApplicationContext(), "access_token");
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

            } ; break;
        }
    }

    @Override
    public void onBackPressed()
    {
        if (mNavigationDrawerFragment.isDrawerOpen())
        {
            mNavigationDrawerFragment.closeDrawer();
        } else if (mNavigationDrawerFragment.getCurrentSelectedPosition() == 1)
        {
            mNavigationDrawerFragment.openDrawer();
        } else
        {
            super.onBackPressed();
        }

    }


    @Override
    public void OnGotScheduleResponse(ArrayList<ToDo> mytodosList)
    {
        if (mytodosList != null)
        {
            ToDoListHelper.TodosList = mytodosList;
            mToDoFragment.SetUpData(mytodosList);
            mToDoFragment.StopRefreshing();

            SerializerHelper.SerializeToFile(ContextUtil.getInstance(), ToDoListHelper.TodosList, SerializerHelper.todosFileName);
        }
    }

    @Override
    public void OnCheckResponse(boolean check)
    {

    }

    @Override
    public void OnGetSaltResponse(String str)
    {

    }

    @Override
    public void OnLoginResponse(boolean value)
    {
        if (value)
        {
            PostHelper.GetOrderedSchedules(this, ConfigHelper.getString(this, "sid"), ConfigHelper.getString(this, "access_token"));
        } else
        {
            AppHelper.ShowShortToast("Fail to login.");
        }
    }

    @Override
    public void OnAddedResponse(boolean isSuccess, ToDo newTodo)
    {
        if (isSuccess)
        {
            ToDoListAdapter adapter = (ToDoListAdapter) mToDoFragment.mToDoRecyclerView.getAdapter();
            adapter.addToDos(newTodo);
            AppHelper.ShowShortToast(getResources().getString(R.string.add_success));

            PostHelper.SetListOrder(this, ConfigHelper.getString(this, "sid"), ToDo.getOrderString(adapter.getListSrc()));
        } else
        {
            AppHelper.ShowShortToast("Fail to add memo :-(");
        }
    }

    @Override
    public void OnSetOrderResponse(boolean isSuccess)
    {
        if (isSuccess)
        {

        }

    }

    @Override
    public void OnRegisteredResponse(boolean isSuccess, String salt)
    {

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
        ToDoListAdapter adapter = (ToDoListAdapter) mToDoFragment.mToDoRecyclerView.getAdapter();
        if (adapter != null)
            adapter.SetCanOperate(isOpen);
    }

    @Override
    public void OnCreatedDeleted(boolean b)
    {
        mDeletedItemFragment.SetUpData(ToDoListHelper.DeletedList);
        if (ToDoListHelper.DeletedList.size() == 0)
        {
            mDeletedItemFragment.ShowNoItemHint();
        } else
            mDeletedItemFragment.HideNoItemHint();
    }

    @Override
    public void OnCreatedToDo(boolean b)
    {
        ArrayList<ToDo> list = SerializerHelper.DeSerializeFromFile(this, SerializerHelper.todosFileName);
        if (list != null)
        {
            ToDoListHelper.TodosList = list;
        }

        if (ConfigHelper.ISLOADLISTONCE)
        {
            mToDoFragment.SetUpData(ToDoListHelper.TodosList);
        } else
        {
            ConfigHelper.ISLOADLISTONCE = true;
            if (!ConfigHelper.ISOFFLINEMODE)
            {
                mToDoFragment.ShowRefreshing();
                PostHelper.GetOrderedSchedules(this, ConfigHelper.getString(this, "sid"), ConfigHelper.getString(this, "access_token"));
            } else
            {
                mToDoFragment.SetUpData(ToDoListHelper.TodosList);
            }
        }
    }

    @Override
    public void OnUpdateContent(boolean isSuccess)
    {
    }
}
