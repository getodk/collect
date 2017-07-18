package org.odk.collect.android.tasks;

import android.content.Context;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.util.Log;

import org.odk.collect.android.listeners.NFCListener;

import java.math.BigInteger;

/**
 * Created by neilpenman on 27/01/2016.
 */
public class NdefReaderTask extends AsyncTask<Tag, Void, String> {

    public NFCListener mStateListener;

    @Override
    protected String doInBackground(Tag... params) {
        Tag tag = params[0];

        byte [] b = tag.getId();
        String tag_id = String.format("%0" + (b.length * 2) + "X", new BigInteger(1,b));

        Log.i("FT Tag: ", tag_id);

        return tag_id;
    }

    public void setDownloaderListener(NFCListener sl) {
        synchronized (this) {
            mStateListener = sl;
        }
    }

    @Override
    protected void onPostExecute(String result) {
        if (mStateListener != null) {
            mStateListener.readComplete(result);
        }
    }
}
