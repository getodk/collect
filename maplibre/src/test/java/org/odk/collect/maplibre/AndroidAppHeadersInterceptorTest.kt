package org.odk.collect.maplibre

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.Signature
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.nullValue
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.odk.collect.testshared.MockWebServerRule

class AndroidAppHeadersInterceptorTest {
    @get:Rule
    val mockWebServerRule = MockWebServerRule()

    private val signature = mock<Signature> {
        on { toByteArray() } doReturn "abc".toByteArray()
    }

    private val packageManager = mock<PackageManager> {
        on { getPackageInfo(eq("org.odk.collect"), any<Int>()) } doReturn PackageInfo().apply { signatures = arrayOf(signature) }
    }

    private val context = mock<Context> {
        on { packageName } doReturn "org.odk.collect"
        on { packageManager } doReturn packageManager
    }

    @Test
    fun `adds Android app headers to requests sent to the host`() {
        val server = mockWebServerRule.start()
        server.enqueue(MockResponse())

        val client = OkHttpClient.Builder()
            .addInterceptor(AndroidAppHeadersInterceptor(context, server.hostName))
            .build()
        client.newCall(Request.Builder().url(server.url("/")).build()).execute().close()

        val request = server.takeRequest()
        assertThat(request.getHeader("X-Android-Package"), equalTo("org.odk.collect"))
        // SHA-1 of "abc"
        assertThat(request.getHeader("X-Android-Cert"), equalTo("A9993E364706816ABA3E25717850C26C9CD0D89D"))
    }

    @Test
    fun `does not add Android app headers to other requests`() {
        val server = mockWebServerRule.start()
        server.enqueue(MockResponse())

        val client = OkHttpClient.Builder()
            .addInterceptor(AndroidAppHeadersInterceptor(context, "tile.googleapis.com"))
            .build()
        client.newCall(Request.Builder().url(server.url("/")).build()).execute().close()

        val request = server.takeRequest()
        assertThat(request.getHeader("X-Android-Package"), nullValue())
        assertThat(request.getHeader("X-Android-Cert"), nullValue())
    }
}
