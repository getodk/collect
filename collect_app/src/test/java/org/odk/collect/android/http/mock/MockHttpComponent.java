package org.odk.collect.android.http.mock;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = MockHttpInterfaceModule.class)
public interface MockHttpComponent {
    /**
     * TODO: Write injectors for test code
     */
}
