package service;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import java.util.ArrayList;

import interfaces.IRequestCallbacks;
import model.ToDo;
import util.AppExtension;
import util.PostHelper;


public class ListWidgetService extends RemoteViewsService
{
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent)
    {
        ListViewsFactory factory=new ListViewsFactory(getApplicationContext(),intent);
        AppExtension.globalListViewFactory=factory;
         return factory;
    }


    public class  ListViewsFactory implements RemoteViewsService.RemoteViewsFactory, IRequestCallbacks
    {
        private Context mContext;
        private int mAppWidgetId;

        private String TEXT_ITEM = "widget_todo_item";
        public ArrayList<ToDo> data =new ArrayList<>();

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
                AsyncTask<Void, Void, ArrayList<ToDo>> task=new GetScheduleAsyncTask(mContext).execute();
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
            ToDo map=data.get(i);
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
        public void OnCheckResponse(boolean check)
        {

        }

        @Override
        public void OnGetSaltResponse(String str)
        {

        }

        @Override
        public void OnLoginResponse(boolean value)
        {

        }

        @Override
        public void OnGotScheduleResponse(boolean value, ArrayList<ToDo> mytodosList)
        {
            data=mytodosList;
        }

        @Override
        public void OnAddedResponse(boolean isSuccess, ToDo newTodo)
        {

        }

        @Override
        public void OnSetOrderResponse(boolean isSuccess)
        {

        }

        @Override
        public void OnRegisteredResponse(boolean isSuccess, String salt)
        {

        }

        @Override
        public void OnDoneResponse(boolean isSuccess)
        {

        }

        @Override
        public void OnDeleteResponse(boolean isSuccess)
        {

        }

        @Override
        public void OnUpdateContent(boolean isSuccess)
        {

        }
    }
}
