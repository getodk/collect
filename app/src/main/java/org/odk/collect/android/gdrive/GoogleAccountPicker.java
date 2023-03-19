package org.odk.collect.android.gdrive;

import android.accounts.Account;
import android.content.Intent;

import com.google.android.gms.auth.GoogleAuthException;

import java.io.IOException;

/**
 * For some reason we run into problems with mocking {@link com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential}
 * so we end up with this!
 */
public interface GoogleAccountPicker {

    String getSelectedAccountName();

    Account[] getAllAccounts();

    void setSelectedAccountName(String accountName);

    String getToken() throws IOException, GoogleAuthException;

    Intent newChooseAccountIntent();
}
