package org.odk.collect.maplibre

import android.app.Application
import android.content.pm.Signature
import android.content.pm.SigningInfo
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.nullValue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf

@RunWith(AndroidJUnit4::class)
class AndroidAppHeadersInterceptorTest {
    private val application = ApplicationProvider.getApplicationContext<Application>()
    private val sentRequests = mutableListOf<Request>()

    private val client = OkHttpClient.Builder()
        .addInterceptor(AndroidAppHeadersInterceptor(application, "tile.googleapis.com"))
        .addInterceptor { chain ->
            sentRequests.add(chain.request())
            Response.Builder()
                .request(chain.request())
                .protocol(Protocol.HTTP_2)
                .code(200)
                .message("OK")
                .build()
        }
        .build()

    @Before
    fun setup() {
        val packageInfo = shadowOf(application.packageManager).getInternalMutablePackageInfo(application.packageName)
        packageInfo.signingInfo = SigningInfo().also {
            shadowOf(it).setSignatures(arrayOf(Signature("abc".toByteArray())))
        }
    }

    @Test
    fun `adds Android app headers to requests sent to the host`() {
        client
            .newCall(Request.Builder().url("https://tile.googleapis.com/v1/2dtiles/1/1/1").build())
            .execute()

        val request = sentRequests.single()
        assertThat(request.header("X-Android-Package"), equalTo(application.packageName))
        // SHA-1 of "abc"
        assertThat(request.header("X-Android-Cert"), equalTo("A9993E364706816ABA3E25717850C26C9CD0D89D"))
    }

    @Test
    fun `does not add Android app headers to other requests`() {
        client
            .newCall(Request.Builder().url("https://tile.openstreetmap.org/1/1/1.png").build())
            .execute()

        val request = sentRequests.single()
        assertThat(request.header("X-Android-Package"), nullValue())
        assertThat(request.header("X-Android-Cert"), nullValue())
    }
}
