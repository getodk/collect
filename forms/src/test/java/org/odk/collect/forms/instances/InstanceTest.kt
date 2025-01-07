package org.odk.collect.forms.instances

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test

class InstanceTest {

    @Test
    fun `canDeleteBeforeSend is true by default`() {
        val instance = Instance.Builder().build()
        assertThat(instance.canDeleteBeforeSend(), equalTo(true))
    }

    @Test
    fun `canDelete returns true if canDeleteBeforeSend is true no matter what the status is`() {
        listOf(
            Instance.STATUS_INCOMPLETE,
            Instance.STATUS_INVALID,
            Instance.STATUS_VALID,
            Instance.STATUS_COMPLETE,
            Instance.STATUS_SUBMISSION_FAILED,
            Instance.STATUS_SUBMITTED
        ).forEach { status ->
            val instance = Instance
                .Builder()
                .status(status)
                .canDeleteBeforeSend(true)
                .build()

            assertThat(instance.canDelete(), equalTo(true))
        }
    }

    @Test
    fun `canDelete returns true if canDeleteBeforeSend is false but form is not finalized or finalized but sent`() {
        listOf(
            Instance.STATUS_INCOMPLETE,
            Instance.STATUS_INVALID,
            Instance.STATUS_VALID,
            Instance.STATUS_SUBMITTED
        ).forEach { status ->
            val instance = Instance
                .Builder()
                .status(status)
                .canDeleteBeforeSend(false)
                .build()

            assertThat(instance.canDelete(), equalTo(true))
        }
    }

    @Test
    fun `canDelete returns false if canDeleteBeforeSend is false but form is finalized`() {
        listOf(
            Instance.STATUS_COMPLETE,
            Instance.STATUS_SUBMISSION_FAILED
        ).forEach { status ->
            val instance = Instance
                .Builder()
                .status(status)
                .canDeleteBeforeSend(false)
                .build()

            assertThat(instance.canDelete(), equalTo(false))
        }
    }
}
