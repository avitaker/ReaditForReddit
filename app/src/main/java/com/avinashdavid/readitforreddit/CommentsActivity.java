package com.avinashdavid.readitforreddit;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.avinashdavid.readitforreddit.MiscUtils.Constants;
import com.avinashdavid.readitforreddit.MiscUtils.GPSUtils;
import com.avinashdavid.readitforreddit.MiscUtils.GeneralUtils;
import com.avinashdavid.readitforreddit.MiscUtils.PreferenceUtils;
import com.avinashdavid.readitforreddit.NetworkUtils.GetCommentsService;
import com.avinashdavid.readitforreddit.NetworkUtils.GetListingsService;
import com.avinashdavid.readitforreddit.NetworkUtils.UriGenerator;
import com.avinashdavid.readitforreddit.PostUtils.CommentObject;
import com.avinashdavid.readitforreddit.PostUtils.CommentRecord;
import com.avinashdavid.readitforreddit.PostUtils.RedditListing;
import com.avinashdavid.readitforreddit.UI.CommentRecordRecyclerAdapter;
import com.avinashdavid.readitforreddit.UI.GetCommentsAsyncTask;

import java.util.List;

import io.realm.RealmResults;
import timber.log.Timber;

public class CommentsActivity extends AppCompatActivity {

//    @BindView(R.id.my_toolbar)
//    Toolbar mToolbar;
    //    @BindView(R.id.listing_recyclerview)RecyclerView mCommentsRecyclerview;
//    @BindView(R.id.collapsing_toolbar) CollapsingToolbarLayout mCollapsingToolbarLayout;

    Toolbar mToolbar;

    public static final String EXTRA_URL = "extraUrl";
    public static final String EXTRA_POST_ID = "extraPostId";

    private static final String KEY_SORT = "keySort";
    String mSortString;

    private static final String KEY_LAYOUTMANAGER_STATE = "comLayManState";
    private Parcelable mLayoutState;

    public static final String TAG_COMMENTS_FRAGMENT = "tagCommFrag";

    RecyclerView mCommentsRecyclerview;
    CollapsingToolbarLayout mCollapsingToolbarLayout;

    private String mAfter;

    private int mItemCount = 0;

    private CommentRecordRecyclerAdapter mCommentsRecyclerViewAdapter;

    private List<CommentRecord> mCommentRecords;

    int mFirstVisibleChild;
    int mOffset;


//    private DividerItemDecoration mDividerItemDecoration;

    RealmResults<CommentObject> mCommentObjectRealmResults;

    LinearLayoutManager mLinearLayoutManager;

    private RequestQueue mRequestQueue;

    private Intent mIntent;

//    private Uri mUrl;

    private String mPostId;

    private WebView mWebview;

    TextView voteCount_textview;
    TextView author_textview;
    TextView subreddit_textview;
    TextView listTitle_textview;
    TextView commentCount_textview;
    TextView domain_textview;
    TextView timecreated_textview;
    TextView selftext_textview;

    Snackbar errorSnack;

    BroadcastReceiver mReceiver;
    IntentFilter mIntentFilter;



    RedditListing mListing;

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceUtils.onActivityCreateSetTheme(this);
        setContentView(R.layout.activity_comments);

        GPSUtils.setScreenName(this, "CommentsActivity");
        Timber.d("oncreate");
        mIntent = getIntent();
        if (mIntent==null && null == savedInstanceState.getString(EXTRA_POST_ID)){
            //TODO set empty view
            Timber.d("nothing here");
        } else if (mIntent != null) {
//            mUrl = mIntent.getParcelableExtra(EXTRA_URL);
            mPostId = mIntent.getStringExtra(EXTRA_POST_ID);
        }
        if (savedInstanceState!=null && savedInstanceState.getString(EXTRA_POST_ID)!= null){
//            mUrl = savedInstanceState.getParcelable(EXTRA_URL);
            mPostId = savedInstanceState.getString(EXTRA_POST_ID);
//            Timber.d("post id in savedinstancestate is " + mPostId);
//            mLayoutState = savedInstanceState.getParcelable(KEY_LAYOUTMANAGER_STATE);
        }

