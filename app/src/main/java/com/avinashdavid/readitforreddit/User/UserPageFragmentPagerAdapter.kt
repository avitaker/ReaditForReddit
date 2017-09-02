package com.avinashdavid.readitforreddit.User

import android.content.Context
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.app.FragmentStatePagerAdapter

/**
 * Created by avinashdavid on 9/2/17.
 */
class UserPageFragmentPagerAdapter(fragmentManager: FragmentManager, context: Context, fragments: List<Fragment>): FragmentStatePagerAdapter(fragmentManager) {
    val parentContext = context
    val fragments = fragments

    override fun getItem(position: Int): Fragment {
        return fragments[position]
    }

    override fun getCount(): Int {
        return fragments.size
    }

    override fun getPageTitle(position: Int): CharSequence {
        when (position) {
            0 -> {
                return "OVERVIEW"
            }
            1 -> {
                return "COMMENTS"
            }
            2 -> {
                return "SUBMITTED"
            }
            else -> {
                return "OVERVIEW"
            }
        }
    }
}