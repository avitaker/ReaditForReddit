package com.avinashdavid.readitforreddit.MiscUtils;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.Spanned;
import android.util.TypedValue;
import android.view.View;

import com.avinashdavid.readitforreddit.R;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

/**
 * Created by avinashdavid on 3/8/17.
 */

public class GeneralUtils {
    public static int calculateTimeDifference(long timeNow, float timeThen){
//        long timeThenLong = (long)timeThen;
        long millisecondsDifference = timeNow - (long)(timeThen*1000);
        return (int)((millisecondsDifference/1000)/60)/60;
    }

    public static String returnFormattedTime(Context context, long timeNow, long timeThen){
        long millisecondsDifference = timeNow - (long)(timeThen*1000);
        double num = 0;
        if (millisecondsDifference > 3.1556952e10) {
            num = (millisecondsDifference/3.1556952e10);
            if (num>1) {
                return context.getString(R.string.format_yearselapsed, (int)num);
            } else {
                return context.getString(R.string.format_year, 1);
            }
        } else if (millisecondsDifference > 2.62975e9) {
            num = (millisecondsDifference/2.62975e9);
            if (num>1) {
                return context.getString(R.string.format_monthselapsed, (int)num);
            } else {
                return context.getString(R.string.format_month, 1);
            }
        } else if (millisecondsDifference > 8.64e7) {
            num = (millisecondsDifference/8.64e7);
            if (num>1) {
                return context.getString(R.string.format_dayselapsed, (int)num);
            } else {
                return context.getString(R.string.format_day, 1);
            }
        } else if (millisecondsDifference > 3.6e6) {
            num = (millisecondsDifference/3.6e6);
            if (num>1) {
                return context.getString(R.string.format_hourselapsed, (int)num);
            } else {
                return context.getString(R.string.format_hour, 1);
            }
        } else if (millisecondsDifference>60000){
            return context.getString(R.string.format_minuteselapsed, (int)(millisecondsDifference/60000));
        } else {
            return context.getString(R.string.just_now);
        }
    }

    @SuppressWarnings("deprecation")
    private static Spanned fromHtml(String source) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(source, Html.FROM_HTML_MODE_LEGACY);
        } else {
            return Html.fromHtml(source);
        }
    }

    public static CharSequence returnFormattedStringFromHtml(String source){
        return trim(fromHtml(fromHtml(source).toString()));
    }

    private static CharSequence trim(CharSequence text) {
        if (text.length()<=0){
            return text;
        }
        while (text.charAt(text.length() - 1) == '\n') {
            text = text.subSequence(0, text.length() - 1);
        }

        return text;
    }

    public static int getThemeAccentColor (final Context context) {
        final TypedValue value = new TypedValue ();
        context.getTheme ().resolveAttribute (R.attr.colorAccent, value, true);
        return value.data;
    }

    public static void setSDKSensitiveBackground(View view, Drawable background){
        if (Build.VERSION.SDK_INT>=16) {
            view.setBackground(background);
        } else {
            view.setBackgroundDrawable(background);
        }
    }

    public static int getSDKSensitiveColor(Context context, int resourceId){
        if (Build.VERSION.SDK_INT>=23){
            return context.getColor(resourceId);
        } else {
            return context.getResources().getColor(resourceId);
        }
    }

    public static void setFont(boolean makeCustom, @Nullable String fontPath){
        if (makeCustom){
            CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                    .setDefaultFontPath(fontPath)
                    .setFontAttrId(R.attr.fontPath)
                    .build());
        } else {
            CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                    .setDefaultFontPath("")
                    .setFontAttrId(R.attr.fontPath)
                    .build());
        }
    }

    public static void setFont(Context activityContext){
        Context context = activityContext.getApplicationContext();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        setFont(sp.getBoolean(context.getString(R.string.pref_key_use_custom_font), false), sp.getString(context.getString(R.string.pref_key_select_font), context.getString(R.string.font_calibri)));
    }

    public static void replaceFragment(AppCompatActivity activity, int containerId, Fragment fragment, String tag) {
        FragmentManager fm = activity.getSupportFragmentManager();
        android.support.v4.app.FragmentTransaction ft = fm.beginTransaction();
        ft.replace(containerId, fragment, tag);
        ft.commit();
    }
}
