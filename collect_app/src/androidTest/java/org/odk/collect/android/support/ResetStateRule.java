package org.odk.collect.android.support;

import static org.apache.commons.io.FileUtils.deleteDirectory;
import static org.odk.collect.androidshared.data.AppStateKt.getState;

import android.app.Application;
import android.content.Context;

import androidx.preference.PreferenceManager;
import androidx.test.core.app.ApplicationProvider;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.odk.collect.android.TestSettingsProvider;
import org.odk.collect.android.database.DatabaseConnection;
import org.odk.collect.android.injection.DaggerUtils;
import org.odk.collect.android.injection.config.AppDependencyComponent;
import org.odk.collect.android.injection.config.AppDependencyModule;
import org.odk.collect.android.preferences.source.SettingsProvider;
import org.odk.collect.android.storage.StoragePathProvider;
import org.odk.collect.android.utilities.MultiClickGuard;
import org.odk.collect.android.views.DecoratedBarcodeView;
import org.odk.collect.androidshared.ui.ToastUtils;

import java.io.File;
import java.io.IOException;

public class ResetStateRule implements TestRule {

    private final AppDependencyModule appDependencyModule;
    private final SettingsProvider settingsProvider = TestSettingsProvider.getSettingsProvider();

    public ResetStateRule() {
        this(null);
    }

    public ResetStateRule(AppDependencyModule appDependencyModule) {
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
            Application application = ApplicationProvider.getApplicationContext();

            resetDagger();
            clearPrefs(application);
            clearDisk();
            clearAppState(application);
            setTestState();

            AppDependencyComponent component = DaggerUtils.getComponent(application.getApplicationContext());

            // Reinitialize any application state with new deps/state
            component.applicationInitializer().initialize();
            base.evaluate();
        }
    }

    private void clearAppState(Application application) {
        getState(application).clear();
    }

    private void setTestState() {
        MultiClickGuard.test = true;
        DecoratedBarcodeView.test = true;
        CopyFormRule.projectCreated = false;
        ToastUtils.setRecordToasts(true);
    }

    private void clearDisk() {
        try {
            StoragePathProvider storagePathProvider = new StoragePathProvider();
            deleteDirectory(new File(storagePathProvider.getOdkRootDirPath()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        DatabaseConnection.closeAll();
    }

    private void resetDagger() {
        if (appDependencyModule == null) {
            CollectHelpers.overrideAppDependencyModule(new AppDependencyModule());
        } else {
            CollectHelpers.overrideAppDependencyModule(appDependencyModule);
        }
    }

    private void clearPrefs(Context context) {
        settingsProvider.clearAll();

        // Delete legacy prefs in case older version of app was run on test device
        PreferenceManager.getDefaultSharedPreferences(context).edit().clear().apply();
        context.getSharedPreferences("admin_prefs", Context.MODE_PRIVATE).edit().clear().apply();
    }
}
