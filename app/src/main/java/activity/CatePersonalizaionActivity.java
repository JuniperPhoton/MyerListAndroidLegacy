package activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.chad.library.adapter.base.callback.ItemDragAndSwipeCallback;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.juniperphoton.jputils.ColorUtil;
import com.juniperphoton.myerlistandroid.R;

import org.json.JSONObject;

import java.util.ArrayList;

import adapter.PickColorAdapter;
import adapter.CateListAdapter;
import api.CloudServices;
import interfaces.IPickColorCallback;
import interfaces.IPickedColor;
import interfaces.IRequestCallback;
import model.ColorWrapper;
import model.ToDoCategory;
import moe.feng.material.statusbar.StatusBarCompat;
import util.AppUtil;
import util.ConfigHelper;
import util.GlobalListLocator;

/**
 * Created by JuniperPhoton on 2016-07-17.
 */
public class CatePersonalizaionActivity extends AppCompatActivity implements IPickColorCallback, IPickedColor {

    private static final String MODIFIED_CATE_JSON_STRING_FORE = "{ \"modified\":true, \"cates\":";

    private RecyclerView mCateRecyclerView;
    private RecyclerView mColorRecyclerView;
    private RelativeLayout mColorRootLayout = null;
    private CateListAdapter mAdapter;
    private ItemTouchHelper mItemTouchHelper;
    private ItemDragAndSwipeCallback mItemDragAndSwipeCallback;

    private ToDoCategory mToDoCategoryToModify;

    private PickColorAdapter mColorAdatper;
    private ArrayList<ColorWrapper> mColors;
    private View mColorsView;

    private AlertDialog mColorDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarCompat.setUpActivity(this);
        setContentView(R.layout.activity_cate_per);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        setupCateViews();
        setupFAB();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void setupCateViews() {
        mCateRecyclerView = (RecyclerView) findViewById(R.id.activity_cate_per_rv);
        mCateRecyclerView.setLayoutManager(new GridLayoutManager(this, 1));

        mAdapter = new CateListAdapter(GlobalListLocator.makeCategoryListForPersonalizaion(), this);
        mItemDragAndSwipeCallback = new ItemDragAndSwipeCallback(mAdapter);
        mItemTouchHelper = new ItemTouchHelper(mItemDragAndSwipeCallback);
        mItemTouchHelper.attachToRecyclerView(mCateRecyclerView);

        mAdapter.enableDragItem(mItemTouchHelper);
        mAdapter.setToggleViewId(R.id.row_cate_per_hamView);

        mCateRecyclerView.setAdapter(mAdapter);
    }

    private void setupColorViews(View view) {
        if (mColorRootLayout == null) {
            mColorRootLayout = (RelativeLayout) view.findViewById(R.id.dialog_cate_per_color_root_rl);
            mColorRecyclerView = (RecyclerView) view.findViewById(R.id.dialog_cate_per_color_rv);
            mColorRecyclerView.setLayoutManager(new GridLayoutManager(this, 7));

            mColors = generateColors();

            mColorAdatper = new PickColorAdapter(mColors, this);
            mColorRecyclerView.setAdapter(mColorAdatper);
        }
    }

