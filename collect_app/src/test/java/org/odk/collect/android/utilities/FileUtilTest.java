package org.odk.collect.android.utilities;

import org.junit.Test;

import java.io.File;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FileUtilTest {

    @Test
    public void whenTryToListFilesOnNullFile_shouldReturnEmptyArray() {
        assertThat(new FileUtil().listFiles(null), is(empty()));
    }

    @Test
    public void whenTryToListFilesOnFileThatDoesNotExist_shouldReturnEmptyArray() {
        File file = mock(File.class);
        when(file.exists()).thenReturn(false);
        assertThat(new FileUtil().listFiles(file), is(empty()));
    }
}
