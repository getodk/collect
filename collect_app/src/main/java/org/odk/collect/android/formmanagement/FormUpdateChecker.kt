package org.odk.collect.android.formmanagement

import android.content.Context
import org.odk.collect.android.R
import org.odk.collect.android.backgroundwork.ChangeLock
import org.odk.collect.android.notifications.Notifier
import org.odk.collect.android.preferences.keys.GeneralKeys
import org.odk.collect.android.preferences.source.SettingsProvider
import org.odk.collect.android.provider.FormsProviderAPI
import org.odk.collect.android.utilities.TranslationHandler
import org.odk.collect.forms.FormSourceException
import java.util.stream.Collectors

class FormUpdateChecker(
    private val context: Context,
    private val changeLock: ChangeLock,
    private val notifier: Notifier,
    private val settingsProvider: SettingsProvider
) {

    fun checkForUpdates(
        serverFormsDetailsFetcher: ServerFormsDetailsFetcher,
        formDownloader: FormDownloader
    ): Boolean {
        return try {
            val serverForms: List<ServerFormDetails> = serverFormsDetailsFetcher.fetchFormDetails()
            val updatedForms =
                serverForms.stream().filter { obj: ServerFormDetails -> obj.isUpdated }
                    .collect(Collectors.toList())
            if (updatedForms.isNotEmpty()) {
                val generalSettings = settingsProvider.getGeneralSettings()
                if (generalSettings.getBoolean(GeneralKeys.KEY_AUTOMATIC_UPDATE)) {
                    val formUpdateDownloader = FormUpdateDownloader()
                    val results = formUpdateDownloader.downloadUpdates(
                        updatedForms,
                        changeLock,
                        formDownloader,
                        TranslationHandler.getString(context, R.string.success),
                        TranslationHandler.getString(context, R.string.failure)
                    )

                    notifier.onUpdatesDownloaded(results)
                } else {
                    notifier.onUpdatesAvailable(updatedForms)
                }
            }

            context.contentResolver.notifyChange(FormsProviderAPI.CONTENT_URI, null)
            true
        } catch (e: FormSourceException) {
            true
        }
    }
}
