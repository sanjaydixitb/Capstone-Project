package com.bsdsolutions.sanjaydixit.redditreader.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

import static com.bsdsolutions.sanjaydixit.redditreader.data.SinglePostContract.PostTableEntry.TABLE_NAME;

/**
 * Created by sanjaydixit on 23/01/17.
 */

public final class SinglePostContract {

    private SinglePostContract() {};

    /* Inner class that defines the table contents */
    public static class PostTableEntry implements BaseColumns {
        public static final String TABLE_NAME = "PostList";
        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_COMMENTS = "comments";
        public static final String COLUMN_NAME_VOTECOUNT = "votes";
        public static final String COLUMN_NAME_IMAGE_LINK = "image";
        public static final String COLUMN_NAME_SUBREDDIT_NAME = "subreddit_name";
    }

    public static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    PostTableEntry._ID + " INTEGER PRIMARY KEY," +
                    PostTableEntry.COLUMN_NAME_ID + " TEXT," +
                    PostTableEntry.COLUMN_NAME_TITLE + " TEXT," +
                    PostTableEntry.COLUMN_NAME_COMMENTS + " INTEGER," +
                    PostTableEntry.COLUMN_NAME_VOTECOUNT + " INTEGER," +
                    PostTableEntry.COLUMN_NAME_IMAGE_LINK + " TEXT," +
                    PostTableEntry.COLUMN_NAME_SUBREDDIT_NAME + " TEXT" +
                    ")";

    public static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + TABLE_NAME;

    public static final String CONTENT_AUTHORITY = "com.bsdsolutions.sanjaydixit.redditreader";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_POSTS = "posts";

    public static final String CONTENT_DIR_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + TABLE_NAME;
    public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + TABLE_NAME;

    public static final Uri POST_TABLE_PATH = BASE_CONTENT_URI.buildUpon().appendPath(PATH_POSTS).build();

    public static Uri buildPostPathUri(long id) {
        return ContentUris.withAppendedId(POST_TABLE_PATH,id);
    }

}

