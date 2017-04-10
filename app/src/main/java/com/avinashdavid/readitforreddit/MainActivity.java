package com.avinashdavid.readitforreddit;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
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
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.avinashdavid.readitforreddit.MiscUtils.Constants;
import com.avinashdavid.readitforreddit.MiscUtils.GeneralUtils;
import com.avinashdavid.readitforreddit.MiscUtils.PreferenceUtils;
import com.avinashdavid.readitforreddit.NetworkUtils.CheckNewSubredditService;
import com.avinashdavid.readitforreddit.NetworkUtils.GetCommentsService;
import com.avinashdavid.readitforreddit.NetworkUtils.GetListingsService;
import com.avinashdavid.readitforreddit.NetworkUtils.GetSubredditInfoService;
import com.avinashdavid.readitforreddit.NetworkUtils.GetSubredditsService;
import com.avinashdavid.readitforreddit.NetworkUtils.UriGenerator;
import com.avinashdavid.readitforreddit.PostUtils.CommentRecord;
import com.avinashdavid.readitforreddit.PostUtils.RedditListing;
import com.avinashdavid.readitforreddit.PostUtils.RedditPost;
import com.avinashdavid.readitforreddit.SubredditUtils.SubredditObject;
import com.avinashdavid.readitforreddit.UI.AddSubredditDialogFragment;
import com.avinashdavid.readitforreddit.UI.CommentRecordRecyclerAdapter;
import com.avinashdavid.readitforreddit.UI.GetCommentsAsyncTask;
import com.avinashdavid.readitforreddit.UI.GoToDialogFragment;
import com.avinashdavid.readitforreddit.UI.RedditPostRecyclerAdapter;
import com.avinashdavid.readitforreddit.UI.SearchDialogFragment;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import timber.log.Timber;

