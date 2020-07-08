package org.odk.collect.android.utilities;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.List;

public class FormsDirDiskFormsSynchronizerTaskTest {

    @Test
    public void rejectIgnoredFiles() {
        Assert.assertFalse(FormsDirDiskFormsSynchronizer.shouldAddFormFile(".ignored"));
        Assert.assertFalse(FormsDirDiskFormsSynchronizer.shouldAddFormFile(".ignored.xml"));
        Assert.assertFalse(FormsDirDiskFormsSynchronizer.shouldAddFormFile(".ignored.xhtml"));
        Assert.assertFalse(FormsDirDiskFormsSynchronizer.shouldAddFormFile(".xml"));
        Assert.assertFalse(FormsDirDiskFormsSynchronizer.shouldAddFormFile(".xhtml"));
    }

    @Test
    public void rejectNonFormFileTypes() {
        Assert.assertFalse(FormsDirDiskFormsSynchronizer.shouldAddFormFile("form"));
        Assert.assertFalse(FormsDirDiskFormsSynchronizer.shouldAddFormFile("form."));
        Assert.assertFalse(FormsDirDiskFormsSynchronizer.shouldAddFormFile("form.html"));
        Assert.assertFalse(FormsDirDiskFormsSynchronizer.shouldAddFormFile("form.js"));
        Assert.assertFalse(FormsDirDiskFormsSynchronizer.shouldAddFormFile("form.xml.foo"));
        Assert.assertFalse(FormsDirDiskFormsSynchronizer.shouldAddFormFile("form.xml1"));
    }

    @Test
    public void acceptXmlForm() {
        Assert.assertTrue(FormsDirDiskFormsSynchronizer.shouldAddFormFile("form.xml"));
    }

    @Test
    public void acceptXhtmlForm() {
        Assert.assertTrue(FormsDirDiskFormsSynchronizer.shouldAddFormFile("form.xhtml"));
    }

    @Test
    public void filterEmptyListOfForms() {
        File[] formDefs = {};
        List<File> files = FormsDirDiskFormsSynchronizer.filterFormsToAdd(formDefs, 0);
        Assert.assertEquals(0, files.size());
    }

    @Test
    public void filterNullListOfForms() {
        List<File> files = FormsDirDiskFormsSynchronizer.filterFormsToAdd(null, 0);
        Assert.assertEquals(0, files.size());
    }
}
