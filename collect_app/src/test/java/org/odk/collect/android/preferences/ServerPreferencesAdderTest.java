package org.odk.collect.android.preferences;

import androidx.fragment.app.FragmentActivity;
import androidx.preference.PreferenceFragmentCompat;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.robolectric.Robolectric;
import org.robolectric.shadows.ShadowToast;

import static junit.framework.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
public class ServerPreferencesAdderTest {

    @Test
    public void whenPreferencesAreAdded_returnsTrue() {
        PreferenceFragmentCompat fragment = mock(PreferenceFragmentCompat.class);
        ServerPreferencesAdder loader = new ServerPreferencesAdder(fragment);

        boolean result = loader.add();
        assertTrue(result);
    }

    @Test
    public void whenAPreferenceHasAnIncorrectType_returnsFalse_andShowsToastError() {
        FragmentActivity activity = Robolectric.setupActivity(FragmentActivity.class);
        PreferenceFragmentCompat fragment = mock(PreferenceFragmentCompat.class);

        doThrow(ClassCastException.class).when(fragment).addPreferencesFromResource(R.xml.odk_server_preferences);
        when(fragment.getActivity()).thenReturn(activity);

        ServerPreferencesAdder loader = new ServerPreferencesAdder(fragment);
        boolean result = loader.add();
        assertFalse(result);

        String toastText = ShadowToast.getTextOfLatestToast();
        assertEquals(toastText, getString(R.string.corrupt_imported_preferences_error));
    }

    private String getString(int id) {
        return ApplicationProvider.getApplicationContext().getString(id);
    }
}
