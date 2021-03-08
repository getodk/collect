package org.odk.collect.android.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.test.core.app.ApplicationProvider
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.robolectric.RobolectricTestRunner
import java.math.BigDecimal

@RunWith(RobolectricTestRunner::class)
class PreferencesDataSourceTest {
    private lateinit var preferencesSource: SharedPreferencesDataSource
    private lateinit var sharedPreferences: SharedPreferences

    @Before
    fun setup() {
        sharedPreferences = ApplicationProvider.getApplicationContext<Context>().getSharedPreferences("prefs", Context.MODE_PRIVATE)
    }

    @Test
    fun `When nothing has been saved to a string preference and a custom default value exist, that value should be returned`() {
        preferencesSource = SharedPreferencesDataSource(sharedPreferences, defaultPrefs)
        assertThat(preferencesSource.getString(KEY_STRING), `is`(DEFAULT_STRING_VALUE))
    }

    @Test
    fun `When nothing has been saved to a string preference and there are no custom defaults, the default value should be returned`() {
        preferencesSource = SharedPreferencesDataSource(sharedPreferences)
        assertThat(preferencesSource.getString(KEY_STRING), `is`(""))
    }

    @Test
    fun `When a value has been saved to a string preference, the saved value should be returned`() {
        preferencesSource = SharedPreferencesDataSource(sharedPreferences, defaultPrefs)
        preferencesSource.save(KEY_STRING, CUSTOM_STRING_VALUE)
        assertThat(preferencesSource.getString(KEY_STRING), `is`(CUSTOM_STRING_VALUE))
    }

    @Test
    fun `When nothing has been saved to a boolean preference and a custom default value exist, that value should be returned`() {
        preferencesSource = SharedPreferencesDataSource(sharedPreferences, defaultPrefs)
        assertThat(preferencesSource.getBoolean(KEY_BOOLEAN), `is`(DEFAULT_BOOLEAN_VALUE))
    }

    @Test
    fun `When nothing has been saved to a boolean preference and there are no custom defaults, the default value should be returned`() {
        preferencesSource = SharedPreferencesDataSource(sharedPreferences)
        assertThat(preferencesSource.getBoolean(KEY_BOOLEAN), `is`(false))
    }

    @Test
    fun `When a value has been saved to a boolean preference, the saved value should be returned`() {
        preferencesSource = SharedPreferencesDataSource(sharedPreferences, defaultPrefs)
        preferencesSource.save(KEY_BOOLEAN, CUSTOM_BOOLEAN_VALUE)
        assertThat(preferencesSource.getBoolean(KEY_BOOLEAN), `is`(CUSTOM_BOOLEAN_VALUE))
    }

    @Test
    fun `When nothing has been saved to a long preference and a custom default value exist, that value should be returned`() {
        preferencesSource = SharedPreferencesDataSource(sharedPreferences, defaultPrefs)
        assertThat(preferencesSource.getLong(KEY_LONG), `is`(DEFAULT_LONG_VALUE))
    }

    @Test
    fun `When nothing has been saved to a long preference and there are no custom defaults, the default value should be returned`() {
        preferencesSource = SharedPreferencesDataSource(sharedPreferences)
        assertThat(preferencesSource.getLong(KEY_LONG), `is`(0L))
    }

    @Test
    fun `When a value has been saved to a long preference, the saved value should be returned`() {
        preferencesSource = SharedPreferencesDataSource(sharedPreferences, defaultPrefs)
        preferencesSource.save(KEY_LONG, CUSTOM_LONG_VALUE)
        assertThat(preferencesSource.getLong(KEY_LONG), `is`(CUSTOM_LONG_VALUE))
    }

    @Test
    fun `When nothing has been saved to an int preference and a custom default value exist, that value should be returned`() {
        preferencesSource = SharedPreferencesDataSource(sharedPreferences, defaultPrefs)
        assertThat(preferencesSource.getInt(KEY_INT), `is`(DEFAULT_INT_VALUE))
    }

    @Test
    fun `When nothing has been saved to an int preference and there are no custom defaults, the default value should be returned`() {
        preferencesSource = SharedPreferencesDataSource(sharedPreferences)
        assertThat(preferencesSource.getInt(KEY_INT), `is`(0))
    }

    @Test
    fun `When a value has been saved to int preferences, the saved value should be returned`() {
        preferencesSource = SharedPreferencesDataSource(sharedPreferences, defaultPrefs)
        preferencesSource.save(KEY_INT, CUSTOM_INT_VALUE)
        assertThat(preferencesSource.getInt(KEY_INT), `is`(CUSTOM_INT_VALUE))
    }

