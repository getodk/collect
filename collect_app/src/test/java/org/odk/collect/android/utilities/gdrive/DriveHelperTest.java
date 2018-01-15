package org.odk.collect.android.utilities.gdrive;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.Permission;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.FileOutputStream;
import java.io.IOException;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * @author Shobhit Agarwal
 */

@RunWith(PowerMockRunner.class)
@PrepareForTest(File.class)
public class DriveHelperTest {

    @Mock
    private DriveHelper.DriveService mockedDriveService;
    @Mock
    private Drive.Files.List mockedRequest;

    private DriveHelper driveHelper;

    @Before
    public void setup() {
        driveHelper = spy(new DriveHelper(mockedDriveService));
    }

    @Test
    public void getRootIdShouldReturnTheProperRootFolderId() throws IOException {
        String rootId = "root_id";

        doReturn(rootId).when(mockedDriveService).getFileId("root", "id");
        assertEquals(rootId, driveHelper.getRootFolderId());
    }

    @Test
    public void buildRequestTest() throws IOException {
        doReturn(mockedRequest).when(mockedDriveService).generateRequest(anyString(), anyString());

        assertNull(driveHelper.buildRequest(null, null));
        assertNull(driveHelper.buildRequest("some query", null));
        assertNull(driveHelper.buildRequest(null, "some fields"));
        assertNotNull(driveHelper.buildRequest("some query", "some fields"));
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
        String result = driveHelper.generateSearchQuery(null, null, null);
        assertNull(result);

        result = driveHelper.generateSearchQuery("sample-folder", null, null);
        assertEquals("name = 'sample-folder' and trashed = false", result);

        result = driveHelper.generateSearchQuery(null, "some-parent-id", null);
        assertEquals("'some-parent-id' in parents and trashed = false", result);

        result = driveHelper.generateSearchQuery(null, null, "xml-mime-type");
        assertEquals("mimeType = 'xml-mime-type' and trashed = false", result);

        result = driveHelper.generateSearchQuery("sample-folder", null, "xml-mime-type");
        assertEquals("name = 'sample-folder' and mimeType = 'xml-mime-type' and trashed = false", result);

        result = driveHelper.generateSearchQuery("sample-folder", "some-parent-id", "xml-mime-type");
        assertEquals("name = 'sample-folder' and 'some-parent-id' in parents and mimeType = 'xml-mime-type' and trashed = false", result);
    }

    @Test
    public void getFilesFromDriveTest() throws IOException {
        doReturn(mockedRequest).when(mockedDriveService).generateRequest(anyString(), anyString());

        driveHelper.getFilesFromDrive(anyString(), anyString());
        verify(mockedDriveService, times(1)).fetchAllFiles(any(Drive.Files.List.class), ArgumentMatchers.<File>anyList());

        clearInvocations(mockedDriveService);

        driveHelper.getFilesFromDrive(null, null);
        verify(mockedDriveService, times(0)).fetchAllFiles(any(Drive.Files.List.class), ArgumentMatchers.<File>anyList());
    }

    @Test
    public void createNewFileTest() {
        assertNotNull(driveHelper.createNewFile(anyString(), anyString(), anyString()));
        assertNotNull(driveHelper.createNewFile("file name", null, null));
    }

    @Test
    public void createFolderInDriveTest() throws IOException {
        File file = driveHelper.createNewFile("filename", DriveHelper.FOLDER_MIME_TYPE, "parentId");
        doReturn("new_folder_id").when(mockedDriveService).createFile(file, "id");

        String folderId = driveHelper.createFolderInDrive("filename", "parentId");
        assertEquals("new_folder_id", folderId);

        Permission permission = new Permission()
                .setType("anyone")
                .setRole("reader");

        verify(mockedDriveService, times(1)).setPermission("new_folder_id", "id", permission);
    }
}
