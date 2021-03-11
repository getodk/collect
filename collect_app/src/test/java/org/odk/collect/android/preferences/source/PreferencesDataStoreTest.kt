package org.odk.collect.android.preferences.source

import org.junit.Before
import org.junit.Test
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
        preferencesDataStore.putString("Key", "value")
        verify(preferencesDataSource).save("Key", "value")
    }

    @Test
    fun `putBoolean() saves boolean in preferences`() {
        preferencesDataStore.putBoolean("Key", true)
        verify(preferencesDataSource).save("Key", true)
    }

    @Test
    fun `putLong() saves long in preferences`() {
        preferencesDataStore.putLong("Key", 3L)
        verify(preferencesDataSource).save("Key", 3L)
    }

    @Test
    fun `putInt() saves int in preferences`() {
        preferencesDataStore.putInt("Key", 5)
        verify(preferencesDataSource).save("Key", 5)
    }

    @Test
    fun `putFloat() saves float in preferences`() {
        preferencesDataStore.putFloat("Key", 8.43f)
        verify(preferencesDataSource).save("Key", 8.43f)
    }

    @Test
    fun `putStringSet() saves set of strings in preferences`() {
        preferencesDataStore.putStringSet("Key", setOf("x", "y", "z"))
        verify(preferencesDataSource).save("Key", setOf("x", "y", "z"))
    }

    @Test
    fun `getString() reads string from preferences`() {
        preferencesDataStore.getString("Key", "value")
        verify(preferencesDataSource).getString("Key")
    }

    @Test
    fun `getBoolean() reads boolean from preferences`() {
        preferencesDataStore.getBoolean("Key", false)
        verify(preferencesDataSource).getBoolean("Key")
    }

    @Test
    fun `getLong() reads long from preferences`() {
        preferencesDataStore.getLong("Key", 0L)
        verify(preferencesDataSource).getLong("Key")
    }

    @Test
    fun `getInt() reads int from preferences`() {
        preferencesDataStore.getInt("Key", 0)
        verify(preferencesDataSource).getInt("Key")
    }

    @Test
    fun `getFloat() reads float from preferences`() {
        preferencesDataStore.getFloat("Key", 1f)
        verify(preferencesDataSource).getFloat("Key")
    }

    @Test
    fun `getStringSet() reads set of strings from preferences`() {
        preferencesDataStore.getStringSet("Key", emptySet())
        verify(preferencesDataSource).getStringSet("Key")
    }
}