    @Test
    fun `When nothing has been saved to a float preference and a custom default value exist, that value should be returned`() {
        preferencesSource = SharedPreferencesDataSource(sharedPreferences, defaultPrefs)
        assertThat(preferencesSource.getFloat(KEY_FLOAT), `is`(DEFAULT_FLOAT_VALUE))
    }

    @Test
    fun `When nothing has been saved to a float preference and there are no custom defaults, the default value should be returned`() {
        preferencesSource = SharedPreferencesDataSource(sharedPreferences)
        assertThat(preferencesSource.getFloat(KEY_FLOAT), `is`(0f))
    }

    @Test
    fun `When a value has been saved to a float preference, the saved value should be returned`() {
        preferencesSource = SharedPreferencesDataSource(sharedPreferences, defaultPrefs)
        preferencesSource.save(KEY_FLOAT, CUSTOM_FLOAT_VALUE)
        assertThat(preferencesSource.getFloat(KEY_FLOAT), `is`(CUSTOM_FLOAT_VALUE))
    }

    @Test
    fun `When nothing has been saved to a stringSet preference and a custom default value exist, that value should be returned`() {
        preferencesSource = SharedPreferencesDataSource(sharedPreferences, defaultPrefs)
        assertThat(preferencesSource.getStringSet(KEY_SET), `is`(DEFAULT_SET_VALUE))
    }

    @Test
    fun `When nothing has been saved to a stringSet preference and there are no custom defaults, the default value should be returned`() {
        preferencesSource = SharedPreferencesDataSource(sharedPreferences)
        assertThat(preferencesSource.getStringSet(KEY_SET), `is`(emptySet()))
    }

    @Test
    fun `When a value has been saved to stringSet preference, the saved value should be returned`() {
        preferencesSource = SharedPreferencesDataSource(sharedPreferences, defaultPrefs)
        preferencesSource.save(KEY_SET, CUSTOM_SET_VALUE)
        assertThat(preferencesSource.getStringSet(KEY_SET), `is`(CUSTOM_SET_VALUE))
    }

    @Test
    fun `When multiple preferences have been saved in one go, all values should be properly saved`() {
        preferencesSource = SharedPreferencesDataSource(sharedPreferences)
        preferencesSource.saveAll(
            mapOf(
                KEY_STRING to CUSTOM_STRING_VALUE,
                KEY_BOOLEAN to CUSTOM_BOOLEAN_VALUE,
                KEY_FLOAT to CUSTOM_FLOAT_VALUE
            )
        )
        assertThat(preferencesSource.getString(KEY_STRING), `is`(CUSTOM_STRING_VALUE))
        assertThat(preferencesSource.getBoolean(KEY_BOOLEAN), `is`(CUSTOM_BOOLEAN_VALUE))
        assertThat(preferencesSource.getFloat(KEY_FLOAT), `is`(CUSTOM_FLOAT_VALUE))
    }

    @Test(expected = RuntimeException::class)
    fun `When unsupported value passed to save, RuntimeException should be thrown`() {
        preferencesSource.save(KEY_FLOAT, BigDecimal(5))
    }

    @Test
    fun `When remove() called, should saved preference be properly removed`() {
        preferencesSource = SharedPreferencesDataSource(sharedPreferences, defaultPrefs)
        preferencesSource.save(KEY_STRING, CUSTOM_STRING_VALUE)
        preferencesSource.remove(KEY_STRING)
        assertThat(preferencesSource.getAll().containsKey(KEY_STRING), `is`(false))
    }

    @Test
    fun `When reset() called, should default value be loaded for given key`() {
        preferencesSource = SharedPreferencesDataSource(sharedPreferences, defaultPrefs)
        preferencesSource.save(KEY_STRING, CUSTOM_STRING_VALUE)
        preferencesSource.reset(KEY_STRING)
        assertThat(preferencesSource.getAll()[KEY_STRING], `is`(DEFAULT_STRING_VALUE))
    }

    @Test
    fun `When clear() called, should all saved preferences be removed and defaults should be loaded`() {
        preferencesSource = SharedPreferencesDataSource(sharedPreferences, defaultPrefs)

        preferencesSource.save(KEY_STRING, CUSTOM_STRING_VALUE)
        preferencesSource.save(KEY_BOOLEAN, CUSTOM_BOOLEAN_VALUE)
        preferencesSource.save(KEY_LONG, CUSTOM_LONG_VALUE)
        preferencesSource.save(KEY_INT, CUSTOM_INT_VALUE)
        preferencesSource.save(KEY_FLOAT, CUSTOM_FLOAT_VALUE)
        preferencesSource.save(KEY_SET, CUSTOM_SET_VALUE)
        preferencesSource.save("something_else", "something_else")

        preferencesSource.clear()

        assertDefaultPrefs(defaultPrefs)
        assertThat(preferencesSource.contains("something_else"), `is`(false))
    }

