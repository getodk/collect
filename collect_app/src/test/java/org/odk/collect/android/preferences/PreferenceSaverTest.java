package org.odk.collect.android.preferences;

import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.listeners.ActionListener;
import org.robolectric.RobolectricTestRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.odk.collect.android.preferences.GeneralKeys.KEY_PASSWORD;
import static org.odk.collect.android.preferences.GeneralKeys.KEY_SERVER_URL;

@RunWith(RobolectricTestRunner.class)
public class PreferenceSaverTest {

    @Test
    public void fromJSON_whenPreferencesAreValid_savesIncludedGeneralKeys_andResetsMissingGeneralKeys() throws Exception {
        JSONObject content = new JSONObject();
        JSONObject general = new JSONObject();
        content.put("general", general);
        content.put("admin", new JSONObject());
        general.put(KEY_PASSWORD, "password");

        GeneralSharedPreferences generalSharedPreferences = mock(GeneralSharedPreferences.class);
        new PreferenceSaver(generalSharedPreferences, AdminSharedPreferences.getInstance()).fromJSON(content.toString(), mock(ActionListener.class));

        verify(generalSharedPreferences).save(KEY_PASSWORD, "password");
        verify(generalSharedPreferences).reset(KEY_SERVER_URL);
    }

    @Test
    public void fromJSON_whenPreferencesAreValid_callsSuccessListener() throws Exception {
        JSONObject content = new JSONObject();
        JSONObject general = new JSONObject();
        content.put("general", general);
        content.put("admin", new JSONObject());
        general.put(KEY_PASSWORD, "password");

        ActionListener listener = mock(ActionListener.class);

        GeneralSharedPreferences generalSharedPreferences = mock(GeneralSharedPreferences.class);
        new PreferenceSaver(generalSharedPreferences, AdminSharedPreferences.getInstance()).fromJSON(content.toString(), listener);

        verify(listener).onSuccess();
    }

    @Test
    public void fromJSON_whenPreferencesAreInvalid_DoesNotSaveOrResetGeneralKeys() throws Exception {
        JSONObject content = new JSONObject();
        JSONObject general = new JSONObject();
        content.put("general", general);
        content.put("admin", new JSONObject());
        general.put(KEY_PASSWORD, false);

        GeneralSharedPreferences generalSharedPreferences = mock(GeneralSharedPreferences.class);
        new PreferenceSaver(generalSharedPreferences, AdminSharedPreferences.getInstance()).fromJSON(content.toString(), mock(ActionListener.class));

        verify(generalSharedPreferences, never()).save(any(), any());
        verify(generalSharedPreferences, never()).reset(any());
    }

    @Test
    public void fromJSON_whenPreferencesAreInvalid_callsFailureListener() throws Exception {
        JSONObject content = new JSONObject();
        JSONObject general = new JSONObject();
        content.put("general", general);
        content.put("admin", new JSONObject());
        general.put(KEY_PASSWORD, false);

        GeneralSharedPreferences generalSharedPreferences = mock(GeneralSharedPreferences.class);
        ActionListener listener = mock(ActionListener.class);
        new PreferenceSaver(generalSharedPreferences, AdminSharedPreferences.getInstance()).fromJSON(content.toString(), listener);

        verify(listener).onFailure(any(GeneralSharedPreferences.ValidationException.class));
    }
}
