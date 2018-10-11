
package org.odk.collect.android.tasks;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.webkit.MimeTypeMap;

import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.external.handler.SmapRemoteDataItem;
import org.odk.collect.android.http.OpenRosaHttpInterface;
import org.odk.collect.android.listeners.SmapRemoteListener;
import org.odk.collect.android.preferences.PreferenceKeys;
import org.odk.collect.android.utilities.WebCredentialsUtils;
import org.odk.collect.android.utilities.WebUtils;
import org.opendatakit.httpclientandroidlib.HttpEntity;
import org.opendatakit.httpclientandroidlib.HttpResponse;
import org.opendatakit.httpclientandroidlib.HttpStatus;
import org.opendatakit.httpclientandroidlib.client.HttpClient;
import org.opendatakit.httpclientandroidlib.client.methods.HttpPost;
import org.opendatakit.httpclientandroidlib.entity.ContentType;
import org.opendatakit.httpclientandroidlib.entity.mime.MultipartEntityBuilder;
import org.opendatakit.httpclientandroidlib.entity.mime.content.FileBody;
import org.opendatakit.httpclientandroidlib.protocol.HttpContext;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
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

        InputStream is = null;
        ByteArrayOutputStream os = null;
        HttpResponse response;

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

                item.data = httpInterface.SubmitFileForResponse(fileName, file, uri, webCredentialsUtils.getCredentials(uri)).toString();

            } else {
                item.data = "";
            }

        } catch (Exception e) {
            item.data = e.getLocalizedMessage();
            Timber.e(e.toString());

        } finally {

            if (os != null) { try {
                    os.close();
                } catch (Exception ex) {
                    // no-op
                }
            }

            if (is != null) {
                try {
                    // ensure stream is consumed...
                    final long count = 1024L;
                    while (is.skip(count) == count) {
                        // skipping to the end of the http entity
                    }
                } catch (Exception ex) {
                    // no-op
                }
                try {
                    is.close();
                } catch (Exception ex) {
                    // no-op
                }
            }
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