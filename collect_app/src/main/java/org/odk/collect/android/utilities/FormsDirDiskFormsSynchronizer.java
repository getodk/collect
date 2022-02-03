package org.odk.collect.android.utilities;

import android.database.SQLException;

import org.odk.collect.analytics.Analytics;
import org.odk.collect.android.R;
import org.odk.collect.android.analytics.AnalyticsEvents;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.formmanagement.DiskFormsSynchronizer;
import org.odk.collect.android.injection.DaggerUtils;
import org.odk.collect.android.storage.StorageSubdirectory;
import org.odk.collect.forms.Form;
import org.odk.collect.forms.FormsRepository;
import org.odk.collect.shared.strings.Md5;
import org.odk.collect.shared.strings.Validator;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import timber.log.Timber;

import static org.odk.collect.strings.localization.LocalizedApplicationKt.getLocalizedString;

public class FormsDirDiskFormsSynchronizer implements DiskFormsSynchronizer {

    private static int counter;

    private final FormsRepository formsRepository;
    private final String formsDir;

    public FormsDirDiskFormsSynchronizer() {
        this(DaggerUtils.getComponent(Collect.getInstance()).formsRepositoryProvider().get(), DaggerUtils.getComponent(Collect.getInstance()).storagePathProvider().getOdkDirPath(StorageSubdirectory.FORMS));
    }

    public FormsDirDiskFormsSynchronizer(FormsRepository formsRepository, String formsDir) {
        this.formsRepository = formsRepository;
        this.formsDir = formsDir;
    }

    @Override
    public void synchronize() {
        synchronizeAndReturnError();
    }

