package org.odk.collect.android.formmanagement;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.List;

public class DiskFormsSynchronizerTaskTest {

    @Test
    public void rejectIgnoredFiles() {
        Assert.assertFalse(DiskFormsSynchronizer.shouldAddFormFile(".ignored"));
        Assert.assertFalse(DiskFormsSynchronizer.shouldAddFormFile(".ignored.xml"));
        Assert.assertFalse(DiskFormsSynchronizer.shouldAddFormFile(".ignored.xhtml"));
        Assert.assertFalse(DiskFormsSynchronizer.shouldAddFormFile(".xml"));
        Assert.assertFalse(DiskFormsSynchronizer.shouldAddFormFile(".xhtml"));
    }

    @Test
    public void rejectNonFormFileTypes() {
        Assert.assertFalse(DiskFormsSynchronizer.shouldAddFormFile("form"));
        Assert.assertFalse(DiskFormsSynchronizer.shouldAddFormFile("form."));
        Assert.assertFalse(DiskFormsSynchronizer.shouldAddFormFile("form.html"));
        Assert.assertFalse(DiskFormsSynchronizer.shouldAddFormFile("form.js"));
        Assert.assertFalse(DiskFormsSynchronizer.shouldAddFormFile("form.xml.foo"));
        Assert.assertFalse(DiskFormsSynchronizer.shouldAddFormFile("form.xml1"));
    }

    @Test
    public void acceptXmlForm() {
        Assert.assertTrue(DiskFormsSynchronizer.shouldAddFormFile("form.xml"));
    }

    @Test
    public void acceptXhtmlForm() {
        Assert.assertTrue(DiskFormsSynchronizer.shouldAddFormFile("form.xhtml"));
    }

    @Test
    public void filterEmptyListOfForms() {
        File[] formDefs = {};
        List<File> files = DiskFormsSynchronizer.filterFormsToAdd(formDefs, 0);
        Assert.assertEquals(0, files.size());
    }

    @Test
    public void filterNullListOfForms() {
        List<File> files = DiskFormsSynchronizer.filterFormsToAdd(null, 0);
        Assert.assertEquals(0, files.size());
    }
}
