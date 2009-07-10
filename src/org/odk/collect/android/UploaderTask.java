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
import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;

import android.os.AsyncTask;
import android.util.Log;


/**
 * Background task for uploading completed forms.
 * 
 * @author Carl Hartung (carlhartung@gmail.com)
 * 
 */
class UploaderTask extends AsyncTask<String, Integer, Void> {
    private final static String t = "UploaderTask";
    UploaderListener mStateListener;


    /*
     * (non-Javadoc)
     * @see android.os.AsyncTask#doInBackground(Params[])
     */
    @Override
    protected Void doInBackground(String... values) {
        for (int i = 0; i < values.length; i++) {
            this.publishProgress(i+1, values.length);
 
            DefaultHttpClient httpclient = new DefaultHttpClient();
            //this should get set in a preferences window or something.
            HttpPost mypost = new HttpPost("http://opendatakit.appspot.com/submission");
            File dir = new File(SharedConstants.ANSWERS_PATH + values[i]);
            File[] files = dir.listFiles();
            if (files == null)
                this.cancel(true);
            
            MultipartEntity entity = new MultipartEntity();
            Log.e(t, "# of files " + files.length);
            
            for(int j = 0; j < files.length; j++) {
                File f = files[j];
                if (f.getName().endsWith(".xml")) {
                    Log.i(t, "adding xml file: " + f.getName());
                    entity.addPart("xml_submission_file", new FileBody(f));
                }
                if (f.getName().endsWith(".png")) {
                    Log.i(t, "adding image file: " + f.getName());
                    entity.addPart("datafile", new FileBody(f));
                } else {
                    Log.i(t, "unhandled file: " + f.getName());
                }
                
            }
            
            mypost.setEntity(entity);
            HttpResponse response = null;
            try {
                response = httpclient.execute(mypost);
            } catch (ClientProtocolException e) {
                Log.e(t, "Protocol Exception Error");
                e.printStackTrace();
            } catch (IOException e) {
                Log.e(t, "IO Execption Error");
                e.printStackTrace();
            }
            if (response != null && response.getStatusLine().getStatusCode() == 200) {
                Log.d(t, "response: " + response.getStatusLine());
            } else {
                Log.d(t, "response: " + response.getStatusLine());
            }
        }
         

        // this is just for testing
        /*   for (int j = 0; j < 5; j++) {
               this.publishProgress(j, 5);
           
               for (int i = 0; i < 1000; i++) {
                   Log.e("testing", "wasiting time " + i );
               }   
               if (j == 3)
                   this.cancel(true);
           }*/
        return null;
    }


    /*
     * (non-Javadoc)
     * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
     */
    @Override
    protected void onPostExecute(Void unused) {
        synchronized (this) {
            if (mStateListener != null) mStateListener.uploadingComplete();
        }
    }



    @Override
    protected void onProgressUpdate(Integer... values) {
        synchronized (this) {
            if (mStateListener != null)
                mStateListener.progressUpdate(values[0].intValue(), values[1].intValue());
        }
    }


    public void setUploaderListener(UploaderListener sl) {
        mStateListener = sl;
    }
}
