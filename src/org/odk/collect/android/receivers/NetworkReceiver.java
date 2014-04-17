package org.odk.collect.android.receivers;

import java.util.ArrayList;
import java.util.HashMap;

import org.odk.collect.android.R;
import org.odk.collect.android.listeners.InstanceUploaderListener;
import org.odk.collect.android.preferences.PreferencesActivity;
import org.odk.collect.android.provider.InstanceProviderAPI;
import org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns;
import org.odk.collect.android.tasks.InstanceUploaderTask;
import org.odk.collect.android.utilities.WebUtils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;

public class NetworkReceiver extends BroadcastReceiver implements InstanceUploaderListener {

    // turning on wifi often gets two CONNECTED events. we only want to run one thread at a time
    public static boolean running = false;
    InstanceUploaderTask mInstanceUploaderTask;


   @Override
	public void onReceive(Context context, Intent intent) {
        // make sure sd card is ready, if not don't try to send
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return;
        }

		String action = intent.getAction();
		ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo currentNetworkInfo = manager.getActiveNetworkInfo();

		if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
			if (currentNetworkInfo != null && currentNetworkInfo.getState() == NetworkInfo.State.CONNECTED) {
				if (interfaceIsEnabled(context, currentNetworkInfo)) {
					uploadForms(context);
				}
			}
		} else if (action.equals("org.odk.collect.android.FormSaved")) {
			ConnectivityManager connectivityManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo ni = connectivityManager.getActiveNetworkInfo();

			if (ni == null || !ni.isConnected()) {
				// not connected, do nothing
			} else {
				if (interfaceIsEnabled(context, ni)) {
					uploadForms(context);
				}
			}
		}
	}

	private boolean interfaceIsEnabled(Context context,
			NetworkInfo currentNetworkInfo) {
		// make sure autosend is enabled on the given connected interface
		SharedPreferences sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(context);
		boolean sendwifi = sharedPreferences.getBoolean(
				PreferencesActivity.KEY_AUTOSEND_WIFI, false);
		boolean sendnetwork = sharedPreferences.getBoolean(
				PreferencesActivity.KEY_AUTOSEND_NETWORK, false);

		return (currentNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI
				&& sendwifi || currentNetworkInfo.getType() == ConnectivityManager.TYPE_MOBILE
				&& sendnetwork);
	}


    private void uploadForms(Context context) {
        if (!running) {
            running = true;

            String selection = InstanceColumns.STATUS + "=? or " + InstanceColumns.STATUS + "=?";
            String selectionArgs[] =
                {
                        InstanceProviderAPI.STATUS_COMPLETE,
                        InstanceProviderAPI.STATUS_SUBMISSION_FAILED
                };

            Cursor c = null;
            try {
                c = context.getContentResolver().query(InstanceColumns.CONTENT_URI, null, selection,
                    selectionArgs, null);

	            ArrayList<Long> toUpload = new ArrayList<Long>();
	            if (c != null && c.getCount() > 0) {
	                c.move(-1);
	                while (c.moveToNext()) {
	                    Long l = c.getLong(c.getColumnIndex(InstanceColumns._ID));
	                    toUpload.add(Long.valueOf(l));
	                }

	                // get the username, password, and server from preferences
	                SharedPreferences settings =
	                        PreferenceManager.getDefaultSharedPreferences(context);

	                String storedUsername = settings.getString(PreferencesActivity.KEY_USERNAME, null);
	                String storedPassword = settings.getString(PreferencesActivity.KEY_PASSWORD, null);
	                String server = settings.getString(PreferencesActivity.KEY_SERVER_URL,
	                        context.getString(R.string.default_server_url));
	                String url = server
	                        + settings.getString(PreferencesActivity.KEY_FORMLIST_URL,
	                                context.getString(R.string.default_odk_formlist));

	                Uri u = Uri.parse(url);
	                WebUtils.addCredentials(storedUsername, storedPassword, u.getHost());

	                mInstanceUploaderTask = new InstanceUploaderTask();
	                mInstanceUploaderTask.setUploaderListener(this);

	                Long[] toSendArray = new Long[toUpload.size()];
	                toUpload.toArray(toSendArray);
	                mInstanceUploaderTask.execute(toSendArray);
	            } else {
	                running = false;
	            }
	        } finally {
	            if (c != null) {
	                c.close();
	            }
            }
        }
    }


    @Override
    public void uploadingComplete(HashMap<String, String> result) {
        // task is done
        mInstanceUploaderTask.setUploaderListener(null);
        running = false;
    }


    @Override
    public void progressUpdate(int progress, int total) {
        // do nothing
    }


    @Override
    public void authRequest(Uri url, HashMap<String, String> doneSoFar) {
        // if we get an auth request, just fail
        mInstanceUploaderTask.setUploaderListener(null);
        running = false;
    }

}