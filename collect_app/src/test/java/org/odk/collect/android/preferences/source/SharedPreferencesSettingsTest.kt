package org.odk.collect.android.preferences.source

import android.content.Context
import android.content.SharedPreferences
import androidx.test.core.app.ApplicationProvider
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.nullValue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.odk.collect.shared.Settings
import org.robolectric.RobolectricTestRunner
import java.math.BigDecimal

@RunWith(RobolectricTestRunner::class)
class SharedPreferencesSettingsTest {
    private lateinit var sharedPreferencesSettings: SharedPreferencesSettings
    private lateinit var sharedPreferences: SharedPreferences

    @Before
    fun setup() {
        sharedPreferences = ApplicationProvider.getApplicationContext<Context>()
            .getSharedPreferences("prefs", Context.MODE_PRIVATE)
    }

    @Test
    fun `When nothing has been saved to a string setting and a custom default value exist, that value should be returned`() {
        sharedPreferencesSettings = SharedPreferencesSettings(sharedPreferences, defaultSettings)
        assertThat(sharedPreferencesSettings.getString(KEY_STRING), `is`(DEFAULT_STRING_VALUE))
    }

    @Test
    fun `When nothing has been saved to a string setting and there are no custom defaults, the default value should be returned`() {
        sharedPreferencesSettings = SharedPreferencesSettings(sharedPreferences)
        assertThat(sharedPreferencesSettings.getString(KEY_STRING), `is`(nullValue()))
    }

    @Test
    fun `When a value has been saved to a string setting, the saved value should be returned`() {
        sharedPreferencesSettings = SharedPreferencesSettings(sharedPreferences, defaultSettings)
        sharedPreferencesSettings.save(KEY_STRING, CUSTOM_STRING_VALUE)
        assertThat(sharedPreferencesSettings.getString(KEY_STRING), `is`(CUSTOM_STRING_VALUE))
    }

    @Test
    fun `When nothing has been saved to a boolean setting and a custom default value exist, that value should be returned`() {
        sharedPreferencesSettings = SharedPreferencesSettings(sharedPreferences, defaultSettings)
        assertThat(sharedPreferencesSettings.getBoolean(KEY_BOOLEAN), `is`(DEFAULT_BOOLEAN_VALUE))
    }

    @Test
    fun `When nothing has been saved to a boolean setting and there are no custom defaults, the default value should be returned`() {
        sharedPreferencesSettings = SharedPreferencesSettings(sharedPreferences)
        assertThat(sharedPreferencesSettings.getBoolean(KEY_BOOLEAN), `is`(false))
    }

    @Test
    fun `When a value has been saved to a boolean setting, the saved value should be returned`() {
        sharedPreferencesSettings = SharedPreferencesSettings(sharedPreferences, defaultSettings)
        sharedPreferencesSettings.save(KEY_BOOLEAN, CUSTOM_BOOLEAN_VALUE)
        assertThat(sharedPreferencesSettings.getBoolean(KEY_BOOLEAN), `is`(CUSTOM_BOOLEAN_VALUE))
    }

    @Test
    fun `When nothing has been saved to a long setting and a custom default value exist, that value should be returned`() {
        sharedPreferencesSettings = SharedPreferencesSettings(sharedPreferences, defaultSettings)
        assertThat(sharedPreferencesSettings.getLong(KEY_LONG), `is`(DEFAULT_LONG_VALUE))
    }

    @Test
    fun `When nothing has been saved to a long setting and there are no custom defaults, the default value should be returned`() {
        sharedPreferencesSettings = SharedPreferencesSettings(sharedPreferences)
        assertThat(sharedPreferencesSettings.getLong(KEY_LONG), `is`(0L))
    }

    @Test
    fun `When a value has been saved to a long setting, the saved value should be returned`() {
        sharedPreferencesSettings = SharedPreferencesSettings(sharedPreferences, defaultSettings)
        sharedPreferencesSettings.save(KEY_LONG, CUSTOM_LONG_VALUE)
        assertThat(sharedPreferencesSettings.getLong(KEY_LONG), `is`(CUSTOM_LONG_VALUE))
    }

    @Test
    fun `When nothing has been saved to an int setting and a custom default value exist, that value should be returned`() {
        sharedPreferencesSettings = SharedPreferencesSettings(sharedPreferences, defaultSettings)
        assertThat(sharedPreferencesSettings.getInt(KEY_INT), `is`(DEFAULT_INT_VALUE))
    }

    @Test
    fun `When nothing has been saved to an int setting and there are no custom defaults, the default value should be returned`() {
        sharedPreferencesSettings = SharedPreferencesSettings(sharedPreferences)
        assertThat(sharedPreferencesSettings.getInt(KEY_INT), `is`(0))
    }

