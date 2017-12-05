package org.odk.collect.android.injection.config.scopes;


import java.lang.annotation.Documented;
import java.lang.annotation.Retention;

import javax.inject.Scope;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Defines the parent (Application wide) scope.
 * Only dependencies that should persist during the lifetime of the Application (e.g. those that
 * depend on the Application Context, or live inside the Application object) should use this Scope.
 */
@Scope
@Documented
@Retention(RUNTIME)
public @interface PerApplication {
}
