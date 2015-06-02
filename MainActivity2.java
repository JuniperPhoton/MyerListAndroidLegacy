package com.example.juniper.myerlistandroid;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;


public class MainActivity2 extends AppCompatActivity
{

    private String[] mPlanetTitles;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private String mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.InitialControl();
    }

    private void InitialControl()
    {


        mPlanetTitles = getResources().getStringArray(R.array.planets_array);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        ArrayList<HashMap<String,Object>> mapList=new ArrayList<HashMap<String,Object>>();

        HashMap<String,Object> map=new HashMap<>();
        map.put("content_icon",R.drawable.delete);
        map.put("content_text",getString(R.string.deleteditems));
        mapList.add(map);

        HashMap<String,Object> map2=new HashMap<>();
        map2.put("content_icon",R.drawable.settings);
        map2.put("content_text",getString(R.string.settings));
        mapList.add(map2);

        HashMap<String,Object> map3=new HashMap<>();
        map3.put("content_icon",R.drawable.like);
        map3.put("content_text",getString(R.string.about));
        mapList.add(map3);

        SimpleAdapter simpleAdapter=new SimpleAdapter(
                this,
                mapList,R.layout.drawer_list_item,
                new String[]{"content_icon","content_text"},
                new int[]{R.id.content_icon,R.id.content_text});

        mDrawerList.setAdapter(simpleAdapter);
        // Set the adapter for the list view
//        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
//                R.layout.drawer_list_item, mPlanetTitles));
        // Set the list's click listener
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener
    {
        @Override
            public void onItemClick(AdapterView parent, View view, int position, long id) {
                selectItem(position);
        }
    }

    /** Swaps fragments in the main content view */
    private void selectItem(int position) {
        // Create a new fragment and specify the planet to show based on position
//         Fragment fragment = new PlanetFragment();
//        Bundle args = new Bundle();
//        args.putInt(PlanetFragment.ARG_PLANET_NUMBER, position);
//        fragment.setArguments(args);
//
//        // Insert the fragment by replacing any existing fragment
//        FragmentManager fragmentManager = getFragmentManager();
//        fragmentManager.beginTransaction()
//                .replace(R.id.content_frame, fragment)
//                .commit();

        // Highlight the selected item, update the title, and close the drawer
//        mDrawerList.setItemChecked(position, true);
//        setTitle(mPlanetTitles[position]);
//        mDrawerLayout.closeDrawer(mDrawerList);
    }
//
//    @Override
//    public void setTitle(CharSequence title) {
//        mTitle = (String)title;
//        getActionBar().setTitle(mTitle);
//    }

}
