package activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.reflect.TypeToken;
import com.juniperphoton.jputils.LocalSettingHelper;
import com.juniperphoton.jputils.SerializerHelper;
import com.juniperphoton.myerlistandroid.R;

import api.CloudServices;
import exception.APIException;
import fragment.ToDoFragment;
import interfaces.INavigationDrawerCallback;
import interfaces.IRequestCallback;
import model.ToDoCategory;
import interfaces.IDrawerStatusChanged;
import fragment.DeletedItemFragment;
import fragment.NavigationDrawerFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import util.AppUtil;
import util.ConfigHelper;
import util.AppExtension;
import model.ToDo;
import util.GlobalListLocator;
import moe.feng.material.statusbar.StatusBarCompat;
import util.SerializationName;
import util.ToastService;
import view.CircleRadioButton;

public class MainActivity extends AppCompatActivity implements INavigationDrawerCallback, IDrawerStatusChanged {
    private NavigationDrawerFragment mNavigationDrawerFragment;
    private ToDoFragment mToDoFragment;
    private DeletedItemFragment mDeletedItemFragment;
    private Toolbar mToolbar;

    private boolean isAddingPaneShown = false;
    private EditText mEditedText;

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

    private int[] mLastCP = new int[2];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarCompat.setUpActivity(this);

        setContentView(R.layout.activity_main);

        initViews(savedInstanceState);

        String access_token = LocalSettingHelper.getString(this, "access_token");

