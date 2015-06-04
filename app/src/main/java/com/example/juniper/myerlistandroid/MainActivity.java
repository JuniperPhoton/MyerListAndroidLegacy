package com.example.juniper.myerlistandroid;

import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import helper.ConfigHelper;
import helper.PostHelper;
import helper.PostHelper.OnCheckResponseListener;
import model.OnActionListener;


public class MainActivity extends ActionBarActivity implements NavigationDrawerCallbacks,ToDoFragment.OnFragmentInteractionListener, OnCheckResponseListener, OnActionListener
{

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;
    private Toolbar mToolbar;

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

        InitialFragment(savedInstanceState);
    }

    private void InitialFragment(Bundle savedInstanceState)
    {
        // Check that the activity is using the layout version with
        // the fragment_container FrameLayout
        if (findViewById(R.id.fragment_container) != null) {

            // However, if we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
            if (savedInstanceState != null) {
                return;
            }

            // Create a new Fragment to be placed in the activity layout
            ToDoFragment firstFragment = new ToDoFragment();

            // In case this activity was started with special instructions from an
            // Intent, pass the Intent's extras to the fragment as arguments
            //firstFragment.setArguments(getIntent().getExtras());

            // Add the fragment to the 'fragment_container' FrameLayout
            getFragmentManager().beginTransaction()
                    .add(R.id.fragment_container,firstFragment).commit();
        }

    }


    @Override
    public void onNavigationDrawerItemSelected(int position)
    {
        // update the main content by replacing fragments
        //Toast.makeText(this, "Menu item selected -> " + position, Toast.LENGTH_SHORT).show();
        switch (position)
        {
            case 0:
            {

            };break;
            case 1:break;
            case 2:break;
            case 3:break;
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


    @Override
    public void onBackPressed()
    {
        if (mNavigationDrawerFragment.isDrawerOpen())
            mNavigationDrawerFragment.closeDrawer();
        else
            super.onBackPressed();
    }

    @Override
    public void onFragmentInteraction(Uri uri)
    {

    }


    @Override
    public void OnCheckResponse(boolean check)
    {
        Toast.makeText(this,"haha",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void OnGotMessage(String msg)
    {
        Toast.makeText(this,msg,Toast.LENGTH_SHORT).show();
    }
}
