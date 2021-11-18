package org.odk.collect.android.injection.config;

import static androidx.core.content.FileProvider.getUriForFile;
import static org.odk.collect.android.preferences.keys.MetaKeys.KEY_INSTALL_ID;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

import android.app.Application;
import android.content.Context;
import android.media.MediaPlayer;
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
import org.odk.collect.android.activities.viewmodels.CurrentProjectViewModel;
import org.odk.collect.android.activities.viewmodels.MainMenuViewModel;
import org.odk.collect.android.activities.viewmodels.SplashScreenViewModel;
import org.odk.collect.android.application.CollectSettingsChangeHandler;
import org.odk.collect.android.application.initialization.AnalyticsInitializer;
import org.odk.collect.android.application.initialization.ApplicationInitializer;
import org.odk.collect.android.application.initialization.CollectSettingsMigrator;
import org.odk.collect.android.application.initialization.ExistingProjectMigrator;
import org.odk.collect.android.application.initialization.ExistingSettingsMigrator;
import org.odk.collect.android.application.initialization.FormUpdatesUpgrade;
import org.odk.collect.android.application.initialization.SettingsMigrator;
import org.odk.collect.android.application.initialization.upgrade.AppUpgrader;
import org.odk.collect.android.backgroundwork.FormUpdateAndInstanceSubmitScheduler;
import org.odk.collect.android.backgroundwork.FormUpdateScheduler;
import org.odk.collect.android.backgroundwork.InstanceSubmitScheduler;
import org.odk.collect.android.configure.SettingsChangeHandler;
import org.odk.collect.android.configure.SettingsImporter;
import org.odk.collect.android.configure.SettingsValidator;
import org.odk.collect.android.configure.StructureAndTypeSettingsValidator;
import org.odk.collect.android.configure.qr.AppConfigurationGenerator;
import org.odk.collect.android.configure.qr.CachingQRCodeGenerator;
import org.odk.collect.android.configure.qr.QRCodeDecoder;
import org.odk.collect.android.configure.qr.QRCodeGenerator;
import org.odk.collect.android.configure.qr.QRCodeUtils;
import org.odk.collect.android.database.itemsets.DatabaseFastExternalItemsetsRepository;
import org.odk.collect.android.events.RxEventBus;
import org.odk.collect.android.formentry.BackgroundAudioViewModel;
import org.odk.collect.android.formentry.FormEntryViewModel;
import org.odk.collect.android.formentry.media.AudioHelperFactory;
import org.odk.collect.android.formentry.media.ScreenContextAudioHelperFactory;
import org.odk.collect.android.formentry.saving.DiskFormSaver;
import org.odk.collect.android.formentry.saving.FormSaveViewModel;
import org.odk.collect.android.formmanagement.FormDownloader;
import org.odk.collect.android.formmanagement.FormMetadataParser;
import org.odk.collect.android.formmanagement.FormSourceProvider;
import org.odk.collect.android.formmanagement.FormsUpdater;
import org.odk.collect.android.formmanagement.InstancesAppState;
import org.odk.collect.android.formmanagement.ServerFormDownloader;
import org.odk.collect.android.formmanagement.ServerFormsDetailsFetcher;
import org.odk.collect.android.formmanagement.matchexactly.SyncStatusAppState;
import org.odk.collect.android.gdrive.GoogleAccountCredentialGoogleAccountPicker;
import org.odk.collect.android.gdrive.GoogleAccountPicker;
import org.odk.collect.android.gdrive.GoogleAccountsManager;
import org.odk.collect.android.gdrive.GoogleApiProvider;
import org.odk.collect.android.geo.DirectoryReferenceLayerRepository;
import org.odk.collect.android.geo.MapProvider;
import org.odk.collect.android.geo.ReferenceLayerRepository;
import org.odk.collect.android.instancemanagement.InstanceAutoSender;
import org.odk.collect.android.itemsets.FastExternalItemsetsRepository;
import org.odk.collect.android.logic.PropertyManager;
import org.odk.collect.android.metadata.InstallIDProvider;
import org.odk.collect.android.metadata.SharedPreferencesInstallIDProvider;
import org.odk.collect.android.network.ConnectivityProvider;
import org.odk.collect.android.network.NetworkStateProvider;
import org.odk.collect.android.notifications.NotificationManagerNotifier;
import org.odk.collect.android.notifications.Notifier;
import org.odk.collect.android.openrosa.CollectThenSystemContentTypeMapper;
import org.odk.collect.android.openrosa.OpenRosaHttpInterface;
import org.odk.collect.android.openrosa.okhttp.OkHttpConnection;
import org.odk.collect.android.openrosa.okhttp.OkHttpOpenRosaServerClientProvider;
import org.odk.collect.android.utilities.MediaUtils;
import org.odk.collect.android.widgets.utilities.FileRequester;
import org.odk.collect.android.widgets.utilities.FileRequesterImpl;
import org.odk.collect.android.widgets.utilities.StringRequester;
import org.odk.collect.android.widgets.utilities.StringRequesterImpl;
import org.odk.collect.androidshared.system.PermissionsChecker;
import org.odk.collect.android.permissions.PermissionsProvider;
import org.odk.collect.android.preferences.PreferenceVisibilityHandler;
import org.odk.collect.android.preferences.ProjectPreferencesViewModel;
import org.odk.collect.android.preferences.keys.MetaKeys;
import org.odk.collect.android.preferences.keys.ProjectKeys;
import org.odk.collect.android.preferences.keys.ProtectedProjectKeys;
import org.odk.collect.android.preferences.source.SettingsProvider;
import org.odk.collect.android.preferences.source.SettingsStore;
import org.odk.collect.android.preferences.source.SharedPreferencesSettingsProvider;
import org.odk.collect.android.projects.CurrentProjectProvider;
import org.odk.collect.android.projects.ProjectCreator;
import org.odk.collect.android.projects.ProjectDeleter;
import org.odk.collect.android.projects.ProjectDetailsCreator;
import org.odk.collect.android.projects.ProjectImporter;
import org.odk.collect.android.storage.StoragePathProvider;
import org.odk.collect.android.storage.StorageSubdirectory;
import org.odk.collect.android.utilities.AdminPasswordProvider;
import org.odk.collect.android.utilities.AndroidUserAgent;
import org.odk.collect.android.utilities.ChangeLockProvider;
import org.odk.collect.android.utilities.CodeCaptureManagerFactory;
import org.odk.collect.android.utilities.DeviceDetailsProvider;
import org.odk.collect.android.utilities.ExternalAppIntentProvider;
import org.odk.collect.android.utilities.ExternalWebPageHelper;
import org.odk.collect.android.utilities.FileProvider;
import org.odk.collect.android.utilities.FormsDirDiskFormsSynchronizer;
import org.odk.collect.android.utilities.FormsRepositoryProvider;
import org.odk.collect.android.utilities.InstancesRepositoryProvider;
import org.odk.collect.android.utilities.LaunchState;
import org.odk.collect.android.utilities.ProjectResetter;
import org.odk.collect.android.utilities.ScreenUtils;
import org.odk.collect.android.utilities.SoftKeyboardController;
import org.odk.collect.android.utilities.StaticCachingDeviceDetailsProvider;
import org.odk.collect.android.utilities.WebCredentialsUtils;
import org.odk.collect.android.version.VersionInformation;
import org.odk.collect.android.views.BarcodeViewDecoder;
import org.odk.collect.androidshared.system.IntentLauncher;
import org.odk.collect.androidshared.system.IntentLauncherImpl;
import org.odk.collect.async.CoroutineAndWorkManagerScheduler;
import org.odk.collect.async.Scheduler;
import org.odk.collect.audiorecorder.recording.AudioRecorder;
import org.odk.collect.audiorecorder.recording.AudioRecorderFactory;
import org.odk.collect.forms.FormsRepository;
import org.odk.collect.projects.ProjectsRepository;
import org.odk.collect.projects.SharedPreferencesProjectsRepository;
import org.odk.collect.shared.strings.UUIDGenerator;
import org.odk.collect.utilities.Clock;
import org.odk.collect.utilities.UserAgentProvider;