        if (access_token != null) {
            initFragment(savedInstanceState, true);
        } else {
            ConfigHelper.ISOFFLINEMODE = true;
            mNavigationDrawerFragment.setupOfflineMode();
            initFragment(savedInstanceState, false);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override

    public void onPause() {
        super.onPause();
    }

    /**
     * Init views
     * @param savedInstanceState
     */
    private void initViews(Bundle savedInstanceState) {
        mToolbar = (Toolbar) findViewById(R.id.activity_main_tb);
        mToolbar.setTitle(R.string.cate_default);
        setSupportActionBar(mToolbar);

        mAddingCateRadioGroup = (RadioGroup) findViewById(R.id.fragment_adding_pane_radio);
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

        mAddingTitleTextView = (TextView) findViewById(R.id.fragment_adding_pane_title_tv);
        mAddingCateHintTextView = (TextView) findViewById(R.id.fragment_adding_pane_cate_tv);
        mAddingPaneLayout = (RelativeLayout) findViewById(R.id.activity_main_adding_pane);
        mAddingPaneLayout.setOnTouchListener(new View.OnTouchListener() {
            //防止触控穿透
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true;
            }
        });

        if (GlobalListLocator.CategoryList != null) updateRatioButtons();

        mEditedText = (EditText) findViewById(R.id.fragment_adding_pane_add_et);
        Button okBtn = (Button) findViewById(R.id.fragment_adding_pane_pane_ok_btn);
        Button cancelBtn = (Button) findViewById(R.id.fragment_adding_pane_cancel_btn);

        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                okClick(v);
            }
        });
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelClick(v);
            }
        });

        mNavigationDrawerFragment = (NavigationDrawerFragment) getSupportFragmentManager()
                .findFragmentById(R.id.activity_main_fragment_drawer);
        mNavigationDrawerFragment.setup(R.id.activity_main_fragment_drawer,
                (DrawerLayout) findViewById(R.id.acitivity_main_drawer), mToolbar);
    }

    private void updateAddingPaneColorByCateId(int cateID) {
        if (mAddingPaneLayout == null) return;
        ToDoCategory category = GlobalListLocator.GetCategoryByCateID(cateID);
        if (category.getID() != -2) {
            mAddingPaneLayout.setBackgroundColor(category.getColor());
        }
    }

    private void initFragment(Bundle savedInstanceState, boolean logined) {
        if (findViewById(R.id.activity_main_fl) != null) {
            if (savedInstanceState != null) {

                Fragment fragment = getSupportFragmentManager().findFragmentByTag(ToDoFragment.class.getName());
                if (fragment instanceof ToDoFragment) {
                    mToDoFragment = (ToDoFragment) fragment;
                }

                getSupportFragmentManager().beginTransaction().show(mToDoFragment).commit();
            } else {
                mToDoFragment = new ToDoFragment();
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.activity_main_fl, mToDoFragment, ToDoFragment.class.getName())
                        .commit();
            }
        }

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
                    SerializationName.STAGED_FILE_NAME);
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
                mToolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.MyerListBlue));
                mToolbar.setTitle(getResources().getString(R.string.cate_default));

                mAddingPaneLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.MyerListBlue));
                mNavigationDrawerFragment.updateRootBackgroundColor(ContextCompat.getColor(this, R.color.MyerListBlue));
            } else if (category.getID() == -1) {
                switchToDeleteFragment();
                mToolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.DeletedColor));
                mNavigationDrawerFragment.updateRootBackgroundColor(ContextCompat.getColor(this, R.color.DeletedColor));
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

    @Override
    public void onFooterSelected() {
        //ToastService.sendToast(getResources().getString(R.string.hint_personalize));
        Intent intent = new Intent(MainActivity.this, CatePersonalizaionActivity.class);
        startActivity(intent);
        mNavigationDrawerFragment.closeDrawer();
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
            getSupportFragmentManager().beginTransaction().replace(R.id.activity_main_fl, mToDoFragment)
                    .commitAllowingStateLoss();

            mToDoFragment.updateData(newList);
        }
    }

    public void switchToDeleteFragment() {
        if (mDeletedItemFragment == null) {
            mDeletedItemFragment = new DeletedItemFragment();
        }
        getSupportFragmentManager().beginTransaction().replace(R.id.activity_main_fl, mDeletedItemFragment).commit();
    }

    public void setupAddingPaneForModifyAndShow(ToDo todo, int[] itemPosition) {
        mAboutToModify = true;
        mEditedText.setText(todo.getContent());
        mToDoAboutToModify = todo;
        showAddingPane(itemPosition);

        ToDoCategory category = GlobalListLocator.GetCategoryByCateID(mToDoAboutToModify.getCate());
        int catePosition = GlobalListLocator.CategoryList.indexOf(category);
        mAddingCateRadioGroup.check(mAddingCateRadioGroup.getChildAt(catePosition).getId());
    }

    public void showAddingPane(int[] startPosition) {
        if (!mAboutToModify) {
            mAddingTitleTextView.setText(R.string.adding_title);
        } else {
            mAddingTitleTextView.setText(R.string.modify_memo_title);
        }
        isAddingPaneShown = true;

        // get the center for the clipping circle
        int cx = mToDoFragment.getFABPostion()[0] + mToDoFragment.getFABRadius();
        int cy = mToDoFragment.getFABPostion()[1] + mToDoFragment.getFABRadius();

        if (mAboutToModify) {
            cx = startPosition[0];
            cy = startPosition[1];
            mLastCP = startPosition;
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

    /**
     *
     */
    public void hideAddingPane() {
        isAddingPaneShown = false;

        // get the center for the clipping circle
        int cx;
        int cy;

        if (mAboutToModify) {
            cx = mLastCP[0];
            cy = mLastCP[1];
        } else {
            // get the center for the clipping circle
            cx = mToDoFragment.getFABPostion()[0] + mToDoFragment.getFABRadius();
            cy = mToDoFragment.getFABPostion()[1] + mToDoFragment.getFABRadius();
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
            imm.hideSoftInputFromWindow(mEditedText.getWindowToken(), 0);
        }

        final ToDo tempToDo = new ToDo();
        tempToDo.setContent(mEditedText.getText().toString());
        tempToDo.setIsDone(false);
        tempToDo.setCate(mCateIDAboutToAdd);
        if (mAboutToModify) {
            tempToDo.setID(mToDoAboutToModify.getID());
        } else tempToDo.setID(java.util.UUID.randomUUID().toString());

        mToDoAboutToAdded = tempToDo;

        //离线模式
        if (ConfigHelper.ISOFFLINEMODE) {
            addNewToDoToList(tempToDo);
        }
        //非离线模式，发送请求
        else {
            if (mAboutToModify) {
                mToDoFragment.updateContent(tempToDo);
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
            imm.hideSoftInputFromWindow(mEditedText.getWindowToken(), 0);
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
                            LocalSettingHelper.getString(this, "sid"),
                            LocalSettingHelper.getString(this, "access_token"),
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

                SerializerHelper.serializeToFile(AppExtension.getInstance(), GlobalListLocator.TodosList, SerializationName.TODOS_FILE_NAME);

                updateListByCategory();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (APIException e) {
            e.printStackTrace();
            ToastService.sendToast(getResources().getString(R.string.hint_request_fail));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onAddedResponse(JSONObject response) {
        try {
            if (response == null) throw new APIException();

            Boolean isSuccess = response.getBoolean("isSuccessed");
            if (isSuccess) {
                ToDo newToDo = ToDo.parseJsonObjToObj(response.getJSONObject("ScheduleInfo"));
                if (newToDo != null) {
                    if (mToDoAboutToAdded != null) {
                        mToDoAboutToAdded.setID(newToDo.getID());
                    }
                    CloudServices.setListOrder(LocalSettingHelper.getString(this, "sid"),
                            LocalSettingHelper.getString(this, "access_token"),
                            ToDo.getOrderString(mToDoFragment.getData()),
                            new IRequestCallback() {
                                @Override
                                public void onResponse(JSONObject jsonObject) {
                                    onUpdateOrder(jsonObject);
                                }
                            });
                }
            } else {
                if (mToDoAboutToAdded != null) {
                    GlobalListLocator.StagedList.add(mToDoAboutToAdded);
                    SerializerHelper.serializeToFile(AppExtension.getInstance(), GlobalListLocator.StagedList, SerializationName.STAGED_FILE_NAME);
                }
                GlobalListLocator.TodosList.add(mToDoAboutToAdded);
                SerializerHelper.serializeToFile(AppExtension.getInstance(), GlobalListLocator.TodosList, SerializationName.TODOS_FILE_NAME);
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
        mToDoFragment.addToDo(newToDo);

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
            if (!isSuccess) {
                throw new APIException();
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
            if (!isSuccess) {
                throw new APIException();
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
            if (!isSuccess) {
                throw new APIException();
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
            if (!response.getBoolean("isSuccessed")) {
                throw new APIException();
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

    @Override
    public void onDrawerStatusChanged(boolean isOpen) {

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
            super.onBackPressed();
//            Intent intent = new Intent(Intent.ACTION_MAIN);
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            intent.addCategory(Intent.CATEGORY_HOME);
//            startActivity(intent);
        }
    }
}
