package org.odk.collect.android.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;

import org.jetbrains.annotations.NotNull;
import org.odk.collect.android.fastexternalitemset.ItemsetDbAdapter;
import org.odk.collect.android.forms.Form;
import org.odk.collect.android.forms.FormsRepository;
import org.odk.collect.android.provider.FormsProviderAPI;
import org.odk.collect.android.storage.StoragePathProvider;
import org.odk.collect.android.utilities.FileUtils;
import org.odk.collect.utilities.Clock;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.annotation.Nullable;

import static android.provider.BaseColumns._ID;
import static org.apache.commons.io.FileUtils.deleteDirectory;
import static org.odk.collect.android.database.DatabaseConstants.FORMS_TABLE_NAME;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.AUTO_DELETE;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.AUTO_SEND;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.BASE64_RSA_PUBLIC_KEY;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.DATE;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.DELETED_DATE;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.DISPLAY_NAME;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.FORM_FILE_PATH;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.FORM_MEDIA_PATH;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.GEOMETRY_XPATH;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.JRCACHE_FILE_PATH;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.JR_FORM_ID;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.JR_VERSION;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.MD5_HASH;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.SUBMISSION_URI;

public class DatabaseFormsRepository implements FormsRepository {

    private final StoragePathProvider storagePathProvider;
    private final Clock clock;

    public DatabaseFormsRepository() {
        this(System::currentTimeMillis);
    }

    public DatabaseFormsRepository(Clock clock) {
        this.clock = clock;
        this.storagePathProvider = new StoragePathProvider();
    }

    @Nullable
    @Override
    public Form get(Long id) {
        return queryForForm(_ID + "=?", new String[]{id.toString()});
    }

    @Nullable
    @Override
    public Form getLatestByFormIdAndVersion(String jrFormId, @Nullable String jrVersion) {
        List<Form> all = getAllByFormIdAndVersion(jrFormId, jrVersion);
        if (!all.isEmpty()) {
            return all.stream().max(Comparator.comparingLong(Form::getDate)).get();
        } else {
            return null;
        }
    }

    @Nullable
    @Override
    public Form getOneByPath(String path) {
        String selection = FORM_FILE_PATH + "=?";
        String[] selectionArgs = {new StoragePathProvider().getRelativeFormPath(path)};
        return queryForForm(selection, selectionArgs);
    }

    @Nullable
    @Override
    public Form getOneByMd5Hash(@NotNull String hash) {
        if (hash == null) {
            throw new IllegalArgumentException("null hash");
        }

        String selection = FormsProviderAPI.FormsColumns.MD5_HASH + "=?";
        String[] selectionArgs = {hash};
        return queryForForm(selection, selectionArgs);
    }

    @Override
    public List<Form> getAll() {
        return queryForForms(null, null);
    }

    @Override
    public List<Form> getAllByFormIdAndVersion(String jrFormId, @Nullable String jrVersion) {
        if (jrVersion != null) {
            return queryForForms(JR_FORM_ID + "=? AND " + JR_VERSION + "=?", new String[]{jrFormId, jrVersion});
        } else {
            return queryForForms(JR_FORM_ID + "=? AND " + JR_VERSION + " IS NULL", new String[]{jrFormId});
        }
    }

    @Override
    public List<Form> getAllByFormId(String formId) {
        return queryForForms(JR_FORM_ID + "=?", new String[]{formId});
    }

    @Override
    public List<Form> getAllNotDeletedByFormId(String jrFormId) {
        return queryForForms(JR_FORM_ID + "=? AND " + DELETED_DATE + " IS NULL", new String[]{jrFormId});
    }


    @Override
    public List<Form> getAllNotDeletedByFormIdAndVersion(String jrFormId, @Nullable String jrVersion) {
        if (jrVersion != null) {
            return queryForForms(DELETED_DATE + " IS NULL AND " + JR_FORM_ID + "=? AND " + JR_VERSION + "=?", new String[]{jrFormId, jrVersion});
        } else {
            return queryForForms(DELETED_DATE + " IS NULL AND " + JR_FORM_ID + "=? AND " + JR_VERSION + " IS NULL", new String[]{jrFormId});
        }
    }

    @Override
    public Form save(@NotNull Form form) {
        final ContentValues values = getValuesFromFormObject(form, storagePathProvider);

        values.put(MD5_HASH, FileUtils.getMd5Hash(new File(form.getFormFilePath())));
        values.put(FORM_MEDIA_PATH, FileUtils.constructMediaPath(form.getFormFilePath()));

        if (form.isDeleted()) {
            values.put(DELETED_DATE, 0L);
        } else {
            values.putNull(DELETED_DATE);
        }

        if (form.getId() == null) {
            values.put(JRCACHE_FILE_PATH, "");
            values.put(DATE, clock.getCurrentTime());

            Long idFromUri = insertForm(values);
            return get(idFromUri);
        } else {
            updateForm(form.getId(), values);
            return get(form.getId());
        }
    }

