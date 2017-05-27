package com.avinashdavid.readitforreddit.UI;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;

import com.avinashdavid.readitforreddit.R;

/**
 * Created by avinashdavid on 5/27/17.
 */

public class AppSettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.app_settings);
    }
}
