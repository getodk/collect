package org.odk.collect.android.activities;

import android.app.NotificationManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.WindowManager;

import org.odk.collect.android.R;
import org.odk.collect.android.fragments.dialogs.SimpleDialog;

public class NotificationActivity extends CollectAbstractActivity {

    public static final int NOTIFICATION_ID = 191919191;                // smap
    public static final String NOTIFICATION_TITLE = "title";
    public static final String NOTIFICATION_MESSAGE = "message";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        makeTheActivityTransparent();

        // Start smap
        NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotifyMgr.cancel(NOTIFICATION_ID);
        // end smap
        showDialog(getIntent().getStringExtra(NOTIFICATION_TITLE),
                getIntent().getStringExtra(NOTIFICATION_MESSAGE) != null
                        ? getIntent().getStringExtra(NOTIFICATION_MESSAGE)
                        : getString(R.string.notification_error));
    }

    private void makeTheActivityTransparent() {
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
    }

    private void showDialog(String title, String message) {
        SimpleDialog
                .newInstance(title, 0, message, getString(R.string.ok), true)
                .show(getSupportFragmentManager(), SimpleDialog.COLLECT_DIALOG_TAG);
    }
}
