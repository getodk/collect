package org.odk.collect.androidshared.livedata

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Rule
import org.junit.Test
import org.odk.collect.androidshared.livedata.LiveDataExt.combine
import org.odk.collect.testshared.getOrAwaitValue

class LiveDataUtilsTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun `#combine has initial value from both LiveData instances`() {
        val combined = MutableLiveData("one").combine(MutableLiveData("two"))
        assertThat(combined.getOrAwaitValue(), equalTo(Pair("one", "two")))
    }

    @Test
    fun `#combine uses null values for uninitialized LiveData instances`() {
        val combined = MutableLiveData<String>().combine(MutableLiveData<String>())
        assertThat(combined.getOrAwaitValue(), equalTo(Pair(null, null)))
    }

    @Test
    fun `#combine updates when either LiveData updates`() {
        val one = MutableLiveData("one")
        val combined = one.combine(MutableLiveData("two"))
        one.value = "one-updated"

        assertThat(combined.getOrAwaitValue(), equalTo(Pair("one-updated", "two")))
    }
}
