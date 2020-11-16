package org.odk.collect.android.widgets.utilities;

import android.content.Context;
import android.net.Uri;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.io.File;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class FileWidgetUtilsTest {
    private final Context context = ApplicationProvider.getApplicationContext();

    private File file;

    @Before
    public void setUp() {
        file = mock(File.class);
        when(file.exists()).thenReturn(true);
    }

    @Test
    public void getFile_whenObjectIsNotOfRequiredType_returnsNull() {
        assertNull(FileWidgetUtils.getFile(context, "blah"));
    }

    @Test
    public void getFile_whenObjectIsOfFileType_returnsFile() {
        assertThat(FileWidgetUtils.getFile(context, file), is(file));
    }

    @Test
    public void getFile_whenObjectIsOfUriType_returnsFile() {
        assertThat(FileWidgetUtils.getFile(context, Uri.fromFile(file)), is(file));
    }
}
