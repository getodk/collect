package org.odk.collect.android.injection.config;

import android.app.Application;

import org.javarosa.core.reference.ReferenceManager;
import org.odk.collect.android.activities.AboutActivity;
import org.odk.collect.android.activities.AppListActivity;
import org.odk.collect.android.activities.DeleteSavedFormActivity;
import org.odk.collect.android.activities.FirstLaunchActivity;
import org.odk.collect.android.activities.FormDownloadListActivity;
import org.odk.collect.android.activities.FormFillingActivity;
import org.odk.collect.android.activities.FormHierarchyActivity;
import org.odk.collect.android.activities.FormMapActivity;
import org.odk.collect.android.activities.InstanceChooserList;
import org.odk.collect.android.activities.InstanceUploaderActivity;
import org.odk.collect.android.activities.InstanceUploaderListActivity;
import org.odk.collect.android.adapters.InstanceUploaderAdapter;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.application.initialization.ApplicationInitializer;
import org.odk.collect.android.application.initialization.ExistingProjectMigrator;
import org.odk.collect.android.audio.AudioRecordingControllerFragment;
import org.odk.collect.android.audio.AudioRecordingErrorDialogFragment;
import org.odk.collect.android.backgroundwork.AutoSendTaskSpec;
import org.odk.collect.android.backgroundwork.AutoUpdateTaskSpec;
import org.odk.collect.android.backgroundwork.SyncFormsTaskSpec;
import org.odk.collect.android.configure.qr.QRCodeScannerFragment;
import org.odk.collect.android.configure.qr.QRCodeTabsActivity;
import org.odk.collect.android.configure.qr.ShowQRCodeFragment;
import org.odk.collect.android.draw.DrawActivity;
import org.odk.collect.android.draw.PenColorPickerDialog;
import org.odk.collect.android.entities.EntitiesRepositoryProvider;
import org.odk.collect.android.external.AndroidShortcutsActivity;
import org.odk.collect.android.external.FormUriActivity;
import org.odk.collect.android.external.FormsProvider;
import org.odk.collect.android.external.InstanceProvider;
import org.odk.collect.android.formentry.BackgroundAudioPermissionDialogFragment;
import org.odk.collect.android.formentry.ODKView;
import org.odk.collect.android.formentry.repeats.DeleteRepeatDialogFragment;
import org.odk.collect.android.formentry.saving.SaveAnswerFileErrorDialogFragment;
import org.odk.collect.android.formentry.saving.SaveFormProgressDialogFragment;
import org.odk.collect.android.formlists.blankformlist.BlankFormListActivity;
import org.odk.collect.android.formmanagement.FormSourceProvider;
import org.odk.collect.android.formmanagement.FormsDataService;
import org.odk.collect.android.fragments.AppListFragment;
import org.odk.collect.android.fragments.BarCodeScannerFragment;
import org.odk.collect.android.fragments.SavedFormListFragment;
import org.odk.collect.android.fragments.dialogs.FormsDownloadResultDialog;
import org.odk.collect.android.fragments.dialogs.SelectMinimalDialog;
import org.odk.collect.android.gdrive.GoogleDriveActivity;
import org.odk.collect.android.gdrive.GoogleSheetsUploaderActivity;
import org.odk.collect.android.geo.GoogleMapFragment;
import org.odk.collect.android.mainmenu.MainMenuActivity;
import org.odk.collect.android.openrosa.OpenRosaHttpInterface;
import org.odk.collect.android.preferences.CaptionedListPreference;
import org.odk.collect.android.preferences.dialogs.AdminPasswordDialogFragment;
import org.odk.collect.android.preferences.dialogs.ChangeAdminPasswordDialog;
import org.odk.collect.android.preferences.dialogs.ResetDialogPreferenceFragmentCompat;
import org.odk.collect.android.preferences.dialogs.ServerAuthDialogFragment;
import org.odk.collect.android.preferences.screens.BaseAdminPreferencesFragment;
import org.odk.collect.android.preferences.screens.BasePreferencesFragment;
import org.odk.collect.android.preferences.screens.BaseProjectPreferencesFragment;
import org.odk.collect.android.preferences.screens.ExperimentalPreferencesFragment;
import org.odk.collect.android.preferences.screens.FormManagementPreferencesFragment;
import org.odk.collect.android.preferences.screens.FormMetadataPreferencesFragment;
import org.odk.collect.android.preferences.screens.IdentityPreferencesFragment;
import org.odk.collect.android.preferences.screens.MapsPreferencesFragment;
import org.odk.collect.android.preferences.screens.ProjectDisplayPreferencesFragment;
import org.odk.collect.android.preferences.screens.ProjectManagementPreferencesFragment;
import org.odk.collect.android.preferences.screens.ProjectPreferencesActivity;
import org.odk.collect.android.preferences.screens.ProjectPreferencesFragment;
import org.odk.collect.android.preferences.screens.ServerPreferencesFragment;
import org.odk.collect.android.preferences.screens.UserInterfacePreferencesFragment;
import org.odk.collect.android.projects.CurrentProjectProvider;
import org.odk.collect.android.projects.ManualProjectCreatorDialog;
import org.odk.collect.android.projects.ProjectSettingsDialog;
import org.odk.collect.android.projects.QrCodeProjectCreatorDialog;
import org.odk.collect.android.storage.StoragePathProvider;
import org.odk.collect.android.tasks.DownloadFormListTask;
import org.odk.collect.android.tasks.InstanceServerUploaderTask;
import org.odk.collect.android.tasks.MediaLoadingTask;
import org.odk.collect.android.upload.InstanceUploader;
import org.odk.collect.android.utilities.AuthDialogUtility;
import org.odk.collect.android.utilities.FormsRepositoryProvider;
import org.odk.collect.android.utilities.InstancesRepositoryProvider;
import org.odk.collect.android.utilities.ProjectResetter;
import org.odk.collect.android.utilities.ThemeUtils;
import org.odk.collect.android.widgets.ExStringWidget;
import org.odk.collect.android.widgets.QuestionWidget;
import org.odk.collect.android.widgets.items.SelectOneFromMapDialogFragment;
import org.odk.collect.androidshared.network.NetworkStateProvider;
import org.odk.collect.async.Scheduler;
import org.odk.collect.location.LocationClient;
import org.odk.collect.maps.MapFragmentFactory;
import org.odk.collect.maps.layers.ReferenceLayerRepository;
import org.odk.collect.permissions.PermissionsChecker;
import org.odk.collect.permissions.PermissionsProvider;
import org.odk.collect.projects.ProjectsRepository;
import org.odk.collect.settings.ODKAppSettingsImporter;
import org.odk.collect.settings.SettingsProvider;

