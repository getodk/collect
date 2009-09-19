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

import android.os.AsyncTask;

import org.odk.collect.android.listeners.FormDownloaderListener;
import org.odk.collect.android.logic.GlobalConstants;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

/**
 * Background task for downloading forms from a url.
 * 
 * @author carlhartung
 * 
 */
public class FormDownloadTask extends AsyncTask<String, Integer, ArrayList<String>> {

    FormDownloaderListener mStateListener;
    String mUrl;
    ArrayList<String> mDownloadedForms = new ArrayList<String>();

    public String formList = "formlist.xml";


    public void setDownloadServer(String newServer) {
        mUrl = newServer;
    }



    @Override
    protected ArrayList<String> doInBackground(String... values) {

        if (mUrl != null && mUrl.endsWith("formList")) {
            if (downloadFile(mUrl, formList)) {
                return mDownloadedForms;
            } else {
                return null;
            }
        } else {
            int formCount = values.length;
            for (int i = 0; i < formCount; i = i + 2) {
                downloadFile(values[i], values[i + 1]);
                // publishProgress(i + 2, formCount);
            }
            return mDownloadedForms;
        }

    }


    private boolean downloadFile(String url, String name) {

        // create url
        URL u = null;
        try {
            u = new URL(url);
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
            File f;
            if (name.equals(formList)) {
                f = new File(GlobalConstants.CACHE_PATH + name);
            } else {
                String path = GlobalConstants.FORMS_PATH + name;
                int i = 2;
                int slash = path.lastIndexOf("/") + 1;
                int period = path.lastIndexOf(".") + 1;
                String base = path.substring(0, slash - 1);
                String filename = path.substring(slash, period - 1);
                String ext = path.substring(period);
                f = new File(path);
                while (f.exists()) {
                    f = new File(base + "/" + filename + " " + i + "." + ext);
                    i++;
                }

            }

            OutputStream os = new FileOutputStream(f);
            byte buf[] = new byte[1024];
            int len;
            while ((len = is.read(buf)) > 0) {
                os.write(buf, 0, len);
            }
            os.flush();
            os.close();
            is.close();
            mDownloadedForms.add(url);

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }


    @Override
    protected void onPostExecute(ArrayList<String> value) {

        synchronized (this) {
            if (mStateListener != null) {
                mStateListener.downloadingComplete(value);
            }
        }
    }



    @Override
    protected void onProgressUpdate(Integer... values) {
        synchronized (this) {
            if (mStateListener != null) {
                // update progress and total
                mStateListener.progressUpdate(values[0].intValue(), values[1].intValue());
            }
        }

    }


    public void setDownloaderListener(FormDownloaderListener sl) {
        synchronized (this) {
            mStateListener = sl;
        }
    }
}
