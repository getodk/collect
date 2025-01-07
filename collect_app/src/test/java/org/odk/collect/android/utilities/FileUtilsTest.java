package org.odk.collect.android.utilities;

import org.hamcrest.Matchers;
import org.junit.Test;

import java.io.File;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FileUtilsTest {

    @Test
    public void mediaDirNameIsCorrect() {
        String expected = "sample-file-media";

        assertEquals(expected, FileUtils.constructMediaPath("sample-file.xml"));
        assertEquals(expected, FileUtils.constructMediaPath("sample-file.extension"));
        assertEquals(expected, FileUtils.constructMediaPath("sample-file.123"));
        assertEquals(expected, FileUtils.constructMediaPath("sample-file.docx"));
    }

    @Test
    @SuppressWarnings("PMD.DoNotHardCodeSDCard")
    public void simplifyScopedStoragePathTest() {
        assertThat(FileUtils.expandAndroidStoragePath(null), is(nullValue()));
        assertThat(FileUtils.expandAndroidStoragePath(""), is(""));
        assertThat(FileUtils.expandAndroidStoragePath("blahblahblah"), is("blahblahblah"));
        assertThat(FileUtils.expandAndroidStoragePath("/storage/emulated/0/Android/data/org.odk.collect.android/files/layers"), is("/sdcard/Android/data/org.odk.collect.android/files/layers"));
        assertThat(FileUtils.expandAndroidStoragePath("/storage/emulated/0/Android/data/org.odk.collect.android/files/layers/countries/countries-raster.mbtiles"), is("/sdcard/Android/data/org.odk.collect.android/files/layers/countries/countries-raster.mbtiles"));
    }

    @Test
    public void whenTryToListFilesOnNullFile_shouldReturnEmptyArray() {
        assertThat(FileUtils.listFiles(null), Matchers.is(empty()));
    }

    @Test
    public void whenTryToListFilesOnFileThatDoesNotExist_shouldReturnEmptyArray() {
        File file = mock(File.class);
        when(file.exists()).thenReturn(false);
        assertThat(FileUtils.listFiles(file), Matchers.is(empty()));
    }
}
