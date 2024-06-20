package org.odk.collect.android.instancemanagement.autosend

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.odk.collect.formstest.FormFixtures

class FormExtTest {
    @Test
    fun `#shouldFormBeSentAutomatically returns true when auto send is not set on a form level and enabled in settings`() {
        val form = FormFixtures.form()
        val result = form.shouldFormBeSentAutomatically(true)
        assertThat(result, equalTo(true))
    }

    @Test
    fun `#shouldFormBeSentAutomatically returns true when auto send is enabled on a form level and enabled in settings`() {
        val form = FormFixtures.form(autoSend = "true")
        val result = form.shouldFormBeSentAutomatically(true)
        assertThat(result, equalTo(true))
    }

    @Test
    fun `#shouldFormBeSentAutomatically returns true when auto send is enabled on a form level and disabled in settings`() {
        val form = FormFixtures.form(autoSend = "true")
        val result = form.shouldFormBeSentAutomatically(false)
        assertThat(result, equalTo(true))
    }

    @Test
    fun `#shouldFormBeSentAutomatically returns false when auto send is not set on a form level and disabled in settings`() {
        val form = FormFixtures.form()
        val result = form.shouldFormBeSentAutomatically(false)
        assertThat(result, equalTo(false))
    }

    @Test
    fun `#shouldFormBeSentAutomatically returns false when auto send is disabled on a form level and disabled in settings`() {
        val form = FormFixtures.form(autoSend = "false")
        val result = form.shouldFormBeSentAutomatically(false)
        assertThat(result, equalTo(false))
    }

    @Test
    fun `#shouldFormBeSentAutomatically returns false when auto send is disabled on a form level and enabled in settings`() {
        val form = FormFixtures.form(autoSend = "false")
        val result = form.shouldFormBeSentAutomatically(true)
        assertThat(result, equalTo(false))
    }

    @Test
    fun `#getAutoSendMode returns NEUTRAL when autoSend is unsupported`() {
        val form = FormFixtures.form(autoSend = "blah")
        assertThat(form.getAutoSendMode(), equalTo(FormAutoSendMode.NEUTRAL))
    }

    @Test
    fun `#getAutoSendMode returns FORCED when autoSend is true but incorrectly cased`() {
        val form = FormFixtures.form(autoSend = "TRUE")
        assertThat(form.getAutoSendMode(), equalTo(FormAutoSendMode.FORCED))
    }

    @Test
    fun `#getAutoSendMode returns FORCED when autoSend is true but with whitespace`() {
        val form = FormFixtures.form(autoSend = " true ")
        assertThat(form.getAutoSendMode(), equalTo(FormAutoSendMode.FORCED))
    }

    @Test
    fun `#getAutoSendMode returns OPT_OUT when autoSend is false but incorrectly cased`() {
        val form = FormFixtures.form(autoSend = "FALSE")
        assertThat(form.getAutoSendMode(), equalTo(FormAutoSendMode.OPT_OUT))
    }

    @Test
    fun `#getAutoSendMode returns OPT_OUT when autoSend is false but with whitespace`() {
        val form = FormFixtures.form(autoSend = " false ")
        assertThat(form.getAutoSendMode(), equalTo(FormAutoSendMode.OPT_OUT))
    }
}
