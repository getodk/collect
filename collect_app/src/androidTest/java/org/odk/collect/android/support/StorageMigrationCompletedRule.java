package org.odk.collect.android.support;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.odk.collect.android.storage.StorageStateProvider;
import org.odk.collect.android.storage.migration.StorageMigrationRepository;
import org.odk.collect.android.storage.migration.StorageMigrationResult;

import javax.inject.Singleton;

import dagger.Provides;

import static org.odk.collect.android.support.CollectHelpers.overrideAppDependencyModule;

public class StorageMigrationCompletedRule implements TestRule {

    @Override
    public Statement apply(Statement base, Description description) {
        return new StorageMigrationCompletedStatement(base);
    }

    private class StorageMigrationCompletedStatement extends Statement {

        private final Statement base;

        StorageMigrationCompletedStatement(Statement base) {
            this.base = base;
        }

        @Override
        public void evaluate() throws Throwable {
            overrideAppDependencyModule(new AppDependencyModule());
            new StorageStateProvider().enableUsingScopedStorage();
            base.evaluate();
        }
    }

    private static class AppDependencyModule extends org.odk.collect.android.injection.config.AppDependencyModule {

        @Provides
        @Singleton
        public StorageMigrationRepository providesStorageMigrationRepository() {
            StorageMigrationRepository storageMigrationRepository = new StorageMigrationRepository();
            storageMigrationRepository.setResult(StorageMigrationResult.SUCCESS);
            return storageMigrationRepository;
        }
    }
}
