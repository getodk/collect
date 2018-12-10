package org.odk.collect.android.preferences;

import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.shadows.ShadowToast;

import static junit.framework.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class AggregatePreferencesAdderTest {

    @Test
    public void whenPreferencesAreAdded_returnsTrue() {
        PreferenceFragment fragment = Robolectric.buildFragment(TestPreferenceFragment.class).create().resume().visible().get();
        AggregatePreferencesAdder loader = new AggregatePreferencesAdder(fragment);

        boolean result = loader.add();
        assertTrue(result);
    }

    @Test
    public void whenAPreferenceHasAnIncorrectType_returnsFalse_andShowsToastError() {
        putBooleanToSharedPrefs("password", false);

        PreferenceFragment fragment = Robolectric.buildFragment(TestPreferenceFragment.class).create().resume().visible().get();
        AggregatePreferencesAdder loader = new AggregatePreferencesAdder(fragment);

        boolean result = loader.add();
        assertFalse(result);

        String toastText = ShadowToast.getTextOfLatestToast();
        assertEquals(toastText, getString(R.string.corrupt_imported_preferences_error));
    }

    private String getString(int id) {
        return RuntimeEnvironment.application.getString(id);
    }

    private void putBooleanToSharedPrefs(String key, boolean value) {
        PreferenceManager.getDefaultSharedPreferences(RuntimeEnvironment.application)
                .edit()
                .putBoolean(key, value)
                .apply();
    }

    public static class TestPreferenceFragment extends PreferenceFragment {

    }
}
