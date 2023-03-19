package org.odk.collect.android.utilities

import androidx.test.espresso.matcher.ViewMatchers.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.not
import org.junit.Test

class ChangeLockProviderTest {

    @Test
    fun `getFormLock returns a different lock for different projects`() {
        val changeLockProvider = ChangeLockProvider()
        val lock1 = changeLockProvider.getFormLock("blah1")
        val lock2 = changeLockProvider.getFormLock("blah2")
        assertThat(lock1, not(equalTo(lock2)))
    }

    @Test
    fun `getFormLock returns the same lock every time for a project`() {
        val changeLockProvider = ChangeLockProvider()
        val lock1 = changeLockProvider.getFormLock("blah")
        val lock2 = changeLockProvider.getFormLock("blah")
        assertThat(lock1, equalTo(lock2))
    }

    @Test
    fun `getInstanceLock returns a different lock for different projects`() {
        val changeLockProvider = ChangeLockProvider()
        val lock1 = changeLockProvider.getInstanceLock("blah1")
        val lock2 = changeLockProvider.getInstanceLock("blah2")
        assertThat(lock1, not(equalTo(lock2)))
    }

    @Test
    fun `getInstanceLock returns the same lock every time for a project`() {
        val changeLockProvider = ChangeLockProvider()
        val lock1 = changeLockProvider.getInstanceLock("blah")
        val lock2 = changeLockProvider.getInstanceLock("blah")
        assertThat(lock1, equalTo(lock2))
    }

    @Test
    fun `getInstanceLock and getFormLock return different locks for the same project`() {
        val changeLockProvider = ChangeLockProvider()
        val formLock = changeLockProvider.getFormLock("blah1")
        val instanceLock = changeLockProvider.getInstanceLock("blah1")
        assertThat(formLock, not(equalTo(instanceLock)))
    }
}
