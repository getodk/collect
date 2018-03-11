package org.odk.collect.android.location.injection;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.inject.Qualifier;

public class Qualifiers {

    @Qualifier
    @Retention(RetentionPolicy.RUNTIME)
    public @interface IsDraggable {

    }

    @Qualifier
    @Retention(RetentionPolicy.RUNTIME)
    public @interface IsReadOnly {

    }

    @Qualifier
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Extras {

    }

    @Qualifier
    @Retention(RetentionPolicy.RUNTIME)
    public @interface InitialLocation {

    }

    @Qualifier
    @Retention(RetentionPolicy.RUNTIME)
    public @interface HasInitialLocation {

    }

    @Qualifier
    @Retention(RetentionPolicy.RUNTIME)
    public @interface ValidWithinMillis {

    }
}
