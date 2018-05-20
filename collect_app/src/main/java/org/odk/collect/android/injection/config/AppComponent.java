package org.odk.collect.android.injection.config;

import android.app.Application;

import org.odk.collect.android.activities.InstanceUploaderList;
import org.odk.collect.android.adapters.InstanceUploaderAdapter;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.fragments.DataManagerList;
import org.odk.collect.android.fragments.ShowQRCodeFragment;
import org.odk.collect.android.injection.ActivityBuilder;
import org.odk.collect.android.injection.config.scopes.PerApplication;
import org.odk.collect.android.preferences.BasePreferenceFragment;
import org.odk.collect.android.preferences.PreferencesActivity;
import org.odk.collect.android.preferences.ResetDialogPreference;
import org.odk.collect.android.receivers.NetworkReceiver;
import org.odk.collect.android.tasks.ServerPollingJob;
import org.odk.collect.android.tasks.sms.SmsNotificationReceiver;
import org.odk.collect.android.tasks.sms.SmsSender;
import org.odk.collect.android.tasks.sms.SmsSentBroadcastReceiver;
import org.odk.collect.android.tasks.sms.SmsService;
import org.odk.collect.android.utilities.gdrive.GoogleAccountsManager;
import org.odk.collect.android.widgets.QuestionWidget;

import dagger.BindsInstance;
import dagger.Component;
import dagger.android.AndroidInjector;
import dagger.android.support.AndroidSupportInjectionModule;

/**
 * Primary module, bootstraps the injection system and
 * injects the main Collect instance here.
 * <p>
 * Shouldn't be modified unless absolutely necessary.
 */
@PerApplication
@Component(modules = {
        AndroidSupportInjectionModule.class,
        AppModule.class,
        ActivityBuilder.class
})
public interface AppComponent extends AndroidInjector<Collect> {

    void inject(QuestionWidget questionWidget);

    void inject(NetworkReceiver networkReceiver);

    void inject(PreferencesActivity preferencesActivity);

    void inject(ShowQRCodeFragment showQRCodeFragment);

    void inject(GoogleAccountsManager googleAccountsManager);

    void inject(ServerPollingJob serverPollingJob);

    void inject(BasePreferenceFragment basePreferenceFragment);

    void inject(ResetDialogPreference resetDialogPreference);

    void inject(SmsService smsService);

    void inject(SmsSender smsSender);

    void inject(SmsSentBroadcastReceiver smsSentBroadcastReceiver);

    void inject(SmsNotificationReceiver smsNotificationReceiver);

    void inject(InstanceUploaderList instanceUploaderList);

    void inject(InstanceUploaderAdapter instanceUploaderAdapter);

    void inject(DataManagerList dataManagerList);

    @Component.Builder
    interface Builder {

        @BindsInstance
        Builder application(Application application);

        AppComponent build();
    }
}
