package com.avinashdavid.readitforreddit;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.method.LinkMovementMethod;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.avinashdavid.readitforreddit.MiscUtils.Constants;
import com.avinashdavid.readitforreddit.MiscUtils.GPSUtils;
import com.avinashdavid.readitforreddit.MiscUtils.GeneralUtils;
import com.avinashdavid.readitforreddit.MiscUtils.PreferenceUtils;
import com.avinashdavid.readitforreddit.NetworkUtils.CheckNewSubredditService;
import com.avinashdavid.readitforreddit.NetworkUtils.GetCommentsService;
import com.avinashdavid.readitforreddit.NetworkUtils.GetListingsService;
import com.avinashdavid.readitforreddit.NetworkUtils.GetSubredditInfoService;
import com.avinashdavid.readitforreddit.NetworkUtils.GetSubredditsService;
import com.avinashdavid.readitforreddit.NetworkUtils.UriGenerator;
import com.avinashdavid.readitforreddit.OAuth.GetAuthActivity;
import com.avinashdavid.readitforreddit.PostUtils.CommentRecord;
import com.avinashdavid.readitforreddit.PostUtils.RedditListing;
import com.avinashdavid.readitforreddit.SubredditUtils.SubredditObject;
import com.avinashdavid.readitforreddit.UI.AddSubredditDialogFragment;
import com.avinashdavid.readitforreddit.UI.AppSettingsActivity;
import com.avinashdavid.readitforreddit.UI.CommentRecordRecyclerAdapter;
import com.avinashdavid.readitforreddit.UI.GetCommentsAsyncTask;
import com.avinashdavid.readitforreddit.UI.GoToDialogFragment;
import com.avinashdavid.readitforreddit.UI.QuitDialog;
import com.avinashdavid.readitforreddit.UI.RedditPostRecyclerAdapter;
import com.avinashdavid.readitforreddit.UI.SearchDialogFragment;
import com.avinashdavid.readitforreddit.User.GetLoggedInUserAbout;
import com.avinashdavid.readitforreddit.User.LoggedInUser;
import com.avinashdavid.readitforreddit.User.UserHistoryActivity;
import com.orm.SugarRecord;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import timber.log.Timber;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static com.orm.SugarRecord.deleteAll;
import static com.orm.SugarRecord.listAll;

