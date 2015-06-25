package fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import activity.StartActivity;
import adapter.NavigationDrawerAdapter;
import adapter.NavigationDrawerCallbacks;
import adapter.NavigationDrawerOtherCallbacks;
import model.NavigationItem;
import com.example.juniper.myerlistandroid.R;

import java.util.ArrayList;
import java.util.List;

import helper.ConfigHelper;

/**
 * Fragment used for managing interactions for and presentation of a navigation drawer.
 * See the <a href="https://developer.android.com/design/patterns/navigation-drawer.html#Interaction">
 * design guidelines</a> for a complete explanation of the behaviors implemented here.
 */
public class NavigationDrawerFragment extends Fragment implements NavigationDrawerCallbacks, NavigationDrawerOtherCallbacks
{

    /**
     * Remember the position of the selected item.
     */
    private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";

    /**
     * Per the design guidelines, you should show the drawer on launch until the user manually
     * expands it. This shared preference tracks this.
     */
    private static final String PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";

    /**
     * A pointer to the current callbacks instance (the Activity).
     */
    private NavigationDrawerCallbacks mCallbacks;
    private NavigationDrawerOtherCallbacks mOtherCallbacks;

    private DrawerStatusListener mDrawerStatusListener;

    /**
     * Helper component that ties the action bar to the navigation drawer.
     */
    private ActionBarDrawerToggle mActionBarDrawerToggle;

    private DrawerLayout mDrawerLayout;
    private RecyclerView mDrawerRecyclerView;
    private RecyclerView mDrawerOtherRecyclerView;
    private View mFragmentContainerView;
    private TextView mEmailView;

    private int mCurrentSelectedPosition = 0;
    private boolean mFromSavedInstanceState;
    private boolean mUserLearnedDrawer;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Read in the flag indicating whether or not the user has demonstrated awareness of the
        // drawer. See PREF_USER_LEARNED_DRAWER for details.
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mUserLearnedDrawer = sp.getBoolean(PREF_USER_LEARNED_DRAWER, false);


