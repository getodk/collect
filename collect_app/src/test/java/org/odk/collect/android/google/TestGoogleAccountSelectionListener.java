package org.odk.collect.android.google;

import android.support.annotation.Nullable;

/**
 * @author Shobhit Agarwal
 */

public class TestGoogleAccountSelectionListener implements GoogleAccountsManager.GoogleAccountSelectionListener {

    @Nullable
    private String accountName;

    public void reset() {
        accountName = null;
    }

    @Override
    public void onGoogleAccountSelected(String accountName) {
        this.accountName = accountName;
    }

    @Nullable
    public String getAccountName() {
        return accountName;
    }
}
