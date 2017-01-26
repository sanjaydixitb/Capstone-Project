package com.bsdsolutions.sanjaydixit.redditreader;

import android.app.Application;

import com.bsdsolutions.sanjaydixit.redditreader.data.PostSyncAdapter;
import com.bsdsolutions.sanjaydixit.redditreader.data.SinglePostContract;
import com.bsdsolutions.sanjaydixit.redditreader.util.JRAWUtils;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

import net.dean.jraw.RedditClient;
import net.dean.jraw.android.AndroidRedditClient;
import net.dean.jraw.android.AndroidTokenStore;
import net.dean.jraw.auth.AuthenticationManager;
import net.dean.jraw.auth.RefreshTokenHandler;
import net.dean.jraw.http.LoggingMode;

import static com.bsdsolutions.sanjaydixit.redditreader.data.PostSyncAdapter.SYNC_FLEXTIME;
import static com.bsdsolutions.sanjaydixit.redditreader.data.PostSyncAdapter.SYNC_INTERVAL;

/**
 * Created by sanjaydixit on 23/01/17.
 */

public class App extends Application {
    private Tracker mTracker;

    @Override
    public void onCreate() {
        super.onCreate();
        JRAWUtils.InitJRAW(this);

        PostSyncAdapter.init(this, SinglePostContract.CONTENT_AUTHORITY, SYNC_INTERVAL , SYNC_FLEXTIME );

    }

    synchronized public Tracker getDefaultTracker() {
        if (mTracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
            mTracker = analytics.newTracker(R.xml.global_tracker);
        }
        return mTracker;
    }
}