import static com.orm.SugarRecord.deleteAll;
import static com.orm.SugarRecord.listAll;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
        SwipeRefreshLayout.OnRefreshListener,
        GoToDialogFragment.GoToDialogListener,
        SearchDialogFragment.SearchDialogListener,
        AddSubredditDialogFragment.AddSubDialogListener,
        RedditPostRecyclerAdapter.ScrollListener{

    int itemCount=20;

    private static final String KEY_SUBREDDIT_NAME = "sr_name_SI";
    private static final String KEY_AFTER = "after_SI";
    private static final String KEY_SORT = "sort_SI";
    private static final String KEY_SEARCH_STRING = "searchStr_SI";
    private static final String KEY_RESTRICT_SEARCH = "restrictSr_SI";
    private static final String KEY_LAYOUTMANAGER_STATE = "KeyForLayoutManagerState";

    private static final String KEY_COMMENTS_POSTID = "keyPostId";

    private static final String TAG_GO_TO_SUBREDDIT = "goToSub";
    private static final String TAG_SEARCH_POSTS = "searchPosts";
    private static final String TAG_ADD_SUBREDDIT = "addSubreddit";

    private static final int ID_HOT = 1;
    private static final int ID_NEW = 2;
    private static final int ID_RISING = 3;
    private static final int ID_TOP = 4;

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

    RealmResults<RedditPost> mRedditPostRealmResults;

    SharedPreferences mApplicationSharedPreferences;
    Parcelable mLayoutState;

    BroadcastReceiver mPostsBroadcastReceiver;
    IntentFilter mPostsIntentFilter;

    BroadcastReceiver mAddSubBroadcastReceiver;
    IntentFilter mAddSubIntentFilter;

    BroadcastReceiver mSubredditInfoReceiver;
    IntentFilter mSubredditInfoIntentFilter;


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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.d("onCreate");
        PreferenceUtils.onActivityCreateSetTheme(this);
        setContentView(R.layout.drawer_main_activity);
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            postponeEnterTransition();
        }

        mApplicationSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        loadingSnack = Snackbar.make(findViewById(R.id.activity_main), R.string.message_loading_more_posts, Snackbar.LENGTH_INDEFINITE);
        usingTabletLayout = (findViewById(R.id.comment_recyclerview)!=null);
        mApplicationSharedPreferences.edit().putBoolean(getString(R.string.pref_boolean_use_tablet_layout), usingTabletLayout).apply();

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
        } else {
            mSubredditString = mApplicationSharedPreferences.getString(getString(R.string.pref_current_subreddit), null);
        }

        if (getIntent().getStringExtra(CommentsActivity.EXTRA_POST_ID)!=null){
            mPostId = getIntent().getStringExtra(CommentsActivity.EXTRA_POST_ID);
        }

        haveToReloadSubreddits = mApplicationSharedPreferences.getBoolean(getString(R.string.pref_reload_subreddits), true);

        mToolbar = (Toolbar)findViewById(R.id.my_toolbar);
        mToolbar.setNavigationIcon(R.drawable.ic_menu_white_24dp);
        mCollapsingToolbarLayout = (CollapsingToolbarLayout)findViewById(R.id.collapsing_toolbar);
        setSupportActionBar(mToolbar);

        mDrawerLayout = (DrawerLayout)findViewById(R.id.my_drawer_layout);
        mNavigationView = (NavigationView)findViewById(R.id.main_navigationview);

        mListingRecyclerview = (RecyclerView)findViewById(R.id.listing_recyclerview);
        mSwipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.swipeRefreshLayout);

        startRealm();


        mSubredditObjectRealmResults = mRealm.where(SubredditObject.class).findAll();
        String rawString = mApplicationSharedPreferences.getString(getString(R.string.pref_subreddit_list), "");
        setSubredditsInNavigationView(rawString);
        setSidebarText((TextView)findViewById(R.id.sidebar_text));

        mNavigationView.setNavigationItemSelectedListener(this);


        mSwipeRefreshLayout.setOnRefreshListener(this);

        mListingsLinearLayoutManager = new LinearLayoutManager(this);
        mListingsLinearLayoutManager.setAutoMeasureEnabled(true);

        mListingRecyclerview.setLayoutManager(mListingsLinearLayoutManager);
        mListingRecyclerview.setNestedScrollingEnabled(true);

        RecyclerView.ItemDecoration mDividerItemDecoration = new DividerItemDecoration(this, mListingsLinearLayoutManager.getOrientation());

        mListingRecyclerview.addItemDecoration(mDividerItemDecoration);
        
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
                        mRedditListings.clear();
                        mRedditListings.addAll(RedditListing.listAll(RedditListing.class));
