package org.odk.collect.android.formmanagement;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.forms.Form;
import org.odk.collect.android.forms.FormsRepository;
import org.odk.collect.android.storage.StorageInitializer;
import org.odk.collect.android.storage.StoragePathProvider;
import org.odk.collect.android.storage.StorageSubdirectory;
import org.odk.collect.android.support.InMemFormsRepository;
import org.odk.collect.android.support.RobolectricHelpers;
import org.odk.collect.android.utilities.MultiFormDownloader;
import org.robolectric.RobolectricTestRunner;

import java.util.HashMap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.odk.collect.android.support.FormUtils.buildForm;

@RunWith(RobolectricTestRunner.class)
@SuppressWarnings("PMD.DoubleBraceInitialization")
public class ServerFormDownloaderTest {

    private final FormsRepository formsRepository = new InMemFormsRepository();

    private StoragePathProvider storagePathProvider;

    @Before
    public void setup() {
        RobolectricHelpers.mountExternalStorage();
        storagePathProvider = new StoragePathProvider();
        new StorageInitializer().createOdkDirsOnStorage();
    }

    @Test
    public void whenFormIsSoftDeleted_unDeletesForm() throws Exception {
        Form form = buildForm(1L, "deleted-form", "version", getFormFilesPath())
                .deleted(true)
                .build();
        formsRepository.save(form);

        ServerFormDetails serverFormDetails = new ServerFormDetails(
                form.getDisplayName(),
                "http://downloadUrl",
                null,
                form.getJrFormId(),
                form.getJrVersion(),
                form.getMD5Hash(),
                null,
                true,
                false
        );

        MultiFormDownloader multiFormDownloader = mock(MultiFormDownloader.class);
        HashMap<ServerFormDetails, String> results = new HashMap<ServerFormDetails, String>() {{
            put(serverFormDetails, Collect.getInstance().getString(R.string.success));
        }};
        when(multiFormDownloader.downloadForms(any(), any())).thenReturn(results);
        
        ServerFormDownloader downloader = new ServerFormDownloader(multiFormDownloader, formsRepository);
        downloader.downloadForm(serverFormDetails);
        assertThat(formsRepository.get(1L).isDeleted(), is(false));
    }

    public String getFormFilesPath() {
        return storagePathProvider.getDirPath(StorageSubdirectory.FORMS);
    }
}