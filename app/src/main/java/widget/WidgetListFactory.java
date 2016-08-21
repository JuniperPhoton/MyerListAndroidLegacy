package widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.juniperphoton.jputils.LocalSettingHelper;
import com.juniperphoton.myerlistandroid.R;
import com.orhanobut.logger.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import api.CloudServices;
import exception.APIException;
import interfaces.IRequestCallback;
import model.ToDo;
import util.AppConfig;
import util.GlobalListLocator;

/**
 * If you are familiar with Adapter of ListView,this is the same as adapter
 * with few changes
 */
public class WidgetListFactory implements RemoteViewsService.RemoteViewsFactory {

    private ArrayList<ToDo> mData;
    private Context mContext;
    private int appWidgetId;

    public WidgetListFactory(Context context, Intent intent) {
        this.mContext = context;
        appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
    }

    private void getToDos() {
        mData = new ArrayList<>();

        CloudServices.getLatestSchedules(AppConfig.getSid(), AppConfig.getAccessToken(), true, new IRequestCallback() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                if (jsonObject != null) Logger.json(jsonObject.toString());
                onGotLatestScheduleResponse(jsonObject);
            }
        });
    }

    private void onGotLatestScheduleResponse(JSONObject response) {
        try {
            if (response == null) throw new APIException();

            boolean isSuccess = response.getBoolean("isSuccessed");
            if (isSuccess) {
                JSONArray array = response.getJSONArray("ScheduleInfo");

                if (array != null) {
                    final ArrayList<ToDo> list = ToDo.parseJsonObjFromArray(array);
                    CloudServices.getListOrder(
                            LocalSettingHelper.getString(mContext, "sid"),
                            LocalSettingHelper.getString(mContext, "access_token"), true,
                            new IRequestCallback() {
                                @Override
                                public void onResponse(JSONObject jsonObject) {
                                    if (jsonObject != null) Logger.json(jsonObject.toString());
                                    onGotListOrder(jsonObject, list);
                                }
                            });
                }
            }
        } catch (APIException e) {
            e.printStackTrace();
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
                mData = ToDo.setOrderByString(originalList, orderStr);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (APIException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate() {
    }

    @Override
    public void onDataSetChanged() {
        getToDos();
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public RemoteViews getViewAt(int position) {

        ToDo item = mData.get(position);

        final RemoteViews remoteView = new RemoteViews(mContext.getPackageName(), R.layout.widget_row_todo);
        remoteView.setTextViewText(R.id.widget_todo_tv, item.getContent());
        remoteView.setViewVisibility(R.id.widget_todo_lineview, item.getIsDone() ? View.VISIBLE : View.GONE);

        remoteView.setInt(R.id.widget_cate_rl, "setBackgroundColor", GlobalListLocator.getCategoryByCateID(item.getCate()).getColor());

        return remoteView;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }
}