package org.odk.collect.geo

import android.app.Application
import org.odk.collect.strings.localization.LocalizedApplication
import java.util.Locale

class RobolectricApplication : Application(), LocalizedApplication {

    override val locale: Locale
        get() = this.resources.configuration.locales[0]
}
