package org.odk.collect.android.utilities

import android.os.Build
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import org.odk.collect.android.BuildConfig

object CollectStrictMode {

    @JvmStatic
    fun enable() {
        if (BuildConfig.DEBUG) {
            val policyBuilder = ThreadPolicy.Builder()
                .detectAll()
                .permitDiskReads() // shared preferences are being read on main thread (`GetAndSubmitFormTest`)
                .permitDiskWrites() // files are being created on the fly (`GetAndSubmitFormTest`)
                .penaltyDeath()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                policyBuilder.permitUnbufferedIo() // `ObjectInputStream#readObject` calls
            }

            StrictMode.setThreadPolicy(policyBuilder.build())
        }
    }

    @JvmStatic
    fun disable() {
        StrictMode.setThreadPolicy(ThreadPolicy.Builder().permitCustomSlowCalls().build())
    }
}
