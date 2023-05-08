package org.odk.collect.android.formentry

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.odk.collect.android.instancemanagement.autosend.AutoSendSettingsProvider
import org.odk.collect.formstest.FormFixtures
import org.odk.collect.settings.InMemSettingsProvider
import org.odk.collect.settings.keys.ProtectedProjectKeys

class FormEndViewModelTest {
    private val settingsProvider = InMemSettingsProvider()
    private val autoSendSettingsProvider = mock<AutoSendSettingsProvider>()
    private val formEndViewModel = FormEndViewModel(settingsProvider, autoSendSettingsProvider)

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
        settingsProvider.getProtectedSettings().save(ProtectedProjectKeys.KEY_FINALIZE, true)
        assertThat(formEndViewModel.isFinalizeEnabled(), equalTo(true))
    }

    @Test
    fun `when 'Finalize' is disabled, isFinalizeEnabled should return false`() {
        settingsProvider.getProtectedSettings().save(ProtectedProjectKeys.KEY_FINALIZE, false)
        assertThat(formEndViewModel.isFinalizeEnabled(), equalTo(false))
    }

    @Test
    fun `when autoSend is enabled in settings, shouldFormBeSentAutomatically should return true`() {
        whenever(autoSendSettingsProvider.isAutoSendEnabledInSettings()).thenReturn(true)
        val form = FormFixtures.form()
        assertThat(formEndViewModel.shouldFormBeSentAutomatically(form), equalTo(true))
    }

    @Test
    fun `when autoSend is disabled in settings, shouldFormBeSentAutomatically should return false`() {
        whenever(autoSendSettingsProvider.isAutoSendEnabledInSettings()).thenReturn(false)
        val form = FormFixtures.form()
        assertThat(formEndViewModel.shouldFormBeSentAutomatically(form), equalTo(false))
    }
}
