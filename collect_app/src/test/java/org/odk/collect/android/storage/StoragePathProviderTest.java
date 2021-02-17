package org.odk.collect.android.storage;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayWithSize;
import static org.hamcrest.Matchers.hasItemInArray;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.odk.collect.utilities.PathUtils.getAbsoluteFilePath;

public class StoragePathProviderTest {

    private StoragePathProvider storagePathProvider;

    @Before
    public void setup() {
        storagePathProvider = spy(new StoragePathProvider());

        doReturn("/storage/emulated/0/Android/data/org.odk.collect.android/files").when(storagePathProvider).getScopedStorageRootDirPath();
    }

    @Test
    public void getStorageRootDirWithScopedStorageTest() {
        assertThat(storagePathProvider.getStorageRootDirPath(), is("/storage/emulated/0/Android/data/org.odk.collect.android/files"));
    }

    @Test
    public void getFormsDirWithScopedStorageTest() {
        assertThat(storagePathProvider.getDirPath(StorageSubdirectory.FORMS), is("/storage/emulated/0/Android/data/org.odk.collect.android/files/forms"));
    }

    @Test
    public void getInstancesDirWithScopedStorageTest() {
        assertThat(storagePathProvider.getDirPath(StorageSubdirectory.INSTANCES), is("/storage/emulated/0/Android/data/org.odk.collect.android/files/instances"));
    }

    @Test
    public void getMetadataDirWithScopedStorageTest() {
        assertThat(storagePathProvider.getDirPath(StorageSubdirectory.METADATA), is("/storage/emulated/0/Android/data/org.odk.collect.android/files/metadata"));
    }

    @Test
    public void getCacheDirWithScopedStorageTest() {
        assertThat(storagePathProvider.getDirPath(StorageSubdirectory.CACHE), is("/storage/emulated/0/Android/data/org.odk.collect.android/files/.cache"));
    }

    @Test
    public void getLayersDirWithScopedStorageTest() {
        assertThat(storagePathProvider.getDirPath(StorageSubdirectory.LAYERS), is("/storage/emulated/0/Android/data/org.odk.collect.android/files/layers"));
    }

    @Test
    public void getSettingsDirWithScopedStorageTest() {
        assertThat(storagePathProvider.getDirPath(StorageSubdirectory.SETTINGS), is("/storage/emulated/0/Android/data/org.odk.collect.android/files/settings"));
    }

    @Test
    public void getODKDirPathsWithScopedStorageTest() {
        String[] storageSubdirectories = storagePathProvider.getOdkDirPaths();
        assertThat(storageSubdirectories, arrayWithSize(5));

        assertThat(storageSubdirectories, hasItemInArray("/storage/emulated/0/Android/data/org.odk.collect.android/files/forms"));
        assertThat(storageSubdirectories, hasItemInArray("/storage/emulated/0/Android/data/org.odk.collect.android/files/instances"));
        assertThat(storageSubdirectories, hasItemInArray("/storage/emulated/0/Android/data/org.odk.collect.android/files/.cache"));
        assertThat(storageSubdirectories, hasItemInArray("/storage/emulated/0/Android/data/org.odk.collect.android/files/metadata"));
        assertThat(storageSubdirectories, hasItemInArray("/storage/emulated/0/Android/data/org.odk.collect.android/files/layers"));
    }

    @Test
    public void getFormDbPathWithScopedStorageTest() {
        assertThat(storagePathProvider.getFormDbPath("All widgets.xml"), is("All widgets.xml"));
        assertThat(storagePathProvider.getFormDbPath("/storage/emulated/0/Android/data/org.odk.collect.android/files/forms/All widgets.xml"), is("All widgets.xml"));

        assertThat(storagePathProvider.getFormDbPath("All widgets-media"), is("All widgets-media"));
        assertThat(storagePathProvider.getFormDbPath("/storage/emulated/0/Android/data/org.odk.collect.android/files/forms/All widgets-media"), is("All widgets-media"));

        assertThat(storagePathProvider.getFormDbPath("All widgets-media/itemsets.csv"), is("All widgets-media/itemsets.csv"));
        assertThat(storagePathProvider.getFormDbPath("/storage/emulated/0/Android/data/org.odk.collect.android/files/forms/All widgets-media/itemsets.csv"), is("All widgets-media/itemsets.csv"));
    }

    @Test
    public void getInstanceDbPathWithScopedStorageTest() {
        assertThat(storagePathProvider.getInstanceDbPath("All widgets_2020-01-20_13-54-11/All widgets_2020-01-20_13-54-11.xml"), is("All widgets_2020-01-20_13-54-11/All widgets_2020-01-20_13-54-11.xml"));
        assertThat(storagePathProvider.getInstanceDbPath("/storage/emulated/0/Android/data/org.odk.collect.android/files/instances/All widgets_2020-01-20_13-54-11/All widgets_2020-01-20_13-54-11.xml"), is("All widgets_2020-01-20_13-54-11/All widgets_2020-01-20_13-54-11.xml"));
    }

