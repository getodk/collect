package org.odk.collect.android.formmanagement

import android.content.Context
import org.javarosa.core.reference.ReferenceManager
import org.odk.collect.analytics.Analytics
import org.odk.collect.android.R
import org.odk.collect.android.backgroundwork.ChangeLock
import org.odk.collect.android.notifications.Notifier
import org.odk.collect.android.preferences.keys.GeneralKeys
import org.odk.collect.android.preferences.source.SettingsProvider
import org.odk.collect.android.provider.FormsProviderAPI
import org.odk.collect.android.storage.StoragePathProvider
import org.odk.collect.android.storage.StorageSubdirectory
import org.odk.collect.android.utilities.FormsDirDiskFormsSynchronizer
import org.odk.collect.android.utilities.FormsRepositoryProvider
import org.odk.collect.android.utilities.TranslationHandler
import org.odk.collect.forms.FormSource
import org.odk.collect.forms.FormSourceException
import org.odk.collect.forms.FormsRepository
import java.io.File
import java.util.stream.Collectors

class FormUpdateChecker(
    private val context: Context,
    private val notifier: Notifier,
    private val analytics: Analytics,
    private val changeLock: ChangeLock,
    private val storagePathProvider: StoragePathProvider,
    private val settingsProvider: SettingsProvider,
    private val formsRepositoryProvider: FormsRepositoryProvider,
    private val formSourceProvider: FormSourceProvider
) {

    fun checkForUpdates(): Boolean {
        val formsRepository: FormsRepository = formsRepositoryProvider.get()
        val formSource: FormSource = formSourceProvider.get()
        val diskFormsSynchronizer: DiskFormsSynchronizer =
            FormsDirDiskFormsSynchronizer(formsRepository)

        val serverFormsDetailsFetcher = ServerFormsDetailsFetcher(
            formsRepository,
            formSource,
            diskFormsSynchronizer
        )

        val formsDirPath: String = storagePathProvider.getOdkDirPath(StorageSubdirectory.FORMS)
        val cacheDirPath: String = storagePathProvider.getOdkDirPath(StorageSubdirectory.CACHE)
        val formDownloader = ServerFormDownloader(
            formSource,
            formsRepository,
            File(cacheDirPath),
            formsDirPath,
            FormMetadataParser(ReferenceManager.instance()),
            analytics
        )

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