        mToolbar = (Toolbar)findViewById(R.id.my_toolbar);
        mCommentsRecyclerview = (RecyclerView)findViewById(R.id.comment_recyclerview);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        if (mPostId != null) {
            Timber.d("post id is " + mPostId);
            setTransitionNamePost(findViewById(R.id.post_info_container), mPostId);
            setTransitionNamePostBg(findViewById(R.id.post_info_toolbar), mPostId);
            initializePostInfo(mPostId);
        }
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (Constants.BROADCAST_COMMENTS_LOADED.equals(action)) {
                    UpdateCommentsAsyncTask updateCommentsAsyncTask = new UpdateCommentsAsyncTask();
                    updateCommentsAsyncTask.execute(mPostId);
                } else if (Constants.BROADCAST_ERROR_WHILE_LOADING_COMMENTS.equals(action)){
                    errorSnack = Snackbar.make(findViewById(R.id.activity_comments), R.string.message_error_loading_comments, Snackbar.LENGTH_INDEFINITE);
                    errorSnack.setAction(R.string.refresh, new CommentsRefreshListener());
                    errorSnack.show();
                }
            }
        };
        mIntentFilter = new IntentFilter();


        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setAutoMeasureEnabled(true);

        mCommentsRecyclerview.setLayoutManager(mLinearLayoutManager);

//        mDividerItemDecoration = new DividerItemDecoration(this, mLinearLayoutManager.getOrientation());

//        mCommentsRecyclerview.addItemDecoration(mDividerItemDecoration);

        mCommentsRecyclerview.setNestedScrollingEnabled(true);
    }

    @TargetApi(21)
    void setTransitionNamePost(View v, String postId){
        v.setTransitionName(getString(R.string.transitionName_Post)+postId);
    }

    @TargetApi(21)
    void setTransitionNamePostBg(View v, String postId){
        v.setTransitionName(getString(R.string.transitionName_PostBg)+postId);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!mPostId.equals(GetCommentsService.sLastPostId)) {
            GetCommentsService.loadCommentsForArticle(CommentsActivity.this, null, mPostId, mSortString);
        } else {
            findViewById(R.id.loadingPanel).setVisibility(View.GONE);
            initUi();
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
            mFirstVisibleChild = sp.getInt(Constants.KEY_COMMENTS_FIRST_CHILD, 0);
            mOffset = sp.getInt(Constants.KEY_COMMENTS_OFFSET, 0);
            mCommentsRecyclerview.scrollToPosition(mFirstVisibleChild);
            mCommentsRecyclerview.post(new Runnable() {
                @Override
                public void run() {
                    mCommentsRecyclerview.scrollBy(0, -mOffset);
                }
            });
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        mIntentFilter.addAction(Constants.BROADCAST_COMMENTS_LOADED);
        mIntentFilter.addAction(Constants.BROADCAST_ERROR_WHILE_LOADING_COMMENTS);
        registerReceiver(mReceiver, mIntentFilter);
    }

    @Override
    protected void onPause() {
        try {
            unregisterReceiver(mReceiver);
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("Receiver not registered")) {
                // Ignore this exception. This is exactly what is desired
                Timber.d("Tried to unregister the reciver when it's not registered");
            } else {
                // unexpected, re-throw
                throw e;
            }
        }
        super.onPause();
    }

    private void initUi(){
        Timber.d("calling initUi");
//        realmResults.addChangeListener(new RealmChangeListener<RealmResults<CommentObject>>() {
//            @Override
//            public void onChange(RealmResults<CommentObject> element) {
//                mCommentsRecyclerViewAdapter.notifyDataSetChanged();
//            }
//        });
        mCommentRecords = CommentRecord.listAll(CommentRecord.class);
        mItemCount = mCommentRecords.size();

        mCommentsRecyclerViewAdapter = new CommentRecordRecyclerAdapter(this, mCommentRecords, mListing);
        mCommentsRecyclerViewAdapter.setHasStableIds(true);

        mCommentsRecyclerview.setAdapter(mCommentsRecyclerViewAdapter);

        mLinearLayoutManager.onRestoreInstanceState(mLayoutState);
    }



    @Override
    protected void onStop() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        View firstChild = mCommentsRecyclerview.getChildAt(0);
        if (firstChild!=null) {
            int firstVisiblePosition = mCommentsRecyclerview.getChildAdapterPosition(firstChild);
            int offset = firstChild.getTop();

            Timber.d("Postition: " + firstVisiblePosition);
            Timber.d("Offset: " + offset);

            sp.edit()
                    .putInt(Constants.KEY_COMMENTS_FIRST_CHILD, firstVisiblePosition)
                    .putInt(Constants.KEY_COMMENTS_OFFSET, offset)
                    .apply();
        }
        mCommentsRecyclerview.setAdapter(null);
        super.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Timber.d("calling saveinstancestate");
        super.onSaveInstanceState(outState);
