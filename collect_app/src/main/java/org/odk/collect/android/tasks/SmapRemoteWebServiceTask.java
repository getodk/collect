
package org.odk.collect.android.tasks;

import android.os.AsyncTask;

import org.odk.collect.android.application.Collect;
import org.odk.collect.android.external.handler.SmapRemoteDataItem;
import org.odk.collect.android.http.OpenRosaHttpInterface;
import org.odk.collect.android.listeners.SmapRemoteListener;
import org.odk.collect.android.utilities.WebCredentialsUtils;
import org.opendatakit.httpclientandroidlib.HttpResponse;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

import javax.inject.Inject;

import timber.log.Timber;

import static java.lang.Thread.sleep;

/**
 * Background task for getting values from the server
 */
public class SmapRemoteWebServiceTask extends AsyncTask<String, Void, SmapRemoteDataItem> {

    private SmapRemoteListener remoteListener;

    @Inject
    OpenRosaHttpInterface httpInterface;

    @Inject
    WebCredentialsUtils webCredentialsUtils;

    public SmapRemoteWebServiceTask(){
        Collect.getInstance().getComponent().inject(this);};

    @Override
    protected SmapRemoteDataItem doInBackground(String... params) {

        String lookupUrl = params[0];
        String timeoutValue = params[1];
        String choices = params[2];

        int timeout = 0;
        try {
            timeout = Integer.valueOf(timeoutValue);
        } catch (Exception e) {

        }

        SmapRemoteDataItem item = new SmapRemoteDataItem();
        item.key = params[0];
        item.data = null;
        if(timeout == 0) {
            item.perSubmission = true;
        }
        if(params[2] != null && params[2].equals("true")) {
            item.choices = true;
        }

        try {

            URL url = new URL(lookupUrl);
            URI uri = url.toURI();

            item.data = httpInterface.getRequest(uri, null, webCredentialsUtils.getCredentials(uri));

        } catch (Exception e) {
            item.data = e.getLocalizedMessage();
            Timber.e(e);

        } finally {


        }

        return item;
    }

    @Override
    protected void onPostExecute(SmapRemoteDataItem data) {
        synchronized (this) {
            try {
                if (remoteListener != null) {
                    remoteListener.remoteComplete(data);
                }
            } catch (Exception e) {
                Timber.e(e);
            }
        }
    }

    public void setSmapRemoteListener(SmapRemoteListener sl) {
        synchronized (this) {
            remoteListener = sl;
        }
    }
}