    @Test
    public void getCacheDbPathWithScopedStorageTest() {
        assertThat(storagePathProvider.getCacheDbPath("a688a8b48b2e50c070bc76239f572e16.formdef"), is("a688a8b48b2e50c070bc76239f572e16.formdef"));
        assertThat(storagePathProvider.getCacheDbPath("/storage/emulated/0/Android/data/org.odk.collect.android/files/.cache/a688a8b48b2e50c070bc76239f572e16.formdef"), is("a688a8b48b2e50c070bc76239f572e16.formdef"));
    }

    @Test
    public void getAbsoluteFormFilePathWithScopedStorageTest() {
        assertThat(getAbsoluteFilePath(storagePathProvider.getDirPath(StorageSubdirectory.FORMS), "All widgets.xml"), is("/storage/emulated/0/Android/data/org.odk.collect.android/files/forms/All widgets.xml"));
        assertThat(getAbsoluteFilePath(storagePathProvider.getDirPath(StorageSubdirectory.FORMS), "/storage/emulated/0/Android/data/org.odk.collect.android/files/forms/All widgets.xml"), is("/storage/emulated/0/Android/data/org.odk.collect.android/files/forms/All widgets.xml"));

        assertThat(getAbsoluteFilePath(storagePathProvider.getDirPath(StorageSubdirectory.FORMS), "All widgets-media"), is("/storage/emulated/0/Android/data/org.odk.collect.android/files/forms/All widgets-media"));
        assertThat(getAbsoluteFilePath(storagePathProvider.getDirPath(StorageSubdirectory.FORMS), "/storage/emulated/0/Android/data/org.odk.collect.android/files/forms/All widgets-media"), is("/storage/emulated/0/Android/data/org.odk.collect.android/files/forms/All widgets-media"));

        assertThat(getAbsoluteFilePath(storagePathProvider.getDirPath(StorageSubdirectory.FORMS), "All widgets-media/itemsets.csv"), is("/storage/emulated/0/Android/data/org.odk.collect.android/files/forms/All widgets-media/itemsets.csv"));
        assertThat(getAbsoluteFilePath(storagePathProvider.getDirPath(StorageSubdirectory.FORMS), "/storage/emulated/0/Android/data/org.odk.collect.android/files/forms/All widgets-media/itemsets.csv"), is("/storage/emulated/0/Android/data/org.odk.collect.android/files/forms/All widgets-media/itemsets.csv"));
    }

    @Test
    public void getAbsoluteInstanceFilePathWithScopedStorageTest() {
        assertThat(storagePathProvider.getAbsoluteInstanceFilePath("All widgets_2020-01-20_13-54-11/All widgets_2020-01-20_13-54-11.xml"), is("/storage/emulated/0/Android/data/org.odk.collect.android/files/instances/All widgets_2020-01-20_13-54-11/All widgets_2020-01-20_13-54-11.xml"));
        assertThat(storagePathProvider.getAbsoluteInstanceFilePath("/storage/emulated/0/Android/data/org.odk.collect.android/files/instances/All widgets_2020-01-20_13-54-11/All widgets_2020-01-20_13-54-11.xml"), is("/storage/emulated/0/Android/data/org.odk.collect.android/files/instances/All widgets_2020-01-20_13-54-11/All widgets_2020-01-20_13-54-11.xml"));
    }

    @Test
    public void getAbsoluteCacheFilePathWithScopedStorageTest() {
        assertThat(storagePathProvider.getAbsoluteCacheFilePath("a688a8b48b2e50c070bc76239f572e16.formdef"), is("/storage/emulated/0/Android/data/org.odk.collect.android/files/.cache/a688a8b48b2e50c070bc76239f572e16.formdef"));
        assertThat(storagePathProvider.getAbsoluteCacheFilePath("/storage/emulated/0/Android/data/org.odk.collect.android/files/.cache/a688a8b48b2e50c070bc76239f572e16.formdef"), is("/storage/emulated/0/Android/data/org.odk.collect.android/files/.cache/a688a8b48b2e50c070bc76239f572e16.formdef"));
    }

    @Test
    public void getRelativeMapLayerPathTest() {
        assertThat(storagePathProvider.getRelativeMapLayerPath(null), is(nullValue()));
        assertThat(storagePathProvider.getRelativeMapLayerPath(""), is(""));
        assertThat(storagePathProvider.getRelativeMapLayerPath("countries/countries-raster.mbtiles"), is("countries/countries-raster.mbtiles"));
        assertThat(storagePathProvider.getRelativeMapLayerPath("/storage/emulated/0/Android/data/org.odk.collect.android/files/layers/countries/countries-raster.mbtiles"), is("countries/countries-raster.mbtiles"));
    }

    @Test
    public void getOfflineMapLayerPathTestWithScopedStorage() {
        assertThat(storagePathProvider.getAbsoluteOfflineMapLayerPath(null), is(nullValue()));
        assertThat(storagePathProvider.getAbsoluteOfflineMapLayerPath("/storage/emulated/0/Android/data/org.odk.collect.android/files/layers/countries/countries-raster.mbtiles"), is("/storage/emulated/0/Android/data/org.odk.collect.android/files/layers/countries/countries-raster.mbtiles"));
    }
}
