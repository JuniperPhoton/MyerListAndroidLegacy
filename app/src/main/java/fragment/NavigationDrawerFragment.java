package fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import activity.AboutActivity;
import activity.MainActivity;
import activity.SettingActivity;
import activity.StartActivity;
import adapter.NavigationDrawerAdapter;
import api.CloudServices;
import exception.APIException;
import interfaces.INavigationDrawerCallback;
import interfaces.IRequestCallback;

import com.juniperphoton.jputils.CustomFontHelper;
import com.juniperphoton.jputils.LocalSettingHelper;
import com.juniperphoton.jputils.SerializerHelper;
import com.juniperphoton.myerlistandroid.R;
import com.orhanobut.logger.Logger;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import model.ToDoCategory;
import common.AppExtension;
import util.AppConfig;
import util.GlobalListLocator;

import util.SerializationName;

public class NavigationDrawerFragment extends Fragment implements INavigationDrawerCallback {

    private static final String TAG = NavigationDrawerFragment.class.getName();

    private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";

    private static final String PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";

    private MainActivity mMainActivity;

    private ActionBarDrawerToggle mActionBarDrawerToggle;

    //表示抽屉里各种控件
    private DrawerLayout mDrawerLayout;
    private RecyclerView mDrawerRecyclerView;
    private RelativeLayout mRootLayout;

    private View mFragmentContainerView;
    private TextView mEmailView;

    private int mCurrentSelectedPosition = 0;
    private boolean mFromSavedInstanceState;
    private boolean mUserLearnedDrawer;

    private RelativeLayout mSettingsLayout;
    private RelativeLayout mAboutLayout;

    private TextView mUndoneTextView;

    private ArrayList<ToDoCategory> mCatesList;

    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
        try {
            mMainActivity = (MainActivity) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement INavigationDrawerCallback.");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Logger.init(TAG);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mUserLearnedDrawer = sp.getBoolean(PREF_USER_LEARNED_DRAWER, false);

        if (savedInstanceState != null) {
            mCurrentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION);
            mFromSavedInstanceState = true;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_navigation_drawer, container, false);
        initViews(view);

        return view;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private void initViews(View view) {
        mUndoneTextView = (TextView) view.findViewById(R.id.fragment_drawer_undone_tv);
        CustomFontHelper.setCustomFont(mUndoneTextView, "fonts/AGENCYB.TTF", getActivity());

        //显示电子邮件
        mEmailView = (TextView) view.findViewById(R.id.fragment_drawer_login_tv);
        mEmailView.setText(LocalSettingHelper.getString(view.getContext(), "email"));

        mRootLayout = (RelativeLayout) view.findViewById(R.id.fragment_drawer_root_ll);
        mRootLayout.setOnTouchListener(new View.OnTouchListener() {
            //避免透过抽屉点击到下面的列表
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true;
            }
        });

        //显示类别
        mDrawerRecyclerView = (RecyclerView) view.findViewById(R.id.fragment_drawer_rv);
        mDrawerRecyclerView.setLayoutManager(new LinearLayoutManager(mMainActivity, LinearLayoutManager.VERTICAL, false));
        mDrawerRecyclerView.setAdapter(new NavigationDrawerAdapter(this, GlobalListLocator.CategoryList));

