package odk.hedera.collect.injection.config;

import android.app.Application;

import org.javarosa.core.reference.ReferenceManager;
import odk.hedera.collect.activities.FormDownloadListActivity;
import odk.hedera.collect.activities.FormEntryActivity;
import odk.hedera.collect.activities.FormHierarchyActivity;
import odk.hedera.collect.activities.FormMapActivity;
import odk.hedera.collect.activities.GeoPointMapActivity;
import odk.hedera.collect.activities.GeoPolyActivity;
import odk.hedera.collect.activities.GoogleDriveActivity;
import odk.hedera.collect.activities.GoogleSheetsUploaderActivity;
import odk.hedera.collect.activities.InstanceUploaderListActivity;
import odk.hedera.collect.activities.MainMenuActivity;
import odk.hedera.collect.activities.SplashScreenActivity;
import odk.hedera.collect.adapters.InstanceUploaderAdapter;
import odk.hedera.collect.analytics.Analytics;
import odk.hedera.collect.application.Collect;
import odk.hedera.collect.events.RxEventBus;
import odk.hedera.collect.formentry.ODKView;
import odk.hedera.collect.formentry.QuitFormDialogFragment;
import odk.hedera.collect.formentry.saving.SaveFormProgressDialogFragment;
import odk.hedera.collect.fragments.DataManagerList;
import odk.hedera.collect.geo.GoogleMapFragment;
import odk.hedera.collect.geo.MapboxMapFragment;
import odk.hedera.collect.geo.OsmDroidMapFragment;
import odk.hedera.collect.logic.PropertyManager;
import odk.hedera.collect.openrosa.OpenRosaHttpInterface;
import odk.hedera.collect.preferences.AdminPasswordDialogFragment;
import odk.hedera.collect.preferences.AdminSharedPreferences;
import odk.hedera.collect.preferences.FormManagementPreferences;
import odk.hedera.collect.preferences.FormMetadataFragment;
import odk.hedera.collect.preferences.GeneralSharedPreferences;
import odk.hedera.collect.preferences.IdentityPreferences;
import odk.hedera.collect.preferences.PreferencesProvider;
import odk.hedera.collect.preferences.ServerPreferencesFragment;
import odk.hedera.collect.preferences.UserInterfacePreferencesFragment;
import odk.hedera.collect.preferences.qr.QRCodeTabsActivity;
import odk.hedera.collect.preferences.qr.ShowQRCodeFragment;
import odk.hedera.collect.storage.StorageInitializer;
import odk.hedera.collect.storage.migration.StorageMigrationDialog;
import odk.hedera.collect.storage.migration.StorageMigrationService;
import odk.hedera.collect.tasks.InstanceServerUploaderTask;
import odk.hedera.collect.tasks.ServerPollingJob;
import odk.hedera.collect.upload.AutoSendWorker;
import odk.hedera.collect.utilities.AuthDialogUtility;
import odk.hedera.collect.utilities.FormDownloader;
import odk.hedera.collect.widgets.ExStringWidget;
import odk.hedera.collect.widgets.QuestionWidget;

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

    void inject(DataManagerList dataManagerList);

    void inject(PropertyManager propertyManager);

    void inject(FormEntryActivity formEntryActivity);

    void inject(InstanceServerUploaderTask uploader);

    void inject(ServerPreferencesFragment serverPreferencesFragment);

    void inject(FormDownloader formDownloader);

    void inject(ServerPollingJob serverPollingJob);

    void inject(AuthDialogUtility authDialogUtility);

    void inject(FormDownloadListActivity formDownloadListActivity);

    void inject(InstanceUploaderListActivity activity);

    void inject(GoogleDriveActivity googleDriveActivity);

    void inject(GoogleSheetsUploaderActivity googleSheetsUploaderActivity);

    void inject(QuestionWidget questionWidget);

    void inject(ExStringWidget exStringWidget);

    void inject(ODKView odkView);

    void inject(FormMetadataFragment formMetadataFragment);

    void inject(GeoPointMapActivity geoMapActivity);

    void inject(GeoPolyActivity geoPolyActivity);

    void inject(FormMapActivity formMapActivity);

    void inject(OsmDroidMapFragment mapFragment);

    void inject(GoogleMapFragment mapFragment);

    void inject(MapboxMapFragment mapFragment);

    void inject(MainMenuActivity mainMenuActivity);

    void inject(QRCodeTabsActivity qrCodeTabsActivity);

    void inject(ShowQRCodeFragment showQRCodeFragment);

    void inject(StorageInitializer storageInitializer);

    void inject(StorageMigrationService storageMigrationService);

    void inject(AutoSendWorker autoSendWorker);

    void inject(StorageMigrationDialog storageMigrationDialog);

    void inject(AdminPasswordDialogFragment adminPasswordDialogFragment);

    void inject(SplashScreenActivity splashScreenActivity);

    void inject(FormHierarchyActivity formHierarchyActivity);

    void inject(FormManagementPreferences formManagementPreferences);

    void inject(IdentityPreferences identityPreferences);

    void inject(UserInterfacePreferencesFragment userInterfacePreferencesFragment);

    void inject(SaveFormProgressDialogFragment saveFormProgressDialogFragment);

    void inject(QuitFormDialogFragment quitFormDialogFragment);

    RxEventBus rxEventBus();

    OpenRosaHttpInterface openRosaHttpInterface();

    ReferenceManager referenceManager();

    Analytics analytics();

    GeneralSharedPreferences generalSharedPreferences();

    AdminSharedPreferences adminSharedPreferences();

    PreferencesProvider preferencesProvider();
}
