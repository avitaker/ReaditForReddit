package com.avinashdavid.readitforreddit.MiscUtils;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.Html;
import android.text.Spanned;
import android.util.TypedValue;
import android.view.View;

import com.avinashdavid.readitforreddit.LinkActivity;
import com.avinashdavid.readitforreddit.R;

/**
 * Created by avinashdavid on 3/8/17.
 */

public class GeneralUtils {
    public static int calculateTimeDifference(long timeNow, float timeThen){
//        long timeThenLong = (long)timeThen;
        long millisecondsDifference = timeNow - (long)(timeThen*1000);
        return (int)((millisecondsDifference/1000)/60)/60;
    }

    public static String returnFormattedTime(Context context, long timeNow, float timeThen){
        long millisecondsDifference = timeNow - (long)(timeThen*1000);
        int num = 0;
        if (millisecondsDifference<60000){
            return context.getString(R.string.just_now);
        } else if (millisecondsDifference>=60000 && millisecondsDifference<3.6e6){
            return context.getString(R.string.format_minuteselapsed, (int)(millisecondsDifference/60000));
        } else if (millisecondsDifference>=3.6e6 && millisecondsDifference<8.64e7){
            num = (int)(millisecondsDifference/3.6e6);
            if (num>1) {
                return context.getString(R.string.format_hourselapsed, num);
            } else {
                return context.getString(R.string.format_hour, num);
            }
        } else if (millisecondsDifference>=8.64e7 && millisecondsDifference<6.048e8){
            num = (int)(millisecondsDifference/8.64e7);
            if (num>1) {
                return context.getString(R.string.format_dayselapsed, num);
            } else {
                return context.getString(R.string.format_day, num);
            }
        } else if (millisecondsDifference>=6.048e8 && millisecondsDifference<3.1556952e10){
            num = (int)(millisecondsDifference/6.048e8);
            if (num>1) {
                return context.getString(R.string.format_monthselapsed, num);
            } else {
                return context.getString(R.string.format_month, num);
            }
        } else {
            num = (int)(millisecondsDifference/3.1556952e10);
            if (num>1) {
                return context.getString(R.string.format_yearselapsed, num);
            } else {
                return context.getString(R.string.format_year, num);
            }
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

    public static void openLinkInEnteralBrowser(Context context, String url){
        Intent i = new Intent(context, LinkActivity.class);
        i.putExtra(LinkActivity.EXTRA_URL, url);
        context.startActivity(i);
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
}
