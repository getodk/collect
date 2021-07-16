
package org.odk.collect.android.smap.tasks;

import android.os.AsyncTask;

import org.odk.collect.android.application.Collect;
import org.odk.collect.android.openrosa.OpenRosaHttpInterface;
import org.odk.collect.android.tasks.DownloadTasksTask;
import org.odk.collect.android.utilities.WebCredentialsUtils;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;

import javax.inject.Inject;

import timber.log.Timber;

/**
 * Background task for changing the current organisation
 */
public class SubmitLocationTask extends AsyncTask<String, Void, String> {

    @Inject
    OpenRosaHttpInterface httpInterface;

    @Inject
    WebCredentialsUtils webCredentialsUtils;

    public SubmitLocationTask(){
        Collect.getInstance().getComponent().inject(this);
    };

    @Override
    protected String doInBackground(String... params) {

        String server = params[0];
        String latString = params[1];
        String lonString = params[2];

        int timeout = 1000;

        String changeUrl = server + "/api/v1/users/location";
        InputStream is = null;
        String status = null;
        try {

            URL url = new URL(changeUrl);
            URI uri = url.toURI();

            // Validate
            Double lat = 0.0;
            Double lon = 0.0;
            try {
                lat = Double.valueOf(latString);
                lon = Double.valueOf(lonString);
            } catch (Exception e) {

            }

            if(!(lat == 0.0 && lon == 0.0)) {
                httpInterface.uploadLocation(latString, lonString, uri, webCredentialsUtils.getCredentials(uri));
            }


        } catch (Exception e) {
            Timber.e(e);

        } finally {

            if(is != null) {
                try {
                    is.close();
                } catch(Exception e) {

                }
            }
        }

        return status;

    }


}