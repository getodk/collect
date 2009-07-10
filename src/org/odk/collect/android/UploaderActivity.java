package org.odk.collect.android;

import java.util.ArrayList;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class UploaderActivity extends Activity implements UploaderListener {

    private final static int PROGRESS_DIALOG = 1;
    private ProgressDialog mProgressDialog;
    private UploaderTask mUploaderTask;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ArrayList<String> toUpload = null;
        Intent i = this.getIntent();
        Bundle b = i.getBundleExtra("BUNDLE");
        if (b != null) {
            toUpload = b.getStringArrayList("UPLOAD");
            for (int j = 0; j < toUpload.size(); j++) {
                Log.e("testing", "got " + toUpload.get(j));
            }
        } else {
            Log.e("testing", "Was also null");
        }

        String serverurl = "something";

        mUploaderTask = (UploaderTask) getLastNonConfigurationInstance();
        if (mUploaderTask == null) {
            showDialog(PROGRESS_DIALOG);
            mUploaderTask = new UploaderTask();
            mUploaderTask.execute(serverurl);
        } else {
            Log.e("testing", "alreaedy ruhnning");
        }
    }



    @Override
    public Object onRetainNonConfigurationInstance() {
        return mUploaderTask;
    }



    @Override
    protected void onDestroy() {
        mUploaderTask.setUploaderListener(null);
        super.onDestroy();
    }



    @Override
    protected void onResume() {
        if (mUploaderTask != null) mUploaderTask.setUploaderListener(this);
        super.onResume();
    }



    public void uploadingComplete() {
        finish();
    }



    public void progressUpdate(int progress, int total) {
        mProgressDialog.setProgress(progress);
    }


    /*
     * (non-Javadoc)
     * @see android.app.Activity#onCreateDialog(int)
     */
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case PROGRESS_DIALOG:
                mProgressDialog = new ProgressDialog(this);
                DialogInterface.OnClickListener loadingButtonListener =
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                mUploaderTask.setUploaderListener(null);
                                finish();
                            }
                        };
                mProgressDialog.setMessage(getString(R.string.loading_form));
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                mProgressDialog.setCancelable(false);
                mProgressDialog.setMax(5);
                mProgressDialog.setButton(getString(R.string.cancel), loadingButtonListener);
                return mProgressDialog;
        }
        return null;
    }



}
