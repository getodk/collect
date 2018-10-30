package org.odk.collect.android.utilities;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.listeners.ActionListener;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static junit.framework.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.odk.collect.android.preferences.PreferenceKeys.KEY_PASSWORD;
import static org.odk.collect.android.utilities.SharedPreferencesUtils.savePreferencesFromJSON;

@RunWith(RobolectricTestRunner.class)
public class SavePreferencesFromJSONTest {

    @Before
    public void setup() {
        prefs().edit().clear().commit(); // Make it simpler to test what is written to prefs
    }

    private SharedPreferences prefs() {
        return PreferenceManager.getDefaultSharedPreferences(RuntimeEnvironment.application);
    }

    @Test
    public void whenAggregatePreferencesAreInCorrect_callsFailureAndDoesntSaveValue() throws Exception {
        JSONObject content = new JSONObject();
        JSONObject general = new JSONObject();
        content.put("general", general);
        content.put("admin", new JSONObject());
        general.put(KEY_PASSWORD, false);

        ActionListener listener = mock(ActionListener.class);
        savePreferencesFromJSON(content.toString(), listener);
        verify(listener).onFailure(any());
        verify(listener, never()).onSuccess();
        assertFalse(prefs().contains(KEY_PASSWORD));
    }
}
