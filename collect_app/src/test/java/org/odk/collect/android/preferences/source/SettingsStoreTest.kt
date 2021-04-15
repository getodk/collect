package org.odk.collect.android.preferences.source

import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.odk.collect.shared.Settings

class SettingsStoreTest {
    private lateinit var settingsStore: SettingsStore
    private lateinit var settings: Settings

    @Before
    fun setup() {
        settings = mock(Settings::class.java)
        settingsStore = SettingsStore(settings)
    }

    @Test
    fun `putString() saves string in settings`() {
        settingsStore.putString("key", "value")
        verify(settings).save("key", "value")
    }

    @Test
    fun `putBoolean() saves boolean in settings`() {
        settingsStore.putBoolean("key", true)
        verify(settings).save("key", true)
    }

    @Test
    fun `putLong() saves long in settings`() {
        settingsStore.putLong("key", 3L)
        verify(settings).save("key", 3L)
    }

    @Test
    fun `putInt() saves int in settings`() {
        settingsStore.putInt("key", 5)
        verify(settings).save("key", 5)
    }

    @Test
    fun `putFloat() saves float in settings`() {
        settingsStore.putFloat("key", 8.43f)
        verify(settings).save("key", 8.43f)
    }

    @Test
    fun `putStringSet() saves set of strings in settings`() {
        settingsStore.putStringSet("key", setOf("x", "y", "z"))
        verify(settings).save("key", setOf("x", "y", "z"))
    }

    @Test
    fun `getString() reads string from settings`() {
        `when`(settings.getString("key")).thenReturn("value")
        val value = settingsStore.getString("key", "")
        assertThat(value, `is`("value"))
    }

    @Test
    fun `getBoolean() reads boolean from settings`() {
        `when`(settings.getBoolean("key")).thenReturn(true)
        val value = settingsStore.getBoolean("key", false)
        assertThat(value, `is`(true))
    }

    @Test
    fun `getLong() reads long from settings`() {
        `when`(settings.getLong("key")).thenReturn(3L)
        val value = settingsStore.getLong("key", 0L)
        assertThat(value, `is`(3L))
    }

    @Test
    fun `getInt() reads int from settings`() {
        `when`(settings.getInt("key")).thenReturn(5)
        val value = settingsStore.getInt("key", 0)
        assertThat(value, `is`(5))
    }

    @Test
    fun `getFloat() reads float from settings`() {
        `when`(settings.getFloat("key")).thenReturn(8.43f)
        val value = settingsStore.getFloat("key", 1f)
        assertThat(value, `is`(8.43f))
    }

    @Test
    fun `getStringSet() reads set of strings from settings`() {
        `when`(settings.getStringSet("key")).thenReturn(setOf("x", "y", "z"))
        val value = settingsStore.getStringSet("key", emptySet())
        assertThat(value, `is`(setOf("x", "y", "z")))
    }
}
