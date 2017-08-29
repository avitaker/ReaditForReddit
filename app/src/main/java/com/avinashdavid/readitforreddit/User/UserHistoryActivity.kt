package com.avinashdavid.readitforreddit.User

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.avinashdavid.readitforreddit.MiscUtils.GeneralUtils
import com.avinashdavid.readitforreddit.MiscUtils.PreferenceUtils
import com.avinashdavid.readitforreddit.R
import kotlinx.android.synthetic.main.activity_user_history.*

/**
 * Created by avinashdavid on 8/21/17.
 */
const val EXTRA_USER_ID = "EXTRA_USER_ID"

class UserHistoryActivity : AppCompatActivity() {
    companion object {

        fun startUserHistoryActivity(context: Context, userId : String) {
            val intent: Intent = Intent(context, UserHistoryActivity::class.java)
            intent.putExtra(EXTRA_USER_ID, userId)
            context.startActivity(intent)
        }
    }

    var userId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PreferenceUtils.onActivityCreateSetTheme(this)
        setContentView(R.layout.activity_user_history)
        setSupportActionBar(this.my_toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeButtonEnabled(true)

        if (intent.getStringExtra(EXTRA_USER_ID)!=null) userId = intent.getStringExtra(EXTRA_USER_ID)
        if (savedInstanceState != null) userId = savedInstanceState.getString(EXTRA_USER_ID)

        setUpFragments(userId)
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState!!.putString(EXTRA_USER_ID, userId)
    }

    fun setUpFragments(userId: String):Unit {
        val fragment : UserOverviewFragment = UserOverviewFragment.newInstance(userId)
        GeneralUtils.replaceFragment(this, R.id.flContainer, fragment, "UserHistory")
    }
}