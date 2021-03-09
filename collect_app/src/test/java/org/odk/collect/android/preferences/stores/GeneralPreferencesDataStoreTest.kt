package org.odk.collect.android.preferences.stores

import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.odk.collect.android.preferences.PreferencesDataSource
import org.odk.collect.android.preferences.PreferencesDataSourceProvider

class GeneralPreferencesDataStoreTest {
    private lateinit var generalPreferencesDataStore: GeneralPreferencesDataStore
    private lateinit var preferencesDataSourceProvider: PreferencesDataSourceProvider
    private lateinit var generalPreferences: PreferencesDataSource

    @Before
    fun setup() {
        preferencesDataSourceProvider = mock(PreferencesDataSourceProvider::class.java)
        generalPreferences = mock(PreferencesDataSource::class.java)
        `when`(preferencesDataSourceProvider.getGeneralPreferences()).thenReturn(generalPreferences)
        generalPreferencesDataStore = GeneralPreferencesDataStore(preferencesDataSourceProvider)
    }

    @Test
    fun `When putString() called, should save() be called on generalPreferences`() {
        generalPreferencesDataStore.putString("Key", "value")
        verify(generalPreferences, times(1)).save("Key", "value")
    }

    @Test
    fun `When putBoolean() called, should save() be called on generalPreferences`() {
        generalPreferencesDataStore.putBoolean("Key", true)
        verify(generalPreferences, times(1)).save("Key", true)
    }

    @Test
    fun `When putLong() called, should save() be called on generalPreferences`() {
        generalPreferencesDataStore.putLong("Key", 3L)
        verify(generalPreferences, times(1)).save("Key", 3L)
    }

    @Test
    fun `When putInt() called, should save() be called on generalPreferences`() {
        generalPreferencesDataStore.putInt("Key", 5)
        verify(generalPreferences, times(1)).save("Key", 5)
    }

    @Test
    fun `When putFloat() called, should save() be called on generalPreferences`() {
        generalPreferencesDataStore.putFloat("Key", 8.43f)
        verify(generalPreferences, times(1)).save("Key", 8.43f)
    }

    @Test
    fun `When putStringSet() called, should save() be called on generalPreferences`() {
        generalPreferencesDataStore.putStringSet("Key", setOf("x", "y", "z"))
        verify(generalPreferences, times(1)).save("Key", setOf("x", "y", "z"))
    }

    @Test
    fun `When getString() called, should getString() be called on generalPreferences`() {
        generalPreferencesDataStore.getString("Key", "value")
        verify(generalPreferences, times(1)).getString("Key")
    }

    @Test
    fun `When getBoolean() called, should getBoolean() be called on generalPreferences`() {
        generalPreferencesDataStore.getBoolean("Key", false)
        verify(generalPreferences, times(1)).getBoolean("Key")
    }

    @Test
    fun `When getLong() called, should getLong() be called on generalPreferences`() {
        generalPreferencesDataStore.getLong("Key", 0L)
        verify(generalPreferences, times(1)).getLong("Key")
    }

    @Test
    fun `When getInt() called, should getInt() be called on generalPreferences`() {
        generalPreferencesDataStore.getInt("Key", 0)
        verify(generalPreferences, times(1)).getInt("Key")
    }

    @Test
    fun `When getFloat() called, should getFloat() be called on generalPreferences`() {
        generalPreferencesDataStore.getFloat("Key", 1f)
        verify(generalPreferences, times(1)).getFloat("Key")
    }

    @Test
    fun `When getStringSet() called, should getStringSet() be called on generalPreferences`() {
        generalPreferencesDataStore.getStringSet("Key", emptySet())
        verify(generalPreferences, times(1)).getStringSet("Key")
    }
}