    @Override
    public void delete(Long id) {
        String selection = _ID + "=?";
        String[] selectionArgs = {String.valueOf(id)};

        deleteForms(selection, selectionArgs);
    }

    @Override
    public void softDelete(Long id) {
        ContentValues values = new ContentValues();
        values.put(DELETED_DATE, System.currentTimeMillis());
        updateForm(id, values);
    }

    @Override
    public void deleteByMd5Hash(@NotNull String md5Hash) {
        String selection = FormsProviderAPI.FormsColumns.MD5_HASH + "=?";
        String[] selectionArgs = {md5Hash};

        deleteForms(selection, selectionArgs);
    }

    @Override
    public void deleteAll() {
        deleteForms(null, null);
    }

    @Override
    public void restore(Long id) {
        ContentValues values = new ContentValues();
        values.putNull(DELETED_DATE);
        updateForm(id, values);
    }

    @Override
    public Cursor rawQuery(String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return queryAndReturnCursor(projection, selection, selectionArgs, sortOrder);
    }

    @Nullable
    private Form queryForForm(String selection, String[] selectionArgs) {
        List<Form> forms = queryForForms(selection, selectionArgs);
        return !forms.isEmpty() ? forms.get(0) : null;
    }

    private List<Form> queryForForms(String selection, String[] selectionArgs) {
        Cursor cursor = queryAndReturnCursor(null, selection, selectionArgs, null);
        return getFormsFromCursor(cursor, storagePathProvider);
    }

    private Cursor queryAndReturnCursor(String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        FormsDatabaseHelper formsDatabaseHelper = FormsDatabaseHelper.getDbHelper();
        SQLiteDatabase readableDatabase = formsDatabaseHelper.getReadableDatabase();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(FORMS_TABLE_NAME);
        return qb.query(readableDatabase, projection, selection, selectionArgs, null, null, sortOrder);
    }

    private Long insertForm(ContentValues values) {
        FormsDatabaseHelper formsDatabaseHelper = FormsDatabaseHelper.getDbHelper();
        SQLiteDatabase writeableDatabase = formsDatabaseHelper.getWritableDatabase();
        return writeableDatabase.insertOrThrow(FORMS_TABLE_NAME, null, values);
    }

    private void updateForm(Long id, ContentValues values) {
        FormsDatabaseHelper formsDatabaseHelper = FormsDatabaseHelper.getDbHelper();
        SQLiteDatabase writeableDatabase = formsDatabaseHelper.getWritableDatabase();
        writeableDatabase.update(FORMS_TABLE_NAME, values, _ID + "=?", new String[]{String.valueOf(id)});
    }

    private void deleteForms(String selection, String[] selectionArgs) {
        List<Form> forms = queryForForms(selection, selectionArgs);
        for (Form form : forms) {
            deleteFilesForForm(form);
        }

        FormsDatabaseHelper formsDatabaseHelper = FormsDatabaseHelper.getDbHelper();
        SQLiteDatabase writeableDatabase = formsDatabaseHelper.getWritableDatabase();
        writeableDatabase.delete(FORMS_TABLE_NAME, selection, selectionArgs);
    }

    @NotNull
    private static List<Form> getFormsFromCursor(Cursor cursor, StoragePathProvider storagePathProvider) {
        List<Form> forms = new ArrayList<>();
        if (cursor != null) {
            cursor.moveToPosition(-1);
            while (cursor.moveToNext()) {
                Form form = getFormFromCurrentCursorPosition(cursor, storagePathProvider);

                forms.add(form);
            }

        }
        return forms;
    }

