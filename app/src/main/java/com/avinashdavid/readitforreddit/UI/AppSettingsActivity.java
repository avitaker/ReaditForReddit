package com.avinashdavid.readitforreddit.UI;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.avinashdavid.readitforreddit.MiscUtils.GeneralUtils;
import com.avinashdavid.readitforreddit.MiscUtils.PreferenceUtils;
import com.avinashdavid.readitforreddit.R;

import timber.log.Timber;

/**
 * Created by avinashdavid on 5/27/17.
 */

public class AppSettingsActivity extends AppCompatActivity {
    public static final int RESULT_CODE_FONT_CHANGED = 101;
    public static final int RESULT_CODE_THEME_CHANGED = 102;

    SharedPreferences sp;
    String currentFont;
    boolean usingCustomFont;

    String currentTheme;

    String currentToolbarColor;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceUtils.onActivityCreateSetTheme(this);
        setContentView(R.layout.simple_activity);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        sp = PreferenceManager.getDefaultSharedPreferences(this);

        usingCustomFont = sp.getBoolean(getString(R.string.pref_key_use_custom_font), false);
        if (usingCustomFont) {
            currentFont = sp.getString(getString(R.string.pref_key_select_font), "NONE");
        } else {
            currentFont = "NONE";
        }
        currentTheme = sp.getString(getString(R.string.pref_key_select_theme), getString(R.string.theme_reddit));
        currentToolbarColor= sp.getString(getString(R.string.pref_key_toolbar_color), getString(R.string.pref_toolbar_default_color));

        getFragmentManager().beginTransaction().replace(R.id.content, new AppSettingsFragment()).commit();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        if (sp.getBoolean(getString(R.string.pref_key_use_custom_font), false)!=usingCustomFont || !sp.getString(getString(R.string.pref_key_select_font), "NONE").equals(currentFont)){
            Timber.d("DIFFERENT");
            GeneralUtils.setFont(this);
            this.setResult(RESULT_CODE_FONT_CHANGED);
        } else if (!currentTheme.equals(sp.getString(getString(R.string.pref_key_select_theme), "0"))){
            Timber.d("THEME DIFFERENT");
            PreferenceUtils.changeToTheme(this, sp.getString(getString(R.string.pref_key_select_theme), "0"));
            this.setResult(RESULT_CODE_THEME_CHANGED);
        } else if (!currentToolbarColor.equals(sp.getString(getString(R.string.pref_key_toolbar_color), getString(R.string.pref_toolbar_default_color)))){
            this.setResult(RESULT_CODE_THEME_CHANGED);
        }
        else {
            setResult(RESULT_CANCELED);
        }
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
