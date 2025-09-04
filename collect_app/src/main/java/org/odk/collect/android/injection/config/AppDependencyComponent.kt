package org.odk.collect.android.injection.config

import android.app.Application
import dagger.BindsInstance
import dagger.Component
import org.javarosa.core.reference.ReferenceManager
import org.odk.collect.android.activities.AboutActivity
import org.odk.collect.android.activities.AppListActivity
import org.odk.collect.android.activities.DeleteFormsActivity
import org.odk.collect.android.activities.FirstLaunchActivity
import org.odk.collect.android.activities.FormDownloadListActivity
import org.odk.collect.android.activities.FormFillingActivity
import org.odk.collect.android.activities.FormMapActivity
import org.odk.collect.android.activities.InstanceChooserList
import org.odk.collect.android.application.Collect
import org.odk.collect.android.application.initialization.ApplicationInitializer
import org.odk.collect.android.application.initialization.ExistingProjectMigrator
import org.odk.collect.android.audio.AudioRecordingControllerFragment
import org.odk.collect.android.audio.AudioRecordingErrorDialogFragment
import org.odk.collect.android.backgroundwork.AutoUpdateTaskSpec
import org.odk.collect.android.backgroundwork.SendFormsTaskSpec
import org.odk.collect.android.backgroundwork.SyncFormsTaskSpec
import org.odk.collect.android.configure.qr.QRCodeScannerFragment
import org.odk.collect.android.configure.qr.QRCodeTabsActivity
import org.odk.collect.android.configure.qr.ShowQRCodeFragment
import org.odk.collect.android.entities.EntitiesRepositoryProvider
import org.odk.collect.android.external.AndroidShortcutsActivity
import org.odk.collect.android.external.FormUriActivity
import org.odk.collect.android.external.FormsProvider
import org.odk.collect.android.external.InstanceProvider
import org.odk.collect.android.formentry.BackgroundAudioPermissionDialogFragment
import org.odk.collect.android.formentry.ODKView
import org.odk.collect.android.formentry.repeats.DeleteRepeatDialogFragment
import org.odk.collect.android.formentry.saving.SaveAnswerFileErrorDialogFragment
import org.odk.collect.android.formentry.saving.SaveFormProgressDialogFragment
import org.odk.collect.android.formhierarchy.FormHierarchyFragmentHostActivity
import org.odk.collect.android.formlists.blankformlist.BlankFormListActivity
import org.odk.collect.android.formmanagement.FormsDataService
import org.odk.collect.android.formmanagement.OpenRosaClientProvider
import org.odk.collect.android.fragments.BarCodeScannerFragment
import org.odk.collect.android.fragments.dialogs.FormsDownloadResultDialog
import org.odk.collect.android.fragments.dialogs.SelectMinimalDialog
import org.odk.collect.android.instancemanagement.InstancesDataService
import org.odk.collect.android.instancemanagement.send.InstanceUploaderActivity
import org.odk.collect.android.instancemanagement.send.InstanceUploaderListActivity
import org.odk.collect.android.mainmenu.MainMenuActivity
import org.odk.collect.android.preferences.dialogs.AdminPasswordDialogFragment
import org.odk.collect.android.preferences.dialogs.ChangeAdminPasswordDialog
import org.odk.collect.android.preferences.dialogs.ResetDialogPreferenceFragmentCompat
import org.odk.collect.android.preferences.dialogs.ServerAuthDialogFragment
import org.odk.collect.android.preferences.screens.BasePreferencesFragment
import org.odk.collect.android.preferences.screens.BaseProjectPreferencesFragment
import org.odk.collect.android.preferences.screens.ExperimentalPreferencesFragment
import org.odk.collect.android.preferences.screens.FormManagementPreferencesFragment
import org.odk.collect.android.preferences.screens.FormMetadataPreferencesFragment
import org.odk.collect.android.preferences.screens.IdentityPreferencesFragment
import org.odk.collect.android.preferences.screens.MapsPreferencesFragment
import org.odk.collect.android.preferences.screens.ProjectDisplayPreferencesFragment
import org.odk.collect.android.preferences.screens.ProjectPreferencesActivity
import org.odk.collect.android.preferences.screens.ProjectPreferencesFragment
import org.odk.collect.android.preferences.screens.ServerPreferencesFragment
import org.odk.collect.android.preferences.screens.UserInterfacePreferencesFragment
import org.odk.collect.android.projects.ManualProjectCreatorDialog
import org.odk.collect.android.projects.ProjectResetter
import org.odk.collect.android.projects.ProjectSettingsDialog
import org.odk.collect.android.projects.ProjectsDataService
import org.odk.collect.android.projects.QrCodeProjectCreatorDialog
import org.odk.collect.android.storage.StoragePathProvider
import org.odk.collect.android.tasks.DownloadFormListTask
import org.odk.collect.android.tasks.InstanceUploaderTask
import org.odk.collect.android.tasks.MediaLoadingTask
import org.odk.collect.android.utilities.AuthDialogUtility
import org.odk.collect.android.utilities.FormsRepositoryProvider
import org.odk.collect.android.utilities.InstancesRepositoryProvider
import org.odk.collect.android.utilities.SavepointsRepositoryProvider
import org.odk.collect.android.utilities.ThemeUtils
import org.odk.collect.android.widgets.QuestionWidget
import org.odk.collect.android.widgets.items.SelectOneFromMapDialogFragment
import org.odk.collect.async.Scheduler
import org.odk.collect.async.network.NetworkStateProvider
import org.odk.collect.draw.DrawActivity
import org.odk.collect.googlemaps.GoogleMapFragment
import org.odk.collect.location.LocationClient
import org.odk.collect.maps.MapFragmentFactory
import org.odk.collect.maps.layers.ReferenceLayerRepository
import org.odk.collect.permissions.PermissionsChecker
import org.odk.collect.permissions.PermissionsProvider
import org.odk.collect.projects.ProjectCreator
import org.odk.collect.projects.ProjectsRepository
import org.odk.collect.settings.ODKAppSettingsImporter
import org.odk.collect.settings.SettingsProvider
import org.odk.collect.webpage.ExternalWebPageHelper
import javax.inject.Singleton

