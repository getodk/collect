package org.odk.collect.android.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import org.odk.collect.android.dao.FormsDao;
import org.odk.collect.android.forms.Form;
import org.odk.collect.android.forms.FormsRepository;
import org.odk.collect.android.provider.FormsProviderAPI;
import org.odk.collect.android.storage.StoragePathProvider;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import static android.provider.BaseColumns._ID;
import static org.odk.collect.android.dao.FormsDao.getFormsFromCursor;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.DELETED;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.JR_FORM_ID;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.JR_VERSION;

public class DatabaseFormsRepository implements FormsRepository {

    @Override
    public boolean contains(String jrFormId) {
        try (Cursor cursor = new FormsDao().getFormsCursorForFormId(jrFormId)) {
            return cursor != null && cursor.getCount() > 0;
        }
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
        return getFormOrNull(new FormsDao().getFormsCursorForFormFilePath(path));
    }

    @Override
    public Uri save(Form form) {
        final ContentValues v = new ContentValues();
        v.put(FormsProviderAPI.FormsColumns.FORM_FILE_PATH, new StoragePathProvider().getFormDbPath(form.getFormFilePath()));
        v.put(FormsProviderAPI.FormsColumns.FORM_MEDIA_PATH, new StoragePathProvider().getFormDbPath(form.getFormMediaPath()));
        v.put(FormsProviderAPI.FormsColumns.DISPLAY_NAME, form.getDisplayName());
        v.put(FormsProviderAPI.FormsColumns.JR_VERSION, form.getJrVersion());
        v.put(FormsProviderAPI.FormsColumns.JR_FORM_ID, form.getJrFormId());
        v.put(FormsProviderAPI.FormsColumns.SUBMISSION_URI, form.getSubmissionUri());
        v.put(FormsProviderAPI.FormsColumns.BASE64_RSA_PUBLIC_KEY, form.getBASE64RSAPublicKey());
        v.put(FormsProviderAPI.FormsColumns.AUTO_DELETE, form.getAutoDelete());
        v.put(FormsProviderAPI.FormsColumns.AUTO_SEND, form.getAutoSend());
        v.put(FormsProviderAPI.FormsColumns.GEOMETRY_XPATH, form.getGeometryXpath());
        return new FormsDao().saveForm(v);
    }

    @Override
    public void delete(Long id) {
        new FormsDao().deleteFormsFromIDs(new String[]{id.toString()});
    }

    @Override
    public void softDelete(Long id) {
        ContentValues values = new ContentValues();
        values.put(DELETED, 1);
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
                    String id = c.getString(c.getColumnIndex(FormsProviderAPI.FormsColumns._ID));
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

    private Form getFormOrNull(Cursor cursor) {
        if (cursor != null && cursor.getCount() > 0) {
            List<Form> forms = getFormsFromCursor(cursor);
            return forms.get(0);
        } else {
            return null;
        }
    }
}
