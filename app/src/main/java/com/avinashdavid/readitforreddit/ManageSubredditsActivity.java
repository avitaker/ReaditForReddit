package com.avinashdavid.readitforreddit;

import android.app.Activity;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.avinashdavid.readitforreddit.MiscUtils.PreferenceUtils;
import com.avinashdavid.readitforreddit.SubredditUtils.SubredditObject;
import com.avinashdavid.readitforreddit.UI.SubredditRecyclerviewAdapter;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import timber.log.Timber;

public class ManageSubredditsActivity extends AppCompatActivity {
    Realm mRealm;
    final ArrayList<String> mSubredditStringArrayList = new ArrayList<>();
    RealmResults<SubredditObject> mSubredditObjectRealmResults;
    RecyclerView mRecyclerView;
    FloatingActionButton fab;
    SubredditRecyclerviewAdapter mAdapter;
    int subredditCount;
    int initialCount;
    boolean mAdded;

    public static final String KEY_CHANGED = "changed";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceUtils.onActivityCreateSetTheme(this);
        setContentView(R.layout.activity_manage_subreddits);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mRecyclerView = (RecyclerView)findViewById(R.id.manage_subreddits_recyclerview);

        subredditCount = 0;
        mAdded = false;

        startRealm();
        mSubredditObjectRealmResults = mRealm.where(SubredditObject.class).findAll().sort("subredditName");
        List<SubredditObject> subredditObjects = mSubredditObjectRealmResults.subList(0, mSubredditObjectRealmResults.size());
        initialCount = subredditObjects.size();
        makeArrayList(subredditObjects);

        setUpRecyclerAndAdapter();

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.hide();
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onDestroy() {
        killRealm();
        super.onDestroy();
    }

    public void startRealm() {
        try {
            mRealm = Realm.getDefaultInstance();

        } catch (Exception e) {
            RealmConfiguration config = new RealmConfiguration.Builder()
                    .deleteRealmIfMigrationNeeded()
                    .build();
            mRealm = Realm.getInstance(config);

        }
    }

    public void makeArrayList(List<SubredditObject> subredditObjects){
        int size = subredditObjects.size();
        for (int i=0; i<size; i++){
            mSubredditStringArrayList.add(subredditObjects.get(i).getSubredditName());
            subredditCount++;
        }
    }

    void setUpRecyclerAndAdapter(){
        mAdapter = new SubredditRecyclerviewAdapter(this, mSubredditStringArrayList);
        mAdapter.setHasStableIds(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mAdapter);
    }

    public boolean removeItem(int position){
        if (position<subredditCount) {
//            final SubredditObject subredditObject = mSubredditObjectRealmResults.get(position);
            final String subredditName = mSubredditStringArrayList.get(position);
            mRealm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    RealmResults<SubredditObject> toRemove = mRealm.where(SubredditObject.class).equalTo("subredditName",subredditName).findAll();
                    toRemove.deleteAllFromRealm();
                }
            });
            mSubredditStringArrayList.remove(position);
            subredditCount--;
            return true;
        }
        return false;
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onBackPressed() {
//        boolean changed = initialCount != subredditCount;
//        if (changed) {
//            setResult(Activity.RESULT_OK);
//        } else {
//            setResult(Activity.RESULT_CANCELED);
//        }
        setResult(Activity.RESULT_OK);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home: {
                    setResult(RESULT_OK);
                finish();
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void killRealm(){
        Timber.d("killing (not starting) realm");
        if (mRealm!=null){
            mRealm.close();
            mRealm = null;
        }
    }
}