        if (savedInstanceState != null)
        {
            mCurrentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION);
            mFromSavedInstanceState = true;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_navigation_drawer, container, false);

        //Show account displaying email address
        mEmailView=(TextView)view.findViewById(R.id.account_block);
        mEmailView.setText(ConfigHelper.getString(view.getContext(), "email"));

        //RecyclerView to display navigation item
        mDrawerRecyclerView = (RecyclerView) view.findViewById(R.id.drawerList);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        mDrawerRecyclerView.setLayoutManager(layoutManager);
        mDrawerRecyclerView.setHasFixedSize(true);

        final List<NavigationItem> navigationItems = getMenu();

        NavigationDrawerAdapter adapter = new NavigationDrawerAdapter(navigationItems);
        adapter.setNavigationDrawerCallbacks(this);
        mDrawerRecyclerView.setAdapter(adapter);

        selectItem(mCurrentSelectedPosition);

        LinearLayoutManager layoutManagerOther = new LinearLayoutManager(getActivity());
        layoutManagerOther.setOrientation(LinearLayoutManager.VERTICAL);

        mDrawerOtherRecyclerView=(RecyclerView)view.findViewById(R.id.drawerList_other);
        mDrawerOtherRecyclerView.setLayoutManager(layoutManagerOther);
        mDrawerOtherRecyclerView.setHasFixedSize(true);

        final List<NavigationItem> otherItems=getOtherMenu();
        NavigationDrawerAdapter adapterOther=new NavigationDrawerAdapter(otherItems);
        adapterOther.setOtherNavigationDrawerCallbacks(this);
        mDrawerOtherRecyclerView.setAdapter(adapterOther);

        return view;
    }

    public boolean isDrawerOpen()
    {
        return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(mFragmentContainerView);
    }

    public ActionBarDrawerToggle getActionBarDrawerToggle()
    {
        return mActionBarDrawerToggle;
    }

    public DrawerLayout getDrawerLayout()
    {
        return mDrawerLayout;
    }

    @Override
    public void onNavigationDrawerItemSelected(int position)
    {
        selectItem(position);
    }

    @Override
    public void OnSelectedOther(int position)
    {
        selectOtherItem(position);
    }

    public List<NavigationItem> getMenu()
    {
        List<NavigationItem> items = new ArrayList<NavigationItem>();
        items.add(new NavigationItem(getResources().getString(R.string.title_section1),getResources().getDrawable(R.drawable.accept)));
        items.add(new NavigationItem(getResources().getString(R.string.title_section2), getResources().getDrawable(R.drawable.delete)));

        return items;
    }

    public List<NavigationItem> getOtherMenu()
    {
        List<NavigationItem> items = new ArrayList<NavigationItem>();

        items.add(new NavigationItem(getResources().getString(R.string.title_section3), getResources().getDrawable(R.drawable.settings)));
        items.add(new NavigationItem(getResources().getString(R.string.title_section4), getResources().getDrawable(R.drawable.like)));
        items.add(new NavigationItem(getResources().getString(R.string.title_section5), getResources().getDrawable(R.drawable.alert)));

        return items;
    }

    /**
     * Users of this fragment must call this method to set up the navigation drawer interactions.
     *
     * @param fragmentId   The android:id of this fragment in its activity's layout.
     * @param drawerLayout The DrawerLayout containing this fragment's UI.
     * @param toolbar      The Toolbar of the activity.
     */
    public void setup(int fragmentId, DrawerLayout drawerLayout, Toolbar toolbar)
    {
        mFragmentContainerView = getActivity().findViewById(fragmentId);
        mDrawerLayout = drawerLayout;

        mDrawerLayout.setStatusBarBackgroundColor(getResources().getColor(R.color.myPrimaryDarkColor));

        mActionBarDrawerToggle = new ActionBarDrawerToggle(getActivity(), mDrawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close)
        {
            @Override
            public void onDrawerClosed(View drawerView)
            {
                super.onDrawerClosed(drawerView);
                if (!isAdded()) return;

                getActivity().invalidateOptionsMenu(); // calls onPrepareOptionsMenu()

                mDrawerStatusListener.OnDrawerStatusChanged(false);
            }

            @Override
            public void onDrawerOpened(View drawerView)
            {
                super.onDrawerOpened(drawerView);
                if (!isAdded()) return;
                if (!mUserLearnedDrawer)
                {
                    mUserLearnedDrawer = true;
                    SharedPreferences sp = PreferenceManager
                            .getDefaultSharedPreferences(getActivity());
                    sp.edit().putBoolean(PREF_USER_LEARNED_DRAWER, true).apply();
                }
                getActivity().invalidateOptionsMenu(); // calls onPrepareOptionsMenu()
                mDrawerStatusListener.OnDrawerStatusChanged(true);
            }
        };

        // If the user hasn't 'learned' about the drawer, open it to introduce them to the drawer,
        // per the navigation drawer design guidelines.
        if (!mUserLearnedDrawer && !mFromSavedInstanceState)
        {
            mDrawerLayout.openDrawer(mFragmentContainerView);
        }

        // Defer code dependent on restoration of previous instance state.
        mDrawerLayout.post(new Runnable()
        {
            @Override
            public void run()
            {
                mActionBarDrawerToggle.syncState();
            }
        });

        mDrawerLayout.setDrawerListener(mActionBarDrawerToggle);
    }

    public void SetupOfflineMode()
    {
        mEmailView.setClickable(true);
        mEmailView.setText(R.string.now_to_login);
        mEmailView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent(getActivity(), StartActivity.class);
                getActivity().startActivity(intent);
            }
        });
    }

    public int getCurrentSelectedPosition()
    {
        return mCurrentSelectedPosition;
    }

    public void selectItem(int position)
    {
        mCurrentSelectedPosition = position;

        if (mCallbacks != null)
        {
            mCallbacks.onNavigationDrawerItemSelected(position);
        }

        ((NavigationDrawerAdapter) mDrawerRecyclerView.getAdapter()).selectPosition(position);

        if (mDrawerLayout != null)
        {
            mDrawerLayout.closeDrawer(mFragmentContainerView);
        }
    }

    public void selectOtherItem(int position)
    {
        if(mOtherCallbacks!=null)
        {
            mOtherCallbacks.OnSelectedOther(position);
        }
        if (mDrawerLayout != null)
        {
            mDrawerLayout.closeDrawer(mFragmentContainerView);
        }
    }


    public void openDrawer()
    {
        mDrawerLayout.openDrawer(mFragmentContainerView);

    }

    public void closeDrawer()
    {
        mDrawerLayout.closeDrawer(mFragmentContainerView);
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        try
        {
            mCallbacks = (NavigationDrawerCallbacks) activity;
            mOtherCallbacks=(NavigationDrawerOtherCallbacks)activity;
            mDrawerStatusListener=(DrawerStatusListener)activity;
        }
        catch (ClassCastException e)
        {
            throw new ClassCastException("Activity must implement NavigationDrawerCallbacks.");
        }
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        mCallbacks = null;
        mOtherCallbacks=null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SELECTED_POSITION, mCurrentSelectedPosition);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
        // Forward the new configuration the drawer toggle component.
        mActionBarDrawerToggle.onConfigurationChanged(newConfig);
    }



    public interface DrawerStatusListener
    {
        void OnDrawerStatusChanged(boolean isOpen);
    }




}
