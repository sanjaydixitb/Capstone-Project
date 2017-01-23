package com.bsdsolutions.sanjaydixit.redditreader.util;

/**
 * Created by sanjaydixit on 22/01/17.
 */

import android.app.Application;

import net.dean.jraw.RedditClient;
import net.dean.jraw.android.AndroidRedditClient;
import net.dean.jraw.android.AndroidTokenStore;
import net.dean.jraw.auth.AuthenticationManager;
import net.dean.jraw.auth.RefreshTokenHandler;
import net.dean.jraw.http.LoggingMode;

public class JRAWUtils {
    public static void InitJRAW(Application app){
        RedditClient reddit = new AndroidRedditClient(app);
        reddit.setLoggingMode(LoggingMode.ALWAYS);
        AuthenticationManager.get().init(reddit, new RefreshTokenHandler(new AndroidTokenStore(app), reddit));
    }
}
