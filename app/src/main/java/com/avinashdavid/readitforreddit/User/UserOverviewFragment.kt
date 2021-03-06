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
import com.avinashdavid.readitforreddit.MiscUtils.*
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
private const val KEY_LOADED = "KEY_LOADED"
private const val KEY_FIRST_CHILD = "KEY_SCROLL_POSITION"
private const val KEY_OFFSET = "KEY_OFFSET"

class UserOverviewFragment : Fragment(), ScrollListener {
    override fun onReachedLast() {
        when (fragmentType) {
            TYPE_OVERVIEW -> {
                loadOverview(userId!!, true)
            }
            TYPE_COMMENTS -> {
                loadComments(userId!!, true)
            }
            TYPE_SUBMITTED -> {
                loadSubmitted(userId!!, true)
            }
        }
    }

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

    var thingsLoaded = false

    var fragmentView : View? = null

    var userId : String? = null
    var fragmentType = 0

    var rvUserOverview: RecyclerView? = null
    var userHistoryAdapter: UserHistoryAdapter? = null

    var mBroadcastReceiver: BroadcastReceiver? = null
    var mIntentFilter = IntentFilter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userId = arguments.getString(KEY_USER_ID)
        fragmentType = arguments.getInt(KEY_FRAGMENT_TYPE)
        retainInstance = true
        if (savedInstanceState!=null) userId = savedInstanceState.getString(KEY_USER_ID)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        fragmentView = inflater?.inflate(R.layout.fragment_user_overview, container, false)

        rvUserOverview = fragmentView!!.findViewById<RecyclerView>(R.id.rvUserOverview)

        rvUserOverview!!.prepareLinear(activity, true)

        setupBroadastReceiver()

        if (savedInstanceState == null) {
            when (fragmentType) {
                TYPE_COMMENTS -> {
                    if (!thingsLoaded) loadComments(userId!!)
                }
                TYPE_OVERVIEW -> {
                    if (!thingsLoaded) loadOverview(userId!!)
                }
                TYPE_SUBMITTED -> {
                    if (!thingsLoaded) loadSubmitted(userId!!)
                }
            }
        } else {
            when (fragmentType) {
                TYPE_COMMENTS -> {
                    displayComments()
                    rvUserOverview!!.scrollToPosition(savedInstanceState.getInt(KEY_FIRST_CHILD))
                }
                TYPE_OVERVIEW -> {
                    displayOverview()
                    rvUserOverview!!.scrollToPosition(savedInstanceState.getInt(KEY_FIRST_CHILD))
                }
                TYPE_SUBMITTED -> {
                    displaySubmitted()
                    rvUserOverview!!.scrollToPosition(savedInstanceState.getInt(KEY_FIRST_CHILD))
                }
            }
        }

        return fragmentView
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onResume() {
        super.onResume()
        try {
            activity.registerReceiver(mBroadcastReceiver!!, mIntentFilter)
        } catch (e: IllegalArgumentException) {
            Timber.e(e)
        }
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
        outState.putBoolean(KEY_LOADED, thingsLoaded)

        val firstChild = rvUserOverview!!.getChildAt(0)
        if (firstChild != null) {
            val firstVilibleChildPosition = rvUserOverview!!.getChildAdapterPosition(firstChild)

            outState.putInt(KEY_FIRST_CHILD, firstVilibleChildPosition)
        }
    }

