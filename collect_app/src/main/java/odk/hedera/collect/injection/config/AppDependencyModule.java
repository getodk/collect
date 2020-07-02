package odk.hedera.collect.injection.config;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.telephony.TelephonyManager;
import android.webkit.MimeTypeMap;

import org.javarosa.core.reference.ReferenceManager;
import org.odk.hedera.collect.BuildConfig;
import odk.hedera.collect.analytics.Analytics;
import odk.hedera.collect.analytics.FirebaseAnalytics;
import odk.hedera.collect.application.initialization.ApplicationInitializer;
import odk.hedera.collect.backgroundwork.CollectBackgroundWorkManager;
import odk.hedera.collect.dao.FormsDao;
import odk.hedera.collect.dao.InstancesDao;
import odk.hedera.collect.events.RxEventBus;
import odk.hedera.collect.formentry.media.AudioHelperFactory;
import odk.hedera.collect.formentry.media.ScreenContextAudioHelperFactory;
import odk.hedera.collect.geo.MapProvider;
import odk.hedera.collect.jobs.CollectJobCreator;
import odk.hedera.collect.metadata.InstallIDProvider;
import odk.hedera.collect.metadata.SharedPreferencesInstallIDProvider;
import odk.hedera.collect.network.ConnectivityProvider;
import odk.hedera.collect.network.NetworkStateProvider;
import odk.hedera.collect.openrosa.CollectThenSystemContentTypeMapper;
import odk.hedera.collect.openrosa.OpenRosaAPIClient;
import odk.hedera.collect.openrosa.OpenRosaHttpInterface;
import odk.hedera.collect.openrosa.okhttp.OkHttpConnection;
import odk.hedera.collect.openrosa.okhttp.OkHttpOpenRosaServerClientProvider;
import odk.hedera.collect.preferences.AdminSharedPreferences;
import odk.hedera.collect.preferences.GeneralSharedPreferences;
import odk.hedera.collect.preferences.PreferencesProvider;
import odk.hedera.collect.preferences.qr.CachingQRCodeGenerator;
import odk.hedera.collect.preferences.qr.QRCodeGenerator;
import odk.hedera.collect.storage.StorageInitializer;
import odk.hedera.collect.storage.StoragePathProvider;
import odk.hedera.collect.storage.StorageStateProvider;
import odk.hedera.collect.storage.migration.StorageEraser;
import odk.hedera.collect.storage.migration.StorageMigrationRepository;
import odk.hedera.collect.storage.migration.StorageMigrator;
import odk.hedera.collect.utilities.ActivityAvailability;
import odk.hedera.collect.utilities.AdminPasswordProvider;
import odk.hedera.collect.utilities.AndroidUserAgent;
import odk.hedera.collect.utilities.DeviceDetailsProvider;
import odk.hedera.collect.utilities.FileProvider;
import odk.hedera.collect.utilities.FormListDownloader;
import odk.hedera.collect.utilities.PermissionUtils;
import odk.hedera.collect.utilities.WebCredentialsUtils;
import odk.hedera.collect.version.VersionInformation;
import org.odk.hedera.async.CoroutineScheduler;
import org.odk.hedera.async.Scheduler;
import odk.hedera.collect.utilities.BackgroundWorkManager;
import odk.hedera.collect.utilities.UserAgentProvider;

import java.io.File;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;