/**
 * Created by avinashdavid on 3/01/17.
 * Main Activity to display Reddit listings, with a navigation drawer, a right drawer to display subreddit information, and a menu to subscribe to a subreddit and change application theme
 */

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
        SwipeRefreshLayout.OnRefreshListener,
        GoToDialogFragment.GoToDialogListener,
        SearchDialogFragment.SearchDialogListener,
        AddSubredditDialogFragment.AddSubDialogListener,
        RedditPostRecyclerAdapter.ScrollListener{

    int itemCount=20;

    private String mAccessToken = null;

    public static final String EXTRA_SUBREDDIT_NAME = "extraSubName";

    private static final String KEY_SUBREDDIT_NAME = "sr_name_SI";
    private static final String KEY_AFTER = "after_SI";
    private static final String KEY_SORT = "sort_SI";
    private static final String KEY_SEARCH_STRING = "searchStr_SI";
    private static final String KEY_RESTRICT_SEARCH = "restrictSr_SI";
    private static final String KEY_LAYOUTMANAGER_STATE = "KeyForLayoutManagerState";
    private static final String KEY_SELECTED_SORT_ID = "keyselectedSort";

    private static final String KEY_COMMENTS_POSTID = "keyPostId";

    private static final String TAG_GO_TO_SUBREDDIT = "goToSub";
    private static final String TAG_SEARCH_POSTS = "searchPosts";
    private static final String TAG_ADD_SUBREDDIT = "addSubreddit";


    public static final int CODE_MANAGE_SUBREDDITS = 7;

    private static final int CODE_APP_SETTINGS = 8;

    private static final int CODE_LOGIN_USER = 9;

    public static int CLICKED_ITEM_POSITION = -1;

    Toolbar mToolbar;
    RecyclerView mListingRecyclerview;
    CollapsingToolbarLayout mCollapsingToolbarLayout;

    NavigationView mNavigationView;
    DrawerLayout mDrawerLayout;

    private Realm mRealm;

    Snackbar loadingSnack;

    private String mAfter;

    public boolean haveToReloadSubreddits;

    private String mSubredditString;
    private String mSortString;
    private String mSearchQueryString;

    LinearLayoutManager mListingsLinearLayoutManager;

    List<RedditListing> mRedditListings;

    int mFirstChildPosition;
    int mOffset;

    public boolean mRestrictSearchBoolean = false;

    public static boolean usingTabletLayout = false;

    private RedditPostRecyclerAdapter mListingRecyclerAdapter;

    private SwipeRefreshLayout mSwipeRefreshLayout;

    RealmResults<SubredditObject> mSubredditObjectRealmResults;


    SharedPreferences mApplicationSharedPreferences;
    Parcelable mLayoutState;

    BroadcastReceiver mPostsBroadcastReceiver;
    IntentFilter mPostsIntentFilter;

    BroadcastReceiver mAddSubBroadcastReceiver;
    IntentFilter mAddSubIntentFilter;

    BroadcastReceiver mSubredditInfoReceiver;
    IntentFilter mSubredditInfoIntentFilter;

    BroadcastReceiver mLoggedInUserReceiver;
    IntentFilter mLoggedInUserIntentFilter;

    int selectedSortID = R.id.sort_hot;


    /**
     * COMMENTS VARIABLES
     */
    RecyclerView mCommentsRecyclerview;
    CommentRecordRecyclerAdapter mCommentRecordRecyclerAdapter;
    LinearLayoutManager mCommentsLinearLayoutManager;
    List<CommentRecord> mCommentRecords;
    int mItemCount;
    private String mPostId;
    RedditListing mCommentsRedditListing;
    BroadcastReceiver mCommentsLoadedBroadcastReceiver;
    IntentFilter mCommentsIntentFilter;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceUtils.onActivityCreateSetTheme(this);
        setContentView(R.layout.drawer_main_activity);
        GPSUtils.setScreenName(this, "MainActivityPaid");

        mApplicationSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        loadingSnack = PreferenceUtils.getThemedSnackbar(this, R.id.activity_main, getString(R.string.message_loading_more_posts), Snackbar.LENGTH_INDEFINITE);
        usingTabletLayout = (findViewById(R.id.comment_recyclerview)!=null);
        mApplicationSharedPreferences.edit().putBoolean(getString(R.string.pref_boolean_use_tablet_layout), usingTabletLayout).apply();

        mAccessToken = mApplicationSharedPreferences.getString(GetAuthActivity.KEY_ACCESS_TOKEN, null);

        if (savedInstanceState!=null) {
            mSubredditString = savedInstanceState.getString(KEY_SUBREDDIT_NAME);
            mSortString = savedInstanceState.getString(KEY_SORT);
            mSearchQueryString = savedInstanceState.getString(KEY_SEARCH_STRING);
            mRestrictSearchBoolean = savedInstanceState.getBoolean(KEY_RESTRICT_SEARCH);
            mAfter = savedInstanceState.getString(KEY_AFTER);
            mLayoutState = savedInstanceState.getParcelable(KEY_LAYOUTMANAGER_STATE);
            if (usingTabletLayout){
                mPostId = savedInstanceState.getString(KEY_COMMENTS_POSTID);
            }
            selectedSortID = savedInstanceState.getInt(KEY_SELECTED_SORT_ID);
            CLICKED_ITEM_POSITION = savedInstanceState.getInt("TEST");
        } else {
            mSubredditString = mApplicationSharedPreferences.getString(getString(R.string.pref_current_subreddit), null);
            mSearchQueryString = mApplicationSharedPreferences.getString(getString(R.string.pref_search_string), null);
        }

        if (getIntent().getStringExtra(CommentsActivity.EXTRA_POST_ID)!=null){
            mPostId = getIntent().getStringExtra(CommentsActivity.EXTRA_POST_ID);
        }

        haveToReloadSubreddits = mApplicationSharedPreferences.getBoolean(getString(R.string.pref_reload_subreddits), true);

        mToolbar = (Toolbar)findViewById(R.id.my_toolbar);
        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(R.attr.menu_icon, typedValue, true);
        mToolbar.setNavigationIcon(typedValue.resourceId);
        mCollapsingToolbarLayout = (CollapsingToolbarLayout)findViewById(R.id.collapsing_toolbar);
        setSupportActionBar(mToolbar);


        mDrawerLayout = (DrawerLayout)findViewById(R.id.my_drawer_layout);
        mNavigationView = (NavigationView)findViewById(R.id.main_navigationview);
        View sidebarDrawer = findViewById(R.id.sidebar_header);

        mListingRecyclerview = (RecyclerView)findViewById(R.id.listing_recyclerview);
        mSwipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.swipeRefreshLayout);

        ArrayList<View> viewsToChange = new ArrayList<>();
        viewsToChange.add(findViewById(R.id.main_appbar));
        viewsToChange.add(findViewById(R.id.collapsing_toolbar));
        viewsToChange.add(findViewById(R.id.my_toolbar));
        viewsToChange.add(mNavigationView.getHeaderView(0).findViewById(R.id.navdrawer_header));
        viewsToChange.add(sidebarDrawer);

        PreferenceUtils.changeToolbarColor(this, viewsToChange);

        startRealm();


//        mSubredditObjectRealmResults = mRealm.where(SubredditObject.class).findAll().sort("subredditName");
        String rawString = mApplicationSharedPreferences.getString(getString(R.string.pref_subreddit_list), "");
        setSubredditsInNavigationView(rawString, mAccessToken);
        setSidebarText((TextView)findViewById(R.id.sidebar_text));

        mNavigationView.setNavigationItemSelectedListener(this);

        setCheckedNavigationItem(getSavedNavigationItemId());


        mSwipeRefreshLayout.setOnRefreshListener(this);


