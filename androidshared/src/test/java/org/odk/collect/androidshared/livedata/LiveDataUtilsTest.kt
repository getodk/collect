package org.odk.collect.androidshared.livedata

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Rule
import org.junit.Test
import org.odk.collect.androidshared.livedata.LiveDataExt.zip
import org.odk.collect.testshared.getOrAwaitValue

class LiveDataUtilsTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun `#zip has initial value from both LiveData instances`() {
        val zipped = MutableLiveData("one").zip(MutableLiveData("two"))
        assertThat(zipped.getOrAwaitValue(), equalTo(Pair("one", "two")))
    }

    @Test
    fun `#zip uses null values for uninitialized LiveData instances`() {
        val zipped = MutableLiveData<String>().zip(MutableLiveData<String>())
        assertThat(zipped.getOrAwaitValue(), equalTo(Pair(null, null)))
    }

    @Test
    fun `#zip updates when either LiveData updates`() {
        val one = MutableLiveData("one")
        val zipped = one.zip(MutableLiveData("two"))
        one.value = "one-updated"

        assertThat(zipped.getOrAwaitValue(), equalTo(Pair("one-updated", "two")))
    }
}
