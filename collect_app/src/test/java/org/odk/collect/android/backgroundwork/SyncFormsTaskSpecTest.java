package org.odk.collect.android.backgroundwork;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.analytics.Analytics;
import org.odk.collect.android.formmanagement.FormSourceProvider;
import org.odk.collect.android.formmanagement.FormUpdateChecker;
import org.odk.collect.android.formmanagement.matchexactly.SyncStatusAppState;
import org.odk.collect.android.injection.config.AppDependencyModule;
import org.odk.collect.android.itemsets.FastExternalItemsetsRepository;
import org.odk.collect.android.notifications.Notifier;
import org.odk.collect.android.preferences.source.SettingsProvider;
import org.odk.collect.android.storage.StoragePathProvider;
import org.odk.collect.android.support.CollectHelpers;
import org.odk.collect.android.utilities.FormsRepositoryProvider;
import org.odk.collect.android.utilities.InstancesRepositoryProvider;

import java.util.HashMap;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(AndroidJUnit4.class)
public class SyncFormsTaskSpecTest {

    private final FormUpdateChecker formUpdateChecker = mock(FormUpdateChecker.class);

    @Before
    public void setup() {
        CollectHelpers.overrideAppDependencyModule(new AppDependencyModule() {
            @Override
            public FormUpdateChecker providesFormUpdateChecker(Context context, Notifier notifier, Analytics analytics, ChangeLock changeLock, StoragePathProvider storagePathProvider, SettingsProvider settingsProvider, FormsRepositoryProvider formsRepositoryProvider, FormSourceProvider formSourceProvider, SyncStatusAppState syncStatusAppState, InstancesRepositoryProvider instancesRepositoryProvider, FastExternalItemsetsRepository fastExternalItemsetsRepository) {
                return formUpdateChecker;
            }
        });
    }

    @Test
    public void callsSynchronize() {
        new SyncFormsTaskSpec().getTask(ApplicationProvider.getApplicationContext(), new HashMap<>()).get();
        verify(formUpdateChecker).synchronizeWithServer();
    }
}