    @Test
    fun `When a value has been saved to int setting, the saved value should be returned`() {
        sharedPreferencesSettings = SharedPreferencesSettings(sharedPreferences, defaultSettings)
        sharedPreferencesSettings.save(KEY_INT, CUSTOM_INT_VALUE)
        assertThat(sharedPreferencesSettings.getInt(KEY_INT), `is`(CUSTOM_INT_VALUE))
    }

    @Test
    fun `When nothing has been saved to a float setting and a custom default value exist, that value should be returned`() {
        sharedPreferencesSettings = SharedPreferencesSettings(sharedPreferences, defaultSettings)
        assertThat(sharedPreferencesSettings.getFloat(KEY_FLOAT), `is`(DEFAULT_FLOAT_VALUE))
    }

    @Test
    fun `When nothing has been saved to a float setting and there are no custom defaults, the default value should be returned`() {
        sharedPreferencesSettings = SharedPreferencesSettings(sharedPreferences)
        assertThat(sharedPreferencesSettings.getFloat(KEY_FLOAT), `is`(0f))
    }

    @Test
    fun `When a value has been saved to a float setting, the saved value should be returned`() {
        sharedPreferencesSettings = SharedPreferencesSettings(sharedPreferences, defaultSettings)
        sharedPreferencesSettings.save(KEY_FLOAT, CUSTOM_FLOAT_VALUE)
        assertThat(sharedPreferencesSettings.getFloat(KEY_FLOAT), `is`(CUSTOM_FLOAT_VALUE))
    }

    @Test
    fun `When nothing has been saved to a stringSet setting and a custom default value exist, that value should be returned`() {
        sharedPreferencesSettings = SharedPreferencesSettings(sharedPreferences, defaultSettings)
        assertThat(sharedPreferencesSettings.getStringSet(KEY_SET), `is`(DEFAULT_SET_VALUE))
    }

    @Test
    fun `When nothing has been saved to a stringSet setting and there are no custom defaults, the default value should be returned`() {
        sharedPreferencesSettings = SharedPreferencesSettings(sharedPreferences)
        assertThat(sharedPreferencesSettings.getStringSet(KEY_SET), `is`(emptySet()))
    }

    @Test
    fun `When a value has been saved to stringSet setting, the saved value should be returned`() {
        sharedPreferencesSettings = SharedPreferencesSettings(sharedPreferences, defaultSettings)
        sharedPreferencesSettings.save(KEY_SET, CUSTOM_SET_VALUE)
        assertThat(sharedPreferencesSettings.getStringSet(KEY_SET), `is`(CUSTOM_SET_VALUE))
    }

    @Test
    fun `When multiple settings have been saved in one go, all values should be properly saved`() {
        sharedPreferencesSettings = SharedPreferencesSettings(sharedPreferences)
        sharedPreferencesSettings.saveAll(
            mapOf(
                KEY_STRING to CUSTOM_STRING_VALUE,
                KEY_BOOLEAN to CUSTOM_BOOLEAN_VALUE,
                KEY_FLOAT to CUSTOM_FLOAT_VALUE
            )
        )
        assertThat(sharedPreferencesSettings.getString(KEY_STRING), `is`(CUSTOM_STRING_VALUE))
        assertThat(sharedPreferencesSettings.getBoolean(KEY_BOOLEAN), `is`(CUSTOM_BOOLEAN_VALUE))
        assertThat(sharedPreferencesSettings.getFloat(KEY_FLOAT), `is`(CUSTOM_FLOAT_VALUE))
    }

    @Test(expected = RuntimeException::class)
    fun `When unsupported value passed to save, RuntimeException should be thrown`() {
        sharedPreferencesSettings.save(KEY_FLOAT, BigDecimal(5))
    }

    @Test
    fun `When remove() called, should saved setting be properly removed`() {
        sharedPreferencesSettings = SharedPreferencesSettings(sharedPreferences, defaultSettings)
        sharedPreferencesSettings.save(KEY_STRING, CUSTOM_STRING_VALUE)
        sharedPreferencesSettings.remove(KEY_STRING)
        assertThat(sharedPreferencesSettings.getAll().containsKey(KEY_STRING), `is`(false))
    }

    @Test
    fun `When reset() called, should default value be loaded for given key`() {
        sharedPreferencesSettings = SharedPreferencesSettings(sharedPreferences, defaultSettings)
        sharedPreferencesSettings.save(KEY_STRING, CUSTOM_STRING_VALUE)
        sharedPreferencesSettings.reset(KEY_STRING)
        assertThat(sharedPreferencesSettings.getAll()[KEY_STRING], `is`(DEFAULT_STRING_VALUE))
    }

