package org.odk.collect.android.metadata;

import android.content.SharedPreferences;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.startsWith;

@RunWith(RobolectricTestRunner.class)
public class SharedPreferencesInstallIDProviderTest {

    private SharedPreferences sharedPreferences;
    private SharedPreferencesInstallIDProvider provider;

    @Before
    public void setup() {
        sharedPreferences = getDefaultSharedPreferences(RuntimeEnvironment.application);
        provider = new SharedPreferencesInstallIDProvider(sharedPreferences, "blah");
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
        sharedPreferences.edit().clear().commit();

        String secondValue = provider.getInstallID();
        assertThat(secondValue, notNullValue());
        assertThat(firstValue, not(equalTo(secondValue)));
    }
}