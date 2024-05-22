package org.odk.collect.strings.format

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.strings.R
import org.odk.collect.strings.localization.getLocalizedResources
import java.text.SimpleDateFormat
import java.util.Locale

@RunWith(AndroidJUnit4::class)
class DateFormatsTest {

    private val application = ApplicationProvider.getApplicationContext<Application>()

    @Test
    fun `all date formats parse in all available locales`() {
        formats.forEach { format ->
            application.resources.assets.locales.forEach { localeCode ->
                val locale = Locale.forLanguageTag(localeCode)
                val resources = application.getLocalizedResources(locale)
                val string = resources.getString(format)
                try {
                    SimpleDateFormat(string, locale)
                } catch (e: Throwable) {
                    throw IllegalArgumentException("Illegal format translation: $string", e)
                }
            }
        }
    }
}

private val formats = setOf(
    R.string.save_explanation_with_last_saved,
    R.string.discard_changes_warning,
    R.string.added_on_date_at_time,
    R.string.updated_on_date_at_time,
    R.string.saved_on_date_at_time,
    R.string.finalized_on_date_at_time,
    R.string.sent_on_date_at_time,
    R.string.sending_failed_on_date_at_time,
    R.string.deleted_on_date_at_time,
    R.string.modified_on_date_at_time,
    R.string.savepoint_recovery_dialog_message
)