//        outState.putParcelable(EXTRA_URL, mUrl);
        outState.putString(EXTRA_POST_ID, mPostId);
//        outState.putParcelable(KEY_LAYOUTMANAGER_STATE, mListingsLinearLayoutManager.onSaveInstanceState());
    }

    public void addMoreItems(){
//        mAfter = mCommentsRecyclerViewAdapter.getData().get(mCommentsRecyclerViewAdapter.getItemCount() - 1).getAfter();
        Intent intent = new Intent(CommentsActivity.this, GetListingsService.class);
        intent.putExtra(GetListingsService.EXTRA_URL, UriGenerator.getBaseListingUri());
        intent.putExtra(GetListingsService.EXTRA_LOAD_AFTER, mAfter);
        startService(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_comments_activity, menu);
        return true;
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                supportFinishAfterTransition();
                return true;
//            case R.id.sort_top: {
//                if (item.isChecked()) item.setChecked(false);
//                else item.setChecked(true);
//                mSortString = item.getTitle().toString();
//                GetCommentsService.loadCommentsForArticle(CommentsActivity.this, null, mPostId, mSortString);
//                return true;
//            }
//            case R.id.sort_new: {
//                if (item.isChecked()) item.setChecked(false);
//                else item.setChecked(true);
//                mSortString = item.getTitle().toString();
//                GetCommentsService.loadCommentsForArticle(CommentsActivity.this, null, mPostId, mSortString);
//                return true;
//            }
//            case R.id.sort_confidence: {
//                if (item.isChecked()) item.setChecked(false);
//                else item.setChecked(true);
//                mSortString = item.getTitle().toString();GetCommentsService.loadCommentsForArticle(CommentsActivity.this, null, mPostId, mSortString);
//                return true;
//            }
//            case R.id.sort_controversial: {
//                if (item.isChecked()) item.setChecked(false);
//                else item.setChecked(true);
//                mSortString = item.getTitle().toString();GetCommentsService.loadCommentsForArticle(CommentsActivity.this, null, mPostId, mSortString);
//                return true;
//            }
//            default:
//                return super.onOptionsItemSelected(item);
            case R.id.sort_by:
                return true;
            default:
                mCommentRecords.clear();
                mCommentsRecyclerViewAdapter.notifyItemRangeRemoved(0, mItemCount);
                mItemCount=0;
                if (item.isChecked()) item.setChecked(false);
                else item.setChecked(true);
                mSortString = item.getTitle().toString();
                GetCommentsService.loadCommentsForArticle(CommentsActivity.this, null, mPostId, mSortString);
                return true;
        }
    }

    void initializePostInfo(String postId){
        voteCount_textview = (TextView)findViewById(R.id.voteCount_textview);
        author_textview = (TextView)findViewById(R.id.author_textview);
        subreddit_textview = (TextView)findViewById(R.id.subreddit_textview);
        listTitle_textview = (TextView)findViewById(R.id.listingTitle_textview);
        commentCount_textview = (TextView)findViewById(R.id.numberOfComments_textview);
        domain_textview = (TextView)findViewById(R.id.listing_domain_textview);
        timecreated_textview = (TextView)findViewById(R.id.time_elapsed_textview);
        selftext_textview = (TextView)findViewById(R.id.selftext_container);

//        RedditPost listing = mRealm.where(RedditPost.class).equalTo("mPostId", postId).findFirst();
        mListing = RedditListing.find(RedditListing.class, "m_post_id = ?", postId).get(0);

//        ((ImageButton)findViewById(R.id.openLink)).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                GeneralUtils.openLinkInEnteralBrowser(CommentsActivity.this, mListing.url);
//            }
//        });

        setToolbarPostInfo(mListing);

//        if (listing.isValid()){
//            Timber.d("null result returned");
//            return;
//        }
//        else {
//            setToolbarPostInfo(listing);
//        }

    }

    void setToolbarPostInfo(final RedditListing listing){
        voteCount_textview.setText(Integer.toString(listing.voteCount));
        author_textview.setText(listing.author);
        final String sub = listing.subreddit;
        subreddit_textview.setText(getString(R.string.format_subreddit, sub));
        subreddit_textview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                try {
                    if (!sp.getString(getString(R.string.pref_current_subreddit), null).equals(sub)){
                        sp.edit().putString(getString(R.string.pref_current_subreddit), sub).commit();
                        RedditListing.deleteAll(RedditListing.class);
                        Intent intent = new Intent(CommentsActivity.this, MainActivity.class);
                        intent.putExtra(MainActivity.EXTRA_SUBREDDIT_NAME, sub);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    }
                } catch (NullPointerException e){
                    sp.edit().putString(getString(R.string.pref_current_subreddit), sub).commit();
                    RedditListing.deleteAll(RedditListing.class);
                    Intent intent = new Intent(CommentsActivity.this, MainActivity.class);
                    intent.putExtra(MainActivity.EXTRA_SUBREDDIT_NAME, sub);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
            }
        });
        listTitle_textview.setText(GeneralUtils.returnFormattedStringFromHtml(listing.mTitle));
        commentCount_textview.setText(getString(R.string.format_numberofcomments, listing.commentsCount));
        domain_textview.setText(listing.domain);
        timecreated_textview.setText(GeneralUtils.returnFormattedTime(this, System.currentTimeMillis(), listing.timeCreated));
        if (listing.selftext_html!=null){
            selftext_textview.setText(GeneralUtils.returnFormattedStringFromHtml(listing.selftext_html));
            selftext_textview.setVisibility(View.VISIBLE);
            selftext_textview.setMovementMethod(LinkMovementMethod.getInstance());
        }
    }

    class UpdateCommentsAsyncTask extends GetCommentsAsyncTask{
        @Override
        protected void onPostExecute(List<CommentRecord> redditListings) {
//            mCommentRecords.clear();
//            mCommentRecords.addAll(redditListings);
//            mCommentsRecyclerViewAdapter.notifyItemRangeInserted(mItemCount, mCommentRecords.size()-mItemCount);
//            mItemCount = mCommentRecords.size();
            if (errorSnack!=null){
                errorSnack.dismiss();
                errorSnack = null;
            }

            mCommentRecords = CommentRecord.listAll(CommentRecord.class);
            mItemCount = mCommentRecords.size();

            mCommentsRecyclerViewAdapter = new CommentRecordRecyclerAdapter(CommentsActivity.this, mCommentRecords, mListing);
            mCommentsRecyclerViewAdapter.setHasStableIds(true);

            findViewById(R.id.loadingPanel).setVisibility(View.GONE);

            mCommentsRecyclerview.setAdapter(mCommentsRecyclerViewAdapter);

            mLinearLayoutManager.onRestoreInstanceState(mLayoutState);
        }
    }

    public void openInLocalBrowser(View v){
//        GeneralUtils.openLinkInEnteralBrowser(this, mListing.url);
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(mListing.url));
        startActivity(browserIntent);
    }

    private class CommentsRefreshListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
            GetCommentsService.loadCommentsForArticle(CommentsActivity.this, null, mPostId, mSortString);
        }
    }
}
