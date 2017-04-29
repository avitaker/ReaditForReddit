package com.avinashdavid.readitforreddit.UI;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.avinashdavid.readitforreddit.CommentsActivity;
import com.avinashdavid.readitforreddit.MainActivity;
import com.avinashdavid.readitforreddit.MiscUtils.GeneralUtils;
import com.avinashdavid.readitforreddit.NetworkUtils.GetCommentsService;
import com.avinashdavid.readitforreddit.PostUtils.RedditListing;
import com.avinashdavid.readitforreddit.R;
import com.squareup.picasso.Picasso;

import java.util.List;

import timber.log.Timber;

/**
 * Created by avinashdavid on 3/17/17.
 */

public class RedditPostRecyclerAdapter extends RecyclerView.Adapter<RedditPostRecyclerAdapter.ListingHolder> {
    private final MainActivity mMainActivity;
    private LayoutInflater mLayoutInflater;
    public List<RedditListing> mRedditListings;
    private boolean usingTabletLayout;

    private RedditPostRecyclerAdapter.ScrollListener mScrollListener;

    private static final String[] imageMarkers = new String[]{
            ".jpg"
    };

    private int VIEW_TYPE_EMPTY = 0;
    private int VIEW_TYPE_NORMAL = 1;
    private int VIEW_TYPE_LOADING = 2;
    private int VIEW_TYPE_NO_THUMBNAIL = 3;
    private int VIEW_TYPE_IMAGE_LINK = 4;

    public RedditPostRecyclerAdapter(@NonNull Context context, List<RedditListing> redditListings) {
        this.mMainActivity = (MainActivity)context;
        this.mLayoutInflater = LayoutInflater.from(mMainActivity);
        mRedditListings = redditListings;
        try {
            mScrollListener = (ScrollListener) context;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(context.toString()
                    + " must implement ScrollListener");
        }
        usingTabletLayout = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(context.getString(R.string.pref_boolean_use_tablet_layout),false);
    }



