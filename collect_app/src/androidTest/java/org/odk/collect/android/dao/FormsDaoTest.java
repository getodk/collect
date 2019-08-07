/*
 * Copyright 2017 Nafundi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.odk.collect.android.dao;

import android.database.Cursor;
import androidx.test.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.dto.Form;
import org.odk.collect.android.provider.FormsProviderAPI;
import org.odk.collect.android.utilities.ResetUtility;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
/**
 * This class contains tests for {@link FormsDao}
 */
public class FormsDaoTest {

    private FormsDao formsDao;

    // sample forms
    private Form biggestNOfSetForm;
    private Form birdsForm;
    private Form miramareForm;
    private Form geoTaggerV2Form;
    private Form widgetsForm;
    private Form sampleForm;
    private Form birds2Form;

    @Before
    public void setUp() throws IOException {
        formsDao = new FormsDao();
        resetAppState();
        setUpSampleForms();
    }

    @Test
    public void getAllFormsCursorTest() {
        Cursor cursor = formsDao.getFormsCursor();
        List<Form> forms = formsDao.getFormsFromCursor(cursor);
        assertEquals(7, forms.size());

        assertEquals(biggestNOfSetForm, forms.get(0));
        assertEquals(birdsForm, forms.get(1));
        assertEquals(miramareForm, forms.get(2));
        assertEquals(geoTaggerV2Form, forms.get(3));
        assertEquals(widgetsForm, forms.get(4));
        assertEquals(sampleForm, forms.get(5));
        assertEquals(birds2Form, forms.get(6));
    }

    @Test
    public void getFormsCursorForFormIdTest() {
        Cursor cursor = formsDao.getFormsCursorForFormId("Birds");
        List<Form> forms = formsDao.getFormsFromCursor(cursor);
        assertEquals(2, forms.size());

        assertEquals(birdsForm, forms.get(0));
    }

    @Test
    public void getFormsCursorTest() {
        Cursor cursor = formsDao.getFormsCursor(null, null, null, null);
        List<Form> forms = formsDao.getFormsFromCursor(cursor);
        assertEquals(7, forms.size());

        assertEquals(biggestNOfSetForm, forms.get(0));
        assertEquals(birdsForm, forms.get(1));
        assertEquals(miramareForm, forms.get(2));
        assertEquals(geoTaggerV2Form, forms.get(3));
        assertEquals(widgetsForm, forms.get(4));
        assertEquals(sampleForm, forms.get(5));
        assertEquals(birds2Form, forms.get(6));

        String sortOrder = FormsProviderAPI.FormsColumns.DISPLAY_NAME + " COLLATE NOCASE DESC";

        cursor = formsDao.getFormsCursor(null, null, null, sortOrder);
        forms = formsDao.getFormsFromCursor(cursor);
        assertEquals(7, forms.size());

        assertEquals(biggestNOfSetForm, forms.get(6));
        assertEquals(birdsForm, forms.get(5));
        assertEquals(birds2Form, forms.get(4));
        assertEquals(geoTaggerV2Form, forms.get(3));
        assertEquals(miramareForm, forms.get(2));
        assertEquals(sampleForm, forms.get(1));
        assertEquals(widgetsForm, forms.get(0));

        String selection = FormsProviderAPI.FormsColumns.DISPLAY_NAME + "=?";
        String[] selectionArgs = {"Miramare"};

        cursor = formsDao.getFormsCursor(null, selection, selectionArgs, null);
        forms = formsDao.getFormsFromCursor(cursor);
        assertEquals(1, forms.size());

        assertEquals(miramareForm, forms.get(0));
    }

    @Test
    public void getFormsCursorForFormFilePathTest() {
        Cursor cursor = formsDao.getFormsCursorForFormFilePath(Collect.FORMS_PATH + "/Miramare.xml");
        List<Form> forms = formsDao.getFormsFromCursor(cursor);
        assertEquals(1, forms.size());

        assertEquals(miramareForm, forms.get(0));
    }

