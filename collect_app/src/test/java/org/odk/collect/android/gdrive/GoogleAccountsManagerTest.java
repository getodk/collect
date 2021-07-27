package org.odk.collect.android.gdrive;

import android.accounts.Account;
import android.content.Intent;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.odk.collect.android.permissions.PermissionsProvider;
import org.odk.collect.android.preferences.keys.ProjectKeys;
import org.odk.collect.shared.Settings;
import org.odk.collect.android.preferences.source.SettingsProvider;
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
@PrepareForTest({GoogleAccountCredential.class, ThemeUtils.class, PermissionsProvider.class})
public class GoogleAccountsManagerTest {

    private static final String EXPECTED_ACCOUNT = "abcd@xyz.com";

    @Mock
    private GoogleAccountCredential mockedCredential;

    @Mock
    private SettingsProvider settingsProvider;

    @Mock
    private Settings generalSettings;

    @Mock
    private Intent mockIntent;

    @Mock
    private ThemeUtils mockThemeUtils;

    private String currentAccount;
    private String savedAccount;
    private GoogleAccountsManager googleAccountsManager;

    @Before
    public void setup() {
        googleAccountsManager = spy(new GoogleAccountsManager(mockedCredential, settingsProvider, mockIntent, mockThemeUtils));
        when(settingsProvider.getGeneralSettings()).thenReturn(generalSettings);
        stubCredential();
        stubPreferences();
        mockPermissionsProvider();
    }

    /**
     * Stubbing
     */
    private void stubSavedAccount(String accountName) {
        when(generalSettings.getString(ProjectKeys.KEY_SELECTED_GOOGLE_ACCOUNT)).thenReturn(accountName);
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

    private void mockPermissionsProvider() {
        mockStatic(PermissionsProvider.class, invocation -> {
            Whitebox.invokeMethod(googleAccountsManager, "chooseAccount");
            return null;
        });
    }

    private void stubPreferences() {
        doAnswer(invocation -> {
            if (invocation.getArgument(0).equals(ProjectKeys.KEY_SELECTED_GOOGLE_ACCOUNT)) {
                savedAccount = invocation.getArgument(1);
            }
            return null;
        }).when(generalSettings).save(anyString(), anyString());
    }

    @Test
    public void isAccountNotSelectedAtStartTest() {
        assertFalse(googleAccountsManager.isAccountSelected());
    }

    @Test
    public void getGoogleAccountNameIfAccountNameIsSavedTest() {
        stubSavedAccount(EXPECTED_ACCOUNT);
        assertEquals(EXPECTED_ACCOUNT, googleAccountsManager.getLastSelectedAccountIfValid());
    }

    @Test
    public void returnNullWhenAccountIsDeleted() {
        //asserting that account exists.
        stubSavedAccount(EXPECTED_ACCOUNT);
        assertEquals(EXPECTED_ACCOUNT, googleAccountsManager.getLastSelectedAccountIfValid());

        //removing the account simulates the deletion of the account via Google account settings.
        removeAccounts();

        assertEquals(googleAccountsManager.getLastSelectedAccountIfValid(), "");
        assertNull(savedAccount);
    }

    @Test
    public void returnBlankWhenAccountNameIsNotSaved() {
        stubSavedAccount("some_other_email");
        stubAccount(EXPECTED_ACCOUNT);
        assertEquals("", googleAccountsManager.getLastSelectedAccountIfValid());
        assertNull(currentAccount);
    }

    @Test
    public void setAccountNameTest() {
        assertNull(currentAccount);
        assertEquals("", googleAccountsManager.getLastSelectedAccountIfValid());

        googleAccountsManager.selectAccount(null);
        assertNull(currentAccount);
        assertEquals("", googleAccountsManager.getLastSelectedAccountIfValid());
        verify(googleAccountsManager, times(0)).selectAccount(anyString());

        googleAccountsManager.selectAccount(EXPECTED_ACCOUNT);
        assertEquals(EXPECTED_ACCOUNT, currentAccount);
        assertEquals(EXPECTED_ACCOUNT, savedAccount);
        verify(googleAccountsManager, times(1)).selectAccount(EXPECTED_ACCOUNT);
    }
}
