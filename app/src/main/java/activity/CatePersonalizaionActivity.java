package activity;

import android.app.Activity;
import android.graphics.Canvas;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;

import com.chad.library.adapter.base.callback.ItemDragAndSwipeCallback;
import com.chad.library.adapter.base.listener.OnItemDragListener;
import com.chad.library.adapter.base.listener.OnItemSwipeListener;
import com.juniperphoton.myerlistandroid.R;

import java.util.ArrayList;

import adapter.CateListAdapter;
import model.ToDoCategory;
import moe.feng.material.statusbar.StatusBarCompat;
import util.GlobalListLocator;

/**
 * Created by JuniperPhoton on 2016-07-17.
 */
public class CatePersonalizaionActivity extends AppCompatActivity {

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
        mRecyclerView.setLayoutManager(new GridLayoutManager(this,1));

        mAdapter = new CateListAdapter(GlobalListLocator.makeCategoryListForPersonalizaion());
        mItemDragAndSwipeCallback = new ItemDragAndSwipeCallback(mAdapter);
        mItemTouchHelper = new ItemTouchHelper(mItemDragAndSwipeCallback);
        mItemTouchHelper.attachToRecyclerView(mRecyclerView);

        mAdapter.enableDragItem(mItemTouchHelper);
        mAdapter.setToggleViewId(R.id.row_cate_per_hamView);

        mRecyclerView.setAdapter(mAdapter);
    }

    public RecyclerView.Adapter getAdatper() {
        return mAdapter;
    }
}
