package com.bsdsolutions.sanjaydixit.redditreader.data;

import java.util.List;

/**
 * Created by sanjaydixit on 23/01/17.
 */

public interface SubredditLoaderCallbackInterface {

    class SubredditInformation{
        public String title, description;
        public String id;
        public String displayName;
    }

    void OnSubredditsLoaded(List<SubredditInformation> subredditInformationList);
}
