package activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.google.gson.reflect.TypeToken;
import com.juniperphoton.myerlistandroid.R;

import api.CloudServices;
import exception.APIException;
import interfaces.INavigationDrawerCallback;
import interfaces.IRequestCallback;
import model.ToDoCategory;
import util.FindRadioBtnHelper;
import interfaces.IDrawerStatusChanged;
import fragment.DeletedItemFragment;
import fragment.NavigationDrawerFragment;

import com.pgyersdk.crash.PgyCrashManager;
import com.umeng.analytics.MobclickAgent;
import com.umeng.update.UmengUpdateAgent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import fragment.ToDoFragment;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import util.AppUtil;
import util.ConfigHelper;
import util.AppExtension;
import adapter.ToDoListAdapter;
import util.SerializerHelper;
import model.ToDo;
import util.ToDoListGlobalLocator;
import moe.feng.material.statusbar.StatusBarCompat;
import util.ToastService;

public class MainActivity extends AppCompatActivity implements INavigationDrawerCallback,
        IDrawerStatusChanged {
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

    private int mCurrentCateID = 0;
    private int mCateIDAboutToAdd = 0;

    private ToDo mToDoAboutToAdded;
    private boolean misAddingStagedItems = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarCompat.setUpActivity(this);

        UmengUpdateAgent.update(this);

//        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
//            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//        }

        setContentView(R.layout.activity_main);

        PgyCrashManager.register(this);

        initialViews();

        String access_token = ConfigHelper.getString(this, "access_token");
        boolean offline = ConfigHelper.getBoolean(this, "offline_mode");

        if (access_token != null) {
            initialFragment(savedInstanceState, true);
        } else {
            ConfigHelper.ISOFFLINEMODE = true;
            mNavigationDrawerFragment.setupOfflineMode();
            initialFragment(savedInstanceState, false);
        }

    }

    //找到需要初始化的控件
    private void initialViews() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(mToolbar);

        mFragmentLayout = (FrameLayout) findViewById(R.id.fragment_container);

        mAddingCateRadioGroup = (RadioGroup) findViewById(R.id.add_pane_radio);
        mAddingCateRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                int index = FindRadioBtnHelper.getCateByRadioBtnID(i);
                mCateIDAboutToAdd = index;
                updateAddingPaneColor(index);
            }
        });

        mAddingPaneLayout = (LinearLayout) findViewById(R.id.fragment_todo_adding_pane);
        mAddingPaneLayout.setOnTouchListener(new View.OnTouchListener() {
            //防止触控穿透
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true;
            }
        });

        mEditedText = (EditText) findViewById(R.id.add_editText);
        mOKBtn = (Button) findViewById(R.id.add_ok_btn);
        mCancelBtn = (Button) findViewById(R.id.add_cancel_btn);

        mOKBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                okClick(v);
            }
        });
        mCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelClick(v);
            }
        });

        ImageView mMaskView = (ImageView) findViewById(R.id.activity_main_mask);
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            mMaskView.setVisibility(View.GONE);
            mToolbar.setPadding(0, 0, 0, 0);
        }

        mNavigationDrawerFragment = (NavigationDrawerFragment) getFragmentManager()
                .findFragmentById(R.id.fragment_drawer);
        mNavigationDrawerFragment.setup(R.id.fragment_drawer,
                (DrawerLayout) findViewById(R.id.drawer), mToolbar);
    }

    //根据选中的颜色改变抽屉的背景色
    private void updateAddingPaneColor(int cateID) {
        if (mAddingPaneLayout == null) return;
        ToDoCategory category = ToDoListGlobalLocator.GetCategoryByID(cateID);
        if (category.getID() != -2) {
            mAddingPaneLayout.setBackgroundColor(category.getColor());
        }
    }

    //初始化 Fragment
    private void initialFragment(Bundle savedInstanceState, boolean logined) {
        if (findViewById(R.id.fragment_container) != null) {
            if (savedInstanceState != null) {
                return;
            }

            mToDoFragment = new ToDoFragment();

            getFragmentManager().beginTransaction().replace(R.id.fragment_container, mToDoFragment)
                    .commitAllowingStateLoss();

            //登录了的，马上同步
            if (logined) {
                mToDoFragment.showRefreshing();
                syncCateAndList();
            }
            //没有网络
            if (!AppUtil.isNetworkAvailable(getApplicationContext())) {
                ToastService.showShortToast(getResources().getString(R.string.NoNetworkHint));
            }
            //暂存区有待办事项的，同步到云端
            if (!ConfigHelper.ISOFFLINEMODE && AppUtil.isNetworkAvailable(AppExtension.getInstance())) {
                if (ToDoListGlobalLocator.StagedList == null) return;
                misAddingStagedItems = true;
                for (ToDo todo : ToDoListGlobalLocator.StagedList) {
                    CloudServices.addToDo(ConfigHelper.getString(this, "sid"),
                            ConfigHelper.getString(this, "access_token"),
                            todo.getContent(), "0", todo.getCate(),
                            new IRequestCallback() {
                                @Override
                                public void onResponse(JSONObject jsonObject) {
                                    onAddedResponse(jsonObject);
                                }
                            });
                }
                ToDoListGlobalLocator.StagedList.clear();
                SerializerHelper.serializeToFile(AppExtension.getInstance(),
                        ToDoListGlobalLocator.StagedList,
                        SerializerHelper.stagedFileName);
            }
        }
    }

    //抽屉选中一个项的时候
    @Override
    public void onDrawerMainItemSelected(int cateID) {
        mCurrentCateID = cateID;
        mCateIDAboutToAdd = cateID;

        RadioButton radioButton = (RadioButton) findViewById(
                FindRadioBtnHelper.getRadioBtnIDByCate(mCateIDAboutToAdd));

        if (radioButton != null) {
            mAddingCateRadioGroup.check(radioButton.getId());
        }

        updateAddingPaneColor(cateID);
        ToDoCategory category = ToDoListGlobalLocator.GetCategoryByID(cateID);

        try {
            if (cateID == 0) {
                if (mToDoFragment == null) {
                    mToDoFragment = new ToDoFragment();
                }

                mToolbar.setBackgroundColor(getResources().getColor(R.color.MyerListBlue));
                mToolbar.setTitle(getResources().getString(R.string.cate_default));

                mAddingPaneLayout.setBackgroundColor(getResources().getColor(R.color.MyerListBlue));
                mNavigationDrawerFragment.updateRootBackgroundColor(getResources().getColor(R.color.MyerListBlue));
            } else if (cateID == -1) {
                switchToDeleteFragment();
                mToolbar.setBackgroundColor(getResources().getColor(R.color.DeletedColor));
                mNavigationDrawerFragment.updateRootBackgroundColor(getResources().getColor(R.color.DeletedColor));
                mToolbar.setTitle(getResources().getString(R.string.deleteditems));
            } else {
                mToolbar.setBackgroundColor(category.getColor());
                mNavigationDrawerFragment.updateRootBackgroundColor(category.getColor());
                mToolbar.setTitle(category.getName());
            }
            updateListByCategory();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateListByCategory() {
        if (mCurrentCateID == -1 || mCurrentCateID==-2) return;

        ArrayList<ToDo> newList = new ArrayList<>();
        if (mCurrentCateID == 0) newList = ToDoListGlobalLocator.TodosList;
        else {
            for (ToDo todo : ToDoListGlobalLocator.TodosList) {
                if (todo.getCate() == mCurrentCateID) {
                    newList.add(todo);
                }
            }
        }

        int count = 0;
        for (ToDo toDo : newList) {
            if (!toDo.getIsDone()) count++;
        }
        if (mNavigationDrawerFragment != null) {
            mNavigationDrawerFragment.updateUndoneTextView(String.valueOf(count));
        }

        if (mToDoFragment != null) {
            getFragmentManager().beginTransaction().replace(R.id.fragment_container, mToDoFragment)
                    .commitAllowingStateLoss();

            mToDoFragment.updateData(newList);
        }
    }

    public void switchToDeleteFragment() {
        if (mDeletedItemFragment == null) {
            mDeletedItemFragment = new DeletedItemFragment();
        }
        getFragmentManager().beginTransaction().replace(R.id.fragment_container, mDeletedItemFragment).
                commitAllowingStateLoss();
    }

    public void showAddingPane() {
        //Android 5.0 以上
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
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

            if (ConfigHelper.getBoolean(AppExtension.getInstance(), "ShowKeyboard")) {
                mEditedText.requestFocus();
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        InputMethodManager inputMethodManager = (InputMethodManager) mEditedText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputMethodManager.showSoftInput(mEditedText, 0);
                    }
                }, 333);
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void hideAddingPane() {
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
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mAddingPaneLayout.setVisibility(View.INVISIBLE);
            }
        });

        // start the animation
        anim.start();
    }

    //从服务器同步列表，并排序
    public void syncCateAndList() {
        mNavigationDrawerFragment.syncCatesOrDefault();
    }

    public void syncList() {
        CloudServices.getLatestSchedules(ConfigHelper.getString(this, "sid"),
                ConfigHelper.getString(this, "access_token"), new IRequestCallback() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        onGotLatestScheduleResponse(jsonObject);
                    }
                });
    }


    //添加面板点击确认
    public void okClick(View v) {
        if (mDialog != null) {
            mDialog.dismiss();
        }
        if (isAddingPaneShown) {
            hideAddingPane();
        }

        ToDo newToAdd = new ToDo();
        newToAdd.setContent(mEditedText.getText().toString());
        newToAdd.setIsDone(false);
        newToAdd.setID(java.util.UUID.randomUUID().toString());
        newToAdd.setCate(mCateIDAboutToAdd);

        mToDoAboutToAdded = newToAdd;

        //离线模式
        if (ConfigHelper.ISOFFLINEMODE) {
            addNewToDoToList(newToAdd);
        }
        //非离线模式，发送请求
        else {
            CloudServices.addToDo(ConfigHelper.getString(AppExtension.getInstance(), "sid"),
                    ConfigHelper.getString(this, "access_token"),
                    mEditedText.getText().toString(), "0", mCateIDAboutToAdd,
                    new IRequestCallback() {
                        @Override
                        public void onResponse(JSONObject jsonObject) {
                            onAddedResponse(jsonObject);
                        }
                    });
        }

        dismissDialog();
    }

    //添加面板点击取消
    public void cancelClick(View v) {
        dismissDialog();
    }

    private void dismissDialog() {
        if (mDialog != null)
            mDialog.dismiss();
        if (isAddingPaneShown)
            hideAddingPane();

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);

        mEditedText.setText("");
        mCateIDAboutToAdd = mCurrentCateID;
    }

    public void setIsAddStagedItems(boolean value) {
        misAddingStagedItems = value;
    }

    private void onGotLatestScheduleResponse(JSONObject response) {
        mToDoFragment.stopRefreshing();
        try {
            if (response == null) throw new APIException();

            boolean isSuccess = response.getBoolean("isSuccessed");
            if (isSuccess) {
                JSONArray array = response.getJSONArray("ScheduleInfo");

                if (array != null) {
                    final ArrayList<ToDo> list = ToDo.parseJsonObjFromArray(array);
                    CloudServices.getListOrder(
                            ConfigHelper.getString(this, "sid"),
                            ConfigHelper.getString(this, "access_token"),
                            new IRequestCallback() {
                                @Override
                                public void onResponse(JSONObject jsonObject) {
                                    onGotListOrder(jsonObject, list);
                                }
                            });
                }
            }
        } catch (APIException e) {
            e.printStackTrace();
            ToastService.showShortToast(getResources().getString(R.string.hint_request_fail));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void onGotListOrder(JSONObject response, final ArrayList<ToDo> originalList) {
        try {
            if (response == null) throw new APIException();

            boolean isSuccess = response.getBoolean("isSuccessed");
            if (isSuccess) {
                String orderStr = response.getJSONArray(("OrderList")).getJSONObject(0).getString("list_order");
                ArrayList<ToDo> listInOrder = ToDo.setOrderByString(originalList, orderStr);
                ToDoListGlobalLocator.TodosList = listInOrder;
                mToDoFragment.updateData(listInOrder);

                ToastService.showShortToast(getResources().getString(R.string.Synced));

                SerializerHelper.serializeToFile(AppExtension.getInstance(), ToDoListGlobalLocator.TodosList, SerializerHelper.todosFileName);

                updateListByCategory();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (APIException e) {
            e.printStackTrace();
            ToastService.showShortToast(getResources().getString(R.string.hint_request_fail));
        }
    }

    public void onAddedResponse(JSONObject response) {
        try {
            if (response == null) throw new APIException();

            Boolean isSuccess = response.getBoolean("isSuccessed");
            if (isSuccess) {
                ToDo newToDo = ToDo.parseJsonObjToObj(response.getJSONObject("ScheduleInfo"));
                addNewToDoToList(newToDo);
                ToDoListAdapter adapter = (ToDoListAdapter) mToDoFragment.mToDoRecyclerView.getAdapter();
                CloudServices.setListOrder(ConfigHelper.getString(this, "sid"),
                        ConfigHelper.getString(this, "access_token"),
                        ToDo.getOrderString(adapter.getListSrc()),
                        new IRequestCallback() {
                            @Override
                            public void onResponse(JSONObject jsonObject) {
                                onUpdateOrder(jsonObject);
                            }
                        });
            } else {
                if (mToDoAboutToAdded != null) {
                    ToDoListGlobalLocator.StagedList.add(mToDoAboutToAdded);
                    SerializerHelper.serializeToFile(AppExtension.getInstance(), ToDoListGlobalLocator.StagedList, SerializerHelper.stagedFileName);
                }
                ToDoListGlobalLocator.TodosList.add(mToDoAboutToAdded);
                SerializerHelper.serializeToFile(AppExtension.getInstance(), ToDoListGlobalLocator.TodosList, SerializerHelper.todosFileName);

            }
            mToDoAboutToAdded = null;
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (APIException e) {
            e.printStackTrace();
            ToastService.showShortToast(getResources().getString(R.string.hint_request_fail));
        }
    }

    private void addNewToDoToList(ToDo newToDo) {
        ToDoListAdapter adapter = (ToDoListAdapter) mToDoFragment.mToDoRecyclerView.getAdapter();
        adapter.addToDo(newToDo);

        ToastService.showShortToast(getResources().getString(R.string.add_success));

        updateListByCategory();

        if (misAddingStagedItems) {
            syncCateAndList();
        }
    }

    private void onUpdateOrder(JSONObject response) {
        try {
            if (response == null) throw new APIException();

            boolean isSuccess = response.getBoolean("isSuccessed");
            if (isSuccess) {

            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (APIException e) {
            e.printStackTrace();
            ToastService.showShortToast(getResources().getString(R.string.hint_request_fail));
        }
    }

    public void onSetDone(JSONObject response) {
        try {
            if (response == null) throw new APIException();

            boolean isSuccess = response.getBoolean("isSuccessed");
            if (isSuccess) {
            } else {
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (APIException e) {
            e.printStackTrace();
            ToastService.showShortToast(getResources().getString(R.string.hint_request_fail));
        }
    }

    public void onDelete(JSONObject response) {
        try {
            if (response == null) throw new APIException();

            boolean isSuccess = response.getBoolean("isSuccessed");
            if (isSuccess) {
            } else {
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (APIException e) {
            e.printStackTrace();
            ToastService.showShortToast(getResources().getString(R.string.hint_request_fail));
        }
    }

    public void onUpdateContent(JSONObject response) {
        try {
            if (response == null) throw new APIException();

            boolean isSuccess = response.getBoolean("isSuccessed");
            if (isSuccess) {
            } else {
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (APIException e) {
            e.printStackTrace();
            ToastService.showShortToast(getResources().getString(R.string.hint_request_fail));
        }
    }

    public void onReCreatedToDo(JSONObject response) {
        onAddedResponse(response);
        mDeletedItemFragment.setupListData(ToDoListGlobalLocator.DeletedList);
    }

    public void onInitial() {
        try {
            Type type = new TypeToken<ArrayList<ToDo>>() {
            }.getType();
            //先序列化回来
            ArrayList<ToDo> list = SerializerHelper.deSerializeFromFile(
                    type, this, SerializerHelper.todosFileName);

            if (list != null) {
                ToDoListGlobalLocator.TodosList = list;
            }
            //已经登陆了
            if (!ConfigHelper.ISOFFLINEMODE) {
                mToDoFragment.updateData(ToDoListGlobalLocator.TodosList);
                syncCateAndList();
            }
            //离线模式
            else {
                mToDoFragment.updateData(ToDoListGlobalLocator.TodosList);
            }
        } catch (Exception e) {

        }

    }

    @Override
    public void onDrawerStatusChanged(boolean isOpen) {

    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override

    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //No call for super(). Bug on API Level > 11.
        //outState.putString("WORKAROUND_FOR_BUG_19917_KEY", "WORKAROUND_FOR_BUG_19917_VALUE");
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        if (mNavigationDrawerFragment.isDrawerOpen()) {
            mNavigationDrawerFragment.closeDrawer();
        } else if (isAddingPaneShown) {
            hideAddingPane();
        } else {
            super.onBackPressed();
        }
    }
}
