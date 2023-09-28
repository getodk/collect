package org.odk.collect.android.mainmenu

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.odk.collect.android.application.initialization.AnalyticsInitializer
import org.odk.collect.android.formmanagement.InstancesAppState
import org.odk.collect.android.instancemanagement.autosend.AutoSendSettingsProvider
import org.odk.collect.android.projects.CurrentProjectProvider
import org.odk.collect.android.utilities.FormsRepositoryProvider
import org.odk.collect.android.utilities.InstancesRepositoryProvider
import org.odk.collect.android.version.VersionInformation
import org.odk.collect.async.Scheduler
import org.odk.collect.permissions.PermissionsChecker
import org.odk.collect.settings.SettingsProvider

open class MainMenuViewModelFactory(
    private val versionInformation: VersionInformation,
    private val application: Application,
    private val settingsProvider: SettingsProvider,
    private val instancesAppState: InstancesAppState,
    private val scheduler: Scheduler,
    private val currentProjectProvider: CurrentProjectProvider,
    private val analyticsInitializer: AnalyticsInitializer,
    private val permissionChecker: PermissionsChecker,
    private val formsRepositoryProvider: FormsRepositoryProvider,
    private val instancesRepositoryProvider: InstancesRepositoryProvider,
    private val autoSendSettingsProvider: AutoSendSettingsProvider
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when (modelClass) {
            MainMenuViewModel::class.java -> MainMenuViewModel(
                application,
                versionInformation,
                settingsProvider,
                instancesAppState,
                scheduler,
                formsRepositoryProvider,
                instancesRepositoryProvider,
                autoSendSettingsProvider
            )

            CurrentProjectViewModel::class.java -> CurrentProjectViewModel(
                currentProjectProvider
            )

            RequestPermissionsViewModel::class.java -> RequestPermissionsViewModel(
                settingsProvider,
                permissionChecker
            )

            else -> throw IllegalArgumentException()
        } as T
    }
}
