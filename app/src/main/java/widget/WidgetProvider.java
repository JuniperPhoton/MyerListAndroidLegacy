package widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.juniperphoton.myerlistandroid.R;

import activity.MainActivity;

/**
 * Created by chao on 8/14/2016.
 */
public class WidgetProvider extends AppWidgetProvider {
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        final int count = appWidgetIds.length;

        for (int i = 0; i < count; i++) {
            int widgetId = appWidgetIds[i];

            // Create an Intent to launch Activity
            Intent intent = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.new_app_widget);
            remoteViews.setOnClickPendingIntent(R.id.widget_ll,pendingIntent);

            //remoteViews.setRemoteAdapter(R.id.widget_recyclerView,);
            appWidgetManager.updateAppWidget(widgetId, remoteViews);
        }
    }
}
