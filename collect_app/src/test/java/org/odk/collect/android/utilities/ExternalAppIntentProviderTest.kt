package org.odk.collect.android.utilities

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.javarosa.form.api.FormEntryPrompt
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@RunWith(AndroidJUnit4::class)
class ExternalAppIntentProviderTest {
    private lateinit var formEntryPrompt: FormEntryPrompt
    private lateinit var externalAppIntentProvider: ExternalAppIntentProvider

    @Before
    fun setup() {
        formEntryPrompt = mock()
        externalAppIntentProvider = ExternalAppIntentProvider()
        whenever(formEntryPrompt.index).thenReturn(mock())
    }

    @Test
    fun intentAction_shouldBeSetProperlyIfThePackageNameEndsWithBrackets() {
        whenever(formEntryPrompt.appearanceHint).thenReturn("ex:com.example.collectanswersprovider()")
        val resultIntent = externalAppIntentProvider.getIntentToRunExternalApp(null, formEntryPrompt)
        assertThat(resultIntent.action, `is`("com.example.collectanswersprovider"))
    }

    @Test
    fun intentAction_shouldBeSetProperlyIfThePackageNameDoesNotEndWithBrackets() {
        whenever(formEntryPrompt.appearanceHint).thenReturn("ex:com.example.collectanswersprovider")
        val resultIntent = externalAppIntentProvider.getIntentToRunExternalApp(null, formEntryPrompt)
        assertThat(resultIntent.action, `is`("com.example.collectanswersprovider"))
    }

    @Test
    fun whenNoParamsSpecified_shouldIntentHaveNoExtras() {
        whenever(formEntryPrompt.appearanceHint).thenReturn("ex:com.example.collectanswersprovider()")
        val resultIntent = externalAppIntentProvider.getIntentToRunExternalApp(null, formEntryPrompt)
        assertThat(resultIntent.extras, nullValue())
    }

    @Test
    fun whenParamsSpecified_shouldIntentHaveExtras() {
        whenever(formEntryPrompt.appearanceHint)
            .thenReturn("ex:com.example.collectanswersprovider(param1='value1', param2='value2')")
        val resultIntent = externalAppIntentProvider.getIntentToRunExternalApp(null, formEntryPrompt)
        assertThat(resultIntent.extras!!.keySet().size, `is`(2))
        assertThat(resultIntent.extras!!.getString("param1"), `is`("value1"))
        assertThat(resultIntent.extras!!.getString("param2"), `is`("value2"))
    }

    @Test
    fun whenParamsContainUri_shouldThatUriBeAddedAsIntentData() {
        whenever(formEntryPrompt.appearanceHint)
            .thenReturn("ex:com.example.collectanswersprovider(param1='value1', uri_data='file:///tmp/android.txt')")
        val resultIntent = externalAppIntentProvider.getIntentToRunExternalApp(null, formEntryPrompt)
        assertThat(resultIntent.data.toString(), `is`("file:///tmp/android.txt"))
        assertThat(resultIntent.extras!!.keySet().size, `is`(1))
        assertThat(resultIntent.extras!!.getString("param1"), `is`("value1"))
    }

    @Test
    fun packageNameCanBeMixedWithOtherAppearances() {
        whenever(formEntryPrompt.appearanceHint)
            .thenReturn("masked ex:com.example.collectanswersprovider(param1='value1', param2='value2') thousands-sep")
        val resultIntent = externalAppIntentProvider.getIntentToRunExternalApp(null, formEntryPrompt)
        assertThat(resultIntent.action, `is`("com.example.collectanswersprovider"))
        assertThat(resultIntent.extras!!.keySet().size, `is`(2))
        assertThat(resultIntent.extras!!.getString("param1"), `is`("value1"))
        assertThat(resultIntent.extras!!.getString("param2"), `is`("value2"))
    }
}
