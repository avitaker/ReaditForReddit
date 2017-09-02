package com.avinashdavid.readitforreddit.User

import com.orm.SugarRecord

/**
 * Created by avinashdavid on 8/30/17.
 */
object UserThingsSingleton{
    var listOfThings: MutableList<SugarRecord> = mutableListOf()

    fun changeThings(list: MutableList<SugarRecord>) {
        listOfThings = list
    }

    fun addToThings(list: MutableList<SugarRecord>) {
        if (listOfThings == null) listOfThings = mutableListOf<SugarRecord>()
        listOfThings.addAll(list)
    }
}