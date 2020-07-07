package org.odk.collect.android.forms;

import android.database.Cursor;

import org.odk.collect.android.dao.FormsDao;

import java.util.List;

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

    @Override
    public void save(Form form) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void delete(Long id) {
        new FormsDao().deleteFormsFromIDs(new String[]{id.toString()});
    }
}
