package org.odk.collect.android.injection.config;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.media.MediaPlayer;
import android.telephony.TelephonyManager;
import android.webkit.MimeTypeMap;

import androidx.annotation.NonNull;
import androidx.lifecycle.AbstractSavedStateViewModelFactory;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import androidx.work.WorkManager;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.drive.DriveScopes;
import com.google.gson.Gson;

import org.javarosa.core.reference.ReferenceManager;
import org.odk.collect.analytics.Analytics;
import org.odk.collect.analytics.BlockableFirebaseAnalytics;
import org.odk.collect.analytics.NoopAnalytics;
import org.odk.collect.android.BuildConfig;
import org.odk.collect.android.R;
import org.odk.collect.android.application.CollectSettingsChangeHandler;
import org.odk.collect.android.application.initialization.ApplicationInitializer;
import org.odk.collect.android.application.initialization.CollectSettingsPreferenceMigrator;
import org.odk.collect.android.application.initialization.SettingsPreferenceMigrator;
import org.odk.collect.android.backgroundwork.ChangeLock;
import org.odk.collect.android.backgroundwork.FormSubmitManager;
import org.odk.collect.android.backgroundwork.FormUpdateManager;
import org.odk.collect.android.backgroundwork.ReentrantLockChangeLock;
import org.odk.collect.android.backgroundwork.SchedulerFormUpdateAndSubmitManager;
import org.odk.collect.android.configure.ServerRepository;
import org.odk.collect.android.configure.SettingsChangeHandler;
import org.odk.collect.android.configure.SettingsImporter;
import org.odk.collect.android.configure.SharedPreferencesServerRepository;
import org.odk.collect.android.configure.StructureAndTypeSettingsValidator;
import org.odk.collect.android.configure.qr.CachingQRCodeGenerator;
import org.odk.collect.android.configure.qr.JsonPreferencesGenerator;
import org.odk.collect.android.configure.qr.QRCodeDecoder;
import org.odk.collect.android.configure.qr.QRCodeGenerator;
import org.odk.collect.android.configure.qr.QRCodeUtils;
import org.odk.collect.android.database.forms.FormsDatabaseProvider;
import org.odk.collect.android.database.instances.InstancesDatabaseProvider;
import org.odk.collect.android.database.itemsets.DatabaseFastExternalItemsetsRepository;
import org.odk.collect.android.events.RxEventBus;
import org.odk.collect.android.formentry.BackgroundAudioViewModel;
import org.odk.collect.android.formentry.FormEntryViewModel;
import org.odk.collect.android.formentry.media.AudioHelperFactory;
import org.odk.collect.android.formentry.media.ScreenContextAudioHelperFactory;
import org.odk.collect.android.formentry.saving.DiskFormSaver;
import org.odk.collect.android.formentry.saving.FormSaveViewModel;
import org.odk.collect.android.formmanagement.DiskFormsSynchronizer;
import org.odk.collect.android.formmanagement.FormDownloader;
import org.odk.collect.android.formmanagement.FormMetadataParser;
import org.odk.collect.android.formmanagement.InstancesCountRepository;
import org.odk.collect.android.formmanagement.ServerFormDownloader;
import org.odk.collect.android.formmanagement.ServerFormsDetailsFetcher;
import org.odk.collect.android.formmanagement.matchexactly.ServerFormsSynchronizer;
import org.odk.collect.android.formmanagement.matchexactly.SyncStatusRepository;
import org.odk.collect.android.gdrive.GoogleAccountCredentialGoogleAccountPicker;
import org.odk.collect.android.gdrive.GoogleAccountPicker;
import org.odk.collect.android.gdrive.GoogleApiProvider;
import org.odk.collect.android.geo.MapProvider;
import org.odk.collect.android.itemsets.FastExternalItemsetsRepository;
import org.odk.collect.android.logic.PropertyManager;
import org.odk.collect.android.metadata.InstallIDProvider;
import org.odk.collect.android.metadata.SharedPreferencesInstallIDProvider;
import org.odk.collect.android.network.ConnectivityProvider;
import org.odk.collect.android.network.NetworkStateProvider;
import org.odk.collect.android.notifications.NotificationManagerNotifier;
import org.odk.collect.android.notifications.Notifier;
import org.odk.collect.android.openrosa.CollectThenSystemContentTypeMapper;
import org.odk.collect.android.openrosa.OpenRosaFormSource;
import org.odk.collect.android.openrosa.OpenRosaHttpInterface;
import org.odk.collect.android.openrosa.OpenRosaResponseParserImpl;
import org.odk.collect.android.openrosa.okhttp.OkHttpConnection;
import org.odk.collect.android.openrosa.okhttp.OkHttpOpenRosaServerClientProvider;
import org.odk.collect.android.permissions.PermissionsChecker;
import org.odk.collect.android.permissions.PermissionsProvider;
import org.odk.collect.android.preferences.keys.AdminKeys;
import org.odk.collect.android.preferences.keys.GeneralKeys;
import org.odk.collect.android.preferences.source.SettingsProvider;
import org.odk.collect.android.preferences.source.SettingsStore;
import org.odk.collect.android.projects.CurrentProjectProvider;
import org.odk.collect.android.projects.ProjectsRepository;
import org.odk.collect.android.projects.SharedPreferencesProjectsRepository;
import org.odk.collect.android.storage.StorageInitializer;
import org.odk.collect.android.storage.StoragePathProvider;
import org.odk.collect.android.storage.StorageSubdirectory;
import org.odk.collect.android.utilities.ActivityAvailability;
import org.odk.collect.android.utilities.AdminPasswordProvider;
import org.odk.collect.android.utilities.AndroidUserAgent;
import org.odk.collect.android.utilities.DeviceDetailsProvider;
import org.odk.collect.android.utilities.ExternalAppIntentProvider;
import org.odk.collect.android.utilities.ExternalWebPageHelper;
import org.odk.collect.android.utilities.FileProvider;
import org.odk.collect.android.utilities.FormsDirDiskFormsSynchronizer;
import org.odk.collect.android.utilities.FormsRepositoryProvider;
import org.odk.collect.android.utilities.InstancesRepositoryProvider;
import org.odk.collect.android.utilities.MediaUtils;
import org.odk.collect.android.utilities.ScreenUtils;
import org.odk.collect.android.utilities.SoftKeyboardController;
import org.odk.collect.android.utilities.UUIDGenerator;
import org.odk.collect.android.utilities.WebCredentialsUtils;
import org.odk.collect.android.version.VersionInformation;
import org.odk.collect.android.views.BarcodeViewDecoder;
import org.odk.collect.async.CoroutineAndWorkManagerScheduler;
import org.odk.collect.async.Scheduler;
import org.odk.collect.audiorecorder.recording.AudioRecorder;
import org.odk.collect.audiorecorder.recording.AudioRecorderFactory;
import org.odk.collect.forms.FormSource;
import org.odk.collect.utilities.Clock;
import org.odk.collect.utilities.UserAgentProvider;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;

