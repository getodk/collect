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
    public void getProjectRootDirPath_returnsAndCreatesDirForProject() {
        String path = storagePathProvider.getProjectRootDirPath("projectId");
        assertThat(path, is(root.getAbsolutePath() + "/projects/projectId"));
        assertThat(new File(path).exists(), is(true));
    }

    @Test
    public void getOdkDirPath_withForms_returnsAndCreatesFormsDirForCurrentProject() {
        String path = storagePathProvider.getOdkDirPath(StorageSubdirectory.FORMS);
        assertThat(path, is(root.getAbsolutePath() + "/projects/123/forms"));
        assertThat(new File(path).exists(), is(true));
    }

    @Test
    public void getOdkDirPath_withInstances_returnsAndCreatesInstancesDirForCurrentProject() {
        String path = storagePathProvider.getOdkDirPath(StorageSubdirectory.INSTANCES);
        assertThat(path, is(root.getAbsolutePath() + "/projects/123/instances"));
        assertThat(new File(path).exists(), is(true));
    }

    @Test
    public void getOdkDirPath_withMetadata_returnsAndCreatesMetadataDirForCurrentProject() {
        String path = storagePathProvider.getOdkDirPath(StorageSubdirectory.METADATA);
        assertThat(path, is(root.getAbsolutePath() + "/projects/123/metadata"));
        assertThat(new File(path).exists(), is(true));
    }

    @Test
    public void getOdkDirPath_withCache_returnsAndCreatesCacheDirForCurrentProject() {
        String path = storagePathProvider.getOdkDirPath(StorageSubdirectory.CACHE);
        assertThat(path, is(root.getAbsolutePath() + "/projects/123/.cache"));
        assertThat(new File(path).exists(), is(true));
    }

    @Test
    public void getOdkDirPath_withLayers_returnsAndCreatesLayersDirForCurrentProject() {
        String path = storagePathProvider.getOdkDirPath(StorageSubdirectory.LAYERS);
        assertThat(path, is(root.getAbsolutePath() + "/projects/123/layers"));
        assertThat(new File(path).exists(), is(true));
    }

    @Test
    public void getOdkDirPath_withSettings_returnsAndCreatesSettingsDirForCurrentProject() {
        String path = storagePathProvider.getOdkDirPath(StorageSubdirectory.SETTINGS);
        assertThat(path, is(root.getAbsolutePath() + "/projects/123/settings"));
        assertThat(new File(path).exists(), is(true));
    }
}
