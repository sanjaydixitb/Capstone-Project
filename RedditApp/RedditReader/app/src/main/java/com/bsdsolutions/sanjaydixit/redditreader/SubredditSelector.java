package com.bsdsolutions.sanjaydixit.redditreader;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toolbar;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

public class SubredditSelector extends AppCompatActivity {
    private Tracker mTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subreddit_selector);

        mTracker = ((App)getApplication()).getDefaultTracker();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setTitle(R.string.activity_subreddit_selector);
        mTracker.setScreenName(getString(R.string.activity_subreddit_selector));
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }
}
