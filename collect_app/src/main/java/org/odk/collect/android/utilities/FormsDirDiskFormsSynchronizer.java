package org.odk.collect.android.utilities;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;

import org.javarosa.core.reference.ReferenceManager;
import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.dao.FormsDao;
import org.odk.collect.android.formmanagement.DiskFormsSynchronizer;
import org.odk.collect.android.provider.FormsProviderAPI;
import org.odk.collect.android.storage.StoragePathProvider;
import org.odk.collect.android.storage.StorageSubdirectory;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import timber.log.Timber;

import static org.odk.collect.android.forms.FormUtils.setupReferenceManagerForForm;
import static org.odk.collect.utilities.PathUtils.getAbsoluteFilePath;

public class FormsDirDiskFormsSynchronizer implements DiskFormsSynchronizer {

    private static int counter;

    @Override
    public void synchronize() {
        synchronizeAndReturnError();
    }

    public String synchronizeAndReturnError() {
        FormsDao formsDao = new FormsDao();
        String statusMessage = "";

        int instance = ++counter;
        Timber.i("[%d] doInBackground begins!", instance);

        List<String> idsToDelete = new ArrayList<>();

        try {
            // Process everything then report what didn't work.
            StringBuilder errors = new StringBuilder();

            StoragePathProvider storagePathProvider = new StoragePathProvider();
            File formDir = new File(storagePathProvider.getDirPath(StorageSubdirectory.FORMS));
            if (formDir.exists() && formDir.isDirectory()) {
                // Get all the files in the /odk/foms directory
                File[] formDefs = formDir.listFiles();

                // Step 1: assemble the candidate form files
                List<File> formsToAdd = filterFormsToAdd(formDefs, instance);

                // Step 2: quickly run through and figure out what files we need to
                // parse and update; this is quick, as we only calculate the md5
                // and see if it has changed.
                List<UriFile> uriToUpdate = new ArrayList<>();
                Cursor cursor = null;
                // open the cursor within a try-catch block so it can always be closed.
                try {
                    cursor = formsDao.getFormsCursor();
                    if (cursor == null) {
                        Timber.e("[%d] Forms Content Provider returned NULL", instance);
                        errors.append("Internal Error: Unable to access Forms content provider\r\n");
                        return errors.toString();
                    }

                    cursor.moveToPosition(-1);

                    while (cursor.moveToNext()) {
                        // For each element in the provider, see if the file already exists
                        String sqlFilename =
                                getAbsoluteFilePath(storagePathProvider.getDirPath(StorageSubdirectory.FORMS), cursor.getString(
                                        cursor.getColumnIndex(FormsProviderAPI.FormsColumns.FORM_FILE_PATH)));
                        String md5 = cursor.getString(
                                cursor.getColumnIndex(FormsProviderAPI.FormsColumns.MD5_HASH));
                        File sqlFile = new File(sqlFilename);
                        if (sqlFile.exists()) {
                            // remove it from the list of forms (we only want forms
                            // we haven't added at the end)
                            formsToAdd.remove(sqlFile);
                            String md5Computed = FileUtils.getMd5Hash(sqlFile);
                            if (md5Computed == null || md5 == null || !md5Computed.equals(md5)) {
                                // Probably someone overwrite the file on the sdcard
                                // So re-parse it and update it's information
                                String id = cursor.getString(
                                        cursor.getColumnIndex(FormsProviderAPI.FormsColumns._ID));
                                Uri updateUri = Uri.withAppendedPath(FormsProviderAPI.FormsColumns.CONTENT_URI, id);
                                uriToUpdate.add(new UriFile(updateUri, sqlFile));
                            }
                        } else {
                            //File not found in sdcard but file path found in database
                            //probably because the file has been deleted or filename was changed in sdcard
                            //Add the ID to list so that they could be deleted all together

                            String id = cursor.getString(
                                    cursor.getColumnIndex(FormsProviderAPI.FormsColumns._ID));

                            idsToDelete.add(id);
                        }
                    }
                } finally {
                    if (cursor != null) {
                        cursor.close();
                    }
                }

                if (!idsToDelete.isEmpty()) {
                    //Delete the forms not found in sdcard from the database
                    formsDao.deleteFormsFromIDs(idsToDelete.toArray(new String[idsToDelete.size()]));
                }

                // Step3: go through uriToUpdate to parse and update each in turn.
                // Note: buildContentValues calls getMetadataFromFormDefinition which parses the
                // form XML. This takes time for large forms and/or slow devices.
                Collections.shuffle(uriToUpdate); // Big win if multiple DiskSyncTasks running
                for (UriFile entry : uriToUpdate) {
                    Uri updateUri = entry.uri;
                    File formDefFile = entry.file;
                    // Probably someone overwrite the file on the sdcard
                    // So re-parse it and update it's information
                    ContentValues values;

                    try {
                        values = buildContentValues(formDefFile);
                    } catch (IllegalArgumentException e) {
                        errors.append(e.getMessage()).append("\r\n");
                        File badFile = new File(formDefFile.getParentFile(),
                                formDefFile.getName() + ".bad");
                        badFile.delete();
                        formDefFile.renameTo(badFile);
                        continue;
                    }

                    // update in content provider
                    int count =
                            Collect.getInstance().getContentResolver()
                                    .update(updateUri, values, null, null);
                    Timber.i("[%d] %d records successfully updated", instance, count);
                }
                uriToUpdate.clear();

                // Step 4: go through the newly-discovered files in xFormsToAdd and add them.
                // This is slow because buildContentValues(...) is slow.
                //
                Collections.shuffle(formsToAdd); // Big win if multiple DiskSyncTasks running
                while (!formsToAdd.isEmpty()) {
                    File formDefFile = formsToAdd.remove(0);

                    // Since parsing is so slow, if there are multiple tasks,
                    // they may have already updated the database.
                    // Skip this file if that is the case.
                    if (isAlreadyDefined(formsDao, formDefFile)) {
                        Timber.i("[%d] skipping -- definition already recorded: %s",
                                instance, formDefFile.getAbsolutePath());
                        continue;
                    }

                    // Parse it for the first time...
                    ContentValues values;

                    try {
                        values = buildContentValues(formDefFile);
                    } catch (IllegalArgumentException e) {
                        errors.append(e.getMessage()).append("\r\n");
                        File badFile = new File(formDefFile.getParentFile(),
                                formDefFile.getName() + ".bad");
                        badFile.delete();
                        formDefFile.renameTo(badFile);
                        continue;
                    }

                    // insert into content provider
                    try {
                        // insert failures are OK and expected if multiple
                        // DiskSync scanners are active.
                        formsDao.saveForm(values);
                    } catch (SQLException e) {
                        Timber.i("[%d] %s", instance, e.toString());
                    }
                }
            }
            if (errors.length() != 0) {
                statusMessage = errors.toString();
            } else {
                Timber.d(TranslationHandler.getString(Collect.getInstance(), R.string.finished_disk_scan));
            }
            return statusMessage;
        } finally {
            Timber.i("[%d] doInBackground ends!", instance);
        }
    }

