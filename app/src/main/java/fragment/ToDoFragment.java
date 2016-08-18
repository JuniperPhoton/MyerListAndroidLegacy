package fragment;

import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.google.gson.reflect.TypeToken;
import com.juniperphoton.jputils.LocalSettingHelper;
import com.juniperphoton.jputils.SerializerHelper;
import com.juniperphoton.myerlistandroid.R;
import com.orhanobut.logger.Logger;

import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;

import activity.MainActivity;
import api.CloudServices;
import interfaces.IRefresh;
import interfaces.IRequestCallback;
import listener.ToDoItemTouchListener;
import util.AppUtil;
import util.ConfigHelper;
import common.AppExtension;
import adapter.ToDoListAdapter;
import util.GlobalListLocator;
import model.ToDo;
import util.SerializationName;
import view.CircleView;

public class ToDoFragment extends Fragment implements IRefresh {
    private static String TAG = ToDoFragment.class.getName();

    private MainActivity mActivity;
    private RecyclerView mToDoRecyclerView;
    private SwipeRefreshLayout mRefreshLayout;
    private FloatingActionButton mAddingFab;

    private LinearLayout mNoItemLayout;

    private boolean mTurnGreen = false;
    private boolean mTurnRed = false;

    private View mCurrentMovingView;

    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
        Log.d(ToDoFragment.class.getName(), "onAttach");
        try {
            if (activity instanceof MainActivity) {
                mActivity = (MainActivity) activity;
            }
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(ToDoFragment.class.getName(), "onCreate");
        Logger.init(TAG);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_to_do_list, container, false);

        Log.d(ToDoFragment.class.getName(), "onCreateView");

        mNoItemLayout = (LinearLayout) view.findViewById(R.id.fragment_todo_no_item_ll);
        mNoItemLayout.setVisibility(View.GONE);

