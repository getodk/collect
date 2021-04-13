package org.odk.collect.formstest;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.odk.collect.forms.Form;
import org.odk.collect.forms.FormsRepository;
import org.odk.collect.shared.Md5;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.function.Supplier;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.odk.collect.formstest.FormUtils.buildForm;
import static org.odk.collect.formstest.FormUtils.createXFormBody;

public abstract class FormsRepositoryTest {

    public abstract FormsRepository buildSubject();

    public abstract FormsRepository buildSubject(Supplier<Long> clock);

    public abstract String getFormFilesPath();

    @Test
    public void getLatestByFormIdAndVersion_whenFormHasNullVersion_returnsForm() {
        FormsRepository formsRepository = buildSubject();
        formsRepository.save(FormUtils.buildForm("1", null, getFormFilesPath())
                .build());

        Form form = formsRepository.getLatestByFormIdAndVersion("1", null);
        MatcherAssert.assertThat(form, Matchers.notNullValue());
        MatcherAssert.assertThat(form.getDbId(), Matchers.is(1L));
    }

    @Test
    public void getLatestByFormIdAndVersion_whenMultipleExist_returnsLatest() {
        Supplier<Long> mockClock = Mockito.mock(Supplier.class);
        Mockito.when(mockClock.get()).thenReturn(2L, 3L, 1L);

        FormsRepository formsRepository = buildSubject(mockClock);
        formsRepository.save(FormUtils.buildForm("1", "1", getFormFilesPath())
                .build());
        formsRepository.save(FormUtils.buildForm("1", "1", getFormFilesPath())
                .build());
        formsRepository.save(FormUtils.buildForm("1", "1", getFormFilesPath())
                .build());

        Form form = formsRepository.getLatestByFormIdAndVersion("1", "1");
        MatcherAssert.assertThat(form, Matchers.notNullValue());
        MatcherAssert.assertThat(form.getDbId(), Matchers.is(2L));
    }

    @Test
    public void getAllByFormIdAndVersion_whenFormHasNullVersion_returnsAllMatchingForms() {
        FormsRepository formsRepository = buildSubject();
        formsRepository.save(FormUtils.buildForm("1", null, getFormFilesPath())
                .build());

        formsRepository.save(FormUtils.buildForm("1", null, getFormFilesPath())
                .build());

        formsRepository.save(FormUtils.buildForm("1", "7", getFormFilesPath())
                .build());

        List<Form> forms = formsRepository.getAllByFormIdAndVersion("1", null);
        MatcherAssert.assertThat(forms.size(), Matchers.is(2));
        MatcherAssert.assertThat(forms.get(0).getVersion(), Matchers.is(Matchers.nullValue()));
        MatcherAssert.assertThat(forms.get(1).getVersion(), Matchers.is(Matchers.nullValue()));
    }

    @Test
    public void getAllNotDeletedByFormId_doesNotReturnDeletedForms() {
        FormsRepository formsRepository = buildSubject();
        formsRepository.save(FormUtils.buildForm("1", "deleted", getFormFilesPath())
                .deleted(true)
                .build()
        );

        formsRepository.save(FormUtils.buildForm("1", "not-deleted", getFormFilesPath())
                .deleted(false)
                .build()
        );

        List<Form> forms = formsRepository.getAllNotDeletedByFormId("1");
        MatcherAssert.assertThat(forms.size(), Matchers.is(1));
        MatcherAssert.assertThat(forms.get(0).getVersion(), Matchers.equalTo("not-deleted"));
    }

    @Test
    public void getAllNotDeletedByFormIdAndVersion_onlyReturnsNotDeletedFormsThatMatchVersion() {
        FormsRepository formsRepository = buildSubject();
        formsRepository.save(FormUtils.buildForm("id", "1", getFormFilesPath())
                .deleted(true)
                .build()
        );
        formsRepository.save(FormUtils.buildForm("id", "1", getFormFilesPath())
                .deleted(false)
                .build()
        );

        formsRepository.save(FormUtils.buildForm("id", "2", getFormFilesPath())
                .deleted(true)
                .build()
        );
        formsRepository.save(FormUtils.buildForm("id", "2", getFormFilesPath())
                .deleted(false)
                .build()
        );

        List<Form> forms = formsRepository.getAllNotDeletedByFormIdAndVersion("id", "2");
        MatcherAssert.assertThat(forms.size(), Matchers.is(1));
        MatcherAssert.assertThat(forms.get(0).getVersion(), Matchers.equalTo("2"));
    }

