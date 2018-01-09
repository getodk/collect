package org.odk.collect.android.google;

import android.support.annotation.Nullable;

import org.odk.collect.android.utilities.gdrive.GoogleAccountsManager;

/**
 * @author Shobhit Agarwal
 */

public class TestGoogleAccountSelectionListener implements GoogleAccountsManager.GoogleAccountSelectionListener {

    @Nullable
    private String accountName;

    @Override
    public void onGoogleAccountSelected(String accountName) {
        this.accountName = accountName;
    }

    @Nullable
    String getAccountName() {
        return accountName;
    }
}