        //设置下拉刷新控件
        mRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.fragment_todo_refresh_srl);
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (ConfigHelper.ISOFFLINEMODE) {
                    stopRefreshing();
                    return;
                }
                getAllSchedules();
            }
        });

        //设置 FAB
        mAddingFab = (FloatingActionButton) view.findViewById(R.id.fragment_todo_add_fab);
        mAddingFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                StringBuffer sb = new StringBuffer();
                for (ToDo todo : ((ToDoListAdapter) mToDoRecyclerView.getAdapter()).getData()) {
                    sb.append(todo.getContent()).append(",");
                }
                Logger.d(sb.toString());

                mActivity.showAddingPane(null);
            }
        });

        if (!LocalSettingHelper.getBoolean(AppExtension.getInstance(), "HandHobbit")) {
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
            layoutParams.setMargins(16, 0, 0, 16);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            mAddingFab.setLayoutParams(layoutParams);
        }

        onInit();

        initRV(view);

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(ToDoFragment.class.getName(), "onPause");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(ToDoFragment.class.getName(), "onDetach");
    }

    public void onInit() {
        try {
            Type type = new TypeToken<ArrayList<ToDo>>() {
            }.getType();
            ArrayList<ToDo> list = SerializerHelper.deSerializeFromFile(
                    type, AppExtension.getInstance(), SerializationName.TODOS_FILE_NAME);

            if (list != null) {
                GlobalListLocator.TodosList = list;
            }
            //已经登陆了
            if (!ConfigHelper.ISOFFLINEMODE) {
                updateData(GlobalListLocator.TodosList);
            }
            //离线模式
            else {
                updateData(GlobalListLocator.TodosList);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateNoItemUI() {
        if ((mToDoRecyclerView.getAdapter()) != null) {
            if (((ToDoListAdapter) mToDoRecyclerView.getAdapter()).getData().size() == 0) {
                mNoItemLayout.setVisibility(View.VISIBLE);
            } else {
                mNoItemLayout.setVisibility(View.GONE);
            }
        } else {
            mNoItemLayout.setVisibility(View.GONE);
        }
    }

    private void initRV(View view) {
        mToDoRecyclerView = (RecyclerView) view.findViewById(R.id.fragment_todo_rv);
        mToDoRecyclerView.setLayoutManager(new LinearLayoutManager(mActivity, LinearLayoutManager.VERTICAL, false));

        ToDoListAdapter adapter = new ToDoListAdapter(GlobalListLocator.TodosList, mActivity, this);
        mToDoRecyclerView.swapAdapter(adapter, true);
        updateNoItemUI();

        mToDoRecyclerView.addOnItemTouchListener(new ToDoItemTouchListener(mActivity, new ToDoItemTouchListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                int[] location = new int[2];
                ToDo toDoItem = getData().get(position);
                CircleView circleView = (CircleView) view.findViewById(R.id.cateCircle);
                circleView.getLocationOnScreen(location);
                mActivity.setupAddingPaneForModifyAndShow(toDoItem,
                        new int[]{location[0] + circleView.getWidth() / 2, location[1] + circleView.getHeight() / 2});
            }

            @Override
            public void onItemLongClick(View view, int position) {

            }

            @Override
            public void onMovingItem(View view, int position, float dx, float dy) {
                if (mCurrentMovingView == null) {
                    mCurrentMovingView = view;
                }

                mCurrentMovingView.scrollTo(-(int) dx, 0);

                if (mCurrentMovingView.getScrollX() < -150 && !mTurnGreen) {
                    playColorChangeAnimation((ImageView) mCurrentMovingView.findViewById(R.id.greenImageView), true);
                } else if (mCurrentMovingView.getScrollX() > 150 && !mTurnRed) {
                    playColorChangeAnimation((ImageView) mCurrentMovingView.findViewById(R.id.redImageView), false);
                }
            }

            @Override
            public void onMoveCompleted(View view, int position) {

                ToDo toDoItem = getData().get(position);

                if (mTurnGreen) {
                    playFadebackAnimation((ImageView) mCurrentMovingView.findViewById(R.id.greenImageView), true);
                } else if (mTurnRed) {
                    playFadebackAnimation((ImageView) mCurrentMovingView.findViewById(R.id.redImageView), false);
                }

                playGoBackAnimation(mCurrentMovingView, mCurrentMovingView.getScrollX());

                //Finish
                if (mCurrentMovingView.getScrollX() < -150) {

                    ImageView lineview = (ImageView) mCurrentMovingView.findViewById(R.id.lineView);
                    if (toDoItem.getIsDone()) {
                        lineview.setVisibility(View.GONE);
                        toDoItem.setIsDone(false);
                    } else {
                        lineview.setVisibility(View.VISIBLE);
                        toDoItem.setIsDone(true);
                    }

                    if (!ConfigHelper.ISOFFLINEMODE) {
                        CloudServices.setDone(LocalSettingHelper.getString(AppExtension.getInstance(), "sid"),
                                LocalSettingHelper.getString(AppExtension.getInstance(), "access_token"), toDoItem.getID(),
                                toDoItem.getIsDone() ? "1" : "0",
                                new IRequestCallback() {
                                    @Override
                                    public void onResponse(JSONObject jsonObject) {
                                        //mActivity.onSetDone(jsonObject);
                                    }
                                });
                    }
                }
                //Delete
                else if (mCurrentMovingView.getScrollX() > 150) {
                    getAdatper().deleteToDo(toDoItem.getID());
                }
                mCurrentMovingView = null;
                SerializerHelper.serializeToFile(AppExtension.getInstance(), getData(), SerializationName.TODOS_FILE_NAME);
            }
        }));
    }

    private void playGoBackAnimation(final View v, final float left) {
        ValueAnimator valueAnimator = ValueAnimator.ofInt((int) left, 0);
        valueAnimator.setDuration(700);
        valueAnimator.setInterpolator(new DecelerateInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                v.scrollTo((int) valueAnimator.getAnimatedValue(), 0);
                if (Math.abs((int) valueAnimator.getAnimatedValue()) < 10) {
                    enableRefresh();
                }
            }
        });
        valueAnimator.start();
    }

    private void playColorChangeAnimation(final ImageView v, boolean isGreen) {
        v.setAlpha(1f);
        AnimationSet animationSet = new AnimationSet(false);

        AlphaAnimation alphaAnimation = new AlphaAnimation(0.1f, 1.0f);
        alphaAnimation.setDuration(700);
        alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                v.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        animationSet.addAnimation(alphaAnimation);
        v.startAnimation(animationSet);

        if (isGreen)
            mTurnGreen = true;
        else
            mTurnRed = true;
    }

    private void playFadebackAnimation(final ImageView v, final boolean isGreen) {
        AnimationSet animationSet = new AnimationSet(false);
        AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, 0.0f);
        alphaAnimation.setDuration(700);
        alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                v.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                v.setVisibility(View.INVISIBLE);
                if (isGreen)
                    mTurnGreen = false;
                else
                    mTurnRed = false;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        animationSet.addAnimation(alphaAnimation);
        v.startAnimation(animationSet);
    }

    public void updateData(ArrayList<ToDo> data) {
        if (mToDoRecyclerView != null) {
            mToDoRecyclerView.setAdapter(new ToDoListAdapter(data, mActivity, this));

            stopRefreshing();
            updateNoItemUI();
        }
    }

    public void showRefreshing() {
        if (mRefreshLayout != null) {
            mRefreshLayout.setRefreshing(true);
        }
    }

    public void stopRefreshing() {
        if (mRefreshLayout != null) {
            mRefreshLayout.setRefreshing(false);
        }
    }

    public void enableRefresh() {
        if (mRefreshLayout != null) {
            mRefreshLayout.setEnabled(true);
        }
    }

    public void disableRefresh() {
        if (mRefreshLayout != null) {
            mRefreshLayout.setEnabled(false);
        }
    }

    public int getFABRadius() {
        return mAddingFab.getWidth() / 2;
    }

    public int[] getFABPostion() {
        int[] position = new int[2];
        mAddingFab.getLocationOnScreen(position);
        return position;
    }

    public void getAllSchedules() {
        Logger.d(mActivity);
        showRefreshing();
        mActivity.syncCateAndList();

        if (!ConfigHelper.ISOFFLINEMODE && AppUtil.isNetworkAvailable(AppExtension.getInstance())) {
            if (GlobalListLocator.StagedList == null) return;
            mActivity.setIsAddStagedItems(true);
            for (ToDo todo : GlobalListLocator.StagedList) {
                CloudServices.addToDo(ConfigHelper.getSid(), ConfigHelper.getAccessToken(),
                        todo.getContent(), "0", todo.getCate(),
                        new IRequestCallback() {
                            @Override
                            public void onResponse(JSONObject jsonObject) {
                                if (jsonObject != null) Logger.d(jsonObject);
                                stopRefreshing();
                                mActivity.onAddedResponse(jsonObject);
                            }
                        });
            }
            GlobalListLocator.StagedList.clear();
            SerializerHelper.serializeToFile(AppExtension.getInstance(), GlobalListLocator.StagedList, SerializationName.STAGED_FILE_NAME);
        }
    }

    public ArrayList<ToDo> getData() {
        if ((mToDoRecyclerView.getAdapter()) != null) {
            return ((ToDoListAdapter) mToDoRecyclerView.getAdapter()).getData();
        } else {
            return null;
        }
    }

    private ToDoListAdapter getAdatper() {
        return ((ToDoListAdapter) mToDoRecyclerView.getAdapter());
    }

    public void updateContent(ToDo toDo) {
        if ((mToDoRecyclerView.getAdapter()) != null) {
            ((ToDoListAdapter) mToDoRecyclerView.getAdapter()).updateToDo(toDo);
        }
    }

    public void addToDo(ToDo todo) {
        if ((mToDoRecyclerView.getAdapter()) != null) {
            ((ToDoListAdapter) mToDoRecyclerView.getAdapter()).addToDo(todo);
        }
    }
}
