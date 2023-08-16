package org.odk.collect.android.formmanagement;

import org.junit.Assert;
import org.junit.Test;

public class ShouldAddFormFileTest {

    @Test
    public void rejectIgnoredFiles() {
        Assert.assertFalse(LocalFormUseCases.shouldAddFormFile(".ignored"));
        Assert.assertFalse(LocalFormUseCases.shouldAddFormFile(".ignored.xml"));
        Assert.assertFalse(LocalFormUseCases.shouldAddFormFile(".ignored.xhtml"));
        Assert.assertFalse(LocalFormUseCases.shouldAddFormFile(".xml"));
        Assert.assertFalse(LocalFormUseCases.shouldAddFormFile(".xhtml"));
    }

    @Test
    public void rejectNonFormFileTypes() {
        Assert.assertFalse(LocalFormUseCases.shouldAddFormFile("form"));
        Assert.assertFalse(LocalFormUseCases.shouldAddFormFile("form."));
        Assert.assertFalse(LocalFormUseCases.shouldAddFormFile("form.html"));
        Assert.assertFalse(LocalFormUseCases.shouldAddFormFile("form.js"));
        Assert.assertFalse(LocalFormUseCases.shouldAddFormFile("form.xml.foo"));
        Assert.assertFalse(LocalFormUseCases.shouldAddFormFile("form.xml1"));
    }

    @Test
    public void acceptXmlForm() {
        Assert.assertTrue(LocalFormUseCases.shouldAddFormFile("form.xml"));
    }

    @Test
    public void acceptXhtmlForm() {
        Assert.assertTrue(LocalFormUseCases.shouldAddFormFile("form.xhtml"));
    }
}