        //显示设置/关于
        mSettingsLayout = (RelativeLayout) view.findViewById(R.id.fragment_drawer_settings_ll);
        mAboutLayout = (RelativeLayout) view.findViewById(R.id.fragment_drawer_about_ll);
        mSettingsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AppExtension.getInstance(), SettingActivity.class);
                startActivity(intent);
            }
        });
        mAboutLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AppExtension.getInstance(), AboutActivity.class);
                startActivity(intent);
            }
        });

        syncCatesOrDefault();
    }

    public boolean isDrawerOpen() {
        return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(mFragmentContainerView);
    }

    public ActionBarDrawerToggle getActionBarDrawerToggle() {
        return mActionBarDrawerToggle;
    }

    public DrawerLayout getDrawerLayout() {
        return mDrawerLayout;
    }

    private ArrayList<ToDoCategory> getDefaultCateList() {
        ArrayList<ToDoCategory> items = new ArrayList<>();
//        items.add(new ToDoCategory(getResources().getString(R.string.cate_default), 0,
//                ContextCompat.getColor(AppExtension.getInstance(), R.color.MyerListBlue)));
        items.add(new ToDoCategory(getResources().getString(R.string.cate_work), 1,
                ContextCompat.getColor(AppExtension.getInstance(), R.color.WorkColor)));
        items.add(new ToDoCategory(getResources().getString(R.string.cate_life), 2,
                ContextCompat.getColor(AppExtension.getInstance(), R.color.LifeColor)));
        items.add(new ToDoCategory(getResources().getString(R.string.cate_family), 3,
                ContextCompat.getColor(AppExtension.getInstance(), R.color.FamilyColor)));
        items.add(new ToDoCategory(getResources().getString(R.string.cate_enter), 4,
                ContextCompat.getColor(AppExtension.getInstance(), R.color.EnterColor)));
//        items.add(new ToDoCategory(getResources().getString(R.string.deleteditems), 5,
//                ContextCompat.getColor(AppExtension.getInstance(), R.color.DeletedColor)));
        return items;
    }


    public void setup(int fragmentId, DrawerLayout drawerLayout, Toolbar toolbar) {
        mFragmentContainerView = getActivity().findViewById(fragmentId);
        mDrawerLayout = drawerLayout;

        mDrawerLayout.setStatusBarBackgroundColor(ContextCompat.getColor(getActivity(), R.color.myPrimaryDarkColor));

        mActionBarDrawerToggle = new ActionBarDrawerToggle(getActivity(), mDrawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                if (!isAdded()) return;

                getActivity().invalidateOptionsMenu(); // calls onPrepareOptionsMenu()

                mMainActivity.onDrawerStatusChanged(false);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                if (!isAdded()) return;
                if (!mUserLearnedDrawer) {
                    mUserLearnedDrawer = true;
                    SharedPreferences sp = PreferenceManager
                            .getDefaultSharedPreferences(getActivity());
                    sp.edit().putBoolean(PREF_USER_LEARNED_DRAWER, true).apply();
                }
                getActivity().invalidateOptionsMenu(); // calls onPrepareOptionsMenu()
                mMainActivity.onDrawerStatusChanged(true);
            }
        };

        if (!mUserLearnedDrawer && !mFromSavedInstanceState) {
            mDrawerLayout.openDrawer(mFragmentContainerView);
        }

        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mActionBarDrawerToggle.syncState();
            }
        });
        mDrawerLayout.addDrawerListener(mActionBarDrawerToggle);
    }

    public void setupOfflineMode() {
        mEmailView.setClickable(true);
        mEmailView.setText(R.string.now_to_login);
        mEmailView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), StartActivity.class);
                getActivity().startActivity(intent);
            }
        });
    }

    private void selectItem(int position) {
        mCurrentSelectedPosition = position;

        if (mMainActivity != null) {
            mMainActivity.onDrawerMainItemSelected(position);
        }

        ((NavigationDrawerAdapter) mDrawerRecyclerView.getAdapter()).selectPosition(position);

        if (mDrawerLayout != null) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mDrawerLayout.closeDrawer(mFragmentContainerView);
                }
            }, 10);
        }
    }

    public void updateUndoneTextView(String count) {
        if (mUndoneTextView != null) {
            mUndoneTextView.setText(count);
        }
    }

    public void updateRootBackgroundColor(int color) {
        if (mRootLayout != null) {
            mRootLayout.setBackgroundColor(color);
        }
    }

    public void syncCatesOrDefault() {
        if (!AppConfig.ISOFFLINEMODE) {
            CloudServices.getCategories(LocalSettingHelper.getString(getActivity(), "sid"),
                    LocalSettingHelper.getString(getActivity(), "access_token"), false, new IRequestCallback() {
                        @Override
                        public void onResponse(JSONObject jsonObject) {
                            if (jsonObject != null) Logger.json(jsonObject.toString());
                            onGotNewestCates(jsonObject);
                        }
                    });
        }
    }

    private void onGotNewestCates(JSONObject response) {
        try {
            ArrayList<ToDoCategory> categoryList;

            if (response == null) {
                categoryList = getDefaultCateList();
            } else {
                boolean isOK = response.getBoolean("isSuccessed");
                if (!isOK) {
                    throw new APIException();
                }
                String cateInfo = response.getString("Cate_Info");
                if (cateInfo.equals("null")) {
                    categoryList = getDefaultCateList();
                } else {
                    JSONObject cateJson = new JSONObject(cateInfo);

                    boolean isModified = cateJson.getBoolean("modified");
                    if (!isModified) {
                        categoryList = getDefaultCateList();
                    } else {
                        categoryList = new ArrayList<>();
                        JSONArray array = cateJson.getJSONArray("cates");
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject cateObj = array.getJSONObject(i);
                            String name = cateObj.getString("name");
                            String color = cateObj.getString("color");
                            int id = cateObj.getInt("id");

                            ToDoCategory category = new ToDoCategory(name, id, Color.parseColor(color));
                            categoryList.add(category);
                        }
                    }
                }
            }

            categoryList.add(0,
                    new ToDoCategory(getResources().getString(R.string.cate_default), 0, ContextCompat.getColor(getActivity(), R.color.MyerListBlue)));
            categoryList.add(categoryList.size(),
                    new ToDoCategory(getResources().getString(R.string.cate_deleted), -1, ContextCompat.getColor(getActivity(), R.color.DeletedColor)));
            categoryList.add(categoryList.size(),
                    new ToDoCategory(getResources().getString(R.string.cate_per), -2, Color.WHITE));

            GlobalListLocator.CategoryList = categoryList;

            SerializerHelper.serializeToFile(AppExtension.getInstance(), GlobalListLocator.CategoryList, SerializationName.CATES_FILE_NAME);

            setAdapter(categoryList);

            mMainActivity.syncList();
        } catch (APIException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setAdapter(ArrayList<ToDoCategory> categoryList) {
        NavigationDrawerAdapter adapter = new NavigationDrawerAdapter(this, categoryList);
        mDrawerRecyclerView.setAdapter(adapter);

        //默认项是所有待办事项
        selectItem(0);
    }

    public void openDrawer() {
        mDrawerLayout.openDrawer(mFragmentContainerView);
    }

    public void closeDrawer() {
        mDrawerLayout.closeDrawer(mFragmentContainerView);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SELECTED_POSITION, mCurrentSelectedPosition);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mActionBarDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onDrawerMainItemSelected(int position) {
        selectItem(position);
    }

    @Override
    public void onFooterSelected() {
        mMainActivity.onFooterSelected();
    }
}
