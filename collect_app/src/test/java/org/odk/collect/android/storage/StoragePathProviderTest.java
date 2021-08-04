package org.odk.collect.android.storage;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.odk.collect.android.projects.CurrentProjectProvider;
import org.odk.collect.projects.Project;
import org.odk.collect.shared.TempFiles;

import java.io.File;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class StoragePathProviderTest {

    private final File root = TempFiles.createTempDir();
    private StoragePathProvider storagePathProvider;

    @Before
    public void setup() {
        CurrentProjectProvider currentProjectProvider = mock(CurrentProjectProvider.class);
        when(currentProjectProvider.getCurrentProject()).thenReturn(new Project.Saved("123", "Project", "D", "#ffffff"));

        storagePathProvider = new StoragePathProvider(currentProjectProvider, root.getAbsolutePath());
    }

    @After
    public void teardown() {
        root.delete();
    }

    @Test
    public void getStorageRootDirPath_returnsRoot() {
        assertThat(storagePathProvider.getOdkRootDirPath(), is(root.getAbsolutePath()));
    }

    @Test
    public void getOdkDirPath_withForms_returnsFormsDirForCurrentProject() {
        assertThat(storagePathProvider.getOdkDirPath(StorageSubdirectory.FORMS), is(root.getAbsolutePath() + "/projects/123/forms"));
    }

    @Test
    public void getOdkDirPath_withInstances_returnsInstancesDirForCurrentProject() {
        assertThat(storagePathProvider.getOdkDirPath(StorageSubdirectory.INSTANCES), is(root.getAbsolutePath() + "/projects/123/instances"));
    }

    @Test
    public void getOdkDirPath_withMetadata_returnsMetadataDirForCurrentProject() {
        assertThat(storagePathProvider.getOdkDirPath(StorageSubdirectory.METADATA), is(root.getAbsolutePath() + "/projects/123/metadata"));
    }

    @Test
    public void getOdkDirPath_withCache_returnsCacheDirForCurrentProject() {
        assertThat(storagePathProvider.getOdkDirPath(StorageSubdirectory.CACHE), is(root.getAbsolutePath() + "/projects/123/.cache"));
    }

    @Test
    public void getOdkDirPath_withLayers_returnsLayersDirForCurrentProject() {
        assertThat(storagePathProvider.getOdkDirPath(StorageSubdirectory.LAYERS), is(root.getAbsolutePath() + "/projects/123/layers"));
    }

    @Test
    public void getOdkDirPath_withSettings_returnsSettingsDirForCurrentProject() {
        assertThat(storagePathProvider.getOdkDirPath(StorageSubdirectory.SETTINGS), is(root.getAbsolutePath() + "/projects/123/settings"));
    }

    @Test
    public void getProjectRootDirPath_whenDirDoesNotExist_createsIt() {
        String projectRootDirPath = storagePathProvider.getProjectRootDirPath("projectId");
        assertThat(new File(projectRootDirPath).exists(), is(true));
    }
}