    public String synchronizeAndReturnError() {
        String statusMessage = "";

        int instance = ++counter;
        Timber.i("[%d] doInBackground begins!", instance);

        List<Long> idsToDelete = new ArrayList<>();

        try {
            // Process everything then report what didn't work.
            StringBuilder errors = new StringBuilder();

            File formDir = new File(formsDir);
            if (formDir.exists() && formDir.isDirectory()) {
                // Get all the files in the /odk/foms directory
                File[] formDefs = formDir.listFiles();

                // Step 1: assemble the candidate form files
                List<File> formsToAdd = filterFormsToAdd(formDefs, instance);

                // Step 2: quickly run through and figure out what files we need to
                // parse and update; this is quick, as we only calculate the md5
                // and see if it has changed.
                List<IdFile> uriToUpdate = new ArrayList<>();
                List<Form> forms = formsRepository.getAll();
                for (Form form : forms) {
                    // For each element in the provider, see if the file already exists
                    String sqlFilename = form.getFormFilePath();
                    String md5 = form.getMD5Hash();

                    File sqlFile = new File(sqlFilename);
                    if (sqlFile.exists()) {
                        // remove it from the list of forms (we only want forms
                        // we haven't added at the end)
                        formsToAdd.remove(sqlFile);
                        String md5Computed = Md5.getMd5Hash(sqlFile);
                        if (md5Computed == null || md5 == null || !md5Computed.equals(md5)) {
                            // Probably someone overwrite the file on the sdcard
                            // So re-parse it and update it's information
                            Long id = form.getDbId();
                            uriToUpdate.add(new IdFile(id, sqlFile));
                        }
                    } else {
                        //File not found in sdcard but file path found in database
                        //probably because the file has been deleted or filename was changed in sdcard
                        //Add the ID to list so that they could be deleted all together

                        Long id = form.getDbId();
                        idsToDelete.add(id);
                    }
                }

                //Delete the forms not found in sdcard from the database
                for (Long id : idsToDelete) {
                    formsRepository.delete(id);
                }

                // Step3: go through uriToUpdate to parse and update each in turn.
                // Note: buildContentValues calls getMetadataFromFormDefinition which parses the
                // form XML. This takes time for large forms and/or slow devices.
                Collections.shuffle(uriToUpdate); // Big win if multiple DiskSyncTasks running
                for (IdFile entry : uriToUpdate) {
                    File formDefFile = entry.file;
                    // Probably someone overwrite the file on the sdcard
                    // So re-parse it and update it's information
                    Form form;

                    try {
                        form = parseForm(formDefFile);
                    } catch (IllegalArgumentException e) {
                        errors.append(e.getMessage()).append("\r\n");
                        File badFile = new File(formDefFile.getParentFile(),
                                formDefFile.getName() + ".bad");
                        badFile.delete();
                        formDefFile.renameTo(badFile);
                        continue;
                    }

                    formsRepository.save(new Form.Builder(form)
                            .dbId(entry.id)
                            .build());
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
                    if (formsRepository.getOneByPath(formDefFile.getAbsolutePath()) != null) {
                        Timber.i("[%d] skipping -- definition already recorded: %s", instance, formDefFile.getAbsolutePath());
                        continue;
                    }

                    // Parse it for the first time...
                    Form form;

                    try {
                        form = parseForm(formDefFile);
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
                        formsRepository.save(form);
                        Analytics.log(AnalyticsEvents.IMPORT_FORM);
                    } catch (SQLException e) {
                        Timber.i("[%d] %s", instance, e.toString());
                    }
                }
            }
            if (errors.length() != 0) {
                statusMessage = errors.toString();
            } else {
                Timber.d(getLocalizedString(Collect.getInstance(), R.string.finished_disk_scan));
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

    private Form parseForm(File formDefFile) throws IllegalArgumentException {
        // Probably someone overwrite the file on the sdcard
        // So re-parse it and update it's information
        Form.Builder builder = new Form.Builder();

        HashMap<String, String> fields;
        try {
            fields = FileUtils.getMetadataFromFormDefinition(formDefFile);
        } catch (RuntimeException e) {
            throw new IllegalArgumentException(formDefFile.getName() + " :: " + e.toString());
        }

        // update date
        Long now = System.currentTimeMillis();
        builder.date(now);

        String title = fields.get(FileUtils.TITLE);

        if (title != null) {
            builder.displayName(title);
        } else {
            throw new IllegalArgumentException(
                    getLocalizedString(Collect.getInstance(), R.string.xform_parse_error,
                            formDefFile.getName(), "title"));
        }
        String formid = fields.get(FileUtils.FORMID);
        if (formid != null) {
            builder.formId(formid);
        } else {
            throw new IllegalArgumentException(
                    getLocalizedString(Collect.getInstance(), R.string.xform_parse_error,
                            formDefFile.getName(), "id"));
        }
        String version = fields.get(FileUtils.VERSION);
        if (version != null) {
            builder.version(version);
        }
        String submission = fields.get(FileUtils.SUBMISSIONURI);
        if (submission != null) {
            if (Validator.isUrlValid(submission)) {
                builder.submissionUri(submission);
            } else {
                throw new IllegalArgumentException(
                        getLocalizedString(Collect.getInstance(), R.string.xform_parse_error,
                                formDefFile.getName(), "submission url"));
            }
        }
        String base64RsaPublicKey = fields.get(FileUtils.BASE64_RSA_PUBLIC_KEY);
        if (base64RsaPublicKey != null) {
            builder.base64RSAPublicKey(base64RsaPublicKey);
        }
        builder.autoDelete(fields.get(FileUtils.AUTO_DELETE));
        builder.autoSend(fields.get(FileUtils.AUTO_SEND));
        builder.geometryXpath(fields.get(FileUtils.GEOMETRY_XPATH));

        // Note, the path doesn't change here, but it needs to be included so the
        // update will automatically update the .md5 and the cache path.
        builder.formFilePath(formDefFile.getAbsolutePath());
        builder.formMediaPath(FileUtils.constructMediaPath(formDefFile.getAbsolutePath()));
        return builder.build();
    }

    private static class IdFile {
        public final Long id;
        public final File file;

        IdFile(Long id, File file) {
            this.id = id;
            this.file = file;
        }
    }
}
