package com.avinashdavid.readitforreddit;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.avinashdavid.readitforreddit.MiscUtils.Constants;
import com.avinashdavid.readitforreddit.MiscUtils.GPSUtils;
import com.avinashdavid.readitforreddit.MiscUtils.GeneralUtils;
import com.avinashdavid.readitforreddit.MiscUtils.PreferenceUtils;
import com.avinashdavid.readitforreddit.NetworkUtils.GetCommentsService;
import com.avinashdavid.readitforreddit.NetworkUtils.GetListingsService;
import com.avinashdavid.readitforreddit.NetworkUtils.UriGenerator;
import com.avinashdavid.readitforreddit.PostUtils.CommentRecord;
import com.avinashdavid.readitforreddit.PostUtils.RedditListing;
import com.avinashdavid.readitforreddit.UI.CommentRecordRecyclerAdapter;
import com.avinashdavid.readitforreddit.UI.FragmentViewImage;
import com.avinashdavid.readitforreddit.UI.GetCommentsAsyncTask;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static com.avinashdavid.readitforreddit.UI.RedditPostRecyclerAdapter.imageMarkers;

/**
 * Created by avinashdavid on 3/05/17.
 * Activity to display comments for a listing in a recyclerview, along with post information on top and a floating action button to open the listing's link
 */

