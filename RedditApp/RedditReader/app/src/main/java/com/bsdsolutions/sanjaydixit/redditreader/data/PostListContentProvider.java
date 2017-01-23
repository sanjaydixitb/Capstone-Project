package com.bsdsolutions.sanjaydixit.redditreader.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

import static android.R.attr.value;
import static com.bsdsolutions.sanjaydixit.redditreader.data.PostReaderDbHelper.DATABASE_NAME;
import static com.bsdsolutions.sanjaydixit.redditreader.data.SinglePostContract.CONTENT_AUTHORITY;
import static com.bsdsolutions.sanjaydixit.redditreader.data.SinglePostContract.PATH_POSTS;

/**
 * Created by sanjaydixit on 23/01/17.
 */

public class PostListContentProvider extends ContentProvider {

    private PostReaderDbHelper mDbHelper;
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private SQLiteDatabase db;

    static {
        /*
         * The calls to addURI() go here, for all of the content URI patterns that the provider
         * should recognize. For this snippet, only the calls for table 3 are shown.
         */

        /*
         * Sets the integer value for multiple rows in table 3 to 1. Notice that no wildcard is used
         * in the path
         */
        sUriMatcher.addURI(CONTENT_AUTHORITY, PATH_POSTS, 1);

    }

    @Override
    public boolean onCreate() {
        mDbHelper = new PostReaderDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] strings, String s, String[] strings1, String s1) {
        db = mDbHelper.getWritableDatabase();
        switch(sUriMatcher.match(uri)) {
            case 1:
                return db.query(DATABASE_NAME,strings,s,strings1,null,null,s1);
        }
        return null;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        Uri result;
        long _id;
        db = mDbHelper.getWritableDatabase();

        _id = db.insert(tableName, null, value);
        if ( _id > 0 ) {
            result = SinglePostContract.buildUri(tableName, _id);
        } else {
            throw new SQLException("Failed to insert row into " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return result;
    }

    @Override
    public int delete(Uri uri, String s, String[] strings) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
        return 0;
    }
}
