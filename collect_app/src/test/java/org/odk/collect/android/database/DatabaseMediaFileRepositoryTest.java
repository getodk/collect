package org.odk.collect.android.database;

import org.junit.Test;
import org.odk.collect.android.dao.FormsDao;
import org.odk.collect.android.utilities.FileUtil;

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
        when(formsDao.getFormMediaPath("1", "1")).thenReturn("/samplePath/");

        assertThat(new DatabaseMediaFileRepository(formsDao, new FileUtil()).getAll("1", "1").size(), is(0));
    }
}
