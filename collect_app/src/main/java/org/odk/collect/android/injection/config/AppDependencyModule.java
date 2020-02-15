package org.odk.collect.android.injection.config;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.webkit.MimeTypeMap;

import org.javarosa.core.reference.ReferenceManager;
import org.odk.collect.android.analytics.Analytics;
import org.odk.collect.android.analytics.FirebaseAnalytics;
import org.odk.collect.android.dao.FormsDao;
import org.odk.collect.android.dao.InstancesDao;
import org.odk.collect.android.events.RxEventBus;
import org.odk.collect.android.formentry.media.AudioHelperFactory;
import org.odk.collect.android.formentry.media.ScreenContextAudioHelperFactory;
import org.odk.collect.android.metadata.InstallIDProvider;
import org.odk.collect.android.metadata.SharedPreferencesInstallIDProvider;
import org.odk.collect.android.openrosa.CollectThenSystemContentTypeMapper;
import org.odk.collect.android.openrosa.OpenRosaAPIClient;
import org.odk.collect.android.openrosa.OpenRosaHttpInterface;
import org.odk.collect.android.openrosa.okhttp.OkHttpConnection;
import org.odk.collect.android.openrosa.okhttp.OkHttpOpenRosaServerClientProvider;
import org.odk.collect.android.tasks.sms.SmsSubmissionManager;
import org.odk.collect.android.tasks.sms.contracts.SmsSubmissionManagerContract;
import org.odk.collect.android.utilities.ActivityAvailability;
import org.odk.collect.android.utilities.AndroidUserAgent;
import org.odk.collect.android.utilities.DeviceDetailsProvider;
import org.odk.collect.android.utilities.FormListDownloader;
import org.odk.collect.android.utilities.PermissionUtils;
import org.odk.collect.android.utilities.WebCredentialsUtils;
import org.odk.collect.utilities.UserAgentProvider;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;

import static org.odk.collect.android.preferences.GeneralKeys.KEY_INSTALL_ID;

/**
 * Add dependency providers here (annotated with @Provides)
 * for objects you need to inject
 */
@Module
public class AppDependencyModule {

    @Provides
    public SmsManager provideSmsManager() {
        return SmsManager.getDefault();
    }

    @Provides
    SmsSubmissionManagerContract provideSmsSubmissionManager(Application application) {
        return new SmsSubmissionManager(application);
    }

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
    public UserAgentProvider providesUserAgent() {
        return new AndroidUserAgent();
    }

    @Provides
    @Singleton
    OpenRosaHttpInterface provideHttpInterface(MimeTypeMap mimeTypeMap, UserAgentProvider userAgentProvider) {
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
    public Analytics providesAnalytics(Application application) {
        com.google.firebase.analytics.FirebaseAnalytics firebaseAnalyticsInstance = com.google.firebase.analytics.FirebaseAnalytics.getInstance(application);
        FirebaseAnalytics firebaseAnalytics = new FirebaseAnalytics(firebaseAnalyticsInstance);

        return new Analytics() {
            @Override
            public void logEvent(String category, String action) {
                firebaseAnalytics.logEvent(category, action);
            }

            @Override
            public void logEvent(String category, String action, String label) {
                firebaseAnalytics.logEvent(category, action, label);
            }
        };
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
    InstallIDProvider providesInstallIDProvider(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return new SharedPreferencesInstallIDProvider(preferences, KEY_INSTALL_ID);
    }

    @Provides
    public DeviceDetailsProvider providesDeviceDetailsProvider(Context context) {
        TelephonyManager telMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        return new DeviceDetailsProvider() {

            @Override
            @SuppressLint("MissingPermission")
            public String getDeviceId() {
                return telMgr.getDeviceId();
            }

            @Override
            @SuppressLint("MissingPermission")
            public String getLine1Number() {
                return telMgr.getLine1Number();
            }

            @Override
            @SuppressLint("MissingPermission")
            public String getSubscriberId() {
                return telMgr.getSubscriberId();
            }

            @Override
            @SuppressLint("MissingPermission")
            public String getSimSerialNumber() {
                return telMgr.getSimSerialNumber();
            }
        };
    }
}
