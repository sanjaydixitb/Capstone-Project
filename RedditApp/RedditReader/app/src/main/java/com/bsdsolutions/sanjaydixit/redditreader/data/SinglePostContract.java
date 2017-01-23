package com.bsdsolutions.sanjaydixit.redditreader.data;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by sanjaydixit on 23/01/17.
 */

public final class SinglePostContract {

    private SinglePostContract() {};

    /* Inner class that defines the table contents */
    public static class FeedEntry implements BaseColumns {
        public static final String TABLE_NAME = "PostList";
        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_COMMENTS = "comments";
        public static final String COLUMN_NAME_UPVOTES = "upvotes";
        public static final String COLUMN_NAME_DOWNVOTES = "downvotes";
        public static final String COLUMN_NAME_IMAGE_LINK = "image";
    }

    public static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + FeedEntry.TABLE_NAME + " (" +
                    FeedEntry._ID + " INTEGER PRIMARY KEY," +
                    FeedEntry.COLUMN_NAME_ID + " TEXT," +
                    FeedEntry.COLUMN_NAME_TITLE + " TEXT," +
                    FeedEntry.COLUMN_NAME_COMMENTS + " INTEGER," +
                    FeedEntry.COLUMN_NAME_UPVOTES + " INTEGER," +
                    FeedEntry.COLUMN_NAME_DOWNVOTES + " INTEGER," +
                    FeedEntry.COLUMN_NAME_IMAGE_LINK + " TEXT" +
                    ")";

    public static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + FeedEntry.TABLE_NAME;

    public static final String CONTENT_AUTHORITY = "com.bsdsolutions.sanjaydixit.redditreader";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_POSTS = "posts";

    public static Uri buildUri() {
        BASE_CONTENT_URI.buildUpon().
    }

}

