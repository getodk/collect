package org.odk.collect.android.database;

import org.junit.Test;
import org.odk.collect.android.dao.FormsDao;
import org.odk.collect.android.utilities.FileUtil;

import java.io.File;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DatabaseMediaFileRepositoryTest {

    @Test
    public void whenThereIsNoFormForgivenFormIdAndVersion_shouldGetAllMethodReturnEmptyArray() {
        FormsDao formsDao = mock(FormsDao.class);
        when(formsDao.getFormMediaPath("1", "1")).thenReturn(null);

        assertThat(new DatabaseMediaFileRepository(formsDao, new FileUtil()).getAll("1", "1").size(), is(0));
    }

    @Test
    public void whenMediaFolderFileDoesNotExist_shouldGetAllMethodReturnEmptyArray() {
        FormsDao formsDao = mock(FormsDao.class);
        FileUtil fileUtil = mock(FileUtil.class);
        File file = mock(File.class);
        String formMediaPath = "/samplePath/";

        when(formsDao.getFormMediaPath("1", "1")).thenReturn(formMediaPath);
        when(fileUtil.getFileAtPath(formMediaPath)).thenReturn(file);
        when(file.exists()).thenReturn(false);

        assertThat(new DatabaseMediaFileRepository(formsDao, new FileUtil()).getAll("1", "1").size(), is(0));
    }

    @Test
    public void whenMediaFolderFileIsNull_shouldGetAllMethodReturnEmptyArray() {
        FormsDao formsDao = mock(FormsDao.class);
        FileUtil fileUtil = mock(FileUtil.class);
        String formMediaPath = "/samplePath/";

        when(formsDao.getFormMediaPath("1", "1")).thenReturn(formMediaPath);
        when(fileUtil.getFileAtPath(formMediaPath)).thenReturn(null);

        assertThat(new DatabaseMediaFileRepository(formsDao, new FileUtil()).getAll("1", "1").size(), is(0));
    }
}
