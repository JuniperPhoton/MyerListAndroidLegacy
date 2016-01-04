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
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import helper.FindRadioBtnHelper;
import interfaces.IDrawerStatusChanged;
import interfaces.INavigationDrawerSubCallbacks;
import fragment.DeletedItemFragment;
import fragment.NavigationDrawerFragment;

import com.example.juniper.myerlistandroid.R;
import com.pgyersdk.crash.PgyCrashManager;
import com.umeng.analytics.MobclickAgent;
import com.umeng.update.UmengUpdateAgent;

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
        IOnReAddedToDo
{
    private NavigationDrawerFragment mNavigationDrawerFragment;
    private ToDoFragment mToDoFragment;
    private DeletedItemFragment mDeletedItemFragment;
    private Toolbar mToolbar;

    private AlertDialog mDialog;

    private boolean isAddingPaneShown = false;
    private EditText mEditedText;
    private FrameLayout mFragmentLayout;
    private Button mOKBtn;
    private Button mCancelBtn;

    private LinearLayout mAddingPaneLayout;

    private RadioGroup mAddingCateRadioGroup;
    private RadioGroup mAddingCateRadioGroupLegacy;

    private int mCurrentCate = 0;
    private int mCateAboutToAdd = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        StatusBarCompat.setUpActivity(this);

        UmengUpdateAgent.update(this);

        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT)
        {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        setContentView(R.layout.activity_main);

        PgyCrashManager.register(this);

        InitialViews();

        String access_token = ConfigHelper.getString(this, "access_token");
        boolean offline = ConfigHelper.getBoolean(this, "offline_mode");

        ConfigHelper.ISOFFLINEMODE = offline;

        //还没有登录/进入离线模式，回到 StartActivity
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

    //找到需要初始化的控件
    private void InitialViews()
    {
        mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(mToolbar);

        mFragmentLayout = (FrameLayout) findViewById(R.id.fragment_container);

        mAddingCateRadioGroup = (RadioGroup) findViewById(R.id.add_pane_radio);
        mAddingCateRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i)
            {
                int index = FindRadioBtnHelper.GetCateByRadioBtnID(i);
                mCateAboutToAdd = index;
                UpdateAddingPaneColor(index);
            }
        });

        mAddingPaneLayout = (LinearLayout) findViewById(R.id.fragment_todo_adding_pane);
        mAddingPaneLayout.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent)
            {
                return true;
            }
        });

        mEditedText = (EditText) findViewById(R.id.add_editText);
        mOKBtn = (Button) findViewById(R.id.add_ok_btn);
        mCancelBtn = (Button) findViewById(R.id.add_cancel_btn);

        mOKBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                OKClick(v);
            }
        });
        mCancelBtn.setOnClickListener(new View.OnClickListener()
        {
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
    }

    private void UpdateAddingPaneColor(int i)
    {
        if (mAddingPaneLayout == null) return;
        switch (i)
        {
            case 0:
            {
                mAddingPaneLayout.setBackgroundColor(getResources().getColor(R.color.MyerListBlue));
            }
            break;
            case 1:
            {
                mAddingPaneLayout.setBackgroundColor(getResources().getColor(R.color.WorkColor));
            }
            break;
            case 2:
            {
                mAddingPaneLayout.setBackgroundColor(getResources().getColor(R.color.LifeColor));
            }
            break;
            case 3:
            {
                mAddingPaneLayout.setBackgroundColor(getResources().getColor(R.color.FamilyColor));
            }
            break;
            case 4:
            {
                mAddingPaneLayout.setBackgroundColor(getResources().getColor(R.color.EnterColor));
            }
            break;
        }
    }



    //初始化 Fragment
    private void InitialFragment(Bundle savedInstanceState, boolean isLogined)
    {
        if (findViewById(R.id.fragment_container) != null)
        {
            if (savedInstanceState != null)
            {
                return;
            }

            mToDoFragment = new ToDoFragment();

            getFragmentManager().beginTransaction().replace(R.id.fragment_container, mToDoFragment).commitAllowingStateLoss();

            //登录了的，马上同步
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
        mCurrentCate = position;
        mCateAboutToAdd = position;
        RadioButton radioButton = (RadioButton) findViewById(FindRadioBtnHelper.GetRadioBtnIDByCate(mCateAboutToAdd));
        if (radioButton != null)
        {
            mAddingCateRadioGroup.check(radioButton.getId());
        }
        UpdateAddingPaneColor(position);
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
                    getFragmentManager().beginTransaction().replace(R.id.fragment_container, mToDoFragment).commitAllowingStateLoss();

                    mToolbar.setBackgroundColor(getResources().getColor(R.color.MyerListBlue));
                    mToolbar.setTitle(getResources().getString(R.string.cate_default));
                    mAddingPaneLayout.setBackgroundColor(getResources().getColor(R.color.MyerListBlue));
                }
                break;
                case 1:
                {
                    UpdateListByCate();
                    mToolbar.setBackgroundColor(getResources().getColor(R.color.WorkColor));
                    mToolbar.setTitle(getResources().getString(R.string.cate_work));
                    mAddingPaneLayout.setBackgroundColor(getResources().getColor(R.color.WorkColor));
                }
                break;
                case 2:
                {
                    UpdateListByCate();
                    mToolbar.setBackgroundColor(getResources().getColor(R.color.LifeColor));
                    mToolbar.setTitle(getResources().getString(R.string.cate_life));
                    mAddingPaneLayout.setBackgroundColor(getResources().getColor(R.color.LifeColor));
                }
                break;
                case 3:
                {
                    UpdateListByCate();
                    mToolbar.setBackgroundColor(getResources().getColor(R.color.FamilyColor));
                    mToolbar.setTitle(getResources().getString(R.string.cate_family));
                    mAddingPaneLayout.setBackgroundColor(getResources().getColor(R.color.FamilyColor));
                }
                break;
                case 4:
                {
                    UpdateListByCate();
                    mToolbar.setBackgroundColor(getResources().getColor(R.color.EnterColor));
                    mToolbar.setTitle(getResources().getString(R.string.cate_enter));
                    mAddingPaneLayout.setBackgroundColor(getResources().getColor(R.color.EnterColor));
                }
                break;
                case 5:
                {
                    SwitchToDeleteFragment();
                    mToolbar.setBackgroundColor(getResources().getColor(R.color.DeletedColor));
                    mToolbar.setTitle(getResources().getString(R.string.deleteditems));
                }
                break;
            }

        } catch (Exception e)
        {

        }
    }

    public void UpdateListByCate()
    {
        if (mCurrentCate == 5) return;

        ArrayList<ToDo> newList = new ArrayList<>();
        if (mCurrentCate == 0) newList = ToDoListHelper.TodosList;
        else
        {
            for (ToDo todo : ToDoListHelper.TodosList)
            {
                if (todo.getCate() == mCurrentCate)
                {
                    newList.add(todo);
                }
            }
        }

        if (mToDoFragment != null)
        {
            getFragmentManager().beginTransaction().replace(R.id.fragment_container, mToDoFragment).commitAllowingStateLoss();

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

        getFragmentManager().beginTransaction().replace(R.id.fragment_container, mDeletedItemFragment).commitAllowingStateLoss();
    }

    public void ShowAddingPane()
    {
        //Android 5.0 以上
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP)
        {
            isAddingPaneShown = true;

            // get the center for the clipping circle
            int cx = mAddingPaneLayout.getWidth() - 160;
            int cy = mAddingPaneLayout.getHeight() - 160;

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
        }
        //Android 5.0 以下
        else
        {
            View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_adding_pane, (ViewGroup) this.findViewById(R.id.dialog_title));

            TextView titleText = (TextView) dialogView.findViewById(R.id.dialog_title_text);
            titleText.setText(getResources().getString(R.string.new_memo_title));

            mEditedText = (EditText) dialogView.findViewById(R.id.newMemoEdit);
            mEditedText.setHint(R.string.new_memo_hint);

            mAddingCateRadioGroupLegacy = (RadioGroup) dialogView.findViewById(R.id.add_pane_radio_legacy);
            mAddingCateRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
            {
                @Override
                public void onCheckedChanged(RadioGroup radioGroup, int i)
                {
                    int index = FindRadioBtnHelper.GetCateByRadioBtnID(i);
                    mCateAboutToAdd = index;
                }
            });

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
        int cx = mAddingPaneLayout.getWidth() - 160;
        int cy = mAddingPaneLayout.getHeight() - 160;

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

    //添加面板点击确认
    public void OKClick(View v)
    {
        if (mDialog != null)
            mDialog.dismiss();
        if (isAddingPaneShown)
            HideAddingPane();
        if (ConfigHelper.ISOFFLINEMODE)
        {
            ToDo newToAdd = new ToDo();
            newToAdd.setContent(mEditedText.getText().toString());
            newToAdd.setIsDone(false);
            newToAdd.setID(java.util.UUID.randomUUID().toString());
            newToAdd.setCate(mCateAboutToAdd);
            OnAddedResponse(true, newToAdd);
        } else
        {
            PostHelper.AddToDo(MainActivity.this, ConfigHelper.getString(ContextUtil.getInstance(), "sid"), mEditedText.getText().toString(), "0", mCateAboutToAdd);
        }

        CancelClick(null);
    }

    //添加面板点击取消
    public void CancelClick(View v)
    {
        if (mDialog != null)
            mDialog.dismiss();
        if (isAddingPaneShown)
            HideAddingPane();

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);

        mEditedText.setText("");
        mCateAboutToAdd = mCurrentCate;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        //No call for super(). Bug on API Level > 11.
        //outState.putString("WORKAROUND_FOR_BUG_19917_KEY", "WORKAROUND_FOR_BUG_19917_VALUE");
        super.onSaveInstanceState(outState);
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
            }
            ;
            break;
            case 1:
            {
                Intent intent = new Intent(getApplicationContext(), AboutActivity.class);
                startActivity(intent);
            }
            ;
            break;
        }
    }

    @Override
    public void onBackPressed()
    {
        if (mNavigationDrawerFragment.isDrawerOpen())
        {
            mNavigationDrawerFragment.closeDrawer();
        } else if (isAddingPaneShown)
        {
            HideAddingPane();
        } else
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

            AppHelper.ShowShortToast(getResources().getString(R.string.Synced));

            SerializerHelper.SerializeToFile(ContextUtil.getInstance(), ToDoListHelper.TodosList, SerializerHelper.todosFileName);

            UpdateListByCate();
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
            adapter.AddToDo(newTodo);
            AppHelper.ShowShortToast(getResources().getString(R.string.add_success));
            PostHelper.SetListOrder(this, ConfigHelper.getString(this, "sid"), ToDo.getOrderString(adapter.GetListSrc()));
            UpdateListByCate();
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
    public void OnReCreatedToDo(boolean b)
    {
        mDeletedItemFragment.SetUpData(ToDoListHelper.DeletedList);
        if (ToDoListHelper.DeletedList.size() == 0)
            mDeletedItemFragment.ShowNoItemHint();
        else
            mDeletedItemFragment.HideNoItemHint();
    }

    @Override
    public void OnUpdateContent(boolean isSuccess)
    {
    }

    public void OnInitial(boolean b)
    {
        ArrayList<ToDo> list = SerializerHelper.DeSerializeFromFile(this, SerializerHelper.todosFileName);
        if (list != null)
        {
            ToDoListHelper.TodosList = list;
        }
        //已经登陆了
        if (!ConfigHelper.ISOFFLINEMODE)
        {
            mToDoFragment.UpdateData(ToDoListHelper.TodosList);
            PostHelper.GetOrderedSchedules(this, ConfigHelper.getString(this, "sid"), ConfigHelper.getString(this, "access_token"));
        }
        //离线模式
        else
        {
            mToDoFragment.UpdateData(ToDoListHelper.TodosList);
        }
    }
}