    fun setupBroadastReceiver() {
        mBroadcastReceiver = object: BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val action:String = intent!!.action
                when (action) {
                    Constants.BROADCAST_USER_COMMENTS_LOADED -> {
//                        userHistoryComments = SugarRecord.listAll(UserHistoryComment::class.java)
//                        if (userCommentAdapter == null) userCommentAdapter = UserCommentAdapter(activity, userHistoryComments)
//                        else userCommentAdapter!!.userHistoryComments = userHistoryComments
//                        rvUserOverview!!.adapter = userCommentAdapter
                        displayComments()
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
                    Constants.BROADCAST_USER_OVERVIEW_LOADED -> {
                        displayOverview()
                    }
                    Constants.BROADCAST_USER_OVERVIEW_ERROR -> {
                        val errorSnack: Snackbar = PreferenceUtils.getThemedSnackbar(activity, R.id.activity_user_history, "Error loading user history", Snackbar.LENGTH_INDEFINITE);
                        errorSnack.setAction("Refresh", object: View.OnClickListener {
                            override fun onClick(v: View?) {
                                loadComments(userId!!)
                            }
                        })
                        errorSnack.show()
                    }
                    Constants.BROADCAST_USER_SUBMITTED_LOADED -> {
                        displaySubmitted()
                    }
                    Constants.BROADCAST_USER_SUBMITTED_ERROR -> {
                        val errorSnack: Snackbar = PreferenceUtils.getThemedSnackbar(activity, R.id.activity_user_history, "Error loading user submissions", Snackbar.LENGTH_INDEFINITE);
                        errorSnack.setAction("Refresh", object: View.OnClickListener {
                            override fun onClick(v: View?) {
                                loadSubmitted(userId!!)
                            }
                        })
                        errorSnack.show()
                    }
                    Constants.BROADCAST_USER_OVERVIEW_MORE_LOADED -> {
//                        val oldSize = userHistoryAdapter!!.itemCount
//                        val newSize = UserThingsSingleton.listOfThings.size
//                        userHistoryAdapter!!.listOfThings.addAll(UserThingsSingleton.listOfThings.subList(oldSize-1, newSize - 1))
//                        userHistoryAdapter!!.notifyItemRangeInserted(oldSize - 1, newSize)
                        userHistoryAdapter!!.listOfThings = UserThingsSingleton.listOfThings
                        userHistoryAdapter!!.notifyDataSetChanged()
                    }
                    Constants.BROADCAST_USER_COMMENTS_MORE_LOADED -> {
//                        val oldSize = userHistoryAdapter!!.itemCount
//                        val newSize = UserThingsSingleton.listOfComments.size
//                        userHistoryAdapter!!.listOfThings.addAll(UserThingsSingleton.listOfComments.subList(oldSize-1, newSize - 1))
//                        userHistoryAdapter!!.notifyItemRangeInserted(oldSize - 1, newSize)
                        val aComments = mutableListOf<SugarRecord>()
                        aComments.addAll(UserThingsSingleton.listOfComments)
                        userHistoryAdapter!!.listOfThings = aComments
                        userHistoryAdapter!!.notifyDataSetChanged()
                    }
                    Constants.BROADCAST_USER_SUBMITTED_MORE_LOADED -> {
//                        val oldSize = userHistoryAdapter!!.itemCount
//                        val newSize = UserThingsSingleton.listOfSubmitted.size
//                        userHistoryAdapter!!.listOfThings.addAll(UserThingsSingleton.listOfSubmitted.subList(oldSize-1, newSize - 1))
//                        userHistoryAdapter!!.notifyItemRangeInserted(oldSize - 1, newSize)
                        val aSubmissions = mutableListOf<SugarRecord>()
                        aSubmissions.addAll(UserThingsSingleton.listOfSubmitted)
                        userHistoryAdapter!!.listOfThings = aSubmissions
                    }
                }
            }
        }
    }

    fun loadComments(userId: String, more: Boolean = false) {
        val actions = listOf(Constants.BROADCAST_USER_COMMENTS_LOADED,
                Constants.BROADCAST_USER_COMMENTS_MORE_LOADED,
                Constants.BROADCAST_USER_COMMENTS_ERROR)
        if (mIntentFilter.countActions() < 3) mIntentFilter.addActions(actions)
        activity.registerReceiver(mBroadcastReceiver!!, mIntentFilter)
        GetUserCommentsService.loadUserComments(activity, userId, more)
    }

    fun loadOverview(userId: String, more: Boolean = false) {
        val actions = listOf(Constants.BROADCAST_USER_OVERVIEW_LOADED,
                Constants.BROADCAST_USER_OVERVIEW_MORE_LOADED,
                Constants.BROADCAST_USER_OVERVIEW_ERROR)
        if (mIntentFilter.countActions() < 3) mIntentFilter.addActions(actions)
        activity.registerReceiver(mBroadcastReceiver!!, mIntentFilter)
        GetUserOverviewService.loadUserOverview(activity, userId, more)
    }

    fun loadSubmitted(userId: String, more: Boolean = false) {
        val actions = listOf(
                Constants.BROADCAST_USER_SUBMITTED_LOADED,
                Constants.BROADCAST_USER_SUBMITTED_MORE_LOADED,
                Constants.BROADCAST_USER_SUBMITTED_ERROR)
        if (mIntentFilter.countActions() < 3) mIntentFilter.addActions(actions)
        activity.registerReceiver(mBroadcastReceiver!!, mIntentFilter)
        GetUserSubmittedService.loadUserSubmitted(activity, userId, more)
    }

    fun displayOverview(){
        val list = UserThingsSingleton.listOfThings
        userHistoryAdapter = UserHistoryAdapter(activity, list, this)
        rvUserOverview!!.adapter = userHistoryAdapter
    }

    fun displayComments() {
        val aComments = mutableListOf<SugarRecord>()
        aComments.addAll(UserThingsSingleton.listOfComments)
        userHistoryAdapter = UserHistoryAdapter(activity, aComments, this)
        rvUserOverview!!.adapter = userHistoryAdapter
    }

    fun displaySubmitted() {
        val aSubmissions = mutableListOf<SugarRecord>()
        aSubmissions.addAll(UserThingsSingleton.listOfSubmitted)
        userHistoryAdapter = UserHistoryAdapter(activity, aSubmissions, this)
        rvUserOverview!!.adapter = userHistoryAdapter
    }
}