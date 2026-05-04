package org.odk.collect.entities.javarosa.parse

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class StringExtTest {

    @Test
    fun `#isV4UUID is false for version 1 UUIDs`() {
        val v1UUID = "6b5ea8de-9565-11e8-9eb6-529269fb1459"
        assertThat(UUID.fromString(v1UUID).version(), equalTo(1))
        assertThat(v1UUID.isV4UUID(), equalTo(false))
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

    @Test
    fun `#isV4UUID is false for invalid UUID string`() {
        val invalid = "not-a-uuid"
        assertThat(invalid.isV4UUID(), equalTo(false))
    }

    @Test
    fun `#toUriWithParam adds single query param to uri`() {
        val url = "https://example.com"
        val result = url.toUriWithParam("id", "123")

        assertThat(result.toString(), equalTo("https://example.com?id=123"))
    }

    @Test
    fun `#toUriWithParam preserves existing query parameters`() {
        val url = "https://example.com?foo=bar"
        val result = url.toUriWithParam("id", "123")

        assertThat(result.toString(), equalTo("https://example.com?foo=bar&id=123"))
    }

    @Test
    fun `#toUriWithParam sets null value as query param`() {
        val url = "https://example.com"
        val result = url.toUriWithParam("id", null)

        assertThat(result.toString(), equalTo("https://example.com?id=null"))
    }

    @Test
    fun `#toUriWithParam does not change uri path`() {
        val url = "https://example.com/path/subpath"
        val result = url.toUriWithParam("id", "123")

        assertThat(result.toString(), equalTo("https://example.com/path/subpath?id=123"))
    }
}
