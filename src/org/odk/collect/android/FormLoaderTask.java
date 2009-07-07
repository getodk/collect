package org.odk.collect.android;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.javarosa.core.model.FormDef;
import org.javarosa.xform.util.XFormUtils;

import android.os.AsyncTask;

class FormLoaderTask extends AsyncTask<String, String, FormHandler> {
    FormLoaderListener mStateListener;


    /*
     * (non-Javadoc)
     * @see android.os.AsyncTask#doInBackground(Params[])
     */
    @Override
    protected FormHandler doInBackground(String... path) {
        FormHandler fh = null;
        FormDef form = null;
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(new File(path[0]));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        form = XFormUtils.getFormFromInputStream(fis);
        if (form == null) return null;

        fh = new FormHandler(form);
        return fh;
    }


    /*
     * (non-Javadoc)
     * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
     */
    @Override
    protected void onPostExecute(FormHandler fh) {
        synchronized (this) {
            if (mStateListener != null) mStateListener.loadingComplete(fh);
        }
    }


    public void setFormLoaderListener(FormLoaderListener sl) {
        mStateListener = sl;
    }
}
