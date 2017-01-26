package com.bsdsolutions.sanjaydixit.redditreader;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.bsdsolutions.sanjaydixit.redditreader.content.PostItemList;
import com.bsdsolutions.sanjaydixit.redditreader.data.PostSyncAdapter;
import com.bsdsolutions.sanjaydixit.redditreader.data.SinglePostContract;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import net.dean.jraw.auth.AuthenticationManager;
import net.dean.jraw.auth.AuthenticationState;
import net.dean.jraw.auth.NoSuchTokenException;
import net.dean.jraw.http.oauth.Credentials;
import net.dean.jraw.http.oauth.OAuthException;

import java.util.ArrayList;

import static com.bsdsolutions.sanjaydixit.redditreader.data.SinglePostContract.CONTENT_AUTHORITY;
import static com.bsdsolutions.sanjaydixit.redditreader.util.Utils.INTENT_PARCELABLE_EXTRA_KEY;

/**
 * An activity representing a list of Posts. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link PostDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class PostListActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;
    private static final int LOADER_ID = 1;
    public static String TAG = "SanjayRedditReader";
    private SimpleItemRecyclerViewAdapter mAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        View recyclerView = findViewById(R.id.post_list);
        assert recyclerView != null;
        setupRecyclerView((RecyclerView) recyclerView);

        getSupportLoaderManager().initLoader(1 , null , this);   // 1 is LOADER_ID

        if (findViewById(R.id.post_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Refresh items
                refreshItems();
            }
        });

        MobileAds.initialize(getApplicationContext(), getResources().getString(R.string.banner_ad_unit_id));

        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)        // All emulators
                .addTestDevice("635482F931F8668B1EE277A5CA92796E").build();
        mAdView.loadAd(adRequest);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.settings:
                displaySubredditSelectorActivity();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void refreshItems() {
        PostSyncAdapter.syncNow(getApplicationContext(), CONTENT_AUTHORITY, null);
    }

    public void displaySubredditSelectorActivity() {
        Intent intent = new Intent(this, SubredditSelectorActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        AuthenticationState state = AuthenticationManager.get().checkAuthState();
        Log.d(TAG, "AuthenticationState for onResume(): " + state);

        switch (state) {
            case READY:
                Intent i = getIntent();
                ArrayList<PostItemList.SinglePost> posts = i.getParcelableArrayListExtra(INTENT_PARCELABLE_EXTRA_KEY);
                if(posts != null && posts.size() > 0) {
                    //TODO: there's got to be a better way to do this.
                    Intent intent = new Intent(getApplicationContext(), PostDetailActivity.class);
                    intent.putExtra(PostDetailFragment.ARG_ITEM, posts.get(0));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    getApplicationContext().startActivity(intent);
                    i.removeExtra(INTENT_PARCELABLE_EXTRA_KEY);
                } else {
                    loadPosts();
                }
                break;
            case NONE:
                Toast.makeText(PostListActivity.this, "Log in first", Toast.LENGTH_SHORT).show();
                login();
                break;
            case NEED_REFRESH:
                refreshAccessTokenAsync();
                Intent intent1 = getIntent();
                ArrayList<PostItemList.SinglePost> postList = intent1.getParcelableArrayListExtra(INTENT_PARCELABLE_EXTRA_KEY);
                if(postList != null && postList.size() > 0) {
                    //TODO: there's got to be a better way to do this.
                    Intent intent = new Intent(getApplicationContext(), PostDetailActivity.class);
                    intent.putExtra(PostDetailFragment.ARG_ITEM, postList.get(0));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    getApplicationContext().startActivity(intent);
                    intent1.removeExtra(INTENT_PARCELABLE_EXTRA_KEY);
                }
                break;
        }

    }

    private void refreshAccessTokenAsync() {
        new AsyncTask<Credentials, Void, Void>() {
            @Override
            protected Void doInBackground(Credentials... params) {
                try {
                    AuthenticationManager.get().refreshAccessToken(LoginActivity.CREDENTIALS);
                } catch (NoSuchTokenException | OAuthException e) {
                    Log.e(TAG, "Could not refresh access token", e);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void v) {
                Log.d(TAG, "Reauthenticated");
            }
        }.execute();
    }

    public void loadPosts() {
        PostSyncAdapter.syncNow(getApplicationContext(),CONTENT_AUTHORITY,null);
    }

    public void login() { startActivity(new Intent(this, LoginActivity.class)); }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        mAdapter = new SimpleItemRecyclerViewAdapter(this, null);
        recyclerView.setAdapter(mAdapter);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch(id) {
            case LOADER_ID:
                return new CursorLoader(PostListActivity.this, SinglePostContract.POST_TABLE_PATH,null,null,null,null);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.changeCursor(data);
        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.changeCursor(null);
        mSwipeRefreshLayout.setRefreshing(false);
    }

    public class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        private Context mContext = null;
        private Cursor mCursor = null;

        public SimpleItemRecyclerViewAdapter( Context context, Cursor cursor) {
            mContext = context;
            mCursor = cursor;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.post_list_content, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            PostItemList.SinglePost post = (PostItemList.SinglePost)getItem(position);
            if(post == null)
                return;
            holder.mItem = post;
            holder.mTitleView.setText(post.title);
            holder.mCommentsView.setText(String.valueOf(post.commentCount));
            holder.mUpvoteView.setText(String.valueOf(post.voteCount));
            holder.mDownvoteView.setText("");
            Picasso.with(mContext).setLoggingEnabled(true);
            Picasso.with(mContext).load(post.image).into(holder.mPostImageView, new Callback() {
                @Override
                public void onSuccess() {
                }

                @Override
                public void onError() {

                }
            });

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mTwoPane) {
                        Bundle arguments = new Bundle();
                        arguments.putParcelable(PostDetailFragment.ARG_ITEM, holder.mItem);
                        PostDetailFragment fragment = new PostDetailFragment();
                        fragment.setArguments(arguments);
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.post_detail_container, fragment)
                                .commit();
                    } else {
                        Context context = v.getContext();
                        Intent intent = new Intent(context, PostDetailActivity.class);
                        intent.putExtra(PostDetailFragment.ARG_ITEM, holder.mItem);

                        context.startActivity(intent);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return (mCursor == null) ? 0 : mCursor.getCount();
        }

        public void changeCursor(Cursor newCursor){
            if (mCursor == newCursor){
                return;
            }

            Cursor prev = mCursor;
            mCursor = newCursor;

            if (mCursor != null){
                notifyDataSetChanged();
            }

            if (prev != null){
                prev.close();
            }
        }

        private Object getItem(int position){
            mCursor.moveToPosition(position);
            if(mCursor == null)
                return null;
            String id = mCursor.getString(mCursor.getColumnIndex(SinglePostContract.PostTableEntry.COLUMN_NAME_ID));
            String title = mCursor.getString(mCursor.getColumnIndex(SinglePostContract.PostTableEntry.COLUMN_NAME_TITLE));
            String image = mCursor.getString(mCursor.getColumnIndex(SinglePostContract.PostTableEntry.COLUMN_NAME_IMAGE_LINK));
            int commentCount = mCursor.getInt(mCursor.getColumnIndex(SinglePostContract.PostTableEntry.COLUMN_NAME_COMMENTS));
            int voteCount = mCursor.getInt(mCursor.getColumnIndex(SinglePostContract.PostTableEntry.COLUMN_NAME_VOTECOUNT));
            PostItemList.SinglePost post = new PostItemList.SinglePost(id,title,image,commentCount,voteCount);
            return post;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public final View mView;
            public final TextView mTitleView;
            public final TextView mCommentsView;
            public final TextView mUpvoteView;
            public final TextView mDownvoteView;
            public final ImageView mPostImageView;
            public PostItemList.SinglePost mItem;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                mTitleView = (TextView) view.findViewById(R.id.post_title);
                mCommentsView = (TextView) view.findViewById(R.id.post_comments_count);
                mUpvoteView = (TextView) view.findViewById(R.id.post_upvote_count);
                mDownvoteView = (TextView) view.findViewById(R.id.post_downvote_count);
                mPostImageView = (ImageView) view.findViewById(R.id.post_image);
            }


            @Override
            public String toString() {
                return super.toString() + " '" + mTitleView.getText() + "'";
            }
        }
    }
}
