package org.odk.collect.android.forms;

import android.database.Cursor;

import org.odk.collect.android.dao.FormsDao;

import java.util.List;

import javax.annotation.Nullable;

public class DatabaseFormRepository implements FormRepository {

    @Override
    public boolean contains(String jrFormID) {
        try (Cursor cursor = new FormsDao().getFormsCursorForFormId(jrFormID)) {
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
            if (cursor != null && cursor.getCount() > 0) {
                List<Form> forms = formsDao.getFormsFromCursor(cursor);
                return forms.get(0);
            } else {
                return null;
            }
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
}
