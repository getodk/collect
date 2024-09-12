package org.odk.collect.androidshared.system

import android.app.ActivityManager
import android.content.Context

/**
 * Checks if the device supports the given OpenGL ES version.
 *
 * Note: This approach may not be 100% reliable because `reqGlEsVersion` indicates
 * the highest version of OpenGL ES that the device's hardware is guaranteed to support
 * at runtime. However, it might not always reflect the actual version available.
 *
 * For a more reliable method, refer to https://developer.android.com/develop/ui/views/graphics/opengl/about-opengl#version-check.
 * This recommended approach is more complex to implement but offers better accuracy.
 */
object OpenGLVersionChecker {
    @JvmStatic
    fun isOpenGLv2Supported(context: Context): Boolean {
        return (context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager)
            .deviceConfigurationInfo.reqGlEsVersion >= 0x20000
    }

    @JvmStatic
    fun isOpenGLv3Supported(context: Context): Boolean {
        return (context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager)
            .deviceConfigurationInfo.reqGlEsVersion >= 0x30000
    }
}
