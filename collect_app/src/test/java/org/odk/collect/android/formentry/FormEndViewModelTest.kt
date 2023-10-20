package org.odk.collect.android.formentry

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.odk.collect.android.formentry.support.InMemFormSessionRepository
import org.odk.collect.android.instancemanagement.autosend.AutoSendSettingsProvider
import org.odk.collect.formstest.FormFixtures
import org.odk.collect.settings.InMemSettingsProvider
import org.odk.collect.settings.keys.ProtectedProjectKeys

@RunWith(AndroidJUnit4::class)
class FormEndViewModelTest {
    private val formSessionRepository = InMemFormSessionRepository()
    private val sessionId = "blah"
    private val settingsProvider = InMemSettingsProvider()
    private val autoSendSettingsProvider = mock<AutoSendSettingsProvider>()
    private val formEndViewModel = FormEndViewModel(formSessionRepository, sessionId, settingsProvider, autoSendSettingsProvider)

    @Test
    fun `when 'Save as draft' is enabled, isSaveDraftEnabled should return true`() {
        settingsProvider.getProtectedSettings().save(ProtectedProjectKeys.KEY_SAVE_AS_DRAFT, true)
        assertThat(formEndViewModel.isSaveDraftEnabled(), equalTo(true))
    }

    @Test
    fun `when 'Save as draft' is disabled, isSaveDraftEnabled should return false`() {
        settingsProvider.getProtectedSettings().save(ProtectedProjectKeys.KEY_SAVE_AS_DRAFT, false)
        assertThat(formEndViewModel.isSaveDraftEnabled(), equalTo(false))
    }

    @Test
    fun `when 'Finalize' is enabled, isFinalizeEnabled should return true`() {
        settingsProvider.getProtectedSettings().save(ProtectedProjectKeys.KEY_FINALIZE_IN_FORM_ENTRY, true)
        assertThat(formEndViewModel.isFinalizeEnabled(), equalTo(true))
    }

    @Test
    fun `when 'Finalize' is disabled, isFinalizeEnabled should return false`() {
        settingsProvider.getProtectedSettings().save(ProtectedProjectKeys.KEY_FINALIZE_IN_FORM_ENTRY, false)
        assertThat(formEndViewModel.isFinalizeEnabled(), equalTo(false))
    }

    @Test
    fun `when autoSend is enabled in settings, shouldFormBeSentAutomatically should return true`() {
        whenever(autoSendSettingsProvider.isAutoSendEnabledInSettings()).thenReturn(true)
        formSessionRepository.set(sessionId, mock(), FormFixtures.form())
        assertThat(formEndViewModel.shouldFormBeSentAutomatically(), equalTo(true))
    }

    @Test
    fun `when autoSend is disabled in settings, shouldFormBeSentAutomatically should return false`() {
        whenever(autoSendSettingsProvider.isAutoSendEnabledInSettings()).thenReturn(false)
        formSessionRepository.set(sessionId, mock(), FormFixtures.form())
        assertThat(formEndViewModel.shouldFormBeSentAutomatically(), equalTo(false))
    }
}
