package org.odk.collect.android.injection.config;

import android.app.Application;

import org.javarosa.core.reference.ReferenceManager;
import org.odk.collect.android.activities.CollectAbstractActivity;
import org.odk.collect.android.activities.DeleteSavedFormActivity;
import org.odk.collect.android.activities.FillBlankFormActivity;
import org.odk.collect.android.activities.FormDownloadListActivity;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.activities.FormHierarchyActivity;
import org.odk.collect.android.activities.FormMapActivity;
import org.odk.collect.android.activities.GeoCompoundActivity;
import org.odk.collect.android.activities.GeoPointMapActivity;
import org.odk.collect.android.activities.GeoPolyActivity;
import org.odk.collect.android.activities.InstanceUploaderActivity;
import org.odk.collect.android.activities.InstanceUploaderListActivity;
import org.odk.collect.android.activities.MainMenuActivity;
import org.odk.collect.android.activities.SmapMain;    // smap
import org.odk.collect.android.activities.SplashScreenActivity;
import org.odk.collect.android.adapters.InstanceUploaderAdapter;
import org.odk.collect.android.analytics.Analytics;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.application.initialization.ApplicationInitializer;
import org.odk.collect.android.audio.AudioRecordingControllerFragment;
import org.odk.collect.android.audio.AudioRecordingErrorDialogFragment;
import org.odk.collect.android.backgroundwork.AutoSendTaskSpec;
import org.odk.collect.android.backgroundwork.AutoUpdateTaskSpec;
import org.odk.collect.android.backgroundwork.SyncFormsTaskSpec;
import org.odk.collect.android.configure.SettingsImporter;
import org.odk.collect.android.configure.qr.QRCodeScannerFragment;
import org.odk.collect.android.configure.qr.QRCodeTabsActivity;
import org.odk.collect.android.configure.qr.ShowQRCodeFragment;
import org.odk.collect.android.formentry.BackgroundAudioPermissionDialogFragment;
import org.odk.collect.android.formentry.ODKView;
import org.odk.collect.android.formentry.QuitFormDialogFragment;
import org.odk.collect.android.formentry.saving.SaveAnswerFileErrorDialogFragment;
import org.odk.collect.android.formentry.saving.SaveFormProgressDialogFragment;
import org.odk.collect.android.fragments.MapBoxInitializationFragment;
import org.odk.collect.android.fragments.BarCodeScannerFragment;
import org.odk.collect.android.fragments.BlankFormListFragment;
import org.odk.collect.android.fragments.SavedFormListFragment;
import org.odk.collect.android.fragments.SmapFormListFragment;
import org.odk.collect.android.fragments.SmapTaskMapFragment;
import org.odk.collect.android.fragments.dialogs.SelectMinimalDialog;
import org.odk.collect.android.gdrive.GoogleDriveActivity;
import org.odk.collect.android.gdrive.GoogleSheetsUploaderActivity;
import org.odk.collect.android.geo.GoogleMapFragment;
import org.odk.collect.android.geo.MapboxMapFragment;
import org.odk.collect.android.geo.OsmDroidMapFragment;
import org.odk.collect.android.logic.PropertyManager;
import org.odk.collect.android.openrosa.OpenRosaHttpInterface;
import org.odk.collect.android.preferences.AdminPasswordDialogFragment;
import org.odk.collect.android.preferences.AdminPreferencesFragment;
import org.odk.collect.android.preferences.AdminSharedPreferences;
import org.odk.collect.android.preferences.BasePreferenceFragment;
import org.odk.collect.android.preferences.ExperimentalPreferencesFragment;
import org.odk.collect.android.preferences.FormManagementPreferences;
import org.odk.collect.android.preferences.FormMetadataFragment;
import org.odk.collect.android.preferences.GeneralPreferencesFragment;
import org.odk.collect.android.preferences.GeneralSharedPreferences;
import org.odk.collect.android.preferences.IdentityPreferences;
import org.odk.collect.android.preferences.PreferencesActivity;
import org.odk.collect.android.preferences.PreferencesProvider;
import org.odk.collect.android.preferences.ServerAuthDialogFragment;
import org.odk.collect.android.preferences.ServerPreferencesFragment;
import org.odk.collect.android.provider.TraceProvider;
import org.odk.collect.android.receivers.LocationReceiver;
import org.odk.collect.android.services.LocationService;
import org.odk.collect.android.services.NotificationService;
import org.odk.collect.android.tasks.DownloadTasksTask;                 // smap
import org.odk.collect.android.tasks.SmapChangeOrganisationTask;        // smap
import org.odk.collect.android.smap.tasks.SubmitLocationTask;        // smap
import org.odk.collect.android.tasks.SmapLoginTask;                     // smap
import org.odk.collect.android.tasks.SmapRemoteWebServicePostTask;      // smap
import org.odk.collect.android.tasks.SmapRemoteWebServiceTask;          // smap
import org.odk.collect.android.preferences.UserInterfacePreferencesFragment;
import org.odk.collect.android.provider.FormsProvider;
import org.odk.collect.android.provider.InstanceProvider;
import org.odk.collect.android.storage.StorageInitializer;
import org.odk.collect.android.storage.migration.StorageMigrationDialog;
import org.odk.collect.android.storage.migration.StorageMigrationService;
import org.odk.collect.android.tasks.InstanceServerUploaderTask;
import org.odk.collect.android.utilities.ApplicationResetter;
import org.odk.collect.android.utilities.AuthDialogUtility;
import org.odk.collect.android.utilities.Utilities;     // smap
import org.odk.collect.android.widgets.ExStringWidget;
import org.odk.collect.android.widgets.QuestionWidget;
import org.odk.collect.android.widgets.SmapFormWidget;

