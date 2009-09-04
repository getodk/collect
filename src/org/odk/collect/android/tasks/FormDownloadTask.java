/*
 * Copyright (C) 2009 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.odk.collect.android.tasks;

import org.odk.collect.android.listeners.FormDownloaderListener;
import org.odk.collect.android.logic.GlobalConstants;

import android.os.AsyncTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Background task for downloading forms from a url.
 * 
 * @author carlhartung
 * 
 */
class FormDownloadTask extends AsyncTask<String, String, Boolean> {
    FormDownloaderListener mStateListener;
    String mName;


    @Override
    protected Boolean doInBackground(String... args) {

        String url = args[0];
        String path = args[1];

        int slash = path.lastIndexOf("/") + 1;
        int period = path.lastIndexOf(".") + 1;
        String base = path.substring(0, slash - 1);
        String filename = path.substring(slash, period - 1);
        String ext = path.substring(period);

        // create url
        URL u = null;
        try {
            u = new URL(args[0]);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return false;
        }

        try {
            // prevent deadlock when connection is invalid
            URLConnection c = u.openConnection();
            c.setConnectTimeout(GlobalConstants.CONNECTION_TIMEOUT);
            c.setReadTimeout(GlobalConstants.CONNECTION_TIMEOUT);

            // write connection to file
            InputStream is = c.getInputStream();

            // if file exists, append a number
            File f = new File(path);
            int i = 2;
            while (f.exists()) {
                f = new File(base + "/" + filename + " " + i + "." + ext);
                i++;
            }
            path = f.getAbsolutePath();

            OutputStream os = new FileOutputStream(f);
            byte buf[] = new byte[1024];
            int len;
            while ((len = is.read(buf)) > 0) {
                os.write(buf, 0, len);
            }
            os.flush();
            os.close();
            is.close();

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        mName = path.substring(path.lastIndexOf("/") + 1);

        return true;
    }


    @Override
    protected void onPostExecute(Boolean result) {

        synchronized (this) {
            if (mStateListener != null) {
                if (mName == null) {
                    mName = "item(s)";
                }
                mStateListener.downloadingComplete(result, mName);
            }
        }
    }


    public void setDownloaderListener(FormDownloaderListener sl) {
        mStateListener = sl;
    }
}
