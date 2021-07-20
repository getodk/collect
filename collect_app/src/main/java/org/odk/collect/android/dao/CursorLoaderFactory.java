package org.odk.collect.android.dao;

import android.net.Uri;

import androidx.loader.content.CursorLoader;

import org.odk.collect.android.application.Collect;
import org.odk.collect.android.database.forms.DatabaseFormColumns;
import org.odk.collect.android.database.instances.DatabaseInstanceColumns;
import org.odk.collect.android.projects.CurrentProjectProvider;
import org.odk.collect.android.external.FormsContract;
import org.odk.collect.android.external.InstancesContract;
import org.odk.collect.forms.instances.Instance;

public class CursorLoaderFactory {

    private final CurrentProjectProvider currentProjectProvider;

    public CursorLoaderFactory(CurrentProjectProvider currentProjectProvider) {
        this.currentProjectProvider = currentProjectProvider;
    }

    public CursorLoader createSentInstancesCursorLoader(String sortOrder) {
        String selection = DatabaseInstanceColumns.STATUS + " =? ";
        String[] selectionArgs = {Instance.STATUS_SUBMITTED};

        return getInstancesCursorLoader(selection, selectionArgs, sortOrder);
    }

    public CursorLoader createSentInstancesCursorLoader(CharSequence charSequence, String sortOrder) {
        CursorLoader cursorLoader;
        if (charSequence.length() == 0) {
            cursorLoader = createSentInstancesCursorLoader(sortOrder);
        } else {
            String selection =
                    DatabaseInstanceColumns.STATUS + " =? and "
                            + DatabaseInstanceColumns.DISPLAY_NAME + " LIKE ?";
            String[] selectionArgs = {
                    Instance.STATUS_SUBMITTED,
                    "%" + charSequence + "%"};

            cursorLoader = getInstancesCursorLoader(selection, selectionArgs, sortOrder);
        }

        return cursorLoader;
    }

    public CursorLoader createEditableInstancesCursorLoader(String sortOrder) {
        String selection = DatabaseInstanceColumns.STATUS + " !=? " +
                "and " + DatabaseInstanceColumns.STATUS + " !=? ";
        String[] selectionArgs = {
                Instance.STATUS_SUBMITTED,
                Instance.STATUS_SUBMISSION_FAILED
        };

        return getInstancesCursorLoader(selection, selectionArgs, sortOrder);
    }

    public CursorLoader createEditableInstancesCursorLoader(CharSequence charSequence, String sortOrder) {
        CursorLoader cursorLoader;
        if (charSequence.length() == 0) {
            cursorLoader = createEditableInstancesCursorLoader(sortOrder);
        } else {
            String selection = DatabaseInstanceColumns.STATUS + " !=? " +
                    "and " + DatabaseInstanceColumns.STATUS + " !=? " +
                    "and " + DatabaseInstanceColumns.DISPLAY_NAME + " LIKE ?";
            String[] selectionArgs = {
                    Instance.STATUS_SUBMITTED,
                    Instance.STATUS_SUBMISSION_FAILED,
                    "%" + charSequence + "%"};

            cursorLoader = getInstancesCursorLoader(selection, selectionArgs, sortOrder);
        }

        return cursorLoader;
    }

    public CursorLoader createSavedInstancesCursorLoader(String sortOrder) {
        String selection = DatabaseInstanceColumns.DELETED_DATE + " IS NULL ";

        return getInstancesCursorLoader(selection, null, sortOrder);
    }

    public CursorLoader createSavedInstancesCursorLoader(CharSequence charSequence, String sortOrder) {
        CursorLoader cursorLoader;
        if (charSequence.length() == 0) {
            cursorLoader = createSavedInstancesCursorLoader(sortOrder);
        } else {
            String selection =
                    DatabaseInstanceColumns.DELETED_DATE + " IS NULL and "
                            + DatabaseInstanceColumns.DISPLAY_NAME + " LIKE ?";
            String[] selectionArgs = {"%" + charSequence + "%"};
            cursorLoader = getInstancesCursorLoader(selection, selectionArgs, sortOrder);
        }

        return cursorLoader;
    }

    public CursorLoader createFinalizedInstancesCursorLoader(String sortOrder) {
        String selection = DatabaseInstanceColumns.STATUS + "=? or " + DatabaseInstanceColumns.STATUS + "=?";
        String[] selectionArgs = {Instance.STATUS_COMPLETE, Instance.STATUS_SUBMISSION_FAILED};

        return getInstancesCursorLoader(selection, selectionArgs, sortOrder);
    }

    public CursorLoader createFinalizedInstancesCursorLoader(CharSequence charSequence, String sortOrder) {
        CursorLoader cursorLoader;
        if (charSequence.length() == 0) {
            cursorLoader = createFinalizedInstancesCursorLoader(sortOrder);
        } else {
            String selection =
                    "(" + DatabaseInstanceColumns.STATUS + "=? or "
                            + DatabaseInstanceColumns.STATUS + "=?) and "
                            + DatabaseInstanceColumns.DISPLAY_NAME + " LIKE ?";
            String[] selectionArgs = {
                    Instance.STATUS_COMPLETE,
                    Instance.STATUS_SUBMISSION_FAILED,
                    "%" + charSequence + "%"};

            cursorLoader = getInstancesCursorLoader(selection, selectionArgs, sortOrder);
        }

        return cursorLoader;
    }

    public CursorLoader createCompletedUndeletedInstancesCursorLoader(String sortOrder) {
        String selection = DatabaseInstanceColumns.DELETED_DATE + " IS NULL and ("
                + DatabaseInstanceColumns.STATUS + "=? or "
                + DatabaseInstanceColumns.STATUS + "=? or "
                + DatabaseInstanceColumns.STATUS + "=?)";

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
            String selection = DatabaseInstanceColumns.DELETED_DATE + " IS NULL and ("
                    + DatabaseInstanceColumns.STATUS + "=? or "
                    + DatabaseInstanceColumns.STATUS + "=? or "
                    + DatabaseInstanceColumns.STATUS + "=?) and "
                    + DatabaseInstanceColumns.DISPLAY_NAME + " LIKE ?";

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
            Uri formUri = newestByFormId ?
                    FormsContract.getContentNewestFormsByFormIdUri(currentProjectProvider.getCurrentProject().getUuid()) :
                    FormsContract.getUri(currentProjectProvider.getCurrentProject().getUuid());
            cursorLoader = new CursorLoader(Collect.getInstance(), formUri, null, DatabaseFormColumns.DELETED_DATE + " IS NULL", new String[]{}, sortOrder);
        } else {
            String selection = DatabaseFormColumns.DISPLAY_NAME + " LIKE ? AND " + DatabaseFormColumns.DELETED_DATE + " IS NULL";
            String[] selectionArgs = {"%" + charSequence + "%"};

            Uri formUri = newestByFormId ?
                    FormsContract.getContentNewestFormsByFormIdUri(currentProjectProvider.getCurrentProject().getUuid()) :
                    FormsContract.getUri(currentProjectProvider.getCurrentProject().getUuid());
            cursorLoader = new CursorLoader(Collect.getInstance(), formUri, null, selection, selectionArgs, sortOrder);
        }
        return cursorLoader;
    }

    private CursorLoader getInstancesCursorLoader(String selection, String[] selectionArgs, String sortOrder) {
        return new CursorLoader(
                Collect.getInstance(),
                InstancesContract.getUri(currentProjectProvider.getCurrentProject().getUuid()),
                null,
                selection,
                selectionArgs,
                sortOrder);
    }
}