import javax.inject.Singleton;

import dagger.BindsInstance;
import dagger.Component;

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

    void inject(InstanceUploaderAdapter instanceUploaderAdapter);

    void inject(SavedFormListFragment savedFormListFragment);

    void inject(PropertyManager propertyManager);

    void inject(FormEntryActivity formEntryActivity);

    void inject(InstanceServerUploaderTask uploader);

    void inject(ServerPreferencesFragment serverPreferencesFragment);

    void inject(AuthDialogUtility authDialogUtility);

    void inject(FormDownloadListActivity formDownloadListActivity);

    void inject(SmapMain smapMain);                                             // smap

    void inject(DownloadTasksTask downloadTasksTask);                           // smap

    void inject(Utilities utilities);                                           // smap

    void inject(SmapRemoteWebServiceTask smapRemoteWebServiceTask);             // smap

    void inject(SmapRemoteWebServicePostTask smapRemoteWebServicePostTask);     // smap

    void inject(SmapLoginTask smapLoginTask);                                   // smap

    void inject(SmapChangeOrganisationTask smapChangeOrganisationTask);         // smap

    void inject(SubmitLocationTask submitLocationTask);                         // smap

    void inject(SmapTaskMapFragment smapTaskMapFragment);                       // smap

    void inject(TraceProvider traceProvider);                                   // smap

    void inject(NotificationService notificationService);                       // smap

    void inject(LocationReceiver locationReceiver);                             // smap

    void inject(SmapFormListFragment smapFormListFragment);                     // smap

    void inject(SmapFormWidget smapFormWidget);                                 // smap

    void inject(InstanceUploaderListActivity activity);

    void inject(GoogleDriveActivity googleDriveActivity);

    void inject(GoogleSheetsUploaderActivity googleSheetsUploaderActivity);

    void inject(QuestionWidget questionWidget);

    void inject(ExStringWidget exStringWidget);

    void inject(ODKView odkView);

    void inject(FormMetadataFragment formMetadataFragment);

    void inject(GeoPointMapActivity geoMapActivity);

    void inject(GeoPolyActivity geoPolyActivity);

    void inject(GeoCompoundActivity geoCompoundActivity);

    void inject(FormMapActivity formMapActivity);

    void inject(OsmDroidMapFragment mapFragment);

    void inject(GoogleMapFragment mapFragment);

    void inject(MapboxMapFragment mapFragment);

    void inject(MainMenuActivity mainMenuActivity);

    void inject(QRCodeTabsActivity qrCodeTabsActivity);

    void inject(ShowQRCodeFragment showQRCodeFragment);

    void inject(StorageInitializer storageInitializer);

    void inject(StorageMigrationService storageMigrationService);

    void inject(AutoSendTaskSpec autoSendTaskSpec);

    void inject(StorageMigrationDialog storageMigrationDialog);

    void inject(AdminPasswordDialogFragment adminPasswordDialogFragment);

    void inject(SplashScreenActivity splashScreenActivity);

    void inject(FormHierarchyActivity formHierarchyActivity);

    void inject(FormManagementPreferences formManagementPreferences);

    void inject(IdentityPreferences identityPreferences);

    void inject(UserInterfacePreferencesFragment userInterfacePreferencesFragment);

    void inject(SaveFormProgressDialogFragment saveFormProgressDialogFragment);

    void inject(QuitFormDialogFragment quitFormDialogFragment);

    void inject(BarCodeScannerFragment barCodeScannerFragment);

    void inject(QRCodeScannerFragment qrCodeScannerFragment);

    void inject(PreferencesActivity preferencesActivity);

    void inject(ApplicationResetter applicationResetter);

    void inject(FillBlankFormActivity fillBlankFormActivity);

    void inject(MapBoxInitializationFragment mapBoxInitializationFragment);

    void inject(SyncFormsTaskSpec syncWork);

    void inject(ExperimentalPreferencesFragment experimentalPreferencesFragment);

    void inject(AutoUpdateTaskSpec autoUpdateTaskSpec);

    void inject(ServerAuthDialogFragment serverAuthDialogFragment);

    void inject(BasePreferenceFragment basePreferenceFragment);

    void inject(BlankFormListFragment blankFormListFragment);

    void inject(InstanceUploaderActivity instanceUploaderActivity);

    void inject(GeneralPreferencesFragment generalPreferencesFragment);

    void inject(DeleteSavedFormActivity deleteSavedFormActivity);

    void inject(AdminPreferencesFragment.MainMenuAccessPreferences mainMenuAccessPreferences);

    void inject(SelectMinimalDialog selectMinimalDialog);

    void inject(AudioRecordingControllerFragment audioRecordingControllerFragment);

    void inject(SaveAnswerFileErrorDialogFragment saveAnswerFileErrorDialogFragment);

    void inject(AudioRecordingErrorDialogFragment audioRecordingErrorDialogFragment);

    void inject(CollectAbstractActivity collectAbstractActivity);

    void inject(FormsProvider formsProvider);

    void inject(InstanceProvider instanceProvider);

    void inject(BackgroundAudioPermissionDialogFragment backgroundAudioPermissionDialogFragment);

    OpenRosaHttpInterface openRosaHttpInterface();

    ReferenceManager referenceManager();

    Analytics analytics();

    GeneralSharedPreferences generalSharedPreferences();

    AdminSharedPreferences adminSharedPreferences();

    PreferencesProvider preferencesProvider();

    ApplicationInitializer applicationInitializer();

    SettingsImporter settingsImporter();
}
