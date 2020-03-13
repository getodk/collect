package org.odk.collect.android.support;

import android.content.Context;
import android.preference.PreferenceManager;

import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.injection.config.AppDependencyModule;
import org.odk.collect.android.preferences.MetaSharedPreferencesProvider;
import org.odk.collect.android.storage.StorageStateProvider;
import org.odk.collect.android.utilities.ResetUtility;

import java.util.Arrays;
import java.util.List;

import static org.odk.collect.android.preferences.AdminPreferencesFragment.ADMIN_PREFERENCES;

public class ResetStateRule implements TestRule {

    private final boolean useScopedStorage;
    private AppDependencyModule appDependencyModule;

    public ResetStateRule() {
        this(false, null);
    }

    public ResetStateRule(boolean useScopedStorage) {
        this(useScopedStorage, null);
    }

    public ResetStateRule(AppDependencyModule appDependencyModule) {
        this(false, appDependencyModule);
    }

    public ResetStateRule(boolean useScopedStorage, AppDependencyModule appDependencyModule) {
        this.useScopedStorage = useScopedStorage;
        this.appDependencyModule = appDependencyModule;
    }

    @Override
    public Statement apply(final Statement base, Description description) {
        return new ResetStateStatement(base);
    }

    private class ResetStateStatement extends Statement {

        private final Statement base;

        ResetStateStatement(Statement base) {
            this.base = base;
        }

        @Override
        public void evaluate() throws Throwable {
            // Reset any singleton state
            if (appDependencyModule != null) {
                CollectHelpers.overrideAppDependencyModule(appDependencyModule);
            } else {
                CollectHelpers.overrideAppDependencyModule(new AppDependencyModule());
            }

            // Make sure we clear all our shared prefs - ignore logic that doesn't reset keys
            Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
            PreferenceManager.getDefaultSharedPreferences(context).edit().clear().commit();
            context.getSharedPreferences(ADMIN_PREFERENCES, 0).edit().clear().commit();
            new MetaSharedPreferencesProvider(context).getMetaSharedPreferences().edit().clear().commit();

            // Reset the app in both the old and new storage locations (just nuke dirs)
            List<Integer> resetActions = Arrays.asList(
                    ResetUtility.ResetAction.RESET_PREFERENCES,
                    ResetUtility.ResetAction.RESET_INSTANCES,
                    ResetUtility.ResetAction.RESET_FORMS,
                    ResetUtility.ResetAction.RESET_LAYERS,
                    ResetUtility.ResetAction.RESET_CACHE,
                    ResetUtility.ResetAction.RESET_OSM_DROID
            );

            new StorageStateProvider().disableUsingScopedStorage();
            new ResetUtility().reset(context, resetActions);
            new StorageStateProvider().enableUsingScopedStorage();
            new ResetUtility().reset(context, resetActions);

            // Setup storage location for tests
            if (useScopedStorage) {
                new StorageStateProvider().enableUsingScopedStorage();
            } else {
                new StorageStateProvider().disableUsingScopedStorage();
            }

            // Any dependencies (PropertyManager for instance) will already have been
            // passed to JavaRosa so make sure everything is reset
            ((Collect) context.getApplicationContext()).initializeJavaRosa();

            base.evaluate();
        }
    }

}
