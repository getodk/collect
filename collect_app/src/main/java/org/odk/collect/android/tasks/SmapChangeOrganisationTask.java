
package org.odk.collect.android.tasks;

import android.content.Intent;
import android.os.AsyncTask;

import org.odk.collect.android.application.Collect;
import org.odk.collect.android.external.handler.SmapRemoteDataItem;
import org.odk.collect.android.listeners.SmapRemoteListener;
import org.odk.collect.android.openrosa.OpenRosaHttpInterface;
import org.odk.collect.android.utilities.SmapInfoDownloader;
import org.odk.collect.android.utilities.WebCredentialsUtils;

import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;

import javax.inject.Inject;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import timber.log.Timber;

/**
 * Background task for changing the current organisation
 */
public class SmapChangeOrganisationTask extends AsyncTask<String, Void, String> {

    @Inject
    OpenRosaHttpInterface httpInterface;

    @Inject
    WebCredentialsUtils webCredentialsUtils;

    public SmapChangeOrganisationTask(){
        Collect.getInstance().getComponent().inject(this);
    };

    @Override
    protected String doInBackground(String... params) {

        String server = params[0];
        String orgName = params[1];

        int timeout = 1000;

        String changeUrl = server + "/api/v1/users/organisation/" + android.net.Uri.encode(orgName, "UTF-8");
       
        InputStream is = null;
        String status = null;
        try {

            URL url = new URL(changeUrl);
            URI uri = url.toURI();

            is = httpInterface.executeGetRequest(uri, null, webCredentialsUtils.getCredentials(uri)).getInputStream();

            HashMap<String, String> headers = new HashMap<String, String> ();
            status = httpInterface.getRequest(uri, "application/json", webCredentialsUtils.getCredentials(uri), headers);

            DownloadTasksTask dt = new DownloadTasksTask();
            dt.doInBackground();


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