import static androidx.core.content.FileProvider.getUriForFile;
import static org.odk.collect.android.preferences.keys.MetaKeys.KEY_INSTALL_ID;

/**
 * Add dependency providers here (annotated with @Provides)
 * for objects you need to inject
 */
@Module
@SuppressWarnings("PMD.CouplingBetweenObjects")
public class AppDependencyModule {

    @Provides
    Context context(Application application) {
        return application;
    }

    @Provides
    @Singleton
    RxEventBus provideRxEventBus() {
        return new RxEventBus();
    }

    @Provides
    MimeTypeMap provideMimeTypeMap() {
        return MimeTypeMap.getSingleton();
    }

    @Provides
    @Singleton
    UserAgentProvider providesUserAgent() {
        return new AndroidUserAgent();
    }

    @Provides
    @Singleton
    public OpenRosaHttpInterface provideHttpInterface(MimeTypeMap mimeTypeMap, UserAgentProvider userAgentProvider) {
        return new OkHttpConnection(
                new OkHttpOpenRosaServerClientProvider(new OkHttpClient()),
                new CollectThenSystemContentTypeMapper(mimeTypeMap),
                userAgentProvider.getUserAgent()
        );
    }

    @Provides
    WebCredentialsUtils provideWebCredentials(SettingsProvider settingsProvider) {
        return new WebCredentialsUtils(settingsProvider.getGeneralSettings());
    }

    @Provides
    public FormDownloader providesFormDownloader(FormSource formSource, FormsRepositoryProvider formsRepositoryProvider, StoragePathProvider storagePathProvider, Analytics analytics) {
        return new ServerFormDownloader(formSource, formsRepositoryProvider.get(), new File(storagePathProvider.getOdkDirPath(StorageSubdirectory.CACHE)), storagePathProvider.getOdkDirPath(StorageSubdirectory.FORMS), new FormMetadataParser(ReferenceManager.instance()), analytics);
    }

