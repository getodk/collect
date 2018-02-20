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
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

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

    @Before
    public void setUp() throws IOException {
        formsDao = new FormsDao();
        resetAppState();
        fillDatabase();
    }

    @Test
    public void getAllFormsCursorTest() {
        Cursor cursor = formsDao.getFormsCursor();
        List<Form> forms = formsDao.getFormsFromCursor(cursor);
        assertEquals(7, forms.size());

        assertEquals("Biggest N of Set", forms.get(0).getDisplayName());
        assertEquals("Added on Wed, Feb 22, 2017 at 15:21", forms.get(0).getDisplaySubtext());

        assertEquals("Birds", forms.get(1).getDisplayName());
        assertEquals("Added on Wed, Feb 22, 2017 at 17:53", forms.get(1).getDisplaySubtext());

        assertEquals("Miramare", forms.get(2).getDisplayName());
        assertEquals("Added on Wed, Feb 22, 2017 at 17:55", forms.get(2).getDisplaySubtext());

        assertEquals("Geo Tagger v2", forms.get(3).getDisplayName());
        assertEquals("Added on Wed, Feb 22, 2017 at 17:53", forms.get(3).getDisplaySubtext());

        assertEquals("Widgets", forms.get(4).getDisplayName());
        assertEquals("Added on Wed, Feb 22, 2017 at 17:55", forms.get(4).getDisplaySubtext());

        assertEquals("sample", forms.get(5).getDisplayName());
        assertEquals("Added on Wed, Feb 22, 2017 at 17:55", forms.get(5).getDisplaySubtext());
    }

    @Test
    public void getFormsCursorForFormIdTest() {
        Cursor cursor = formsDao.getFormsCursorForFormId("Birds");
        List<Form> forms = formsDao.getFormsFromCursor(cursor);
        assertEquals(2, forms.size());

        assertEquals("Birds", forms.get(0).getDisplayName());
        assertEquals("Added on Wed, Feb 22, 2017 at 17:53", forms.get(0).getDisplaySubtext());
    }

    @Test
    public void getFormsCursorTest() {
        Cursor cursor = formsDao.getFormsCursor(null, null, null, null);
        List<Form> forms = formsDao.getFormsFromCursor(cursor);
        assertEquals(7, forms.size());

        assertEquals("Biggest N of Set", forms.get(0).getDisplayName());
        assertEquals("Added on Wed, Feb 22, 2017 at 15:21", forms.get(0).getDisplaySubtext());

        assertEquals("Birds", forms.get(1).getDisplayName());
        assertEquals("Added on Wed, Feb 22, 2017 at 17:53", forms.get(1).getDisplaySubtext());

        assertEquals("Miramare", forms.get(2).getDisplayName());
        assertEquals("Added on Wed, Feb 22, 2017 at 17:55", forms.get(2).getDisplaySubtext());

        assertEquals("Geo Tagger v2", forms.get(3).getDisplayName());
        assertEquals("Added on Wed, Feb 22, 2017 at 17:53", forms.get(3).getDisplaySubtext());

        assertEquals("Widgets", forms.get(4).getDisplayName());
        assertEquals("Added on Wed, Feb 22, 2017 at 17:55", forms.get(4).getDisplaySubtext());

        assertEquals("sample", forms.get(5).getDisplayName());
        assertEquals("Added on Wed, Feb 22, 2017 at 17:55", forms.get(5).getDisplaySubtext());

        assertEquals("Birds", forms.get(6).getDisplayName());
        assertEquals("Added on Wed, Feb 22, 2017 at 17:53", forms.get(6).getDisplaySubtext());

        String sortOrder = FormsProviderAPI.FormsColumns.DISPLAY_NAME + " COLLATE NOCASE DESC";

        cursor = formsDao.getFormsCursor(null, null, null, sortOrder);
        forms = formsDao.getFormsFromCursor(cursor);
        assertEquals(7, forms.size());

        assertEquals("Widgets", forms.get(0).getDisplayName());
        assertEquals("Added on Wed, Feb 22, 2017 at 17:55", forms.get(0).getDisplaySubtext());

        assertEquals("sample", forms.get(1).getDisplayName());
        assertEquals("Added on Wed, Feb 22, 2017 at 17:55", forms.get(1).getDisplaySubtext());

        assertEquals("Miramare", forms.get(2).getDisplayName());
        assertEquals("Added on Wed, Feb 22, 2017 at 17:55", forms.get(2).getDisplaySubtext());

        assertEquals("Geo Tagger v2", forms.get(3).getDisplayName());
        assertEquals("Added on Wed, Feb 22, 2017 at 17:53", forms.get(3).getDisplaySubtext());

        assertEquals("Birds", forms.get(4).getDisplayName());
        assertEquals("Added on Wed, Feb 22, 2017 at 17:53", forms.get(4).getDisplaySubtext());

        assertEquals("Birds", forms.get(5).getDisplayName());
        assertEquals("Added on Wed, Feb 22, 2017 at 17:53", forms.get(5).getDisplaySubtext());

        assertEquals("Biggest N of Set", forms.get(6).getDisplayName());
        assertEquals("Added on Wed, Feb 22, 2017 at 15:21", forms.get(6).getDisplaySubtext());


        String selection = FormsProviderAPI.FormsColumns.DISPLAY_NAME + "=?";
        String[] selectionArgs = {"Miramare"};

        cursor = formsDao.getFormsCursor(null, selection, selectionArgs, null);
        forms = formsDao.getFormsFromCursor(cursor);
        assertEquals(1, forms.size());

        assertEquals("Miramare", forms.get(0).getDisplayName());
        assertEquals("Added on Wed, Feb 22, 2017 at 17:55", forms.get(0).getDisplaySubtext());
    }

    @Test
    public void getFormsCursorForFormFilePathTest() {
        Cursor cursor = formsDao.getFormsCursorForFormFilePath(Collect.FORMS_PATH + "/Miramare.xml");
        List<Form> forms = formsDao.getFormsFromCursor(cursor);
        assertEquals(1, forms.size());

        assertEquals("Miramare", forms.get(0).getDisplayName());
        assertEquals("Added on Wed, Feb 22, 2017 at 17:55", forms.get(0).getDisplaySubtext());
    }

    @Test
    public void updateInstanceTest() {
        Cursor cursor = formsDao.getFormsCursorForFormFilePath(Collect.FORMS_PATH + "/Widgets.xml");
        List<Form> forms = formsDao.getFormsFromCursor(cursor);
        assertEquals(1, forms.size());

        assertEquals("Widgets", forms.get(0).getDisplayName());
        assertEquals("Widgets", forms.get(0).getJrFormId());

        Form form = new Form.Builder()
                .displayName("Widgets")
                .displaySubtext("Added on Wed, Feb 22, 2017 at 17:55")
                .jrFormId("Widgets2")
                .date(1487782554846L)
                .formMediaPath(Collect.FORMS_PATH + "/Widgets-media")
                .formFilePath(Collect.FORMS_PATH + "/Widgets.xml")
                .jrCacheFilePath(Collect.ODK_ROOT + "/.cache/0eacc6333449e66826326eb5fcc75749.formdef")
                .build();

        String where = FormsProviderAPI.FormsColumns.DISPLAY_NAME + "=?";
        String[] whereArgs = {"Widgets"};
        assertEquals(formsDao.updateForm(formsDao.getValuesFromFormObject(form), where, whereArgs), 1);

        cursor = formsDao.getFormsCursorForFormFilePath(Collect.FORMS_PATH + "/Widgets.xml");
        forms = formsDao.getFormsFromCursor(cursor);
        assertEquals(1, forms.size());

        assertEquals("Widgets", forms.get(0).getDisplayName());
        assertEquals("Widgets2", forms.get(0).getJrFormId());
    }

    @Test
    public void getFormMediaPathTest() {
        String mediaPath = formsDao.getFormMediaPath("Birds", "4");
        assertEquals(Collect.FORMS_PATH + "/Birds_4-media", mediaPath);
    }

    private void fillDatabase() throws IOException {
        assertTrue(new File(Collect.FORMS_PATH + "/Biggest N of Set.xml").createNewFile());
        Form form1 = new Form.Builder()
                .displayName("Biggest N of Set")
                .displaySubtext("Added on Wed, Feb 22, 2017 at 15:21")
                .jrFormId("N_Biggest")
                .date(1487773315435L)
                .formMediaPath(Collect.FORMS_PATH + "/Biggest N of Set-media")
                .formFilePath(Collect.FORMS_PATH + "/Biggest N of Set.xml")
                .jrCacheFilePath(Collect.ODK_ROOT + "/.cache/ccce6015dd1b8f935f5f3058e81eeb43.formdef")
                .build();

        formsDao.saveForm(formsDao.getValuesFromFormObject(form1));

        assertTrue(new File(Collect.FORMS_PATH + "/Birds.xml").createNewFile());
        Form form2 = new Form.Builder()
                .displayName("Birds")
                .displaySubtext("Added on Wed, Feb 22, 2017 at 17:53")
                .jrFormId("Birds")
                .jrVersion("3")
                .date(1487782404899L)
                .formMediaPath(Collect.FORMS_PATH + "/Birds-media")
                .formFilePath(Collect.FORMS_PATH + "/Birds.xml")
                .jrCacheFilePath(Collect.ODK_ROOT + "/.cache/4cd980d50f884362afba842cbff3a798.formdef")
                .build();

        formsDao.saveForm(formsDao.getValuesFromFormObject(form2));

        assertTrue(new File(Collect.FORMS_PATH + "/Miramare.xml").createNewFile());
        Form form3 = new Form.Builder()
                .displayName("Miramare")
                .displaySubtext("Added on Wed, Feb 22, 2017 at 17:55")
                .jrFormId("Miramare")
                .date(1487782545945L)
                .formMediaPath(Collect.FORMS_PATH + "/Miramare-media")
                .formFilePath(Collect.FORMS_PATH + "/Miramare.xml")
                .jrCacheFilePath(Collect.ODK_ROOT + "/.cache/e733627cdbf220929bf9c4899cb983ea.formdef")
                .build();

        formsDao.saveForm(formsDao.getValuesFromFormObject(form3));

        assertTrue(new File(Collect.FORMS_PATH + "/Geo Tagger v2.xml").createNewFile());
        Form form4 = new Form.Builder()
                .displayName("Geo Tagger v2")
                .displaySubtext("Added on Wed, Feb 22, 2017 at 17:53")
                .jrFormId("geo_tagger_v2")
                .date(1487782428992L)
                .formMediaPath(Collect.FORMS_PATH + "/Geo Tagger v2-media")
                .formFilePath(Collect.FORMS_PATH + "/Geo Tagger v2.xml")
                .jrCacheFilePath(Collect.ODK_ROOT + "/.cache/1d5e9109298c8ef02bc523b17d7c0451.formdef")
                .build();

        formsDao.saveForm(formsDao.getValuesFromFormObject(form4));

        assertTrue(new File(Collect.FORMS_PATH + "/Widgets.xml").createNewFile());
        Form form5 = new Form.Builder()
                .displayName("Widgets")
                .displaySubtext("Added on Wed, Feb 22, 2017 at 17:55")
                .jrFormId("Widgets")
                .date(1487782554846L)
                .formMediaPath(Collect.FORMS_PATH + "/Widgets-media")
                .formFilePath(Collect.FORMS_PATH + "/Widgets.xml")
                .jrCacheFilePath(Collect.ODK_ROOT + "/.cache/0eacc6333449e66826326eb5fcc75749.formdef")
                .build();

        formsDao.saveForm(formsDao.getValuesFromFormObject(form5));

        assertTrue(new File(Collect.FORMS_PATH + "/sample.xml").createNewFile());
        Form form6 = new Form.Builder()
                .displayName("sample")
                .displaySubtext("Added on Wed, Feb 22, 2017 at 17:55")
                .jrFormId("sample")
                .date(1487782555840L)
                .formMediaPath(Collect.FORMS_PATH + "/sample-media")
                .formFilePath(Collect.FORMS_PATH + "/sample.xml")
                .jrCacheFilePath(Collect.ODK_ROOT + "/.cache/4f495fddd1f2544f65444ea83d25f425.formdef")
                .build();

        formsDao.saveForm(formsDao.getValuesFromFormObject(form6));

        assertTrue(new File(Collect.FORMS_PATH + "/Birds_4.xml").createNewFile());
        Form form7 = new Form.Builder()
                .displayName("Birds")
                .displaySubtext("Added on Wed, Feb 22, 2017 at 17:53")
                .jrFormId("Birds")
                .jrVersion("4")
                .date(1512390303610L)
                .formMediaPath(Collect.FORMS_PATH + "/Birds_4-media")
                .formFilePath(Collect.FORMS_PATH + "/Birds_4.xml")
                .jrCacheFilePath(Collect.ODK_ROOT + "/.cache/4cd980d50f884362afba842cbff3a775.formdef")
                .build();

        formsDao.saveForm(formsDao.getValuesFromFormObject(form7));
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
