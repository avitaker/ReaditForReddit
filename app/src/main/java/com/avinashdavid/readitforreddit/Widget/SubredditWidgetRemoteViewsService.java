package com.avinashdavid.readitforreddit.Widget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.widget.ListView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.avinashdavid.readitforreddit.CommentsActivity;
import com.avinashdavid.readitforreddit.MiscUtils.Constants;
import com.avinashdavid.readitforreddit.MiscUtils.GeneralUtils;
import com.avinashdavid.readitforreddit.NetworkUtils.GetListingsService;
import com.avinashdavid.readitforreddit.PostUtils.RedditListing;
import com.avinashdavid.readitforreddit.PostUtils.RedditPost;
import com.avinashdavid.readitforreddit.R;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import timber.log.Timber;

import static android.R.attr.data;
import static android.R.style.Widget;

/**
 * Created by avinashdavid on 3/16/17.
 */

public class SubredditWidgetRemoteViewsService extends RemoteViewsService {
    public static final String KEY_POSTS = "finallyPosts";
    List<RedditListing> mRedditPosts;

    @Override
    public RemoteViewsFactory onGetViewFactory(final Intent intent) {
        RemoteViewsFactory remoteViewsFactory = new RemoteViewsFactory() {

            private ArrayList<ArrayList<String>> mArrayList;

            @Override
            public void onCreate() {
                Timber.d("SubredditWidgetRemoteViewsService started");
//                mArrayLists = new ArrayList<>();
////                mRedditPosts = new RealmResults<RedditPost>();
//                mRedditPosts = RedditListing.listAll(RedditListing.class);
//                Timber.d("post count in oncreate " + Integer.toString(mRedditPosts.size()));
            }

            @Override
            public void onDataSetChanged() {
                Timber.d("onDataSetChanged called");
                mRedditPosts = RedditListing.listAll(RedditListing.class);
                Timber.d("post count in datasetchanged " + Integer.toString(mRedditPosts.size()));
            }

            @Override
            public void onDestroy() {
                mRedditPosts = null;
            }

            @Override
            public int getCount() {
                int returnInt = mRedditPosts == null ? 0:mRedditPosts.size();
                Timber.d("posts count in getCount " + Integer.toString(returnInt));
                return returnInt;
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if (position == ListView.INVALID_POSITION || mRedditPosts == null || mRedditPosts.get(position)==null){
                    Timber.d("nothing to see here");
                    return null;
                }

                RedditListing redditPost = mRedditPosts.get(position);
                Timber.d("getViewAt for redditpost: " + redditPost.mPostId);
//                Timber.d("HARHAR" + mArrayLists.get(0).get(Constants.INDEX_TITLE_SUBREDDIT_WIDGET));
                Context context = SubredditWidgetRemoteViewsService.this;
                RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.item_widget);
                String voteCount = context.getString(R.string.format_points, redditPost.voteCount);
                String author= redditPost.author;
                String subreddit = redditPost.subreddit;
                String title = redditPost.mTitle;
                String numComments = context.getString(R.string.format_numberofcomments,redditPost.commentsCount);
                String domain = redditPost.domain;
                String timeelapsed = GeneralUtils.returnFormattedTime(context, System.currentTimeMillis(), redditPost.timeCreated);

                remoteViews.setTextViewText(R.id.voteCount_textview, voteCount);
                remoteViews.setTextViewText(R.id.listingTitle_textview, title);
                remoteViews.setTextViewText(R.id.time_elapsed_textview, timeelapsed);
                remoteViews.setTextViewText(R.id.author_textview, author);
                remoteViews.setTextViewText(R.id.subreddit_textview, subreddit);
                remoteViews.setTextViewText(R.id.listing_domain_textview, domain);
                remoteViews.setTextViewText(R.id.numberOfComments_textview, numComments);

                String postId = redditPost.mPostId;

                final Intent fillInIntent = new Intent();
                fillInIntent.putExtra(CommentsActivity.EXTRA_POST_ID, postId);
                remoteViews.setOnClickFillInIntent(R.id.post_info_container, fillInIntent);

                return remoteViews;
            }

            @Override
            public RemoteViews getLoadingView() {
                RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.widget_empty_view);
                return remoteViews;
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                if (mRedditPosts.get(position) != null){
                    return mRedditPosts.get(position).getId();
                }
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }



        };
        return remoteViewsFactory;
    }


}
