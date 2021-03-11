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
        `when`(preferencesDataSource.getString(KEY)).thenReturn(STRING_ANSWER)
        `when`(preferencesDataSource.getBoolean(KEY)).thenReturn(BOOLEAN_ANSWER)
        `when`(preferencesDataSource.getLong(KEY)).thenReturn(LONG_ANSWER)
        `when`(preferencesDataSource.getInt(KEY)).thenReturn(INT_ANSWER)
        `when`(preferencesDataSource.getFloat(KEY)).thenReturn(FLOAT_ANSWER)
        `when`(preferencesDataSource.getStringSet(KEY)).thenReturn(SET_OF_STRINGS_ANSWER)

        preferencesDataStore = PreferencesDataStore(preferencesDataSource)
    }

    @Test
    fun `putString() saves string in preferences`() {
        preferencesDataStore.putString(KEY, STRING_ANSWER)
        verify(preferencesDataSource).save(KEY, STRING_ANSWER)
    }

    @Test
    fun `putBoolean() saves boolean in preferences`() {
        preferencesDataStore.putBoolean(KEY, BOOLEAN_ANSWER)
        verify(preferencesDataSource).save(KEY, BOOLEAN_ANSWER)
    }

    @Test
    fun `putLong() saves long in preferences`() {
        preferencesDataStore.putLong(KEY, LONG_ANSWER)
        verify(preferencesDataSource).save(KEY, LONG_ANSWER)
    }

    @Test
    fun `putInt() saves int in preferences`() {
        preferencesDataStore.putInt(KEY, INT_ANSWER)
        verify(preferencesDataSource).save(KEY, INT_ANSWER)
    }

    @Test
    fun `putFloat() saves float in preferences`() {
        preferencesDataStore.putFloat(KEY, FLOAT_ANSWER)
        verify(preferencesDataSource).save(KEY, FLOAT_ANSWER)
    }

    @Test
    fun `putStringSet() saves set of strings in preferences`() {
        preferencesDataStore.putStringSet(KEY, SET_OF_STRINGS_ANSWER)
        verify(preferencesDataSource).save(KEY, SET_OF_STRINGS_ANSWER)
    }

    @Test
    fun `getString() reads string from preferences`() {
        val answer = preferencesDataStore.getString(KEY, "")
        verify(preferencesDataSource).getString(KEY)
        assertThat(answer, `is`(STRING_ANSWER))
    }

    @Test
    fun `getBoolean() reads boolean from preferences`() {
        val answer = preferencesDataStore.getBoolean(KEY, false)
        verify(preferencesDataSource).getBoolean(KEY)
        assertThat(answer, `is`(BOOLEAN_ANSWER))
    }

    @Test
    fun `getLong() reads long from preferences`() {
        val answer = preferencesDataStore.getLong(KEY, 0L)
        verify(preferencesDataSource).getLong(KEY)
        assertThat(answer, `is`(LONG_ANSWER))
    }

    @Test
    fun `getInt() reads int from preferences`() {
        val answer = preferencesDataStore.getInt(KEY, 0)
        verify(preferencesDataSource).getInt(KEY)
        assertThat(answer, `is`(INT_ANSWER))
    }

    @Test
    fun `getFloat() reads float from preferences`() {
        val answer = preferencesDataStore.getFloat(KEY, 1f)
        verify(preferencesDataSource).getFloat(KEY)
        assertThat(answer, `is`(FLOAT_ANSWER))
    }

    @Test
    fun `getStringSet() reads set of strings from preferences`() {
        val answer = preferencesDataStore.getStringSet(KEY, emptySet())
        verify(preferencesDataSource).getStringSet(KEY)
        assertThat(answer, `is`(SET_OF_STRINGS_ANSWER))
    }

    companion object {
        private const val KEY = "Key"
        private const val STRING_ANSWER = "value"
        private const val BOOLEAN_ANSWER = true
        private const val LONG_ANSWER = 3L
        private const val INT_ANSWER = 5
        private const val FLOAT_ANSWER = 8.43f
        private val SET_OF_STRINGS_ANSWER = setOf("x", "y", "z")
    }
}