import java.io.File;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;

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
        return new WebCredentialsUtils(settingsProvider.getUnprotectedSettings());
    }

    @Provides
    public FormDownloader providesFormDownloader(FormSourceProvider formSourceProvider, FormsRepositoryProvider formsRepositoryProvider, StoragePathProvider storagePathProvider, Analytics analytics) {
        return new ServerFormDownloader(formSourceProvider.get(), formsRepositoryProvider.get(), new File(storagePathProvider.getOdkDirPath(StorageSubdirectory.CACHE)), storagePathProvider.getOdkDirPath(StorageSubdirectory.FORMS), new FormMetadataParser(), analytics);
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
    @Singleton
    public SettingsProvider providesSettingsProvider(Context context) {
        return new SharedPreferencesSettingsProvider(context);
    }


    @Provides
    InstallIDProvider providesInstallIDProvider(SettingsProvider settingsProvider) {
        return new SharedPreferencesInstallIDProvider(settingsProvider.getMetaSettings(), KEY_INSTALL_ID);
    }

    @Provides
    public DeviceDetailsProvider providesDeviceDetailsProvider(Context context, InstallIDProvider installIDProvider) {
        return new StaticCachingDeviceDetailsProvider(installIDProvider, context);
    }

    @Provides
    @Singleton
    public MapProvider providesMapProvider() {
        return new MapProvider();
    }

    @Provides
    public StoragePathProvider providesStoragePathProvider(Context context, CurrentProjectProvider currentProjectProvider, ProjectsRepository projectsRepository) {
        File externalFilesDir = context.getExternalFilesDir(null);

        if (externalFilesDir != null) {
            return new StoragePathProvider(currentProjectProvider, projectsRepository, externalFilesDir.getAbsolutePath());
        } else {
            throw new IllegalStateException("Storage is not available!");
        }
    }

    @Provides
    public AdminPasswordProvider providesAdminPasswordProvider(SettingsProvider settingsProvider) {
        return new AdminPasswordProvider(settingsProvider.getProtectedSettings());
    }

    @Provides
    public FormUpdateScheduler providesFormUpdateManger(Scheduler scheduler, SettingsProvider settingsProvider, Application application) {
        return new FormUpdateAndInstanceSubmitScheduler(scheduler, settingsProvider, application);
    }

    @Provides
    public InstanceSubmitScheduler providesFormSubmitManager(Scheduler scheduler, SettingsProvider settingsProvider, Application application) {
        return new FormUpdateAndInstanceSubmitScheduler(scheduler, settingsProvider, application);
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

    @Provides
    public SettingsMigrator providesPreferenceMigrator(SettingsProvider settingsProvider) {
        return new CollectSettingsMigrator(settingsProvider.getMetaSettings());
    }

    @Provides
    @Singleton
    public PropertyManager providesPropertyManager(RxEventBus eventBus, PermissionsProvider permissionsProvider, DeviceDetailsProvider deviceDetailsProvider, SettingsProvider settingsProvider) {
        return new PropertyManager(eventBus, permissionsProvider, deviceDetailsProvider, settingsProvider);
    }

    @Provides
    public SettingsChangeHandler providesSettingsChangeHandler(PropertyManager propertyManager, FormUpdateScheduler formUpdateScheduler, Analytics analytics, SettingsProvider settingsProvider) {
        return new CollectSettingsChangeHandler(propertyManager, formUpdateScheduler, analytics, settingsProvider);
    }

    @Provides
    public SettingsImporter providesCollectSettingsImporter(SettingsProvider settingsProvider, SettingsMigrator preferenceMigrator, SettingsValidator settingsValidator, SettingsChangeHandler settingsChangeHandler, ProjectsRepository projectsRepository, Context context) {
        return new SettingsImporter(
                settingsProvider,
                preferenceMigrator,
                settingsValidator,
                ProjectKeys.getDefaults(),
                ProtectedProjectKeys.getDefaults(),
                settingsChangeHandler,
                projectsRepository,
                new ProjectDetailsCreator(context)
        );
    }

    @Provides
    public SettingsValidator providesSettingsValidator() {
        return new StructureAndTypeSettingsValidator(ProjectKeys.getDefaults(), ProtectedProjectKeys.getDefaults());
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
    @Singleton
    public SyncStatusAppState providesServerFormSyncRepository(Context context) {
        return new SyncStatusAppState(context);
    }

    @Provides
    public ServerFormsDetailsFetcher providesServerFormDetailsFetcher(FormsRepositoryProvider formsRepositoryProvider, FormSourceProvider formSourceProvider, StoragePathProvider storagePathProvider) {
        FormsRepository formsRepository = formsRepositoryProvider.get();
        return new ServerFormsDetailsFetcher(formsRepository, formSourceProvider.get(), new FormsDirDiskFormsSynchronizer(formsRepository, storagePathProvider.getOdkDirPath(StorageSubdirectory.FORMS)));
    }

    @Provides
    public Notifier providesNotifier(Application application, SettingsProvider settingsProvider) {
        return new NotificationManagerNotifier(application, settingsProvider);
    }

    @Provides
    @Singleton
    public ChangeLockProvider providesChangeLockProvider() {
        return new ChangeLockProvider();
    }

    @Provides
    public GoogleApiProvider providesGoogleApiProvider(Context context) {
        return new GoogleApiProvider(context);
    }

    @Provides
    public GoogleAccountPicker providesGoogleAccountPicker(Context context) {
        return new GoogleAccountCredentialGoogleAccountPicker(GoogleAccountCredential
                .usingOAuth2(context, singletonList(DriveScopes.DRIVE))
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
    public FormSaveViewModel.FactoryFactory providesFormSaveViewModelFactoryFactory(Analytics analytics, Scheduler scheduler, AudioRecorder audioRecorder, CurrentProjectProvider currentProjectProvider) {
        return (owner, defaultArgs) -> new AbstractSavedStateViewModelFactory(owner, defaultArgs) {
            @NonNull
            @Override
            protected <T extends ViewModel> T create(@NonNull String key, @NonNull Class<T> modelClass, @NonNull SavedStateHandle handle) {
                return (T) new FormSaveViewModel(handle, System::currentTimeMillis, new DiskFormSaver(), new MediaUtils(), analytics, scheduler, audioRecorder, currentProjectProvider);
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
    public AppConfigurationGenerator providesJsonPreferencesGenerator(SettingsProvider settingsProvider, CurrentProjectProvider currentProjectProvider) {
        return new AppConfigurationGenerator(settingsProvider, currentProjectProvider);
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
        return new FormEntryViewModel.Factory(clock);
    }

    @Provides
    public BackgroundAudioViewModel.Factory providesBackgroundAudioViewModelFactory(AudioRecorder audioRecorder, SettingsProvider settingsProvider, PermissionsChecker permissionsChecker, Clock clock, Analytics analytics) {
        return new BackgroundAudioViewModel.Factory(audioRecorder, settingsProvider.getUnprotectedSettings(), permissionsChecker, clock);
    }

    @Provides
    @Named("GENERAL_SETTINGS_STORE")
    public SettingsStore providesGeneralSettingsStore(SettingsProvider settingsProvider) {
        return new SettingsStore(settingsProvider.getUnprotectedSettings());
    }

    @Provides
    @Named("ADMIN_SETTINGS_STORE")
    public SettingsStore providesAdminSettingsStore(SettingsProvider settingsProvider) {
        return new SettingsStore(settingsProvider.getProtectedSettings());
    }

    @Provides
    public ExternalWebPageHelper providesExternalWebPageHelper() {
        return new ExternalWebPageHelper();
    }

    @Provides
    @Singleton
    public ProjectsRepository providesProjectsRepository(UUIDGenerator uuidGenerator, Gson gson, SettingsProvider settingsProvider) {
        return new SharedPreferencesProjectsRepository(uuidGenerator, gson, settingsProvider.getMetaSettings(), MetaKeys.KEY_PROJECTS);
    }

    @Provides
    public ProjectCreator providesProjectCreator(ProjectImporter projectImporter, ProjectsRepository projectsRepository,
                                                 CurrentProjectProvider currentProjectProvider, SettingsImporter settingsImporter,
                                                 Context context) {
        return new ProjectCreator(projectImporter, projectsRepository, currentProjectProvider, settingsImporter);
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
    public InstancesAppState providesInstancesAppState(Application application, InstancesRepositoryProvider instancesRepositoryProvider, CurrentProjectProvider currentProjectProvider) {
        return new InstancesAppState(application, instancesRepositoryProvider, currentProjectProvider);
    }

    @Provides
    public FastExternalItemsetsRepository providesItemsetsRepository() {
        return new DatabaseFastExternalItemsetsRepository();
    }

    @Provides
    public CurrentProjectProvider providesCurrentProjectProvider(SettingsProvider settingsProvider, ProjectsRepository projectsRepository) {
        return new CurrentProjectProvider(settingsProvider, projectsRepository);
    }

    @Provides
    public FormsRepositoryProvider providesFormsRepositoryProvider(Application application) {
        return new FormsRepositoryProvider(application);
    }

    @Provides
    public InstancesRepositoryProvider providesInstancesRepositoryProvider(Context context, StoragePathProvider storagePathProvider) {
        return new InstancesRepositoryProvider(context, storagePathProvider);
    }

    @Provides
    public SplashScreenViewModel.Factory providesSplashScreenViewModel(SettingsProvider settingsProvider, ProjectsRepository projectsRepository) {
        return new SplashScreenViewModel.Factory(settingsProvider.getUnprotectedSettings(), projectsRepository);
    }

    @Provides
    public ProjectPreferencesViewModel.Factory providesProjectPreferencesViewModel(AdminPasswordProvider adminPasswordProvider) {
        return new ProjectPreferencesViewModel.Factory(adminPasswordProvider);
    }

    @Provides
    public ProjectImporter providesProjectImporter(ProjectsRepository projectsRepository, StoragePathProvider storagePathProvider) {
        return new ProjectImporter(projectsRepository);
    }

    @Provides
    public LaunchState providesAppStateProvider(Context context, SettingsProvider settingsProvider) {
        return new LaunchState(context, settingsProvider.getMetaSettings(), BuildConfig.VERSION_CODE);
    }

    @Provides
    public MainMenuViewModel.Factory providesMainMenuViewModelFactory(VersionInformation versionInformation, Application application,
                                                                      SettingsProvider settingsProvider, InstancesAppState instancesAppState,
                                                                      Scheduler scheduler) {
        return new MainMenuViewModel.Factory(versionInformation, application, settingsProvider, instancesAppState, scheduler);
    }

    @Provides
    public AnalyticsInitializer providesAnalyticsInitializer(Analytics analytics, VersionInformation versionInformation, SettingsProvider settingsProvider) {
        return new AnalyticsInitializer(analytics, versionInformation, settingsProvider);
    }

    @Provides
    public CurrentProjectViewModel.Factory providesCurrentProjectViewModel(CurrentProjectProvider currentProjectProvider, AnalyticsInitializer analyticsInitializer, StoragePathProvider storagePathProvider, ProjectsRepository projectsRepository) {
        return new CurrentProjectViewModel.Factory(currentProjectProvider, analyticsInitializer);
    }

    @Provides
    public FormSourceProvider providesFormSourceProvider(SettingsProvider settingsProvider, OpenRosaHttpInterface openRosaHttpInterface) {
        return new FormSourceProvider(settingsProvider, openRosaHttpInterface);
    }

    @Provides
    public FormsUpdater providesFormUpdateChecker(Context context, Notifier notifier, Analytics analytics, StoragePathProvider storagePathProvider, SettingsProvider settingsProvider, FormsRepositoryProvider formsRepositoryProvider, FormSourceProvider formSourceProvider, SyncStatusAppState syncStatusAppState, InstancesRepositoryProvider instancesRepositoryProvider, ChangeLockProvider changeLockProvider) {
        return new FormsUpdater(context, notifier, analytics, storagePathProvider, settingsProvider, formsRepositoryProvider, formSourceProvider, syncStatusAppState, instancesRepositoryProvider, changeLockProvider);
    }

    @Provides
    public InstanceAutoSender providesInstanceAutoSender(Context context, ChangeLockProvider changeLockProvider, Notifier notifier, Analytics analytics, FormsRepositoryProvider formsRepositoryProvider, InstancesRepositoryProvider instancesRepositoryProvider, GoogleAccountsManager googleAccountsManager, GoogleApiProvider googleApiProvider, PermissionsProvider permissionsProvider, SettingsProvider settingsProvider, InstancesAppState instancesAppState) {
        return new InstanceAutoSender(context, changeLockProvider, notifier, analytics, formsRepositoryProvider, instancesRepositoryProvider, googleAccountsManager, googleApiProvider, permissionsProvider, settingsProvider, instancesAppState);
    }

    @Provides
    public CodeCaptureManagerFactory providesCodeCaptureManagerFactory() {
        return CodeCaptureManagerFactory.INSTANCE;
    }

    @Provides
    public ExistingProjectMigrator providesExistingProjectMigrator(Context context, StoragePathProvider storagePathProvider, ProjectsRepository projectsRepository, SettingsProvider settingsProvider, CurrentProjectProvider currentProjectProvider) {
        return new ExistingProjectMigrator(context, storagePathProvider, projectsRepository, settingsProvider, currentProjectProvider, new ProjectDetailsCreator(context));
    }

    @Provides
    public FormUpdatesUpgrade providesFormUpdatesUpgrader(Scheduler scheduler, ProjectsRepository projectsRepository, FormUpdateScheduler formUpdateScheduler) {
        return new FormUpdatesUpgrade(scheduler, projectsRepository, formUpdateScheduler);
    }

    @Provides
    public ExistingSettingsMigrator providesExistingSettingsMigrator(ProjectsRepository projectsRepository, SettingsProvider settingsProvider, SettingsMigrator settingsMigrator) {
        return new ExistingSettingsMigrator(projectsRepository, settingsProvider, settingsMigrator);
    }

    @Provides
    public AppUpgrader providesAppUpgrader(SettingsProvider settingsProvider, ExistingProjectMigrator existingProjectMigrator, FormUpdatesUpgrade formUpdatesUpgrade, ExistingSettingsMigrator existingSettingsMigrator) {
        return new AppUpgrader(settingsProvider.getMetaSettings(), asList(
                existingProjectMigrator,
                existingSettingsMigrator,
                formUpdatesUpgrade
        ));
    }

    @Provides
    public ApplicationInitializer providesApplicationInitializer(Application context, UserAgentProvider userAgentProvider, PropertyManager propertyManager, Analytics analytics, LaunchState launchState, AppUpgrader appUpgrader, AnalyticsInitializer analyticsInitializer, ProjectsRepository projectsRepository, SettingsProvider settingsProvider) {
        return new ApplicationInitializer(context, userAgentProvider, propertyManager, analytics, launchState, appUpgrader, analyticsInitializer, projectsRepository, settingsProvider);
    }

    @Provides
    public ProjectDeleter providesProjectDeleter(ProjectsRepository projectsRepository, CurrentProjectProvider currentProjectProvider, FormUpdateScheduler formUpdateScheduler, InstanceSubmitScheduler instanceSubmitScheduler, InstancesRepositoryProvider instancesRepositoryProvider, StoragePathProvider storagePathProvider, ChangeLockProvider changeLockProvider, SettingsProvider settingsProvider) {
        return new ProjectDeleter(projectsRepository, currentProjectProvider, formUpdateScheduler, instanceSubmitScheduler, instancesRepositoryProvider.get(), storagePathProvider.getProjectRootDirPath(currentProjectProvider.getCurrentProject().getUuid()), changeLockProvider, settingsProvider);
    }

    @Provides
    public ProjectResetter providesProjectResetter(StoragePathProvider storagePathProvider, PropertyManager propertyManager, SettingsProvider settingsProvider, InstancesRepositoryProvider instancesRepositoryProvider, FormsRepositoryProvider formsRepositoryProvider) {
        return new ProjectResetter(storagePathProvider, propertyManager, settingsProvider, instancesRepositoryProvider, formsRepositoryProvider);
    }

    @Provides
    public PreferenceVisibilityHandler providesDisabledPreferencesRemover(SettingsProvider settingsProvider, VersionInformation versionInformation) {
        return new PreferenceVisibilityHandler(settingsProvider, versionInformation);
    }

    @Provides
    public ReferenceLayerRepository providesReferenceLayerRepository(StoragePathProvider storagePathProvider) {
        return new DirectoryReferenceLayerRepository(
                storagePathProvider.getOdkDirPath(StorageSubdirectory.LAYERS),
                storagePathProvider.getOdkDirPath(StorageSubdirectory.SHARED_LAYERS)
        );
    }

    @Provides
    public IntentLauncher providesIntentLauncher() {
        return IntentLauncherImpl.INSTANCE;
    }

    @Provides
    public FileRequester providesFileRequester(IntentLauncher intentLauncher, ExternalAppIntentProvider externalAppIntentProvider) {
        return new FileRequesterImpl(intentLauncher, externalAppIntentProvider);
    }

    @Provides
    public StringRequester providesStringRequester(IntentLauncher intentLauncher, ExternalAppIntentProvider externalAppIntentProvider) {
        return new StringRequesterImpl(intentLauncher, externalAppIntentProvider);
    }
}
