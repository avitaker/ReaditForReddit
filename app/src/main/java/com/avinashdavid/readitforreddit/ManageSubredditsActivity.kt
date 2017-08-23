package com.avinashdavid.readitforreddit

import android.app.Activity
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.MenuItem

import com.avinashdavid.readitforreddit.MiscUtils.PreferenceUtils
import com.avinashdavid.readitforreddit.SubredditUtils.SubredditObject
import com.avinashdavid.readitforreddit.UI.SubredditRecyclerviewAdapter

import java.util.ArrayList

import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.RealmResults
import kotlinx.android.synthetic.main.activity_manage_subreddits.*

/**
 * Created by avinashdavid on 4/16/17.
 * Activity to display all current subreddit subscriptions and delete as desired
 */

class ManageSubredditsActivity : AppCompatActivity() {
    internal var mRealm: Realm? = null
    internal val mSubredditStringArrayList = ArrayList<String>()
    internal lateinit var mSubredditObjectRealmResults: RealmResults<SubredditObject>
//    internal lateinit var mRecyclerView: RecyclerView
//    internal lateinit var fab: FloatingActionButton
    internal lateinit var mAdapter: SubredditRecyclerviewAdapter
    internal var subredditCount: Int = 0
    internal var initialCount: Int = 0
    internal var mAdded: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PreferenceUtils.onActivityCreateSetTheme(this)
        setContentView(R.layout.activity_manage_subreddits)
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

//        mRecyclerView = findViewById(R.id.manage_subreddits_recyclerview) as RecyclerView

        subredditCount = 0
        mAdded = false

        startRealm()
        mSubredditObjectRealmResults = mRealm!!.where(SubredditObject::class.java).findAll().sort("subredditName")
        val subredditObjects = mSubredditObjectRealmResults.subList(0, mSubredditObjectRealmResults.size)
        initialCount = subredditObjects.size
        makeArrayList(subredditObjects)

        setUpRecyclerAndAdapter()

//        fab = findViewById(R.id.fab) as FloatingActionButton
        this.fab.hide()
        //        fab.setOnClickListener(new View.OnClickListener() {
        //            @Override
        //            public void onClick(View view) {
        //                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
        //                        .setAction("Action", null).show();
        //            }
        //        });
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
    }

    override fun onDestroy() {
        killRealm()
        super.onDestroy()
    }

    fun startRealm() {
        try {
            mRealm = Realm.getDefaultInstance()

        } catch (e: Exception) {
            val config = RealmConfiguration.Builder()
                    .deleteRealmIfMigrationNeeded()
                    .build()
            mRealm = Realm.getInstance(config)

        }

    }

    fun makeArrayList(subredditObjects: List<SubredditObject>) {
        val size = subredditObjects.size
        for (i in 0..size - 1) {
            mSubredditStringArrayList.add(subredditObjects[i].subredditName)
            subredditCount++
        }
    }

    internal fun setUpRecyclerAndAdapter() {
        mAdapter = SubredditRecyclerviewAdapter(this, mSubredditStringArrayList)
        mAdapter.setHasStableIds(true)
        this.manage_subreddits_recyclerview.layoutManager = LinearLayoutManager(this)

        this.manage_subreddits_recyclerview.adapter = mAdapter
//        mRecyclerView.adapter = mAdapter
    }

    fun removeItem(position: Int): Boolean {
        if (position < subredditCount) {
            //            final SubredditObject subredditObject = mSubredditObjectRealmResults.get(position);
            val subredditName = mSubredditStringArrayList[position]
            mRealm!!.executeTransaction {
                val toRemove = mRealm!!.where(SubredditObject::class.java).equalTo("subredditName", subredditName).findAll()
                toRemove.deleteAllFromRealm()
            }
            mSubredditStringArrayList.removeAt(position)
            subredditCount--
            return true
        }
        return false
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onBackPressed() {
        //        boolean changed = initialCount != subredditCount;
        //        if (changed) {
        //            setResult(Activity.RESULT_OK);
        //        } else {
        //            setResult(Activity.RESULT_CANCELED);
        //        }
        setResult(Activity.RESULT_OK)
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                setResult(Activity.RESULT_OK)
                finish()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    fun killRealm() {
        if (mRealm != null) {
            mRealm!!.close()
            mRealm = null
        }
    }

    companion object {

        val KEY_CHANGED = "changed"
    }
}
