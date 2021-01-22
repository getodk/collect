package org.odk.collect.android.forms;

import org.junit.Test;
import org.odk.collect.android.utilities.FileUtils;

import java.io.File;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.odk.collect.android.support.FormUtils.buildForm;

public abstract class FormsRepositoryTest {

    public abstract FormsRepository buildSubject();

    public abstract String getFormFilesPath();

    @Test
    public void getLatestByFormIdAndVersion_whenFormHasNullVersion_returnsForm() {
        FormsRepository formsRepository = buildSubject();
        formsRepository.save(buildForm("1", null, getFormFilesPath())
                .build());

        Form form = formsRepository.getLatestByFormIdAndVersion("1", null);
        assertThat(form, notNullValue());
        assertThat(form.getId(), is(1L));
    }

    @Test
    public void getLatestByFormIdAndVersion_whenMultipleExist_returnsLatest() throws InterruptedException {
        FormsRepository formsRepository = buildSubject();
        formsRepository.save(buildForm("1", "1", getFormFilesPath())
                .build());
        Thread.sleep(2); // ensure that the in-memory inserts don't all get the same timestamp
        formsRepository.save(buildForm("1", "1", getFormFilesPath())
                .build());
        Thread.sleep(2); // ensure that the in-memory inserts don't all get the same timestamp
        formsRepository.save(buildForm("1", "1", getFormFilesPath())
                .build());

        Form form = formsRepository.getLatestByFormIdAndVersion("1", "1");
        assertThat(form, notNullValue());
        assertThat(form.getId(), is(3L));
    }

    @Test
    public void getAllByFormIdAndVersion_whenFormHasNullVersion_returnsAllMatchingForms() {
        FormsRepository formsRepository = buildSubject();
        formsRepository.save(buildForm("1", null, getFormFilesPath())
                .build());

        formsRepository.save(buildForm("1", null, getFormFilesPath())
                .build());

        formsRepository.save(buildForm("1", "7", getFormFilesPath())
                .build());

        List<Form> forms = formsRepository.getAllByFormIdAndVersion("1", null);
        assertThat(forms.size(), is(2));
        assertThat(forms.get(0).getJrVersion(), is(nullValue()));
        assertThat(forms.get(1).getJrVersion(), is(nullValue()));
    }

    @Test
    public void getAllNotDeletedByFormId_doesNotReturnDeletedForms() {
        FormsRepository formsRepository = buildSubject();
        formsRepository.save(buildForm("1", "deleted", getFormFilesPath())
                .deleted(true)
                .build()
        );

        formsRepository.save(buildForm("1", "not-deleted", getFormFilesPath())
                .deleted(false)
                .build()
        );

        List<Form> forms = formsRepository.getAllNotDeletedByFormId("1");
        assertThat(forms.size(), is(1));
        assertThat(forms.get(0).getJrVersion(), equalTo("not-deleted"));
    }

    @Test
    public void getAllNotDeletedByFormIdAndVersion_onlyReturnsNotDeletedFormsThatMatchVersion() {
        FormsRepository formsRepository = buildSubject();
        formsRepository.save(buildForm("id", "1", getFormFilesPath())
                .deleted(true)
                .build()
        );
        formsRepository.save(buildForm("id", "1", getFormFilesPath())
                .deleted(false)
                .build()
        );

        formsRepository.save(buildForm("id", "2", getFormFilesPath())
                .deleted(true)
                .build()
        );
        formsRepository.save(buildForm("id", "2", getFormFilesPath())
                .deleted(false)
                .build()
        );

        List<Form> forms = formsRepository.getAllNotDeletedByFormIdAndVersion("id", "2");
        assertThat(forms.size(), is(1));
        assertThat(forms.get(0).getJrVersion(), equalTo("2"));
    }

    @Test
    public void softDelete_marksDeletedAsTrue() {
        FormsRepository formsRepository = buildSubject();
        formsRepository.save(buildForm("1", null, getFormFilesPath())
                .build());

        formsRepository.softDelete(1L);
        assertThat(formsRepository.get(1L).isDeleted(), is(true));
    }

    @Test
    public void restore_marksDeletedAsFalse() {
        FormsRepository formsRepository = buildSubject();
        formsRepository.save(buildForm("1", null, getFormFilesPath())
                .deleted(true)
                .build());

        formsRepository.restore(1L);
        assertThat(formsRepository.get(1L).isDeleted(), is(false));
    }

    @Test
    public void save_addsId() {
        FormsRepository formsRepository = buildSubject();
        Form form = buildForm("id", "version", getFormFilesPath()).build();

        formsRepository.save(form);
        assertThat(formsRepository.getAll().get(0).getId(), notNullValue());
    }

    @Test
    public void save_addsHashBasedOnFormFile() {
        FormsRepository formsRepository = buildSubject();
        Form form = buildForm("id", "version", getFormFilesPath()).build();
        assertThat(form.getMD5Hash(), equalTo(null));

        formsRepository.save(form);

        String expectedHash = FileUtils.getMd5Hash(new File(form.getFormFilePath()));
        assertThat(formsRepository.get(1L).getMD5Hash(), equalTo(expectedHash));
    }
}