    @Test
    public void softDelete_marksDeletedAsTrue() {
        FormsRepository formsRepository = buildSubject();
        formsRepository.save(FormUtils.buildForm("1", null, getFormFilesPath())
                .build());

        formsRepository.softDelete(1L);
        MatcherAssert.assertThat(formsRepository.get(1L).isDeleted(), Matchers.is(true));
    }

    @Test
    public void restore_marksDeletedAsFalse() {
        FormsRepository formsRepository = buildSubject();
        formsRepository.save(FormUtils.buildForm("1", null, getFormFilesPath())
                .deleted(true)
                .build());

        formsRepository.restore(1L);
        MatcherAssert.assertThat(formsRepository.get(1L).isDeleted(), Matchers.is(false));
    }

    @Test
    public void save_addsId() {
        FormsRepository formsRepository = buildSubject();
        Form form = FormUtils.buildForm("id", "version", getFormFilesPath()).build();

        formsRepository.save(form);
        MatcherAssert.assertThat(formsRepository.getAll().get(0).getDbId(), Matchers.notNullValue());
    }

    @Test
    public void save_addsMediaPath_whereMediaDirCanBeCreated() {
        FormsRepository formsRepository = buildSubject();
        Form form = FormUtils.buildForm("id", "version", getFormFilesPath()).build();
        MatcherAssert.assertThat(form.getFormMediaPath(), Matchers.equalTo(null));

        Form savedForm = formsRepository.save(form);
        MatcherAssert.assertThat(new File(savedForm.getFormMediaPath()).mkdir(), Matchers.is(true));
    }

    @Test
    public void save_addsHashBasedOnFormFile() {
        FormsRepository formsRepository = buildSubject();
        Form form = FormUtils.buildForm("id", "version", getFormFilesPath()).build();
        MatcherAssert.assertThat(form.getMD5Hash(), Matchers.equalTo(null));

        formsRepository.save(form);

        String expectedHash = Md5.getMd5Hash(new File(form.getFormFilePath()));
        MatcherAssert.assertThat(formsRepository.get(1L).getMD5Hash(), Matchers.equalTo(expectedHash));
    }

    @Test(expected = Exception.class)
    public void save_whenNoFormFilePath_explodes() {
        FormsRepository formsRepository = buildSubject();
        Form form = FormUtils.buildForm("id", "version", getFormFilesPath()).build();
        form = new Form.Builder(form)
                .formFilePath(null)
                .build();

        formsRepository.save(form);
    }

    @Test
    public void save_whenFormHasId_updatesExisting() {
        FormsRepository formsRepository = buildSubject();
        Form originalForm = formsRepository.save(FormUtils.buildForm("id", "version", getFormFilesPath())
                .displayName("original")
                .build());

        formsRepository.save(new Form.Builder(originalForm)
                .displayName("changed")
                .build());

        MatcherAssert.assertThat(formsRepository.get(originalForm.getDbId()).getDisplayName(), Matchers.is("changed"));
    }

    @Test
    public void save_whenFormHasId_updatesHash() throws IOException {
        FormsRepository formsRepository = buildSubject();
        Form originalForm = formsRepository.save(FormUtils.buildForm("id", "version", getFormFilesPath())
                .displayName("original")
                .build());

        String newFormBody = FormUtils.createXFormBody("id", "version", "A different title");
        File formFile = new File(originalForm.getFormFilePath());
        FileUtils.writeByteArrayToFile(formFile, newFormBody.getBytes());

        formsRepository.save(new Form.Builder(originalForm)
                .displayName("changed")
                .build());

        String expectedHash = Md5.getMd5Hash(formFile);
        MatcherAssert.assertThat(formsRepository.get(originalForm.getDbId()).getMD5Hash(), Matchers.is(expectedHash));
    }

    @Test
    public void delete_deletesFiles() throws Exception {
        FormsRepository formsRepository = buildSubject();
        Form form = formsRepository.save(FormUtils.buildForm("id", "version", getFormFilesPath()).build());

        // FormRepository doesn't automatically create all form files
        File mediaDir = new File(form.getFormMediaPath());
        mediaDir.mkdir();
        File cacheFile = new File(form.getJrCacheFilePath());
        cacheFile.createNewFile();

        File formFile = new File(form.getFormFilePath());
        MatcherAssert.assertThat(formFile.exists(), Matchers.is(true));
        MatcherAssert.assertThat(mediaDir.exists(), Matchers.is(true));
        MatcherAssert.assertThat(cacheFile.exists(), Matchers.is(true));

        formsRepository.delete(form.getDbId());
        MatcherAssert.assertThat(formFile.exists(), Matchers.is(false));
        MatcherAssert.assertThat(mediaDir.exists(), Matchers.is(false));
        MatcherAssert.assertThat(cacheFile.exists(), Matchers.is(false));
    }

