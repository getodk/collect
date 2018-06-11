package org.odk.collect.android.http;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = HttpInterfaceModule.class)
public interface HttpComponent {
    HttpInterface buildHttpInterface();
}
