package org.odk.collect.android.utilities.gdrive;

import android.accounts.Account;
import android.app.Activity;
import android.content.Intent;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.odk.collect.android.preferences.GeneralSharedPreferences;
import org.odk.collect.android.preferences.GeneralKeys;
import org.odk.collect.android.preferences.ServerPreferencesFragment;
import org.odk.collect.android.utilities.PermissionUtils;
import org.odk.collect.android.utilities.ThemeUtils;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

/**
 * @author Shobhit Agarwal
 */

@RunWith(PowerMockRunner.class)
@PrepareForTest({GoogleAccountCredential.class, ThemeUtils.class, PermissionUtils.class})
public class GoogleAccountsManagerTest {

    @Mock
    private GoogleAccountCredential mockedCredential;
    @Mock
    private GeneralSharedPreferences mockPreferences;
    @Mock
    private Intent mockIntent;
    @Mock
    private ThemeUtils mockThemeUtils;
    @Mock
    private ServerPreferencesFragment fragment;
    @Mock
    private Activity activity;

    private TestGoogleAccountSelectionListener listener;
    private GoogleAccountsManager googleAccountsManager;

    private String currentAccount;
    private String savedAccount;
    private static final String EXPECTED_ACCOUNT = "abcd@xyz.com";

    /**
     * Stubbing
     */

    private void stubSavedAccount(String accountName) {
        when(mockPreferences.get(GeneralKeys.KEY_SELECTED_GOOGLE_ACCOUNT)).thenReturn(accountName);
        stubAccount(accountName);
    }

    private void stubCredential() {
        doAnswer(invocation -> {
            currentAccount = invocation.getArgument(0);
            return null;
        }).when(mockedCredential).setSelectedAccountName(anyString());
    }

    private void stubAccount(String name) {
        Account account = mock(Account.class);
        Whitebox.setInternalState(account, "name", name);
        doReturn(new Account[]{account}).when(mockedCredential).getAllAccounts();
    }

    private void removeAccounts() {
        doReturn(null).when(mockedCredential).getAllAccounts();
    }

    private void mockPermissionUtils() {
        mockStatic(PermissionUtils.class, invocation -> {
            Whitebox.invokeMethod(googleAccountsManager, "chooseAccount");
            return null;
        });
    }

    private void stubPreferences() {
        doAnswer(invocation -> {
            if (invocation.getArgument(0).equals(GeneralKeys.KEY_SELECTED_GOOGLE_ACCOUNT)) {
                savedAccount = invocation.getArgument(1);
            }
            return null;
        }).when(mockPreferences).save(anyString(), anyString());
    }

    @Before
    public void setup() {
        googleAccountsManager = spy(new GoogleAccountsManager(mockedCredential, mockPreferences, mockIntent, mockThemeUtils, activity, fragment));
        listener = new TestGoogleAccountSelectionListener();
        googleAccountsManager.setListener(listener);

        stubCredential();
        stubPreferences();
        mockPermissionUtils();
    }

    @Test
    public void isAccountNotSelectedAtStartTest() {
        assertFalse(googleAccountsManager.isGoogleAccountSelected());
    }

    @Test
    public void getGoogleAccountNameIfAccountNameIsSavedTest() {
        stubSavedAccount(EXPECTED_ACCOUNT);
        assertEquals(EXPECTED_ACCOUNT, googleAccountsManager.getSelectedAccount());
    }

    @Test
    public void returnNullWhenAccountIsDeleted() {
        //asserting that account exists.
        stubSavedAccount(EXPECTED_ACCOUNT);
        assertEquals(EXPECTED_ACCOUNT, googleAccountsManager.getSelectedAccount());

        //removing the account simulates the deletion of the account via Google account settings.
        removeAccounts();

        assertEquals(googleAccountsManager.getSelectedAccount(), "");
        assertNull(savedAccount);
    }

    @Test
    public void returnBlankWhenAccountNameIsNotSaved() {
        assertEquals("", googleAccountsManager.getSelectedAccount());
        assertNull(currentAccount);
    }

    @Test
    public void displayAccountPickerDialogWhenAutoChooseDisabledTest() {
        stubSavedAccount(EXPECTED_ACCOUNT);
        googleAccountsManager.disableAutoChooseAccount();
        googleAccountsManager.chooseAccountAndRequestPermissionIfNeeded();

        assertNull(listener.getAccountName());
        assertNull(currentAccount);
        verify(googleAccountsManager, times(0)).selectAccount(EXPECTED_ACCOUNT);
        verify(googleAccountsManager, times(1)).showAccountPickerDialog();
    }

    @Test
    public void autoSelectAccountInAutoChooseWhenAccountIsAvailableTest() {
        stubSavedAccount(EXPECTED_ACCOUNT);
        googleAccountsManager.chooseAccountAndRequestPermissionIfNeeded();

        assertEquals(EXPECTED_ACCOUNT, listener.getAccountName());
        assertEquals(EXPECTED_ACCOUNT, currentAccount);
        verify(googleAccountsManager, times(1)).selectAccount(EXPECTED_ACCOUNT);
        verify(googleAccountsManager, times(0)).showAccountPickerDialog();
    }

    @Test
    public void displayAccountPickerDialogInAutoChooseWhenNoAccountIsNotAvailableTest() {
        googleAccountsManager.chooseAccountAndRequestPermissionIfNeeded();

        assertNull(listener.getAccountName());
        assertNull(currentAccount);
        verify(googleAccountsManager, times(0)).selectAccount(EXPECTED_ACCOUNT);
        verify(googleAccountsManager, times(1)).showAccountPickerDialog();
    }

    @Test
    public void accountNameShouldBeSetProperlyIfPermissionsGivenAndAutoSelectEnabledTest() {
        stubSavedAccount(EXPECTED_ACCOUNT);
        googleAccountsManager.chooseAccountAndRequestPermissionIfNeeded();

        assertEquals(EXPECTED_ACCOUNT, listener.getAccountName());
        assertEquals(EXPECTED_ACCOUNT, currentAccount);
    }

    @Test
    public void setAccountNameTest() {

        assertNull(currentAccount);
        assertEquals("", googleAccountsManager.getSelectedAccount());

        googleAccountsManager.setSelectedAccountName(null);
        assertNull(currentAccount);
        assertEquals("", googleAccountsManager.getSelectedAccount());
        verify(googleAccountsManager, times(0)).selectAccount(anyString());

        googleAccountsManager.setSelectedAccountName(EXPECTED_ACCOUNT);
        assertEquals(EXPECTED_ACCOUNT, currentAccount);
        assertEquals(EXPECTED_ACCOUNT, savedAccount);
        verify(googleAccountsManager, times(1)).selectAccount(EXPECTED_ACCOUNT);
    }
}
