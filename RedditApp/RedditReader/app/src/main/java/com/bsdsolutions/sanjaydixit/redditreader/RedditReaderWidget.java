package com.bsdsolutions.sanjaydixit.redditreader;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.bsdsolutions.sanjaydixit.redditreader.util.Utils;

/**
 * Implementation of App Widget functionality.
 */
public class RedditReaderWidget extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        context.startService(new Intent(context, RedditReaderWidgetRemoteViewsService.class));
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (Utils.UPDATE_APP_WIDGET.equals(intent.getAction())) {
            context.startService(new Intent(context, RedditReaderWidgetRemoteViewsService.class));
        }
    }

}

