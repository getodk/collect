package org.odk.collect.entities.javarosa.parse

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.odk.collect.entities.javarosa.parse.StringExt.isV4UUID
import java.util.UUID

class StringExtTest {

    @Test
    fun `#isV4UUID is false for version 1 UUIDs`() {
        val v2UUID = "6b5ea8de-9565-11e8-9eb6-529269fb1459"
        assertThat(UUID.fromString(v2UUID).version(), equalTo(1))
        assertThat(v2UUID.isV4UUID(), equalTo(false))
    }

    @Test
    fun `#isV4UUID is false for version 2 UUIDs`() {
        val v2UUID = "00000000-0000-2000-8000-000000000000"
        assertThat(UUID.fromString(v2UUID).version(), equalTo(2))
        assertThat(v2UUID.isV4UUID(), equalTo(false))
    }

    @Test
    fun `#isV4UUID is false for version 3 UUIDs`() {
        val v3UUID = UUID.nameUUIDFromBytes("blah".toByteArray()).toString()
        assertThat(v3UUID.isV4UUID(), equalTo(false))
    }

    /**
     * We just test up to version 4 here because Java only natively supports those versions (in
     * [UUID]).
     */
    @Test
    fun `#isV4UUID is true for version 4 UUIDs`() {
        val v4UUID = UUID.randomUUID().toString()
        assertThat(v4UUID.isV4UUID(), equalTo(true))
    }
}