//        RecyclerView.ItemDecoration mDividerItemDecoration = new DividerItemDecoration(this, mListingsLinearLayoutManager.getOrientation());
//
//        mListingRecyclerview.addItemDecoration(mDividerItemDecoration);

        mPostsBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (loadingSnack.isShown()){
                    loadingSnack.dismiss();
                }
                String action = intent.getAction();
                if (Constants.BROADCAST_POSTS_LOADED.equals(action)) {
                    if (mAfter == null) {
                        refreshUI(false);
                    } else {
                        mListingRecyclerAdapter.mRedditListings.clear();
                        mListingRecyclerAdapter.mRedditListings.addAll(RedditListing.listAll(RedditListing.class));
//                    findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                        mListingRecyclerAdapter.notifyItemRangeInserted(itemCount, 20);
                        itemCount = mRedditListings.size();
                    }
                } else if (Constants.BROADCAST_ERROR_WHILE_RETREIVING_POSTS.equals(action)){
                    mSwipeRefreshLayout.setRefreshing(false);
                    Snackbar mySnackbar = PreferenceUtils.getThemedSnackbar(MainActivity.this, R.id.activity_main, getString(R.string.error_while_loading_posts), Snackbar.LENGTH_INDEFINITE);
                    mySnackbar.setAction(R.string.retry, new MyRefreshListener());
                    mySnackbar.show();
                } else if (Constants.BROADCAST_SUBREDDITS_LOADED.equals(action)){
                    setSubredditsInNavigationView("", mAccessToken);
                } else if (Constants.BROADCAST_RANDOM_SUBREDDIT_POSTS_LOADED.equals(action)){
                    refreshUI(true);
                }
            }
        };
        mPostsIntentFilter = new IntentFilter();

        mAddSubBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                Activity activity = MainActivity.this;
                if (action.equals(Constants.BROADCAST_SUBREDDIT_ADDED)) {
                    mApplicationSharedPreferences.edit().putBoolean(getString(R.string.pref_reload_subreddits), true).commit();
                    setSubredditsInNavigationView("", mAccessToken);
                    stopAllReceivers();
                    activity.finish();
                    Intent intent1 = new Intent(activity, activity.getClass());
                    intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    activity.startActivity(intent1);
                    PreferenceUtils.getThemedSnackbar(activity, R.id.activity_main, getString(R.string.message_subreddit_added), Snackbar.LENGTH_LONG).show();
                } else if (action.equals(Constants.BROADCAST_SUBREDDIT_PRESENT)){
                    PreferenceUtils.getThemedSnackbar(activity, R.id.activity_main, getString(R.string.message_subreddit_present), Snackbar.LENGTH_LONG).show();
                } else if (action.equals(Constants.BROADCAST_SUBREDDIT_BANNED)){
                    PreferenceUtils.getThemedSnackbar(activity, R.id.activity_main, getString(R.string.message_subreddit_banned), Snackbar.LENGTH_LONG).show();
                } else if (action.equals(Constants.BROADCAST_NO_SUCH_SUBREDDIT)){
                    PreferenceUtils.getThemedSnackbar(activity, R.id.activity_main, getString(R.string.message_subreddit_not_valid), Snackbar.LENGTH_LONG).show();
                }
            }
        };
        mAddSubIntentFilter = new IntentFilter();

        mSubredditInfoReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                TextView sidebarText = (TextView)findViewById(R.id.sidebar_text);
                String action = intent.getAction();

                if (action.equals(Constants.BROADCAST_SIDEBAR)){
                    setSidebarText(sidebarText);
                } else if (action.equals(Constants.BROADCAST_SIDEBAR_ERROR)){
                    sidebarText.setText(getString(R.string.error_loading_sidebar));
                }
            }
        };
        mSubredditInfoIntentFilter = new IntentFilter();

        mLoggedInUserReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();

                if (action.equals(Constants.BROADCAST_GET_LOGGEDIN_USER_SUCCESS)){
                    LoggedInUser loggedInUser = LoggedInUser.Companion.getCurrentLoggedInUser();
                    try {
                        ((TextView)findViewById(R.id.tvUserHandle_Drawer)).setText(loggedInUser.getUsername());
                        findViewById(R.id.tvDefaultDrawerHeader).setVisibility(View.GONE);
                        findViewById(R.id.llDrawerUserInformation).setVisibility(View.VISIBLE);
                        mNavigationView.getMenu().removeItem(R.id.itLogin);
                    } catch (NullPointerException e) {
                        findViewById(R.id.tvDefaultDrawerHeader).setVisibility(View.VISIBLE);
                        findViewById(R.id.llDrawerUserInformation).setVisibility(View.GONE);
                        mNavigationView.getMenu().removeItem(R.id.itProfile);
                        mNavigationView.getMenu().removeItem(R.id.itLogOut);
                    }
                } else if (action.equals(Constants.BROADCAST_GET_LOGGEDIN_USER_ERROR)){
                    findViewById(R.id.tvDefaultDrawerHeader).setVisibility(View.VISIBLE);
                    findViewById(R.id.llDrawerUserInformation).setVisibility(View.GONE);
                    mNavigationView.getMenu().removeItem(R.id.itProfile);
                    mNavigationView.getMenu().removeItem(R.id.itLogOut);
//                    sidebarText.setText(getString(R.string.error_loading_sidebar));
                }
            }
        };
        mLoggedInUserIntentFilter = new IntentFilter(Constants.BROADCAST_GET_LOGGEDIN_USER_SUCCESS);
        mLoggedInUserIntentFilter.addAction(Constants.BROADCAST_GET_LOGGEDIN_USER_ERROR);

        if (usingTabletLayout){
            mCommentsRecyclerview = (RecyclerView)findViewById(R.id.comment_recyclerview);
            mCommentsLinearLayoutManager = new LinearLayoutManager(this);
            mCommentsLinearLayoutManager.setAutoMeasureEnabled(true);

            mCommentsRecyclerview.setLayoutManager(mCommentsLinearLayoutManager);
            mCommentsRecyclerview.setNestedScrollingEnabled(true);

            RecyclerView.ItemDecoration mCommDividerItemDecoration = new DividerItemDecoration(this, mCommentsLinearLayoutManager.getOrientation());

            mCommentsRecyclerview.addItemDecoration(mCommDividerItemDecoration);

            mCommentsLoadedBroadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    mPostId = mApplicationSharedPreferences.getString(getString(R.string.pref_last_post), null);
                    UpdateCommentsPaneAsyncTask updateCommentsPaneAsyncTask = new UpdateCommentsPaneAsyncTask();
                    updateCommentsPaneAsyncTask.execute(mPostId);
                }
            };

            mCommentsIntentFilter = new IntentFilter();
            mItemCount = 0;
        }

        if (getIntent().getStringExtra(EXTRA_SUBREDDIT_NAME)!=null){
            mSwipeRefreshLayout.setRefreshing(true);
            onRefresh();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        try {
            initUi(mSubredditString, mSearchQueryString, mSortString, mRestrictSearchBoolean, false);
        } finally {
            mFirstChildPosition = mApplicationSharedPreferences.getInt(Constants.KEY_POSTS_SCROLL_POSITION, 0);
            mOffset = mApplicationSharedPreferences.getInt(Constants.KEY_POSTS_OFFSET, 0);
            setMainScroll(mFirstChildPosition, mOffset);
        }
        if (usingTabletLayout){
            if (mPostId==null){
                return;
            }
            if (!mPostId.equals(mApplicationSharedPreferences.getString(getString(R.string.pref_last_post),null))) {
                GetCommentsService.loadCommentsForArticle(MainActivity.this, null, mPostId, mSortString);
            } else {
                initCommentsUi();
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
                int commentFirstChild = sp.getInt(Constants.KEY_COMMENTS_FIRST_CHILD, 0);
                final int commentOffset = sp.getInt(Constants.KEY_COMMENTS_OFFSET, 0);
                setCommentScroll(commentFirstChild, commentOffset);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        startAllReceivers();
        GetLoggedInUserAbout.Companion.loadLoggedInUserInformation(this);
//        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
    }

    @Override
    protected void onPause() {
        stopAllReceivers();
        View firstChild = mListingRecyclerview.getChildAt(0);
        if (firstChild!=null) {
            int firstVisiblePosition = mListingRecyclerview.getChildAdapterPosition(firstChild);
            int offset = firstChild.getTop();

            saveMainScroll(firstVisiblePosition, offset);
        }

        if (usingTabletLayout){
            View firstComment = mCommentsRecyclerview.getChildAt(0);
            if (firstComment!=null){
                int firstVisiblePosition = mCommentsRecyclerview.getChildAdapterPosition(firstChild);
                int offset = 0;
                try {
                    offset = firstChild.getTop();
                } catch (NullPointerException e){
                    Timber.e(e);
                }

                saveCommentScroll(firstVisiblePosition, offset);
            }
        }

        super.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_SUBREDDIT_NAME, mSubredditString);
        outState.putString(KEY_SORT, mSortString);
        outState.putString(KEY_SEARCH_STRING, mSearchQueryString);
        outState.putString(KEY_AFTER, mAfter);
        outState.putBoolean(KEY_RESTRICT_SEARCH, mRestrictSearchBoolean);
        outState.putInt("TEST", CLICKED_ITEM_POSITION);
        outState.putParcelable(KEY_LAYOUTMANAGER_STATE, mListingsLinearLayoutManager.onSaveInstanceState());
        if (usingTabletLayout){
            outState.putString(KEY_COMMENTS_POSTID, mPostId);
        }
        outState.putInt(KEY_SELECTED_SORT_ID, selectedSortID);
    }

    @Override
    public void onBackPressed() {
        openQuitDialog();
    }

    private void setMainScroll(int firstChildPosition, int offset){
        mListingRecyclerview.scrollToPosition(firstChildPosition);
        mListingRecyclerview.scrollBy(0, -offset);
    }

    private void saveMainScroll(int firstChildPosition, int offset){
        mApplicationSharedPreferences.edit()
                .putInt(Constants.KEY_POSTS_SCROLL_POSITION, firstChildPosition)
                .putInt(Constants.KEY_POSTS_OFFSET, offset)
                .apply();
    }

    public void mainScrollToTop(View v){
        saveMainScroll(0,0);
//        setMainScroll(0,0);
        mListingRecyclerview.smoothScrollToPosition(0);
    }

    private void setCommentScroll(int firstChildPosition, int offset){
        mCommentsRecyclerview.scrollToPosition(firstChildPosition);
        mCommentsRecyclerview.scrollBy(0, -offset);
    }

    private void saveCommentScroll(int firstChildPosition, int offset){
        mApplicationSharedPreferences.edit()
                .putInt(Constants.KEY_COMMENTS_FIRST_CHILD, firstChildPosition)
                .putInt(Constants.KEY_COMMENTS_OFFSET, offset)
                .apply();
    }

    private void initUi(@Nullable String subredditString, @Nullable String searchString, @Nullable String sortString, boolean restrictSr, boolean forceRefresh){

        if (searchString==null) {
            mCollapsingToolbarLayout.setTitle(mSubredditString == null ? getString(R.string.frontpage) : mSubredditString);
        } else {
            mCollapsingToolbarLayout.setTitle(getString(R.string.format_search_results, searchString));
        }

        mAfter = null;

        Timber.e(Integer.toString(CLICKED_ITEM_POSITION));
        mRedditListings = getPosts(subredditString, searchString, null, sortString, restrictSr, forceRefresh);

        mListingRecyclerAdapter = new RedditPostRecyclerAdapter(this, mRedditListings);

        mListingRecyclerAdapter.setHasStableIds(true);
        mListingRecyclerview.setAdapter(mListingRecyclerAdapter);

        mListingsLinearLayoutManager = new LinearLayoutManager(this);
        mListingsLinearLayoutManager.setAutoMeasureEnabled(true);

        mListingRecyclerview.setLayoutManager(mListingsLinearLayoutManager);
        mListingRecyclerview.setNestedScrollingEnabled(true);

        mFirstChildPosition = mApplicationSharedPreferences.getInt(Constants.KEY_POSTS_SCROLL_POSITION, 0);
        mOffset = mApplicationSharedPreferences.getInt(Constants.KEY_POSTS_OFFSET, 0);
        mListingRecyclerview.scrollToPosition(mFirstChildPosition);
        mListingRecyclerview.scrollBy(0, -mOffset);
//        supportPostponeEnterTransition();
//
//        try {
//        mRedditListings = getPosts(subredditString, searchString, null, sortString, restrictSr, forceRefresh);
//
//        mListingRecyclerAdapter = new RedditPostRecyclerAdapter(this, mRedditListings);
//
//        mListingRecyclerAdapter.setHasStableIds(true);
//        mListingRecyclerview.setAdapter(mListingRecyclerAdapter);
//
//        mListingsLinearLayoutManager = new LinearLayoutManager(this);
//        mListingsLinearLayoutManager.setAutoMeasureEnabled(true);
//
//        mListingRecyclerview.setLayoutManager(mListingsLinearLayoutManager);
//        mListingRecyclerview.setNestedScrollingEnabled(true);
//
//        mFirstChildPosition = mApplicationSharedPreferences.getInt(Constants.KEY_POSTS_SCROLL_POSITION, 0);
//        mOffset = mApplicationSharedPreferences.getInt(Constants.KEY_POSTS_OFFSET, 0);
//        mListingRecyclerview.scrollToPosition(mFirstChildPosition);
//        mListingRecyclerview.scrollBy(0, -mOffset);
//    } finally {
//        if (android.os.Build.VERSION.SDK_INT >= 21) {
//            Timber.e("OKAY");
//            if (mListingRecyclerview.getAdapter().getItemCount()>CLICKED_ITEM_POSITION && CLICKED_ITEM_POSITION>-1){
//                Timber.e("FUCKING WORK YOU PIECE OF GODDAMN SHIT");
//                mListingRecyclerAdapter.notifyDataSetChanged();
//                RecyclerView.ViewHolder holder = mListingRecyclerview.findViewHolderForItemId(mListingRecyclerAdapter.getItemId(CLICKED_ITEM_POSITION));
//                if (holder!=null)
//                    holder.itemView.setTransitionName(getString(R.string.transitionName_Post));
//                else
//                    Timber.e("FUCK MY GODDAMN SHITTY ASSS FUCKING LIFE");
////                mListingsLinearLayoutManager.findViewByPosition(CLICKED_ITEM_POSITION).setTransitionName(getString(R.string.transitionName_Post));
//            }
//        }
//    }
}

    private void initCommentsUi(){

        mCommentRecords = CommentRecord.listAll(CommentRecord.class);
        mItemCount = mCommentRecords.size();

    }

    public void addMoreItems(String after){
        itemCount = mRedditListings.size();
        GetListingsService.loadListingsSearch(this, mSubredditString, mSearchQueryString, mAfter, mSortString, mRestrictSearchBoolean);
    }

    @Override
    protected void onDestroy() {
        killRealm();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main_activity, menu);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode==CODE_MANAGE_SUBREDDITS){
            if (resultCode == Activity.RESULT_OK){
                setSubredditsInNavigationView("", mAccessToken);
            }
        } else if (requestCode == CODE_APP_SETTINGS){
//            Toast.makeText(MainActivity.this, getString(R.string.message_restart_to_see_changes), Toast.LENGTH_LONG).show();
            setCheckedNavigationItem(getSavedNavigationItemId());
            if (resultCode == AppSettingsActivity.RESULT_CODE_FONT_CHANGED || resultCode == AppSettingsActivity.RESULT_CODE_THEME_CHANGED){
                AppCompatActivity activity = MainActivity.this;
                stopAllReceivers();
                activity.finish();
                Intent intent1 = new Intent(activity, activity.getClass());
                intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                activity.startActivity(intent1);
            }
        } else if (requestCode == CODE_LOGIN_USER) {
            if (resultCode == Activity.RESULT_OK) {
                Toast.makeText(this, "Getting subreddits", Toast.LENGTH_LONG).show();
                mAccessToken = mApplicationSharedPreferences.getString(GetAuthActivity.KEY_ACCESS_TOKEN, null);
                haveToReloadSubreddits = true;
                GetSubredditsService.loadSubreddits(this, null, true, mAccessToken);
            }
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(selectedSortID).setChecked(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (item.isChecked()) item.setChecked(false);
        else item.setChecked(true);
        switch (itemId){
            case android.R.id.home:
                mDrawerLayout.openDrawer(Gravity.START);
                return true;
            case R.id.sort_hot: {
                selectedSortID = itemId;
                mSortString = item.getTitle().toString();
                onRefresh();
                return true;
            }
            case R.id.sort_new: {
                selectedSortID = itemId;
                mSortString = item.getTitle().toString();
                onRefresh();
                return true;
            }
            case R.id.sort_rising: {
                selectedSortID = itemId;
                mSortString = item.getTitle().toString();
                onRefresh();
                return true;
            }
            case R.id.sort_top: {
                selectedSortID = itemId;
                mSortString = item.getTitle().toString();
                onRefresh();
                return true;
            }
            case R.id.sort_top_day: {
                selectedSortID = itemId;
                mSortString = item.getTitle().toString();
                onRefresh();
                return true;
            }
            case R.id.sort_top_week: {
                selectedSortID = itemId;
                mSortString = item.getTitle().toString();
                onRefresh();
                return true;
            }
            case R.id.sort_top_month: {
                selectedSortID = itemId;
                mSortString = item.getTitle().toString();
                onRefresh();
                return true;
            }
            case R.id.sort_top_year: {
                selectedSortID = itemId;
                mSortString = item.getTitle().toString();
                onRefresh();
                return true;
            }
            case R.id.sort_top_all:{
                selectedSortID = itemId;
                mSortString = item.getTitle().toString();
                onRefresh();
                return true;
            }
            case R.id.add_this_subreddit:{
                if (mSearchQueryString==null) {
                    if (mSubredditString!=null) {
                        Uri url = UriGenerator.getUriSubredditAbout(mSubredditString);
                        Intent intent = new Intent(this, CheckNewSubredditService.class);
                        intent.putExtra(CheckNewSubredditService.EXTRA_URL, url);
                        startService(intent);
                    } else {
                        PreferenceUtils.getThemedSnackbar(MainActivity.this, R.id.activity_main, getString(R.string.cannot_subscribe_to_frontpage), Snackbar.LENGTH_LONG).show();

                    }
                } else {
                    PreferenceUtils.getThemedSnackbar(MainActivity.this, R.id.activity_main, getString(R.string.message_cant_add_search), Snackbar.LENGTH_LONG).show();
                }
                return true;
            }
//            case R.id.theme_default:{
////                PreferenceUtils.changeToTheme(this, PreferenceUtils.THEME_DEFAULT);
//                return true;
//            }
//            case R.id.theme_saturated:{
////                PreferenceUtils.changeToTheme(this, PreferenceUtils.THEME_SATURATED);
//                return true;
//            }
//            case R.id.theme_white:{
////                PreferenceUtils.changeToTheme(this, PreferenceUtils.THEME_WHITE);
//                return true;
//            }
//            case R.id.theme_strawberries:{
////                PreferenceUtils.changeToTheme(this, PreferenceUtils.THEME_STRAWBERRIES);
//                return true;
//            }
//            case R.id.theme_coastal:{
////                PreferenceUtils.changeToTheme(this, PreferenceUtils.THEME_COASTAL);
//                return true;
//            }
//            case R.id.theme_solarized_light:{
////                PreferenceUtils.changeToTheme(this, PreferenceUtils.THEME_SOLARIZED_LIGHT);
//                return true;
//            }
//            case R.id.theme_dark:{
////                PreferenceUtils.changeToTheme(this, PreferenceUtils.THEME_DARK_1);
//                return true;
//            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void startRealm(){
        try{
            mRealm = Realm.getDefaultInstance();

        }catch (Exception e){

            // Get a Realm instance for this thread
            RealmConfiguration config = new RealmConfiguration.Builder()
                    .deleteRealmIfMigrationNeeded()
                    .build();
            mRealm = Realm.getInstance(config);

        }
    }


    public void setSubredditsInNavigationView(String rawString, @Nullable String authToken){
        final Menu menu = mNavigationView.getMenu();
        String subTitle;
        if (rawString.length()==0 || haveToReloadSubreddits){
            if (mRealm == null){
                startRealm();
            }
            mSubredditObjectRealmResults = mRealm.where(SubredditObject.class).findAll().sort("subredditName");
            if (mSubredditObjectRealmResults.size()<=0) {
                mListingRecyclerview.setVisibility(View.GONE);
                findViewById(R.id.firstTimeLayout).setVisibility(View.VISIBLE);
                getSubredditsForNavigationMenu(null, authToken != null, authToken);
                Timber.e("no subreddits in realm");
            } else {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < mSubredditObjectRealmResults.size(); i++) {
                    SubredditObject thisObj = mSubredditObjectRealmResults.get(i);
                    subTitle = thisObj.getSubredditName();
                    sb.append(subTitle);
                    sb.append(PreferenceUtils.SUBREDDIT_SEPARATOR);
                }
                SharedPreferences.Editor editor = mApplicationSharedPreferences.edit();
                editor.putString(getString(R.string.pref_subreddit_list), sb.toString()).
                        apply();
//                Timber.d("current subreddit list is " + mApplicationSharedPreferences.getString(getString(R.string.pref_subreddit_list), "FUCK"));
                editor.putBoolean(getString(R.string.pref_reload_subreddits), false).apply();
                finish();
                startActivity(new Intent(this, MainActivity.class));
            }
        } else {
            String[] subreddits = rawString.split(PreferenceUtils.SUBREDDIT_SEPARATOR);
            for (int i = 0; i < subreddits.length; i++){
                subTitle = subreddits[i];
                menu.add(R.id.subreddits_group, i, 1, subTitle).setCheckable(true).setChecked(false);
                if (subTitle.equals(mSubredditString)){
                    menu.findItem(i).setChecked(true);
                }
            }
            if (RedditListing.listAll(RedditListing.class).size()<=0){
                onRefresh();
            }
        }
    }

    void getSubredditsForNavigationMenu(@Nullable String where, boolean mineWhere, @Nullable String authToken){
        if (mRealm==null){
            startRealm();
        }
        GetSubredditsService.loadSubreddits(this, where, mineWhere, authToken);
    }

    public List<RedditListing> getPosts(@Nullable String subredditString, @Nullable String searchQuery, @Nullable String after, @Nullable String sort, boolean restrictSr, boolean forceRefresh){
        if ( forceRefresh){
//            Timber.d("have to refresh posts");
            if (searchQuery!=null){
                GetListingsService.loadListingsSearch(this, subredditString, searchQuery, after, sort, restrictSr);
            } else {
                GetListingsService.loadListingsSubreddit(this, subredditString, sort, 0, after, true);
            }
        }
        return SugarRecord.listAll(RedditListing.class);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Menu menu = mNavigationView.getMenu();
        int itemId = item.getItemId();
        if (itemId == R.id.goToFrontpage){
            saveCheckedItem(itemId);
            mSubredditString = null;
            setDefaultSubreddit(null);

            mSwipeRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    mSwipeRefreshLayout.setRefreshing(true);
                    deleteAll(RedditListing.class);
                    onRefresh();
                }
            });
        } else if (itemId == R.id.go_to_subreddit) {
            openGoToDialog();
        } else if (itemId == R.id.search_posts){
            openSearchDialog();
        } else if (itemId == R.id.add_subscription){
//            openAddSubDialog();
            Intent intent = new Intent(this, ManageSubredditsActivity.class);
            startActivityForResult(intent, CODE_MANAGE_SUBREDDITS);
        } else if (itemId == R.id.random_subreddit){
            GetListingsService.loadListingsRandom(this);
            mListingRecyclerview.setAdapter(null);
            mSwipeRefreshLayout.setRefreshing(true);
            saveCheckedItem(itemId);
        } else if (itemId == R.id.app_settings){
            Intent intent = new Intent(this, AppSettingsActivity.class);
            startActivityForResult(intent, CODE_APP_SETTINGS);
        } else if (itemId == R.id.itLogin) {
            Intent intent = new Intent(this, GetAuthActivity.class);
            startActivityForResult(intent, CODE_LOGIN_USER);
        } else if (itemId == R.id.itProfile) {
            UserHistoryActivity.Companion.startUserHistoryActivity(this);
        } else if (itemId == R.id.itLogOut) {
            Toast.makeText(this, "IMPLEMENT ME", Toast.LENGTH_LONG).show();
            //TODO: LOGOUT
//            UserHistoryActivity.Companion.startUserHistoryActivity(this);
        }
        else {
            saveCheckedItem(itemId);
            mAfter = null;
            mSearchQueryString = null;
            mRestrictSearchBoolean = false;
            mSubredditString = (String) item.getTitle();
            mSwipeRefreshLayout.setRefreshing(true);
            mSwipeRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    deleteAll(RedditListing.class);
                    onRefresh();
                }
            });
        }
        mAfter = null;
        mSearchQueryString = null;
        mRestrictSearchBoolean = false;
        mApplicationSharedPreferences.edit().putString(getString(R.string.pref_search_string), null).apply();
        setDefaultSubreddit(mSubredditString);
        mDrawerLayout.closeDrawer(Gravity.START);
        for (int i = 0; i< menu.size(); i++){
            menu.getItem(i).setChecked(false);
        }
        item.setChecked(true);
        return true;
    }

    public void killRealm(){
//        Timber.d("killing (not starting) realm");
        if (mRealm!=null){
            mRealm.close();
            mRealm = null;
        }
    }

    @Override
    public void onRefresh() {
        mSwipeRefreshLayout.setRefreshing(true);
        mAfter = null;
//        findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
        mListingRecyclerview.setAdapter(null);

        GetListingsService.loadListingsSearch(this, mSubredditString, mSearchQueryString, mAfter, mSortString, mRestrictSearchBoolean);
        if (mSearchQueryString==null) {
            GetSubredditInfoService.loadSidebar(this, mSubredditString);
        }
        if (mSearchQueryString==null) {
            mCollapsingToolbarLayout.setTitle(mSubredditString == null ? getString(R.string.frontpage) : mSubredditString);
        } else {
            mCollapsingToolbarLayout.setTitle(getString(R.string.format_search_results, mSearchQueryString));
        }
//        mSwipeRefreshLayout.setRefreshing(false);
    }

    public void refreshComments(){
        mCommentsRecyclerview.setAdapter(null);
    }

    @Override
    public void OnGoToDialogPositiveClick(DialogFragment dialogFragment, String query) {
        if (query.length()>0) {
            saveCheckedItem();
            mSubredditString = query;
            mSearchQueryString = null;
            setDefaultSubreddit(mSubredditString);
            mSwipeRefreshLayout.setRefreshing(true);

            deleteAll(RedditListing.class);
            onRefresh();
        }
    }

    @Override
    public void OnGoToDialogNegativeClick(DialogFragment dialogFragment) {
        setCheckedNavigationItem(getSavedNavigationItemId());
    }

    void openGoToDialog(){
        DialogFragment dialogFragment = new GoToDialogFragment();
        dialogFragment.show(getSupportFragmentManager(), TAG_GO_TO_SUBREDDIT);
        mRestrictSearchBoolean = false;
        mSearchQueryString = null;
    }

    void saveCheckedItem(){
        mApplicationSharedPreferences.edit().putInt(getString(R.string.pref_last_valid_nav_item), getCheckedItemIndex(mNavigationView)).apply();
    }

    void saveCheckedItem(int id){
        mApplicationSharedPreferences.edit().putInt(getString(R.string.pref_last_valid_nav_item), id).apply();
    }

    void setDefaultSubreddit(String name){
        mApplicationSharedPreferences.edit().putString(getString(R.string.pref_current_subreddit), name).apply();
    }

    void openSearchDialog(){
        DialogFragment dialogFragment = new SearchDialogFragment();
        dialogFragment.show(getSupportFragmentManager(), TAG_SEARCH_POSTS);
    }

    void openQuitDialog(){
        DialogFragment quitDialog = new QuitDialog();
        quitDialog.show(getSupportFragmentManager(), "dontgo");
    }

    void openAddSubDialog(){
        DialogFragment dialogFragment = new AddSubredditDialogFragment();
        dialogFragment.show(getSupportFragmentManager(), TAG_ADD_SUBREDDIT);
    }

    private int getCheckedItemIndex(NavigationView navigationView) {
        Menu menu = navigationView.getMenu();
        for (int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);
            if (item.isChecked()) {
                return item.getItemId();
            }
        }

        return -1;
    }

    private int getSavedNavigationItemId(){
       return mApplicationSharedPreferences.getInt(getString(R.string.pref_last_valid_nav_item),R.id.goToFrontpage);
    }

    public void setCheckedNavigationItem(int index){
        Menu menu = mNavigationView.getMenu();
        for (int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);
            if (item.getItemId()==index){
                item.setChecked(true);
            } else
                item.setChecked(false);
        }
        mNavigationView.setCheckedItem(index);
    }

    @Override
    public void OnSearchDialogPositiveClick(DialogFragment dialogFragment, String query) {
        saveCheckedItem();
        mSwipeRefreshLayout.setRefreshing(true);

        mSearchQueryString = query;
        mApplicationSharedPreferences.edit().putString(getString(R.string.pref_search_string), mSearchQueryString).apply();
        deleteAll(RedditListing.class);
        onRefresh();
    }

    @Override
    public void OnSearchDialogNegativeClick(DialogFragment dialogFragment) {
        mSearchQueryString = null;
        mRestrictSearchBoolean = false;
        setCheckedNavigationItem(getSavedNavigationItemId());
    }

    public void limitSearchToSubreddit(View view){
        mRestrictSearchBoolean = !mRestrictSearchBoolean;
//        Timber.d("restrict search is " + Boolean.toString(mRestrictSearchBoolean));
    }

    @Override
    public void OnReachedLast(String after) {
        loadingSnack.show();
        if (mAfter==null){
            mAfter = after;
            addMoreItems(mAfter);
        }
        else if (!mAfter.equals(after)) {
            mAfter = after;
            addMoreItems(mAfter);
        }
    }

    void startAllReceivers(){
        mPostsIntentFilter.addAction(Constants.BROADCAST_POSTS_LOADED);
        mPostsIntentFilter.addAction(Constants.BROADCAST_ERROR_WHILE_RETREIVING_POSTS);
        mPostsIntentFilter.addAction(Constants.BROADCAST_SUBREDDITS_LOADED);
        mPostsIntentFilter.addAction(Constants.BROADCAST_RANDOM_SUBREDDIT_POSTS_LOADED);
        registerReceiver(mPostsBroadcastReceiver, mPostsIntentFilter);

        mAddSubIntentFilter.addAction(Constants.BROADCAST_SUBREDDIT_BANNED);
        mAddSubIntentFilter.addAction(Constants.BROADCAST_SUBREDDIT_ADDED);
        mAddSubIntentFilter.addAction(Constants.BROADCAST_NO_SUCH_SUBREDDIT);
        mAddSubIntentFilter.addAction(Constants.BROADCAST_SUBREDDIT_PRESENT);
        registerReceiver(mAddSubBroadcastReceiver, mAddSubIntentFilter);

        mSubredditInfoIntentFilter.addAction(Constants.BROADCAST_SIDEBAR);
        mSubredditInfoIntentFilter.addAction(Constants.BROADCAST_SIDEBAR_ERROR);
        registerReceiver(mSubredditInfoReceiver, mSubredditInfoIntentFilter);

        registerReceiver(mLoggedInUserReceiver, mLoggedInUserIntentFilter);

        if (usingTabletLayout){
            mCommentsIntentFilter.addAction(Constants.BROADCAST_COMMENTS_LOADED);
            registerReceiver(mCommentsLoadedBroadcastReceiver, mCommentsIntentFilter);
        }


    }

    void stopAllReceivers(){
        try {
            unregisterReceiver(mPostsBroadcastReceiver);
            unregisterReceiver(mAddSubBroadcastReceiver);
            unregisterReceiver(mSubredditInfoReceiver);
            unregisterReceiver(mLoggedInUserReceiver);
            if (usingTabletLayout){
                unregisterReceiver(mCommentsLoadedBroadcastReceiver);
            }
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("Receiver not registered")) {
                // Ignore this exception. This is exactly what is desired
                Timber.e("Tried to unregister the receiver when it's not registered");
            } else {
                // unexpected, re-throw
                throw e;
            }
        }
    }

    void refreshUI(boolean isRandom){
        mRedditListings = RedditListing.listAll(RedditListing.class);
        mListingRecyclerAdapter = new RedditPostRecyclerAdapter(this, mRedditListings);

        if (isRandom){
            mSubredditString = mRedditListings.get(0).subreddit;
            mCollapsingToolbarLayout.setTitle(mSubredditString);
            mApplicationSharedPreferences.edit().putString(getString(R.string.pref_current_subreddit), mSubredditString).apply();
        }

        mListingRecyclerAdapter.setHasStableIds(true);
        mSwipeRefreshLayout.setRefreshing(false);
        mListingRecyclerview.setAdapter(mListingRecyclerAdapter);
        itemCount = mRedditListings.size();
    }

    public void setSidebarText(TextView sidebarTextview){
        String textHtml = mApplicationSharedPreferences.getString(GetSubredditInfoService.KEY_DESCRIPTION_HTML, getString(R.string.pref_sidebar_default_value));
        sidebarTextview.setText(GeneralUtils.returnFormattedStringFromHtml(textHtml));
        sidebarTextview.setMovementMethod(LinkMovementMethod.getInstance());
        ((ScrollView)findViewById(R.id.sidebar_scrollview)).scrollTo(0,0);
    }

    @Override
    public void OnAddDialogPositiveClick(DialogFragment dialogFragment, String query) {
        if (query.length()>0) {
            Uri url = UriGenerator.getUriSubredditAbout(query);
            Intent intent = new Intent(this, CheckNewSubredditService.class);
            intent.putExtra(CheckNewSubredditService.EXTRA_URL, url);
            startService(intent);
        }
    }

    private class UpdateCommentsPaneAsyncTask extends GetCommentsAsyncTask{
        @Override
        protected void onPostExecute(List<CommentRecord> commentRecords) {
            mCommentRecords = CommentRecord.listAll(CommentRecord.class);

            mCommentsRedditListing = RedditListing.find(RedditListing.class, "m_post_id = ?", mPostId).get(0);
            mCommentRecordRecyclerAdapter = new CommentRecordRecyclerAdapter(MainActivity.this, mCommentRecords, mCommentsRedditListing,false);
            mCommentRecordRecyclerAdapter.setHasStableIds(true);

            mCommentsRecyclerview.setAdapter(mCommentRecordRecyclerAdapter);

            mCommentsLinearLayoutManager.onRestoreInstanceState(mLayoutState);

            mItemCount = mCommentRecords.size();
        }
    }

    private class MyRefreshListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            mSwipeRefreshLayout.setRefreshing(true);
            getPosts(mSubredditString, mSearchQueryString, mAfter, mSortString, mRestrictSearchBoolean, true);
            onRefresh();
        }
    }
}