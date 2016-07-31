package activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.os.Bundle;
import android.support.v4.view.LayoutInflaterFactory;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.chad.library.adapter.base.callback.ItemDragAndSwipeCallback;
import com.chad.library.adapter.base.listener.OnItemDragListener;
import com.chad.library.adapter.base.listener.OnItemSwipeListener;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.juniperphoton.myerlistandroid.R;

import org.json.JSONObject;

import java.util.ArrayList;

import adapter.CateListAdapter;
import api.CloudServices;
import interfaces.IRequestCallback;
import model.ToDoCategory;
import moe.feng.material.statusbar.StatusBarCompat;
import util.AppExtension;
import util.AppUtil;
import util.ConfigHelper;
import util.GlobalListLocator;

/**
 * Created by JuniperPhoton on 2016-07-17.
 */
public class CatePersonalizaionActivity extends AppCompatActivity {

    private static final String MODIFIED_CATE_JSON_STRING_FORE = "{ \"modified\":true, \"cates\":";

    private RecyclerView mRecyclerView;
    private CateListAdapter mAdapter;
    private ItemTouchHelper mItemTouchHelper;
    private ItemDragAndSwipeCallback mItemDragAndSwipeCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarCompat.setUpActivity(this);
        setContentView(R.layout.activity_cate_per);

        mRecyclerView = (RecyclerView) findViewById(R.id.activity_cate_per_rv);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 1));

        mAdapter = new CateListAdapter(GlobalListLocator.makeCategoryListForPersonalizaion());
        mItemDragAndSwipeCallback = new ItemDragAndSwipeCallback(mAdapter);
        mItemTouchHelper = new ItemTouchHelper(mItemDragAndSwipeCallback);
        mItemTouchHelper.attachToRecyclerView(mRecyclerView);

        mAdapter.enableDragItem(mItemTouchHelper);
        mAdapter.setToggleViewId(R.id.row_cate_per_hamView);

        mRecyclerView.setAdapter(mAdapter);

        FloatingActionButton cancelFAB = (FloatingActionButton) findViewById(R.id.activity_cate_per_cancelView);
        FloatingActionButton acceptFAB = (FloatingActionButton) findViewById(R.id.activity_cate_per_acceptView);
        cancelFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(CatePersonalizaionActivity.this);
                builder.setTitle(getString(R.string.logout_title));
                builder.setMessage(getString(R.string.a_cate_per_discard_content));
                builder.setPositiveButton(getString(R.string.ok_btn), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                });
                builder.setNegativeButton(getResources().getString(R.string.cancel_btn), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                builder.create().show();
            }
        });

        acceptFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveData();
            }
        });
    }

    public RecyclerView.Adapter getAdatper() {
        return mAdapter;
    }

    private void saveData() {
        JsonArray jsonArray = new JsonArray();
        for (ToDoCategory category : mAdapter.getData()) {
            if (category.getID() <= 0) continue;
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("name", category.getName());
            jsonObject.addProperty("color", category.getColor());
            jsonObject.addProperty("id", category.getID());
            jsonArray.add(jsonObject);
        }
        String arrayString = MODIFIED_CATE_JSON_STRING_FORE + jsonArray.toString() + "}";
        if (!ConfigHelper.ISOFFLINEMODE && AppUtil.isNetworkAvailable(this)) {
            ProgressDialog dialog = new ProgressDialog(this, ProgressDialog.STYLE_SPINNER);
            dialog.show();
            CloudServices.updateCates(ConfigHelper.getSid(), ConfigHelper.getAccessToken(), arrayString, new IRequestCallback() {
                @Override
                public void onResponse(JSONObject jsonObject) {
                    if (jsonObject != null) {
                        finish();
                    }
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(CatePersonalizaionActivity.this);
        builder.setTitle(getString(R.string.logout_title));
        builder.setMessage(getString(R.string.a_cate_per_discard_content));
        builder.setPositiveButton(getString(R.string.ok_btn), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                CatePersonalizaionActivity.super.onBackPressed();
            }
        });
        builder.setNegativeButton(getResources().getString(R.string.cancel_btn), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.create().show();
    }
}
