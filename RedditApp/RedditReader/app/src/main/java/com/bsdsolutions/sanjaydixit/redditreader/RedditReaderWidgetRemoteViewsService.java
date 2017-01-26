package com.bsdsolutions.sanjaydixit.redditreader;

import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Parcelable;
import android.view.View;
import android.widget.RemoteViews;

import com.bsdsolutions.sanjaydixit.redditreader.content.PostItemList;
import com.bsdsolutions.sanjaydixit.redditreader.data.SinglePostContract;
import com.bsdsolutions.sanjaydixit.redditreader.util.Utils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.bsdsolutions.sanjaydixit.redditreader.data.SinglePostContract.POST_TABLE_PATH;
import static com.bsdsolutions.sanjaydixit.redditreader.data.SinglePostContract.PostTableEntry.COLUMN_NAME_COMMENTS;
import static com.bsdsolutions.sanjaydixit.redditreader.data.SinglePostContract.PostTableEntry.COLUMN_NAME_ID;
import static com.bsdsolutions.sanjaydixit.redditreader.data.SinglePostContract.PostTableEntry.COLUMN_NAME_IMAGE_LINK;
import static com.bsdsolutions.sanjaydixit.redditreader.data.SinglePostContract.PostTableEntry.COLUMN_NAME_SUBREDDIT_NAME;
import static com.bsdsolutions.sanjaydixit.redditreader.data.SinglePostContract.PostTableEntry.COLUMN_NAME_TITLE;
import static com.bsdsolutions.sanjaydixit.redditreader.data.SinglePostContract.PostTableEntry.COLUMN_NAME_VOTECOUNT;
import static com.bsdsolutions.sanjaydixit.redditreader.util.Utils.getSubscribedRedditSet;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * helper methods.
 */
public class RedditReaderWidgetRemoteViewsService extends IntentService {

    public RedditReaderWidgetRemoteViewsService() {
        super("RedditReaderWidgetRemoteViewsService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        AppWidgetManager widgetsManager = AppWidgetManager.getInstance(this);

        int[] widgetIds = widgetsManager.getAppWidgetIds(new ComponentName(this, RedditReaderWidget.class));

        String message = null;

        // load list of random rows so we have different submission in each widget instance
        Cursor cursor = null;
        if (message == null) {
            String sort = "RANDOM() LIMIT " + widgetIds.length;
            String[] projection = {COLUMN_NAME_ID, COLUMN_NAME_TITLE, COLUMN_NAME_COMMENTS, COLUMN_NAME_VOTECOUNT, COLUMN_NAME_IMAGE_LINK, COLUMN_NAME_SUBREDDIT_NAME};
            Set<String> subredditSet = getSubscribedRedditSet(getApplicationContext());
            if(subredditSet == null || subredditSet.size() < 1) {
                message = getString(R.string.message_no_valid_submissions);
                cursor = getContentResolver().query(POST_TABLE_PATH, projection, null, null, sort);
            }
            else {
                int argcount = subredditSet.size(); // number of IN arguments
                StringBuilder inList = new StringBuilder(argcount * 2);
                for (int i = 0; i < argcount; i++) {
                    if (i > 0) {
                        inList.append(",");
                    }
                    inList.append("?");
                }
                cursor = getContentResolver().query(POST_TABLE_PATH, projection, COLUMN_NAME_SUBREDDIT_NAME + " IN (" + inList.toString() + ")", subredditSet.toArray(new String[subredditSet.size()]), sort);
            }
            if (cursor == null) {
                message = getString(R.string.message_internal_error);
            } else if (!cursor.moveToFirst()) {
                message = getString(R.string.message_no_valid_submissions);
            }
        }

        // go over the widgets and populate them with data (or message)
        for (int widgetId : widgetIds) {
            RemoteViews views = new RemoteViews(getPackageName(), R.layout.reddit_reader_widget);

            // prepare the application launching intent
            Intent i = new Intent(this, PostListActivity.class);

            if (message != null) {
                message = getString(R.string.app_name) + ": " + message;
                views.setTextViewText(R.id.appwidget_text, message);
                views.setViewVisibility(R.id.appwidget_post_image,View.GONE);
            } else {

                String id = cursor.getString(cursor.getColumnIndex(SinglePostContract.PostTableEntry.COLUMN_NAME_ID));
                String title = cursor.getString(cursor.getColumnIndex(SinglePostContract.PostTableEntry.COLUMN_NAME_TITLE));
                String image = cursor.getString(cursor.getColumnIndex(SinglePostContract.PostTableEntry.COLUMN_NAME_IMAGE_LINK));
                String subredditId = cursor.getString(cursor.getColumnIndex(SinglePostContract.PostTableEntry.COLUMN_NAME_SUBREDDIT_NAME));
                int commentCount = cursor.getInt(cursor.getColumnIndex(SinglePostContract.PostTableEntry.COLUMN_NAME_COMMENTS));
                int voteCount = cursor.getInt(cursor.getColumnIndex(SinglePostContract.PostTableEntry.COLUMN_NAME_VOTECOUNT));
                PostItemList.SinglePost post = new PostItemList.SinglePost(id,title,image,subredditId,commentCount,voteCount);
                List<PostItemList.SinglePost> postList = new ArrayList<>();
                postList.add(post);
                i.putParcelableArrayListExtra(Utils.INTENT_PARCELABLE_EXTRA_KEY, (ArrayList<? extends Parcelable>) postList);

                views.setTextViewText(R.id.appwidget_text, title);
                views.setViewVisibility(R.id.appwidget_text, View.VISIBLE);

                try {
                    if(image != null) {
                        Bitmap b = Picasso.with(getApplicationContext()).load(image).get();
                        views.setViewVisibility(R.id.appwidget_post_image,View.VISIBLE);
                        views.setImageViewBitmap(R.id.appwidget_post_image, b);
                    } else {
                        views.setViewVisibility(R.id.appwidget_post_image,View.GONE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (!cursor.isLast()) {
                    cursor.moveToNext();
                }
            }

            // set the application launching intent
            PendingIntent pi = PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
            views.setOnClickPendingIntent(R.id.appwidget_text, pi);

            widgetsManager.updateAppWidget(widgetId, views);
        }

        if (cursor != null) {
            cursor.close();
        }
    }

}
