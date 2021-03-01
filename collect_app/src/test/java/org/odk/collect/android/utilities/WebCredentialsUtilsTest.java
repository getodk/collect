package org.odk.collect.android.utilities;

import org.junit.Test;
import org.odk.collect.android.logic.PropertyManager;
import org.odk.collect.android.preferences.GeneralKeys;
import org.odk.collect.android.preferences.PreferencesDataSource;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class WebCredentialsUtilsTest {

    @Test
    public void saveCredentialsPreferencesMethod_shouldSaveNewCredentialsAndReloadPropertyManager() {
        PreferencesDataSource generalPreferences = mock(PreferencesDataSource.class);
        WebCredentialsUtils webCredentialsUtils = new WebCredentialsUtils(generalPreferences);
        PropertyManager propertyManager = mock(PropertyManager.class);

        webCredentialsUtils.saveCredentialsPreferences("username", "password", propertyManager);

        verify(generalPreferences, times(1)).save(GeneralKeys.KEY_USERNAME, "username");
        verify(generalPreferences, times(1)).save(GeneralKeys.KEY_PASSWORD, "password");
        verify(propertyManager, times(1)).reload();
    }
}
