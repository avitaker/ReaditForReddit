package com.avinashdavid.readitforreddit.PostUtils;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by avinashdavid on 3/11/17.
 */

public class DeleteCommentsService extends IntentService {

    private Realm mRealm;

    public DeleteCommentsService() {
        super(DeleteCommentsService.class.getSimpleName());
        mRealm = null;
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        try{
            mRealm = Realm.getDefaultInstance();

        }catch (Exception e){

            // Get a Realm instance for this thread
            RealmConfiguration config = new RealmConfiguration.Builder()
                    .deleteRealmIfMigrationNeeded()
                    .build();
            mRealm = Realm.getInstance(config);

        }
        mRealm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                mRealm.where(CommentObject.class).findAll().deleteAllFromRealm();
            }
        });
    }

    @Override
    public void onDestroy() {
        if (mRealm!=null){
            mRealm.close();
        }
        super.onDestroy();
    }

    public static void deletePosts(Context context){
        Intent intent = new Intent(context, DeleteCommentsService.class);
        context.startService(intent);
    }
}
