package org.odk.collect.android.storage;

import org.junit.Before;
import org.junit.Test;
import org.odk.collect.android.projects.CurrentProjectProvider;
import org.odk.collect.projects.Project;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayWithSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class StoragePathProviderTest {

    private StoragePathProvider storagePathProvider;

    @Before
    public void setup() {
        CurrentProjectProvider currentProjectProvider = mock(CurrentProjectProvider.class);
        when(currentProjectProvider.getCurrentProject()).thenReturn(new Project.Saved("123", "Project", "D", "#ffffff"));

        storagePathProvider = new StoragePathProvider(currentProjectProvider, "/root");
    }

    @Test
    public void getStorageRootDirWithScopedStorageTest() {
        assertThat(storagePathProvider.getOdkRootDirPath(), is("/root"));
    }

    @Test
    public void getFormsDirWithScopedStorageTest() {
        assertThat(storagePathProvider.getOdkDirPath(StorageSubdirectory.FORMS), is("/root/projects/123/forms"));
    }

    @Test
    public void getInstancesDirWithScopedStorageTest() {
        assertThat(storagePathProvider.getOdkDirPath(StorageSubdirectory.INSTANCES), is("/root/projects/123/instances"));
    }

    @Test
    public void getMetadataDirWithScopedStorageTest() {
        assertThat(storagePathProvider.getOdkDirPath(StorageSubdirectory.METADATA), is("/root/projects/123/metadata"));
    }

    @Test
    public void getCacheDirWithScopedStorageTest() {
        assertThat(storagePathProvider.getOdkDirPath(StorageSubdirectory.CACHE), is("/root/projects/123/.cache"));
    }

    @Test
    public void getLayersDirWithScopedStorageTest() {
        assertThat(storagePathProvider.getOdkDirPath(StorageSubdirectory.LAYERS), is("/root/projects/123/layers"));
    }

    @Test
    public void getSettingsDirWithScopedStorageTest() {
        assertThat(storagePathProvider.getOdkDirPath(StorageSubdirectory.SETTINGS), is("/root/projects/123/settings"));
    }

    @Test
    public void getODKDirPathsWithScopedStorageTest() {
        String[] storageSubdirectories = storagePathProvider.getOdkRootDirPaths();
        assertThat(storageSubdirectories, arrayWithSize(1));
    }

    @Test
    public void getFormDbPathWithScopedStorageTest() {
        assertThat(storagePathProvider.getRelativeFormPath("All widgets.xml"), is("All widgets.xml"));
        assertThat(storagePathProvider.getRelativeFormPath("/root/projects/123/forms/All widgets.xml"), is("All widgets.xml"));

        assertThat(storagePathProvider.getRelativeFormPath("All widgets-media"), is("All widgets-media"));
        assertThat(storagePathProvider.getRelativeFormPath("/root/projects/123/forms/All widgets-media"), is("All widgets-media"));

        assertThat(storagePathProvider.getRelativeFormPath("All widgets-media/itemsets.csv"), is("All widgets-media/itemsets.csv"));
        assertThat(storagePathProvider.getRelativeFormPath("/root/projects/123/forms/All widgets-media/itemsets.csv"), is("All widgets-media/itemsets.csv"));
    }

    @Test
    public void getInstanceDbPathWithScopedStorageTest() {
        assertThat(storagePathProvider.getRelativeInstancePath("All widgets_2020-01-20_13-54-11/All widgets_2020-01-20_13-54-11.xml"), is("All widgets_2020-01-20_13-54-11/All widgets_2020-01-20_13-54-11.xml"));
        assertThat(storagePathProvider.getRelativeInstancePath("/root/projects/123/instances/All widgets_2020-01-20_13-54-11/All widgets_2020-01-20_13-54-11.xml"), is("All widgets_2020-01-20_13-54-11/All widgets_2020-01-20_13-54-11.xml"));
    }

    @Test
    public void getCacheDbPathWithScopedStorageTest() {
        assertThat(storagePathProvider.getRelativeCachePath("a688a8b48b2e50c070bc76239f572e16.formdef"), is("a688a8b48b2e50c070bc76239f572e16.formdef"));
        assertThat(storagePathProvider.getRelativeCachePath("/root/projects/123/.cache/a688a8b48b2e50c070bc76239f572e16.formdef"), is("a688a8b48b2e50c070bc76239f572e16.formdef"));
    }

    @Test
    public void getAbsoluteFormFilePathWithScopedStorageTest() {
        assertThat(storagePathProvider.getAbsoluteFormFilePath("All widgets.xml"), is("/root/projects/123/forms/All widgets.xml"));
        assertThat(storagePathProvider.getAbsoluteFormFilePath("/root/projects/123/forms/All widgets.xml"), is("/root/projects/123/forms/All widgets.xml"));

        assertThat(storagePathProvider.getAbsoluteFormFilePath("All widgets-media"), is("/root/projects/123/forms/All widgets-media"));
        assertThat(storagePathProvider.getAbsoluteFormFilePath("/root/projects/123/forms/All widgets-media"), is("/root/projects/123/forms/All widgets-media"));

        assertThat(storagePathProvider.getAbsoluteFormFilePath("All widgets-media/itemsets.csv"), is("/root/projects/123/forms/All widgets-media/itemsets.csv"));
        assertThat(storagePathProvider.getAbsoluteFormFilePath("/root/projects/123/forms/All widgets-media/itemsets.csv"), is("/root/projects/123/forms/All widgets-media/itemsets.csv"));
    }

    @Test
    public void getAbsoluteInstanceFilePathWithScopedStorageTest() {
        assertThat(storagePathProvider.getAbsoluteInstanceFilePath("All widgets_2020-01-20_13-54-11/All widgets_2020-01-20_13-54-11.xml"), is("/root/projects/123/instances/All widgets_2020-01-20_13-54-11/All widgets_2020-01-20_13-54-11.xml"));
        assertThat(storagePathProvider.getAbsoluteInstanceFilePath("/root/projects/123/instances/All widgets_2020-01-20_13-54-11/All widgets_2020-01-20_13-54-11.xml"), is("/root/projects/123/instances/All widgets_2020-01-20_13-54-11/All widgets_2020-01-20_13-54-11.xml"));
    }

    @Test
    public void getAbsoluteCacheFilePathWithScopedStorageTest() {
        assertThat(storagePathProvider.getAbsoluteCacheFilePath("a688a8b48b2e50c070bc76239f572e16.formdef"), is("/root/projects/123/.cache/a688a8b48b2e50c070bc76239f572e16.formdef"));
        assertThat(storagePathProvider.getAbsoluteCacheFilePath("/root/projects/123/.cache/a688a8b48b2e50c070bc76239f572e16.formdef"), is("/root/projects/123/.cache/a688a8b48b2e50c070bc76239f572e16.formdef"));
    }

    @Test
    public void getRelativeMapLayerPathTest() {
        assertThat(storagePathProvider.getRelativeMapLayerPath("countries/countries-raster.mbtiles"), is("countries/countries-raster.mbtiles"));
        assertThat(storagePathProvider.getRelativeMapLayerPath("/root/projects/123/layers/countries/countries-raster.mbtiles"), is("countries/countries-raster.mbtiles"));
    }

    @Test
    public void getOfflineMapLayerPathTestWithScopedStorage() {
        assertThat(storagePathProvider.getAbsoluteOfflineMapLayerPath("/root/projects/123/layers/countries/countries-raster.mbtiles"), is("/root/projects/123/layers/countries/countries-raster.mbtiles"));
    }
}
