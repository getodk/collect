
package org.odk.collect.android.tasks;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.webkit.MimeTypeMap;

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

import timber.log.Timber;

import static com.google.common.io.Files.getFileExtension;

/**
 * Background task for appending a timer event to the timer log
 */
public class SmapRemoteWebServicePostTask extends AsyncTask<String, Void, SmapRemoteDataItem> {

    private SmapRemoteListener remoteListener;

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

                HttpContext localContext = Collect.getInstance().getHttpContext();
                HttpClient httpclient = WebUtils.createHttpClient(WebUtils.CONNECTION_TIMEOUT);

                // Add credentials
                SharedPreferences sharedPreferences =
                        PreferenceManager.getDefaultSharedPreferences(Collect.getInstance());

                String username = sharedPreferences.getString(PreferenceKeys.KEY_USERNAME, null);
                String password = sharedPreferences.getString(PreferenceKeys.KEY_PASSWORD, null);

                if (username != null && password != null) {
                    WebUtils.addCredentials(username, password, uri.getHost());
                }

                MultipartEntityBuilder builder = MultipartEntityBuilder.create();
                MimeTypeMap m = MimeTypeMap.getSingleton();
                String mime = m.getMimeTypeFromExtension(getFileExtension(fileName));
                ContentType contentType = null;
                if (mime != null) {
                    contentType = ContentType.create(mime);
                } else {
                    Timber.w("No specific MIME type found for file: %s", fileName);
                    contentType = ContentType.APPLICATION_OCTET_STREAM;
                }
                FileBody fb = new FileBody(file, contentType);
                builder.addPart(file.getName(), fb);

                // set up request...
                HttpPost httppost = WebUtils.createOpenRosaHttpPost(Uri.parse(uri.toString()));
                httppost.setEntity(builder.build());

                response = httpclient.execute(httppost, localContext);
                int statusCode = response.getStatusLine().getStatusCode();

                if (statusCode != HttpStatus.SC_OK) {
                    WebUtils.discardEntityBytes(response);
                    if (statusCode == HttpStatus.SC_UNAUTHORIZED) {
                        // clear the cookies -- should not be necessary?
                        Collect.getInstance().getCookieStore().clear();
                    }
                    String errMsg =
                            Collect.getInstance().getString(R.string.file_fetch_failed, lookupUrl,
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