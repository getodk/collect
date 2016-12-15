/*
 * Copyright (C) Smap Consulting
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

package org.odk.collect.android.activities;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.listeners.NFCListener;
import org.odk.collect.android.tasks.NdefReaderTask;

import java.text.DecimalFormat;

public class NFCActivity extends Activity implements NFCListener {

    private ProgressDialog mNfcDialog;
    private String mNfcId;
    private NfcAdapter mNfcAdapter;		// NFC
    public NdefReaderTask mReadNFC;
    private PendingIntent mNfcPendingIntent;
    private IntentFilter[] mNfcFilters;
    private String TAG = "NFCActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();


        setTitle(getString(R.string.app_name) + " > " + getString(R.string.smap_read_nfc));

        /*
		 * NFC
		 */
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if (mNfcAdapter == null) {
            Toast.makeText(
                    NFCActivity.this,
                    getString(R.string.smap_nfc_not_available),
                    Toast.LENGTH_SHORT).show();
            finish();
        } else if (!mNfcAdapter.isEnabled()) {
            Toast.makeText(
                    NFCActivity.this,
                    getString(R.string.smap_nfc_not_enabled),
                    Toast.LENGTH_SHORT).show();
            finish();
        } else {
            /*
             * Set up NFC adapter
             */

            // Pending intent
            Intent nfcIntent = new Intent(getApplicationContext(), getClass());
            nfcIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            mNfcPendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, nfcIntent, 0);

            // Filter
            IntentFilter filter = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
            mNfcFilters = new IntentFilter[] {
                    filter
            };


            Toast.makeText(
                    NFCActivity.this,
                    getString(R.string.smap_nfc_is_available),
                    Toast.LENGTH_SHORT).show();

        }

        setupNfcDialog();

    }

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

    @Override
    protected void onPause() {
        super.onPause();


        if(mNfcAdapter != null) {
            stopNFCDispatch(this, mNfcAdapter);        // NFC
        }


        // We're not using managed dialogs, so we have to dismiss the dialog to prevent it from
        // leaking memory.
        if (mNfcDialog != null && mNfcDialog.isShowing())
            mNfcDialog.dismiss();
    }


    @Override
    protected void onResume() {
        super.onResume();

        if(mNfcAdapter != null && mNfcAdapter.isEnabled()) {
            setupNFCDispatch(this, mNfcAdapter);        // NFC
        }

        mNfcDialog.show();
    }

    @Override
    protected void onStart() {
    	super.onStart();
		Collect.getInstance().getActivityLogger().logOnStart(this);
    }

    @Override
    protected void onStop() {
		Collect.getInstance().getActivityLogger().logOnStop(this);
    	super.onStop();
    }

    /**
     * Sets up the look and actions for the progress dialog while the GPS is searching.
     */
    private void setupNfcDialog() {
    	Collect.getInstance().getActivityLogger().logInstanceAction(this, "setupNFCDialog", "show");
        // dialog displayed while reading NFC
        mNfcDialog = new ProgressDialog(this);
        DialogInterface.OnClickListener nfcButtonListener =
            new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case DialogInterface. BUTTON_NEGATIVE:
                            Collect.getInstance().getActivityLogger().logInstanceAction(this, "cancelLocation", "cancel");
                            mNfcId = null;
                            finish();
                            break;
                    }
                }
            };

        // back button doesn't cancel
        mNfcDialog.setCancelable(false);
        mNfcDialog.setIndeterminate(true);
        mNfcDialog.setIcon(android.R.drawable.ic_dialog_info);
        mNfcDialog.setTitle(getString(R.string.smap_reading_nfc));
        mNfcDialog.setMessage(getString(R.string.smap_swipe_nfc));
        //mLocationDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.accept_location),
        //    geopointButtonListener);
        mNfcDialog.setButton(DialogInterface. BUTTON_NEGATIVE, getString(R.string.cancel_location),
            nfcButtonListener);
    }


    private void returnNfc(String id) {

        Intent i = new Intent();
        i.putExtra(FormEntryActivity.NFC_RESULT, id);
        setResult(RESULT_OK, i);

        finish();
    }





    private String truncateDouble(float number) {
        DecimalFormat df = new DecimalFormat("#.##");
        return df.format(number);
    }


    @Override
    public void readComplete(String result) {

        Log.i(TAG, "NFC tag read: " + result);
        returnNfc(result);

    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleNFCIntent(intent);
    }

    /*
     * NFC detected
     */
    private void handleNFCIntent(Intent intent) {

        Log.i(TAG, "NFC tag discovered");
        String action = intent.getAction();
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

        mReadNFC = new NdefReaderTask();
        mReadNFC.setDownloaderListener(this);
        mReadNFC.execute(tag);

    }

    /**
     * @param activity The corresponding {@link Activity} requesting the foreground dispatch.
     * @param adapter The {@link NfcAdapter} used for the foreground dispatch.
     */
    public void setupNFCDispatch(final Activity activity, NfcAdapter adapter) {
        adapter.enableForegroundDispatch(activity, mNfcPendingIntent, mNfcFilters, null);
    }

    /**
     * @param activity The corresponding {@link Activity} requesting to stop the foreground dispatch.
     * @param adapter The {@link NfcAdapter} used for the foreground dispatch.
     */
    public static void stopNFCDispatch(final Activity activity, NfcAdapter adapter) {

        if (adapter != null) {
            adapter.disableForegroundDispatch(activity);
        }
    }








}
