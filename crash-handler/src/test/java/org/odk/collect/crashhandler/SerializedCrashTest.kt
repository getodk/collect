package org.odk.collect.crashhandler

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test

class SerializedCrashTest {

    @Test
    fun `can round trip encode and decode`() {
        val original = SerializedCrash(true, "blah")
        val encoded = original.encode()
        val decoded = SerializedCrash.decode(encoded)
        assertThat(decoded, equalTo(original))
    }

    @Test
    fun `decode returns null if string is invalid`() {
        val decoded = SerializedCrash.decode("blah")
        assertThat(decoded, equalTo(null))
    }
}
