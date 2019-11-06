
package org.odk.collect.android.tasks;

import android.os.AsyncTask;

import org.odk.collect.android.application.Collect;
import org.odk.collect.android.http.openrosa.HttpCredentials;
import org.odk.collect.android.http.openrosa.OpenRosaHttpInterface;
import org.odk.collect.android.listeners.SmapLoginListener;
import org.odk.collect.android.utilities.WebCredentialsUtils;

import java.net.URI;
import java.net.URL;

import javax.inject.Inject;

import timber.log.Timber;

/**
 * Background task for getting values from the server
 */
public class SmapLoginTask extends AsyncTask<String, Void, String> {

    private SmapLoginListener remoteListener;

    @Inject
    OpenRosaHttpInterface httpInterface;

    @Inject
    WebCredentialsUtils webCredentialsUtils;

    public SmapLoginTask(){
        Collect.getInstance().getComponent().inject(this);
    };

    @Override
    protected String doInBackground(String... params) {

        String server = params[0];
        String username = params[1];
        String password = params[2];
        String status = null;

        try {

            URL url = new URL(server + "/login");
            URI uri = url.toURI();

            status = httpInterface.loginRequest(uri, null,
                    new HttpCredentials(username, password));

        } catch (Exception e) {
            status = "error: " + e.getLocalizedMessage();
        }

        return status;
    }

    @Override
    protected void onPostExecute(String status) {
        synchronized (this) {
            try {
                if (remoteListener != null) {
                    remoteListener.loginComplete(status);
                }
            } catch (Exception e) {
                Timber.e(e);
            }
        }
    }

    public void setListener(SmapLoginListener sl) {
        synchronized (this) {
            remoteListener = sl;
        }
    }


}