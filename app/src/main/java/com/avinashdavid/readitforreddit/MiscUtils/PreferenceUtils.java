package com.avinashdavid.readitforreddit.MiscUtils;

import android.app.Activity;
import android.content.Intent;
import android.preference.PreferenceManager;

import com.avinashdavid.readitforreddit.R;

/**
 * Created by avinashdavid on 3/10/17.
 */

public class PreferenceUtils {
    /**
     * DefaultSharedPreferences keys
     */
    public static final String KEY_SUBREDDIT_LIST = "sp_SubList";

    /**
     * Organization constants
     */
    public static final String SUBREDDIT_SEPARATOR = ";;";

    private static int sTheme;
    public final static int THEME_DEFAULT = 0;
    public static final int THEME_SATURATED = 9;
    public final static int THEME_OCEAN = 1;
    public final static int THEME_COFFEE = 2;
    public static final int THEME_APPLES = 3;
    public static final int THEME_BLUEBERRIES = 4;
    public static final int THEME_STRAWBERRIES = 5;
    public static final int THEME_COASTAL = 6;
    public static final int THEME_DARK_1 = 7;
    public static final int THEME_DESERT = 8;
    /**
     * Set the theme of the Activity, and restart it by creating a new Activity of the same type.
     */
    public static void changeToTheme(Activity activity, int theme)
    {
        PreferenceManager.getDefaultSharedPreferences(activity).edit().putInt(activity.getString(R.string.pref_theme), theme).apply();
        sTheme = theme;
        activity.finish();
        activity.startActivity(new Intent(activity, activity.getClass()));
    }

    public static void onActivityCreateSetTheme(Activity activity)
    {
        sTheme = PreferenceManager.getDefaultSharedPreferences(activity).getInt(activity.getString(R.string.pref_theme), THEME_DEFAULT);
        switch (sTheme)
        {
            case THEME_DEFAULT:
                activity.setTheme(R.style.AppTheme);
                break;
            case THEME_OCEAN:
                activity.setTheme(R.style.ThemeOcean);
                break;
            case THEME_COFFEE:
                activity.setTheme(R.style.ThemeCoffee);
                break;
            case THEME_APPLES:
                activity.setTheme(R.style.ThemeApples);
                break;
            case THEME_BLUEBERRIES:
                activity.setTheme(R.style.ThemeBlueberry);
                break;
            case THEME_STRAWBERRIES:
                activity.setTheme(R.style.ThemeStrawberriesAndCream);
                break;
            case THEME_COASTAL:
                activity.setTheme(R.style.ThemeGrecianHoliday);
                break;
            case THEME_DARK_1:
                activity.setTheme(R.style.DarkTheme);
                break;
            case THEME_DESERT:
                activity.setTheme(R.style.ThemeDesert);
                break;
            case THEME_SATURATED:
                activity.setTheme(R.style.ThemeRedditSaturated);
                break;
            default:
                activity.setTheme(R.style.AppTheme);
                break;
        }
    }

}
