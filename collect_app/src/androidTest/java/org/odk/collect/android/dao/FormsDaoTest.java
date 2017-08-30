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

    private final String BIGGEST_N_OF_SET = "Biggest N of Set";
    private final String BIRDS = "Birds";
    private final String MIRAMARE = "Miramare";
    private final String GEO_TAGGER_V2 = "Geo Tagger v2";
    private final String WIDGETS = "Widgets";
    private final String SAMPLE = "sample";

    private final String TIMESTAMP1 = "Added on Wed, Feb 22, 2017 at 15:21";
    private final String TIMESTAMP2 = "Added on Wed, Feb 22, 2017 at 17:53";
    private final String TIMESTAMP3 = "Added on Wed, Feb 22, 2017 at 17:55";

    private final String WIDGETS_FORM = "/Widgets.xml";

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
        assertEquals(6, forms.size());

        assertEquals(BIGGEST_N_OF_SET, forms.get(0).getDisplayName());
        assertEquals(TIMESTAMP1, forms.get(0).getDisplaySubtext());

        assertEquals(BIRDS, forms.get(1).getDisplayName());
        assertEquals(TIMESTAMP2, forms.get(1).getDisplaySubtext());

        assertEquals(MIRAMARE, forms.get(2).getDisplayName());
        assertEquals(TIMESTAMP3, forms.get(2).getDisplaySubtext());

        assertEquals(GEO_TAGGER_V2, forms.get(3).getDisplayName());
        assertEquals(TIMESTAMP2, forms.get(3).getDisplaySubtext());

        assertEquals(WIDGETS, forms.get(4).getDisplayName());
        assertEquals(TIMESTAMP3, forms.get(4).getDisplaySubtext());

        assertEquals(SAMPLE, forms.get(5).getDisplayName());
        assertEquals(TIMESTAMP3, forms.get(5).getDisplaySubtext());
    }

    @Test
    public void getFormsCursorForFormIdTest() {
        Cursor cursor = formsDao.getFormsCursorForFormId(BIRDS);
        List<Form> forms = formsDao.getFormsFromCursor(cursor);
        assertEquals(1, forms.size());

        assertEquals(BIRDS, forms.get(0).getDisplayName());
        assertEquals(TIMESTAMP2, forms.get(0).getDisplaySubtext());
    }

    @Test
    public void getFormsCursorTest() {
        Cursor cursor = formsDao.getFormsCursor(null, null, null, null);
        List<Form> forms = formsDao.getFormsFromCursor(cursor);
        assertEquals(6, forms.size());

        assertEquals(BIGGEST_N_OF_SET, forms.get(0).getDisplayName());
        assertEquals(TIMESTAMP1, forms.get(0).getDisplaySubtext());

        assertEquals(BIRDS, forms.get(1).getDisplayName());
        assertEquals(TIMESTAMP2, forms.get(1).getDisplaySubtext());

        assertEquals(MIRAMARE, forms.get(2).getDisplayName());
        assertEquals(TIMESTAMP3, forms.get(2).getDisplaySubtext());

        assertEquals(GEO_TAGGER_V2, forms.get(3).getDisplayName());
        assertEquals(TIMESTAMP2, forms.get(3).getDisplaySubtext());

        assertEquals(WIDGETS, forms.get(4).getDisplayName());
        assertEquals(TIMESTAMP3, forms.get(4).getDisplaySubtext());

        assertEquals(SAMPLE, forms.get(5).getDisplayName());
        assertEquals(TIMESTAMP3, forms.get(5).getDisplaySubtext());

        String sortOrder = FormsProviderAPI.FormsColumns.DISPLAY_NAME + " DESC";

        cursor = formsDao.getFormsCursor(null, null, null, sortOrder);
        forms = formsDao.getFormsFromCursor(cursor);
        assertEquals(6, forms.size());

        assertEquals(SAMPLE, forms.get(0).getDisplayName());
        assertEquals(TIMESTAMP3, forms.get(0).getDisplaySubtext());

        assertEquals(WIDGETS, forms.get(1).getDisplayName());
        assertEquals(TIMESTAMP3, forms.get(1).getDisplaySubtext());

        assertEquals(MIRAMARE, forms.get(2).getDisplayName());
        assertEquals(TIMESTAMP3, forms.get(2).getDisplaySubtext());

        assertEquals(GEO_TAGGER_V2, forms.get(3).getDisplayName());
        assertEquals(TIMESTAMP2, forms.get(3).getDisplaySubtext());

        assertEquals(BIRDS, forms.get(4).getDisplayName());
        assertEquals(TIMESTAMP2, forms.get(4).getDisplaySubtext());

        assertEquals(BIGGEST_N_OF_SET, forms.get(5).getDisplayName());
        assertEquals(TIMESTAMP1, forms.get(5).getDisplaySubtext());


        String selection = FormsProviderAPI.FormsColumns.DISPLAY_NAME + "=?";
        String[] selectionArgs = {MIRAMARE};

        cursor = formsDao.getFormsCursor(null, selection, selectionArgs, null);
        forms = formsDao.getFormsFromCursor(cursor);
        assertEquals(1, forms.size());

        assertEquals(MIRAMARE, forms.get(0).getDisplayName());
        assertEquals(TIMESTAMP3, forms.get(0).getDisplaySubtext());
    }

    @Test
    public void getFormsCursorForFormFilePathTest() {
        Cursor cursor = formsDao.getFormsCursorForFormFilePath(Collect.FORMS_PATH + "/Miramare.xml");
        List<Form> forms = formsDao.getFormsFromCursor(cursor);
        assertEquals(1, forms.size());

        assertEquals(MIRAMARE, forms.get(0).getDisplayName());
        assertEquals(TIMESTAMP3, forms.get(0).getDisplaySubtext());
    }

    @Test
    public void updateInstanceTest() {
        Cursor cursor = formsDao.getFormsCursorForFormFilePath(Collect.FORMS_PATH + WIDGETS_FORM);
        List<Form> forms = formsDao.getFormsFromCursor(cursor);
        assertEquals(1, forms.size());

        assertEquals(WIDGETS, forms.get(0).getDisplayName());
        assertEquals(WIDGETS, forms.get(0).getJrFormId());

        Form form = new Form.Builder()
                .displayName(WIDGETS)
                .displaySubtext(TIMESTAMP3)
                .jrFormId("Widgets2")
                .date(1487782554846L)
                .formMediaPath(Collect.FORMS_PATH + "/Widgets-media")
                .formFilePath(Collect.FORMS_PATH + WIDGETS_FORM)
                .jrCacheFilePath(Collect.ODK_ROOT + "/.cache/0eacc6333449e66826326eb5fcc75749.formdef")
                .build();

        String where = FormsProviderAPI.FormsColumns.DISPLAY_NAME + "=?";
        String[] whereArgs = {WIDGETS};
        assertEquals(formsDao.updateForm(formsDao.getValuesFromFormObject(form), where, whereArgs), 1);

        cursor = formsDao.getFormsCursorForFormFilePath(Collect.FORMS_PATH + WIDGETS_FORM);
        forms = formsDao.getFormsFromCursor(cursor);
        assertEquals(1, forms.size());

        assertEquals(WIDGETS, forms.get(0).getDisplayName());
        assertEquals("Widgets2", forms.get(0).getJrFormId());
    }

    private void fillDatabase() throws IOException {
        assertTrue(new File(Collect.FORMS_PATH + "/Biggest N of Set.xml").createNewFile());
        Form form1 = new Form.Builder()
                .displayName(BIGGEST_N_OF_SET)
                .displaySubtext(TIMESTAMP1)
                .jrFormId("N_Biggest")
                .date(1487773315435L)
                .formMediaPath(Collect.FORMS_PATH + "/Biggest N of Set-media")
                .formFilePath(Collect.FORMS_PATH + "/Biggest N of Set.xml")
                .jrCacheFilePath(Collect.ODK_ROOT + "/.cache/ccce6015dd1b8f935f5f3058e81eeb43.formdef")
                .build();

        formsDao.saveForm(formsDao.getValuesFromFormObject(form1));

        assertTrue(new File(Collect.FORMS_PATH + "/Birds.xml").createNewFile());
        Form form2 = new Form.Builder()
                .displayName(BIRDS)
                .displaySubtext(TIMESTAMP2)
                .jrFormId(BIRDS)
                .date(1487782404899L)
                .formMediaPath(Collect.FORMS_PATH + "/Birds-media")
                .formFilePath(Collect.FORMS_PATH + "/Birds.xml")
                .jrCacheFilePath(Collect.ODK_ROOT + "/.cache/4cd980d50f884362afba842cbff3a798.formdef")
                .build();

        formsDao.saveForm(formsDao.getValuesFromFormObject(form2));

        assertTrue(new File(Collect.FORMS_PATH + "/Miramare.xml").createNewFile());
        Form form3 = new Form.Builder()
                .displayName(MIRAMARE)
                .displaySubtext(TIMESTAMP3)
                .jrFormId(MIRAMARE)
                .date(1487782545945L)
                .formMediaPath(Collect.FORMS_PATH + "/Miramare-media")
                .formFilePath(Collect.FORMS_PATH + "/Miramare.xml")
                .jrCacheFilePath(Collect.ODK_ROOT + "/.cache/e733627cdbf220929bf9c4899cb983ea.formdef")
                .build();

        formsDao.saveForm(formsDao.getValuesFromFormObject(form3));

        assertTrue(new File(Collect.FORMS_PATH + "/Geo Tagger v2.xml").createNewFile());
        Form form4 = new Form.Builder()
                .displayName(GEO_TAGGER_V2)
                .displaySubtext(TIMESTAMP2)
                .jrFormId("geo_tagger_v2")
                .date(1487782428992L)
                .formMediaPath(Collect.FORMS_PATH + "/Geo Tagger v2-media")
                .formFilePath(Collect.FORMS_PATH + "/Geo Tagger v2.xml")
                .jrCacheFilePath(Collect.ODK_ROOT + "/.cache/1d5e9109298c8ef02bc523b17d7c0451.formdef")
                .build();

        formsDao.saveForm(formsDao.getValuesFromFormObject(form4));

        assertTrue(new File(Collect.FORMS_PATH + WIDGETS_FORM).createNewFile());
        Form form5 = new Form.Builder()
                .displayName(WIDGETS)
                .displaySubtext(TIMESTAMP3)
                .jrFormId(WIDGETS)
                .date(1487782554846L)
                .formMediaPath(Collect.FORMS_PATH + "/Widgets-media")
                .formFilePath(Collect.FORMS_PATH + WIDGETS_FORM)
                .jrCacheFilePath(Collect.ODK_ROOT + "/.cache/0eacc6333449e66826326eb5fcc75749.formdef")
                .build();

        formsDao.saveForm(formsDao.getValuesFromFormObject(form5));

        assertTrue(new File(Collect.FORMS_PATH + "/sample.xml").createNewFile());
        Form form6 = new Form.Builder()
                .displayName(SAMPLE)
                .displaySubtext(TIMESTAMP3)
                .jrFormId(SAMPLE)
                .date(1487782555840L)
                .formMediaPath(Collect.FORMS_PATH + "/sample-media")
                .formFilePath(Collect.FORMS_PATH + "/sample.xml")
                .jrCacheFilePath(Collect.ODK_ROOT + "/.cache/4f495fddd1f2544f65444ea83d25f425.formdef")
                .build();

        formsDao.saveForm(formsDao.getValuesFromFormObject(form6));
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

        List<Integer> failedResetActions = new ResetUtility().reset(Collect.getInstance(), resetActions);
        assertEquals(0, failedResetActions.size());
    }
}
