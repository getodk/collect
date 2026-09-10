package org.odk.collect.shared.strings

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.nullValue
import org.junit.Test
import java.net.URI

class URIExtTest {

    @Test
    fun `#getQueryParameter returns null when there is no query`() {
        val uri = URI("https://example.com")
        assertThat(uri.getQueryParameter("id"), nullValue())
    }

    @Test
    fun `#getQueryParameter returns the value when the query has a trailing separator`() {
        val uri = URI("https://example.com?id=123&")
        assertThat(uri.getQueryParameter("id"), equalTo("123"))
    }

    @Test
    fun `#getQueryParameter returns the value when the URI has a path`() {
        val uri = URI("https://example.com/blah?id=123")
        assertThat(uri.getQueryParameter("id"), equalTo("123"))
    }

    @Test
    fun `#getQueryParameter returns value when the query is malformed with extra =`() {
        val uri = URI("https://example.com?id=a=b")
        assertThat(uri.getQueryParameter("id"), equalTo("a=b"))
    }

    @Test
    fun `#getQueryParameter returns first value when the parameter is used more than once`() {
        val uri = URI("https://example.com?id=1&id=2")
        assertThat(uri.getQueryParameter("id"), equalTo("1"))
    }

    @Test
    fun `#getQueryParameter returns empty string when there is no value`() {
        val uri = URI("https://example.com?id")
        assertThat(uri.getQueryParameter("id"), equalTo(""))
    }
}
