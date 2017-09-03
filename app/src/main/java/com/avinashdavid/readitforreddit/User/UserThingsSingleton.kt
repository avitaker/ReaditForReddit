package com.avinashdavid.readitforreddit.User

import com.orm.SugarRecord

/**
 * Created by avinashdavid on 8/30/17.
 */
object UserThingsSingleton{
    var listOfThings: MutableList<SugarRecord> = mutableListOf()
    var listOfSubmitted: MutableList<UserHistoryListing> = mutableListOf()
    var listOfComments: MutableList<UserHistoryComment> = mutableListOf()

    fun changeThings(list: MutableList<SugarRecord>) {
        listOfThings = list
    }

    fun addToThings(list: MutableList<SugarRecord>) {
        if (listOfThings == null) listOfThings = mutableListOf<SugarRecord>()
        listOfThings.addAll(list)
    }

    fun changeSubmitted(list: MutableList<UserHistoryListing>) {
        listOfSubmitted = list
    }

    fun addToSubmitted(list: MutableList<UserHistoryListing>) {
        if (listOfSubmitted == null) listOfSubmitted = mutableListOf<UserHistoryListing>()
        listOfSubmitted.addAll(list)
    }

    fun changeComments(list: MutableList<UserHistoryComment>) {
        listOfComments = list
    }

    fun addToComments(list: MutableList<UserHistoryComment>) {
        if (listOfComments == null) listOfComments = mutableListOf<UserHistoryComment>()
        listOfComments.addAll(list)
    }
}