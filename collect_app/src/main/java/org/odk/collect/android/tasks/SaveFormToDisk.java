/*
 * Copyright (C) 2009 University of Washington
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.odk.collect.android.tasks;

import static org.odk.collect.android.analytics.AnalyticsEvents.ENCRYPT_SUBMISSION;
import static org.odk.collect.strings.localization.LocalizedApplicationKt.getLocalizedString;

import android.content.ContentValues;
import android.net.Uri;
import android.util.Pair;

import androidx.annotation.NonNull;

import org.javarosa.core.model.condition.EvaluationContext;
import org.javarosa.core.model.data.GeoPointData;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.services.transport.payload.ByteArrayPayload;
import org.javarosa.xpath.XPathException;
import org.javarosa.xpath.XPathNodeset;
import org.javarosa.xpath.XPathParseTool;
import org.javarosa.xpath.expr.XPathExpression;
import org.javarosa.xpath.parser.XPathSyntaxException;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.odk.collect.analytics.Analytics;
import org.odk.collect.android.analytics.AnalyticsEvents;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.database.instances.DatabaseInstanceColumns;
import org.odk.collect.android.exception.EncryptionException;
import org.odk.collect.android.external.InstancesContract;
import org.odk.collect.android.formentry.FormEntryUseCases;
import org.odk.collect.android.formentry.saving.FormSaver;
import org.odk.collect.android.javarosawrapper.FailedValidationResult;
import org.odk.collect.android.javarosawrapper.FormController;
import org.odk.collect.android.javarosawrapper.ValidationResult;
import org.odk.collect.android.storage.StoragePathProvider;
import org.odk.collect.android.storage.StorageSubdirectory;
import org.odk.collect.android.utilities.ContentUriHelper;
import org.odk.collect.android.utilities.EncryptionUtils;
import org.odk.collect.android.utilities.EncryptionUtils.EncryptedFormInformation;
import org.odk.collect.android.utilities.FileUtils;
import org.odk.collect.android.utilities.FormsRepositoryProvider;
import org.odk.collect.android.utilities.MediaUtils;
import org.odk.collect.entities.EntitiesRepository;
import org.odk.collect.forms.Form;
import org.odk.collect.forms.instances.Instance;
import org.odk.collect.forms.instances.InstancesRepository;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import timber.log.Timber;

/**
 * Background task for loading a form.
 *
 * @author Carl Hartung (carlhartung@gmail.com)
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */
public class SaveFormToDisk {

    private final boolean saveAndExit;
    private final boolean shouldFinalize;
    private final FormController formController;
    private final MediaUtils mediaUtils;
    private final InstancesRepository instancesRepository;
    private Uri uri;
    private String instanceName;
    private final ArrayList<String> tempFiles;
    private final String currentProjectId;
    private final EntitiesRepository entitiesRepository;

    public static final int SAVED = 500;
    public static final int SAVE_ERROR = 501;
    public static final int SAVED_AND_EXIT = 504;
    public static final int ENCRYPTION_ERROR = 505;

    public SaveFormToDisk(FormController formController, MediaUtils mediaUtils, boolean saveAndExit, boolean shouldFinalize, String updatedName,
                          Uri uri, ArrayList<String> tempFiles, String currentProjectId, EntitiesRepository entitiesRepository,  InstancesRepository instancesRepository) {
        this.formController = formController;
        this.mediaUtils = mediaUtils;
        this.uri = uri;
        this.saveAndExit = saveAndExit;
        this.shouldFinalize = shouldFinalize;
        this.instanceName = updatedName;
        this.tempFiles = tempFiles;
        this.currentProjectId = currentProjectId;
        this.entitiesRepository = entitiesRepository;
        this.instancesRepository = instancesRepository;
    }

