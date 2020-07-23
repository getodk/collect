package org.odk.collect.android.forms;

import android.content.ContentValues;
import android.database.Cursor;

import org.odk.collect.android.dao.FormsDao;
import org.odk.collect.android.utilities.MultiFormDownloader;

import java.util.List;

import javax.annotation.Nullable;

import static org.odk.collect.android.dao.FormsDao.getFormsFromCursor;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.JR_FORM_ID;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.LAST_DETECTED_FORM_VERSION_HASH;

public class DatabaseFormRepository implements FormRepository {

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
    public Form getByMd5Hash(String hash) {
        FormsDao formsDao = new FormsDao();

        try (Cursor cursor = formsDao.getFormsCursorForMd5Hash(hash)) {
            return getFormOrNull(cursor);
        }
    }

    @Override
    public void save(Form form) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void delete(Long id) {
        new FormsDao().deleteFormsFromIDs(new String[]{id.toString()});
    }

    @Override
    public void setLastDetectedUpdated(String jrFormId, String formHash, String manifestHash) {
        String formVersionHash = MultiFormDownloader.getMd5Hash(formHash) + manifestHash;

        ContentValues values = new ContentValues();
        values.put(LAST_DETECTED_FORM_VERSION_HASH, formVersionHash);
        new FormsDao().updateForm(values, JR_FORM_ID + "=?", new String[]{jrFormId});
    }

    @Override
    public Form getByLastDetectedUpdate(String formHash, String manifestHash) {
        FormsDao formsDao = new FormsDao();
        String formVersionHash = MultiFormDownloader.getMd5Hash(formHash) + manifestHash;

        try (Cursor cursor = formsDao.getFormsCursor(LAST_DETECTED_FORM_VERSION_HASH + "=?", new String[]{formVersionHash})) {
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
