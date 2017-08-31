package com.avinashdavid.readitforreddit.User

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.avinashdavid.readitforreddit.MiscUtils.Constants
import com.avinashdavid.readitforreddit.MiscUtils.GeneralUtils
import com.avinashdavid.readitforreddit.MiscUtils.PreferenceUtils
import com.avinashdavid.readitforreddit.MiscUtils.prepareLinear
import com.avinashdavid.readitforreddit.R
import com.orm.SugarRecord
import kotlinx.android.synthetic.main.activity_user_history.*
import kotlinx.android.synthetic.main.fragment_user_overview.view.*
import timber.log.Timber
import java.util.*

/**
 * Created by avinashdavid on 8/21/17.
 */
private const val KEY_USER_ID = "keyUserId"
private const val KEY_FRAGMENT_TYPE = "keyFragmentType"

class UserOverviewFragment : Fragment() {
    companion object {
        const val TYPE_OVERVIEW = 0
        const val TYPE_COMMENTS = 1
        const val TYPE_SUBMITTED = 2

        fun newInstance(userId : String, fragmentType: Int = TYPE_COMMENTS): UserOverviewFragment {
            val args : Bundle = Bundle()
            args.putString(KEY_USER_ID, userId)
            args.putInt(KEY_FRAGMENT_TYPE, fragmentType)

            val fragment = UserOverviewFragment()
            fragment.arguments = args
            return fragment
        }
    }

    var fragmentView : View? = null

    var userId : String? = null
    var fragmentType = 0

    var rvUserOverview: RecyclerView? = null
    var userCommentAdapter: UserCommentAdapter? = null

    var mBroadcastReceiver: BroadcastReceiver? = null
    var mIntentFilter = IntentFilter()
    var userHistoryComments: List<UserHistoryComment>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userId = arguments.getString(KEY_USER_ID)
        fragmentType = arguments.getInt(KEY_FRAGMENT_TYPE)
        if (savedInstanceState!=null) userId = savedInstanceState.getString(KEY_USER_ID)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        fragmentView = inflater?.inflate(R.layout.fragment_user_overview, container, false)

        rvUserOverview = fragmentView!!.findViewById(R.id.rvUserOverview) as RecyclerView

        rvUserOverview!!.prepareLinear(activity, true)

        return fragmentView
    }

    override fun onStart() {
        super.onStart()
        setupBroadastReceiver()
    }

    override fun onResume() {
        super.onResume()
        mIntentFilter.addAction(Constants.BROADCAST_USER_COMMENTS_LOADED)
        mIntentFilter.addAction(Constants.BROADCAST_USER_COMMENTS_ERROR)
        activity.registerReceiver(mBroadcastReceiver!!, mIntentFilter)

        if (fragmentType == TYPE_COMMENTS) loadComments(userId!!)
    }

    override fun onPause() {
        super.onPause()
        try {
            activity.unregisterReceiver(mBroadcastReceiver!!)
        } catch (e: IllegalArgumentException) {
            Timber.e(e)
        }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState!!.putString(KEY_USER_ID, userId)
    }

    fun setupBroadastReceiver() {
        mBroadcastReceiver = object: BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val action:String = intent!!.action
                when (action) {
                    Constants.BROADCAST_USER_COMMENTS_LOADED -> {
                        userHistoryComments = SugarRecord.listAll(UserHistoryComment::class.java)
                        if (userCommentAdapter == null) userCommentAdapter = UserCommentAdapter(activity, userHistoryComments)
                        else userCommentAdapter!!.userHistoryComments = userHistoryComments
                        rvUserOverview!!.adapter = userCommentAdapter
                    }
                    Constants.BROADCAST_USER_COMMENTS_ERROR -> {
                        val errorSnack: Snackbar = PreferenceUtils.getThemedSnackbar(activity, R.id.activity_user_history, "Error loading user comments", Snackbar.LENGTH_INDEFINITE);
                        errorSnack.setAction("Refresh", object: View.OnClickListener {
                            override fun onClick(v: View?) {
                                loadComments(userId!!)
                            }
                        })
                        errorSnack.show()
                    }
                }
            }
        }
    }

    fun loadComments(userId: String) {
        GetUserCommentsService.loadUserComments(activity, userId)
    }
}