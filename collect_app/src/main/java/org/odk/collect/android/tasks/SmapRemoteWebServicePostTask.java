
package org.odk.collect.android.tasks;

import android.os.AsyncTask;

import org.odk.collect.android.application.Collect;
import org.odk.collect.android.external.handler.SmapRemoteDataItem;
import org.odk.collect.android.http.OpenRosaHttpInterface;
import org.odk.collect.android.listeners.SmapRemoteListener;
import org.odk.collect.android.utilities.WebCredentialsUtils;

import java.io.File;
import java.net.URI;
import java.net.URL;

import javax.inject.Inject;

import timber.log.Timber;

import static com.google.common.io.Files.getFileExtension;

/**
 * Background task for sending a file to the sever and getting a response
 */
public class SmapRemoteWebServicePostTask extends AsyncTask<String, Void, SmapRemoteDataItem> {

    private SmapRemoteListener remoteListener;

    @Inject
    OpenRosaHttpInterface httpInterface;

    @Inject
    WebCredentialsUtils webCredentialsUtils;

    public SmapRemoteWebServicePostTask() {Collect.getInstance().getComponent().inject(this);}

    @Override
    protected SmapRemoteDataItem doInBackground(String... params) {

        String lookupUrl = params[0];
        String fileName = params[1];
        String timeoutValue = params[2];

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

        try {
            String filePath = Collect.getInstance().getFormController().getInstanceFile().getParent() +
                    File.separator + fileName;
            File file = new File(filePath);
            String extension = getFileExtension(fileName);
            if(file.exists() && extension.equals("jpg")) {

                URL url = new URL(lookupUrl);
                URI uri = url.toURI();

                item.data = httpInterface.SubmitFileForResponse(fileName, file, uri, webCredentialsUtils.getCredentials(uri));

            } else {
                item.data = "";
            }

        } catch (Exception e) {
            item.data = e.getLocalizedMessage();
            Timber.e(e.toString());

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