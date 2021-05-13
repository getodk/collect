package org.odk.collect.android.metadata;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.odk.collect.android.TestSettingsProvider;
import org.odk.collect.shared.Settings;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.startsWith;

@RunWith(AndroidJUnit4.class)
public class SharedPreferencesInstallIDProviderTest {

    private final Settings metaPreferences = TestSettingsProvider.getMetaSettings();
    private SharedPreferencesInstallIDProvider provider;

    @Before
    public void setup() {
        provider = new SharedPreferencesInstallIDProvider(metaPreferences, "blah");
    }

    @Test
    public void returnsSameValueEveryTime() {
        String firstValue = provider.getInstallID();
        String secondValue = provider.getInstallID();
        assertThat(firstValue, equalTo(secondValue));
    }

    @Test
    public void returnsValueWithPrefix() {
        assertThat(provider.getInstallID(), startsWith("collect:"));
    }

    @Test
    public void returns24CharacterValue() {
        assertThat(provider.getInstallID().length(), equalTo(24));
    }

    @Test
    public void clearingSharedPreferences_resetsInstallID() {
        String firstValue = provider.getInstallID();
        metaPreferences.clear();

        String secondValue = provider.getInstallID();
        assertThat(secondValue, notNullValue());
        assertThat(firstValue, not(equalTo(secondValue)));
    }
}
