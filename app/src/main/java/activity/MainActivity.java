package activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import interfaces.IDrawerStatusChanged;
import interfaces.INavigationDrawerSubCallbacks;
import fragment.DeletedItemFragment;
import fragment.NavigationDrawerFragment;

import com.example.juniper.myerlistandroid.R;
import com.umeng.analytics.MobclickAgent;

import fragment.ToDoFragment;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import helper.AppHelper;
import helper.ConfigHelper;
import helper.ContextUtil;
import helper.PostHelper;
import interfaces.INavigationDrawerMainCallbacks;
import adapter.ToDoListAdapter;
import helper.SerializerHelper;
import interfaces.IOnAddedToDo;
import interfaces.IOnReAddedToDo;
import interfaces.IRequestCallbacks;
import model.ToDo;
import model.ToDoListHelper;
import moe.feng.material.statusbar.StatusBarCompat;


public class MainActivity extends AppCompatActivity implements
        INavigationDrawerMainCallbacks,
        IRequestCallbacks,
        INavigationDrawerSubCallbacks,
        IDrawerStatusChanged,
        IOnReAddedToDo,
        IOnAddedToDo
{
    private NavigationDrawerFragment mNavigationDrawerFragment;
    private ToDoFragment mToDoFragment;
    private DeletedItemFragment mDeletedItemFragment;
    private Toolbar mToolbar;

    private AlertDialog mDialog;

    private boolean isAddingPaneShown = false;
    private LinearLayout mAddingPaneLayout;
    private EditText mEditedText;
    private Button mOKBtn;
    private Button mCancelBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        StatusBarCompat.setUpActivity(this);

        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT)
        {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        setContentView(R.layout.activity_main);

        mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(mToolbar);

        mAddingPaneLayout = (LinearLayout) findViewById(R.id.fragment_todo_adding_pane);
        mEditedText=(EditText)findViewById(R.id.add_editText);
        mOKBtn=(Button)findViewById(R.id.add_ok_btn);
        mCancelBtn=(Button)findViewById(R.id.add_cancel_btn);

        mOKBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                OKClick(v);
            }
        });
        mCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                CancelClick(v);
            }
        });

        ImageView mMaskView = (ImageView) findViewById(R.id.activity_main_mask);
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT)
        {
            mMaskView.setVisibility(View.GONE);
            mToolbar.setPadding(0, 0, 0, 0);
        }

        mNavigationDrawerFragment = (NavigationDrawerFragment) getFragmentManager().findFragmentById(R.id.fragment_drawer);
        mNavigationDrawerFragment.setup(R.id.fragment_drawer, (DrawerLayout) findViewById(R.id.drawer), mToolbar);

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
    public void OnDrawerMainItemSelected(int position)
    {
        try
        {
            switch (position)
            {
                case 0:
                {
                    if (mToDoFragment == null)
                    {
                        mToDoFragment = new ToDoFragment();
                    } else
                    {
                        mToDoFragment.UpdateData(ToDoListHelper.TodosList);
                    }
                    ToDoListAdapter adapter = (ToDoListAdapter) mToDoFragment.mToDoRecyclerView.getAdapter();
                    if (adapter != null)
                        adapter.SetCanChangeCate(true);
                    getFragmentManager().beginTransaction().replace(R.id.fragment_container, mToDoFragment).commit();

                    mToolbar.setBackgroundColor(getResources().getColor(R.color.MyerListBlue));
                    mToolbar.setTitle(getResources().getString(R.string.cate_default));
                } break;
                case 1:
                {
                    FilterListByCate(1);
                    mToolbar.setBackgroundColor(getResources().getColor(R.color.WorkColor));
                    mToolbar.setTitle(getResources().getString(R.string.cate_work));
                }
                break;
                case 2:
                {
                    FilterListByCate(2);
                    mToolbar.setBackgroundColor(getResources().getColor(R.color.LifeColor));
                    mToolbar.setTitle(getResources().getString(R.string.cate_life));
                }
                break;
                case 3:
                {
                    FilterListByCate(3);
                    mToolbar.setBackgroundColor(getResources().getColor(R.color.FamilyColor));
                    mToolbar.setTitle(getResources().getString(R.string.cate_family));
                }
                break;
                case 4:
                {
                    FilterListByCate(4);
                    mToolbar.setBackgroundColor(getResources().getColor(R.color.EnterColor));
                    mToolbar.setTitle(getResources().getString(R.string.cate_enter));
                }
                break;
            }

        } catch (Exception e)
        {

        }
    }

    public void FilterListByCate(int cate)
    {
        ArrayList<ToDo> newList = new ArrayList<>();
        for (ToDo todo : ToDoListHelper.TodosList)
        {
            if (todo.getCate() == cate)
            {
                newList.add(todo);
            }
        }
        if (mToDoFragment != null)
        {
            mToDoFragment.UpdateData(newList);
            ToDoListAdapter adapter = (ToDoListAdapter) mToDoFragment.mToDoRecyclerView.getAdapter();
            if (adapter != null)
                adapter.SetCanChangeCate(false);
        }
    }

    public void SwitchToDeleteFragment()
    {
        if (mDeletedItemFragment == null)
        {
            mDeletedItemFragment = new DeletedItemFragment();
        }

        getFragmentManager().beginTransaction().replace(R.id.fragment_container, mDeletedItemFragment).commit();
    }

    public void ShowAddingPane()
    {

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP)
        {
            isAddingPaneShown = true;

            // get the center for the clipping circle
            int cx = mAddingPaneLayout.getWidth() - 200;
            int cy = mAddingPaneLayout.getHeight() - 200;

            // get the final radius for the clipping circle
            int finalRadius = Math.max(mAddingPaneLayout.getWidth(), mAddingPaneLayout.getHeight());

            // create the animator for this view (the start radius is zero)
            Animator anim;
            anim = ViewAnimationUtils.createCircularReveal(mAddingPaneLayout, cx, cy, 0, finalRadius);

            // make the view visible and start the animation
            mAddingPaneLayout.setVisibility(View.VISIBLE);
            anim.start();

            if (ConfigHelper.getBoolean(ContextUtil.getInstance(), "ShowKeyboard"))
            {
                mEditedText.requestFocus();
                Timer timer = new Timer();
                timer.schedule(new TimerTask()
                {
                    @Override
                    public void run()
                    {
                        InputMethodManager inputMethodManager = (InputMethodManager) mEditedText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputMethodManager.showSoftInput(mEditedText, 0);
                    }
                }, 333);
            }
        } else
        {
            View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_adding_pane, (ViewGroup) this.findViewById(R.id.dialog_title));

            TextView titleText = (TextView) dialogView.findViewById(R.id.dialog_title_text);
            titleText.setText(getResources().getString(R.string.new_memo_title));

            mEditedText = (EditText) dialogView.findViewById(R.id.newMemoEdit);
            mEditedText.setHint(R.string.new_memo_hint);

            Button okBtn = (Button) dialogView.findViewById(R.id.add_ok_btn);
            okBtn.setText(R.string.ok_btn);
            okBtn.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                   OKClick(view);
                }
            });

            Button cancelBtn = (Button) dialogView.findViewById(R.id.add_cancel_btn);
            cancelBtn.setText(R.string.cancel_btn);
            cancelBtn.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                   CancelClick(view);
                }
            });

            if (!ConfigHelper.getBoolean(ContextUtil.getInstance(), "HandHobbit"))
            {
                LinearLayout linearLayout = (LinearLayout) dialogView.findViewById(R.id.dialog_btn_layout);
                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
                layoutParams.setMargins(20, 0, 0, 0);
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                linearLayout.setLayoutParams(layoutParams);
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            mDialog = builder.setView((dialogView)).show();

            if (ConfigHelper.getBoolean(ContextUtil.getInstance(), "ShowKeyboard"))
            {
                mEditedText.requestFocus();
                Timer timer = new Timer();
                timer.schedule(new TimerTask()
                {
                    @Override
                    public void run()
                    {
                        InputMethodManager inputMethodManager = (InputMethodManager) mEditedText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputMethodManager.showSoftInput(mEditedText, 0);
                    }
                }, 333);
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void HideAddingPane()
    {
        isAddingPaneShown = false;

        // get the center for the clipping circle
        int cx = mAddingPaneLayout.getWidth() - 200;
        int cy = mAddingPaneLayout.getHeight() - 200;

        // get the initial radius for the clipping circle
        int initialRadius = Math.max(mAddingPaneLayout.getWidth(), mAddingPaneLayout.getHeight());

        // create the animation (the final radius is zero)
        Animator anim =
                ViewAnimationUtils.createCircularReveal(mAddingPaneLayout, cx, cy, initialRadius, 0);

        // make the view invisible when the animation is done
        anim.addListener(new AnimatorListenerAdapter()
        {
            @Override
            public void onAnimationEnd(Animator animation)
            {
                super.onAnimationEnd(animation);
                mAddingPaneLayout.setVisibility(View.INVISIBLE);
            }
        });

        // start the animation
        anim.start();
    }

    public void OKClick(View v)
    {
        if(mDialog!=null) mDialog.dismiss();
        if(isAddingPaneShown) HideAddingPane();
        if (ConfigHelper.ISOFFLINEMODE)
        {
            ToDo newToAdd = new ToDo();
            newToAdd.setContent(mEditedText.getText().toString());
            newToAdd.setIsDone(false);
            newToAdd.setID(java.util.UUID.randomUUID().toString());
            OnAddedResponse(true, newToAdd);
        } else
        {
            PostHelper.AddToDo(MainActivity.this, ConfigHelper.getString(ContextUtil.getInstance(), "sid"), mEditedText.getText().toString(), "0", 0);
        }

        CancelClick(null);
    }

    public void CancelClick(View  v)
    {
        if(mDialog!=null) mDialog.dismiss();
        if(isAddingPaneShown) HideAddingPane();

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(0, InputMethodManager.HIDE_IMPLICIT_ONLY);

        mEditedText.setText("");
    }

    @Override
    public void OnDrawerSubItemSelected(int position)
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
        }
    }

    @Override
    public void onBackPressed()
    {
        if (mNavigationDrawerFragment.isDrawerOpen())
        {
            mNavigationDrawerFragment.closeDrawer();
        }
        else if (mNavigationDrawerFragment.getCurrentSelectedPosition() == 1)
        {
            mNavigationDrawerFragment.openDrawer();
        }
        else if(isAddingPaneShown)
        {
            HideAddingPane();
        }
        else
        {
            super.onBackPressed();
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
    public void OnGotScheduleResponse(ArrayList<ToDo> list)
    {
        if (list != null)
        {
            ToDoListHelper.TodosList = list;
            mToDoFragment.UpdateData(list);
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
            adapter.AddToDos(newTodo);
            AppHelper.ShowShortToast(getResources().getString(R.string.add_success));

            PostHelper.SetListOrder(this, ConfigHelper.getString(this, "sid"), ToDo.getOrderString(adapter.GetListSrc()));
        } else
        {
            AppHelper.ShowShortToast("Fail to add memo :-(");
        }
    }

    @Override
    public void OnSetOrderResponse(boolean isSuccess)
    {
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
            adapter.SetEnable(isOpen);
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
            mToDoFragment.UpdateData(ToDoListHelper.TodosList);
        } else
        {
            ConfigHelper.ISLOADLISTONCE = true;
            if (!ConfigHelper.ISOFFLINEMODE)
            {
                mToDoFragment.ShowRefreshing();
                PostHelper.GetOrderedSchedules(this, ConfigHelper.getString(this, "sid"), ConfigHelper.getString(this, "access_token"));
            } else
            {
                mToDoFragment.UpdateData(ToDoListHelper.TodosList);
            }
        }
    }

    @Override
    public void OnReCreatedToDo(boolean b)
    {
        mDeletedItemFragment.SetUpData(ToDoListHelper.DeletedList);
        if (ToDoListHelper.DeletedList.size() == 0)
        {
            mDeletedItemFragment.ShowNoItemHint();
        } else
            mDeletedItemFragment.HideNoItemHint();
    }

    @Override
    public void OnUpdateContent(boolean isSuccess)
    {
    }
}
