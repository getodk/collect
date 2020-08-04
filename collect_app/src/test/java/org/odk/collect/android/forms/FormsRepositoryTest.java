package org.odk.collect.android.forms;

import org.junit.Test;
import org.odk.collect.android.utilities.FileUtils;

import java.io.File;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

public abstract class FormsRepositoryTest {

    public abstract FormsRepository buildSubject();

    public abstract String getFormFilesPath();

    @Test
    public void get_whenFormHasNullVersion_returnsForm() {
        FormsRepository formsRepository = buildSubject();

        File formFile = new File(getFormFilesPath() + "/form.xml");
        FileUtils.write(formFile, "blah".getBytes());
        String mediaPath = new File(getFormFilesPath() + "/form-media").getAbsolutePath();

        formsRepository.save(new Form.Builder()
                .id(1L)
                .displayName("Test Form")
                .formFilePath(formFile.getAbsolutePath())
                .formMediaPath(mediaPath)
                .jrFormId("1")
                .jrVersion(null)
                .build()
        );

        Form form = formsRepository.get("1", null);
        assertThat(form, notNullValue());
        assertThat(form.getId(), is(1L));
    }
}