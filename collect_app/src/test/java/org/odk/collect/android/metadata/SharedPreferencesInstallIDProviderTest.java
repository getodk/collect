package org.odk.collect.android.metadata;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.injection.DaggerUtils;
import org.odk.collect.android.injection.config.AppDependencyComponent;
import org.odk.collect.android.preferences.PreferencesDataSource;
import org.robolectric.RobolectricTestRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.startsWith;

@RunWith(RobolectricTestRunner.class)
public class SharedPreferencesInstallIDProviderTest {

    private PreferencesDataSource metaPreferences;
    private SharedPreferencesInstallIDProvider provider;

    @Before
    public void setup() {
        AppDependencyComponent component = DaggerUtils.getComponent(ApplicationProvider.<Collect>getApplicationContext());
        metaPreferences = component.preferencesRepository().getMetaPreferences();
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