//                    findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                        mListingRecyclerAdapter.notifyItemRangeInserted(itemCount, 20);
                        itemCount = mRedditListings.size();
                    }
                } else if (Constants.BROADCAST_ERROR_WHILE_RETREIVING_POSTS.equals(action)){
                    mSwipeRefreshLayout.setRefreshing(false);
                    Snackbar mySnackbar = Snackbar.make(findViewById(R.id.activity_main),
                            R.string.error_while_loading_posts, Snackbar.LENGTH_LONG);
                    mySnackbar.setAction(R.string.retry, new MyRefreshListener());
                    mySnackbar.show();
                } else if (Constants.BROADCAST_SUBREDDITS_LOADED.equals(action)){
                    setSubredditsInNavigationView("");
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
                    Timber.d("received subreddit valid broadcast");
                    mApplicationSharedPreferences.edit().putBoolean(getString(R.string.pref_reload_subreddits), true).commit();
                    setSubredditsInNavigationView("");
                    activity.unregisterReceiver(mAddSubBroadcastReceiver);
                    activity.finish();
                    activity.startActivity(new Intent(activity, activity.getClass()));
                    Snackbar.make(findViewById(R.id.activity_main),
                            R.string.message_subreddit_added, Snackbar.LENGTH_LONG).show();
//                    Toast.makeText(activity, "Subreddit added", Toast.LENGTH_LONG).show();
                } else if (action.equals(Constants.BROADCAST_SUBREDDIT_PRESENT)){
                    Snackbar.make(findViewById(R.id.activity_main),
                            R.string.message_subreddit_present, Snackbar.LENGTH_LONG).show();
                } else if (action.equals(Constants.BROADCAST_SUBREDDIT_BANNED)){
                    Snackbar.make(findViewById(R.id.activity_main),
                            R.string.message_subreddit_banned, Snackbar.LENGTH_LONG).show();
                } else if (action.equals(Constants.BROADCAST_NO_SUCH_SUBREDDIT)){
                    Snackbar.make(findViewById(R.id.activity_main),
                            R.string.message_subreddit_not_valid, Snackbar.LENGTH_LONG).show();
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
                    mPostId = GetCommentsService.sLastPostId;
                    UpdateCommentsPaneAsyncTask updateCommentsPaneAsyncTask = new UpdateCommentsPaneAsyncTask();
                    updateCommentsPaneAsyncTask.execute(mPostId);
                }
            };

            mCommentsIntentFilter = new IntentFilter();
            mItemCount = 0;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Timber.d("onStart");
        try {
            initUi(mSubredditString, mSearchQueryString, mSortString, mRestrictSearchBoolean, false);
        } finally {
            mFirstChildPosition = mApplicationSharedPreferences.getInt(Constants.KEY_POSTS_SCROLL_POSITION, 0);
            mOffset = mApplicationSharedPreferences.getInt(Constants.KEY_POSTS_OFFSET, 0);
            mListingRecyclerview.scrollToPosition(mFirstChildPosition);
            mListingRecyclerview.scrollBy(0, -mOffset);
//            mListingRecyclerview.post(new Runnable() {
//                @Override
//                public void run() {
//                    if (android.os.Build.VERSION.SDK_INT >= 21) {
//                        startPostponedEnterTransition();
//                    }
//                }
//            });
        }
        if (usingTabletLayout){
            if (mPostId==null){
                return;
            }
            if (!mPostId.equals(GetCommentsService.sLastPostId)) {
                GetCommentsService.loadCommentsForArticle(MainActivity.this, null, mPostId, mSortString);
            } else {
                initCommentsUi();
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
                int commentFirstChild = sp.getInt(Constants.KEY_COMMENTS_FIRST_CHILD, 0);
                final int commentOffset = sp.getInt(Constants.KEY_COMMENTS_OFFSET, 0);
                mCommentsRecyclerview.scrollToPosition(commentFirstChild);
                mCommentsRecyclerview.post(new Runnable() {
                    @Override
                    public void run() {
                        mCommentsRecyclerview.scrollBy(0, -commentOffset);
                    }
                });
            }
        }
    }

