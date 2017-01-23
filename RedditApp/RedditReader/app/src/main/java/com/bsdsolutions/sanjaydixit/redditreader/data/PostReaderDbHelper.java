package com.bsdsolutions.sanjaydixit.redditreader.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.bsdsolutions.sanjaydixit.redditreader.content.PostItemList;

import java.util.List;

import static com.bsdsolutions.sanjaydixit.redditreader.data.SinglePostContract.PostTableEntry.TABLE_NAME;
import static com.bsdsolutions.sanjaydixit.redditreader.data.SinglePostContract.SQL_CREATE_ENTRIES;
import static com.bsdsolutions.sanjaydixit.redditreader.data.SinglePostContract.SQL_DELETE_ENTRIES;
import static com.bsdsolutions.sanjaydixit.redditreader.data.SinglePostContract.PostTableEntry;

/**
 * Created by sanjaydixit on 23/01/17.
 */

public class PostReaderDbHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = TABLE_NAME;

    public PostReaderDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(SQL_CREATE_ENTRIES);
        //TODO: REMOVE!
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_NAME, null, null);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL(SQL_DELETE_ENTRIES);
        onCreate(sqLiteDatabase);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public void insertIntoDatabase(List<PostItemList.SinglePost> postList) {
        // Gets the data repository in write mode
        SQLiteDatabase db = getWritableDatabase();

        for(PostItemList.SinglePost post : postList) {

            String id = post.id;
            String title = post.title;
            int comments = post.commentCount;
            int upvotes = post.voteCount;
            String imageLink = post.image;


// Create a new map of values, where column names are the keys
            ContentValues values = new ContentValues();
            values.put(PostTableEntry.COLUMN_NAME_ID, id);
            values.put(PostTableEntry.COLUMN_NAME_TITLE, title);
            values.put(PostTableEntry.COLUMN_NAME_COMMENTS, comments);
            values.put(PostTableEntry.COLUMN_NAME_VOTECOUNT, upvotes);
            values.put(PostTableEntry.COLUMN_NAME_IMAGE_LINK, imageLink);

            String[] projection = {
                    PostTableEntry._ID,
                    PostTableEntry.COLUMN_NAME_ID,
            };

// Filter results WHERE "title" = 'My Title'
            String selection = PostTableEntry.COLUMN_NAME_ID + " = ?";
            String[] selectionArgs = {String.valueOf(id)};

// How you want the results sorted in the resulting Cursor
            String sortOrder =
                    PostTableEntry.COLUMN_NAME_ID + " DESC";

            Cursor result = getDataFromDatabase(projection, selection, selectionArgs, sortOrder);
            if (result != null && result.getCount() != 0) {
                selection = PostTableEntry.COLUMN_NAME_ID + " LIKE ?";

                //Delete the entry and make a new one so that new ID is newer?
                int count = db.delete(
                        PostTableEntry.TABLE_NAME,
                        selection,
                        selectionArgs);
            }
            if(result != null)
                result.close();

// Insert the new row, returning the primary key value of the new row
            long newRowId = db.insert(PostTableEntry.TABLE_NAME, null, values);

        }
    }

    public Cursor getDataFromDatabase(String projection[], String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(
                PostTableEntry.TABLE_NAME,  // The table to query
                projection,                               // The columns to return
                selection,                                // The columns for the WHERE clause
                selectionArgs,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );
        return cursor;
    }

}
