package com.bsdsolutions.sanjaydixit.redditreader;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.design.widget.CollapsingToolbarLayout;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.TextView;

import com.bsdsolutions.sanjaydixit.redditreader.content.PostItemList;
import com.squareup.picasso.Picasso;

import net.dean.jraw.models.Submission;

/**
 * A fragment representing a single Post detail screen.
 * This fragment is either contained in a {@link PostListActivity}
 * in two-pane mode (on tablets) or a {@link PostDetailActivity}
 * on handsets.
 */
public class PostDetailFragment extends Fragment {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM = "item";

    /**
     * The dummy content this fragment is presenting.
     */
    private PostItemList.SinglePost mItem;

    private ImageView mImageView = null;
    private View.OnClickListener mClickListener = null;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public PostDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM)) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
            mItem = getArguments().getParcelable(ARG_ITEM);

            Activity activity = this.getActivity();
            CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
            final String itemType = mItem.type;
            final String url = mItem.url;

            mClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent();
                    i.setAction(Intent.ACTION_VIEW);

                    String mimeType = null;
                    String fileExt = null;


                    if (Submission.PostHint.IMAGE.toString().equals(itemType)) {
                        //Image
                        fileExt = MimeTypeMap.getFileExtensionFromUrl(url);
                        if (!"".equals(fileExt)) {
                            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExt);
                        }
                    } else if (Submission.PostHint.LINK.toString().equals(itemType)) {
                        //Link
                    } else if (Submission.PostHint.SELF.toString().equals(itemType)) {
                        //Text
                        return;
                    } else if (Submission.PostHint.VIDEO.toString().equals(itemType)) {
                        //Video
                        fileExt = MimeTypeMap.getFileExtensionFromUrl(url);
                        if (!"".equals(fileExt)) {
                            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExt);
                        }
                    } else {
                        return;
                        //Unknown
                    }
                    if (mimeType == null) {
                        i.setData(Uri.parse(url));
                        getContext().startActivity(i);
                    } else {

                        i.setDataAndType(Uri.parse(url), mimeType);
                        if (getContext().getPackageManager().resolveActivity(i, PackageManager.MATCH_ALL) != null) {
                            getContext().startActivity(i);
                        } else {
                            // remove specific type and try again
                            i.setData(Uri.parse(url));
                            getContext().startActivity(i);
                        }
                    }
                }
            };
            if (appBarLayout != null) {
                appBarLayout.setTitle(mItem.title);
                appBarLayout.setOnClickListener(mClickListener);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.post_detail, container, false);

        // Show the dummy content as text in a TextView.
        if (mItem != null) {
            ((TextView) rootView.findViewById(R.id.post_detail)).setText(mItem.title);
            mImageView = (ImageView) rootView.findViewById(R.id.detail_image_view);
            if (mItem.image != null && mItem.image.length() > 0 && mImageView != null) {
                mImageView.setVisibility(View.VISIBLE);
                mImageView.setOnClickListener(mClickListener);
                Picasso.with(getContext()).load(mItem.image).into(mImageView);
            }
        } else {
            mImageView.setVisibility(View.GONE);
            ((TextView) rootView.findViewById(R.id.post_detail)).setText(R.string.select_post_to_view_details);
        }


        return rootView;
    }
}
