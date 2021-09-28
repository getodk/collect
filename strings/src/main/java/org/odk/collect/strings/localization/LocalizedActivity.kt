package org.odk.collect.strings.localization

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import androidx.appcompat.app.AppCompatActivity

open class LocalizedActivity : AppCompatActivity() {

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        applyOverrideConfiguration(Configuration())
    }

    override fun applyOverrideConfiguration(newConfig: Configuration) {
        super.applyOverrideConfiguration(updateConfigurationIfSupported(newConfig))
    }

    private fun updateConfigurationIfSupported(config: Configuration): Configuration? {
        if (Build.VERSION.SDK_INT >= 24) {
            if (!config.locales.isEmpty) {
                return config
            }
        } else {
            if (config.locale != null) {
                return config
            }
        }

        val locale = (applicationContext as LocalizedApplication).locale
        config.setLocale(locale)
        config.setLayoutDirection(locale)
        return config
    }
}
