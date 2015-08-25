package widget;

import android.app.Application;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.example.juniper.myerlistandroid.R;

import java.util.ArrayList;
import java.util.List;

import activity.MainActivity;
import helper.ContextUtil;
import service.ListWidgetService;


/**
 * Implementation of App Widget functionality.
 */
public class ListWidgetProvider extends AppWidgetProvider
{
    private static String REFRESH_ACTION="MYERLIST.APPWIDGET_UPDATE";
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
    {
        // There may be multiple widgets active, so update all of them
        final int N = appWidgetIds.length;
        for (int i = 0; i < N; i++)
        {
            updateAppWidget(context, appWidgetManager, appWidgetIds[i]);
        }
    }

    @Override
     public void onReceive(Context context, Intent intent)
     {
        if(intent.getAction().equals(REFRESH_ACTION))
        {
            AppWidgetManager am = AppWidgetManager.getInstance(context);
            int[] appWidgetIds=am.getAppWidgetIds(new ComponentName(context,getClass()));
            final int N = appWidgetIds.length;
            for (int i = 0; i < N; i++)
            {
                updateAppWidget(context, am, appWidgetIds[i]);
            }
        }
        else
        {
            super.onReceive(context, intent);
        }
     }


    @Override
    public void onEnabled(Context context)
    {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context)
    {
        // Enter relevant functionality for when the last widget is disabled
    }

    public static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId)
    {
        Intent launchIntent=new Intent(context, MainActivity.class);
        PendingIntent pendingLaunchIntent=PendingIntent.getActivity(context,0,launchIntent,0);

        Intent serviceIntent=new Intent(context, ListWidgetService.class);

        Intent refreshIntent=new Intent();
        refreshIntent.setAction(REFRESH_ACTION);
        PendingIntent pendingRefreshIntent=PendingIntent.getBroadcast(context, 0, refreshIntent, 0);

        ListWidgetService.ListViewsFactory factory=ContextUtil.globalListViewFactory;

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.list_widget);
        views.setOnClickPendingIntent(R.id.widget_root_ll, pendingLaunchIntent);
        views.setRemoteAdapter(R.id.widget_lv, serviceIntent);
        views.setOnClickPendingIntent(R.id.widget_refresh_btn, pendingRefreshIntent);
        factory.updateData(context);
        views.setTextViewText(R.id.widget_undo_count_tv,context.getResources().getString(R.string.UndoHint)+String.valueOf(factory.data.size()));

        appWidgetManager.updateAppWidget(appWidgetId, views);
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widget_lv);

    }
}

