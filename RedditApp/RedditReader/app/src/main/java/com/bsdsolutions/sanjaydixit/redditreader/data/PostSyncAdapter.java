package com.bsdsolutions.sanjaydixit.redditreader.data;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.bsdsolutions.sanjaydixit.redditreader.PostListActivity;
import com.bsdsolutions.sanjaydixit.redditreader.R;
import com.bsdsolutions.sanjaydixit.redditreader.util.JRAWUtils;
import com.bsdsolutions.sanjaydixit.redditreader.util.Utils;

import net.dean.jraw.RedditClient;
import net.dean.jraw.auth.AuthenticationManager;
import net.dean.jraw.auth.AuthenticationState;
import net.dean.jraw.models.Submission;
import net.dean.jraw.paginators.SubredditPaginator;

import java.util.HashSet;
import java.util.List;

/**
 * Created by sanjaydixit on 23/01/17.
 */

public class PostSyncAdapter extends AbstractThreadedSyncAdapter {

    private ContentResolver mContentResolver;
    public static final String ACCOUNT_TYPE = "redditreader.sanjaydixit.bsdsolutions.com";

    private static final String ARG_SYNC_PENDING = "SYNC_PENDING";

    public static final int SYNC_INTERVAL = 60 * 60; // 1 hour
    public static final int SYNC_FLEXTIME = 60 * 10; // 10 minutes

    public PostSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContentResolver = context.getContentResolver();
    }

    public PostSyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context,autoInitialize,allowParallelSyncs);
        mContentResolver = context.getContentResolver();
    }

    public static void init(Context ctx, String authority, int syncInterval, int syncFlexTime) {
        Account account = getSyncAccount(ctx, authority);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // enable inexact timers
            SyncRequest request = new SyncRequest.Builder()
                    .syncPeriodic(syncInterval, syncFlexTime)
                    .setSyncAdapter(account, authority)
                    .setExtras(Bundle.EMPTY)
                    .build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account, authority, Bundle.EMPTY, syncInterval);
        }

        ContentResolver.setSyncAutomatically(account, authority, true);
    }

    public static void syncNow(Context ctx, String authority, Bundle extras) {
        if (extras == null) {
            extras = new Bundle();
        }
        extras.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        extras.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(ctx, authority), authority, extras);
    }

    static Account getSyncAccount(Context ctx, String authority) {
        AccountManager accountManager = (AccountManager) ctx.getSystemService(Context.ACCOUNT_SERVICE);

        Account newAccount = new Account(ctx.getString(R.string.app_name), ACCOUNT_TYPE);

        // If the password doesn't exist, the account doesn't exist
        if (accountManager.getPassword(newAccount) == null) {

            // create new account
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }

            syncNow(ctx, authority, null);
        }

        return newAccount;
    }

    @Override
    public void onPerformSync(Account account, Bundle bundle, String s, ContentProviderClient contentProviderClient, SyncResult syncResult) {
        Log.d(PostListActivity.TAG, "Starting sync");

        AuthenticationManager authMngr = AuthenticationManager.get();

        if (authMngr.checkAuthState() == AuthenticationState.NEED_REFRESH) {
            // refresh access token
            try {
                Log.d(PostListActivity.TAG, "Refreshing access token");
                authMngr.refreshAccessToken(JRAWUtils.APP_CREDENTIALS);
            } catch (Exception e) {
                return;
            }
        }

        HashSet<String> selectedSubredditSet = new HashSet<>();
        selectedSubredditSet.addAll(Utils.getSubscribedRedditSet(getContext()));
        if(selectedSubredditSet.size() > 0) {
            for (String subreddit : selectedSubredditSet) {
                getPosts(subreddit);
            }
        } else {
            getPosts(null);
        }

        Intent dataUpdatedIntent = new Intent(Utils.UPDATE_APP_WIDGET).setPackage(getContext().getPackageName());
        getContext().sendBroadcast(dataUpdatedIntent);

        Log.d(PostListActivity.TAG, "Sync finished");
    }

    private void getPosts(String subredditName) {
        Log.v(PostListActivity.TAG, "Getting posts for subreddit: " + subredditName);
        Context ctx = getContext();
        RedditClient redditClient = AuthenticationManager.get().getRedditClient();

        ContentResolver cr = ctx.getContentResolver();

        SubredditPaginator paginator = null;

        if(subredditName == null || subredditName.isEmpty())
            paginator = new SubredditPaginator(redditClient);
        else {
            paginator = new SubredditPaginator(redditClient, subredditName);
        }

        List<Submission> submissions = null;
        try {
            submissions = paginator.next();
        } catch (Exception e) {
            Log.w(PostListActivity.TAG, "Exception : ", e);
        }

        if (submissions == null) {
            return;
        }

        Log.v(PostListActivity.TAG, "Got " + submissions.size() + " posts!");

        for (Submission submission : submissions) {
            if ( submission == null || submission.isNsfw() || submission.getTitle().toLowerCase().contains("nsfw")) {
                continue;
            }

            ContentValues values = new ContentValues();
            values.put(SinglePostContract.PostTableEntry.COLUMN_NAME_ID, String.valueOf(submission.getId()));
            Log.d(PostListActivity.TAG,"Post ID: " + submission.getId());
            values.put(SinglePostContract.PostTableEntry.COLUMN_NAME_TITLE, String.valueOf(submission.getTitle()));
            values.put(SinglePostContract.PostTableEntry.COLUMN_NAME_COMMENTS, String.valueOf(submission.getCommentCount()));
            values.put(SinglePostContract.PostTableEntry.COLUMN_NAME_VOTECOUNT, String.valueOf(submission.getScore()));
            values.put(SinglePostContract.PostTableEntry.COLUMN_NAME_IMAGE_LINK, String.valueOf(submission.getThumbnail()));
            values.put(SinglePostContract.PostTableEntry.COLUMN_NAME_SUBREDDIT_NAME, String.valueOf(submission.getSubredditName()));
            try {
                cr.insert(SinglePostContract.POST_TABLE_PATH, values);
            } catch (Exception e) {
                Log.e(PostListActivity.TAG, "Exception :", e);
            }
        }


    }
}