import javax.inject.Singleton;

import dagger.BindsInstance;
import dagger.Component;

/**
 * Dagger component for the application. Should include
 * application level Dagger Modules and be built with Application
 * object.
 * <p>
 * Add an `inject(MyClass myClass)` method here for objects you want
 * to inject into so Dagger knows to wire it up.
 * <p>
 * Annotated with @Singleton so modules can include @Singletons that will
 * be retained at an application level (as this an instance of this components
 * is owned by the Application object).
 * <p>
 * If you need to call a provider directly from the component (in a test
 * for example) you can add a method with the type you are looking to fetch
 * (`MyType myType()`) to this interface.
 * <p>
 * To read more about Dagger visit: https://google.github.io/dagger/users-guide
 **/

@Singleton
@Component(modules = {
        AppDependencyModule.class
})
public interface AppDependencyComponent {

    @Component.Builder
    interface Builder {

        @BindsInstance
        Builder application(Application application);

        Builder appDependencyModule(AppDependencyModule testDependencyModule);

        AppDependencyComponent build();
    }

    void inject(Collect collect);

    void inject(AboutActivity aboutActivity);

    void inject(InstanceUploaderAdapter instanceUploaderAdapter);

    void inject(SavedFormListFragment savedFormListFragment);

    void inject(FormFillingActivity formFillingActivity);

    void inject(InstanceServerUploaderTask uploader);

    void inject(ServerPreferencesFragment serverPreferencesFragment);

    void inject(ProjectDisplayPreferencesFragment projectDisplayPreferencesFragment);

    void inject(ProjectManagementPreferencesFragment projectManagementPreferencesFragment);

    void inject(AuthDialogUtility authDialogUtility);

    void inject(FormDownloadListActivity formDownloadListActivity);

    void inject(InstanceUploaderListActivity activity);

    void inject(GoogleDriveActivity googleDriveActivity);

    void inject(GoogleSheetsUploaderActivity googleSheetsUploaderActivity);

    void inject(QuestionWidget questionWidget);

    void inject(ExStringWidget exStringWidget);

    void inject(ODKView odkView);

    void inject(FormMetadataPreferencesFragment formMetadataPreferencesFragment);

    void inject(FormMapActivity formMapActivity);

    void inject(GoogleMapFragment mapFragment);

    void inject(MainMenuActivity mainMenuActivity);

    void inject(QRCodeTabsActivity qrCodeTabsActivity);

    void inject(ShowQRCodeFragment showQRCodeFragment);

    void inject(AutoSendTaskSpec autoSendTaskSpec);

