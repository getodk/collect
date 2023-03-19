package org.odk.collect.android.support;

import android.accounts.Account;
import android.content.Intent;

import com.google.android.gms.auth.GoogleAuthException;

import org.odk.collect.android.gdrive.GoogleAccountPicker;

import java.io.IOException;

public class FakeGoogleAccountPicker implements GoogleAccountPicker {

    private String deviceAccount;
    private String selectedAccountName;

    public void setDeviceAccount(String deviceAccount) {
        this.deviceAccount = deviceAccount;
    }

    @Override
    public String getSelectedAccountName() {
        return selectedAccountName;
    }

    @Override
    public Account[] getAllAccounts() {
        if (deviceAccount != null) {
            return new Account[] {new Account(deviceAccount, "com.google")};
        } else {
            return new Account[]{};
        }
    }

    @Override
    public void setSelectedAccountName(String accountName) {
        this.selectedAccountName = accountName;
    }

    @Override
    public String getToken() throws IOException, GoogleAuthException {
        if (selectedAccountName != null) {
            return "token";
        } else {
            return null;
        }
    }

    @Override
    public Intent newChooseAccountIntent() {
        return new Intent("com.google.android.gms.common.account.CHOOSE_ACCOUNT");
    }
}
