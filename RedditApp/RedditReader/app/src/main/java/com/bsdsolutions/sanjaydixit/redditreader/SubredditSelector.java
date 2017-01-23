package com.bsdsolutions.sanjaydixit.redditreader;

import android.content.Context;
import android.os.AsyncTask;
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
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import net.dean.jraw.RedditClient;
import net.dean.jraw.auth.AuthenticationManager;
import net.dean.jraw.models.Listing;
import net.dean.jraw.models.Subreddit;
import net.dean.jraw.paginators.UserSubredditsPaginator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SubredditSelector extends AppCompatActivity implements SubredditLoaderCallbackInterface {
    private Tracker mTracker;
    private DownloadSubredditsTask mDownloadTask;
    private SubredditSelectorAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subreddit_selector);

        mTracker = ((App)getApplication()).getDefaultTracker();
        mDownloadTask = new DownloadSubredditsTask(this, this);

        View recyclerView = findViewById(R.id.subreddit_list);
        assert recyclerView != null;
        mAdapter = new SubredditSelectorAdapter();
        ((RecyclerView)recyclerView).setAdapter(mAdapter);

    }

    @Override
    protected void onResume() {
        super.onResume();
        setTitle(R.string.activity_subreddit_selector);
        mTracker.setScreenName(getString(R.string.activity_subreddit_selector));
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        mDownloadTask.execute();
    }

    @Override
    public void OnSubredditsLoaded(List<SubredditInformation> subredditInformationList) {
        if(mAdapter == null)
            mAdapter = new SubredditSelectorAdapter();
        mAdapter.mItems.clear();
        mAdapter.mItems.addAll(subredditInformationList);
        mAdapter.notifyDataSetChanged();
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
            SubredditInformation info = mItems.get(position);
            holder.mTitleView.setText(info.title);
            holder.mDescriptionView.setText(info.description);
            holder.mCheckBox.setChecked(true);
            holder.mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if(!b) {
                        //TODO:Remove it!
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
            List<SubredditInformation> subredditInformationList = new ArrayList<>();

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
                return subredditInformationList;
            }

            if (latestSubreddits.isEmpty()) {
                Log.e(PostListActivity.TAG, "No Subreddits!");
                return subredditInformationList;
            }

            for (Subreddit subreddit: latestSubreddits.values()) {
                SubredditInformation information = new SubredditInformation();
                information.id = Long.parseLong(subreddit.getId());
                information.title = subreddit.getDisplayName();
                information.description = subreddit.getPublicDescription();
                subredditInformationList.add(information);
            }

            //TODO:remove subreddits not selected anymore

            return subredditInformationList;
        }

        @Override
        protected void onPostExecute(List<SubredditInformation> subredditInformationList) {
            super.onPostExecute(subredditInformationList);
            mCallbackInterface.OnSubredditsLoaded(subredditInformationList);
        }
    }

}
