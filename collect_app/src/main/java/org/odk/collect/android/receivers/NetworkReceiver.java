
package org.odk.collect.android.receivers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.odk.collect.android.R;
import org.odk.collect.android.activities.NotificationActivity;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.listeners.InstanceUploaderListener;
import org.odk.collect.android.preferences.PreferencesActivity;
import org.odk.collect.android.provider.InstanceProviderAPI;
import org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns;
import org.odk.collect.android.tasks.GoogleMapsEngineAbstractUploader;
import org.odk.collect.android.tasks.InstanceUploaderTask;
import org.odk.collect.android.utilities.WebUtils;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GooglePlayServicesAvailabilityException;
import com.google.android.gms.auth.UserRecoverableAuthException;

public class NetworkReceiver extends BroadcastReceiver implements InstanceUploaderListener {

    // turning on wifi often gets two CONNECTED events. we only want to run one thread at a time
    public static boolean running = false;
    InstanceUploaderTask mInstanceUploaderTask;

    GoogleMapsEngineAutoUploadTask mGoogleMapsEngineUploadTask;

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

            ArrayList<Long> toUpload = new ArrayList<Long>();
            Cursor c = context.getContentResolver().query(InstanceColumns.CONTENT_URI, null,
                    selection, selectionArgs, null);
            try {
                if (c != null && c.getCount() > 0) {
                    c.move(-1);
                    while (c.moveToNext()) {
                        Long l = c.getLong(c.getColumnIndex(InstanceColumns._ID));
                        toUpload.add(Long.valueOf(l));
                    }
                }
            } finally {
                if (c != null) {
                    c.close();
                }
            }

            if (toUpload.size() < 1) {
                running = false; 
                return;
            }

            Long[] toSendArray = new Long[toUpload.size()];
            toUpload.toArray(toSendArray);

            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);

            String protocol = settings.getString(PreferencesActivity.KEY_PROTOCOL,
            		context.getString(R.string.protocol_odk_default));

            if (protocol.equals(context.getString(R.string.protocol_google_maps_engine))) {
                mGoogleMapsEngineUploadTask = new GoogleMapsEngineAutoUploadTask(context);
                String googleUsername = settings.getString(
                        PreferencesActivity.KEY_SELECTED_GOOGLE_ACCOUNT, null);
                if (googleUsername == null || googleUsername.equalsIgnoreCase("")) {
                    // just quit if there's no username
                    running = false;
                    return;
                }
                mGoogleMapsEngineUploadTask.setUserName(googleUsername);
                mGoogleMapsEngineUploadTask.setUploaderListener(this);
                mGoogleMapsEngineUploadTask.execute(toSendArray);

            } else {
                // get the username, password, and server from preferences

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

                mInstanceUploaderTask.execute(toSendArray);
            }
        }
    }

    @Override
    public void uploadingComplete(HashMap<String, String> result) {
        // task is done
        if (mInstanceUploaderTask != null) {
            mInstanceUploaderTask.setUploaderListener(null);
        }
        if (mGoogleMapsEngineUploadTask != null) {
            mGoogleMapsEngineUploadTask.setUploaderListener(null);
        }
        running = false;

        StringBuilder message = new StringBuilder();
        message.append(Collect.getInstance().getString(R.string.odk_auto_note) + " :: \n\n");

        if (result == null) {
            message.append(Collect.getInstance().getString(R.string.odk_auth_auth_fail));
        } else {

            StringBuilder selection = new StringBuilder();
            Set<String> keys = result.keySet();
            Iterator<String> it = keys.iterator();

            String[] selectionArgs = new String[keys.size()];
            int i = 0;
            while (it.hasNext()) {
                String id = it.next();
                selection.append(InstanceColumns._ID + "=?");
                selectionArgs[i++] = id;
                if (i != keys.size()) {
                    selection.append(" or ");
                }
            }

            {
                Cursor results = null;
                try {
                    results = Collect
                            .getInstance()
                            .getContentResolver()
                            .query(InstanceColumns.CONTENT_URI, null, selection.toString(),
                                    selectionArgs, null);
                    if (results.getCount() > 0) {
                        results.moveToPosition(-1);
                        while (results.moveToNext()) {
                            String name = results.getString(results
                                    .getColumnIndex(InstanceColumns.DISPLAY_NAME));
                            String id = results.getString(results
                                    .getColumnIndex(InstanceColumns._ID));
                            message.append(name + " - " + result.get(id) + "\n\n");
                        }
                    }
                } finally {
                    if (results != null) {
                        results.close();
                    }
                }
            }
        }

        Intent notifyIntent = new Intent(Collect.getInstance(), NotificationActivity.class);
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        notifyIntent.putExtra(NotificationActivity.NOTIFICATION_KEY, message.toString().trim());

        PendingIntent pendingNotify = PendingIntent.getActivity(Collect.getInstance(), 0,
                notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(Collect.getInstance())
                .setSmallIcon(R.drawable.notes)
                .setContentTitle(Collect.getInstance().getString(R.string.odk_auto_note))
                .setContentIntent(pendingNotify)
                .setContentText(message.toString().trim())
                .setAutoCancel(true)
                .setLargeIcon(
                        BitmapFactory.decodeResource(Collect.getInstance().getResources(),
                                android.R.drawable.ic_dialog_info));

        NotificationManager mNotificationManager = (NotificationManager)Collect.getInstance()
                .getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(1328974928, mBuilder.build());
    }


    @Override
    public void progressUpdate(int progress, int total) {
        // do nothing
    }


    @Override
    public void authRequest(Uri url, HashMap<String, String> doneSoFar) {
        // if we get an auth request, just fail
        if (mInstanceUploaderTask != null) {
            mInstanceUploaderTask.setUploaderListener(null);
        }
        if (mGoogleMapsEngineUploadTask != null) {
            mGoogleMapsEngineUploadTask.setUploaderListener(null);
        }
        running = false;
    }

    private class GoogleMapsEngineAutoUploadTask extends
            GoogleMapsEngineAbstractUploader<Long, Integer, HashMap<String, String>> {

        private Context mContext;

        public GoogleMapsEngineAutoUploadTask(Context c) {
            mContext = c;
        }

        @Override
        protected HashMap<String, String> doInBackground(Long... values) {

            mResults = new HashMap<String, String>();

            String selection = InstanceColumns._ID + "=?";
            String[] selectionArgs = new String[(values == null) ? 0 : values.length];
            if (values != null) {
                for (int i = 0; i < values.length; i++) {
                    if (i != values.length - 1) {
                        selection += " or " + InstanceColumns._ID + "=?";
                    }
                    selectionArgs[i] = values[i].toString();
                }
            }

            String token = null;
            try {
                token = authenticate(mContext, mGoogleUserName);
            } catch (IOException e) {
                // network or server error, the call is expected to succeed if
                // you try again later. Don't attempt to call again immediately
                // - the request is likely to fail, you'll hit quotas or
                // back-off.
                return null;
            } catch (GooglePlayServicesAvailabilityException playEx) {
                return null;
            } catch (UserRecoverableAuthException e) {
                e.printStackTrace();
                return null;
            } catch (GoogleAuthException e) {
                // Failure. The call is not expected to ever succeed so it
                // should not be retried.
                return null;
            }
            mContext = null;

            if (token == null) {
                // failed, so just return
                return null;
            }

            uploadInstances(selection, selectionArgs, token);
            return mResults;
        }
    }

}