    public static List<File> filterFormsToAdd(File[] formDefs, int backgroundInstanceId) {
        List<File> formsToAdd = new LinkedList<>();
        if (formDefs != null) {
            for (File candidate : formDefs) {
                if (shouldAddFormFile(candidate.getName())) {
                    formsToAdd.add(candidate);
                } else {
                    Timber.i("[%d] Ignoring: %s", backgroundInstanceId, candidate.getAbsolutePath());
                }
            }
        }
        return formsToAdd;
    }

    public static boolean shouldAddFormFile(String fileName) {
        // discard files beginning with "."
        // discard files not ending with ".xml" or ".xhtml"
        boolean ignoredFile = fileName.startsWith(".");
        boolean xmlFile = fileName.endsWith(".xml");
        boolean xhtmlFile = fileName.endsWith(".xhtml");
        return !ignoredFile && (xmlFile || xhtmlFile);
    }

    private boolean isAlreadyDefined(FormsDao formsDao, File formDefFile) {
        // first try to see if a record with this filename already exists...
        Cursor c = null;
        try {
            c = formsDao.getFormsCursorForFormFilePath(formDefFile.getAbsolutePath());
            return c == null || c.getCount() > 0;
        } finally {
            if (c != null) {
                c.close();
            }
        }
    }

