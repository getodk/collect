package org.odk.collect.android.backgroundwork;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.formmanagement.DiskFormsSynchronizer;
import org.odk.collect.android.formmanagement.FormDownloader;
import org.odk.collect.android.formmanagement.ServerFormDetails;
import org.odk.collect.android.formmanagement.ServerFormsDetailsFetcher;
import org.odk.collect.android.storage.StoragePathProvider;
import org.odk.collect.android.forms.FormsRepository;
import org.odk.collect.android.forms.MediaFileRepository;
import org.odk.collect.android.injection.config.AppDependencyModule;
import org.odk.collect.android.notifications.Notifier;
import org.odk.collect.android.forms.FormSource;
import org.odk.collect.android.forms.ManifestFile;
import org.odk.collect.android.preferences.GeneralKeys;
import org.odk.collect.android.preferences.PreferencesProvider;
import org.odk.collect.android.support.BooleanChangeLock;
import org.odk.collect.android.support.RobolectricHelpers;
import org.robolectric.RobolectricTestRunner;

import java.util.HashMap;
import java.util.function.Supplier;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@SuppressWarnings("PMD.DoubleBraceInitialization")
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
            public FormDownloader providesFormDownloader(FormSource formSource, FormsRepository formsRepository, StoragePathProvider storagePathProvider) {
                return formDownloader;
            }

            @Override
            public ServerFormsDetailsFetcher providesServerFormDetailsFetcher(FormsRepository formsRepository, MediaFileRepository mediaFileRepository, FormSource formSource, DiskFormsSynchronizer diskFormsSynchronizer) {
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

    @Test
    public void whenAutoDownloadEnabled_andDownloadIsCancelled_sendsCompletedDownloadsToNotifier() throws Exception {
        generalPrefs.edit().putBoolean(GeneralKeys.KEY_AUTOMATIC_UPDATE, true).apply();

        ServerFormDetails form1 = new ServerFormDetails("", "", "", "form1", "", "", false, true, new ManifestFile("", emptyList()));
        ServerFormDetails form2 = new ServerFormDetails("", "", "", "form2", "", "", false, true, new ManifestFile("", emptyList()));
        when(serverFormsDetailsFetcher.fetchFormDetails()).thenReturn(asList(form1, form2));

        // Cancel form download after downloading one form
        doAnswer(new Answer<Void>() {

            private boolean calledBefore;

            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                if (!calledBefore) {
                    calledBefore = true;
                } else {
                    throw new InterruptedException();
                }

                return null;
            }
        }).when(formDownloader).downloadForm(any(), any(), any());

        AutoUpdateTaskSpec taskSpec = new AutoUpdateTaskSpec();
        Supplier<Boolean> task = taskSpec.getTask(ApplicationProvider.getApplicationContext());
        task.get();

        verify(notifier).onUpdatesDownloaded(new HashMap<ServerFormDetails, String>() {
            {
                put(form1, Collect.getInstance().getString(R.string.success));
            }
        });
    }
}