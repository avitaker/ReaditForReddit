package com.avinashdavid.readitforreddit.UI;

import android.os.AsyncTask;

import com.avinashdavid.readitforreddit.PostUtils.CommentRecord;

import java.util.List;

/**
 * Created by avinashdavid on 3/18/17.
 */

public class GetCommentsAsyncTask extends AsyncTask<String, Integer, List<CommentRecord>> {

    @Override
    protected List<CommentRecord> doInBackground(String... params) {
        List<CommentRecord> comments = CommentRecord.listAll(CommentRecord.class);
        return comments;
    }
}
