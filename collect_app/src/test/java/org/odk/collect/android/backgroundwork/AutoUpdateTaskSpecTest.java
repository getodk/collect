package org.odk.collect.android.backgroundwork;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.formmanagement.DiskFormsSynchronizer;
import org.odk.collect.android.formmanagement.FormDownloader;
import org.odk.collect.android.formmanagement.ServerFormDetails;
import org.odk.collect.android.formmanagement.ServerFormsDetailsFetcher;
import org.odk.collect.android.storage.StoragePathProvider;
import org.odk.collect.forms.FormsRepository;
import org.odk.collect.forms.MediaFileRepository;
import org.odk.collect.android.injection.config.AppDependencyModule;
import org.odk.collect.android.notifications.Notifier;
import org.odk.collect.server.FormListApi;
import org.odk.collect.server.ManifestFile;
import org.odk.collect.android.preferences.GeneralKeys;
import org.odk.collect.android.preferences.PreferencesProvider;
import org.odk.collect.android.support.BooleanChangeLock;
import org.odk.collect.android.support.RobolectricHelpers;
import org.robolectric.RobolectricTestRunner;

import java.util.function.Supplier;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class AutoUpdateTaskSpecTest {

    private final BooleanChangeLock changeLock = new BooleanChangeLock();
    private final FormDownloader formDownloader = mock(FormDownloader.class);
    private final ServerFormsDetailsFetcher serverFormsDetailsFetcher = mock(ServerFormsDetailsFetcher.class);
    private final Notifier notifier = mock(Notifier.class);
    private SharedPreferences generalPrefs;

    @Before
    public void setup() {
        generalPrefs = ApplicationProvider.getApplicationContext().getSharedPreferences("test", Context.MODE_PRIVATE);

        RobolectricHelpers.overrideAppDependencyModule(new AppDependencyModule() {
            @Override
            public ChangeLock providesFormsChangeLock() {
                return changeLock;
            }

            @Override
            public FormDownloader providesFormDownloader(FormListApi formListApi, FormsRepository formsRepository, StoragePathProvider storagePathProvider) {
                return formDownloader;
            }

            @Override
            public ServerFormsDetailsFetcher providesServerFormDetailsFetcher(FormsRepository formsRepository, MediaFileRepository mediaFileRepository, FormListApi formListAPI, DiskFormsSynchronizer diskFormsSynchronizer) {
                return serverFormsDetailsFetcher;
            }

            @Override
            public PreferencesProvider providesPreferencesProvider(Context context) {
                return new PreferencesProvider(context) {
                    @Override
                    public SharedPreferences getGeneralSharedPreferences() {
                        return generalPrefs;
                    }
                };
            }

            @Override
            public Notifier providesNotifier(Application application, PreferencesProvider preferencesProvider) {
                return notifier;
            }
        });
    }

    @Test
    public void whenThereAreUpdatedFormsOnServer_sendsUpdatesToNotifier() throws Exception {
        ServerFormDetails updatedForm = new ServerFormDetails("", "", "", "", "", "", false, true, new ManifestFile("", emptyList()));
        ServerFormDetails oldForm = new ServerFormDetails("", "", "", "", "", "", false, false, new ManifestFile("", emptyList()));
        when(serverFormsDetailsFetcher.fetchFormDetails()).thenReturn(asList(
                updatedForm,
                oldForm
        ));

        AutoUpdateTaskSpec taskSpec = new AutoUpdateTaskSpec();
        Supplier<Boolean> task = taskSpec.getTask(ApplicationProvider.getApplicationContext());
        task.get();

        verify(notifier).onUpdatesAvailable(asList(updatedForm));
    }

    @Test
    public void whenAutoDownloadEnabled_andChangeLockLocked_doesNotDownload() throws Exception {
        when(serverFormsDetailsFetcher.fetchFormDetails()).thenReturn(asList(new ServerFormDetails("", "", "", "", "", "", false, true, new ManifestFile("", emptyList()))));
        generalPrefs.edit().putBoolean(GeneralKeys.KEY_AUTOMATIC_UPDATE, true).apply();
        changeLock.lock();

        AutoUpdateTaskSpec taskSpec = new AutoUpdateTaskSpec();
        Supplier<Boolean> task = taskSpec.getTask(ApplicationProvider.getApplicationContext());
        task.get();

        verifyNoInteractions(formDownloader);
    }
}