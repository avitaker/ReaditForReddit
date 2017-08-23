package com.avinashdavid.readitforreddit.UserHistoryDisplay

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.avinashdavid.readitforreddit.NetworkUtils.GetUserCommentsService
import com.avinashdavid.readitforreddit.R

/**
 * Created by avinashdavid on 8/21/17.
 */
class UserOverviewFragment : Fragment() {
    companion object {
        private const val KEY_USER_ID = "keyUserId"

        fun newInstance(userId : String): UserOverviewFragment {
            val args : Bundle = Bundle()
            args.putString(UserOverviewFragment.KEY_USER_ID, userId)

            val fragment = UserOverviewFragment()
            fragment.arguments = args
            return fragment
        }
    }

    var fragmentView : View? = null

    var userId : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userId = arguments.getString(KEY_USER_ID)
        if (savedInstanceState!=null) userId = savedInstanceState.getString(KEY_USER_ID)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        fragmentView = inflater?.inflate(R.layout.fragment_user_overview, container, false)

        return fragmentView
    }

    override fun onStart() {
        super.onStart()
        GetUserCommentsService.loadUserComments(activity, userId!!)
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState!!.putString(KEY_USER_ID, userId)
    }
}