    @Test
    fun `When clear() called, should all saved settings be removed and defaults should be loaded`() {
        sharedPreferencesSettings = SharedPreferencesSettings(sharedPreferences, defaultSettings)

        sharedPreferencesSettings.save(KEY_STRING, CUSTOM_STRING_VALUE)
        sharedPreferencesSettings.save(KEY_BOOLEAN, CUSTOM_BOOLEAN_VALUE)
        sharedPreferencesSettings.save(KEY_LONG, CUSTOM_LONG_VALUE)
        sharedPreferencesSettings.save(KEY_INT, CUSTOM_INT_VALUE)
        sharedPreferencesSettings.save(KEY_FLOAT, CUSTOM_FLOAT_VALUE)
        sharedPreferencesSettings.save(KEY_SET, CUSTOM_SET_VALUE)
        sharedPreferencesSettings.save("something_else", "something_else")

        sharedPreferencesSettings.clear()

        assertDefaultPrefs(defaultSettings)
        assertThat(sharedPreferencesSettings.contains("something_else"), `is`(false))
    }

    @Test
    fun `When getAll() called, should return all saved settings`() {
        sharedPreferencesSettings = SharedPreferencesSettings(sharedPreferences)
        sharedPreferencesSettings.saveAll(
            mapOf(
                KEY_STRING to CUSTOM_STRING_VALUE,
                KEY_FLOAT to CUSTOM_FLOAT_VALUE
            )
        )
        val allPreferences = sharedPreferencesSettings.getAll()
        assertThat(allPreferences.size, `is`(2))
        assertThat(allPreferences.containsKey(KEY_STRING), `is`(true))
        assertThat(allPreferences.containsValue(CUSTOM_STRING_VALUE), `is`(true))
        assertThat(allPreferences.containsKey(KEY_FLOAT), `is`(true))
        assertThat(allPreferences.containsValue(CUSTOM_FLOAT_VALUE), `is`(true))
    }

    @Test
    fun `When contains() called, should return true for saved settings`() {
        sharedPreferencesSettings = SharedPreferencesSettings(sharedPreferences)
        sharedPreferencesSettings.saveAll(
            mapOf(
                KEY_STRING to CUSTOM_STRING_VALUE,
                KEY_FLOAT to CUSTOM_FLOAT_VALUE
            )
        )
        assertThat(sharedPreferencesSettings.contains(KEY_STRING), `is`(true))
        assertThat(sharedPreferencesSettings.contains(KEY_BOOLEAN), `is`(false))
        assertThat(sharedPreferencesSettings.contains(KEY_LONG), `is`(false))
        assertThat(sharedPreferencesSettings.contains(KEY_INT), `is`(false))
        assertThat(sharedPreferencesSettings.contains(KEY_FLOAT), `is`(true))
        assertThat(sharedPreferencesSettings.contains(KEY_SET), `is`(false))
    }

    @Test
    fun `When setDefaultForAllSettingsWithoutValues() called, should save all defaults that do not exist`() {
        sharedPreferencesSettings = SharedPreferencesSettings(sharedPreferences, defaultSettings)
        sharedPreferencesSettings.save(KEY_STRING, CUSTOM_STRING_VALUE)
        assertThat(sharedPreferencesSettings.getAll().size, `is`(1))
        sharedPreferencesSettings.setDefaultForAllSettingsWithoutValues()
        val prefs = sharedPreferencesSettings.getAll()
        assertThat(prefs.size, `is`(defaultSettings.size))
        assertThat(prefs[KEY_STRING], `is`(CUSTOM_STRING_VALUE))
        assertThat(prefs[KEY_BOOLEAN], `is`(DEFAULT_BOOLEAN_VALUE))
        assertThat(prefs[KEY_LONG], `is`(DEFAULT_LONG_VALUE))
        assertThat(prefs[KEY_INT], `is`(DEFAULT_INT_VALUE))
        assertThat(prefs[KEY_FLOAT], `is`(DEFAULT_FLOAT_VALUE))
        assertThat(prefs[KEY_SET], `is`(DEFAULT_SET_VALUE))
    }