    @Provides
    @Singleton
    public Analytics providesAnalytics(Application application) {
        try {
            return new BlockableFirebaseAnalytics(application);
        } catch (IllegalStateException e) {
            // Couldn't setup Firebase so use no-op instance
            return new NoopAnalytics();
        }
    }

    @Provides
    public PermissionsProvider providesPermissionsProvider(PermissionsChecker permissionsChecker) {
        return new PermissionsProvider(permissionsChecker);
    }

    @Provides
    public ReferenceManager providesReferenceManager() {
        return ReferenceManager.instance();
    }

    @Provides
    public AudioHelperFactory providesAudioHelperFactory(Scheduler scheduler) {
        return new ScreenContextAudioHelperFactory(scheduler, MediaPlayer::new);
    }

    @Provides
    public ActivityAvailability providesActivityAvailability(Context context) {
        return new ActivityAvailability(context);
    }

    @Provides
    @Singleton
    public StorageInitializer providesStorageInitializer() {
        return new StorageInitializer();
    }

    @Provides
    @Singleton
    public SettingsProvider providesSettingsProvider(Context context) {
        return new SettingsProvider(context);
    }

    @Provides
    InstallIDProvider providesInstallIDProvider(SettingsProvider settingsProvider) {
        return new SharedPreferencesInstallIDProvider(settingsProvider.getMetaSettings(), KEY_INSTALL_ID);
    }

    @Provides
    public DeviceDetailsProvider providesDeviceDetailsProvider(Context context, InstallIDProvider installIDProvider) {
        return new DeviceDetailsProvider() {

            @Override
            @SuppressLint({"MissingPermission", "HardwareIds"})
            public String getDeviceId() {
                return installIDProvider.getInstallID();
            }

            @Override
            @SuppressLint({"MissingPermission", "HardwareIds"})
            public String getLine1Number() {
                TelephonyManager telMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                return telMgr.getLine1Number();
            }
        };
    }

    @Provides
    @Singleton
    public MapProvider providesMapProvider() {
        return new MapProvider();
    }

    @Provides
    public StoragePathProvider providesStoragePathProvider() {
        return new StoragePathProvider();
    }

    @Provides
    public AdminPasswordProvider providesAdminPasswordProvider(SettingsProvider settingsProvider) {
        return new AdminPasswordProvider(settingsProvider.getAdminSettings());
    }

    @Provides
    public FormUpdateManager providesFormUpdateManger(Scheduler scheduler, SettingsProvider settingsProvider, Application application) {
        return new SchedulerFormUpdateAndSubmitManager(scheduler, settingsProvider.getGeneralSettings(), application);
    }

    @Provides
    public FormSubmitManager providesFormSubmitManager(Scheduler scheduler, SettingsProvider settingsProvider, Application application) {
        return new SchedulerFormUpdateAndSubmitManager(scheduler, settingsProvider.getGeneralSettings(), application);
    }

    @Provides
    public NetworkStateProvider providesConnectivityProvider() {
        return new ConnectivityProvider();
    }

    @Provides
    public QRCodeGenerator providesQRCodeGenerator(Context context) {
        return new CachingQRCodeGenerator();
    }

    @Provides
    public VersionInformation providesVersionInformation() {
        return new VersionInformation(() -> BuildConfig.VERSION_NAME);
    }

    @Provides
    public FileProvider providesFileProvider(Context context) {
        return filePath -> getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", new File(filePath));
    }

    @Provides
    public WorkManager providesWorkManager(Context context) {
        return WorkManager.getInstance(context);
    }

    @Provides
    public Scheduler providesScheduler(WorkManager workManager) {
        return new CoroutineAndWorkManagerScheduler(workManager);
    }

    @Singleton
    @Provides
    public ApplicationInitializer providesApplicationInitializer(Application application, UserAgentProvider userAgentProvider,
                                                                 SettingsPreferenceMigrator preferenceMigrator, PropertyManager propertyManager,
                                                                 Analytics analytics, StorageInitializer storageInitializer, SettingsProvider settingsProvider) {
        return new ApplicationInitializer(application, userAgentProvider, preferenceMigrator, propertyManager, analytics, storageInitializer, settingsProvider);
    }

    @Provides
    public SettingsPreferenceMigrator providesPreferenceMigrator(SettingsProvider settingsProvider) {
        return new CollectSettingsPreferenceMigrator(settingsProvider.getMetaSettings());
    }

    @Provides
    @Singleton
    public PropertyManager providesPropertyManager(RxEventBus eventBus, PermissionsProvider permissionsProvider, DeviceDetailsProvider deviceDetailsProvider, SettingsProvider settingsProvider) {
        return new PropertyManager(eventBus, permissionsProvider, deviceDetailsProvider, settingsProvider);
    }

