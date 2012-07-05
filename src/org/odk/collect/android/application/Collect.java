/*
 * Copyright (C) 2011 University of Washington
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.odk.collect.android.application;

import java.io.File;

import org.apache.http.client.CookieStore;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.SyncBasicHttpContext;
import org.odk.collect.android.R;
import org.odk.collect.android.database.Logger;
import org.odk.collect.android.preferences.PreferencesActivity;
import org.odk.collect.android.utilities.AgingCredentialsProvider;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;

/**
 * Extends the Application class to implement 
 * @author carlhartung
 *
 */
public class Collect extends Application {

    // Storage paths
    public static final String ODK_ROOT = Environment.getExternalStorageDirectory() + File.separator + "odk";
    public static final String FORMS_PATH = ODK_ROOT + File.separator + "forms";
    public static final String INSTANCES_PATH = ODK_ROOT + File.separator + "instances";
    public static final String CACHE_PATH = ODK_ROOT + File.separator + ".cache";
    public static final String METADATA_PATH = ODK_ROOT + File.separator + "metadata";
    public static final String TMPFILE_PATH = CACHE_PATH + File.separator + "tmp.jpg";
    public static final String LOG_PATH = ODK_ROOT + "/log";
    
    public static final String DEFAULT_FONTSIZE = "21";

    private HttpContext localContext = null;
    private Logger mLogger;
    private static Collect singleton = null;


    public static Collect getInstance() {
        return singleton;
    }
    
    public Logger getLogger() {
    	return mLogger;
    }

    public static int getQuestionFontsize() {
        SharedPreferences settings =
                PreferenceManager.getDefaultSharedPreferences(Collect.getInstance());
        String question_font =
                settings.getString(PreferencesActivity.KEY_FONT_SIZE, Collect.DEFAULT_FONTSIZE);
        int questionFontsize = Integer.valueOf(question_font);
        return questionFontsize;
    }

    public String getVersionedAppName() {
        String versionDetail = "";
		try {
	        PackageInfo pinfo;
			pinfo = getPackageManager().getPackageInfo(getPackageName(), 0);
	        int versionNumber = pinfo.versionCode;
	        String versionName = pinfo.versionName;
	        versionDetail = " " + versionName + "(" + versionNumber + ")";
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return getString(R.string.app_name) + versionDetail;
    }
    /**
     * Creates required directories on the SDCard (or other external storage)
     * @throws RuntimeException if there is no SDCard or the directory exists as a non directory
     */
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


    /**
     * Shared HttpContext so a user doesn't have to re-enter login information
     * @return
     */
    public synchronized HttpContext getHttpContext() {
        if (localContext == null) {
            // set up one context for all HTTP requests so that authentication
            // and cookies can be retained.
            localContext = new SyncBasicHttpContext(new BasicHttpContext());

            // establish a local cookie store for this attempt at downloading...
            CookieStore cookieStore = new BasicCookieStore();
            localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);

            // and establish a credentials provider.  Default is 7 minutes.
            CredentialsProvider credsProvider = new AgingCredentialsProvider(7 * 60 * 1000);
            localContext.setAttribute(ClientContext.CREDS_PROVIDER, credsProvider);
        }
        return localContext;
    }


    @Override
    public void onCreate() {
        singleton = this;
        mLogger = new Logger();
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        super.onCreate();
    }

    
    public String getId() {
        TelephonyManager mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        return String.valueOf(Math.abs(mTelephonyManager.getDeviceId().hashCode()));
    }

}
