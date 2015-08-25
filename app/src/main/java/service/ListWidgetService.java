package service;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import android.widget.Toast;

import com.example.juniper.myerlistandroid.R;

import org.apache.http.impl.client.DefaultHttpClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import helper.ConfigHelper;
import helper.ContextUtil;
import helper.GetScheduleAsyncTask;
import helper.PostHelper;
import helper.SerializerHelper;
import model.Schedule;

public class ListWidgetService extends RemoteViewsService
{
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent)
    {
        ListViewsFactory factory=new ListViewsFactory(getApplicationContext(),intent);
        ContextUtil.globalListViewFactory=factory;
         return factory;
    }


    public class  ListViewsFactory implements RemoteViewsService.RemoteViewsFactory,PostHelper.OnGetSchedulesCallback
    {
        private Context mContext;
        private int mAppWidgetId;

        private String TEXT_ITEM = "widget_todo_item";
        public ArrayList<Schedule> data =new ArrayList<>();

        public ListViewsFactory(Context context,Intent intent)
        {
            mContext = context;
            mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);

        }

        public void updateData(Context context)
        {
            try
            {
                AsyncTask<Void, Void, ArrayList<Schedule>> task=new GetScheduleAsyncTask(mContext).execute();
                ArrayList list=task.get();
                data=list;
            }
            catch (Exception e)
            {

            }
        }

        @Override
        public void onCreate()
        {
            updateData(mContext);
        }

        @Override
        public void onDataSetChanged()
        {
            updateData(mContext);
        }

        @Override
        public void onDestroy()
        {
            data.clear();
        }

        @Override
        public int getCount()
        {
            return data.size();
        }

        @Override
        public RemoteViews getViewAt(int i)
        {
            Schedule map=data.get(i);
            RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.widget_row_todo);
            rv.setTextViewText(R.id.widget_todo_tv, map.getContent());
            return rv;
        }

        @Override
        public RemoteViews getLoadingView()
        {
            return null;
        }

        @Override
        public int getViewTypeCount()
        {
            return 1;
        }

        @Override
        public long getItemId(int i)
        {
            return i;
        }

        @Override
        public boolean hasStableIds()
        {
            return true;
        }

        @Override
        public void OnGotScheduleResponse(ArrayList<Schedule> mytodosList)
        {
            data=mytodosList;
        }
    }
}