    @Test
    public void updateInstanceTest() {
        Cursor cursor = formsDao.getFormsCursorForFormFilePath(Collect.FORMS_PATH + "/Widgets.xml");
        List<Form> forms = formsDao.getFormsFromCursor(cursor);

        assertEquals(1, forms.size());
        assertEquals(widgetsForm, forms.get(0));

        widgetsForm = new Form.Builder()
                .displayName("Widgets")
                .jrFormId("Widgets2")
                .md5Hash("d41d8cd98f00b204e9800998ecf8427e")
                .date(1487782554846L)
                .formMediaPath(Collect.FORMS_PATH + "/Widgets-media")
                .formFilePath(Collect.FORMS_PATH + "/Widgets.xml")
                .jrCacheFilePath(Collect.ODK_ROOT + "/.cache/0eacc6333449e66826326eb5fcc75749.formdef")
                .build();

        String where = FormsProviderAPI.FormsColumns.DISPLAY_NAME + "=?";
        String[] whereArgs = {"Widgets"};
        assertEquals(formsDao.updateForm(formsDao.getValuesFromFormObject(widgetsForm), where, whereArgs), 1);

        cursor = formsDao.getFormsCursorForFormFilePath(Collect.FORMS_PATH + "/Widgets.xml");
        forms = formsDao.getFormsFromCursor(cursor);

        assertEquals(1, forms.size());
        assertEquals(widgetsForm, forms.get(0));
    }

    @Test
    public void getFormMediaPathTest() {
        String mediaPath = formsDao.getFormMediaPath("Birds", "4");
        assertEquals(Collect.FORMS_PATH + "/Birds_4-media", mediaPath);
    }