    @Nullable
    public SaveToDiskResult saveForm(FormSaver.ProgressListener progressListener) {
        SaveToDiskResult saveToDiskResult = new SaveToDiskResult();

        progressListener.onProgressUpdate(getLocalizedString(Collect.getInstance(), org.odk.collect.strings.R.string.survey_saving_validating_message));

        ValidationResult validationResult;
        try {
            validationResult = formController.validateAnswers(shouldFinalize);
            if (shouldFinalize && validationResult instanceof FailedValidationResult) {
                // validation failed, pass specific failure
                saveToDiskResult.setSaveResult(((FailedValidationResult) validationResult).getStatus(), shouldFinalize);
                return saveToDiskResult;
            }
        } catch (Exception e) {
            Timber.e(e);
            saveToDiskResult.setSaveErrorMessage(e.getMessage());
            saveToDiskResult.setSaveResult(SAVE_ERROR, shouldFinalize);
            return saveToDiskResult;
        }

        if (shouldFinalize) {
            FormEntryUseCases.finalizeInstance(formController, entitiesRepository);
        }

        // close all open databases of external data.
        Collect.getInstance().getExternalDataManager().close();

        // if there is a meta/instanceName field, be sure we are using the latest value
        // just in case the validate somehow triggered an update.
        String updatedSaveName = formController.getSubmissionMetadata().instanceName;
        if (updatedSaveName != null) {
            instanceName = updatedSaveName;
        }

        try {
            Instance instance = exportData(shouldFinalize, progressListener, validationResult);

            if (formController.getInstanceFile() != null) {
                removeIndexFile(formController.getInstanceFile().getName());
            }

            saveToDiskResult.setSaveResult(saveAndExit ? SAVED_AND_EXIT : SAVED, shouldFinalize);
            saveToDiskResult.setInstance(instance);
        } catch (EncryptionException e) {
            saveToDiskResult.setSaveErrorMessage(e.getMessage());
            saveToDiskResult.setSaveResult(ENCRYPTION_ERROR, shouldFinalize);
        } catch (Exception e) {
            Timber.e(e);

            saveToDiskResult.setSaveErrorMessage(e.getMessage());
            saveToDiskResult.setSaveResult(SAVE_ERROR, shouldFinalize);
        }

        return saveToDiskResult;
    }

    /**
     * Updates the status and editability for the database row corresponding to the instance that is
     * currently managed by the {@link FormController}. There are three cases:
     * - the instance was opened for edit so its database row already exists
     * - a new instance was just created so its database row doesn't exist and needs to be created
     * - a new instance was created at the start of this editing session but the user has already
     * saved it so its database row already exists
     * <p>
     * Post-condition: the uri field is set to the URI of the instance database row that matches
     * the instance currently managed by the {@link FormController}.
     */
    private Instance updateInstanceDatabase(boolean incomplete, boolean canEditAfterCompleted, ValidationResult validationResult) {
        FormInstance formInstance = formController.getFormDef().getInstance();

        String instancePath = formController.getInstanceFile().getAbsolutePath();
        InstancesRepository instances = instancesRepository;
        Instance instance = instances.getOneByPath(instancePath);

        Instance.Builder instanceBuilder;
        if (instance != null) {
            instanceBuilder = new Instance.Builder(instance);
        } else {
            instanceBuilder = new Instance.Builder();
        }

        if (instanceName != null) {
            instanceBuilder.displayName(instanceName);
        }

        if (!shouldFinalize) {
            if (validationResult instanceof FailedValidationResult) {
                instanceBuilder.status(Instance.STATUS_INVALID);
            } else {
                instanceBuilder.status(Instance.STATUS_VALID);
            }
        } else if (incomplete) {
            instanceBuilder.status(Instance.STATUS_INCOMPLETE);
        } else {
            instanceBuilder.status(Instance.STATUS_COMPLETE);
        }

        instanceBuilder.canEditWhenComplete(canEditAfterCompleted);

        if (instance != null) {
            String geometryXpath = getGeometryXpathForInstance(instance);
            Pair<String, String> geometryContentValues = extractGeometryContentValues(formInstance, geometryXpath);
            if (geometryContentValues != null) {
                instanceBuilder.geometryType(geometryContentValues.first);
                instanceBuilder.geometry(geometryContentValues.second);
            }

            Instance newInstance = instancesRepository.save(instanceBuilder.build());
            uri = InstancesContract.getUri(currentProjectId, newInstance.getDbId());
        } else {
            Timber.i("No instance found, creating");
            Form form = new FormsRepositoryProvider(Collect.getInstance()).get().get(ContentUriHelper.getIdFromUri(uri));

            // add missing fields into values
            instanceBuilder.instanceFilePath(instancePath);
            instanceBuilder.submissionUri(form.getSubmissionUri());
            if (instanceName != null) {
                instanceBuilder.displayName(instanceName);
            } else {
                instanceBuilder.displayName(form.getDisplayName());
            }

            instanceBuilder.formId(form.getFormId());
            instanceBuilder.formVersion(form.getVersion());

            Pair<String, String> geometryContentValues = extractGeometryContentValues(formInstance, form.getGeometryXpath());
            if (geometryContentValues != null) {
                instanceBuilder.geometryType(geometryContentValues.first);
                instanceBuilder.geometry(geometryContentValues.second);
            }
        }

        Instance newInstance = instancesRepository.save(instanceBuilder.build());
        uri = InstancesContract.getUri(currentProjectId, newInstance.getDbId());
        return newInstance;
    }

