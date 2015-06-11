package activity;

import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.juniper.myerlistandroid.NavigationDrawerCallbacks;

import fragment.NavigationDrawerFragment;
import com.example.juniper.myerlistandroid.R;
import com.example.juniper.myerlistandroid.ToDoListAdapter;

import org.w3c.dom.Text;

import fragment.ToDoFragment;

import java.security.NoSuchAlgorithmException;
import java.sql.BatchUpdateException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import helper.AppHelper;
import helper.ConfigHelper;
import helper.ContextUtil;
import helper.PostHelper;
import helper.PostHelper.OnGetSchedulesListener;
import model.Schedule;


public class MainActivity extends ActionBarActivity implements
        NavigationDrawerCallbacks,
        OnGetSchedulesListener,
        PostHelper.OnLoginResponseListener,
        PostHelper.OnAddedMemoListener,
        PostHelper.OnSetOrderListener
{

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;
    private ToDoFragment mToDoFragment;
    private Toolbar mToolbar;
    private EditText mNewMemoText;
    private MainActivity mInstance;
    private AlertDialog mDialog;

    private int mCurrentPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(mToolbar);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.fragment_drawer);

        // Set up the drawer.
        mNavigationDrawerFragment.setup(R.id.fragment_drawer, (DrawerLayout) findViewById(R.id.drawer), mToolbar);

        mInstance=this;

        try
        {
            InitialFragment(savedInstanceState);
        }
        catch (Exception e)
        {

        }

    }

    private void InitialFragment(Bundle savedInstanceState) throws NoSuchAlgorithmException
    {
        // Check that the activity is using the layout version with
        // the fragment_container FrameLayout
        if (findViewById(R.id.fragment_container) != null)
        {

            // However, if we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
            if (savedInstanceState != null) {
                return;
            }

            // Create a new Fragment to be placed in the activity layout
            ToDoFragment firstFragment = new ToDoFragment();
            mToDoFragment=firstFragment;
            // In case this activity was started with special instructions from an
            // Intent, pass the Intent's extras to the fragment as arguments
            //firstFragment.setArguments(getIntent().getExtras());

            // Add the fragment to the 'fragment_container' FrameLayout
            getFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container,firstFragment).commit();

            Intent intent=getIntent();
            if(intent.getStringExtra("LOGIN_STATE").equals("AboutToLogin"))
            {
                mToDoFragment.ShowRefreshing();

                PostHelper.Login(this, ConfigHelper.getString(this, "email"), ConfigHelper.getString(this, "password"), ConfigHelper.getString(this, "salt"));

            }
        }

    }


    @Override
    public void onNavigationDrawerItemSelected(int position)
    {
        mCurrentPosition=position;
        // update the main content by replacing fragments
        //Toast.makeText(this, "Menu item selected -> " + position, Toast.LENGTH_SHORT).show();
        switch (position)
        {
            case 0:
            {
                   try
                   {
                       ToDoFragment firstFragment = new ToDoFragment();
                       mToDoFragment=firstFragment;

                       getFragmentManager().beginTransaction()
                               .replace(R.id.fragment_container,firstFragment).commit();
                       mToDoFragment.ShowRefreshing();
                       PostHelper.Login(this, ConfigHelper.getString(this, "email"), ConfigHelper.getString(this, "password"), ConfigHelper.getString(this, "salt"));

                       getFragmentManager().beginTransaction().replace(R.id.fragment_container,mToDoFragment).
                               addToBackStack("todo_Fragment").
                               setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).
                               commit();
                       getSupportActionBar().setTitle("MyerList");
                   }
                   catch (Exception E)
                   {
                        E.printStackTrace();
                   }

            };break;
            case 1:
            {
                getSupportActionBar().setTitle("Deleted items");
            };break;
            case 2:
            {
                Intent intent=new Intent(getApplicationContext(),SettingActivity.class);
                startActivity(intent);
            };break;
            case 3:
            {
                Intent intent=new Intent(getApplicationContext(),AboutActivity.class);
                startActivity(intent);
            };break;
            case 4:
            {
                AlertDialog.Builder builder=new AlertDialog.Builder(this);
                builder.setTitle(R.string.logout_title);
                builder.setMessage(R.string.logout_content);
                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i)
                    {
                        ConfigHelper.putBoolean(getApplicationContext(), "offline_mode", false);
                        ConfigHelper.DeleteKey(getApplicationContext(),"email");
                        Intent intent = new Intent(getApplicationContext(), StartActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
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

    public void toAddClick(View view)
    {
        View dialogView=(View)LayoutInflater.from(this).inflate(R.layout.add_todo_dialog, (ViewGroup) findViewById(R.id.dialog_title));

        TextView titleText=(TextView)dialogView.findViewById(R.id.dialog_title_text);
        titleText.setText("ADD A MEMO");

        this.mNewMemoText=(EditText)dialogView.findViewById(R.id.newMemoEdit);

        Button okBtn=(Button)dialogView.findViewById(R.id.add_ok_btn);
        okBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                PostHelper.AddMemo(mInstance, ConfigHelper.getString(getApplicationContext(), "sid"), mNewMemoText.getText().toString(), "0");
                mDialog.dismiss();
            }
        });

        Button cancelBtn=(Button)dialogView.findViewById(R.id.add_cancel_btn);
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

         AlertDialog.Builder builder=new AlertDialog.Builder(this);
         mDialog=builder.setView((dialogView)).show();
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



    @Override
    public void onBackPressed()
    {
        if (mNavigationDrawerFragment.isDrawerOpen())
        {
            mNavigationDrawerFragment.closeDrawer();
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
            mToDoFragment.SetUpData(mytodosList);
            mToDoFragment.StopRefreshing();
        }
    }

    @Override
    public void OnLoginResponse(boolean value)
    {
        if(value)
        {
            PostHelper.GetOrderedSchedules(this, ConfigHelper.getString(this, "sid"), ConfigHelper.getString(this, "access_token"));
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
            AppHelper.ShowShortToast("New memo added ;D");

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
}