public class CommentsActivity extends AppCompatActivity
//        implements LoaderManager.LoaderCallbacks<Cursor>
{

//    @BindView(R.id.my_toolbar)
//    Toolbar mToolbar;
    //    @BindView(R.id.listing_recyclerview)RecyclerView mCommentsRecyclerview;
//    @BindView(R.id.collapsing_toolbar) CollapsingToolbarLayout mCollapsingToolbarLayout;


    Toolbar mToolbar;

    public static final String EXTRA_URL = "extraUrl";
    public static final String EXTRA_POST_ID = "extraPostId";
    public static final String EXTRA_COMMENT_ID = "EXTRA_COMMENT_ID";
    public static final String EXTRA_NUMBER_OF_PARENTS = "EXTRA_NUMBER_OF_PARENTS";

    private static final String KEY_SORT = "keySort";
    String mSortString;


    public static final String TAG_COMMENTS_FRAGMENT = "tagCommFrag";

    RecyclerView mCommentsRecyclerview;
    CollapsingToolbarLayout mCollapsingToolbarLayout;

    FloatingActionButton fab;

    private String mAfter;

    private int mItemCount = 0;

    private CommentRecordRecyclerAdapter mCommentsRecyclerViewAdapter;

    private List<CommentRecord> mCommentRecords;

    int mFirstVisibleChild;
    int mOffset;

    LinearLayoutManager mLinearLayoutManager;


    private Intent mIntent;
    String lastPostId;

    private String mPostId;
    private String mCommentId;
    private int mNumberOfParents;

    TextView voteCount_textview;
    TextView author_textview;
    TextView subreddit_textview;
    TextView listTitle_textview;
    TextView commentCount_textview;
    TextView domain_textview;
    TextView timecreated_textview;
    TextView selftext_textview;

    ImageView thumbnailImgView;

    Snackbar errorSnack;

    BroadcastReceiver mReceiver;
    IntentFilter mIntentFilter;

    Cursor mCursor;
    SharedPreferences sp;

    RedditListing mListing;

    int versionNum;

    private GestureDetector gestureDetector;

    public static void startCommentActivity(Context c, String postId) {
        Intent intent = new Intent(c, CommentsActivity.class);
        intent.putExtra(CommentsActivity.EXTRA_POST_ID, postId);
        c.startActivity(intent);
        ((AppCompatActivity)c).overridePendingTransition(R.anim.enter_right, R.anim.slide_to_left);
    }

    public static void startCommentActivityForThread(Context c, String postId, String commentId, int numberOfParents) {
        Intent intent = new Intent(c, CommentsActivity.class);
        intent.putExtra(CommentsActivity.EXTRA_POST_ID, postId);
        intent.putExtra(CommentsActivity.EXTRA_COMMENT_ID, commentId);
        intent.putExtra(CommentsActivity.EXTRA_NUMBER_OF_PARENTS, numberOfParents);
        c.startActivity(intent);
        ((AppCompatActivity)c).overridePendingTransition(R.anim.enter_right, R.anim.slide_to_left);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceUtils.onActivityCreateSetTheme(this);
        versionNum = Build.VERSION.SDK_INT;
//        SwipeBack.attach(this, Position.LEFT)
//                .setContentView(R.layout.activity_comments)
//                .setSwipeBackView(R.layout.swipeback);
        setContentView(R.layout.activity_comments);
        if (versionNum >=21) {
            supportPostponeEnterTransition();
        }
        gestureDetector = new GestureDetector(new SwipeGestureDetector());

        GPSUtils.setScreenName(this, "CommentsActivity");
        Timber.d("oncreate");
        mIntent = getIntent();
        if (mIntent.getStringExtra(EXTRA_POST_ID)==null && null == savedInstanceState.getString(EXTRA_POST_ID)){
            //TODO set empty view
            Timber.d("nothing here");
            finish();
        } else if (mIntent.getStringExtra(EXTRA_POST_ID) != null) {
            mPostId = mIntent.getStringExtra(EXTRA_POST_ID);
            if (mIntent.getStringExtra(EXTRA_COMMENT_ID) != null) {
                mCommentId = mIntent.getStringExtra(EXTRA_COMMENT_ID);
                mNumberOfParents = mIntent.getIntExtra(EXTRA_NUMBER_OF_PARENTS, -1);
            }
        }

        if (savedInstanceState!=null && savedInstanceState.getString(EXTRA_POST_ID)!= null){
            mPostId = savedInstanceState.getString(EXTRA_POST_ID);
            if (savedInstanceState.getString(EXTRA_COMMENT_ID) != null) {
                mCommentId = savedInstanceState.getString(EXTRA_COMMENT_ID);
                mNumberOfParents = savedInstanceState.getInt(EXTRA_NUMBER_OF_PARENTS, -1);
            }
        }

        mToolbar = (Toolbar)findViewById(R.id.my_toolbar);
        mCommentsRecyclerview = (RecyclerView)findViewById(R.id.comment_recyclerview);
        sp = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());

        setSupportActionBar(mToolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        ArrayList<View> viewsToChange = new ArrayList<>();
        viewsToChange.add(findViewById(R.id.main_appbar));
        viewsToChange.add(findViewById(R.id.collapsing_toolbar));
        viewsToChange.add(findViewById(R.id.post_info_toolbar));
        viewsToChange.add(findViewById(R.id.my_toolbar));
        PreferenceUtils.changeToolbarColor(this, viewsToChange);

        fab = (FloatingActionButton)findViewById(R.id.comments_fab);

        if (mPostId != null) {
//            if (versionNum>=21) {
//                setTransitionNamePost(findViewById(R.id.post_info_container), "");
////                setTransitionNamePostBg(findViewById(R.id.post_info_toolbar), "");
//            }
            if  (mPostId.contains("t3_")) {
                mPostId = mPostId.substring(3);
            }
            initializePostInfo(mPostId);
        }
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (Constants.BROADCAST_COMMENTS_LOADED.equals(action)) {
                    UpdateCommentsAsyncTask updateCommentsAsyncTask = new UpdateCommentsAsyncTask();
                    updateCommentsAsyncTask.execute(mPostId);
                    mListing = RedditListing.find(RedditListing.class, "m_post_id = ?", mPostId).get(0);

                    setToolbarPostInfo(mListing);
                    mCommentsRecyclerview.addOnScrollListener(new RecyclerView.OnScrollListener(){
                        @Override
                        public void onScrolled(RecyclerView recyclerView, int dx, int dy){
                            if (dy > 10)
                                fab.hide();
                            else if (dy < -10)
                                fab.show();
                        }
                    });
                } else if (Constants.BROADCAST_ERROR_WHILE_LOADING_COMMENTS.equals(action)){
                    errorSnack = PreferenceUtils.getThemedSnackbar(CommentsActivity.this, R.id.activity_comments, getString(R.string.message_error_loading_comments) + ": " + intent.getStringExtra(Constants.KEY_NETWORK_REQUEST_ERROR), Snackbar.LENGTH_INDEFINITE);
                    errorSnack.setAction(R.string.refresh, new CommentsRefreshListener());
                    errorSnack.show();
                }
            }
        };
        mIntentFilter = new IntentFilter();


        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setAutoMeasureEnabled(true);

        mCommentsRecyclerview.setLayoutManager(mLinearLayoutManager);

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
        lastPostId = sp.getString(getString(R.string.pref_last_post), null);
        String lastCommentThreadId = sp.getString(getString(R.string.pref_last_comment_thread), null);
        if (!mPostId.equals(lastPostId) && mCommentId == null) {
            CommentRecord.deleteAll(CommentRecord.class);
            SharedPreferences.Editor editor = sp.edit();
            editor.putInt(Constants.KEY_COMMENTS_FIRST_CHILD, 0);
            editor.putInt(Constants.KEY_COMMENTS_OFFSET, 0);
            editor.apply();
            GetCommentsService.loadCommentsForArticle(CommentsActivity.this, null, mPostId, mSortString);
        } else if (mCommentId != null) {
            if (lastCommentThreadId != null) {
                if (lastCommentThreadId.equals(mCommentId)) {
                    //RESTORE STATE
                } else {
                    GetCommentsService.loadCommentsInThread(this, mPostId, mCommentId, mNumberOfParents);
                }
            } else GetCommentsService.loadCommentsInThread(this, mPostId, mCommentId, mNumberOfParents);
        } else {
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
            (findViewById(R.id.loadingPanel)).setVisibility(View.GONE);
            mCommentsRecyclerview.addOnScrollListener(new RecyclerView.OnScrollListener(){
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy){
                    if (dy > 0)
                        fab.hide();
                    else if (dy < 0)
                        fab.show();
                }
            });