    @Provides
    public ServerRepository providesServerRepository(Context context, SettingsProvider settingsProvider) {
        return new SharedPreferencesServerRepository(context.getString(R.string.default_server_url), settingsProvider.getMetaSettings());
    }

    @Provides
    public SettingsChangeHandler providesSettingsChangeHandler(PropertyManager propertyManager, FormUpdateManager formUpdateManager, ServerRepository serverRepository, Analytics analytics, SettingsProvider settingsProvider) {
        return new CollectSettingsChangeHandler(propertyManager, formUpdateManager, serverRepository, analytics, settingsProvider);
    }

    @Provides
    public SettingsImporter providesCollectSettingsImporter(SettingsProvider settingsProvider, SettingsPreferenceMigrator preferenceMigrator, SettingsChangeHandler settingsChangeHandler) {
        HashMap<String, Object> generalDefaults = GeneralKeys.DEFAULTS;
        Map<String, Object> adminDefaults = AdminKeys.getDefaults();
        return new SettingsImporter(
                settingsProvider.getGeneralSettings(),
                settingsProvider.getAdminSettings(),
                preferenceMigrator,
                new StructureAndTypeSettingsValidator(generalDefaults, adminDefaults),
                generalDefaults,
                adminDefaults,
                settingsChangeHandler
        );
    }

    @Provides
    public BarcodeViewDecoder providesBarcodeViewDecoder() {
        return new BarcodeViewDecoder();
    }

    @Provides
    public QRCodeDecoder providesQRCodeDecoder() {
        return new QRCodeUtils();
    }

    @Provides
    public FormSource providesFormSource(SettingsProvider settingsProvider, Context context, OpenRosaHttpInterface openRosaHttpInterface, WebCredentialsUtils webCredentialsUtils, Analytics analytics) {
        String serverURL = settingsProvider.getGeneralSettings().getString(GeneralKeys.KEY_SERVER_URL);
        String formListPath = settingsProvider.getGeneralSettings().getString(GeneralKeys.KEY_FORMLIST_URL);

        return new OpenRosaFormSource(serverURL, formListPath, openRosaHttpInterface, webCredentialsUtils, analytics, new OpenRosaResponseParserImpl());
    }

    @Provides
    public DiskFormsSynchronizer providesDiskFormSynchronizer() {
        return new FormsDirDiskFormsSynchronizer();
    }

    @Provides
    @Singleton
    public SyncStatusRepository providesServerFormSyncRepository() {
        return new SyncStatusRepository();
    }

    @Provides
    public ServerFormsDetailsFetcher providesServerFormDetailsFetcher(FormsRepositoryProvider formsRepositoryProvider, FormSource formSource, DiskFormsSynchronizer diskFormsSynchronizer) {
        return new ServerFormsDetailsFetcher(formsRepositoryProvider.get(), formSource, diskFormsSynchronizer);
    }

    @Provides
    public ServerFormsSynchronizer providesServerFormSynchronizer(ServerFormsDetailsFetcher serverFormsDetailsFetcher, FormsRepositoryProvider formsRepositoryProvider, FormDownloader formDownloader, InstancesRepositoryProvider instancesRepositoryProvider, FastExternalItemsetsRepository fastExternalItemsetsRepository) {
        return new ServerFormsSynchronizer(serverFormsDetailsFetcher, formsRepositoryProvider.get(), instancesRepositoryProvider.get(), formDownloader, fastExternalItemsetsRepository);
    }

    @Provides
    public Notifier providesNotifier(Application application, SettingsProvider settingsProvider) {
        return new NotificationManagerNotifier(application, settingsProvider);
    }

    @Provides
    @Named("FORMS")
    @Singleton
    public ChangeLock providesFormsChangeLock() {
        return new ReentrantLockChangeLock();
    }

    @Provides
    @Named("INSTANCES")
    @Singleton
    public ChangeLock providesInstancesChangeLock() {
        return new ReentrantLockChangeLock();
    }

    @Provides
    public GoogleApiProvider providesGoogleApiProvider(Context context) {
        return new GoogleApiProvider(context);
    }

    @Provides
    public GoogleAccountPicker providesGoogleAccountPicker(Context context) {
        return new GoogleAccountCredentialGoogleAccountPicker(GoogleAccountCredential
                .usingOAuth2(context, Collections.singletonList(DriveScopes.DRIVE))
                .setBackOff(new ExponentialBackOff()));
    }

