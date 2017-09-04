package com.avinashdavid.readitforreddit.MiscUtils;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.avinashdavid.readitforreddit.R;

import java.util.ArrayList;
import java.util.List;

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
    public final static int THEME_OCEAN = 1;
    public final static int THEME_COFFEE = 2;
    public static final int THEME_APPLES = 3;
    public static final int THEME_BLUEBERRIES = 4;
    public static final int THEME_STRAWBERRIES = 5;
    public static final int THEME_COASTAL = 6;
    public static final int THEME_DARK_1 = 7;
    public static final int THEME_DESERT = 8;
    public static final int THEME_SATURATED = 9;
    public static final int THEME_WHITE = 10;
    public static final int THEME_SOLARIZED_LIGHT = 11;

    private static String sToolbarColor;
    private static String sBackgroundColor;
    /**
     * Set the theme of the Activity, and restart it by creating a new Activity of the same type.
     */
    public static void changeToTheme(Activity activity, String theme)
    {
//        PreferenceManager.getDefaultSharedPreferences(activity).edit().putInt(activity.getString(R.string.pref_theme), theme).apply();
        sTheme = Integer.parseInt(theme);
    }

    public static void onActivityCreateSetTheme(Activity activity)
    {
        sTheme = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(activity).getString(activity.getString(R.string.pref_key_select_theme), "10"));
        switch (sTheme)
        {
            case THEME_DEFAULT:
                activity.setTheme(R.style.RedditTheme);
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
            case THEME_SATURATED:
                activity.setTheme(R.style.ThemeRedditSaturated);
                break;
            case THEME_WHITE:
                activity.setTheme(R.style.ThemeWhite);
                break;
            case THEME_SOLARIZED_LIGHT:
                activity.setTheme(R.style.ThemeSolarizedLight);
                break;
            default:
                activity.setTheme(R.style.AppTheme);
                break;
        }
    }

    public static Snackbar getThemedSnackbar(Activity activity, int coordinatorViewId, String text, int duration){
        Snackbar snackbar = Snackbar.make(activity.findViewById(coordinatorViewId), text, duration);
        if (sTheme==THEME_WHITE){
            View view = snackbar.getView();
            view.setBackgroundColor(ContextCompat.getColor(activity, android.R.color.white));
        } else if (sTheme == THEME_SOLARIZED_LIGHT){
            View view = snackbar.getView();
            view.setBackgroundColor(ContextCompat.getColor(activity, R.color.solarized_bg));
        }
        return snackbar;
    }

    public static void changeToolbarColor(AppCompatActivity activity, @NonNull List<Integer> ids) {
        ArrayList<View> views = new ArrayList<>();
        for (int i=0; i<ids.size(); i++) {
            if (activity.findViewById(ids.get(i)) != null) {
                views.add(activity.findViewById(ids.get(i)));
            }
        }
        changeToolbarColor(activity, views);
    }

    public static void changeToolbarColor(AppCompatActivity activity, @Nullable ArrayList<View> otherViews){
        sToolbarColor = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext()).getString(activity.getString(R.string.pref_key_toolbar_color), activity.getString(R.string.pref_toolbar_default_color));
        Drawable colorDrawable;
        if (!sToolbarColor.equals(activity.getString(R.string.pref_toolbar_default_color))){
            colorDrawable = new ColorDrawable(Color.parseColor(sToolbarColor));
            activity.getSupportActionBar().setBackgroundDrawable(colorDrawable);
            if (otherViews!=null){
                for (View v : otherViews){
                    if (sToolbarColor.equals(activity.getString(R.string.color_red))){
                        v.setBackgroundColor(activity.getResources().getColor(R.color.color_red));
                    } else if (sToolbarColor.equals(activity.getString(R.string.color_blue))){
                        v.setBackgroundColor(activity.getResources().getColor(R.color.color_blue));
                    }
                    else if (sToolbarColor.equals(activity.getString(R.string.color_green))){
                        v.setBackgroundColor(activity.getResources().getColor(R.color.color_green));
                    }
                    else if (sToolbarColor.equals(activity.getString(R.string.color_yellow))){
                        v.setBackgroundColor(activity.getResources().getColor(R.color.color_yellow));
                    }
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                int color = 0;
                if (sToolbarColor.equals(activity.getString(R.string.color_red))){
                    activity.getWindow().setStatusBarColor(ContextCompat.getColor(activity, R.color.red_dark));
                } else if (sToolbarColor.equals(activity.getString(R.string.color_blue))){
                    activity.getWindow().setStatusBarColor(ContextCompat.getColor(activity, R.color.blue_dark));
                }
                else if (sToolbarColor.equals(activity.getString(R.string.color_green))){
                    activity.getWindow().setStatusBarColor(ContextCompat.getColor(activity, R.color.green_dark));
                }
                else if (sToolbarColor.equals(activity.getString(R.string.color_yellow))){
                    activity.getWindow().setStatusBarColor(ContextCompat.getColor(activity, R.color.yellow_dark));
                }
            }
        }
    }

    public static void changeBackgroundColor(Activity activity, Window window){
        sBackgroundColor = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext()).getString(activity.getString(R.string.pref_key_background_color), activity.getString(R.string.pref_toolbar_default_color));
        if (!sBackgroundColor.equals(activity.getString(R.string.pref_toolbar_default_color))){
            window.setBackgroundDrawable(new ColorDrawable(Color.parseColor(sBackgroundColor)));
        }
    }
}