    private ArrayList<ColorWrapper> generateColors() {
        ArrayList<ColorWrapper> list = new ArrayList<>();
        list.add(new ColorWrapper(Color.parseColor("#F75B44")));
        list.add(new ColorWrapper(Color.parseColor("#EC4128")));
        list.add(new ColorWrapper(Color.parseColor("#F73215")));
        list.add(new ColorWrapper(Color.parseColor("#F7445B")));
        list.add(new ColorWrapper(Color.parseColor("#E1184B")));
        list.add(new ColorWrapper(Color.parseColor("#C11943")));
        list.add(new ColorWrapper(Color.parseColor("#80224C")));
        list.add(new ColorWrapper(Color.parseColor("#66436F")));
        list.add(new ColorWrapper(Color.parseColor("#713A80")));
        list.add(new ColorWrapper(Color.parseColor("#5F3A80")));
        list.add(new ColorWrapper(Color.parseColor("#4D3A80")));
        list.add(new ColorWrapper(Color.parseColor("#352F44")));
        list.add(new ColorWrapper(Color.parseColor("#474E88")));
        list.add(new ColorWrapper(Color.parseColor("#2E3675")));
        list.add(new ColorWrapper(Color.parseColor("#2A2E51")));
        list.add(new ColorWrapper(Color.parseColor("#417C98")));
        list.add(new ColorWrapper(Color.parseColor("#FF6FD1FF")));
        list.add(new ColorWrapper(Color.parseColor("#FF3CBBF7")));
        list.add(new ColorWrapper(Color.parseColor("#FF217CDC")));
        list.add(new ColorWrapper(Color.parseColor("#FF4CAFFF")));
        list.add(new ColorWrapper(Color.parseColor("#FF5474C1")));
        list.add(new ColorWrapper(Color.parseColor("#317CA0")));
        list.add(new ColorWrapper(Color.parseColor("#39525F")));
        list.add(new ColorWrapper(Color.parseColor("#4F9595")));
        list.add(new ColorWrapper(Color.parseColor("#2C8D8D")));
        list.add(new ColorWrapper(Color.parseColor("#FF00BEBE")));
        list.add(new ColorWrapper(Color.parseColor("#257575")));
        list.add(new ColorWrapper(Color.parseColor("#2B8A78")));
        list.add(new ColorWrapper(Color.parseColor("#3FBEA6")));
        list.add(new ColorWrapper(Color.parseColor("#3FBE7D")));
        list.add(new ColorWrapper(Color.parseColor("#1C9B5A")));
        list.add(new ColorWrapper(Color.parseColor("#5A9849")));
        list.add(new ColorWrapper(Color.parseColor("#739849")));
        list.add(new ColorWrapper(Color.parseColor("#C9D639")));
        list.add(new ColorWrapper(Color.parseColor("#D6CD00")));
        list.add(new ColorWrapper(Color.parseColor("#F7C142")));
        list.add(new ColorWrapper(Color.parseColor("#FFF7D842")));
        list.add(new ColorWrapper(Color.parseColor("#F79E42")));
        list.add(new ColorWrapper(Color.parseColor("#FF8726")));
        list.add(new ColorWrapper(Color.parseColor("#FFEF7919")));
        return list;
    }

    private void setupFAB() {
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
            jsonObject.addProperty("color", ColorUtil.parseColorToString(category.getColor()));
            jsonObject.addProperty("id", category.getID());
            jsonArray.add(jsonObject);
        }
        String arrayString = MODIFIED_CATE_JSON_STRING_FORE + jsonArray.toString() + "}";
        if (!ConfigHelper.ISOFFLINEMODE && AppUtil.isNetworkAvailable(this)) {
            GlobalListLocator.makeAndUpdateCategoryList((ArrayList<ToDoCategory>) mAdapter.getNormalData());
            GlobalListLocator.onUpdateCateList = true;

            ProgressDialog dialog = new ProgressDialog(this, ProgressDialog.STYLE_SPINNER);
            dialog.setMessage(getResources().getString(R.string.loading_hint));
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

    @Override
    public void onPickColor(ToDoCategory category) {
        mToDoCategoryToModify = category;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if (mColorsView == null) {
            mColorsView = LayoutInflater.from(this).inflate(R.layout.dialog_color_picker, null);
            setupColorViews(mColorsView);
        }
        builder.setView(mColorsView);
        if (mColorDialog == null) {
            mColorDialog = builder.create();
        }
        mColorDialog.show();
    }

    @Override
    public void pickedColor(int color) {
        if (mToDoCategoryToModify != null) {
            mAdapter.updateItemColor(mToDoCategoryToModify.getID(), color);
            if (mColorDialog != null) {
                mColorDialog.dismiss();
            }
        }
    }
}
