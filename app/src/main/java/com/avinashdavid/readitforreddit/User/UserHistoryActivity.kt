package com.avinashdavid.readitforreddit.User

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.view.View
import com.avinashdavid.readitforreddit.MiscUtils.Constants
import com.avinashdavid.readitforreddit.MiscUtils.GeneralUtils
import com.avinashdavid.readitforreddit.MiscUtils.PreferenceUtils
import com.avinashdavid.readitforreddit.R
import com.orm.SugarRecord
import kotlinx.android.synthetic.main.activity_user_history.*
import java.util.*

/**
 * Created by avinashdavid on 8/21/17.
 */
const val EXTRA_USER_ID = "EXTRA_USER_ID"

class UserHistoryActivity : AppCompatActivity() {
    companion object {

        fun startUserHistoryActivity(context: Context, userName: String) {
            val intent: Intent = Intent(context, UserHistoryActivity::class.java)
            intent.putExtra(EXTRA_USER_ID, userName)
            context.startActivity(intent)
            (context as AppCompatActivity).overridePendingTransition(R.anim.enter_right, R.anim.slide_to_left)
        }

        fun startUserHistoryActivity(context: Context) {
            val intent: Intent = Intent(context, UserHistoryActivity::class.java)
            if (LoggedInUser.Companion.currentLoggedInUser!=null) intent.putExtra(EXTRA_USER_ID, LoggedInUser.Companion.currentLoggedInUser!!.username)
            context.startActivity(intent)
            (context as AppCompatActivity).overridePendingTransition(R.anim.enter_right, R.anim.slide_to_left)
        }
    }

    var mUserId = ""
    val mIntentFilter = IntentFilter()
    val mBroadcastReceiver = object: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent!!.action
            when (action) {
                Constants.BROADCAST_USER_ABOUT_LOADED -> {
                    val userAbout = SugarRecord.listAll(UserAbout::class.java).last()
                    val userAge: String = GeneralUtils.returnFormattedTime(this@UserHistoryActivity, Calendar.getInstance(TimeZone.getTimeZone("UTC")).timeInMillis, userAbout.created_utc)
                    tvUserHandleAndAge.text = getString(R.string.format_user_nameAndAge, userAbout.name, userAge)
                    tvUserKarma.text = getString(R.string.format_user_karma, userAbout.link_karma.toString(), userAbout.comment_karma.toString())
                }
                Constants.BROADCAST_USER_ABOUT_ERROR -> {
                    val errorSnack: Snackbar = PreferenceUtils.getThemedSnackbar(this@UserHistoryActivity, R.id.activity_user_history, "Error loading user information", Snackbar.LENGTH_INDEFINITE);
                    errorSnack.setAction("Refresh", object: View.OnClickListener {
                        override fun onClick(v: View?) {
                            loadUserAbout(mUserId)
                        }
                    })
                    errorSnack.show()
                }
            }
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PreferenceUtils.onActivityCreateSetTheme(this)
        setContentView(R.layout.activity_user_history)
        setSupportActionBar(my_toolbar)
        my_toolbar.title = "Profile"
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeButtonEnabled(true)

        val listOfIds = listOf(R.id.main_appbar, R.id.collapsing_toolbar, R.id.my_toolbar)
        PreferenceUtils.changeToolbarColor(this, listOfIds)

        if (intent.getStringExtra(EXTRA_USER_ID)!=null) mUserId = intent.getStringExtra(EXTRA_USER_ID)
        if (savedInstanceState != null) mUserId = savedInstanceState.getString(EXTRA_USER_ID)

        setUpFragments(mUserId)
    }

    override fun onResume() {
        super.onResume()
        mIntentFilter.addAction(Constants.BROADCAST_USER_ABOUT_LOADED)
        mIntentFilter.addAction(Constants.BROADCAST_USER_ABOUT_ERROR)
        registerReceiver(mBroadcastReceiver, mIntentFilter)

        loadUserAbout(mUserId)
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(mBroadcastReceiver)
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState!!.putString(EXTRA_USER_ID, mUserId)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {

        }
        finish()
        return super.onOptionsItemSelected(item)
    }

    fun setUpFragments(userId: String):Unit {
        val submitted : UserOverviewFragment = UserOverviewFragment.newInstance(userId, UserOverviewFragment.TYPE_SUBMITTED)
        val comments : UserOverviewFragment = UserOverviewFragment.newInstance(userId, UserOverviewFragment.TYPE_COMMENTS)
        val overview : UserOverviewFragment = UserOverviewFragment.newInstance(userId, UserOverviewFragment.TYPE_OVERVIEW)

        val fragments = listOf(overview, comments, submitted)
        val pagerAdapter = UserPageFragmentPagerAdapter(supportFragmentManager, this, fragments)
        vpUSerPages.offscreenPageLimit = 3
        vpUSerPages.adapter = pagerAdapter
        tlUserPages.setupWithViewPager(vpUSerPages)

//        GeneralUtils.replaceFragment(this, R.id.flContainer, submitted, "UserHistory")
    }

    fun loadUserAbout(userId: String) {
        GetUserAboutService.loadUserAbout(this, userId)
    }
}