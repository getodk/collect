package org.odk.collect.android.tasks;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.List;

public class DiskSyncTaskTest {
    @Test
    public void rejectIgnoredFiles() {
        Assert.assertFalse(DiskSyncTask.shouldAddFormFile(".ignored"));
        Assert.assertFalse(DiskSyncTask.shouldAddFormFile(".ignored.xml"));
        Assert.assertFalse(DiskSyncTask.shouldAddFormFile(".ignored.xhtml"));
        Assert.assertFalse(DiskSyncTask.shouldAddFormFile(".xml"));
        Assert.assertFalse(DiskSyncTask.shouldAddFormFile(".xhtml"));
    }

    @Test
    public void rejectNonFormFileTypes() {
        Assert.assertFalse(DiskSyncTask.shouldAddFormFile("form"));
        Assert.assertFalse(DiskSyncTask.shouldAddFormFile("form."));
        Assert.assertFalse(DiskSyncTask.shouldAddFormFile("form.html"));
        Assert.assertFalse(DiskSyncTask.shouldAddFormFile("form.js"));
        Assert.assertFalse(DiskSyncTask.shouldAddFormFile("form.xml.foo"));
        Assert.assertFalse(DiskSyncTask.shouldAddFormFile("form.xml1"));
    }

    @Test
    public void acceptXmlForm() {
        Assert.assertTrue(DiskSyncTask.shouldAddFormFile("form.xml"));
    }

    @Test
    public void acceptXhtmlForm() {
        Assert.assertTrue(DiskSyncTask.shouldAddFormFile("form.xhtml"));
    }

    @Test
    public void filterEmptyListOfForms() {
        File[] formDefs = {};
        List<File> files = DiskSyncTask.filterFormsToAdd(formDefs, 0);
        Assert.assertEquals(0, files.size());
    }

    @Test
    public void filterNullListOfForms() {
        List<File> files = DiskSyncTask.filterFormsToAdd(null, 0);
        Assert.assertEquals(0, files.size());
    }
}
