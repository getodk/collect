package org.odk.collect.android.utilities;

import android.support.annotation.NonNull;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.MockitoRule;
import org.odk.collect.android.utilities.tempinstance.ExistingInstanceChecker;
import org.odk.collect.android.utilities.tempinstance.MediaDeleter;
import org.odk.collect.android.utilities.tempinstance.TempInstanceRemover;
import org.odk.collect.android.utilities.tempinstance.TemporaryInstanceFileManager;

import java.io.File;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TempInstanceRemoverTest {

    @Rule
    public MockitoRule mockitoRule;

    @Mock
    private TemporaryInstanceFileManager temporaryInstanceFileManager;

    @Mock
    private ExistingInstanceChecker existingInstanceChecker;

    @Mock
    private MediaDeleter mediaDeleter;

    @InjectMocks
    private TempInstanceRemover tempInstanceRemover;

    @Mock
    private File instancePath;

    @Mock
    private File tempFile;

    @Mock
    private File instanceFolder;

    @Mock
    private File parentFile;

    @Mock
    private File listedFile;

    @Before
    public void setupMocks() {
        when(temporaryInstanceFileManager.getSavePointFileForInstancePath(instancePath))
                .thenReturn(tempFile);

        when(temporaryInstanceFileManager.getInstanceFolder(instancePath))
                .thenReturn(instanceFolder);

        when(instancePath.getParentFile()).thenReturn(parentFile);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    public void tempInstanceRemoveShouldDeleteInstanceFolderIfItExists() {
        // Mocked actions:
        when(tempFile.exists()).thenReturn(true);
        when(existingInstanceChecker.shouldDeleteExistingInstance(instancePath)).thenReturn(true);

        when(instanceFolder.exists()).thenReturn(true);
        when(instanceFolder.isDirectory()).thenReturn(true);
        when(instanceFolder.listFiles()).thenReturn(new File[] {listedFile});
        when(listedFile.exists()).thenReturn(true);

        // Actual method call:
        tempInstanceRemover.removeTempInstanceAtInstancePath(instancePath);

        // Verified actions:
        verify(tempFile, times(1)).delete();

        verify(mediaDeleter, times(1)).deleteAudioInFolderFromMediaProvider(parentFile);
        verify(mediaDeleter, times(1)).deleteImagesInFolderFromMediaProvider(parentFile);
        verify(mediaDeleter, times(1)).deleteVideoInFolderFromMediaProvider(parentFile);

        verify(listedFile, times(1)).delete();
        verify(instanceFolder, times(1)).delete();
    }
}
