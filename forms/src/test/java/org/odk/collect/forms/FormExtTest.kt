package org.odk.collect.forms

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.odk.collect.formstest.FormFixtures

class FormExtTest {
    @Test
    fun `should return true when auto send is not set on a form level and enabled in settings`() {
        val form = FormFixtures.form()
        val result = form.shouldFormBeSentAutomatically(true)
        assertThat(result, equalTo(true))
    }

    @Test
    fun `should return true when auto send is enabled on a form level and enabled in settings`() {
        val form = FormFixtures.form(autoSend = "true")
        val result = form.shouldFormBeSentAutomatically(true)
        assertThat(result, equalTo(true))
    }

    @Test
    fun `should return true when auto send is enabled on a form level but not sanitized and enabled in settings`() {
        val form = FormFixtures.form(autoSend = " True ")
        val result = form.shouldFormBeSentAutomatically(true)
        assertThat(result, equalTo(true))
    }

    @Test
    fun `should return true when auto send is set on a form level but with a wrong value and enabled in settings`() {
        val form = FormFixtures.form(autoSend = "something")
        val result = form.shouldFormBeSentAutomatically(true)
        assertThat(result, equalTo(true))
    }

    @Test
    fun `should return true when auto send is enabled on a form level and disabled in settings`() {
        val form = FormFixtures.form(autoSend = "true")
        val result = form.shouldFormBeSentAutomatically(false)
        assertThat(result, equalTo(true))
    }

    @Test
    fun `should return true when auto send is enabled on a form level but not sanitized and disabled in settings`() {
        val form = FormFixtures.form(autoSend = " True ")
        val result = form.shouldFormBeSentAutomatically(false)
        assertThat(result, equalTo(true))
    }

    @Test
    fun `should return false when auto send is not set on a form level and disabled in settings`() {
        val form = FormFixtures.form()
        val result = form.shouldFormBeSentAutomatically(false)
        assertThat(result, equalTo(false))
    }

    @Test
    fun `should return false when auto send is disabled on a form level and disabled in settings`() {
        val form = FormFixtures.form(autoSend = "false")
        val result = form.shouldFormBeSentAutomatically(false)
        assertThat(result, equalTo(false))
    }

    @Test
    fun `should return false when auto send is disabled on a form level and enabled in settings`() {
        val form = FormFixtures.form(autoSend = "false")
        val result = form.shouldFormBeSentAutomatically(true)
        assertThat(result, equalTo(false))
    }

    @Test
    fun `should return false when auto send is set on a form level but with a wrong value and disabled in settings`() {
        val form = FormFixtures.form(autoSend = "something")
        val result = form.shouldFormBeSentAutomatically(false)
        assertThat(result, equalTo(false))
    }
}
