package com.bsdsolutions.sanjaydixit.redditreader.util;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by sanjaydixit on 23/01/17.
 */

public class Utils {
    public static final String UPDATE_APP_WIDGET = "com.bsdsolutions.sanjaydixit.redditreader.UPDATE_WIDGET";

    public static final String INTENT_PARCELABLE_EXTRA_KEY = "intent_parcelable_extra_key";

    public static final String SUBREDDIT_SET_SHARED_PREFS_KEY = "subreddit_set_shared_prefs_key";

    public static final String SUBREDDIT_SET_SHARED_PREFS_FILE_NAME = "subreddit_set_shared_prefs_file_name";

    public static Set<String> getSubscribedRedditSet(Context context) {
        Set<String> subredditSet = new HashSet<>();
        SharedPreferences preferences = context.getSharedPreferences(SUBREDDIT_SET_SHARED_PREFS_FILE_NAME,Context.MODE_PRIVATE);
        subredditSet.addAll(preferences.getStringSet(SUBREDDIT_SET_SHARED_PREFS_KEY,new HashSet<String>()));
        return subredditSet;
    }

    public static void setSubscribedRedditSet(Context context, Set<String> subredditSet) {
        SharedPreferences preferences = context.getSharedPreferences(SUBREDDIT_SET_SHARED_PREFS_FILE_NAME,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putStringSet(SUBREDDIT_SET_SHARED_PREFS_KEY,subredditSet);
        editor.apply();
    }
}