    @Provides
    ScreenUtils providesScreenUtils(Context context) {
        return new ScreenUtils(context);
    }

    @Provides
    public AudioRecorder providesAudioRecorder(Application application) {
        return new AudioRecorderFactory(application).create();
    }

    @Provides
    public FormSaveViewModel.FactoryFactory providesFormSaveViewModelFactoryFactory(Analytics analytics, Scheduler scheduler, AudioRecorder audioRecorder) {
        return (owner, defaultArgs) -> new AbstractSavedStateViewModelFactory(owner, defaultArgs) {
            @NonNull
            @Override
            protected <T extends ViewModel> T create(@NonNull String key, @NonNull Class<T> modelClass, @NonNull SavedStateHandle handle) {
                return (T) new FormSaveViewModel(handle, System::currentTimeMillis, new DiskFormSaver(), new MediaUtils(), analytics, scheduler, audioRecorder);
            }
        };
    }

    @Provides
    public Clock providesClock() {
        return System::currentTimeMillis;
    }

    @Provides
    public SoftKeyboardController provideSoftKeyboardController() {
        return new SoftKeyboardController();
    }

    @Provides
    public JsonPreferencesGenerator providesJsonPreferencesGenerator(SettingsProvider settingsProvider) {
        return new JsonPreferencesGenerator(settingsProvider);
    }

    @Provides
    @Singleton
    public PermissionsChecker providesPermissionsChecker(Context context) {
        return new PermissionsChecker(context);
    }

    @Provides
    @Singleton
    public ExternalAppIntentProvider providesExternalAppIntentProvider() {
        return new ExternalAppIntentProvider();
    }

    @Provides
    public FormEntryViewModel.Factory providesFormEntryViewModelFactory(Clock clock, Analytics analytics) {
        return new FormEntryViewModel.Factory(clock, analytics);
    }

    @Provides
    public BackgroundAudioViewModel.Factory providesBackgroundAudioViewModelFactory(AudioRecorder audioRecorder, SettingsProvider settingsProvider, PermissionsChecker permissionsChecker, Clock clock, Analytics analytics) {
        return new BackgroundAudioViewModel.Factory(audioRecorder, settingsProvider.getGeneralSettings(), permissionsChecker, clock, analytics);
    }

    @Provides
    @Named("GENERAL_SETTINGS_STORE")
    @Singleton
    public SettingsStore providesGeneralSettingsStore(SettingsProvider settingsProvider) {
        return new SettingsStore(settingsProvider.getGeneralSettings());
    }

    @Provides
    @Named("ADMIN_SETTINGS_STORE")
    @Singleton
    public SettingsStore providesAdminSettingsStore(SettingsProvider settingsProvider) {
        return new SettingsStore(settingsProvider.getAdminSettings());
    }

    @Provides
    public ExternalWebPageHelper providesExternalWebPageHelper() {
        return new ExternalWebPageHelper();
    }

    @Provides
    @Singleton
    public ProjectsRepository providesProjectsRepository(UUIDGenerator uuidGenerator, Gson gson, SettingsProvider settingsProvider) {
        return new SharedPreferencesProjectsRepository(uuidGenerator, gson, settingsProvider.getMetaSettings());
    }

    @Provides
    public Gson providesGson() {
        return new Gson();
    }

    @Provides
    @Singleton
    public UUIDGenerator providesUUIDGenerator() {
        return new UUIDGenerator();
    }

    @Provides
    @Singleton
    public InstancesCountRepository providesFormCountRepository() {
        return new InstancesCountRepository();
    }

    @Provides
    @Singleton
    public FormsDatabaseProvider providesFormsDatabaseProvider() {
        return new FormsDatabaseProvider();
    }

    @Provides
    public FastExternalItemsetsRepository providesItemsetsRepository() {
        return new DatabaseFastExternalItemsetsRepository();
    }

    @Provides
    @Singleton
    public InstancesDatabaseProvider providesInstanceDatabaseProvider() {
        return new InstancesDatabaseProvider();
    }

    @Provides
    public CurrentProjectProvider providesCurrentProjectProvider(SettingsProvider settingsProvider, ProjectsRepository projectsRepository) {
        return new CurrentProjectProvider(settingsProvider, projectsRepository);
    }

    @Provides
    public FormsRepositoryProvider providesFormsRepositoryProvider() {
        return new FormsRepositoryProvider();
    }

    @Provides
    public InstancesRepositoryProvider providesInstancesRepositoryProvider() {
        return new InstancesRepositoryProvider();
    }
}
