package org.odk.collect.android.backgroundwork;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.dao.FormsDao;
import org.odk.collect.android.formmanagement.ServerFormDetails;
import org.odk.collect.android.formmanagement.ServerFormsDetailsFetcher;
import org.odk.collect.android.formmanagement.previouslydownloaded.ServerFormsUpdateChecker;
import org.odk.collect.android.forms.FormsRepository;
import org.odk.collect.android.injection.config.AppDependencyModule;
import org.odk.collect.android.openrosa.OpenRosaHttpInterface;
import org.odk.collect.android.preferences.GeneralKeys;
import org.odk.collect.android.preferences.PreferencesProvider;
import org.odk.collect.android.support.BooleanChangeLock;
import org.odk.collect.android.support.RobolectricHelpers;
import org.odk.collect.android.utilities.MultiFormDownloader;
import org.odk.collect.android.utilities.WebCredentialsUtils;
import org.robolectric.RobolectricTestRunner;

import java.util.function.Supplier;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class AutoUpdateTaskSpecTest {

    private final BooleanChangeLock changeLock = new BooleanChangeLock();
    private final MultiFormDownloader multiFormDownloader = mock(MultiFormDownloader.class);
    private final ServerFormsUpdateChecker serverFormsUpdateChecker = mock(ServerFormsUpdateChecker.class);
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
            public MultiFormDownloader providesMultiFormDownloader(FormsDao formsDao, OpenRosaHttpInterface openRosaHttpInterface, WebCredentialsUtils webCredentialsUtils) {
                return multiFormDownloader;
            }

            @Override
            public ServerFormsUpdateChecker providesServerFormUpdatesChecker(ServerFormsDetailsFetcher serverFormsDetailsFetcher, FormsRepository formsRepository) {
                return serverFormsUpdateChecker;
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
        });
    }

    @Test
    public void whenAutoDownloadEnabled_andChangeLockLocked_doesNotDownload() {
        when(serverFormsUpdateChecker.check()).thenReturn(asList(new ServerFormDetails("", "", "", "", "", "", "", false, true)));
        generalPrefs.edit().putBoolean(GeneralKeys.KEY_AUTOMATIC_UPDATE, true).apply();
        changeLock.lock();

        AutoUpdateTaskSpec taskSpec = new AutoUpdateTaskSpec();
        Supplier<Boolean> task = taskSpec.getTask(ApplicationProvider.getApplicationContext());
        task.get();

        verifyNoInteractions(multiFormDownloader);
    }
}