package org.odk.collect.android.storage;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayWithSize;
import static org.hamcrest.Matchers.hasItemInArray;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class StoragePathProviderTest {

    private StorageStateProvider storageStateProvider;
    private StoragePathProvider storagePathProvider;

    @Before
    public void setup() {
        storageStateProvider = mock(StorageStateProvider.class);
        storagePathProvider = spy(new StoragePathProvider(storageStateProvider));

        doReturn("/storage/emulated/0/Android/data/org.odk.collect.android/files").when(storagePathProvider).getScopedExternalFilesDirPath();
        doReturn("/storage/emulated/0").when(storagePathProvider).getUnscopedExternalFilesDirPath();
    }

    private void mockUsingScopedStorage() {
        when(storageStateProvider.isScopedStorageUsed()).thenReturn(true);
    }

    private void mockUsingUnscopedStorage() {
        when(storageStateProvider.isScopedStorageUsed()).thenReturn(false);
    }

    @Test
    public void getFormsDirWithUnscopedStorageTest() {
        mockUsingUnscopedStorage();

        assertThat(storagePathProvider.getDirPath(StorageSubdirectory.FORMS), is("/storage/emulated/0/odk/forms"));
    }

    @Test
    public void getFormsDirWithScopedStorageTest() {
        mockUsingScopedStorage();

        assertThat(storagePathProvider.getDirPath(StorageSubdirectory.FORMS), is("/storage/emulated/0/Android/data/org.odk.collect.android/files/odk/forms"));
    }

    @Test
    public void getInstancesDirWithUnscopedStorageTest() {
        mockUsingUnscopedStorage();

        assertThat(storagePathProvider.getDirPath(StorageSubdirectory.INSTANCES), is("/storage/emulated/0/odk/instances"));
    }

    @Test
    public void getInstancesDirWithScopedStorageTest() {
        mockUsingScopedStorage();

        assertThat(storagePathProvider.getDirPath(StorageSubdirectory.INSTANCES), is("/storage/emulated/0/Android/data/org.odk.collect.android/files/odk/instances"));
    }

    @Test
    public void getMetadataDirWithUnscopedStorageTest() {
        mockUsingUnscopedStorage();

        assertThat(storagePathProvider.getDirPath(StorageSubdirectory.METADATA), is("/storage/emulated/0/odk/metadata"));
    }

    @Test
    public void getMetadataDirWithScopedStorageTest() {
        mockUsingScopedStorage();

        assertThat(storagePathProvider.getDirPath(StorageSubdirectory.METADATA), is("/storage/emulated/0/Android/data/org.odk.collect.android/files/odk/metadata"));
    }

    @Test
    public void getCacheDirWithUnscopedStorageTest() {
        mockUsingUnscopedStorage();

        assertThat(storagePathProvider.getDirPath(StorageSubdirectory.CACHE), is("/storage/emulated/0/odk/.cache"));
    }

    @Test
    public void getCacheDirWithScopedStorageTest() {
        mockUsingScopedStorage();

        assertThat(storagePathProvider.getDirPath(StorageSubdirectory.CACHE), is("/storage/emulated/0/Android/data/org.odk.collect.android/files/odk/.cache"));
    }

    @Test
    public void getLayersDirWithUnscopedStorageTest() {
        mockUsingUnscopedStorage();

        assertThat(storagePathProvider.getDirPath(StorageSubdirectory.LAYERS), is("/storage/emulated/0/odk/layers"));
    }

    @Test
    public void getLayersDirWithScopedStorageTest() {
        mockUsingScopedStorage();

        assertThat(storagePathProvider.getDirPath(StorageSubdirectory.LAYERS), is("/storage/emulated/0/Android/data/org.odk.collect.android/files/odk/layers"));
    }

    @Test
    public void getSettingsDirWithUnscopedStorageTest() {
        mockUsingUnscopedStorage();

        assertThat(storagePathProvider.getDirPath(StorageSubdirectory.SETTINGS), is("/storage/emulated/0/odk/settings"));
    }

    @Test
    public void getSettingsDirWithScopedStorageTest() {
        mockUsingScopedStorage();

        assertThat(storagePathProvider.getDirPath(StorageSubdirectory.SETTINGS), is("/storage/emulated/0/Android/data/org.odk.collect.android/files/odk/settings"));
    }

    @Test
    public void getODKDirPathsWithUnscopedStorageTest() {
        mockUsingUnscopedStorage();

        String[] storageSubdirectories = storagePathProvider.getOdkDirPaths();
        assertThat(storageSubdirectories, arrayWithSize(6));

        assertThat(storageSubdirectories, hasItemInArray("/storage/emulated/0/odk"));
        assertThat(storageSubdirectories, hasItemInArray("/storage/emulated/0/odk/forms"));
        assertThat(storageSubdirectories, hasItemInArray("/storage/emulated/0/odk/instances"));
        assertThat(storageSubdirectories, hasItemInArray("/storage/emulated/0/odk/.cache"));
        assertThat(storageSubdirectories, hasItemInArray("/storage/emulated/0/odk/metadata"));
        assertThat(storageSubdirectories, hasItemInArray("/storage/emulated/0/odk/layers"));
    }

    @Test
    public void getODKDirPathsWithScopedStorageTest() {
        mockUsingScopedStorage();

        String[] storageSubdirectories = storagePathProvider.getOdkDirPaths();
        assertThat(storageSubdirectories, arrayWithSize(6));

        assertThat(storageSubdirectories, hasItemInArray("/storage/emulated/0/Android/data/org.odk.collect.android/files/odk"));
        assertThat(storageSubdirectories, hasItemInArray("/storage/emulated/0/Android/data/org.odk.collect.android/files/odk/forms"));
        assertThat(storageSubdirectories, hasItemInArray("/storage/emulated/0/Android/data/org.odk.collect.android/files/odk/instances"));
        assertThat(storageSubdirectories, hasItemInArray("/storage/emulated/0/Android/data/org.odk.collect.android/files/odk/.cache"));
        assertThat(storageSubdirectories, hasItemInArray("/storage/emulated/0/Android/data/org.odk.collect.android/files/odk/metadata"));
        assertThat(storageSubdirectories, hasItemInArray("/storage/emulated/0/Android/data/org.odk.collect.android/files/odk/layers"));
    }

    @Test
    public void getFormDbPathWithUnscopedStorageTest() {
        mockUsingUnscopedStorage();

        assertThat(storagePathProvider.getFormDbPath("All widgets.xml"), is("/storage/emulated/0/odk/forms/All widgets.xml"));
        assertThat(storagePathProvider.getFormDbPath("/storage/emulated/0/odk/forms/All widgets.xml"), is("/storage/emulated/0/odk/forms/All widgets.xml"));

        assertThat(storagePathProvider.getFormDbPath("All widgets-media"), is("/storage/emulated/0/odk/forms/All widgets-media"));
        assertThat(storagePathProvider.getFormDbPath("/storage/emulated/0/odk/forms/All widgets-media"), is("/storage/emulated/0/odk/forms/All widgets-media"));

        assertThat(storagePathProvider.getFormDbPath("All widgets-media/itemsets.csv"), is("/storage/emulated/0/odk/forms/All widgets-media/itemsets.csv"));
        assertThat(storagePathProvider.getFormDbPath("/storage/emulated/0/odk/forms/All widgets-media/itemsets.csv"), is("/storage/emulated/0/odk/forms/All widgets-media/itemsets.csv"));
    }

    @Test
    public void getFormDbPathWithScopedStorageTest() {
        mockUsingScopedStorage();

        assertThat(storagePathProvider.getFormDbPath("All widgets.xml"), is("All widgets.xml"));
        assertThat(storagePathProvider.getFormDbPath("/storage/emulated/0/Android/data/org.odk.collect.android/files/odk/forms/All widgets.xml"), is("All widgets.xml"));

        assertThat(storagePathProvider.getFormDbPath("All widgets-media"), is("All widgets-media"));
        assertThat(storagePathProvider.getFormDbPath("/storage/emulated/0/Android/data/org.odk.collect.android/files/odk/forms/All widgets-media"), is("All widgets-media"));

        assertThat(storagePathProvider.getFormDbPath("All widgets-media/itemsets.csv"), is("All widgets-media/itemsets.csv"));
        assertThat(storagePathProvider.getFormDbPath("/storage/emulated/0/Android/data/org.odk.collect.android/files/odk/forms/All widgets-media/itemsets.csv"), is("All widgets-media/itemsets.csv"));
    }

    @Test
    public void getInstanceDbPathWithUnscopedStorageTest() {
        mockUsingUnscopedStorage();

        assertThat(storagePathProvider.getInstanceDbPath("All widgets_2020-01-20_13-54-11/All widgets_2020-01-20_13-54-11.xml"), is("/storage/emulated/0/odk/instances/All widgets_2020-01-20_13-54-11/All widgets_2020-01-20_13-54-11.xml"));
        assertThat(storagePathProvider.getInstanceDbPath("/storage/emulated/0/odk/instances/All widgets_2020-01-20_13-54-11/All widgets_2020-01-20_13-54-11.xml"), is("/storage/emulated/0/odk/instances/All widgets_2020-01-20_13-54-11/All widgets_2020-01-20_13-54-11.xml"));
    }

    @Test
    public void getInstanceDbPathWithScopedStorageTest() {
        mockUsingScopedStorage();

        assertThat(storagePathProvider.getInstanceDbPath("All widgets_2020-01-20_13-54-11/All widgets_2020-01-20_13-54-11.xml"), is("All widgets_2020-01-20_13-54-11/All widgets_2020-01-20_13-54-11.xml"));
        assertThat(storagePathProvider.getInstanceDbPath("/storage/emulated/0/Android/data/org.odk.collect.android/files/odk/instances/All widgets_2020-01-20_13-54-11/All widgets_2020-01-20_13-54-11.xml"), is("All widgets_2020-01-20_13-54-11/All widgets_2020-01-20_13-54-11.xml"));
    }

    @Test
    public void getCacheDbPathWithUnscopedStorageTest() {
        mockUsingUnscopedStorage();

        assertThat(storagePathProvider.getCacheDbPath("a688a8b48b2e50c070bc76239f572e16.formdef"), is("/storage/emulated/0/odk/.cache/a688a8b48b2e50c070bc76239f572e16.formdef"));
        assertThat(storagePathProvider.getCacheDbPath("/storage/emulated/0/odk/.cache/a688a8b48b2e50c070bc76239f572e16.formdef"), is("/storage/emulated/0/odk/.cache/a688a8b48b2e50c070bc76239f572e16.formdef"));
    }

    @Test
    public void getCacheDbPathWithScopedStorageTest() {
        mockUsingScopedStorage();

        assertThat(storagePathProvider.getCacheDbPath("a688a8b48b2e50c070bc76239f572e16.formdef"), is("a688a8b48b2e50c070bc76239f572e16.formdef"));
        assertThat(storagePathProvider.getCacheDbPath("/storage/emulated/0/Android/data/org.odk.collect.android/files/odk/.cache/a688a8b48b2e50c070bc76239f572e16.formdef"), is("a688a8b48b2e50c070bc76239f572e16.formdef"));
    }

    @Test
    public void getAbsoluteFormFilePathWithUnscopedStorageTest() {
        mockUsingUnscopedStorage();

        assertThat(storagePathProvider.getAbsoluteFormFilePath("All widgets.xml"), is("/storage/emulated/0/odk/forms/All widgets.xml"));
        assertThat(storagePathProvider.getAbsoluteFormFilePath("/storage/emulated/0/odk/forms/All widgets.xml"), is("/storage/emulated/0/odk/forms/All widgets.xml"));

        assertThat(storagePathProvider.getAbsoluteFormFilePath("All widgets-media"), is("/storage/emulated/0/odk/forms/All widgets-media"));
        assertThat(storagePathProvider.getAbsoluteFormFilePath("/storage/emulated/0/odk/forms/All widgets-media"), is("/storage/emulated/0/odk/forms/All widgets-media"));

        assertThat(storagePathProvider.getAbsoluteFormFilePath("All widgets-media/itemsets.csv"), is("/storage/emulated/0/odk/forms/All widgets-media/itemsets.csv"));
        assertThat(storagePathProvider.getAbsoluteFormFilePath("/storage/emulated/0/odk/forms/All widgets-media/itemsets.csv"), is("/storage/emulated/0/odk/forms/All widgets-media/itemsets.csv"));
    }

    @Test
    public void getAbsoluteFormFilePathWithScopedStorageTest() {
        mockUsingScopedStorage();

        assertThat(storagePathProvider.getAbsoluteFormFilePath("All widgets.xml"), is("/storage/emulated/0/Android/data/org.odk.collect.android/files/odk/forms/All widgets.xml"));
        assertThat(storagePathProvider.getAbsoluteFormFilePath("/storage/emulated/0/Android/data/org.odk.collect.android/files/odk/forms/All widgets.xml"), is("/storage/emulated/0/Android/data/org.odk.collect.android/files/odk/forms/All widgets.xml"));

        assertThat(storagePathProvider.getAbsoluteFormFilePath("All widgets-media"), is("/storage/emulated/0/Android/data/org.odk.collect.android/files/odk/forms/All widgets-media"));
        assertThat(storagePathProvider.getAbsoluteFormFilePath("/storage/emulated/0/Android/data/org.odk.collect.android/files/odk/forms/All widgets-media"), is("/storage/emulated/0/Android/data/org.odk.collect.android/files/odk/forms/All widgets-media"));

        assertThat(storagePathProvider.getAbsoluteFormFilePath("All widgets-media/itemsets.csv"), is("/storage/emulated/0/Android/data/org.odk.collect.android/files/odk/forms/All widgets-media/itemsets.csv"));
        assertThat(storagePathProvider.getAbsoluteFormFilePath("/storage/emulated/0/Android/data/org.odk.collect.android/files/odk/forms/All widgets-media/itemsets.csv"), is("/storage/emulated/0/Android/data/org.odk.collect.android/files/odk/forms/All widgets-media/itemsets.csv"));
    }

    @Test
    public void getAbsoluteInstanceFilePathWithUnscopedStorageTest() {
        mockUsingUnscopedStorage();

        assertThat(storagePathProvider.getAbsoluteInstanceFilePath("All widgets_2020-01-20_13-54-11/All widgets_2020-01-20_13-54-11.xml"), is("/storage/emulated/0/odk/instances/All widgets_2020-01-20_13-54-11/All widgets_2020-01-20_13-54-11.xml"));
        assertThat(storagePathProvider.getAbsoluteInstanceFilePath("/storage/emulated/0/odk/instances/All widgets_2020-01-20_13-54-11/All widgets_2020-01-20_13-54-11.xml"), is("/storage/emulated/0/odk/instances/All widgets_2020-01-20_13-54-11/All widgets_2020-01-20_13-54-11.xml"));
    }

    @Test
    public void getAbsoluteInstanceFilePathWithScopedStorageTest() {
        mockUsingScopedStorage();

        assertThat(storagePathProvider.getAbsoluteInstanceFilePath("All widgets_2020-01-20_13-54-11/All widgets_2020-01-20_13-54-11.xml"), is("/storage/emulated/0/Android/data/org.odk.collect.android/files/odk/instances/All widgets_2020-01-20_13-54-11/All widgets_2020-01-20_13-54-11.xml"));
        assertThat(storagePathProvider.getAbsoluteInstanceFilePath("/storage/emulated/0/Android/data/org.odk.collect.android/files/odk/instances/All widgets_2020-01-20_13-54-11/All widgets_2020-01-20_13-54-11.xml"), is("/storage/emulated/0/Android/data/org.odk.collect.android/files/odk/instances/All widgets_2020-01-20_13-54-11/All widgets_2020-01-20_13-54-11.xml"));
    }

    @Test
    public void getAbsoluteCacheFilePathWithUnscopedStorageTest() {
        mockUsingUnscopedStorage();

        assertThat(storagePathProvider.getAbsoluteCacheFilePath("a688a8b48b2e50c070bc76239f572e16.formdef"), is("/storage/emulated/0/odk/.cache/a688a8b48b2e50c070bc76239f572e16.formdef"));
        assertThat(storagePathProvider.getAbsoluteCacheFilePath("/storage/emulated/0/odk/.cache/a688a8b48b2e50c070bc76239f572e16.formdef"), is("/storage/emulated/0/odk/.cache/a688a8b48b2e50c070bc76239f572e16.formdef"));
    }

    @Test
    public void getAbsoluteCacheFilePathWithScopedStorageTest() {
        mockUsingScopedStorage();

        assertThat(storagePathProvider.getAbsoluteCacheFilePath("a688a8b48b2e50c070bc76239f572e16.formdef"), is("/storage/emulated/0/Android/data/org.odk.collect.android/files/odk/.cache/a688a8b48b2e50c070bc76239f572e16.formdef"));
        assertThat(storagePathProvider.getAbsoluteCacheFilePath("/storage/emulated/0/Android/data/org.odk.collect.android/files/odk/.cache/a688a8b48b2e50c070bc76239f572e16.formdef"), is("/storage/emulated/0/Android/data/org.odk.collect.android/files/odk/.cache/a688a8b48b2e50c070bc76239f572e16.formdef"));
    }
}
