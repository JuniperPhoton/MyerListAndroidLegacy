package fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import activity.AboutActivity;
import activity.SettingActivity;
import activity.StartActivity;
import adapter.NavigationDrawerAdapter;
import interfaces.IDrawerStatusChanged;
import interfaces.INavigationDrawerMainCallbacks;
import model.NavigationItemWithIcon;

import com.juniperphoton.myerlistandroid.R;

import java.util.ArrayList;
import java.util.List;

import util.AppExtension;
import util.ConfigHelper;

public class NavigationDrawerFragment extends Fragment implements INavigationDrawerMainCallbacks {

    private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";

    private static final String PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";

    private INavigationDrawerMainCallbacks mCallbacks;

    private IDrawerStatusChanged mDrawerStatusListener;

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

    private LinearLayout mSettingsLayout;
    private LinearLayout mAboutLayout;

    private TextView mUndoneTextView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
        View view = inflater.inflate(R.layout.fragment_navigation_drawer, container, false);

        mUndoneTextView = (TextView) view.findViewById(R.id.undoneCount_textview);

        //显示电子邮件
        mEmailView = (TextView) view.findViewById(R.id.account_block);
        mEmailView.setText(ConfigHelper.getString(view.getContext(), "email"));

        mRootLayout = (RelativeLayout) view.findViewById(R.id.drawer_root_layout);
        mRootLayout.setOnTouchListener(new View.OnTouchListener() {
            //避免透过抽屉点击到下面的列表
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true;
            }
        });

        //显示类别
        mDrawerRecyclerView = (RecyclerView) view.findViewById(R.id.drawerList);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        mDrawerRecyclerView.setLayoutManager(layoutManager);
        mDrawerRecyclerView.setHasFixedSize(true);

        //获得类别列表
        final List<NavigationItemWithIcon> navigationItemWithIcons = getCateList();

        NavigationDrawerAdapter adapter = new NavigationDrawerAdapter(navigationItemWithIcons);
        adapter.setNavigationDrawerCallbacks(this);
        mDrawerRecyclerView.setAdapter(adapter);

        //默认项是所有待办事项
        selectItem(mCurrentSelectedPosition);

        //显示设置/关于
        mSettingsLayout = (LinearLayout) view.findViewById(R.id.drawer_settings_layout);
        mAboutLayout = (LinearLayout) view.findViewById(R.id.drawer_about_layout);
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
        return view;
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

    @Override
    public void OnDrawerMainItemSelected(int position) {
        selectItem(position);
    }

    public List<NavigationItemWithIcon> getCateList() {
        List<NavigationItemWithIcon> items = new ArrayList<>();
        items.add(new NavigationItemWithIcon(getResources().getString(R.string.cate_default), getResources().getDrawable(R.drawable.cate_default)));
        items.add(new NavigationItemWithIcon(getResources().getString(R.string.cate_work), getResources().getDrawable(R.drawable.cate_work)));
        items.add(new NavigationItemWithIcon(getResources().getString(R.string.cate_life), getResources().getDrawable(R.drawable.cate_life)));
        items.add(new NavigationItemWithIcon(getResources().getString(R.string.cate_family), getResources().getDrawable(R.drawable.cate_family)));
        items.add(new NavigationItemWithIcon(getResources().getString(R.string.cate_enter), getResources().getDrawable(R.drawable.cate_enter)));
        items.add(new NavigationItemWithIcon(getResources().getString(R.string.deleteditems), getResources().getDrawable(R.drawable.cate_deleted)));
        return items;
    }


    public void setup(int fragmentId, DrawerLayout drawerLayout, Toolbar toolbar) {
        mFragmentContainerView = getActivity().findViewById(fragmentId);
        mDrawerLayout = drawerLayout;

        mDrawerLayout.setStatusBarBackgroundColor(getResources().getColor(R.color.myPrimaryDarkColor));

        mActionBarDrawerToggle = new ActionBarDrawerToggle(getActivity(), mDrawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                if (!isAdded()) return;

                getActivity().invalidateOptionsMenu(); // calls onPrepareOptionsMenu()

                mDrawerStatusListener.OnDrawerStatusChanged(false);
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
                mDrawerStatusListener.OnDrawerStatusChanged(true);
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

        mDrawerLayout.setDrawerListener(mActionBarDrawerToggle);
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

    public int getCurrentSelectedPosition() {
        return mCurrentSelectedPosition;
    }

    public void selectItem(int position) {
        mCurrentSelectedPosition = position;

        if (mCallbacks != null) {
            mCallbacks.OnDrawerMainItemSelected(position);
        }

        ((NavigationDrawerAdapter) mDrawerRecyclerView.getAdapter()).selectPosition(position);

        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(mFragmentContainerView);
        }
    }

    public void updateUndoneTextView(String count) {
        if (mUndoneTextView != null) {
            mUndoneTextView.setText(count);
        }
    }

    public void updateRootBackgroundColor(int color){
        if(mRootLayout!=null){
            mRootLayout.setBackgroundColor(color);
        }
    }

    public void openDrawer() {
        mDrawerLayout.openDrawer(mFragmentContainerView);
    }

    public void closeDrawer() {
        mDrawerLayout.closeDrawer(mFragmentContainerView);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallbacks = (INavigationDrawerMainCallbacks) activity;
            mDrawerStatusListener = (IDrawerStatusChanged) activity;
        }
        catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement INavigationDrawerMainCallbacks.");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
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
}
