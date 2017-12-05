package org.odk.collect.android.injection.config.scopes;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;

import javax.inject.Scope;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Dependencies in this Scope will last as long as a single Activity does. Anything depending on
 * the Activity Context should be labeled with this Scope.
 */
@Scope
@Documented
@Retention(RUNTIME)
public @interface PerActivity {
}