//            mCommentsRecyclerview.setOnTouchListener(new View.OnTouchListener() {
//                @Override
//                public boolean onTouch(View v, MotionEvent event) {
//                    switch(event.getAction()) {
//                        case MotionEvent.ACTION_DOWN:
//                            _downX = event.getX();
//                            _downY = event.getY();
//
//                        case MotionEvent.ACTION_UP:
//                            float deltaX = event.getX() - _downX;
//                            float deltaY = event.getY() - _downY;
//
//                            if(Math.abs(deltaX) > SWIPE_THRESHOLD && deltaX < 0 && Math.abs(deltaY) < SWIPE_THRESHOLD_VERTICAL)
//                                onBackPressed();
//                    }
//
//                    return true;
//                }
//            });
        }
//        setTransitionNamePost(findViewById(R.id.post_info_container), null);
//        setTransitionNamePostBg(findViewById(R.id.post_info_toolbar), null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mIntentFilter.addAction(Constants.BROADCAST_COMMENTS_LOADED);
        mIntentFilter.addAction(Constants.BROADCAST_ERROR_WHILE_LOADING_COMMENTS);
        registerReceiver(mReceiver, mIntentFilter);
//        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
    }

    @Override
    protected void onPause() {
//        setTransitionNamePost(findViewById(R.id.post_info_container), null);
//        setTransitionNamePostBg(findViewById(R.id.post_info_toolbar), null);
        try {
            unregisterReceiver(mReceiver);
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("Receiver not registered")) {
                // Ignore this exception. This is exactly what is desired
                Timber.e("Tried to unregister the reciver when it's not registered");
            } else {
                // unexpected, re-throw
                throw e;
            }
        }