    void inject(AdminPasswordDialogFragment adminPasswordDialogFragment);

    void inject(FormHierarchyActivity formHierarchyActivity);

    void inject(FormManagementPreferencesFragment formManagementPreferencesFragment);

    void inject(IdentityPreferencesFragment identityPreferencesFragment);

    void inject(UserInterfacePreferencesFragment userInterfacePreferencesFragment);

    void inject(SaveFormProgressDialogFragment saveFormProgressDialogFragment);

    void inject(BarCodeScannerFragment barCodeScannerFragment);

    void inject(QRCodeScannerFragment qrCodeScannerFragment);

    void inject(ProjectPreferencesActivity projectPreferencesActivity);

    void inject(ResetDialogPreferenceFragmentCompat resetDialogPreferenceFragmentCompat);

    void inject(SyncFormsTaskSpec syncWork);

    void inject(ExperimentalPreferencesFragment experimentalPreferencesFragment);

    void inject(AutoUpdateTaskSpec autoUpdateTaskSpec);

    void inject(ServerAuthDialogFragment serverAuthDialogFragment);

    void inject(BasePreferencesFragment basePreferencesFragment);

    void inject(InstanceUploaderActivity instanceUploaderActivity);

    void inject(ProjectPreferencesFragment projectPreferencesFragment);

    void inject(DeleteSavedFormActivity deleteSavedFormActivity);

    void inject(SelectMinimalDialog selectMinimalDialog);

    void inject(AudioRecordingControllerFragment audioRecordingControllerFragment);

    void inject(SaveAnswerFileErrorDialogFragment saveAnswerFileErrorDialogFragment);

    void inject(AudioRecordingErrorDialogFragment audioRecordingErrorDialogFragment);

    void inject(InstanceChooserList instanceChooserList);

    void inject(FormsProvider formsProvider);

    void inject(InstanceProvider instanceProvider);

    void inject(BackgroundAudioPermissionDialogFragment backgroundAudioPermissionDialogFragment);

    void inject(AppListFragment appListFragment);

    void inject(ChangeAdminPasswordDialog changeAdminPasswordDialog);

    void inject(MediaLoadingTask mediaLoadingTask);

    void inject(ThemeUtils themeUtils);

    void inject(BaseProjectPreferencesFragment baseProjectPreferencesFragment);

    void inject(BaseAdminPreferencesFragment baseAdminPreferencesFragment);

    void inject(CaptionedListPreference captionedListPreference);

    void inject(AndroidShortcutsActivity androidShortcutsActivity);

    void inject(ProjectSettingsDialog projectSettingsDialog);

    void inject(ManualProjectCreatorDialog manualProjectCreatorDialog);

    void inject(QrCodeProjectCreatorDialog qrCodeProjectCreatorDialog);

    void inject(FirstLaunchActivity firstLaunchActivity);

    void inject(InstanceUploader instanceUploader);

    void inject(FormUriActivity formUriActivity);

    void inject(MapsPreferencesFragment mapsPreferencesFragment);

    void inject(FormsDownloadResultDialog formsDownloadResultDialog);

    void inject(SelectOneFromMapDialogFragment selectOneFromMapDialogFragment);

    void inject(DrawActivity drawActivity);

    void inject(PenColorPickerDialog colorPickerDialog);

    void inject(BlankFormListActivity blankFormListActivity);

    void inject(DeleteRepeatDialogFragment deleteRepeatDialogFragment);

    void inject(AppListActivity appListActivity);

    void inject(DownloadFormListTask downloadFormListTask);

    OpenRosaHttpInterface openRosaHttpInterface();

    ReferenceManager referenceManager();

    SettingsProvider settingsProvider();

    ApplicationInitializer applicationInitializer();

    ODKAppSettingsImporter settingsImporter();

    ProjectsRepository projectsRepository();

    CurrentProjectProvider currentProjectProvider();

    StoragePathProvider storagePathProvider();

    FormsRepositoryProvider formsRepositoryProvider();

    InstancesRepositoryProvider instancesRepositoryProvider();

    FormSourceProvider formSourceProvider();

    ExistingProjectMigrator existingProjectMigrator();

    ProjectResetter projectResetter();

    MapFragmentFactory mapFragmentFactory();

    Scheduler scheduler();

    LocationClient locationClient();

    PermissionsProvider permissionsProvider();

    PermissionsChecker permissionsChecker();

    ReferenceLayerRepository referenceLayerRepository();

    NetworkStateProvider networkStateProvider();

    EntitiesRepositoryProvider entitiesRepositoryProvider();

    FormsDataService formsDataService();
}
