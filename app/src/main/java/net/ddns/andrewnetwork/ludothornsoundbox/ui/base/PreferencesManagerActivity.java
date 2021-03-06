package net.ddns.andrewnetwork.ludothornsoundbox.ui.base;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.StyleRes;

import net.ddns.andrewnetwork.ludothornsoundbox.R;
import net.ddns.andrewnetwork.ludothornsoundbox.ui.base.core.extras.ActivitiesManager;
import net.ddns.andrewnetwork.ludothornsoundbox.ui.main.MainActivity;

public abstract class PreferencesManagerActivity extends BaseActivity {
    protected boolean loadAtOnce;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences settings = getSharedPreferences("net.ddns.andrewnetwork.ludothornsoundbox_preferences", MODE_PRIVATE);

        managePreferences(settings);

    }

    protected void managePreferences(SharedPreferences settings) {

        this.loadAtOnce = getLoadAtOnce(settings);
        getFontPreference(settings);
    }

    private void getFontPreference(SharedPreferences settings) {
        boolean isAppFont = settings.getBoolean(getString(R.string.usa_font_app_key), false);

        Resources.Theme theme = getTheme();

        @StyleRes int themeID = R.style.DefaultFont;

        if (isAppFont) {
            themeID = R.style.AppFont;
        }

        theme.applyStyle(themeID, true);

    }

    //OLD METHOD TO RESTART APP
    public static void doRestart(Context c) {
        try {
            //check if the context is given
            if (c != null) {
                //fetch the packagemanager so we can get the default launch activity
                // (you can replace this intent with any other activity if you want
                PackageManager pm = c.getPackageManager();
                //check if we got the PackageManager
                if (pm != null) {
                    //create the intent with the default start activity for your application
                    Intent mStartActivity = pm.getLaunchIntentForPackage(
                            c.getPackageName()
                    );
                    if (mStartActivity != null) {
                        mStartActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        //create a pending intent so the application is restarted after System.exit(0) was called.
                        // We use an AlarmManager to call this intent in 100ms
                        int mPendingIntentId = 223344;
                        PendingIntent mPendingIntent = PendingIntent
                                .getActivity(c, mPendingIntentId, mStartActivity,
                                        PendingIntent.FLAG_CANCEL_CURRENT);
                        AlarmManager mgr = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
                        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
                        //kill the application
                        System.exit(0);
                    } else {
                        Log.e(TAG, "Was not able to restart application, mStartActivity null");
                    }
                } else {
                    Log.e(TAG, "Was not able to restart application, PM null");
                }
            } else {
                Log.e(TAG, "Was not able to restart application, Context null");
            }
        } catch (Exception ex) {
            Log.e(TAG, "Was not able to restart application");
        }
    }

    public void restartApp() {

        closeOtherActivities();

        Intent mStartActivity = new Intent(this, MainActivity.class);
        int mPendingIntentId = 123456;
        PendingIntent mPendingIntent = PendingIntent.getActivity(this, mPendingIntentId, mStartActivity,
                PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager mgr = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
        System.exit(0);

    }

    private boolean getLoadAtOnce(SharedPreferences settings) {
        return settings.getBoolean(getString(R.string.carica_audio_insieme_key), false);
    }

    private void closeOtherActivities() {
        for (Activity activity : ActivitiesManager.getInstance().getActivityStack()) {
            if (this != activity) {
                activity.finish();
            }
        }
    }
}
