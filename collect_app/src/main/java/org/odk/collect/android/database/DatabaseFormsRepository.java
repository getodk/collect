package org.odk.collect.android.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;

import org.jetbrains.annotations.NotNull;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.forms.Form;
import org.odk.collect.android.forms.FormsRepository;
import org.odk.collect.android.injection.DaggerUtils;
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
import static org.odk.collect.android.database.DatabaseFormColumns.DATE;
import static org.odk.collect.android.database.DatabaseFormColumns.DELETED_DATE;
import static org.odk.collect.android.database.DatabaseFormColumns.FORM_FILE_PATH;
import static org.odk.collect.android.database.DatabaseFormColumns.FORM_MEDIA_PATH;
import static org.odk.collect.android.database.DatabaseFormColumns.JRCACHE_FILE_PATH;
import static org.odk.collect.android.database.DatabaseFormColumns.JR_FORM_ID;
import static org.odk.collect.android.database.DatabaseFormColumns.JR_VERSION;
import static org.odk.collect.android.database.DatabaseFormColumns.MD5_HASH;
import static org.odk.collect.android.forms.FormUtils.getFormFromCurrentCursorPosition;
import static org.odk.collect.android.forms.FormUtils.getValuesFromForm;

public class DatabaseFormsRepository implements FormsRepository {

    private final StoragePathProvider storagePathProvider;
    private final Clock clock;
    private final FormsDatabaseProvider formsDatabaseProvider;

    public DatabaseFormsRepository() {
        this(System::currentTimeMillis);
    }

    public DatabaseFormsRepository(Clock clock) {
        this.clock = clock;
        this.storagePathProvider = new StoragePathProvider();
        this.formsDatabaseProvider = DaggerUtils.getComponent(Collect.getInstance()).formsDatabaseProvider();
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

        String selection = DatabaseFormColumns.MD5_HASH + "=?";
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
        final ContentValues values = getValuesFromForm(form, storagePathProvider);

        String md5Hash = FileUtils.getMd5Hash(new File(form.getFormFilePath()));
        values.put(MD5_HASH, md5Hash);
        values.put(FORM_MEDIA_PATH, storagePathProvider.getRelativeFormPath(FileUtils.constructMediaPath(form.getFormFilePath())));
        values.put(JRCACHE_FILE_PATH, md5Hash + ".formdef");

        if (form.isDeleted()) {
            values.put(DELETED_DATE, 0L);
        } else {
            values.putNull(DELETED_DATE);
        }

        if (form.getDbId() == null) {
            values.put(DATE, clock.getCurrentTime());

            Long idFromUri = insertForm(values);
            return get(idFromUri);
        } else {
            updateForm(form.getDbId(), values);
            return get(form.getDbId());
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
        String selection = DatabaseFormColumns.MD5_HASH + "=?";
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
    public Cursor rawQuery(String[] projection, String selection, String[] selectionArgs, String sortOrder, String groupBy) {
        return queryAndReturnCursor(projection, selection, selectionArgs, sortOrder, groupBy);
    }

    @Nullable
    private Form queryForForm(String selection, String[] selectionArgs) {
        List<Form> forms = queryForForms(selection, selectionArgs);
        return !forms.isEmpty() ? forms.get(0) : null;
    }

    private List<Form> queryForForms(String selection, String[] selectionArgs) {
        try (Cursor cursor = queryAndReturnCursor(null, selection, selectionArgs, null, null)) {
            return getFormsFromCursor(cursor, storagePathProvider);
        }
    }

    private Cursor queryAndReturnCursor(String[] projection, String selection, String[] selectionArgs, String sortOrder, String groupBy) {
        SQLiteDatabase readableDatabase = formsDatabaseProvider.getReadableDatabase();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(FORMS_TABLE_NAME);
        return qb.query(readableDatabase, projection, selection, selectionArgs, groupBy, null, sortOrder);
    }

    private Long insertForm(ContentValues values) {
        SQLiteDatabase writeableDatabase = formsDatabaseProvider.getWriteableDatabase();
        return writeableDatabase.insertOrThrow(FORMS_TABLE_NAME, null, values);
    }

    private void updateForm(Long id, ContentValues values) {
        SQLiteDatabase writeableDatabase = formsDatabaseProvider.getWriteableDatabase();
        writeableDatabase.update(FORMS_TABLE_NAME, values, _ID + "=?", new String[]{String.valueOf(id)});
    }

    private void deleteForms(String selection, String[] selectionArgs) {
        List<Form> forms = queryForForms(selection, selectionArgs);
        for (Form form : forms) {
            deleteFilesForForm(form);
        }

        SQLiteDatabase writeableDatabase = formsDatabaseProvider.getWriteableDatabase();
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
    }
}
