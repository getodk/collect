package org.odk.collect.android.google;

import com.google.api.services.drive.Drive;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.FileOutputStream;
import java.io.IOException;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * @author Shobhit Agarwal
 */

@RunWith(PowerMockRunner.class)
public class DriveHelperTest {

    @Mock
    private DriveService mockedDriveService;
    @Mock
    private Drive.Files.List mockedRequest;

    private DriveHelper driveHelper;

    private void stubDriveService() throws IOException {

    }

    @Before
    public void setup() throws IOException {
        driveHelper = spy(new DriveHelper(mockedDriveService));
        stubDriveService();
    }

    @Test
    public void getRootIdTest() throws IOException {
        String rootId = "root_id";

        doReturn(rootId).when(mockedDriveService).getFileId("root", "id");
        assertEquals(rootId, driveHelper.getRootFolderId());
    }

    @Test
    public void buildRequestTest() throws IOException {
        String query = "some query";
        String fields = "some fields";
        doReturn(mockedRequest).when(mockedDriveService).generateRequest(anyString(), anyString());

        assertNull(driveHelper.buildRequest(null, null));
        assertNull(driveHelper.buildRequest(query, null));
        assertNull(driveHelper.buildRequest(null, fields));
        assertNotNull(driveHelper.buildRequest(query, fields));
    }

    @Test
    public void downloadFileTest() throws IOException {
        String fileId = "some_file_id";
        FileOutputStream fileOutputStream = mock(FileOutputStream.class);

        driveHelper.downloadFile(fileId, fileOutputStream);
        verify(mockedDriveService, times(1)).downloadFile(fileId, fileOutputStream);
    }

    @Test
    public void generateSearchQueryTest() {
        String folderName = "sample-folder";
        String parentId = "some-parent-id";
        String mimeType = "xml-mime-type";

        String result;

        result = driveHelper.generateSearchQuery(null, null, null);
        assertNull(result);

        result = driveHelper.generateSearchQuery(folderName, null, null);
        assertEquals("name = 'sample-folder' and trashed = false", result);

        result = driveHelper.generateSearchQuery(null, parentId, null);
        assertEquals("'some-parent-id' in parents and trashed = false", result);

        result = driveHelper.generateSearchQuery(null, null, mimeType);
        assertEquals("mimeType = 'xml-mime-type' and trashed = false", result);

        result = driveHelper.generateSearchQuery(folderName, null, mimeType);
        assertEquals("name = 'sample-folder' and mimeType = 'xml-mime-type' and trashed = false", result);

        result = driveHelper.generateSearchQuery(folderName, parentId, mimeType);
        assertEquals("name = 'sample-folder' and 'some-parent-id' in parents and mimeType = 'xml-mime-type' and trashed = false", result);
    }

    @Test
    public void getMediaDirNameTest() {
        String expected = "sample-file-media";

        assertEquals(expected, driveHelper.getMediaDirName("sample-file.xml"));
        assertEquals(expected, driveHelper.getMediaDirName("sample-file.extension"));
        assertEquals(expected, driveHelper.getMediaDirName("sample-file.123"));
        assertEquals(expected, driveHelper.getMediaDirName("sample-file.docx"));
    }
}