    private void setUpSampleForms() throws IOException {
        assertTrue(new File(Collect.FORMS_PATH + "/Biggest N of Set.xml").createNewFile());
        biggestNOfSetForm = new Form.Builder()
                .displayName("Biggest N of Set")
                .jrFormId("N_Biggest")
                .md5Hash("d41d8cd98f00b204e9800998ecf8427e")
                .date(1487773315435L)
                .formMediaPath(Collect.FORMS_PATH + "/Biggest N of Set-media")
                .formFilePath(Collect.FORMS_PATH + "/Biggest N of Set.xml")
                .jrCacheFilePath(Collect.ODK_ROOT + "/.cache/ccce6015dd1b8f935f5f3058e81eeb43.formdef")
                .build();

        formsDao.saveForm(formsDao.getValuesFromFormObject(biggestNOfSetForm));

        assertTrue(new File(Collect.FORMS_PATH + "/Birds.xml").createNewFile());
        birdsForm = new Form.Builder()
                .displayName("Birds")
                .jrFormId("Birds")
                .jrVersion("3")
                .md5Hash("d41d8cd98f00b204e9800998ecf8427e")
                .date(1487782404899L)
                .formMediaPath(Collect.FORMS_PATH + "/Birds-media")
                .formFilePath(Collect.FORMS_PATH + "/Birds.xml")
                .jrCacheFilePath(Collect.ODK_ROOT + "/.cache/4cd980d50f884362afba842cbff3a798.formdef")
                .build();

        formsDao.saveForm(formsDao.getValuesFromFormObject(birdsForm));

        assertTrue(new File(Collect.FORMS_PATH + "/Miramare.xml").createNewFile());
        miramareForm = new Form.Builder()
                .displayName("Miramare")
                .jrFormId("Miramare")
                .md5Hash("d41d8cd98f00b204e9800998ecf8427e")
                .date(1487782545945L)
                .formMediaPath(Collect.FORMS_PATH + "/Miramare-media")
                .formFilePath(Collect.FORMS_PATH + "/Miramare.xml")
                .jrCacheFilePath(Collect.ODK_ROOT + "/.cache/e733627cdbf220929bf9c4899cb983ea.formdef")
                .build();

        formsDao.saveForm(formsDao.getValuesFromFormObject(miramareForm));

        assertTrue(new File(Collect.FORMS_PATH + "/Geo Tagger v2.xml").createNewFile());
        geoTaggerV2Form = new Form.Builder()
                .displayName("Geo Tagger v2")
                .jrFormId("geo_tagger_v2")
                .md5Hash("d41d8cd98f00b204e9800998ecf8427e")
                .date(1487782428992L)
                .formMediaPath(Collect.FORMS_PATH + "/Geo Tagger v2-media")
                .formFilePath(Collect.FORMS_PATH + "/Geo Tagger v2.xml")
                .jrCacheFilePath(Collect.ODK_ROOT + "/.cache/1d5e9109298c8ef02bc523b17d7c0451.formdef")
                .build();

        formsDao.saveForm(formsDao.getValuesFromFormObject(geoTaggerV2Form));

        assertTrue(new File(Collect.FORMS_PATH + "/Widgets.xml").createNewFile());
        widgetsForm = new Form.Builder()
                .displayName("Widgets")
                .jrFormId("Widgets")
                .md5Hash("d41d8cd98f00b204e9800998ecf8427e")
                .date(1487782554846L)
                .formMediaPath(Collect.FORMS_PATH + "/Widgets-media")
                .formFilePath(Collect.FORMS_PATH + "/Widgets.xml")
                .jrCacheFilePath(Collect.ODK_ROOT + "/.cache/0eacc6333449e66826326eb5fcc75749.formdef")
                .build();

        formsDao.saveForm(formsDao.getValuesFromFormObject(widgetsForm));

        assertTrue(new File(Collect.FORMS_PATH + "/sample.xml").createNewFile());
        sampleForm = new Form.Builder()
                .displayName("sample")
                .jrFormId("sample")
                .md5Hash("d41d8cd98f00b204e9800998ecf8427e")
                .date(1487782555840L)
                .formMediaPath(Collect.FORMS_PATH + "/sample-media")
                .formFilePath(Collect.FORMS_PATH + "/sample.xml")
                .jrCacheFilePath(Collect.ODK_ROOT + "/.cache/4f495fddd1f2544f65444ea83d25f425.formdef")
                .build();

        formsDao.saveForm(formsDao.getValuesFromFormObject(sampleForm));

        assertTrue(new File(Collect.FORMS_PATH + "/Birds_4.xml").createNewFile());
        birds2Form = new Form.Builder()
                .displayName("Birds")
                .jrFormId("Birds")
                .jrVersion("4")
                .md5Hash("d41d8cd98f00b204e9800998ecf8427e")
                .date(1512390303610L)
                .formMediaPath(Collect.FORMS_PATH + "/Birds_4-media")
                .formFilePath(Collect.FORMS_PATH + "/Birds_4.xml")
                .jrCacheFilePath(Collect.ODK_ROOT + "/.cache/4cd980d50f884362afba842cbff3a775.formdef")
                .build();

        formsDao.saveForm(formsDao.getValuesFromFormObject(birds2Form));
    }

    @After
    public void tearDown() {
        resetAppState();
    }

    private void resetAppState() {
        List<Integer> resetActions = Arrays.asList(
                ResetUtility.ResetAction.RESET_PREFERENCES, ResetUtility.ResetAction.RESET_INSTANCES,
                ResetUtility.ResetAction.RESET_FORMS, ResetUtility.ResetAction.RESET_LAYERS,
                ResetUtility.ResetAction.RESET_CACHE, ResetUtility.ResetAction.RESET_OSM_DROID
        );

        List<Integer> failedResetActions = new ResetUtility().reset(InstrumentationRegistry.getTargetContext(), resetActions);
        assertEquals(0, failedResetActions.size());
    }
}
