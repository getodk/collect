package org.odk.collect.maplibre

import android.content.Context
import androidx.core.content.pm.PackageInfoCompat
import okhttp3.Interceptor
import okhttp3.Response
import java.security.MessageDigest

/**
 * Adds the headers that identify the app to requests sent to [host]. Google APIs need them
 * when the API key is restricted to Android apps.
 *
 * See https://docs.cloud.google.com/docs/authentication/api-keys#android
 */
class AndroidAppHeadersInterceptor(private val context: Context, private val host: String) : Interceptor {
    private val headers by lazy {
        val signature = PackageInfoCompat.getSignatures(context.packageManager, context.packageName).last()
        val sha1 = MessageDigest.getInstance("SHA-1").digest(signature.toByteArray())

        mapOf(
            "X-Android-Package" to context.packageName,
            "X-Android-Cert" to sha1.joinToString("") { "%02X".format(it) }
        )
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        return if (request.url.host == host) {
            val builder = request.newBuilder()
            headers.forEach { (name, value) -> builder.header(name, value) }
            chain.proceed(builder.build())
        } else {
            chain.proceed(request)
        }
    }
}
