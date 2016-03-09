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

import com.juniperphoton.myerlistandroid.R;

import api.CloudServices;
import exception.APIException;
import interfaces.INavigationDrawerCallback;
import interfaces.IRequestCallback;
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

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import util.AppUtil;
import util.ConfigHelper;
import util.AppExtension;
import adapter.ToDoListAdapter;
import util.SerializerHelper;
import model.ToDo;
import util.ToDoListReference;
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

    private int mCurrentCate = 0;
    private int mCateAboutToAdd = 0;

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

        ConfigHelper.ISOFFLINEMODE = offline;

        //还没有登录/进入离线模式，回到 StartActivity
        if (!offline && access_token == null) {
            ConfigHelper.ISOFFLINEMODE = false;
            Intent intent = new Intent(this, StartActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                    Intent.FLAG_ACTIVITY_CLEAR_TASK |
                    Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
        else if (access_token != null) {
            initialFragment(savedInstanceState, true);
        }
        else {
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
                mCateAboutToAdd = index;
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
                okclick(v);
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
    private void updateAddingPaneColor(int i) {
        if (mAddingPaneLayout == null) return;
        switch (i) {
            case 0: {
                mAddingPaneLayout.setBackgroundColor(getResources().getColor(R.color.MyerListBlue));
            }
            break;
            case 1: {
                mAddingPaneLayout.setBackgroundColor(getResources().getColor(R.color.WorkColor));
            }
            break;
            case 2: {
                mAddingPaneLayout.setBackgroundColor(getResources().getColor(R.color.LifeColor));
            }
            break;
            case 3: {
                mAddingPaneLayout.setBackgroundColor(getResources().getColor(R.color.FamilyColor));
            }
            break;
            case 4: {
                mAddingPaneLayout.setBackgroundColor(getResources().getColor(R.color.EnterColor));
            }
            break;
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
                syncList();
            }
            //没有网络
            if (!AppUtil.isNetworkAvailable(getApplicationContext())) {
                ToastService.showShortToast(getResources().getString(R.string.NoNetworkHint));
            }
            //暂存区有待办事项的，同步到云端
            if (!ConfigHelper.ISOFFLINEMODE && AppUtil.isNetworkAvailable(AppExtension.getInstance())) {
                if (ToDoListReference.StagedList == null) return;
                misAddingStagedItems = true;
                for (ToDo todo : ToDoListReference.StagedList) {
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
                ToDoListReference.StagedList.clear();
                SerializerHelper.serializeToFile(AppExtension.getInstance(),
                        ToDoListReference.StagedList,
                        SerializerHelper.stagedFileName);
            }
        }
    }

    //抽屉选中一个项的时候
    @Override
    public void onDrawerMainItemSelected(int position) {
        mCurrentCate = position;
        mCateAboutToAdd = position;

        RadioButton radioButton = (RadioButton) findViewById(FindRadioBtnHelper.getRadioBtnIDByCate(mCateAboutToAdd));

        if (radioButton != null) {
            mAddingCateRadioGroup.check(radioButton.getId());
        }

        updateAddingPaneColor(position);

        try {
            switch (position) {
                case 0: {
                    if (mToDoFragment == null) {
                        mToDoFragment = new ToDoFragment();
                    }

                    updateListByCate();

                    mToolbar.setBackgroundColor(getResources().getColor(R.color.MyerListBlue));
                    mToolbar.setTitle(getResources().getString(R.string.cate_default));

                    mAddingPaneLayout.setBackgroundColor(getResources().getColor(R.color.MyerListBlue));
                    mNavigationDrawerFragment.updateRootBackgroundColor(getResources().getColor(R.color.MyerListBlue));
                }
                break;
                case 1: {
                    updateListByCate();
                    mToolbar.setBackgroundColor(getResources().getColor(R.color.WorkColor));
                    mToolbar.setTitle(getResources().getString(R.string.cate_work));
                    mAddingPaneLayout.setBackgroundColor(getResources().getColor(R.color.WorkColor));
                    mNavigationDrawerFragment.updateRootBackgroundColor(getResources().getColor(R.color.WorkColor));

                }
                break;
                case 2: {
                    updateListByCate();
                    mToolbar.setBackgroundColor(getResources().getColor(R.color.LifeColor));
                    mToolbar.setTitle(getResources().getString(R.string.cate_life));
                    mAddingPaneLayout.setBackgroundColor(getResources().getColor(R.color.LifeColor));
                    mNavigationDrawerFragment.updateRootBackgroundColor(getResources().getColor(R.color.LifeColor));

                }
                break;
                case 3: {
                    updateListByCate();
                    mToolbar.setBackgroundColor(getResources().getColor(R.color.FamilyColor));
                    mToolbar.setTitle(getResources().getString(R.string.cate_family));
                    mAddingPaneLayout.setBackgroundColor(getResources().getColor(R.color.FamilyColor));
                    mNavigationDrawerFragment.updateRootBackgroundColor(getResources().getColor(R.color.FamilyColor));

                }
                break;
                case 4: {
                    updateListByCate();
                    mToolbar.setBackgroundColor(getResources().getColor(R.color.EnterColor));
                    mToolbar.setTitle(getResources().getString(R.string.cate_enter));
                    mAddingPaneLayout.setBackgroundColor(getResources().getColor(R.color.EnterColor));
                    mNavigationDrawerFragment.updateRootBackgroundColor(getResources().getColor(R.color.EnterColor));

                }
                break;
                case 5: {
                    switchToDeleteFragment();
                    mToolbar.setBackgroundColor(getResources().getColor(R.color.DeletedColor));
                    mNavigationDrawerFragment.updateRootBackgroundColor(getResources().getColor(R.color.DeletedColor));
                    mToolbar.setTitle(getResources().getString(R.string.deleteditems));
                }
                break;
            }

        }
        catch (Exception e) {

        }
    }

    public void updateListByCate() {
        if (mCurrentCate == 5) return;

        ArrayList<ToDo> newList = new ArrayList<>();
        if (mCurrentCate == 0) newList = ToDoListReference.TodosList;
        else {
            for (ToDo todo : ToDoListReference.TodosList) {
                if (todo.getCate() == mCurrentCate) {
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

            mToDoFragment.UpdateData(newList);

            ToDoListAdapter adapter = (ToDoListAdapter) mToDoFragment.mToDoRecyclerView.getAdapter();
            if (adapter != null) {
                if (mCurrentCate != 0)
                    adapter.setCanChangeCate(false);
                else adapter.setCanChangeCate(true);
            }
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
        //Android 5.0 以下
        else {
//            View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_adding_pane, (ViewGroup) this.findViewById(R.id.dialog_title));
//
//            TextView titleText = (TextView) dialogView.findViewById(R.id.dialog_title_text);
//            titleText.setText(getResources().getString(R.string.new_memo_title));
//
//            mEditedText = (EditText) dialogView.findViewById(R.id.newMemoEdit);
//            mEditedText.setHint(R.string.new_memo_hint);
//
//            mAddingCateRadioGroupLegacy = (RadioGroup) dialogView.findViewById(R.id.add_pane_radio_legacy);
//            mAddingCateRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
//                @Override
//                public void onCheckedChanged(RadioGroup radioGroup, int i) {
//                    int index = FindRadioBtnHelper.getCateByRadioBtnID(i);
//                    mCateAboutToAdd = index;
//                }
//            });
//
//            Button okBtn = (Button) dialogView.findViewById(R.id.add_ok_btn);
//            okBtn.setText(R.string.ok_btn);
//            okBtn.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    okclick(view);
//                }
//            });
//
//            Button cancelBtn = (Button) dialogView.findViewById(R.id.add_cancel_btn);
//            cancelBtn.setText(R.string.cancel_btn);
//            cancelBtn.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    cancelClick(view);
//                }
//            });
//
//            if (!ConfigHelper.getBoolean(AppExtension.getInstance(), "HandHobbit")) {
//                LinearLayout linearLayout = (LinearLayout) dialogView.findViewById(R.id.dialog_btn_layout);
//                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
//                layoutParams.setMargins(20, 0, 0, 0);
//                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
//                linearLayout.setLayoutParams(layoutParams);
//            }
//
//            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
//            mDialog = builder.setView((dialogView)).show();
//
//            if (ConfigHelper.getBoolean(AppExtension.getInstance(), "ShowKeyboard")) {
//                mEditedText.requestFocus();
//                Timer timer = new Timer();
//                timer.schedule(new TimerTask() {
//                    @Override
//                    public void run() {
//                        InputMethodManager inputMethodManager = (InputMethodManager) mEditedText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
//                        inputMethodManager.showSoftInput(mEditedText, 0);
//                    }
//                }, 333);
//            }
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
    public void okclick(View v) {
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
        newToAdd.setCate(mCateAboutToAdd);

        mToDoAboutToAdded = newToAdd;

        //离线模式
        if (ConfigHelper.ISOFFLINEMODE) {
            addNewToDoToList(newToAdd);
        }
        //非离线模式，发送请求
        else {
            CloudServices.addToDo(ConfigHelper.getString(AppExtension.getInstance(), "sid"),
                    ConfigHelper.getString(this, "access_token"),
                    mEditedText.getText().toString(), "0", mCateAboutToAdd,
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
        mCateAboutToAdd = mCurrentCate;
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
        }
        catch (APIException e) {
            e.printStackTrace();
            ToastService.showShortToast(getResources().getString(R.string.hint_request_fail));
        }
        catch (Exception e) {
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
                ToDoListReference.TodosList = listInOrder;
                mToDoFragment.UpdateData(listInOrder);

                ToastService.showShortToast(getResources().getString(R.string.Synced));

                SerializerHelper.serializeToFile(AppExtension.getInstance(), ToDoListReference.TodosList, SerializerHelper.todosFileName);

                updateListByCate();
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        catch (APIException e) {
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
            }
            else {
                if (mToDoAboutToAdded != null) {
                    ToDoListReference.StagedList.add(mToDoAboutToAdded);
                    SerializerHelper.serializeToFile(AppExtension.getInstance(), ToDoListReference.StagedList, SerializerHelper.stagedFileName);
                }
                ToDoListReference.TodosList.add(mToDoAboutToAdded);
                SerializerHelper.serializeToFile(AppExtension.getInstance(), ToDoListReference.TodosList, SerializerHelper.todosFileName);

            }
            mToDoAboutToAdded = null;
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        catch (APIException e) {
            e.printStackTrace();
            ToastService.showShortToast(getResources().getString(R.string.hint_request_fail));
        }
    }

    private void addNewToDoToList(ToDo newToDo) {
        ToDoListAdapter adapter = (ToDoListAdapter) mToDoFragment.mToDoRecyclerView.getAdapter();
        adapter.addToDo(newToDo);

        ToastService.showShortToast(getResources().getString(R.string.add_success));

        updateListByCate();

        if (misAddingStagedItems) {
            syncList();
        }
    }

    private void onUpdateOrder(JSONObject response) {
        try {
            if (response == null) throw new APIException();

            boolean isSuccess = response.getBoolean("isSuccessed");
            if (isSuccess) {

            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        catch (APIException e) {
            e.printStackTrace();
            ToastService.showShortToast(getResources().getString(R.string.hint_request_fail));
        }
    }

    public void onSetDone(JSONObject response) {
        try {
            if (response == null) throw new APIException();

            boolean isSuccess = response.getBoolean("isSuccessed");
            if (isSuccess) {
            }
            else {
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        catch (APIException e) {
            e.printStackTrace();
            ToastService.showShortToast(getResources().getString(R.string.hint_request_fail));
        }
    }

    public void onDelete(JSONObject response) {
        try {
            if (response == null) throw new APIException();

            boolean isSuccess = response.getBoolean("isSuccessed");
            if (isSuccess) {
            }
            else {
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        catch (APIException e) {
            e.printStackTrace();
            ToastService.showShortToast(getResources().getString(R.string.hint_request_fail));
        }
    }

    public void onUpdateContent(JSONObject response) {
        try {
            if (response == null) throw new APIException();

            boolean isSuccess = response.getBoolean("isSuccessed");
            if (isSuccess) {
            }
            else {
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        catch (APIException e) {
            e.printStackTrace();
            ToastService.showShortToast(getResources().getString(R.string.hint_request_fail));
        }
    }

    public void onReCreatedToDo(JSONObject response) {
        onAddedResponse(response);
        mDeletedItemFragment.setupListData(ToDoListReference.DeletedList);
    }

    public void onInitial() {
        //先序列化回来
        ArrayList<ToDo> list = SerializerHelper.deSerializeFromFile(this, SerializerHelper.todosFileName);
        if (list != null) {
            ToDoListReference.TodosList = list;
        }
        //已经登陆了
        if (!ConfigHelper.ISOFFLINEMODE) {
            mToDoFragment.UpdateData(ToDoListReference.TodosList);
            syncList();
        }
        //离线模式
        else {
            mToDoFragment.UpdateData(ToDoListReference.TodosList);
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
        }
        else if (isAddingPaneShown) {
            hideAddingPane();
        }
        else {
            super.onBackPressed();
        }
    }
}
