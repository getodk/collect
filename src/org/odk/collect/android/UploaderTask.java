package org.odk.collect.android;

import android.os.AsyncTask;
import android.util.Log;

class UploaderTask extends AsyncTask<String, Integer, Void> {
    UploaderListener mStateListener;


    /*
     * (non-Javadoc)
     * @see android.os.AsyncTask#doInBackground(Params[])
     */
    @Override
    protected Void doInBackground(String... path) {
   
        /*
        File server = new File(path[0]);
        Log.e("testing", "trying host " + server);
        DefaultHttpClient httpclient = new DefaultHttpClient();
        HttpPost mypost = new HttpPost(server.toString());
        
        
        File xml = new File("/sdcard/odk/answers/sensing_2009-04-08_16-52-56/sensing_2009-04-08_16-52-56.xml");
        File image = new File("/sdcard/odk/answers/sensing_2009-04-08_16-52-56/image_1239234775827.png");            
        MultipartEntity entity = new MultipartEntity();
        entity.addPart("xmlfile", new FileBody(xml));
        if (image != null)
            entity.addPart("imagefile", new FileBody(image));
        mypost.setEntity(entity);

        HttpResponse response = null;
        try {
            response = httpclient.execute(mypost);
        } catch (ClientProtocolException e) {
            Log.e("testing", "Protocol Exception Error");
            e.printStackTrace();
        } catch (IOException e) {
            Log.e("testing", "IO Execption Error");
            e.printStackTrace();
        }
        if (response != null && response.getStatusLine().getStatusCode() == 200) {
            Log.d("httpPost", "response: " + response.getStatusLine());
        } else {
            Log.d("httpPost", "response: " + response.getStatusLine());
        }*/
        
        //this is just for testing
        for (int j = 0; j < 5; j++) {
            this.publishProgress(j, 5);
        
            for (int i = 0; i < 1000; i++) {
                Log.e("testing", "wasiting time " + i );
            }   
        }
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
            if (mStateListener != null) mStateListener.progressUpdate(values[0].intValue(), values[1].intValue());
        }
    }


    public void setUploaderListener(UploaderListener sl) {
        mStateListener = sl;
    }
}
