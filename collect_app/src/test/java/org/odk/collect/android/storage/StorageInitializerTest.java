package org.odk.collect.android.storage;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

@RunWith(AndroidJUnit4.class)
public class StorageInitializerTest {
    private StoragePathProvider storagePathProvider;
    private StorageInitializer storageInitializer;

    @Before
    public void setup() {
        storagePathProvider = new StoragePathProvider();
        storageInitializer = new StorageInitializer(storagePathProvider);

        for (String dirName : storagePathProvider.getOdkRootDirPaths()) {
            File dir = new File(dirName);
            dir.delete();
        }
    }

    @Test
    public void createOdkDirsOnStorage_shouldCreteRequiredDirs() {
        for (String dirName : storagePathProvider.getOdkRootDirPaths()) {
            File dir = new File(dirName);
            assertThat(dir.exists(), is(false));
        }

        storageInitializer.createOdkDirsOnStorage();

        for (String dirName : storagePathProvider.getOdkRootDirPaths()) {
            File dir = new File(dirName);
            assertThat(dir.exists(), is(true));
            assertThat(dir.isDirectory(), is(true));
        }
    }
}