    @Test
    public void delete_whenMediaPathIsFile_deletesFiles() throws Exception {
        FormsRepository formsRepository = buildSubject();
        Form form = formsRepository.save(FormUtils.buildForm("id", "version", getFormFilesPath()).build());

        // FormRepository currently doesn't manage media file path other than deleting it
        String mediaPath = form.getFormMediaPath();
        new File(mediaPath).createNewFile();

        File formFile = new File(form.getFormFilePath());
        File mediaDir = new File(form.getFormMediaPath());
        MatcherAssert.assertThat(formFile.exists(), Matchers.is(true));
        MatcherAssert.assertThat(mediaDir.exists(), Matchers.is(true));

        formsRepository.delete(1L);
        MatcherAssert.assertThat(formFile.exists(), Matchers.is(false));
        MatcherAssert.assertThat(mediaDir.exists(), Matchers.is(false));
    }

    @Test
    public void deleteAll_deletesAllForms() {
        FormsRepository formsRepository = buildSubject();
        formsRepository.save(FormUtils.buildForm("id1", "version", getFormFilesPath()).build());
        formsRepository.save(FormUtils.buildForm("id2", "version", getFormFilesPath()).build());

        List<Form> forms = formsRepository.getAll();

        formsRepository.deleteAll();
        MatcherAssert.assertThat(formsRepository.getAll().size(), Matchers.is(0));

        for (Form form : forms) {
            MatcherAssert.assertThat(new File(form.getFormFilePath()).exists(), Matchers.is(false));
            MatcherAssert.assertThat(new File(form.getFormMediaPath()).exists(), Matchers.is(false));
        }
    }

    @Test
    public void deleteByMd5Hash_deletesFormsWithMatchingHash() {
        FormsRepository formsRepository = buildSubject();
        formsRepository.save(FormUtils.buildForm("id1", "version", getFormFilesPath()).build());
        formsRepository.save(FormUtils.buildForm("id1", "version", getFormFilesPath()).build());
        formsRepository.save(FormUtils.buildForm("id2", "version", getFormFilesPath()).build());

        List<Form> id1Forms = formsRepository.getAllByFormIdAndVersion("id1", "version");
        MatcherAssert.assertThat(id1Forms.size(), Matchers.is(2));
        MatcherAssert.assertThat(id1Forms.get(0).getMD5Hash(), Matchers.is(id1Forms.get(1).getMD5Hash()));

        formsRepository.deleteByMd5Hash(id1Forms.get(0).getMD5Hash());
        MatcherAssert.assertThat(formsRepository.getAll().size(), Matchers.is(1));
        MatcherAssert.assertThat(formsRepository.getAll().get(0).getFormId(), Matchers.is("id2"));
    }

    @Test(expected = Exception.class)
    public void getOneByMd5Hash_whenHashIsNull_explodes() {
        buildSubject().getOneByMd5Hash(null);
    }

    @Test
    public void getOneByMd5Hash_returnsMatchingForm() {
        FormsRepository formsRepository = buildSubject();
        formsRepository.save(FormUtils.buildForm("id1", "version", getFormFilesPath()).build());
        Form form2 = formsRepository.save(FormUtils.buildForm("id2", "version", getFormFilesPath()).build());

        MatcherAssert.assertThat(formsRepository.getOneByMd5Hash(form2.getMD5Hash()), Matchers.is(form2));
    }

    @Test
    public void getOneByPath_returnsMatchingForm() {
        FormsRepository formsRepository = buildSubject();
        formsRepository.save(FormUtils.buildForm("id1", "version", getFormFilesPath()).build());

        Form form2 = FormUtils.buildForm("id2", "version", getFormFilesPath()).build();
        formsRepository.save(form2);

        MatcherAssert.assertThat(formsRepository.getOneByPath(form2.getFormFilePath()).getFormId(), Matchers.is("id2"));
    }

    @Test
    public void getAllFormId_returnsMatchingForms() {
        FormsRepository formsRepository = buildSubject();
        Form form1 = formsRepository.save(FormUtils.buildForm("id1", "version", getFormFilesPath()).build());
        Form form2 = formsRepository.save(FormUtils.buildForm("id1", "other_version", getFormFilesPath()).build());
        formsRepository.save(FormUtils.buildForm("id2", "version", getFormFilesPath()).build());

        List<Form> forms = formsRepository.getAllByFormId("id1");
        MatcherAssert.assertThat(forms.size(), Matchers.is(2));
        MatcherAssert.assertThat(forms, Matchers.contains(form1, form2));
    }
}