//

    @Override
    protected void onResume() {
        super.onResume();
        startAllReceivers();
    }

    @Override
    protected void onPause() {
        stopAllReceivers();
        View firstChild = mListingRecyclerview.getChildAt(0);
        if (firstChild!=null) {
            int firstVisiblePosition = mListingRecyclerview.getChildAdapterPosition(firstChild);
            int offset = firstChild.getTop();

            mApplicationSharedPreferences.edit()
                    .putInt(Constants.KEY_POSTS_SCROLL_POSITION, firstVisiblePosition)
                    .putInt(Constants.KEY_POSTS_OFFSET, offset)
                    .apply();
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

                mApplicationSharedPreferences.edit()
                        .putInt(Constants.KEY_COMMENTS_FIRST_CHILD, firstVisiblePosition)
                        .putInt(Constants.KEY_COMMENTS_OFFSET, offset)
                        .apply();
            }
        }
        super.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Timber.d("onsaveinstance called");
        outState.putString(KEY_SUBREDDIT_NAME, mSubredditString);
        outState.putString(KEY_SORT, mSortString);
        outState.putString(KEY_SEARCH_STRING, mSearchQueryString);
        outState.putString(KEY_AFTER, mAfter);
        outState.putBoolean(KEY_RESTRICT_SEARCH, mRestrictSearchBoolean);
        outState.putParcelable(KEY_LAYOUTMANAGER_STATE, mListingsLinearLayoutManager.onSaveInstanceState());
        if (usingTabletLayout){
            outState.putString(KEY_COMMENTS_POSTID, mPostId);
        }
    }

    private void initUi(@Nullable String subredditString, @Nullable String searchString, @Nullable String sortString, boolean restrictSr, boolean forceRefresh){
//        Timber.d("calling initUi");
        mAfter = null;

        mRedditListings = getPosts(subredditString, searchString, null, sortString, restrictSr, forceRefresh);

        mListingRecyclerAdapter = new RedditPostRecyclerAdapter(this, mRedditListings);

        mListingRecyclerAdapter.setHasStableIds(true);
        mListingRecyclerview.setAdapter(mListingRecyclerAdapter);

        mFirstChildPosition = mApplicationSharedPreferences.getInt(Constants.KEY_POSTS_SCROLL_POSITION, 0);
        mOffset = mApplicationSharedPreferences.getInt(Constants.KEY_POSTS_OFFSET, 0);
        mListingRecyclerview.scrollToPosition(mFirstChildPosition);
        mListingRecyclerview.scrollBy(0, -mOffset);

        if (android.os.Build.VERSION.SDK_INT >= 21) {
            startPostponedEnterTransition();
        }

        if (searchString==null) {
            mCollapsingToolbarLayout.setTitle(mSubredditString == null ? getString(R.string.frontpage) : mSubredditString);
        } else {
            mCollapsingToolbarLayout.setTitle(getString(R.string.format_search_results, searchString));
        }

//        mListingsLinearLayoutManager.onRestoreInstanceState(mLayoutState);
    }

    private void initCommentsUi(){
        Timber.d("calling initCommentsUi");

        mCommentRecords = CommentRecord.listAll(CommentRecord.class);
        mItemCount = mCommentRecords.size();

    }

    public void addMoreItems(String after){
        Timber.d("addMoreItems called for " + after);
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
//        String[] sortStrings = getResources().getStringArray(R.array.sort_listing_options);
//        MenuItem menuItem = menu.findItem(R.id.sort_by);
//        SubMenu subMenu = menuItem.getSubMenu();
//        for (int i = 0; i < sortStrings.length; i++){
//            subMenu.add(R.id.sort_options, i+1, i, sortStrings[i]);
//        }
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
                mSortString = item.getTitle().toString();
                onRefresh();
                return true;
            }
            case R.id.sort_new: {
                mSortString = item.getTitle().toString();
                onRefresh();
                return true;
            }
            case R.id.sort_rising: {
                mSortString = item.getTitle().toString();
                onRefresh();
                return true;
            }
            case R.id.sort_top: {
                mSortString = item.getTitle().toString();
                onRefresh();
                return true;
            }
            case R.id.add_this_subreddit:{
                if (mSearchQueryString==null) {
                    Uri url = UriGenerator.getUriSubredditAbout(mSubredditString);
                    Intent intent = new Intent(this, CheckNewSubredditService.class);
                    intent.putExtra(CheckNewSubredditService.EXTRA_URL, url);
                    startService(intent);
                } else {
                    Snackbar.make(findViewById(R.id.activity_main), R.string.message_cant_add_search, Snackbar.LENGTH_LONG).show();
                }
                return true;
            }
            case R.id.theme_default:{
                PreferenceUtils.changeToTheme(this, PreferenceUtils.THEME_DEFAULT);
                return true;
            }
            case R.id.theme_saturated:{
                PreferenceUtils.changeToTheme(this, PreferenceUtils.THEME_SATURATED);
                return true;
            }
            case R.id.theme_desert:{
                PreferenceUtils.changeToTheme(this, PreferenceUtils.THEME_DESERT);
                return true;
            }
            case R.id.theme_ocean:{
                PreferenceUtils.changeToTheme(this, PreferenceUtils.THEME_OCEAN);
                return true;
            }
            case R.id.theme_apples:{
                PreferenceUtils.changeToTheme(this, PreferenceUtils.THEME_APPLES);
                return true;
            }
            case R.id.theme_coffee:{
                PreferenceUtils.changeToTheme(this, PreferenceUtils.THEME_COFFEE);
                return true;
            }
            case R.id.theme_blueberry:{
                PreferenceUtils.changeToTheme(this, PreferenceUtils.THEME_BLUEBERRIES);
                return true;
            }
            case R.id.theme_strawberries:{
                PreferenceUtils.changeToTheme(this, PreferenceUtils.THEME_STRAWBERRIES);
                return true;
            }
            case R.id.theme_coastal:{
                PreferenceUtils.changeToTheme(this, PreferenceUtils.THEME_COASTAL);
                return true;
            }
            case R.id.theme_dark:{
                PreferenceUtils.changeToTheme(this, PreferenceUtils.THEME_DARK_1);
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void startRealm(){
        Timber.d("starting (not killing) realm");
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


    public void setSubredditsInNavigationView(String rawString){
        final Menu menu = mNavigationView.getMenu();
        String subTitle;
        if (rawString.length()==0 || haveToReloadSubreddits){
            if (mRealm == null){
                startRealm();
            }
            mSubredditObjectRealmResults = mRealm.where(SubredditObject.class).findAll();
            if (mSubredditObjectRealmResults.size()<=0) {
                getSubredditsForNavigationMenu(null, null);
//                setSubredditsInNavigationView("");
                Timber.d("no subreddits in realm");
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
                Timber.d("current subreddit list is " + mApplicationSharedPreferences.getString(getString(R.string.pref_subreddit_list), "FUCK"));
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

    void getSubredditsForNavigationMenu(@Nullable String where, @Nullable String mineWhere){
        Timber.d("calling getSubredditsForNavigationMenu");
        if (mRealm==null){
            startRealm();
        }
        GetSubredditsService.loadSubreddits(this, where, mineWhere);
    }

    public List<RedditListing> getPosts(@Nullable String subredditString, @Nullable String searchQuery, @Nullable String after, @Nullable String sort, boolean restrictSr, boolean forceRefresh){
        if ( forceRefresh){
            Timber.d("have to refresh posts");
            if (searchQuery!=null){
                GetListingsService.loadListingsSearch(this, subredditString, searchQuery, after, sort, restrictSr);
            } else {
                GetListingsService.loadListingsSubreddit(this, subredditString, sort, 0, after, true);
            }
        }
        return listAll(RedditListing.class);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Menu menu = mNavigationView.getMenu();
        for (int i = 0; i< menu.size(); i++){
            menu.getItem(i).setChecked(false);
        }
        item.setChecked(true);
        int itemId = item.getItemId();
        if (itemId == R.id.goToFrontpage){
            mSubredditString = null;
            mSearchQueryString = null;
            mRestrictSearchBoolean = false;
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
            openAddSubDialog();
        } else if (itemId == R.id.random_subreddit){
            GetListingsService.loadListingsRandom(this);
            mListingRecyclerview.setAdapter(null);
            mSwipeRefreshLayout.setRefreshing(true);
        }
        else {
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
        setDefaultSubreddit(mSubredditString);
        mDrawerLayout.closeDrawer(Gravity.START);
        return true;
    }

    public void killRealm(){
        Timber.d("killing (not starting) realm");
        if (mRealm!=null){
            mRealm.close();
            mRealm = null;
        }
    }

    @Override
    public void onRefresh() {
        mAfter = null;
//        findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
        mListingRecyclerview.setAdapter(null);

        GetListingsService.loadListingsSearch(this, mSubredditString, mSearchQueryString, mAfter, mSortString, mRestrictSearchBoolean);
        if (mSubredditString!=null) {
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
        mSubredditString = query;
        mSearchQueryString = null;
        setDefaultSubreddit(mSubredditString);
        mSwipeRefreshLayout.setRefreshing(true);

        deleteAll(RedditListing.class);
        onRefresh();
    }

    @Override
    public void OnGoToDialogNegativeClick(DialogFragment dialogFragment) {

    }

    void openGoToDialog(){
        DialogFragment dialogFragment = new GoToDialogFragment();
        dialogFragment.show(getSupportFragmentManager(), TAG_GO_TO_SUBREDDIT);
        mRestrictSearchBoolean = false;
        mSearchQueryString = null;
    }

    void setDefaultSubreddit(String name){
        mApplicationSharedPreferences.edit().putString(getString(R.string.pref_current_subreddit), name).apply();
    }

    void openSearchDialog(){
        DialogFragment dialogFragment = new SearchDialogFragment();
        dialogFragment.show(getSupportFragmentManager(), TAG_SEARCH_POSTS);
    }

    void openAddSubDialog(){
        DialogFragment dialogFragment = new AddSubredditDialogFragment();
        dialogFragment.show(getSupportFragmentManager(), TAG_ADD_SUBREDDIT);
    }

    @Override
    public void OnSearchDialogPositiveClick(DialogFragment dialogFragment, String query) {
        mSwipeRefreshLayout.setRefreshing(true);

        mSearchQueryString = query;
        deleteAll(RedditListing.class);
        onRefresh();
    }

    @Override
    public void OnSearchDialogNegativeClick(DialogFragment dialogFragment) {
        mSearchQueryString = null;
        mRestrictSearchBoolean = false;
    }

    public void limitSearchToSubreddit(View view){
        mRestrictSearchBoolean = !mRestrictSearchBoolean;
        Timber.d("restrict search is " + Boolean.toString(mRestrictSearchBoolean));
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
            if (usingTabletLayout){
                unregisterReceiver(mCommentsLoadedBroadcastReceiver);
            }
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("Receiver not registered")) {
                // Ignore this exception. This is exactly what is desired
                Timber.d("Tried to unregister the receiver when it's not registered");
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
        }

        mListingRecyclerAdapter.setHasStableIds(true);
        mSwipeRefreshLayout.setRefreshing(false);
        mListingRecyclerview.setAdapter(mListingRecyclerAdapter);
        itemCount = mRedditListings.size();
    }

    public void setSidebarText(TextView sidebarTextview){
        String textHtml = mApplicationSharedPreferences.getString(GetSubredditInfoService.KEY_DESCRIPTION_HTML, getString(R.string.pref_sidebar_default_value));
        sidebarTextview.setText(GeneralUtils.returnFormattedStringFromHtml(textHtml));
    }

    @Override
    public void OnAddDialogPositiveClick(DialogFragment dialogFragment, String query) {
        Uri url = UriGenerator.getUriSubredditAbout(query);
        Intent intent = new Intent(this, CheckNewSubredditService.class);
        intent.putExtra(CheckNewSubredditService.EXTRA_URL, url);
        startService(intent);
    }

    private class UpdateCommentsPaneAsyncTask extends GetCommentsAsyncTask{
        @Override
        protected void onPostExecute(List<CommentRecord> commentRecords) {
            mCommentRecords = CommentRecord.listAll(CommentRecord.class);
            mCommentsRedditListing = RedditListing.find(RedditListing.class, "m_post_id = ?", mPostId).get(0);
            mCommentRecordRecyclerAdapter = new CommentRecordRecyclerAdapter(MainActivity.this, mCommentRecords, mCommentsRedditListing);
            mCommentRecordRecyclerAdapter.setHasStableIds(true);

            mCommentsRecyclerview.setAdapter(mCommentRecordRecyclerAdapter);

            mCommentsLinearLayoutManager.onRestoreInstanceState(mLayoutState);
//            mCommentRecordRecyclerAdapter.notifyItemRangeInserted(mItemCount, mCommentRecords.size()-mItemCount);
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
