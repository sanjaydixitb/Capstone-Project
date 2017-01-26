package com.bsdsolutions.sanjaydixit.redditreader;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.bsdsolutions.sanjaydixit.redditreader.data.SubredditLoaderCallbackInterface;
import com.bsdsolutions.sanjaydixit.redditreader.util.JRAWUtils;
import com.bsdsolutions.sanjaydixit.redditreader.util.Utils;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import net.dean.jraw.RedditClient;
import net.dean.jraw.auth.AuthenticationManager;
import net.dean.jraw.auth.AuthenticationState;
import net.dean.jraw.models.Listing;
import net.dean.jraw.models.Subreddit;
import net.dean.jraw.paginators.UserSubredditsPaginator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.security.AccessController.getContext;

public class SubredditSelectorActivity extends AppCompatActivity implements SubredditLoaderCallbackInterface {
    private Tracker mTracker;
    private DownloadSubredditsTask mDownloadTask;
    private SubredditSelectorAdapter mAdapter;

    private Set<String> mSelectedSubredditSet = null;

    private SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subreddit_selector);

        mTracker = ((App)getApplication()).getDefaultTracker();

        View recyclerView = findViewById(R.id.subreddit_list);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefreshsubredditSelector);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshList();
            }
        });

        assert recyclerView != null;
        mAdapter = new SubredditSelectorAdapter();
        ((RecyclerView)recyclerView).setAdapter(mAdapter);

        mSelectedSubredditSet = new HashSet<>();

    }

    private void refreshList() {
        mTracker.setScreenName(getString(R.string.activity_subreddit_selector));
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        mSwipeRefreshLayout.setRefreshing(true);
        mDownloadTask = new DownloadSubredditsTask(this, this);
        mDownloadTask.execute();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setTitle(R.string.activity_subreddit_selector);
        mSelectedSubredditSet.clear();
        mSelectedSubredditSet.addAll(Utils.getSubscribedRedditSet(getApplicationContext()));
        refreshList();
    }

    @Override
    protected void onPause() {
        Utils.setSubscribedRedditSet(getApplicationContext(),mSelectedSubredditSet);
        Intent dataUpdatedIntent = new Intent(Utils.UPDATE_APP_WIDGET).setPackage(getPackageName());
        sendBroadcast(dataUpdatedIntent);
        super.onPause();
    }

    @Override
    public void OnSubredditsLoaded(List<SubredditInformation> subredditInformationList) {
        if(mAdapter == null)
            mAdapter = new SubredditSelectorAdapter();
        mAdapter.mItems.clear();
        mAdapter.mItems.addAll(subredditInformationList);
        mAdapter.notifyDataSetChanged();
        mSwipeRefreshLayout.setRefreshing(false);
    }

    public class SubredditSelectorAdapter extends RecyclerView.Adapter<SubredditSelectorViewHolder> {
        public List<SubredditInformation> mItems = new ArrayList<>();

        @Override
        public SubredditSelectorViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.subreddit_selector_item, parent, false);
            return new SubredditSelectorViewHolder(view);        }

        @Override
        public void onBindViewHolder(SubredditSelectorViewHolder holder, int position) {
            final SubredditInformation info = mItems.get(position);
            holder.mTitleView.setText(info.title);
            holder.mDescriptionView.setText(info.description);
            holder.mCheckBox.setChecked(mSelectedSubredditSet.contains(info.displayName));
            holder.mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if(!b) {
                        mSelectedSubredditSet.remove(info.displayName);
                    } else {
                        mSelectedSubredditSet.add(info.displayName);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return mItems.size();
        }
    }

    public class SubredditSelectorViewHolder extends RecyclerView.ViewHolder {

        public final TextView mTitleView;
        public final TextView mDescriptionView;
        public final CheckBox mCheckBox;

        public SubredditSelectorViewHolder(View itemView) {
            super(itemView);
            mTitleView = (TextView) itemView.findViewById(R.id.subreddit_title);
            mDescriptionView = (TextView) itemView.findViewById(R.id.subreddit_description);
            mCheckBox = (CheckBox) itemView.findViewById(R.id.subreddit_checkbox);
        }
    }

    public class DownloadSubredditsTask extends AsyncTask<Void, Void ,List<SubredditInformation>> {
        private SubredditLoaderCallbackInterface mCallbackInterface;
        private Context mContext;

        public DownloadSubredditsTask(Context context, SubredditLoaderCallbackInterface callbackInterface) {
            mCallbackInterface = callbackInterface;
            mContext = context;
        }

        @Override
        protected List<SubredditInformation> doInBackground(Void... voids) {
            Log.d(PostListActivity.TAG, "Fetching Subreddits user has subscribed to!");
            List<SubredditInformation> subredditInformationList = new ArrayList<>();

            AuthenticationManager authMngr = AuthenticationManager.get();

            if (authMngr.checkAuthState() == AuthenticationState.NEED_REFRESH) {
                // refresh access token
                try {
                    Log.d(PostListActivity.TAG, "Refreshing access token");
                    authMngr.refreshAccessToken(JRAWUtils.APP_CREDENTIALS);
                } catch (Exception e) {
                    Log.e(PostListActivity.TAG,"Exception: " + e);
                    return subredditInformationList;
                }
            }

            RedditClient redditClient = AuthenticationManager.get().getRedditClient();

            UserSubredditsPaginator paginator = new UserSubredditsPaginator(redditClient, "subscriber");

            HashMap<String, Subreddit> latestSubreddits = new HashMap<>();
            try {
                while (paginator.hasNext()) {
                    Listing<Subreddit> subreddits = paginator.next();
                    for (Subreddit subreddit: subreddits) {
                        if (!subreddit.isNsfw()) {
                            latestSubreddits.put(subreddit.getId(), subreddit);
                        }
                    }
                }
            } catch (Exception e) {
                Log.e(PostListActivity.TAG,"Exception: " + e);
                return subredditInformationList;
            }

            if (latestSubreddits.isEmpty()) {
                Log.e(PostListActivity.TAG, "No Subreddits!");
                return subredditInformationList;
            }

            for (Subreddit subreddit: latestSubreddits.values()) {
                SubredditInformation information = new SubredditInformation();
                information.id = subreddit.getId();
                information.title = subreddit.getDisplayName();
                information.description = subreddit.getPublicDescription();
                information.displayName = subreddit.getDisplayName();
                subredditInformationList.add(information);
            }

            return subredditInformationList;
        }

        @Override
        protected void onPostExecute(List<SubredditInformation> subredditInformationList) {
            super.onPostExecute(subredditInformationList);
            mCallbackInterface.OnSubredditsLoaded(subredditInformationList);
        }
    }

}