    @Test
    fun `When getAll() called, should return all saved preferences`() {
        preferencesSource = SharedPreferencesDataSource(sharedPreferences)
        preferencesSource.saveAll(
            mapOf(
                KEY_STRING to CUSTOM_STRING_VALUE,
                KEY_FLOAT to CUSTOM_FLOAT_VALUE
            )
        )
        val allPreferences = preferencesSource.getAll()
        assertThat(allPreferences.size, `is`(2))
        assertThat(allPreferences.containsKey(KEY_STRING), `is`(true))
        assertThat(allPreferences.containsValue(CUSTOM_STRING_VALUE), `is`(true))
        assertThat(allPreferences.containsKey(KEY_FLOAT), `is`(true))
        assertThat(allPreferences.containsValue(CUSTOM_FLOAT_VALUE), `is`(true))
    }

    @Test
    fun `When contains() called, should return true for saved preferences`() {
        preferencesSource = SharedPreferencesDataSource(sharedPreferences)
        preferencesSource.saveAll(
            mapOf(
                KEY_STRING to CUSTOM_STRING_VALUE,
                KEY_FLOAT to CUSTOM_FLOAT_VALUE
            )
        )
        assertThat(preferencesSource.contains(KEY_STRING), `is`(true))
        assertThat(preferencesSource.contains(KEY_BOOLEAN), `is`(false))
        assertThat(preferencesSource.contains(KEY_LONG), `is`(false))
        assertThat(preferencesSource.contains(KEY_INT), `is`(false))
        assertThat(preferencesSource.contains(KEY_FLOAT), `is`(true))
        assertThat(preferencesSource.contains(KEY_SET), `is`(false))
    }

    @Test
    fun `When loadDefaultPreferencesIfNotExist() called, should save all defaults that do not exist`() {
        preferencesSource = SharedPreferencesDataSource(sharedPreferences, defaultPrefs)
        preferencesSource.save(KEY_STRING, CUSTOM_STRING_VALUE)
        assertThat(preferencesSource.getAll().size, `is`(1))
        preferencesSource.loadDefaultPreferencesIfNotExist()
        val prefs = preferencesSource.getAll()
        assertThat(prefs.size, `is`(defaultPrefs.size))
        assertThat(prefs[KEY_STRING], `is`(CUSTOM_STRING_VALUE))
        assertThat(prefs[KEY_BOOLEAN], `is`(DEFAULT_BOOLEAN_VALUE))
        assertThat(prefs[KEY_LONG], `is`(DEFAULT_LONG_VALUE))
        assertThat(prefs[KEY_INT], `is`(DEFAULT_INT_VALUE))
        assertThat(prefs[KEY_FLOAT], `is`(DEFAULT_FLOAT_VALUE))
        assertThat(prefs[KEY_SET], `is`(DEFAULT_SET_VALUE))
    }

    @Test
    fun `When PreferenceChangeListener registered, should listen to changes in preferences`() {
        preferencesSource = SharedPreferencesDataSource(sharedPreferences, defaultPrefs)
        val listener: PreferencesDataSource.OnPreferenceChangeListener = mock(PreferencesDataSource.OnPreferenceChangeListener::class.java)
        preferencesSource.registerOnPreferenceChangeListener(listener)
        preferencesSource.save("test", "string")
        verify(listener).onPreferenceChanged("test")
        preferencesSource.unregisterOnPreferenceChangeListener(listener)
        preferencesSource.save("test2", "string2")
        verify(listener, never()).onPreferenceChanged("test2")
    }

    fun assertDefaultPrefs(allPreferences: Map<String, *>) {
        assertThat(allPreferences.size, `is`(defaultPrefs.size))
        assertThat(allPreferences[KEY_STRING], `is`(DEFAULT_STRING_VALUE))
        assertThat(allPreferences[KEY_BOOLEAN], `is`(DEFAULT_BOOLEAN_VALUE))
        assertThat(allPreferences[KEY_LONG], `is`(DEFAULT_LONG_VALUE))
        assertThat(allPreferences[KEY_INT], `is`(DEFAULT_INT_VALUE))
        assertThat(allPreferences[KEY_FLOAT], `is`(DEFAULT_FLOAT_VALUE))
        assertThat(allPreferences[KEY_SET], `is`(DEFAULT_SET_VALUE))
    }

    private val defaultPrefs: Map<String, Any>
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