//        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
        super.onPause();
    }

    @Override
    public void onBackPressed() {
//        if (versionNum>=21) {
//            setTransitionNamePost(findViewById(R.id.post_info_container), null);
//            setTransitionNamePostBg(findViewById(R.id.post_info_toolbar), null);
//        }
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
//        super.onBackPressed();
    }

    private void setCommentScroll(int firstChildPosition, int offset){
        mCommentsRecyclerview.scrollToPosition(firstChildPosition);
        mCommentsRecyclerview.scrollBy(0, -offset);
    }

    private void saveCommentScroll(int firstChildPosition, int offset){
        sp.edit()
                .putInt(Constants.KEY_COMMENTS_FIRST_CHILD, firstChildPosition)
                .putInt(Constants.KEY_COMMENTS_OFFSET, offset)
                .apply();
    }

    public void commentScrollToTop(View v){
        saveCommentScroll(0,0);
//        setCommentScroll(0,0);
        mCommentsRecyclerview.smoothScrollToPosition(0);

    }

    private void initUi(){
//        realmResults.addChangeListener(new RealmChangeListener<RealmResults<CommentObject>>() {
//            @Override
//            public void onChange(RealmResults<CommentObject> element) {
//                mCommentsRecyclerViewAdapter.notifyDataSetChanged();
//            }
//        });
        mCommentRecords = CommentRecord.listAll(CommentRecord.class);
        mItemCount = mCommentRecords.size();

        mCommentsRecyclerViewAdapter = new CommentRecordRecyclerAdapter(this, mCommentRecords, mListing, false);
        mCommentsRecyclerViewAdapter.setHasStableIds(true);

        mCommentsRecyclerview.setAdapter(mCommentsRecyclerViewAdapter);
    }



    @Override
    protected void onStop() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        View firstChild = mCommentsRecyclerview.getChildAt(0);
        if (firstChild!=null) {
            int firstVisiblePosition = mCommentsRecyclerview.getChildAdapterPosition(firstChild);
            int offset = firstChild.getTop();



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
        super.onSaveInstanceState(outState);
//        outState.putParcelable(EXTRA_URL, mUrl);
        outState.putString(EXTRA_POST_ID, mPostId);
        outState.putString(EXTRA_COMMENT_ID, mCommentId);
        outState.putInt(EXTRA_NUMBER_OF_PARENTS, mNumberOfParents);
//        outState.putParcelable(KEY_LAYOUTMANAGER_STATE, mListingsLinearLayoutManager.onSaveInstanceState());
    }

    public void addMoreItems(){
//        mAfter = mCommentsRecyclerViewAdapter.getData().get(mCommentsRecyclerViewAdapter.getItemCount() - 1).getAfter();
        Intent intent = new Intent(CommentsActivity.this, GetListingsService.class);
        intent.putExtra(GetListingsService.EXTRA_URL, UriGenerator.getBaseUnauthUrl());
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
//                if (versionNum>=21) {
//                    setTransitionNamePost(findViewById(R.id.post_info_container), null);
//                    setTransitionNamePostBg(findViewById(R.id.post_info_toolbar), null);
//                    finishAfterTransition();
//                } else {
//                    supportFinishAfterTransition();
//                }
                onBackPressed();
                return true;
            case R.id.refresh_comments:
                refreshComments();
                return true;
            case R.id.share_activity_comments:
                return true;
            case R.id.share_link:
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_SUBJECT, mListing.mTitle);
                sendIntent.putExtra(Intent.EXTRA_TEXT, mListing.url);
                sendIntent.setType("text/plain");
                startActivity(sendIntent);
                return true;
            case R.id.share_comments:
                sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.comments_share_title));
                sendIntent.putExtra(Intent.EXTRA_TEXT, (UriGenerator.getShareableUriComments(mPostId)).toString());
                sendIntent.setType("text/plain");
                startActivity(sendIntent);
                return true;
            case R.id.sort_by:
                return true;
            default:
