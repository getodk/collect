package org.odk.collect.android.preferences.source

import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

class PreferencesDataStoreTest {
    private lateinit var preferencesDataStore: PreferencesDataStore
    private lateinit var preferencesDataSource: PreferencesDataSource

    @Before
    fun setup() {
        preferencesDataSource = mock(PreferencesDataSource::class.java)
        preferencesDataStore = PreferencesDataStore(preferencesDataSource)
    }

    @Test
    fun `putString() saves string in preferences`() {
        preferencesDataStore.putString("key", "value")
        verify(preferencesDataSource).save("key", "value")
    }

    @Test
    fun `putBoolean() saves boolean in preferences`() {
        preferencesDataStore.putBoolean("key", true)
        verify(preferencesDataSource).save("key", true)
    }

    @Test
    fun `putLong() saves long in preferences`() {
        preferencesDataStore.putLong("key", 3L)
        verify(preferencesDataSource).save("key", 3L)
    }

    @Test
    fun `putInt() saves int in preferences`() {
        preferencesDataStore.putInt("key", 5)
        verify(preferencesDataSource).save("key", 5)
    }

    @Test
    fun `putFloat() saves float in preferences`() {
        preferencesDataStore.putFloat("key", 8.43f)
        verify(preferencesDataSource).save("key", 8.43f)
    }

    @Test
    fun `putStringSet() saves set of strings in preferences`() {
        preferencesDataStore.putStringSet("key", setOf("x", "y", "z"))
        verify(preferencesDataSource).save("key", setOf("x", "y", "z"))
    }

    @Test
    fun `getString() reads string from preferences`() {
        `when`(preferencesDataSource.getString("key")).thenReturn("value")
        val value = preferencesDataStore.getString("key", "")
        assertThat(value, `is`("value"))
    }

    @Test
    fun `getBoolean() reads boolean from preferences`() {
        `when`(preferencesDataSource.getBoolean("key")).thenReturn(true)
        val value = preferencesDataStore.getBoolean("key", false)
        assertThat(value, `is`(true))
    }

    @Test
    fun `getLong() reads long from preferences`() {
        `when`(preferencesDataSource.getLong("key")).thenReturn(3L)
        val value = preferencesDataStore.getLong("key", 0L)
        assertThat(value, `is`(3L))
    }

    @Test
    fun `getInt() reads int from preferences`() {
        `when`(preferencesDataSource.getInt("key")).thenReturn(5)
        val value = preferencesDataStore.getInt("key", 0)
        assertThat(value, `is`(5))
    }

    @Test
    fun `getFloat() reads float from preferences`() {
        `when`(preferencesDataSource.getFloat("key")).thenReturn(8.43f)
        val value = preferencesDataStore.getFloat("key", 1f)
        assertThat(value, `is`(8.43f))
    }

    @Test
    fun `getStringSet() reads set of strings from preferences`() {
        `when`(preferencesDataSource.getStringSet("key")).thenReturn(setOf("x", "y", "z"))
        val value = preferencesDataStore.getStringSet("key", emptySet())
        assertThat(value, `is`(setOf("x", "y", "z")))
    }
}
