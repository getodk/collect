
package org.odk.collect.android.application;

import org.apache.http.client.CookieStore;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.SyncBasicHttpContext;
import org.odk.collect.android.utilities.AgingCredentialsProvider;

import android.app.Application;
import android.os.Environment;
import android.util.Log;

import java.io.File;

public class Collect extends Application {

    // Storage paths
    public static final String ODK_ROOT = Environment.getExternalStorageDirectory() + "/odk";
    public static final String FORMS_PATH = ODK_ROOT + "/forms";
    public static final String INSTANCES_PATH = ODK_ROOT + "/instances";
    public static final String CACHE_PATH = ODK_ROOT + "/.cache";
    public static final String METADATA_PATH = ODK_ROOT + "/metadata";
    public static final String TMPFILE_PATH = CACHE_PATH + "/tmp.jpg";

    private HttpContext localContext = null;
    private static Collect singleton = null;


    public static Collect getInstance() {
        return singleton;
    }


    public static void createODKDirs() throws RuntimeException {
        String cardstatus = Environment.getExternalStorageState();
        if (cardstatus.equals(Environment.MEDIA_REMOVED)
                || cardstatus.equals(Environment.MEDIA_UNMOUNTABLE)
                || cardstatus.equals(Environment.MEDIA_UNMOUNTED)
                || cardstatus.equals(Environment.MEDIA_MOUNTED_READ_ONLY)
                || cardstatus.equals(Environment.MEDIA_SHARED)) {
            RuntimeException e =
                new RuntimeException("ODK reports :: SDCard error: "
                        + Environment.getExternalStorageState());
            throw e;
        }

        String[] dirs = {
                ODK_ROOT, FORMS_PATH, INSTANCES_PATH, CACHE_PATH, METADATA_PATH
        };

        for (String dirName : dirs) {
            File dir = new File(dirName);
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    RuntimeException e =
                        new RuntimeException("ODK reports :: Cannot create directory: " + dirName);
                    throw e;
                }
            } else {
                if (!dir.isDirectory()) {
                    RuntimeException e =
                        new RuntimeException("ODK reports :: " + dirName
                                + " exists, but is not a directory");
                    throw e;
                }
            }
        }
    }


    public synchronized HttpContext getHttpContext() {
        if (localContext == null) {
            // set up one context for all HTTP requests so that authentication
            // and cookies can be retained.
            localContext = new SyncBasicHttpContext(new BasicHttpContext());

            // establish a local cookie store for this attempt at downloading...
            CookieStore cookieStore = new BasicCookieStore();
            localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);

            // and establish a credentials provider...
            CredentialsProvider credsProvider = new AgingCredentialsProvider(7 * 60 * 1000);
            localContext.setAttribute(ClientContext.CREDS_PROVIDER, credsProvider);
        }
        return localContext;
    }


    @Override
    public void onCreate() {
        singleton = this;
        super.onCreate();
    }

}