    @Override
    public ListingHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        boolean withImage = false;
        if (viewType==VIEW_TYPE_NORMAL) {
            view = mLayoutInflater.inflate(R.layout.item_listing, parent, false);
            withImage = true;
        }
        else if (viewType == VIEW_TYPE_IMAGE_LINK){
            view = mLayoutInflater.inflate(R.layout.item_listing_clickable_thumbnail, parent, false);
            withImage = true;
        }
        else if (viewType== VIEW_TYPE_NO_THUMBNAIL){
            view = mLayoutInflater.inflate(R.layout.item_listing_no_image, parent, false);
        } else {
            view = mLayoutInflater.inflate(R.layout.widget_empty_view, parent, false);
        }
        return new ListingHolder(view, withImage);
    }

    @Override
    public void onBindViewHolder(final ListingHolder holder, int position) {
        int viewtype = getItemViewType(position);
        if (viewtype==VIEW_TYPE_NORMAL || viewtype == VIEW_TYPE_NO_THUMBNAIL || viewtype == VIEW_TYPE_IMAGE_LINK) {
            final RedditListing redditPost = mRedditListings.get(position);
            if (viewtype!=VIEW_TYPE_IMAGE_LINK) {
                holder.bindListing(redditPost, false);
            } else {
                holder.bindListing(redditPost, true);
            }

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (usingTabletLayout){
                        mMainActivity.refreshComments();
                        startCommentsService(mMainActivity, redditPost.mPostId);
                    } else {
                        if (android.os.Build.VERSION.SDK_INT >= 21) {
                            startIntentV21(mMainActivity, redditPost.mPostId, holder.itemView);
                        } else {
                            startIntentRegular(mMainActivity, redditPost.mPostId);
                        }
                    }
                }
            });

            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    FragmentManager fm = mMainActivity.getSupportFragmentManager();
                    FragmentTransaction ft = fm.beginTransaction();
                    Fragment prev = fm.findFragmentByTag("dialog");
                    if (prev != null) {
                        ft.remove(prev);
                    }
                    ft.addToBackStack(null);

                    PostOptionsDialogFragment newFragment = PostOptionsDialogFragment.newInstance(redditPost.mTitle, GeneralUtils.returnFormattedStringFromHtml(redditPost.url).toString(), redditPost.subreddit);
                    newFragment.show(ft, PostOptionsDialogFragment.TAG_POST_OPTIONS);
                    return true;
                }
            });



        }
    }

    @Override
    public int getItemCount() {
        return mRedditListings.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        int returnInt = 0;
        if (getItemCount()==0){
            returnInt = VIEW_TYPE_EMPTY;
        } else if (position >= mRedditListings.size()){
            returnInt = VIEW_TYPE_LOADING;
        } else {
            RedditListing listing = mRedditListings.get(position);
            String thumbnailUrl = listing.thumbnailUrl;
            String url = listing.url;
            if (thumbnailUrl.length()==0 || thumbnailUrl.equals("self") || thumbnailUrl.equals("default") || thumbnailUrl.equals("nsfw")) {
                returnInt = VIEW_TYPE_NO_THUMBNAIL;
            } else {
                returnInt = VIEW_TYPE_NORMAL;
                for (int i=0; i<imageMarkers.length; i++){
                    if (url.contains(imageMarkers[i])){
                        returnInt = VIEW_TYPE_IMAGE_LINK;
                    }
                }
            }
        }
        return returnInt;
    }

    @Override
    public void onViewAttachedToWindow(ListingHolder holder) {
        super.onViewAttachedToWindow(holder);
        if (holder.getAdapterPosition() > mRedditListings.size() - 3){
            String after = mRedditListings.get(mRedditListings.size()-1).after;
            mScrollListener.OnReachedLast(after);
        }
    }

    public static class ListingHolder extends RecyclerView.ViewHolder {
        TextView voteCount_textview;
        TextView author_textview;
        TextView subreddit_textview;
        TextView listTitle_textview;
        TextView commentCount_textview;
        TextView domain_textview;
        TextView timecreated_textview;
        LinearLayout mRelativeLayout;
        RelativeLayout mCardRelativeLayout;
        ImageView thumbnail_imageview;

        TextView emptyTextView;

        Context mContext;

        public String mPostId;
        final int goldColor;
        final int redColor;
        ColorStateList ogColor;

        private RedditListing mRedditPost;

        public ListingHolder(View itemView, boolean withImage) {
            super(itemView);
            if (itemView.findViewById(R.id.post_info_container)!=null) {
                voteCount_textview = (TextView) itemView.findViewById(R.id.voteCount_textview);
                author_textview = (TextView) itemView.findViewById(R.id.author_textview);
                subreddit_textview = (TextView) itemView.findViewById(R.id.subreddit_textview);
                listTitle_textview = (TextView) itemView.findViewById(R.id.listingTitle_textview);
                commentCount_textview = (TextView) itemView.findViewById(R.id.numberOfComments_textview);
                domain_textview = (TextView) itemView.findViewById(R.id.listing_domain_textview);
                timecreated_textview = (TextView) itemView.findViewById(R.id.time_elapsed_textview);
                if (withImage) {
                    thumbnail_imageview = (ImageView) itemView.findViewById(R.id.post_thumbnail);
                }
                mRelativeLayout = (LinearLayout) itemView.findViewById(R.id.post_info_container);
                mCardRelativeLayout = (RelativeLayout) itemView.findViewById(R.id.card_view);
                ogColor = listTitle_textview.getTextColors();
                mContext = itemView.getContext();
            } else {
                mContext = itemView.getContext();
                Timber.e("no postinfo");
            }
            goldColor = GeneralUtils.getSDKSensitiveColor(mContext, R.color.gold);
            redColor = GeneralUtils.getSDKSensitiveColor(mContext, android.R.color.holo_red_dark);
        }


        public void bindListing(RedditListing listing, boolean imageLink){
            mRedditPost = listing;
            mPostId = listing.mPostId;
            if (!MainActivity.usingTabletLayout) {
                setTransitionNamePost(mContext, mRelativeLayout, mPostId);
                setTransitionNamePostBg(mContext, mCardRelativeLayout, mPostId);
            }
            voteCount_textview.setText(Integer.toString(listing.voteCount));
            author_textview.setText(listing.author);
            subreddit_textview.setText(mContext.getString(R.string.format_subreddit, listing.subreddit));
            listTitle_textview.setText(GeneralUtils.returnFormattedStringFromHtml(listing.mTitle));
            commentCount_textview.setText(mContext.getString(R.string.format_numberofcomments, listing.commentsCount));
            domain_textview.setText(listing.domain);
            timecreated_textview.setText(GeneralUtils.returnFormattedTime(mContext, System.currentTimeMillis(), listing.timeCreated));
            if (thumbnail_imageview!=null) {
                Picasso picasso = Picasso.with(mContext);
                if (imageLink){
                    final String linkUrl = mRedditPost.url;
//                    picasso.setIndicatorsEnabled(true);
                    thumbnail_imageview.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            FragmentViewImage fragmentViewImage = FragmentViewImage.getImageViewFragment(linkUrl);
                            FragmentManager fragmentManager = ((AppCompatActivity)mContext).getSupportFragmentManager();
                            FragmentTransaction ft = fragmentManager.beginTransaction();
                            Fragment prev = fragmentManager.findFragmentByTag(FragmentViewImage.TAG_IMAGE_FRAGMENT);
                            if (prev != null) {
                                ft.remove(prev);
                            }
                            ft.addToBackStack(null);

                            fragmentViewImage.show(ft, FragmentViewImage.TAG_IMAGE_FRAGMENT);
                        }
                    });
                } else {

                }
                picasso.load(listing.thumbnailUrl).into(thumbnail_imageview);
            }

            if (listing.isGilded){
                author_textview.setTextColor(goldColor);
            } else {
                if (author_textview.getCurrentTextColor() == goldColor) {
                    author_textview.setTextColor(GeneralUtils.getThemeAccentColor(mContext));
                }
            }

            if (listing.isNSFW){
                listTitle_textview.setTextColor(redColor);
            } else {
                if (listTitle_textview.getCurrentTextColor() == redColor){
                    listTitle_textview.setTextColor(ogColor);
                }
            }
        }
    }

    public interface ScrollListener{
        void OnReachedLast(String after);
    }

    @TargetApi(21)
    void startIntentV21(Activity c, String postId, View v){
        Intent intent = new Intent(c, CommentsActivity.class);
        intent.putExtra(CommentsActivity.EXTRA_POST_ID, postId);
        Pair<View, String> p0 = Pair.create(v.findViewById(R.id.card_view), c.getString(R.string.transitionName_PostBg) + postId);
        Pair<View, String> p1 = Pair.create(v.findViewById(R.id.post_info_container), c.getString(R.string.transitionName_Post) + postId);
        ActivityOptionsCompat options = ActivityOptionsCompat.
                makeSceneTransitionAnimation(c, p0, p1);
        c.startActivity(intent, options.toBundle());
    }

    void startIntentRegular(Context c, String postId){
        Intent intent = new Intent(c, CommentsActivity.class);
        intent.putExtra(CommentsActivity.EXTRA_POST_ID, postId);
        c.startActivity(intent);
    }

    void startCommentsService(Context c, String postId){
        GetCommentsService.loadCommentsForArticle(c, null, postId, null);
    }

    @TargetApi(21)
    static void setTransitionNamePost(Context c, View v, String postId){
        v.setTransitionName(c.getString(R.string.transitionName_Post)+postId);
    }

    @TargetApi(21)
    static void setTransitionNamePostBg(Context c, View v, String postId){
        v.setTransitionName(c.getString(R.string.transitionName_PostBg)+postId);
    }
}