    /**
     * Parses the given form definition file to get basic form identifiers as a ContentValues object.
     *
     * Note: takes time for complex forms and/or slow devices.
     *
     * @return key-value list to update or insert into the content provider
     * @throws IllegalArgumentException if the file failed to parse, is missing title or form_id
     * fields or includes an invalid submission URL.
     */
    private ContentValues buildContentValues(File formDefFile) throws IllegalArgumentException {
        // Probably someone overwrite the file on the sdcard
        // So re-parse it and update it's information
        ContentValues updateValues = new ContentValues();

        HashMap<String, String> fields;
        try {
            // If the form definition includes external secondary instances, they need to be resolved
            final File formMediaDir = FileUtils.getFormMediaDir(formDefFile);
            setupReferenceManagerForForm(ReferenceManager.instance(), formMediaDir);

            FileUtils.getOrCreateLastSavedSrc(formDefFile);
            fields = FileUtils.getMetadataFromFormDefinition(formDefFile);
        } catch (RuntimeException e) {
            throw new IllegalArgumentException(formDefFile.getName() + " :: " + e.toString());
        }

        // update date
        Long now = System.currentTimeMillis();
        updateValues.put(FormsProviderAPI.FormsColumns.DATE, now);

        String title = fields.get(FileUtils.TITLE);

        if (title != null) {
            updateValues.put(FormsProviderAPI.FormsColumns.DISPLAY_NAME, title);
        } else {
            throw new IllegalArgumentException(
                    TranslationHandler.getString(Collect.getInstance(), R.string.xform_parse_error,
                            formDefFile.getName(), "title"));
        }
        String formid = fields.get(FileUtils.FORMID);
        if (formid != null) {
            updateValues.put(FormsProviderAPI.FormsColumns.JR_FORM_ID, formid);
        } else {
            throw new IllegalArgumentException(
                    TranslationHandler.getString(Collect.getInstance(), R.string.xform_parse_error,
                            formDefFile.getName(), "id"));
        }
        String version = fields.get(FileUtils.VERSION);
        if (version != null) {
            updateValues.put(FormsProviderAPI.FormsColumns.JR_VERSION, version);
        }
        String submission = fields.get(FileUtils.SUBMISSIONURI);
        if (submission != null) {
            if (Validator.isUrlValid(submission)) {
                updateValues.put(FormsProviderAPI.FormsColumns.SUBMISSION_URI, submission);
            } else {
                throw new IllegalArgumentException(
                        TranslationHandler.getString(Collect.getInstance(), R.string.xform_parse_error,
                                formDefFile.getName(), "submission url"));
            }
        }
        String base64RsaPublicKey = fields.get(FileUtils.BASE64_RSA_PUBLIC_KEY);
        if (base64RsaPublicKey != null) {
            updateValues.put(FormsProviderAPI.FormsColumns.BASE64_RSA_PUBLIC_KEY, base64RsaPublicKey);
        }
        updateValues.put(FormsProviderAPI.FormsColumns.AUTO_DELETE, fields.get(FileUtils.AUTO_DELETE));
        updateValues.put(FormsProviderAPI.FormsColumns.AUTO_SEND, fields.get(FileUtils.AUTO_SEND));
        updateValues.put(FormsProviderAPI.FormsColumns.GEOMETRY_XPATH, fields.get(FileUtils.GEOMETRY_XPATH));

        // Note, the path doesn't change here, but it needs to be included so the
        // update will automatically update the .md5 and the cache path.
        updateValues.put(FormsProviderAPI.FormsColumns.FORM_FILE_PATH, new StoragePathProvider().getFormDbPath(formDefFile.getAbsolutePath()));
        updateValues.putNull(FormsProviderAPI.FormsColumns.DELETED_DATE);

        return updateValues;
    }

    private static class UriFile {
        public final Uri uri;
        public final File file;

        UriFile(Uri uri, File file) {
            this.uri = uri;
            this.file = file;
        }
    }
}
