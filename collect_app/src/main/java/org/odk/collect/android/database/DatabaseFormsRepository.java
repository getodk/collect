package org.odk.collect.android.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import org.odk.collect.android.dao.FormsDao;
import org.odk.collect.android.storage.StoragePathProvider;
import org.odk.collect.android.forms.Form;
import org.odk.collect.android.forms.FormsRepository;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import static android.provider.BaseColumns._ID;
import static org.odk.collect.android.dao.FormsDao.getFormsFromCursor;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.AUTO_DELETE;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.AUTO_SEND;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.BASE64_RSA_PUBLIC_KEY;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.DELETED_DATE;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.DISPLAY_NAME;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.FORM_FILE_PATH;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.FORM_MEDIA_PATH;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.GEOMETRY_XPATH;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.JR_FORM_ID;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.JR_VERSION;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.SUBMISSION_URI;

public class DatabaseFormsRepository implements FormsRepository {

    private final StoragePathProvider storagePathProvider;

    public DatabaseFormsRepository() {
        storagePathProvider = new StoragePathProvider();
    }

    @Override
    public List<Form> getByJrFormIdNotDeleted(String jrFormId) {
        return queryForForms(JR_FORM_ID + "=? AND " + DELETED_DATE + " IS NULL", new String[]{jrFormId});
    }

    @Override
    public List<Form> getAll() {
        try (Cursor cursor = new FormsDao().getFormsCursor()) {
            return new FormsDao().getFormsFromCursor(cursor);
        }
    }

    @Nullable
    @Override
    public Form get(Long id) {
        return queryForForm(_ID + "=?", new String[]{id.toString()});
    }

    @Nullable
    @Override
    public Form get(String jrFormId, @Nullable String jrVersion) {
        if (jrVersion != null) {
            return queryForForm(JR_FORM_ID + "=? AND " + JR_VERSION + "=?", new String[]{jrFormId, jrVersion});
        } else {
            return queryForForm(JR_FORM_ID + "=? AND " + JR_VERSION + " IS NULL", new String[]{jrFormId});
        }
    }

    @Nullable
    @Override
    public Form getByMd5Hash(String hash) {
        FormsDao formsDao = new FormsDao();

        try (Cursor cursor = formsDao.getFormsCursorForMd5Hash(hash)) {
            return getFormOrNull(cursor);
        }
    }

    @Nullable
    @Override
    public Form getByPath(String path) {
        try (Cursor cursor = new FormsDao().getFormsCursorForFormFilePath(path)) {
            return getFormOrNull(cursor);
        }
    }

    @Override
    public Uri save(Form form) {
        final ContentValues v = new ContentValues();
        v.put(FORM_FILE_PATH, storagePathProvider.getFormDbPath(form.getFormFilePath()));
        v.put(FORM_MEDIA_PATH, storagePathProvider.getFormDbPath(form.getFormMediaPath()));
        v.put(DISPLAY_NAME, form.getDisplayName());
        v.put(JR_VERSION, form.getJrVersion());
        v.put(JR_FORM_ID, form.getJrFormId());
        v.put(SUBMISSION_URI, form.getSubmissionUri());
        v.put(BASE64_RSA_PUBLIC_KEY, form.getBASE64RSAPublicKey());
        v.put(AUTO_DELETE, form.getAutoDelete());
        v.put(AUTO_SEND, form.getAutoSend());
        v.put(GEOMETRY_XPATH, form.getGeometryXpath());

        if (form.isDeleted()) {
            v.put(DELETED_DATE, 0L);
        } else {
            v.putNull(DELETED_DATE);
        }

        return new FormsDao().saveForm(v);
    }

    @Override
    public void delete(Long id) {
        new FormsDao().deleteFormsFromIDs(new String[]{id.toString()});
    }

    @Override
    public void softDelete(Long id) {
        ContentValues values = new ContentValues();
        values.put(DELETED_DATE, System.currentTimeMillis());
        new FormsDao().updateForm(values, _ID + "=?", new String[]{id.toString()});
    }

    @Override
    public void restore(Long id) {
        ContentValues values = new ContentValues();
        values.putNull(DELETED_DATE);
        new FormsDao().updateForm(values, _ID + "=?", new String[]{id.toString()});
    }

    @Override
    public void deleteFormsByMd5Hash(String md5Hash) {
        FormsDao formsDao = new FormsDao();
        List<String> idsToDelete = new ArrayList<>();
        Cursor c = null;
        try {
            for (String hash : new String[]{md5Hash}) {
                c = formsDao.getFormsCursorForMd5Hash(hash);
                if (c != null && c.moveToFirst()) {
                    String id = c.getString(c.getColumnIndex(_ID));
                    idsToDelete.add(id);
                    c.close();
                    c = null;
                }
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }

        formsDao.deleteFormsFromIDs(idsToDelete.toArray(new String[idsToDelete.size()]));
    }

    @Nullable
    private Form queryForForm(String selection, String[] selectionArgs) {
        try (Cursor cursor = new FormsDao().getFormsCursor(selection, selectionArgs)) {
            return getFormOrNull(cursor);
        }
    }

    @Nullable
    private List<Form> queryForForms(String selection, String[] selectionArgs) {
        try (Cursor cursor = new FormsDao().getFormsCursor(selection, selectionArgs)) {
            return new FormsDao().getFormsFromCursor(cursor);
        }
    }

    private Form getFormOrNull(Cursor cursor) {
        if (cursor != null && cursor.getCount() > 0) {
            List<Form> forms = getFormsFromCursor(cursor);
            return forms.get(0);
        } else {
            return null;
        }
    }
}
