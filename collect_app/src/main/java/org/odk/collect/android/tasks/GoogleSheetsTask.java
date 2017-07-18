/*
 * Copyright (C) 2014 Nafundi
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

package org.odk.collect.android.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;

import org.odk.collect.android.listeners.InstanceUploaderListener;

import java.io.IOException;

/**
 * @author carlhartung (chartung@nafundi.com)
 */
public abstract class GoogleSheetsTask<Params, Progress, Result> extends
        AsyncTask<Params, Progress, Result> {

    private final static String tag = "GoogleSheetsTask";

    public final static int PLAYSTORE_REQUEST_CODE = 55551;
    public final static int USER_RECOVERABLE_REQUEST_CODE = 55552;

    protected String mGoogleUserName = null;
    protected InstanceUploaderListener mStateListener;

    public void setUserName(String username) {
        mGoogleUserName = username;
    }

    public void setUploaderListener(InstanceUploaderListener sl) {
        synchronized (this) {
            mStateListener = sl;
        }
    }

    protected String authenticate(Context context, String mGoogleUserName) throws IOException,
            GoogleAuthException {
        // use google auth utils to get oauth2 token
        String scope =
                "oauth2:https://spreadsheets.google.com/feeds https://picasaweb.google.com/data/";
        String token = null;

        if (mGoogleUserName == null) {
            Log.e(tag, "Google user not set");
            return null;
        }

        token = GoogleAuthUtil.getToken(context, mGoogleUserName, scope);
        return token;

    }

}
