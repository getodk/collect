package org.odk.collect.android.preferences;

import androidx.test.core.app.ApplicationProvider;

import junit.framework.TestCase;

import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.odk.collect.android.preferences.AdminKeys.KEY_ADMIN_PW;
import static org.odk.collect.android.preferences.GeneralKeys.KEY_PASSWORD;

@RunWith(RobolectricTestRunner.class)
public class JsonPreferencesGeneratorTest extends TestCase {
    private JsonPreferencesGenerator jsonPreferencesGenerator;
    private AdminSharedPreferences adminSharedPreferences;
    private GeneralSharedPreferences generalSharedPreferences;

    @Before
    public void setup() {
        jsonPreferencesGenerator = new JsonPreferencesGenerator();
        adminSharedPreferences = new AdminSharedPreferences(ApplicationProvider.getApplicationContext());
        generalSharedPreferences = new GeneralSharedPreferences(ApplicationProvider.getApplicationContext());
    }

    @Test
    public void whenAdminPasswordIncluded_shouldBePresentInJson() throws JSONException {
        adminSharedPreferences.save(KEY_ADMIN_PW, "123456");
        String jsonPrefs = jsonPreferencesGenerator.getJSONFromPreferences(Collections.singletonList(KEY_ADMIN_PW));
        assertThat(jsonPrefs, containsString("admin_pw"));
        assertThat(jsonPrefs, containsString("123456"));
    }

    @Test
    public void whenAdminPasswordExcluded_shouldNotBePresentInJson() throws JSONException {
        adminSharedPreferences.save(KEY_ADMIN_PW, "123456");
        String jsonPrefs = jsonPreferencesGenerator.getJSONFromPreferences(new ArrayList<>());
        assertThat(jsonPrefs, not(containsString("admin_pw")));
        assertThat(jsonPrefs, not(containsString("123456")));
    }

    @Test
    public void whenUserPasswordIncluded_shouldBePresentInJson() throws JSONException {
        generalSharedPreferences.save(KEY_PASSWORD, "123456");
        String jsonPrefs = jsonPreferencesGenerator.getJSONFromPreferences(Collections.singletonList(KEY_PASSWORD));
        assertThat(jsonPrefs, containsString("password"));
        assertThat(jsonPrefs, containsString("123456"));
    }

    @Test
    public void whenUserPasswordExcluded_shouldNotBePresentInJson() throws JSONException {
        generalSharedPreferences.save(KEY_PASSWORD, "123456");
        String jsonPrefs = jsonPreferencesGenerator.getJSONFromPreferences(new ArrayList<>());
        assertThat(jsonPrefs, not(containsString("password")));
        assertThat(jsonPrefs, not(containsString("123456")));
    }
}