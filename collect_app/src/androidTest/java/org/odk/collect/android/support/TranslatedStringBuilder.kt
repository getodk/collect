package org.odk.collect.android.support

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import org.odk.collect.strings.localization.getLocalizedString

class TranslatedStringBuilder(private val separator: String = " ") {

    private val context = ApplicationProvider.getApplicationContext<Application>()
    private var string = ""

    fun addString(stringId: Int, vararg formatArgs: Any): TranslatedStringBuilder {
        string += if (string.isEmpty()) {
            context.getLocalizedString(stringId, formatArgs)
        } else {
            separator + context.getLocalizedString(stringId, formatArgs)
        }

        return this
    }

    fun build(): String {
        return string
    }
}