    @Test
    fun `When SettingChangeListener registered, should listen to changes in settings`() {
        sharedPreferencesSettings = SharedPreferencesSettings(sharedPreferences, defaultSettings)
        val listener: Settings.OnSettingChangeListener =
            mock(Settings.OnSettingChangeListener::class.java)
        sharedPreferencesSettings.registerOnSettingChangeListener(listener)
        sharedPreferencesSettings.save("test", "string")
        verify(listener).onSettingChanged("test")

        sharedPreferencesSettings.unregisterOnSettingChangeListener(listener)
        sharedPreferencesSettings.save("test2", "string2")
        verify(listener, never()).onSettingChanged("test2")
    }

    @Test
    fun `can register multiple change listeners`() {
        sharedPreferencesSettings = SharedPreferencesSettings(sharedPreferences, defaultSettings)
        val listener1: Settings.OnSettingChangeListener =
            mock(Settings.OnSettingChangeListener::class.java)
        sharedPreferencesSettings.registerOnSettingChangeListener(listener1)

        val listener2: Settings.OnSettingChangeListener =
            mock(Settings.OnSettingChangeListener::class.java)
        sharedPreferencesSettings.registerOnSettingChangeListener(listener2)

        sharedPreferencesSettings.save("test", "string")
        verify(listener1).onSettingChanged("test")
        verify(listener2).onSettingChanged("test")

        sharedPreferencesSettings.unregisterOnSettingChangeListener(listener1)
        sharedPreferencesSettings.save("test2", "string2")
        verify(listener1, never()).onSettingChanged("test2")
        verify(listener2).onSettingChanged("test2")
    }

    /*
    Accounts for a possible bug listeners are unregistered but stay in the implementations list. In
    that case the second unregister results in the wrong listener being unregistered from
    SharedPreferences.
     */
    @Test
    fun `unregister works multiple times for the same listener`() {
        sharedPreferencesSettings = SharedPreferencesSettings(sharedPreferences, defaultSettings)
        val listener: Settings.OnSettingChangeListener =
            mock(Settings.OnSettingChangeListener::class.java)

        sharedPreferencesSettings.registerOnSettingChangeListener(listener)
        sharedPreferencesSettings.unregisterOnSettingChangeListener(listener)

        sharedPreferencesSettings.registerOnSettingChangeListener(listener)
        sharedPreferencesSettings.unregisterOnSettingChangeListener(listener)

        sharedPreferencesSettings.save("test", "string")
        verify(listener, never()).onSettingChanged("test")
    }

    fun assertDefaultPrefs(allSettings: Map<String, *>) {
        assertThat(allSettings.size, `is`(defaultSettings.size))
        assertThat(allSettings[KEY_STRING], `is`(DEFAULT_STRING_VALUE))
        assertThat(allSettings[KEY_BOOLEAN], `is`(DEFAULT_BOOLEAN_VALUE))
        assertThat(allSettings[KEY_LONG], `is`(DEFAULT_LONG_VALUE))
        assertThat(allSettings[KEY_INT], `is`(DEFAULT_INT_VALUE))
        assertThat(allSettings[KEY_FLOAT], `is`(DEFAULT_FLOAT_VALUE))
        assertThat(allSettings[KEY_SET], `is`(DEFAULT_SET_VALUE))
    }

    private val defaultSettings: Map<String, Any>
        get() = mapOf(
            KEY_STRING to DEFAULT_STRING_VALUE,
            KEY_BOOLEAN to DEFAULT_BOOLEAN_VALUE,
            KEY_LONG to DEFAULT_LONG_VALUE,
            KEY_INT to DEFAULT_INT_VALUE,
            KEY_FLOAT to DEFAULT_FLOAT_VALUE,
            KEY_SET to DEFAULT_SET_VALUE
        )

    companion object {
        private const val KEY_STRING = "keyString"
        private const val KEY_BOOLEAN = "keyBoolean"
        private const val KEY_LONG = "keyLong"
        private const val KEY_INT = "keyInt"
        private const val KEY_FLOAT = "keyFloat"
        private const val KEY_SET = "keySet"
        private const val DEFAULT_STRING_VALUE = "qwerty"
        private const val DEFAULT_BOOLEAN_VALUE = true
        private const val DEFAULT_LONG_VALUE = 15L
        private const val DEFAULT_INT_VALUE = 23
        private const val DEFAULT_FLOAT_VALUE = 44.65f
        private val DEFAULT_SET_VALUE: Set<String> = setOf("a", "b", "c")
        private const val CUSTOM_STRING_VALUE = "12345"
        private const val CUSTOM_BOOLEAN_VALUE = false
        private const val CUSTOM_LONG_VALUE = 5L
        private const val CUSTOM_INT_VALUE = 3
        private const val CUSTOM_FLOAT_VALUE = 8.25f
        private val CUSTOM_SET_VALUE: Set<String> = setOf("x", "y", "z")
    }
}
