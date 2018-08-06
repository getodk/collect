package org.odk.collect.android.http.injection;

import org.odk.collect.android.http.CollectServerClient;
import org.odk.collect.android.injection.config.AppModule;
import org.odk.collect.android.preferences.ServerPreferencesFragment;
import org.odk.collect.android.receivers.NetworkReceiver;
import org.odk.collect.android.tasks.InstanceServerUploader;
import org.odk.collect.android.utilities.DownloadFormListUtils;
import org.odk.collect.android.utilities.FormDownloader;

import javax.inject.Singleton;

import dagger.Component;

@Component(modules = AppModule.class)
@Singleton
public interface HttpComponent {
    void inject(InstanceServerUploader uploader);

    void inject(CollectServerClient collectClient);

    void inject(ServerPreferencesFragment serverPreferencesFragment);

    void inject(FormDownloader formDownloader);

    void inject(NetworkReceiver networkReceiver);

    void inject(DownloadFormListUtils downloadFormListUtils);
}
