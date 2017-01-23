package com.bsdsolutions.sanjaydixit.redditreader;

import android.app.Application;

import com.bsdsolutions.sanjaydixit.redditreader.util.JRAWUtils;

import net.dean.jraw.RedditClient;
import net.dean.jraw.android.AndroidRedditClient;
import net.dean.jraw.android.AndroidTokenStore;
import net.dean.jraw.auth.AuthenticationManager;
import net.dean.jraw.auth.RefreshTokenHandler;
import net.dean.jraw.http.LoggingMode;

/**
 * Created by sanjaydixit on 23/01/17.
 */

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        JRAWUtils.InitJRAW(this);
    }
}