    /**
     * Extracts geometry information from the given xpath path in the given instance.
     * <p>
     * Returns a ContentValues object with values set for InstanceColumns.GEOMETRY and
     * InstanceColumns.GEOMETRY_TYPE. Those value are null if anything goes wrong with
     * parsing the geometry and converting it to GeoJSON.
     * <p>
     * Returns null if the given XPath path is null.
     */
    private Pair<String, String> extractGeometryContentValues(FormInstance instance, String xpath) {
        if (xpath == null) {
            return null;
        }

        try {
            XPathExpression expr = XPathParseTool.parseXPath(xpath);
            EvaluationContext context = new EvaluationContext(instance);
            Object result = expr.eval(instance, context);
            if (result instanceof XPathNodeset) {
                XPathNodeset nodes = (XPathNodeset) result;
                if (nodes.size() == 0) {
                    Timber.i("TreeElement is missing for xpath %s!, probably it's just not relevant", xpath);
                    return null;
                }

                // For now, only use the first node found.
                TreeElement element = instance.resolveReference(nodes.getRefAt(0));
                IAnswerData value = element.getValue();

                if (value instanceof GeoPointData) {
                    try {
                        JSONObject json = toGeoJson((GeoPointData) value);
                        Timber.i("Geometry for \"%s\" instance found at %s: %s",
                                instance.getName(), xpath, json);

                        return new Pair<>(json.getString("type"), json.toString());
                    } catch (JSONException e) {
                        Timber.w("Could not convert GeoPointData %s to GeoJSON", value);
                    }
                }
            }
        } catch (XPathException | XPathSyntaxException e) {
            Timber.w(e, "Could not evaluate geometry XPath %s in instance", xpath);
        }

        return new Pair<>(null, null);
    }

    @NonNull
    private JSONObject toGeoJson(GeoPointData data) throws JSONException {
        // For a GeoPointData object, the four fields exposed by getPart() are
        // latitude, longitude, altitude, and accuracy radius, in that order.
        double lat = data.getPart(0);
        double lon = data.getPart(1);

        // In GeoJSON, longitude comes before latitude.
        JSONArray coordinates = new JSONArray();
        coordinates.put(lon);
        coordinates.put(lat);

        JSONObject geometry = new JSONObject();
        geometry.put("type", "Point");
        geometry.put("coordinates", coordinates);
        return geometry;
    }

    /**
     * Return the formIndex file for a given instance.
     */
    public static File getFormIndexFile(String instanceName) {
        File tempDir = new File(new StoragePathProvider().getOdkDirPath(StorageSubdirectory.CACHE));
        return new File(tempDir, instanceName + ".index");
    }

    public static void removeIndexFile(String instanceName) {
        File formIndexFile = getFormIndexFile(instanceName);
        FileUtils.deleteAndReport(formIndexFile);
    }

