package org.odk.collect.android.gdrive;

import android.accounts.Account;
import android.content.Intent;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;

import java.io.IOException;

public class GoogleAccountCredentialGoogleAccountPicker implements GoogleAccountPicker {

    private final GoogleAccountCredential googleAccountCredential;

    public GoogleAccountCredentialGoogleAccountPicker(GoogleAccountCredential googleAccountCredential) {
        this.googleAccountCredential = googleAccountCredential;
    }

    @Override
    public String getSelectedAccountName() {
        return googleAccountCredential.getSelectedAccountName();
    }

    @Override
    public Account[] getAllAccounts() {
        return googleAccountCredential.getAllAccounts();
    }

    @Override
    public void setSelectedAccountName(String accountName) {
        googleAccountCredential.setSelectedAccountName(accountName);
    }

    @Override
    public String getToken() throws IOException, GoogleAuthException {
        return googleAccountCredential.getToken();
    }

    @Override
    public Intent newChooseAccountIntent() {
        return googleAccountCredential.newChooseAccountIntent();
    }
}
