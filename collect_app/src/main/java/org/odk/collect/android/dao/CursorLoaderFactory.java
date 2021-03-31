package org.odk.collect.android.dao;

import android.net.Uri;

import androidx.loader.content.CursorLoader;

import org.odk.collect.android.application.Collect;
import org.odk.collect.android.database.DatabaseFormColumns;
import org.odk.collect.android.instances.Instance;
import org.odk.collect.android.provider.FormsProviderAPI;
import org.odk.collect.android.provider.InstanceProviderAPI;

public class CursorLoaderFactory {

    public CursorLoader createSentInstancesCursorLoader(CharSequence charSequence, String sortOrder) {
        CursorLoader cursorLoader;
        if (charSequence.length() == 0) {
            cursorLoader = createSentInstancesCursorLoader(sortOrder);
        } else {
            String selection =
                    InstanceProviderAPI.InstanceColumns.STATUS + " =? and "
                            + InstanceProviderAPI.InstanceColumns.DISPLAY_NAME + " LIKE ?";
            String[] selectionArgs = {
                    Instance.STATUS_SUBMITTED,
                    "%" + charSequence + "%"};

            cursorLoader = getInstancesCursorLoader(selection, selectionArgs, sortOrder);
        }

        return cursorLoader;
    }

    public CursorLoader createSentInstancesCursorLoader(String sortOrder) {
        String selection = InstanceProviderAPI.InstanceColumns.STATUS + " =? ";
        String[] selectionArgs = {Instance.STATUS_SUBMITTED};

        return getInstancesCursorLoader(selection, selectionArgs, sortOrder);
    }

    public CursorLoader createUnsentInstancesCursorLoader(String sortOrder) {
        String selection = InstanceProviderAPI.InstanceColumns.STATUS + " !=? ";
        String[] selectionArgs = {Instance.STATUS_SUBMITTED};

        return getInstancesCursorLoader(selection, selectionArgs, sortOrder);
    }

    public CursorLoader createUnsentInstancesCursorLoader(CharSequence charSequence, String sortOrder) {
        CursorLoader cursorLoader;
        if (charSequence.length() == 0) {
            cursorLoader = createUnsentInstancesCursorLoader(sortOrder);
        } else {
            String selection =
                    InstanceProviderAPI.InstanceColumns.STATUS + " !=? and "
                            + InstanceProviderAPI.InstanceColumns.DISPLAY_NAME + " LIKE ?";
            String[] selectionArgs = {
                    Instance.STATUS_SUBMITTED,
                    "%" + charSequence + "%"};

            cursorLoader = getInstancesCursorLoader(selection, selectionArgs, sortOrder);
        }

        return cursorLoader;
    }

    public CursorLoader createSavedInstancesCursorLoader(String sortOrder) {
        String selection = InstanceProviderAPI.InstanceColumns.DELETED_DATE + " IS NULL ";

        return getInstancesCursorLoader(selection, null, sortOrder);
    }

    public CursorLoader createSavedInstancesCursorLoader(CharSequence charSequence, String sortOrder) {
        CursorLoader cursorLoader;
        if (charSequence.length() == 0) {
            cursorLoader = createSavedInstancesCursorLoader(sortOrder);
        } else {
            String selection =
                    InstanceProviderAPI.InstanceColumns.DELETED_DATE + " IS NULL and "
                            + InstanceProviderAPI.InstanceColumns.DISPLAY_NAME + " LIKE ?";
            String[] selectionArgs = {"%" + charSequence + "%"};
            cursorLoader = getInstancesCursorLoader(selection, selectionArgs, sortOrder);
        }

        return cursorLoader;
    }

    public CursorLoader createFinalizedInstancesCursorLoader(String sortOrder) {
        String selection = InstanceProviderAPI.InstanceColumns.STATUS + "=? or " + InstanceProviderAPI.InstanceColumns.STATUS + "=?";
        String[] selectionArgs = {Instance.STATUS_COMPLETE, Instance.STATUS_SUBMISSION_FAILED};

        return getInstancesCursorLoader(selection, selectionArgs, sortOrder);
    }