    /**
     * Write's the data to the sdcard, and updates the instances content provider.
     * In theory we don't have to write to disk, and this is where you'd add
     * other methods.
     */
    private Instance exportData(boolean markCompleted, FormSaver.ProgressListener progressListener, ValidationResult validationResult) throws IOException, EncryptionException {
        progressListener.onProgressUpdate(getLocalizedString(Collect.getInstance(), org.odk.collect.strings.R.string.survey_saving_collecting_message));

        ByteArrayPayload payload = formController.getFilledInFormXml();
        // write out xml
        String instancePath = formController.getInstanceFile().getAbsolutePath();

        for (String fileName : tempFiles) {
            mediaUtils.deleteMediaFile(fileName);
        }

        progressListener.onProgressUpdate(getLocalizedString(Collect.getInstance(), org.odk.collect.strings.R.string.survey_saving_saving_message));

        writeFile(payload, instancePath);

        // Write last-saved instance
        String lastSavedPath = formController.getLastSavedPath();
        writeFile(payload, lastSavedPath);

        // update the uri. We have exported the reloadable instance, so update status...
        // Since we saved a reloadable instance, it is flagged as re-openable so that if any error
        // occurs during the packaging of the data for the server fails (e.g., encryption),
        // we can still reopen the filled-out form and re-save it at a later time.
        Instance instance = updateInstanceDatabase(true, true, validationResult);

        if (markCompleted) {
            // now see if the packaging of the data for the server would make it
            // non-reopenable (e.g., encryption or other fraction of the form).
            boolean canEditAfterCompleted = formController.isSubmissionEntireForm();
            if (!canEditAfterCompleted) {
                Analytics.log(AnalyticsEvents.PARTIAL_FORM_FINALIZED, "form");
            }

            boolean isEncrypted = false;

            // build a submission.xml to hold the data being submitted
            // and (if appropriate) encrypt the files on the side

            // pay attention to the ref attribute of the submission profile...
            File instanceXml = formController.getInstanceFile();
            File submissionXml = new File(instanceXml.getParentFile(), "submission.xml");

            payload = formController.getSubmissionXml();

            // write out submission.xml -- the data to actually submit to aggregate

            progressListener.onProgressUpdate(
                    getLocalizedString(Collect.getInstance(), org.odk.collect.strings.R.string.survey_saving_finalizing_message));

            writeFile(payload, submissionXml.getAbsolutePath());

            // see if the form is encrypted and we can encrypt it...
            EncryptedFormInformation formInfo = EncryptionUtils.getEncryptedFormInformation(uri, formController.getSubmissionMetadata());
            if (formInfo != null) {
                // if we are encrypting, the form cannot be reopened afterward
                canEditAfterCompleted = false;
                // and encrypt the submission (this is a one-way operation)...

                progressListener.onProgressUpdate(
                        getLocalizedString(Collect.getInstance(), org.odk.collect.strings.R.string.survey_saving_encrypting_message));

                EncryptionUtils.generateEncryptedSubmission(instanceXml, submissionXml, formInfo);
                isEncrypted = true;

                Analytics.log(ENCRYPT_SUBMISSION, "form");
            }

            // At this point, we have:
            // 1. the saved original instanceXml,
            // 2. all the plaintext attachments
            // 2. the submission.xml that is the completed xml (whether encrypting or not)
            // 3. all the encrypted attachments if encrypting (isEncrypted = true).
            //
            // NEXT:
            // 1. Update the instance database (with status complete).
            // 2. Overwrite the instanceXml with the submission.xml
            //    and remove the plaintext attachments if encrypting

            instance = updateInstanceDatabase(false, canEditAfterCompleted, validationResult);

            if (!canEditAfterCompleted) {
                manageFilesAfterSavingEncryptedForm(instanceXml, submissionXml);
            } else {
                // try to delete the submissionXml file, since it is
                // identical to the existing instanceXml file
                // (we don't need to delete and rename anything).
                if (!submissionXml.delete()) {
                    String msg = "Error deleting " + submissionXml.getAbsolutePath()
                            + " (instance is re-openable)";
                    Timber.w(msg);
                }
            }

            // if encrypted, delete all plaintext files
            // (anything not named instanceXml or anything not ending in .enc)
            if (isEncrypted) {
                instance = instancesRepository.get(ContentUriHelper.getIdFromUri(uri));

                // Clear the geometry. Done outside of updateInstanceDatabase to avoid multiple
                // branches and because it has no knowledge of encryption status.
                instancesRepository.save(new Instance.Builder(instance)
                        .geometry(null)
                        .geometryType(null)
                        .build()
                );

                ContentValues values = new ContentValues();
                values.put(DatabaseInstanceColumns.GEOMETRY, (String) null);
                values.put(DatabaseInstanceColumns.GEOMETRY_TYPE, (String) null);

                if (!EncryptionUtils.deletePlaintextFiles(instanceXml, new File(lastSavedPath))) {
                    Timber.e(new Error("Error deleting plaintext files for " + instanceXml.getAbsolutePath()));
                }
            }
        }

        return instance;
    }

    /**
     * Returns the XPath path of the geo feature used for mapping that corresponds to the blank form
     * that the instance with the given uri is an instance of.
     */
    private static String getGeometryXpathForInstance(Instance instance) {
        Form form = new FormsRepositoryProvider(Collect.getInstance()).get().getLatestByFormIdAndVersion(instance.getFormId(), instance.getFormVersion());
        if (form != null) {
            return form.getGeometryXpath();
        } else {
            return null;
        }
    }

    public static void manageFilesAfterSavingEncryptedForm(File instanceXml, File submissionXml) throws IOException {
        // AT THIS POINT, there is no going back.  We are committed
        // to returning "success" (true) whether or not we can
        // rename "submission.xml" to instanceXml and whether or
        // not we can delete the plaintext media files.
        //
        // Handle the fall-out for a failed "submission.xml" rename
        // in the InstanceUploaderTask task.  Leftover plaintext media
        // files are handled during form deletion.

        // delete the restore Xml file.
        if (!instanceXml.delete()) {
            String msg = "Error deleting " + instanceXml.getAbsolutePath()
                    + " prior to renaming submission.xml";
            Timber.e(new Error(msg));
            throw new IOException(msg);
        }

        // rename the submission.xml to be the instanceXml
        if (!submissionXml.renameTo(instanceXml)) {
            String msg =
                    "Error renaming submission.xml to " + instanceXml.getAbsolutePath();
            Timber.e(new Error(msg));
            throw new IOException(msg);
        }
    }

    /**
     * Writes payload contents to the disk.
     */
    public static void writeFile(ByteArrayPayload payload, String path) throws IOException {
        org.odk.collect.shared.files.FileUtils.saveToFile(payload.getPayloadStream(), path);
    }
}
