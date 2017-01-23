package com.bsdsolutions.sanjaydixit.redditreader;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;

/**
 * Created by sanjaydixit on 23/01/17.
 */

public class PostSyncAdapter extends AbstractThreadedSyncAdapter {

    private Context mContext = null;

    public PostSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContext = context;
    }

    @Override
    public void onPerformSync(Account account, Bundle bundle, String s, ContentProviderClient contentProviderClient, SyncResult syncResult) {

    }
}
