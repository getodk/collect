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

package org.odk.collect.android;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import android.os.AsyncTask;

/**
 * Background task for downloading forms from a url.
 * 
 * @author carlhartung
 *
 */
class FormDownloadTask extends AsyncTask<String, String, Boolean> {
    FormDownloaderListener mStateListener;


    /*
     * (non-Javadoc)
     * @see android.os.AsyncTask#doInBackground(Params[])
     */
    @Override
    protected Boolean doInBackground(String... path) {
        URL u = null;
        try {
            u = new URL(path[0]);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return false;
        }

        try {
        // prevent deadlock when connection is invalid
        URLConnection c = u.openConnection();
        c.setConnectTimeout(SharedConstants.CONNECTION_TIMEOUT);
        c.setReadTimeout(SharedConstants.CONNECTION_TIMEOUT);

        InputStream is = c.getInputStream();

        String filename = u.getFile();
        filename = filename.substring(filename.lastIndexOf('/') + 1);

        if (filename.matches(SharedConstants.VALID_FILENAME)) {
            File f = new File(SharedConstants.FORMS_PATH + "/" + filename);
            OutputStream os = new FileOutputStream(f);
            byte buf[] = new byte[1024];
            int len;
            while ((len = is.read(buf)) > 0)
                os.write(buf, 0, len);
            os.flush();
            os.close();
            is.close();
        } 
        }catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }


    /*
     * (non-Javadoc)
     * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
     */
    @Override
    protected void onPostExecute(Boolean result) {
        synchronized (this) {
            if (mStateListener != null) mStateListener.downloadingComplete(result);
        }
    }


    public void setDownloaderListener(FormDownloaderListener sl) {
        mStateListener = sl;
    }
}