import static androidx.core.content.FileProvider.getUriForFile;
import static odk.hedera.collect.preferences.MetaKeys.KEY_INSTALL_ID;

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
    public InstancesDao provideInstancesDao() {
        return new InstancesDao();
    }

    @Provides
    public FormsDao provideFormsDao() {
        return new FormsDao();
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
    public OpenRosaAPIClient provideCollectServerClient(OpenRosaHttpInterface httpInterface, WebCredentialsUtils webCredentialsUtils) {
        return new OpenRosaAPIClient(httpInterface, webCredentialsUtils);
    }

    @Provides
    WebCredentialsUtils provideWebCredentials() {
        return new WebCredentialsUtils();
    }

    @Provides
    FormListDownloader provideDownloadFormListDownloader(
            Application application,
            OpenRosaAPIClient openRosaAPIClient,
            WebCredentialsUtils webCredentialsUtils,
            FormsDao formsDao) {
        return new FormListDownloader(
                application,
                openRosaAPIClient,
                webCredentialsUtils,
                formsDao
        );
    }

    @Provides
    @Singleton
    public Analytics providesAnalytics(Application application, GeneralSharedPreferences generalSharedPreferences) {
        com.google.firebase.analytics.FirebaseAnalytics firebaseAnalyticsInstance = com.google.firebase.analytics.FirebaseAnalytics.getInstance(application);
        return new FirebaseAnalytics(firebaseAnalyticsInstance, generalSharedPreferences);
    }

    @Provides
    public PermissionUtils providesPermissionUtils() {
        return new PermissionUtils();
    }

    @Provides
    public ReferenceManager providesReferenceManager() {
        return ReferenceManager.instance();
    }

    @Provides
    public AudioHelperFactory providesAudioHelperFactory() {
        return new ScreenContextAudioHelperFactory();
    }

    @Provides
    public ActivityAvailability providesActivityAvailability(Context context) {
        return new ActivityAvailability(context);
    }

    @Provides
    @Singleton
    public StorageMigrationRepository providesStorageMigrationRepository() {
        return new StorageMigrationRepository();
    }

    @Provides
    @Singleton
    public StorageInitializer providesStorageInitializer() {
        return new StorageInitializer();
    }

    @Provides
    StorageMigrator providesStorageMigrator(StoragePathProvider storagePathProvider, StorageStateProvider storageStateProvider, StorageMigrationRepository storageMigrationRepository, ReferenceManager referenceManager, BackgroundWorkManager backgroundWorkManager, Analytics analytics) {
        StorageEraser storageEraser = new StorageEraser(storagePathProvider);

        return new StorageMigrator(storagePathProvider, storageStateProvider, storageEraser, storageMigrationRepository, GeneralSharedPreferences.getInstance(), referenceManager, backgroundWorkManager, analytics);
    }

    @Provides
    PreferencesProvider providesPreferencesProvider(Context context) {
        return new PreferencesProvider(context);
    }

    @Provides
    InstallIDProvider providesInstallIDProvider(PreferencesProvider preferencesProvider) {
        return new SharedPreferencesInstallIDProvider(preferencesProvider.getMetaSharedPreferences(), KEY_INSTALL_ID);
    }

    @Provides
    public DeviceDetailsProvider providesDeviceDetailsProvider(Context context) {
        TelephonyManager telMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        return new DeviceDetailsProvider() {

            @Override
            @SuppressLint({"MissingPermission", "HardwareIds"})
            public String getDeviceId() {
                return telMgr.getDeviceId();
            }

            @Override
            @SuppressLint({"MissingPermission", "HardwareIds"})
            public String getLine1Number() {
                return telMgr.getLine1Number();
            }

            @Override
            @SuppressLint({"MissingPermission", "HardwareIds"})
            public String getSubscriberId() {
                return telMgr.getSubscriberId();
            }

            @Override
            @SuppressLint({"MissingPermission", "HardwareIds"})
            public String getSimSerialNumber() {
                return telMgr.getSimSerialNumber();
            }
        };
    }

    @Provides
    @Singleton
    GeneralSharedPreferences providesGeneralSharedPreferences(Context context) {
        return new GeneralSharedPreferences(context);
    }

    @Provides
    @Singleton
    AdminSharedPreferences providesAdminSharedPreferences(Context context) {
        return new AdminSharedPreferences(context);
    }

    @Provides
    @Singleton
    public MapProvider providesMapProvider() {
        return new MapProvider();
    }

    @Provides
    public StorageStateProvider providesStorageStateProvider() {
        return new StorageStateProvider();
    }

    @Provides
    public StoragePathProvider providesStoragePathProvider() {
        return new StoragePathProvider();
    }

    @Provides
    public AdminPasswordProvider providesAdminPasswordProvider() {
        return new AdminPasswordProvider(AdminSharedPreferences.getInstance());
    }

    @Provides
    public CollectJobCreator providesCollectJobCreator() {
        return new CollectJobCreator();
    }

    @Provides
    public BackgroundWorkManager providesBackgroundWorkManager() {
        return new CollectBackgroundWorkManager();
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
    public Scheduler providesScheduler() {
        return new CoroutineScheduler();
    }

    @Singleton
    @Provides
    public ApplicationInitializer providesApplicationInitializer(Application application, CollectJobCreator collectJobCreator, PreferencesProvider preferencesProvider, UserAgentProvider userAgentProvider) {
        return new ApplicationInitializer(application, collectJobCreator, preferencesProvider.getMetaSharedPreferences(), userAgentProvider);
    }
}
