package org.odk.collect.android.preferences.source

import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
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
    fun `When putString() called, should save() be called on generalPreferences`() {
        preferencesDataStore.putString("Key", "value")
        verify(preferencesDataSource, times(1)).save("Key", "value")
    }

    @Test
    fun `When putBoolean() called, should save() be called on generalPreferences`() {
        preferencesDataStore.putBoolean("Key", true)
        verify(preferencesDataSource, times(1)).save("Key", true)
    }

    @Test
    fun `When putLong() called, should save() be called on generalPreferences`() {
        preferencesDataStore.putLong("Key", 3L)
        verify(preferencesDataSource, times(1)).save("Key", 3L)
    }

    @Test
    fun `When putInt() called, should save() be called on generalPreferences`() {
        preferencesDataStore.putInt("Key", 5)
        verify(preferencesDataSource, times(1)).save("Key", 5)
    }

    @Test
    fun `When putFloat() called, should save() be called on generalPreferences`() {
        preferencesDataStore.putFloat("Key", 8.43f)
        verify(preferencesDataSource, times(1)).save("Key", 8.43f)
    }

    @Test
    fun `When putStringSet() called, should save() be called on generalPreferences`() {
        preferencesDataStore.putStringSet("Key", setOf("x", "y", "z"))
        verify(preferencesDataSource, times(1)).save("Key", setOf("x", "y", "z"))
    }

    @Test
    fun `When getString() called, should getString() be called on generalPreferences`() {
        preferencesDataStore.getString("Key", "value")
        verify(preferencesDataSource, times(1)).getString("Key")
    }

    @Test
    fun `When getBoolean() called, should getBoolean() be called on generalPreferences`() {
        preferencesDataStore.getBoolean("Key", false)
        verify(preferencesDataSource, times(1)).getBoolean("Key")
    }

    @Test
    fun `When getLong() called, should getLong() be called on generalPreferences`() {
        preferencesDataStore.getLong("Key", 0L)
        verify(preferencesDataSource, times(1)).getLong("Key")
    }

    @Test
    fun `When getInt() called, should getInt() be called on generalPreferences`() {
        preferencesDataStore.getInt("Key", 0)
        verify(preferencesDataSource, times(1)).getInt("Key")
    }

    @Test
    fun `When getFloat() called, should getFloat() be called on generalPreferences`() {
        preferencesDataStore.getFloat("Key", 1f)
        verify(preferencesDataSource, times(1)).getFloat("Key")
    }

    @Test
    fun `When getStringSet() called, should getStringSet() be called on generalPreferences`() {
        preferencesDataStore.getStringSet("Key", emptySet())
        verify(preferencesDataSource, times(1)).getStringSet("Key")
    }
}
