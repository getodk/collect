package org.odk.collect.android.google;

import android.content.Intent;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.odk.collect.android.preferences.GeneralSharedPreferences;
import org.odk.collect.android.preferences.PreferenceKeys;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

/**
 * @author Shobhit Agarwal
 */

@RunWith(PowerMockRunner.class)
@PrepareForTest(GoogleAccountCredential.class)
public class GoogleAccountsManagerTest {

    private String expectedAccount = "abcd@xyz.com";

    private GoogleAccountsManager googleAccountsManager;

    @Mock
    private GoogleAccountCredential mockedCredential;
    @Mock
    private GeneralSharedPreferences mockPreferences;
    @Mock
    private Intent mockIntent;

    private TestGoogleAccountSelectionListener listener;

    @Before
    public void setup() {
        googleAccountsManager = spy(new GoogleAccountsManager(mockedCredential, mockPreferences, mockIntent));

        when(mockedCredential.setSelectedAccountName(anyString())).thenReturn(mockedCredential);

        listener = new TestGoogleAccountSelectionListener();
        googleAccountsManager.setListener(listener);
    }

    @Test
    public void isAccountNotSelectedAtStartTest() {
        assertFalse(googleAccountsManager.isGoogleAccountSelected());
    }

    @Test
    public void getGoogleAccountNameIfAccountNameIsSavedTest() {
        when(mockPreferences.get(PreferenceKeys.KEY_SELECTED_GOOGLE_ACCOUNT)).thenReturn(expectedAccount);
        assertEquals(expectedAccount, googleAccountsManager.getGoogleAccountName());
    }

    @Test
    public void returnBlankWhenAccountNameIsNotSaved() {
        when(mockPreferences.get(PreferenceKeys.KEY_SELECTED_GOOGLE_ACCOUNT)).thenReturn("");
        assertEquals("", googleAccountsManager.getGoogleAccountName());
    }

    @Test
    public void displayAccountPickerDialogWhenAutoChooseDisabledTest() {
        doReturn(true).when(googleAccountsManager).checkAccountPermission();
        when(mockPreferences.get(PreferenceKeys.KEY_SELECTED_GOOGLE_ACCOUNT)).thenReturn(expectedAccount);
        googleAccountsManager.setAutoChooseAccount(false);
        googleAccountsManager.chooseAccount();
        assertNull(listener.getAccountName());
        verify(googleAccountsManager, times(0)).selectAccount(expectedAccount);
        verify(googleAccountsManager, times(1)).showAccountPickerDialog();
    }

    @Test
    public void autoSelectAccountInAutoChooseWhenAccountIsAvailableTest() {
        doReturn(true).when(googleAccountsManager).checkAccountPermission();
        when(mockPreferences.get(PreferenceKeys.KEY_SELECTED_GOOGLE_ACCOUNT)).thenReturn(expectedAccount);
        googleAccountsManager.setAutoChooseAccount(true);
        googleAccountsManager.chooseAccount();
        assertEquals(expectedAccount, listener.getAccountName());
        verify(googleAccountsManager, times(1)).selectAccount(expectedAccount);
        verify(googleAccountsManager, times(0)).showAccountPickerDialog();
    }

    @Test
    public void displayAccountPickerDialogInAutoChooseWhenNoAccountIsNotAvailableTest() {
        doReturn(true).when(googleAccountsManager).checkAccountPermission();
        when(mockPreferences.get(PreferenceKeys.KEY_SELECTED_GOOGLE_ACCOUNT)).thenReturn("");
        googleAccountsManager.setAutoChooseAccount(true);
        googleAccountsManager.chooseAccount();
        assertNull(listener.getAccountName());
        verify(googleAccountsManager, times(0)).selectAccount(expectedAccount);
        verify(googleAccountsManager, times(1)).showAccountPickerDialog();
    }

    @Test
    public void permissionsNotAvailableTest() {
        doReturn(false).when(googleAccountsManager).checkAccountPermission();
        doNothing().when(googleAccountsManager).requestAccountPermission();
        when(mockPreferences.get(PreferenceKeys.KEY_SELECTED_GOOGLE_ACCOUNT)).thenReturn(expectedAccount);
        googleAccountsManager.setAutoChooseAccount(true);
        googleAccountsManager.chooseAccount();
        assertNull(listener.getAccountName());
        verify(googleAccountsManager, times(0)).selectAccount(expectedAccount);
        verify(googleAccountsManager, times(0)).showAccountPickerDialog();
        verify(googleAccountsManager, times(1)).requestAccountPermission();
    }
}