//                mCommentRecords.clear();
//                mCommentsRecyclerViewAdapter.notifyItemRangeRemoved(0, mItemCount);
                mItemCount=0;
                if (item.isChecked()) item.setChecked(false);
                else item.setChecked(true);
                mSortString = item.getTitle().toString();
                refreshComments();
                return true;
        }
    }

    void refreshComments(){
        saveCommentScroll(0,0);
        mCommentsRecyclerview.setAdapter(null);
        findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
        GetCommentsService.loadCommentsForArticle(CommentsActivity.this, null, mPostId, mSortString);
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
        thumbnailImgView = (ImageView)findViewById(R.id.post_thumbnail);

        List<RedditListing> listings = RedditListing.find(RedditListing.class, "m_post_id = ?", postId);
        if (listings.size() > 0) {
            mListing = RedditListing.find(RedditListing.class, "m_post_id = ?", postId).get(0);

            setToolbarPostInfo(mListing);
        }
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
        String thumbnailUrl = listing.thumbnailUrl;
        if (thumbnailUrl.length()==0 || thumbnailUrl.equals("self") || thumbnailUrl.equals("default") || thumbnailUrl.equals("nsfw")) {
            findViewById(R.id.imageContainer).setVisibility(View.GONE);
        } else {
            Glide.with(this).load(listing.thumbnailUrl).into(thumbnailImgView);
            boolean clickable = false;
            for (int i=0; i<imageMarkers.length; i++){
                if (listing.url.contains(imageMarkers[i])){
                    clickable = true;
                }
            }
            if (!clickable){
                findViewById(R.id.imgMarker).setVisibility(View.GONE);
            } else {
                thumbnailImgView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FragmentViewImage fragmentViewImage = FragmentViewImage.getImageViewFragment(listing.url);
                        FragmentManager fragmentManager = (CommentsActivity.this).getSupportFragmentManager();
                        FragmentTransaction ft = fragmentManager.beginTransaction();
                        Fragment prev = fragmentManager.findFragmentByTag(FragmentViewImage.TAG_IMAGE_FRAGMENT);
                        if (prev != null) {
                            ft.remove(prev);
                        }
                        ft.addToBackStack(null);

                        fragmentViewImage.show(ft, FragmentViewImage.TAG_IMAGE_FRAGMENT);
                    }
                });
            }
        }
        if (versionNum>=21) {
            supportStartPostponedEnterTransition();
        }
    }

    class UpdateCommentsAsyncTask extends GetCommentsAsyncTask{
        @Override
        protected void onPostExecute(List<CommentRecord> redditListings) {
            if (errorSnack!=null){
                errorSnack.dismiss();
                errorSnack = null;
            }

            mCommentRecords = CommentRecord.listAll(CommentRecord.class);
            mItemCount = mCommentRecords.size();

            mCommentsRecyclerViewAdapter = new CommentRecordRecyclerAdapter(CommentsActivity.this, mCommentRecords, mListing, mCommentId!=null);
            mCommentsRecyclerViewAdapter.setHasStableIds(true);

            findViewById(R.id.loadingPanel).setVisibility(View.GONE);

//            DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(CommentsActivity.this,
//                    mLinearLayoutManager.getOrientation());
//            mCommentsRecyclerview.addItemDecoration(dividerItemDecoration);

            mCommentsRecyclerview.setAdapter(mCommentsRecyclerViewAdapter);

            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(CommentsActivity.this);
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

    public void openInLocalBrowser(View v){
//        GeneralUtils.openLinkInEnteralBrowser(this, mListing.url);
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(mListing.url));
        startActivity(browserIntent);
    }

    private class CommentsRefreshListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            refreshComments();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (gestureDetector.onTouchEvent(event)) {
            return true;
        }
        return super.onTouchEvent(event);
    }

    private void onLeftSwipe() {
        onBackPressed();
    }

    private void onRightSwipe() {
        // Do something
    }

    // Private class for gestures
    private class SwipeGestureDetector extends GestureDetector.SimpleOnGestureListener {
        // Swipe properties, you can change it to make the swipe
        // longer or shorter and speed
        private static final int SWIPE_MIN_DISTANCE = 120;
        private static final int SWIPE_MAX_OFF_PATH = 200;
        private static final int SWIPE_THRESHOLD_VELOCITY = 200;

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            try {
                float diffAbs = Math.abs(e1.getY() - e2.getY());
                float diff = e1.getX() - e2.getX();

                if (diffAbs > SWIPE_MAX_OFF_PATH) {
                    return false;
                }

                // Left swipe
                if (diff > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    CommentsActivity.this.onLeftSwipe();

                    // Right swipe
                } else if (-diff > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    CommentsActivity.this.onRightSwipe();
                }
            } catch (Exception e) {
                Log.e("YourActivity", "Error on gestures");
            }
            return false;
        }
    }
}