    public CursorLoader createFinalizedInstancesCursorLoader(CharSequence charSequence, String sortOrder) {
        CursorLoader cursorLoader;
        if (charSequence.length() == 0) {
            cursorLoader = createFinalizedInstancesCursorLoader(sortOrder);
        } else {
            String selection =
                    "(" + InstanceProviderAPI.InstanceColumns.STATUS + "=? or "
                            + InstanceProviderAPI.InstanceColumns.STATUS + "=?) and "
                            + InstanceProviderAPI.InstanceColumns.DISPLAY_NAME + " LIKE ?";
            String[] selectionArgs = {
                    Instance.STATUS_COMPLETE,
                    Instance.STATUS_SUBMISSION_FAILED,
                    "%" + charSequence + "%"};

            cursorLoader = getInstancesCursorLoader(selection, selectionArgs, sortOrder);
        }

        return cursorLoader;
    }

    public CursorLoader createCompletedUndeletedInstancesCursorLoader(String sortOrder) {
        String selection = InstanceProviderAPI.InstanceColumns.DELETED_DATE + " IS NULL and ("
                + InstanceProviderAPI.InstanceColumns.STATUS + "=? or "
                + InstanceProviderAPI.InstanceColumns.STATUS + "=? or "
                + InstanceProviderAPI.InstanceColumns.STATUS + "=?)";

        String[] selectionArgs = {Instance.STATUS_COMPLETE,
                Instance.STATUS_SUBMISSION_FAILED,
                Instance.STATUS_SUBMITTED};

        return getInstancesCursorLoader(selection, selectionArgs, sortOrder);
    }

    public CursorLoader createCompletedUndeletedInstancesCursorLoader(CharSequence charSequence, String sortOrder) {
        CursorLoader cursorLoader;
        if (charSequence.length() == 0) {
            cursorLoader = createCompletedUndeletedInstancesCursorLoader(sortOrder);
        } else {
            String selection = InstanceProviderAPI.InstanceColumns.DELETED_DATE + " IS NULL and ("
                    + InstanceProviderAPI.InstanceColumns.STATUS + "=? or "
                    + InstanceProviderAPI.InstanceColumns.STATUS + "=? or "
                    + InstanceProviderAPI.InstanceColumns.STATUS + "=?) and "
                    + InstanceProviderAPI.InstanceColumns.DISPLAY_NAME + " LIKE ?";

            String[] selectionArgs = {
                    Instance.STATUS_COMPLETE,
                    Instance.STATUS_SUBMISSION_FAILED,
                    Instance.STATUS_SUBMITTED,
                    "%" + charSequence + "%"};

            cursorLoader = getInstancesCursorLoader(selection, selectionArgs, sortOrder);
        }
        return cursorLoader;
    }

    /**
     * Returns a loader filtered by the specified charSequence in the specified sortOrder. If
     * newestByFormId is true, only the most recently-downloaded version of each form is included.
     */
    public CursorLoader getFormsCursorLoader(CharSequence charSequence, String sortOrder, boolean newestByFormId) {
        CursorLoader cursorLoader;

        if (charSequence.length() == 0) {
            Uri formUri = newestByFormId ? FormsProviderAPI.CONTENT_NEWEST_FORMS_BY_FORMID_URI
                    : FormsProviderAPI.CONTENT_URI;

            cursorLoader = new CursorLoader(Collect.getInstance(), formUri, null, DatabaseFormColumns.DELETED_DATE + " IS NULL", new String[]{}, sortOrder);
        } else {
            String selection = DatabaseFormColumns.DISPLAY_NAME + " LIKE ? AND " + DatabaseFormColumns.DELETED_DATE + " IS NULL";
            String[] selectionArgs = {"%" + charSequence + "%"};

            Uri formUri = newestByFormId ? FormsProviderAPI.CONTENT_NEWEST_FORMS_BY_FORMID_URI
                    : FormsProviderAPI.CONTENT_URI;

            cursorLoader = new CursorLoader(Collect.getInstance(), formUri, null, selection, selectionArgs, sortOrder);
        }
        return cursorLoader;
    }

    private CursorLoader getInstancesCursorLoader(String selection, String[] selectionArgs, String sortOrder) {
        return new CursorLoader(
                Collect.getInstance(),
                InstanceProviderAPI.InstanceColumns.CONTENT_URI,
                null,
                selection,
                selectionArgs,
                sortOrder);
    }
}
