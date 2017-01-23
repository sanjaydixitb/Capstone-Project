package com.bsdsolutions.sanjaydixit.redditreader.util;

/**
 * Created by sanjaydixit on 22/01/17.
 */

import android.app.Application;

import com.bsdsolutions.sanjaydixit.redditreader.BuildConfig;

import net.dean.jraw.RedditClient;
import net.dean.jraw.android.AndroidRedditClient;
import net.dean.jraw.android.AndroidTokenStore;
import net.dean.jraw.auth.AuthenticationManager;
import net.dean.jraw.auth.RefreshTokenHandler;
import net.dean.jraw.http.LoggingMode;
import net.dean.jraw.http.oauth.Credentials;

public class JRAWUtils {

    public static Credentials APP_CREDENTIALS = Credentials.webapp(BuildConfig.REDDIT_CLIENT_ID, BuildConfig.REDDIT_APP_SECRET, BuildConfig.REDDIT_REDIRECT_URL);

    public static void InitJRAW(Application app){
        RedditClient reddit = new AndroidRedditClient(app);
        reddit.setLoggingMode(LoggingMode.ALWAYS);
        AuthenticationManager.get().init(reddit, new RefreshTokenHandler(new AndroidTokenStore(app), reddit));
    }
}