    private static Form getFormFromCurrentCursorPosition(Cursor cursor, StoragePathProvider storagePathProvider) {
        int idColumnIndex = cursor.getColumnIndex(_ID);
        int displayNameColumnIndex = cursor.getColumnIndex(DISPLAY_NAME);
        int descriptionColumnIndex = cursor.getColumnIndex(FormsProviderAPI.FormsColumns.DESCRIPTION);
        int jrFormIdColumnIndex = cursor.getColumnIndex(JR_FORM_ID);
        int jrVersionColumnIndex = cursor.getColumnIndex(JR_VERSION);
        int formFilePathColumnIndex = cursor.getColumnIndex(FORM_FILE_PATH);
        int submissionUriColumnIndex = cursor.getColumnIndex(SUBMISSION_URI);
        int base64RSAPublicKeyColumnIndex = cursor.getColumnIndex(BASE64_RSA_PUBLIC_KEY);
        int md5HashColumnIndex = cursor.getColumnIndex(FormsProviderAPI.FormsColumns.MD5_HASH);
        int dateColumnIndex = cursor.getColumnIndex(FormsProviderAPI.FormsColumns.DATE);
        int jrCacheFilePathColumnIndex = cursor.getColumnIndex(FormsProviderAPI.FormsColumns.JRCACHE_FILE_PATH);
        int formMediaPathColumnIndex = cursor.getColumnIndex(FORM_MEDIA_PATH);
        int languageColumnIndex = cursor.getColumnIndex(FormsProviderAPI.FormsColumns.LANGUAGE);
        int autoSendColumnIndex = cursor.getColumnIndex(AUTO_SEND);
        int autoDeleteColumnIndex = cursor.getColumnIndex(AUTO_DELETE);
        int geometryXpathColumnIndex = cursor.getColumnIndex(GEOMETRY_XPATH);
        int deletedDateColumnIndex = cursor.getColumnIndex(DELETED_DATE);

        return new Form.Builder()
                .id(cursor.getLong(idColumnIndex))
                .displayName(cursor.getString(displayNameColumnIndex))
                .description(cursor.getString(descriptionColumnIndex))
                .jrFormId(cursor.getString(jrFormIdColumnIndex))
                .jrVersion(cursor.getString(jrVersionColumnIndex))
                .formFilePath(storagePathProvider.getAbsoluteFormFilePath(cursor.getString(formFilePathColumnIndex)))
                .submissionUri(cursor.getString(submissionUriColumnIndex))
                .base64RSAPublicKey(cursor.getString(base64RSAPublicKeyColumnIndex))
                .md5Hash(cursor.getString(md5HashColumnIndex))
                .date(cursor.getLong(dateColumnIndex))
                .jrCacheFilePath(storagePathProvider.getAbsoluteCacheFilePath(cursor.getString(jrCacheFilePathColumnIndex)))
                .formMediaPath(storagePathProvider.getAbsoluteFormFilePath(cursor.getString(formMediaPathColumnIndex)))
                .language(cursor.getString(languageColumnIndex))
                .autoSend(cursor.getString(autoSendColumnIndex))
                .autoDelete(cursor.getString(autoDeleteColumnIndex))
                .geometryXpath(cursor.getString(geometryXpathColumnIndex))
                .deleted(!cursor.isNull(deletedDateColumnIndex))
                .build();
    }

    private static ContentValues getValuesFromFormObject(Form form, StoragePathProvider storagePathProvider) {
        ContentValues values = new ContentValues();
        values.put(FormsProviderAPI.FormsColumns.DISPLAY_NAME, form.getDisplayName());
        values.put(FormsProviderAPI.FormsColumns.DESCRIPTION, form.getDescription());
        values.put(FormsProviderAPI.FormsColumns.JR_FORM_ID, form.getJrFormId());
        values.put(FormsProviderAPI.FormsColumns.JR_VERSION, form.getJrVersion());
        values.put(FormsProviderAPI.FormsColumns.FORM_FILE_PATH, storagePathProvider.getRelativeFormPath(form.getFormFilePath()));
        values.put(FormsProviderAPI.FormsColumns.SUBMISSION_URI, form.getSubmissionUri());
        values.put(FormsProviderAPI.FormsColumns.BASE64_RSA_PUBLIC_KEY, form.getBASE64RSAPublicKey());
        values.put(FormsProviderAPI.FormsColumns.MD5_HASH, form.getMD5Hash());
        values.put(FormsProviderAPI.FormsColumns.FORM_MEDIA_PATH, storagePathProvider.getRelativeFormPath(form.getFormMediaPath()));
        values.put(FormsProviderAPI.FormsColumns.LANGUAGE, form.getLanguage());
        values.put(FormsProviderAPI.FormsColumns.AUTO_SEND, form.getAutoSend());
        values.put(FormsProviderAPI.FormsColumns.AUTO_DELETE, form.getAutoDelete());
        values.put(FormsProviderAPI.FormsColumns.GEOMETRY_XPATH, form.getGeometryXpath());

        return values;
    }

    private void deleteFilesForForm(Form form) {
        // Delete form file
        if (form.getFormFilePath() != null) {
            new File(form.getFormFilePath()).delete();
        }

        // Delete cache file
        if (form.getJrCacheFilePath() != null) {
            new File(form.getJrCacheFilePath()).delete();
        }

        // Delete media files
        if (form.getFormMediaPath() != null) {
            try {
                File mediaDir = new File(form.getFormMediaPath());

                if (mediaDir.isDirectory()) {
                    deleteDirectory(mediaDir);
                } else {
                    mediaDir.delete();
                }
            } catch (IOException ignored) {
                // Ignored
            }
        }

        // Delete itemsets
        ItemsetDbAdapter ida = new ItemsetDbAdapter();
        ida.open();
        ida.delete(form.getFormMediaPath() + "/itemsets.csv");
        ida.close();
    }
}
