package org.odk.collect.android.forms;

import org.junit.Test;
import org.odk.collect.android.utilities.FileUtils;

import java.io.File;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

public abstract class FormsRepositoryTest {

    public abstract FormsRepository buildSubject();

    public abstract String getFormFilesPath();

    @Test
    public void get_whenFormHasNullVersion_returnsForm() {
        FormsRepository formsRepository = buildSubject();
        formsRepository.save(buildForm(1L, "1", null)
                .build());

        Form form = formsRepository.get("1", null);
        assertThat(form, notNullValue());
        assertThat(form.getId(), is(1L));
    }

    @Test
    public void softDelete_marksDeletedAsTrue() {
        FormsRepository formsRepository = buildSubject();
        formsRepository.save(buildForm(1L, "1", null)
                .build());

        formsRepository.softDelete(1L);
        assertThat(formsRepository.get(1L).isDeleted(), is(true));
    }

    @Test
    public void restore_marksDeletedAsFalse() {
        FormsRepository formsRepository = buildSubject();
        formsRepository.save(buildForm(1L, "1", null)
                .deleted(true)
                .build());

        formsRepository.restore(1L);
        assertThat(formsRepository.get(1L).isDeleted(), is(false));
    }

    @Test
    public void getByJrFormIdNotDeleted_doesNotReturnDeletedForms() {
        FormsRepository formsRepository = buildSubject();
        formsRepository.save(buildForm(1L, "1", "deleted")
                .deleted(true)
                .build()
        );

        formsRepository.save(buildForm(2L, "1", "not-deleted")
                .deleted(false)
                .build()
        );

        List<Form> forms = formsRepository.getByJrFormIdNotDeleted("1");
        assertThat(forms.size(), is(1));
        assertThat(forms.get(0).getJrVersion(), equalTo("not-deleted"));
    }

    private Form.Builder buildForm(long id, String jrFormId, String jrVersion) {
        String fileName = jrFormId + "-" + jrVersion;
        File formFile = new File(getFormFilesPath() + "/" + fileName + ".xml");
        FileUtils.write(formFile, "blah".getBytes());
        String mediaPath = new File(getFormFilesPath() + "/" + fileName + "-media").getAbsolutePath();

        return new Form.Builder()
                .id(id)
                .displayName("Test Form")
                .formFilePath(formFile.getAbsolutePath())
                .formMediaPath(mediaPath)
                .jrFormId(jrFormId)
                .jrVersion(jrVersion);
    }
}