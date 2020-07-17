package org.odk.collect.android.notifications;

import android.content.ContentValues;
import android.database.Cursor;

import org.odk.collect.android.dao.FormsDao;
import org.odk.collect.android.utilities.MultiFormDownloader;

import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.JR_FORM_ID;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.LAST_DETECTED_FORM_VERSION_HASH;

public class DatabaseNotificationRepository implements NotificationRepository {

    @Override
    public void markFormUpdateNotified(String formId, String formHash, String manifestHash) {
        String formVersionHash = MultiFormDownloader.getMd5Hash(formHash) + manifestHash;

        ContentValues values = new ContentValues();
        values.put(LAST_DETECTED_FORM_VERSION_HASH, formVersionHash);
        new FormsDao().updateForm(values, JR_FORM_ID + "=?", new String[]{formId});
    }

    @Override
    public boolean hasFormUpdateBeenNotified(String formHash, String manifestHash) {
        String formVersionHash = MultiFormDownloader.getMd5Hash(formHash) + manifestHash;

        Cursor cursor = new FormsDao().getFormsCursor(LAST_DETECTED_FORM_VERSION_HASH + "=?", new String[]{formVersionHash});
        return cursor == null || cursor.getCount() > 0;
    }
}
