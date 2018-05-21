
package org.odk.collect.android.tasks;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.external.handler.SmapRemoteDataItem;
import org.odk.collect.android.listeners.SmapRemoteListener;
import org.odk.collect.android.preferences.PreferenceKeys;
import org.odk.collect.android.utilities.WebUtils;
import org.opendatakit.httpclientandroidlib.HttpEntity;
import org.opendatakit.httpclientandroidlib.HttpResponse;
import org.opendatakit.httpclientandroidlib.HttpStatus;
import org.opendatakit.httpclientandroidlib.client.HttpClient;
import org.opendatakit.httpclientandroidlib.client.methods.HttpGet;
import org.opendatakit.httpclientandroidlib.protocol.HttpContext;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import timber.log.Timber;

/**
 * Background task for appending a timer event to the timer log
 */
public class SmapRemoteWebServicePostTask extends AsyncTask<String, Void, SmapRemoteDataItem> {

    private String downloadUrl;
    private SmapRemoteListener remoteListener;

    @Override
    protected SmapRemoteDataItem doInBackground(String... params) {

        String downloadUrl = params[0];
        String timeoutValue = params[1];

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
            URI uri;
            boolean isCancelled = false;
            boolean success = false;
            try {
                // assume the downloadUrl is escaped properly
                URL url = new URL(downloadUrl);
                uri = url.toURI();
            } catch (MalformedURLException | URISyntaxException e) {
                Timber.e(e, "Unable to get a URI for download URL : %s  due to %s : ", downloadUrl, e.getMessage());
                throw e;
            }

            HttpContext localContext = Collect.getInstance().getHttpContext();

            HttpClient httpclient = WebUtils.createHttpClient(WebUtils.CONNECTION_TIMEOUT);

            // Add credentials
            SharedPreferences sharedPreferences =
                    PreferenceManager.getDefaultSharedPreferences(Collect.getInstance());

            String username = sharedPreferences.getString(PreferenceKeys.KEY_USERNAME, null);
            String password = sharedPreferences.getString(PreferenceKeys.KEY_PASSWORD, null);

            String server =
                    sharedPreferences.getString(PreferenceKeys.KEY_SERVER_URL, null);

            if (username != null && password != null) {
                Uri u = Uri.parse(downloadUrl);
                WebUtils.addCredentials(username, password, u.getHost());
            }

            // set up request...
            HttpGet req = WebUtils.createOpenRosaHttpGet(uri);

            response = httpclient.execute(req, localContext);
            int statusCode = response.getStatusLine().getStatusCode();

            if (statusCode != HttpStatus.SC_OK) {
                WebUtils.discardEntityBytes(response);
                if (statusCode == HttpStatus.SC_UNAUTHORIZED) {
                    // clear the cookies -- should not be necessary?
                    Collect.getInstance().getCookieStore().clear();
                }
                String errMsg =
                        Collect.getInstance().getString(R.string.file_fetch_failed, downloadUrl,
                                response.getStatusLine().getReasonPhrase(), String.valueOf(statusCode));
                Timber.e(errMsg);
                throw new Exception(errMsg);
            }

            HttpEntity entity = response.getEntity();
            is = entity.getContent();

            os = new ByteArrayOutputStream();
            byte[] buf = new byte[4096];
            int len;
            while ((len = is.read(buf)) > 0) {
                os.write(buf, 0, len);
            }
            os.flush();
            item.data = os.toString();

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