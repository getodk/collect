package org.odk.collect.android.preferences

import androidx.test.ext.junit.runners.AndroidJUnit4
import junit.framework.TestCase
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.not
import org.json.JSONException
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.android.TestSettingsProvider
import org.odk.collect.android.configure.qr.JsonPreferencesGenerator
import org.odk.collect.android.preferences.keys.AdminKeys
import org.odk.collect.android.preferences.keys.GeneralKeys

@RunWith(AndroidJUnit4::class)
class JsonPreferencesGeneratorTest : TestCase() {
    private val settingsProvider = TestSettingsProvider.getSettingsProvider()
    private lateinit var jsonPreferencesGenerator: JsonPreferencesGenerator

    @Before
    fun setup() {
        settingsProvider.getGeneralSettings().clear()
        settingsProvider.getAdminSettings().clear()

        jsonPreferencesGenerator = JsonPreferencesGenerator(settingsProvider)
    }

    @Test
    fun whenAdminPasswordIncluded_shouldBePresentInJson() {
        settingsProvider.getAdminSettings().save(AdminKeys.KEY_ADMIN_PW, "123456")

        val jsonPrefs = jsonPreferencesGenerator.getJSONFromPreferences(listOf(AdminKeys.KEY_ADMIN_PW))
        assertThat(jsonPrefs, containsString("admin_pw"))
        assertThat(jsonPrefs, containsString("123456"))
    }

    @Test
    fun whenAdminPasswordExcluded_shouldNotBePresentInJson() {
        settingsProvider.getAdminSettings().save(AdminKeys.KEY_ADMIN_PW, "123456")

        val jsonPrefs = jsonPreferencesGenerator.getJSONFromPreferences(ArrayList())

        assertThat(jsonPrefs, not(containsString("admin_pw")))
        assertThat(jsonPrefs, not(containsString("123456")))
    }

    @Test
    @Throws(JSONException::class)
    fun whenUserPasswordIncluded_shouldBePresentInJson() {
        settingsProvider.getGeneralSettings().save(GeneralKeys.KEY_PASSWORD, "123456")

        val jsonPrefs = jsonPreferencesGenerator.getJSONFromPreferences(listOf(GeneralKeys.KEY_PASSWORD))

        assertThat(jsonPrefs, containsString("password"))
        assertThat(jsonPrefs, containsString("123456"))
    }

    @Test
    fun whenUserPasswordExcluded_shouldNotBePresentInJson() {
        settingsProvider.getGeneralSettings().save(GeneralKeys.KEY_PASSWORD, "123456")

        val jsonPrefs = jsonPreferencesGenerator.getJSONFromPreferences(ArrayList())

        assertThat(jsonPrefs, not(containsString("password")))
        assertThat(jsonPrefs, not(containsString("123456")))
    }
}
