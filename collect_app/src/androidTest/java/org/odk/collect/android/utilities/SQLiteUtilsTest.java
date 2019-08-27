package org.odk.collect.android.utilities;

import android.database.sqlite.SQLiteDatabase;

import org.junit.Test;
import org.odk.collect.android.database.helpers.FormsDatabaseHelper;
import org.odk.collect.android.database.helpers.InstancesDatabaseHelper;
import org.odk.collect.android.provider.FormsProviderAPI;
import org.odk.collect.android.provider.InstanceProviderAPI;

import java.io.File;
import java.io.IOException;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static org.odk.collect.android.database.helpers.FormsDatabaseHelper.FORMS_TABLE_NAME;
import static org.odk.collect.android.database.helpers.InstancesDatabaseHelper.INSTANCES_TABLE_NAME;
import static org.odk.collect.android.test.FileUtils.copyFileFromAssets;

public class SQLiteUtilsTest {

    @Test
    public void doesColumnExistTest() throws IOException {
        copyFileFromAssets("database" + File.separator + "formsV7.db", FormsDatabaseHelper.DATABASE_PATH);
        copyFileFromAssets("database" + File.separator + "instancesV5.db", InstancesDatabaseHelper.DATABASE_PATH);

        //forms.db
        SQLiteDatabase formsDb = SQLiteDatabase.openDatabase(FormsDatabaseHelper.DATABASE_PATH, null, SQLiteDatabase.OPEN_READONLY);

        assertTrue(SQLiteUtils.doesColumnExist(formsDb, FORMS_TABLE_NAME, FormsProviderAPI.FormsColumns._ID));
        assertTrue(SQLiteUtils.doesColumnExist(formsDb, FORMS_TABLE_NAME, FormsProviderAPI.FormsColumns.DISPLAY_NAME));
        assertTrue(SQLiteUtils.doesColumnExist(formsDb, FORMS_TABLE_NAME, FormsProviderAPI.FormsColumns.DESCRIPTION));
        assertTrue(SQLiteUtils.doesColumnExist(formsDb, FORMS_TABLE_NAME, FormsProviderAPI.FormsColumns.JR_FORM_ID));
        assertTrue(SQLiteUtils.doesColumnExist(formsDb, FORMS_TABLE_NAME, FormsProviderAPI.FormsColumns.JR_VERSION));
        assertTrue(SQLiteUtils.doesColumnExist(formsDb, FORMS_TABLE_NAME, FormsProviderAPI.FormsColumns.MD5_HASH));
        assertTrue(SQLiteUtils.doesColumnExist(formsDb, FORMS_TABLE_NAME, FormsProviderAPI.FormsColumns.DATE));
        assertTrue(SQLiteUtils.doesColumnExist(formsDb, FORMS_TABLE_NAME, FormsProviderAPI.FormsColumns.FORM_MEDIA_PATH));
        assertTrue(SQLiteUtils.doesColumnExist(formsDb, FORMS_TABLE_NAME, FormsProviderAPI.FormsColumns.FORM_FILE_PATH));
        assertTrue(SQLiteUtils.doesColumnExist(formsDb, FORMS_TABLE_NAME, FormsProviderAPI.FormsColumns.LANGUAGE));
        assertTrue(SQLiteUtils.doesColumnExist(formsDb, FORMS_TABLE_NAME, FormsProviderAPI.FormsColumns.SUBMISSION_URI));
        assertTrue(SQLiteUtils.doesColumnExist(formsDb, FORMS_TABLE_NAME, FormsProviderAPI.FormsColumns.BASE64_RSA_PUBLIC_KEY));
        assertTrue(SQLiteUtils.doesColumnExist(formsDb, FORMS_TABLE_NAME, FormsProviderAPI.FormsColumns.JRCACHE_FILE_PATH));
        assertTrue(SQLiteUtils.doesColumnExist(formsDb, FORMS_TABLE_NAME, FormsProviderAPI.FormsColumns.AUTO_SEND));
        assertTrue(SQLiteUtils.doesColumnExist(formsDb, FORMS_TABLE_NAME, FormsProviderAPI.FormsColumns.AUTO_DELETE));
        assertTrue(SQLiteUtils.doesColumnExist(formsDb, FORMS_TABLE_NAME, FormsProviderAPI.FormsColumns.LAST_DETECTED_FORM_VERSION_HASH));

        assertFalse(SQLiteUtils.doesColumnExist(formsDb, FORMS_TABLE_NAME, "displaySubtext"));

        //instances.db
        SQLiteDatabase instancesDb = SQLiteDatabase.openDatabase(InstancesDatabaseHelper.DATABASE_PATH, null, SQLiteDatabase.OPEN_READONLY);

        assertTrue(SQLiteUtils.doesColumnExist(instancesDb, INSTANCES_TABLE_NAME, InstanceProviderAPI.InstanceColumns._ID));
        assertTrue(SQLiteUtils.doesColumnExist(instancesDb, INSTANCES_TABLE_NAME, InstanceProviderAPI.InstanceColumns.DISPLAY_NAME));
        assertTrue(SQLiteUtils.doesColumnExist(instancesDb, INSTANCES_TABLE_NAME, InstanceProviderAPI.InstanceColumns.SUBMISSION_URI));
        assertTrue(SQLiteUtils.doesColumnExist(instancesDb, INSTANCES_TABLE_NAME, InstanceProviderAPI.InstanceColumns.CAN_EDIT_WHEN_COMPLETE));
        assertTrue(SQLiteUtils.doesColumnExist(instancesDb, INSTANCES_TABLE_NAME, InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH));
        assertTrue(SQLiteUtils.doesColumnExist(instancesDb, INSTANCES_TABLE_NAME, InstanceProviderAPI.InstanceColumns.JR_FORM_ID));
        assertTrue(SQLiteUtils.doesColumnExist(instancesDb, INSTANCES_TABLE_NAME, InstanceProviderAPI.InstanceColumns.JR_VERSION));
        assertTrue(SQLiteUtils.doesColumnExist(instancesDb, INSTANCES_TABLE_NAME, InstanceProviderAPI.InstanceColumns.STATUS));
        assertTrue(SQLiteUtils.doesColumnExist(instancesDb, INSTANCES_TABLE_NAME, InstanceProviderAPI.InstanceColumns.LAST_STATUS_CHANGE_DATE));
        assertTrue(SQLiteUtils.doesColumnExist(instancesDb, INSTANCES_TABLE_NAME, InstanceProviderAPI.InstanceColumns.DELETED_DATE));

        assertFalse(SQLiteUtils.doesColumnExist(instancesDb, INSTANCES_TABLE_NAME, "displaySubtext"));
    }
}
