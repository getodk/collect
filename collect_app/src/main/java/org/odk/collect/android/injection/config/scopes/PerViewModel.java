package org.odk.collect.android.injection.config.scopes;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;

import javax.inject.Scope;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Anything that should persist during the lifetime of a ViewModel should use this Scope.
 * Dependencies that rely on the Activity Context should NOT use this scope, as a ViewModel can
 * outlive an Activity.
 */
@Scope
@Documented
@Retention(RUNTIME)
public @interface PerViewModel {
}
