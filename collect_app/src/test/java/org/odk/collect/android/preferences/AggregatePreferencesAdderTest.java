package org.odk.collect.android.preferences;

import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import androidx.preference.PreferenceFragmentCompat;
import androidx.test.core.app.ApplicationProvider;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowToast;

import static junit.framework.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

@RunWith(RobolectricTestRunner.class)
public class AggregatePreferencesAdderTest {

    @Test
    public void whenPreferencesAreAdded_returnsTrue() {
        PreferenceFragmentCompat fragment = mock(PreferenceFragmentCompat.class);
//        PreferenceFragment fragment = Robolectric.buildFragment(TestPreferenceFragment.class).create().resume().visible().get();
        AggregatePreferencesAdder loader = new AggregatePreferencesAdder(fragment);

        boolean result = loader.add();
        assertTrue(result);
    }

    @Test
    public void whenAPreferenceHasAnIncorrectType_returnsFalse_andShowsToastError() {
        putBooleanToSharedPrefs("password", false);
        PreferenceFragmentCompat fragment = mock(PreferenceFragmentCompat.class);
//        PreferenceFragment fragment = Robolectric.buildFragment(TestPreferenceFragment.class).create().resume().visible().get();
        AggregatePreferencesAdder loader = new AggregatePreferencesAdder(fragment);

        boolean result = loader.add();
        assertFalse(result);

        String toastText = ShadowToast.getTextOfLatestToast();
        assertEquals(toastText, getString(R.string.corrupt_imported_preferences_error));
    }

    private String getString(int id) {
        return ApplicationProvider.getApplicationContext().getString(id);
    }

    private void putBooleanToSharedPrefs(String key, boolean value) {
        PreferenceManager.getDefaultSharedPreferences(ApplicationProvider.getApplicationContext())
                .edit()
                .putBoolean(key, value)
                .apply();
    }

    public static class TestPreferenceFragment extends PreferenceFragment {

    }
}
