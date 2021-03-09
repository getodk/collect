package org.odk.collect.android.preferences.source

import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify

class AdminPreferencesDataStoreTest {
    private lateinit var adminPreferencesDataStore: AdminPreferencesDataStore
    private lateinit var preferencesDataSourceProvider: PreferencesDataSourceProvider
    private lateinit var adminPreferences: PreferencesDataSource

    @Before
    fun setup() {
        preferencesDataSourceProvider = mock(PreferencesDataSourceProvider::class.java)
        adminPreferences = mock(PreferencesDataSource::class.java)
        `when`(preferencesDataSourceProvider.getAdminPreferences()).thenReturn(adminPreferences)
        adminPreferencesDataStore = AdminPreferencesDataStore(preferencesDataSourceProvider)
    }

    @Test
    fun `When putString() called, should save() be called on adminPreferences`() {
        adminPreferencesDataStore.putString("Key", "value")
        verify(adminPreferences, times(1)).save("Key", "value")
    }

    @Test
    fun `When putBoolean() called, should save() be called on adminPreferences`() {
        adminPreferencesDataStore.putBoolean("Key", true)
        verify(adminPreferences, times(1)).save("Key", true)
    }

    @Test
    fun `When putLong() called, should save() be called on adminPreferences`() {
        adminPreferencesDataStore.putLong("Key", 3L)
        verify(adminPreferences, times(1)).save("Key", 3L)
    }

    @Test
    fun `When putInt() called, should save() be called on adminPreferences`() {
        adminPreferencesDataStore.putInt("Key", 5)
        verify(adminPreferences, times(1)).save("Key", 5)
    }

    @Test
    fun `When putFloat() called, should save() be called on adminPreferences`() {
        adminPreferencesDataStore.putFloat("Key", 8.43f)
        verify(adminPreferences, times(1)).save("Key", 8.43f)
    }

    @Test
    fun `When putStringSet() called, should save() be called on adminPreferences`() {
        adminPreferencesDataStore.putStringSet("Key", setOf("x", "y", "z"))
        verify(adminPreferences, times(1)).save("Key", setOf("x", "y", "z"))
    }

    @Test
    fun `When getString() called, should getString() be called on adminPreferences`() {
        adminPreferencesDataStore.getString("Key", "value")
        verify(adminPreferences, times(1)).getString("Key")
    }

    @Test
    fun `When getBoolean() called, should getBoolean() be called on adminPreferences`() {
        adminPreferencesDataStore.getBoolean("Key", false)
        verify(adminPreferences, times(1)).getBoolean("Key")
    }

    @Test
    fun `When getLong() called, should getLong() be called on adminPreferences`() {
        adminPreferencesDataStore.getLong("Key", 0L)
        verify(adminPreferences, times(1)).getLong("Key")
    }

    @Test
    fun `When getInt() called, should getInt() be called on adminPreferences`() {
        adminPreferencesDataStore.getInt("Key", 0)
        verify(adminPreferences, times(1)).getInt("Key")
    }

    @Test
    fun `When getFloat() called, should getFloat() be called on adminPreferences`() {
        adminPreferencesDataStore.getFloat("Key", 1f)
        verify(adminPreferences, times(1)).getFloat("Key")
    }

    @Test
    fun `When getStringSet() called, should getStringSet() be called on adminPreferences`() {
        adminPreferencesDataStore.getStringSet("Key", emptySet())
        verify(adminPreferences, times(1)).getStringSet("Key")
    }
}
