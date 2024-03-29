package activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.reflect.TypeToken;
import com.juniperphoton.myerlistandroid.R;

import api.CloudServices;
import exception.APIException;
import interfaces.INavigationDrawerCallback;
import interfaces.IRequestCallback;
import model.ToDoCategory;
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
import util.GlobalListLocator;
import moe.feng.material.statusbar.StatusBarCompat;
import util.ToastService;
import view.CircleRadioButton;

public class MainActivity extends AppCompatActivity implements INavigationDrawerCallback,
        IDrawerStatusChanged {
    private NavigationDrawerFragment mNavigationDrawerFragment;
    private ToDoFragment mToDoFragment;
    private DeletedItemFragment mDeletedItemFragment;
    private Toolbar mToolbar;

    private boolean isAddingPaneShown = false;
    private EditText mEditedText;
    private FrameLayout mFragmentLayout;
    private Button mOKBtn;
    private Button mCancelBtn;

    private RelativeLayout mAddingPaneLayout;
    private TextView mAddingCateHintTextView;
    private TextView mAddingTitleTextView;

    private RadioGroup mAddingCateRadioGroup;

    private int mCurrentDisplayedCateID = 0;
    private int mCateIDAboutToAdd = 0;

    private ToDo mToDoAboutToAdded;
    private boolean misStagedItemsNotEmpty = false;

    private boolean mAboutToModify = false;
    private ToDo mToDoAboutToModify;

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

        initViews();

        String access_token = ConfigHelper.getString(this, "access_token");
        boolean offline = ConfigHelper.getBoolean(this, "offline_mode");

        if (access_token != null) {
            initFragment(savedInstanceState, true);
        } else {
            ConfigHelper.ISOFFLINEMODE = true;
            mNavigationDrawerFragment.setupOfflineMode();
            initFragment(savedInstanceState, false);
        }

    }

    //找到需要初始化的控件
    private void initViews() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(mToolbar);

        mFragmentLayout = (FrameLayout) findViewById(R.id.fragment_container);

        mAddingCateRadioGroup = (RadioGroup) findViewById(R.id.add_pane_radio);
        mAddingCateRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                RadioButton button = (RadioButton) radioGroup.findViewById(checkedId);
                ToDoCategory category = GlobalListLocator.CategoryList.get(radioGroup.indexOfChild(button));
                mCateIDAboutToAdd = category.getID();
                updateAddingPaneColorByCateId(category.getID());
                mAddingCateHintTextView.setText(category.getName());
            }
        });

        mAddingTitleTextView=(TextView)findViewById(R.id.dialog_title_text);
        mAddingCateHintTextView = (TextView) findViewById(R.id.fragment_adding_pane_cate_textView);
        mAddingPaneLayout = (RelativeLayout) findViewById(R.id.main_a_adding_panel);
        mAddingPaneLayout.setOnTouchListener(new View.OnTouchListener() {
            //防止触控穿透
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true;
            }
        });

        if(GlobalListLocator.CategoryList!=null) updateRatioButtons();

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
    private void updateAddingPaneColorByCateId(int cateID) {
        if (mAddingPaneLayout == null) return;
        ToDoCategory category = GlobalListLocator.GetCategoryByCateID(cateID);
        if (category.getID() != -2) {
            mAddingPaneLayout.setBackgroundColor(category.getColor());
        }
    }

    //初始化 Fragment
    private void initFragment(Bundle savedInstanceState, boolean logined) {
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
                ToastService.sendToast(getResources().getString(R.string.NoNetworkHint));
            }
            //暂存区有待办事项的，同步到云端
            if (!ConfigHelper.ISOFFLINEMODE && AppUtil.isNetworkAvailable(AppExtension.getInstance())) {
                if (GlobalListLocator.StagedList == null) return;
                misStagedItemsNotEmpty = true;
                for (ToDo todo : GlobalListLocator.StagedList) {
                    CloudServices.addToDo(ConfigHelper.getSid(), ConfigHelper.getAccessToken(),
                            todo.getContent(), "0", todo.getCate(),
                            new IRequestCallback() {
                                @Override
                                public void onResponse(JSONObject jsonObject) {
                                    onAddedResponse(jsonObject);
                                }
                            });
                }
                GlobalListLocator.StagedList.clear();
                misStagedItemsNotEmpty = false;
                SerializerHelper.serializeToFile(AppExtension.getInstance(),
                        GlobalListLocator.StagedList,
                        SerializerHelper.stagedFileName);
            }
        }
    }

    private void updateRatioButtons() {
        mAddingCateRadioGroup.removeAllViews();
        for (ToDoCategory category : GlobalListLocator.CategoryList) {
            if (category.getID() == -1 || category.getID() == -2) continue;
            CircleRadioButton circleRadioButton = new CircleRadioButton(this);
            circleRadioButton.setCircleColor(category.getColor());
            circleRadioButton.setLeft(5);
            mAddingCateRadioGroup.addView(circleRadioButton);
        }
        mAddingCateRadioGroup.check(mAddingCateRadioGroup.getChildAt(0).getId());
    }

    //抽屉选中一个项的时候
    @Override
    public void onDrawerMainItemSelected(int position) {
        ToDoCategory category = GlobalListLocator.CategoryList.get(position);
        mCurrentDisplayedCateID = category.getID();
        mCateIDAboutToAdd = category.getID();

        RadioButton radioButton = (RadioButton) mAddingCateRadioGroup.getChildAt(position);
        if (radioButton != null) {
            mAddingCateRadioGroup.check(radioButton.getId());
        }

        updateAddingPaneColorByCateId(category.getID());

        try {
            if (category.getID() == 0) {
                if (mToDoFragment == null) {
                    mToDoFragment = new ToDoFragment();
                }

                mToolbar.setBackgroundColor(getResources().getColor(R.color.MyerListBlue));
                mToolbar.setTitle(getResources().getString(R.string.cate_default));

                mAddingPaneLayout.setBackgroundColor(getResources().getColor(R.color.MyerListBlue));
                mNavigationDrawerFragment.updateRootBackgroundColor(getResources().getColor(R.color.MyerListBlue));
            } else if (category.getID() == -1) {
                switchToDeleteFragment();
                mToolbar.setBackgroundColor(getResources().getColor(R.color.DeletedColor));
                mNavigationDrawerFragment.updateRootBackgroundColor(getResources().getColor(R.color.DeletedColor));
                mToolbar.setTitle(getResources().getString(R.string.deleteditems));
            }
            else {
                mToolbar.setBackgroundColor(category.getColor());
                mNavigationDrawerFragment.updateRootBackgroundColor(category.getColor());
                mToolbar.setTitle(category.getName());
            }
            updateListByCategory();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onFooterSelected() {
        ToastService.sendToast(getResources().getString(R.string.hint_personalize));
    }

    public void updateListByCategory() {
        if (mCurrentDisplayedCateID == -1 || mCurrentDisplayedCateID == -2) return;

        ArrayList<ToDo> newList = new ArrayList<>();
        if (mCurrentDisplayedCateID == 0) newList = GlobalListLocator.TodosList;
        else {
            for (ToDo todo : GlobalListLocator.TodosList) {
                if (todo.getCate() == mCurrentDisplayedCateID) {
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

    public void setupAddingPaneForModifyAndShow(ToDo todo) {
        mAboutToModify = true;
        mEditedText.setText(todo.getContent());
        mToDoAboutToModify = todo;
        showAddingPane();

        ToDoCategory category = GlobalListLocator.GetCategoryByCateID(mToDoAboutToModify.getCate());
        int position = GlobalListLocator.CategoryList.indexOf(category);
        mAddingCateRadioGroup.check(mAddingCateRadioGroup.getChildAt(position).getId());
    }

    public void showAddingPane() {
        if(!mAboutToModify){
            mAddingTitleTextView.setText(R.string.adding_title);
        }
        else {
            mAddingTitleTextView.setText(R.string.modify_memo_title);
        }
        isAddingPaneShown = true;

        // get the center for the clipping circle
        int cx = mAddingPaneLayout.getWidth() - 160;
        int cy = mAddingPaneLayout.getHeight() - 160;

        if (mAboutToModify) {
            cx = 50;
            cy = 50;
        }

        // get the final radius for the clipping circle
        int finalRadius = Math.max(mAddingPaneLayout.getWidth(), mAddingPaneLayout.getHeight());

        // create the animator for this view (the start radius is zero)
        Animator anim;
        anim = ViewAnimationUtils.createCircularReveal(mAddingPaneLayout, cx, cy, 0, finalRadius);

        // make the view visible and start the animation
        mAddingPaneLayout.setVisibility(View.VISIBLE);
        anim.start();

        if (!mAboutToModify) {
            mEditedText.requestFocus();
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    InputMethodManager inputMethodManager = (InputMethodManager) mEditedText.getContext().
                            getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.showSoftInput(mEditedText, 0);
                }
            }, 333);
        }
    }

    public void hideAddingPane() {
        isAddingPaneShown = false;

        // get the center for the clipping circle
        int cx = mAddingPaneLayout.getWidth() - 160;
        int cy = mAddingPaneLayout.getHeight() - 160;

        if (mAboutToModify) {
            cx = 50;
            cy = 50;
        }

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

                mAboutToModify = false;
                mEditedText.setText("");
                mCateIDAboutToAdd = mCurrentDisplayedCateID;

                mAddingCateRadioGroup.check(mAddingCateRadioGroup.getChildAt(0).getId());
                updateAddingPaneColorByCateId(mCurrentDisplayedCateID);
            }
        });

        // start the animation
        anim.start();
    }

    //从服务器同步列表，并排序
    public void syncCateAndList() {
        mNavigationDrawerFragment.syncCatesOrDefault();
    }

    //在同步完类类别后调用
    public void syncList() {
        updateRatioButtons();
        CloudServices.getLatestSchedules(ConfigHelper.getSid(), ConfigHelper.getAccessToken(), new IRequestCallback() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                onGotLatestScheduleResponse(jsonObject);
            }
        });
    }

    //添加面板点击确认
    public void okClick(View v) {

        if (mEditedText.getText().toString().isEmpty()) {
            ToastService.sendToast(getResources().getString(R.string.hint_empty_input));
            return;
        }

        if (isAddingPaneShown) {
            hideAddingPane();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(mEditedText.getWindowToken(),0);
        }

        final ToDo tempToDo = new ToDo();
        tempToDo.setContent(mEditedText.getText().toString());
        tempToDo.setIsDone(false);
        if (mAboutToModify) {
            tempToDo.setID(mToDoAboutToModify.getID());
        } else tempToDo.setID(java.util.UUID.randomUUID().toString());
        tempToDo.setCate(mCateIDAboutToAdd);

        mToDoAboutToAdded = tempToDo;

        //离线模式
        if (ConfigHelper.ISOFFLINEMODE) {
            addNewToDoToList(tempToDo);
        }
        //非离线模式，发送请求
        else {
            if (mAboutToModify) {
                ToDoListAdapter adapter = (ToDoListAdapter) mToDoFragment.mToDoRecyclerView.getAdapter();
                adapter.updateContent(tempToDo);
            } else {
                addNewToDoToList(tempToDo);
                CloudServices.addToDo(ConfigHelper.getSid(), ConfigHelper.getAccessToken(), mEditedText.getText().toString(),
                        "0", mCateIDAboutToAdd,
                        new IRequestCallback() {
                            @Override
                            public void onResponse(JSONObject jsonObject) {
                                onAddedResponse(jsonObject);
                            }
                        });
            }
        }
        dismissDialog();
    }

    //添加面板点击取消
    public void cancelClick(View v) {
        dismissDialog();
    }

    private void dismissDialog() {
        if (isAddingPaneShown) {
            hideAddingPane();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(mEditedText.getWindowToken(),0);
        }
    }

    public void setIsAddStagedItems(boolean value) {
        misStagedItemsNotEmpty = value;
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
            ToastService.sendToast(getResources().getString(R.string.hint_request_fail));
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
                GlobalListLocator.TodosList = listInOrder;
                mToDoFragment.updateData(listInOrder);

                ToastService.sendToast(getResources().getString(R.string.Synced));

                SerializerHelper.serializeToFile(AppExtension.getInstance(), GlobalListLocator.TodosList, SerializerHelper.todosFileName);

                updateListByCategory();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (APIException e) {
            e.printStackTrace();
            ToastService.sendToast(getResources().getString(R.string.hint_request_fail));
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void onAddedResponse(JSONObject response) {
        try {
            if (response == null) throw new APIException();

            Boolean isSuccess = response.getBoolean("isSuccessed");
            if (isSuccess) {
                ToDo newToDo = ToDo.parseJsonObjToObj(response.getJSONObject("ScheduleInfo"));
                mToDoAboutToAdded.setID(newToDo.getID());
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
                    GlobalListLocator.StagedList.add(mToDoAboutToAdded);
                    SerializerHelper.serializeToFile(AppExtension.getInstance(), GlobalListLocator.StagedList, SerializerHelper.stagedFileName);
                }
                GlobalListLocator.TodosList.add(mToDoAboutToAdded);
                SerializerHelper.serializeToFile(AppExtension.getInstance(), GlobalListLocator.TodosList, SerializerHelper.todosFileName);
            }
            mToDoAboutToAdded = null;
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (APIException e) {
            e.printStackTrace();
            ToastService.sendToast(getResources().getString(R.string.hint_request_fail));
        }
    }

    private void addNewToDoToList(ToDo newToDo) {
        ToDoListAdapter adapter = (ToDoListAdapter) mToDoFragment.mToDoRecyclerView.getAdapter();
        adapter.addToDo(newToDo);

        ToastService.sendToast(getResources().getString(R.string.add_success));

        updateListByCategory();

        if (misStagedItemsNotEmpty) {
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
            ToastService.sendToast(getResources().getString(R.string.hint_request_fail));
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
            ToastService.sendToast(getResources().getString(R.string.hint_request_fail));
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
            ToastService.sendToast(getResources().getString(R.string.hint_request_fail));
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
            ToastService.sendToast(getResources().getString(R.string.hint_request_fail));
        }
    }

    public void onReCreatedToDo(JSONObject response) {
        onAddedResponse(response);
        mDeletedItemFragment.setupListData(GlobalListLocator.DeletedList);
    }


    public void onInit() {
        try {
            Type type = new TypeToken<ArrayList<ToDo>>() {
            }.getType();
            //先序列化回来
            ArrayList<ToDo> list = SerializerHelper.deSerializeFromFile(
                    type, this, SerializerHelper.todosFileName);

            if (list != null) {
                GlobalListLocator.TodosList = list;
            }
            //已经登陆了
            if (!ConfigHelper.ISOFFLINEMODE) {
                mToDoFragment.updateData(GlobalListLocator.TodosList);
            }
            //离线模式
            else {
                mToDoFragment.updateData(GlobalListLocator.TodosList);
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
            dismissDialog();
        } else {
            //super.onBackPressed();
            Intent intent= new Intent(Intent.ACTION_MAIN);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addCategory(Intent.CATEGORY_HOME);
            startActivity(intent);
        }
    }
}