/**
 * Dagger component for the application. Should include
 * application level Dagger Modules and be built with Application
 * object.
 *
 * Add an `inject(MyClass myClass)` method here for objects you want
 * to inject into so Dagger knows to wire it up.
 *
 * Annotated with @Singleton so modules can include @Singletons that will
 * be retained at an application level (as this an instance of this components
 * is owned by the Application object).
 *
 * If you need to call a provider directly from the component (in a test
 * for example) you can add a method with the type you are looking to fetch
 * (`MyType myType()`) to this interface.
 *
 * To read more about Dagger visit: https://google.github.io/dagger/users-guide
 */
@Singleton
@Component(modules = [AppDependencyModule::class])
interface AppDependencyComponent {
    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(application: Application): Builder

        fun appDependencyModule(testDependencyModule: AppDependencyModule): Builder

        fun build(): AppDependencyComponent
    }

    fun inject(collect: Collect)

    fun inject(aboutActivity: AboutActivity)

    fun inject(formFillingActivity: FormFillingActivity)

    fun inject(uploader: InstanceUploaderTask)

    fun inject(serverPreferencesFragment: ServerPreferencesFragment)

    fun inject(projectDisplayPreferencesFragment: ProjectDisplayPreferencesFragment)

    fun inject(authDialogUtility: AuthDialogUtility)

    fun inject(formDownloadListActivity: FormDownloadListActivity)

    fun inject(activity: InstanceUploaderListActivity)

    @Deprecated("should use {@link QuestionWidget.Dependencies} instead")
    fun inject(questionWidget: QuestionWidget)

    fun inject(odkView: ODKView)

    fun inject(formMetadataPreferencesFragment: FormMetadataPreferencesFragment)

    fun inject(formMapActivity: FormMapActivity)

    fun inject(mapFragment: GoogleMapFragment)

    fun inject(mainMenuActivity: MainMenuActivity)

    fun inject(qrCodeTabsActivity: QRCodeTabsActivity)

    fun inject(showQRCodeFragment: ShowQRCodeFragment)

    fun inject(sendFormsTaskSpec: SendFormsTaskSpec)

    fun inject(adminPasswordDialogFragment: AdminPasswordDialogFragment)

    fun inject(formManagementPreferencesFragment: FormManagementPreferencesFragment)

    fun inject(identityPreferencesFragment: IdentityPreferencesFragment)

    fun inject(userInterfacePreferencesFragment: UserInterfacePreferencesFragment)

    fun inject(saveFormProgressDialogFragment: SaveFormProgressDialogFragment)

    fun inject(barCodeScannerFragment: BarCodeScannerFragment)

    fun inject(qrCodeScannerFragment: QRCodeScannerFragment)

    fun inject(projectPreferencesActivity: ProjectPreferencesActivity)

    fun inject(resetDialogPreferenceFragmentCompat: ResetDialogPreferenceFragmentCompat)

    fun inject(syncWork: SyncFormsTaskSpec)

    fun inject(experimentalPreferencesFragment: ExperimentalPreferencesFragment)

    fun inject(autoUpdateTaskSpec: AutoUpdateTaskSpec)

    fun inject(serverAuthDialogFragment: ServerAuthDialogFragment)

    fun inject(basePreferencesFragment: BasePreferencesFragment)

    fun inject(instanceUploaderActivity: InstanceUploaderActivity)

    fun inject(projectPreferencesFragment: ProjectPreferencesFragment)

    fun inject(deleteFormsActivity: DeleteFormsActivity)

    fun inject(selectMinimalDialog: SelectMinimalDialog)

    fun inject(audioRecordingControllerFragment: AudioRecordingControllerFragment)

    fun inject(saveAnswerFileErrorDialogFragment: SaveAnswerFileErrorDialogFragment)

    fun inject(audioRecordingErrorDialogFragment: AudioRecordingErrorDialogFragment)

    fun inject(instanceChooserList: InstanceChooserList)

    fun inject(formsProvider: FormsProvider)

    fun inject(instanceProvider: InstanceProvider)

    fun inject(backgroundAudioPermissionDialogFragment: BackgroundAudioPermissionDialogFragment)

    fun inject(changeAdminPasswordDialog: ChangeAdminPasswordDialog)

    fun inject(mediaLoadingTask: MediaLoadingTask)

    fun inject(themeUtils: ThemeUtils)

    fun inject(baseProjectPreferencesFragment: BaseProjectPreferencesFragment)

    fun inject(androidShortcutsActivity: AndroidShortcutsActivity)

    fun inject(projectSettingsDialog: ProjectSettingsDialog)

    fun inject(manualProjectCreatorDialog: ManualProjectCreatorDialog)

    fun inject(qrCodeProjectCreatorDialog: QrCodeProjectCreatorDialog)

    fun inject(firstLaunchActivity: FirstLaunchActivity)

    fun inject(formUriActivity: FormUriActivity)

    fun inject(mapsPreferencesFragment: MapsPreferencesFragment)

    fun inject(formsDownloadResultDialog: FormsDownloadResultDialog)

    fun inject(selectOneFromMapDialogFragment: SelectOneFromMapDialogFragment)

    fun inject(drawActivity: DrawActivity)

    fun inject(blankFormListActivity: BlankFormListActivity)

    fun inject(deleteRepeatDialogFragment: DeleteRepeatDialogFragment)

    fun inject(appListActivity: AppListActivity)

    fun inject(downloadFormListTask: DownloadFormListTask)

    fun inject(formHierarchyFragmentHostActivity: FormHierarchyFragmentHostActivity)

    fun referenceManager(): ReferenceManager

    fun settingsProvider(): SettingsProvider

    fun applicationInitializer(): ApplicationInitializer

    fun settingsImporter(): ODKAppSettingsImporter

    fun projectsRepository(): ProjectsRepository

    fun projectCreator(): ProjectCreator

    fun currentProjectProvider(): ProjectsDataService

    fun storagePathProvider(): StoragePathProvider

    fun formsRepositoryProvider(): FormsRepositoryProvider

    fun instancesRepositoryProvider(): InstancesRepositoryProvider

    fun savepointsRepositoryProvider(): SavepointsRepositoryProvider

    fun formSourceProvider(): OpenRosaClientProvider

    fun existingProjectMigrator(): ExistingProjectMigrator

    fun projectResetter(): ProjectResetter

    fun mapFragmentFactory(): MapFragmentFactory

    fun scheduler(): Scheduler

    fun locationClient(): LocationClient

    fun permissionsProvider(): PermissionsProvider

    fun permissionsChecker(): PermissionsChecker

    fun referenceLayerRepository(): ReferenceLayerRepository

    fun networkStateProvider(): NetworkStateProvider

    fun entitiesRepositoryProvider(): EntitiesRepositoryProvider

    fun formsDataService(): FormsDataService

    fun instancesDataService(): InstancesDataService

    fun projectDependencyModuleFactory(): ProjectDependencyModuleFactory

    fun externalWebPageHelper(): ExternalWebPageHelper
}
