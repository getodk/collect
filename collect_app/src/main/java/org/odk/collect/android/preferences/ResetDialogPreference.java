package org.odk.collect.android.preferences;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;

import org.odk.collect.android.R;

public class ResetDialogPreference extends DialogPreference implements
        DialogInterface.OnClickListener {
    final static String TAG = "ResetDialogPreference";


    CheckBox mPreferences;
    CheckBox mInstances;
    Context mContext;

    public ResetDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDialogLayoutResource(R.layout.reset_dialog_layout);
        mContext = context;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            resetSelected();
        }
    }

    private void resetSelected() {
        boolean resetPreferences = mPreferences.isChecked();
        boolean resetInstances = mInstances.isChecked();

        if (!resetPreferences && !resetInstances) {
            Toast.makeText(getContext(), R.string.reset_dialog_nothing, Toast.LENGTH_LONG).show();
            return;
        }

        if (resetPreferences) {
            resetPreferences();
        }

        Toast.makeText(getContext(), R.string.reset_dialog_finished, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        ((Activity) mContext).finish();
    }

    private void resetPreferences() {
        PreferenceManager.getDefaultSharedPreferences(getContext())
                .edit()
                .clear()
                .commit();
    }

    private void restartApp(Context c) {
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
                        //create a pending intent so the application is restarted after System
                        // .exit(0) was called.
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

    @Override
    public void onBindDialogView(View view) {
        mPreferences = (CheckBox) view.findViewById(R.id.preferences);
        mInstances = (CheckBox) view.findViewById(R.id.instances);

        super.onBindDialogView(view);
    }

    @Override
    protected void onClick() {
        super.onClick();
    }
}