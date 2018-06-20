package org.odk.collect.android.http.mock;

import org.odk.collect.android.http.CollectServerClient;
import org.odk.collect.android.tasks.InstanceServerUploader;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = MockHttpInterfaceModule.class)
public interface MockHttpComponent {
    void inject(InstanceServerUploader uploader);
    void inject(CollectServerClient collectClient);
}
