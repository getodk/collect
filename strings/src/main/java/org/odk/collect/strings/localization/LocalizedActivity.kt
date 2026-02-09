package org.odk.collect.strings.localization

import android.content.Context
import android.content.res.Configuration
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
        if (!config.locales.isEmpty) {
            return config
        }

        val locale = when (applicationContext) {
            is LocalizedApplication -> (applicationContext as LocalizedApplication).locale
            else -> config.locales[0]
        }

        config.setLocale(locale)
        config.setLayoutDirection(locale)
        return config
    }
}
