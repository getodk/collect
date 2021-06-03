/*
 * Copyright (C) 2017 Nyoman Ribeka
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

import android.net.Uri;
import android.os.AsyncTask;

import org.apache.commons.io.FileUtils;
import org.odk.collect.android.R;
import org.odk.collect.android.analytics.AnalyticsEvents;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.exception.EncryptionException;
import org.odk.collect.android.injection.DaggerUtils;
import org.odk.collect.android.instancemanagement.InstanceDeleter;
import org.odk.collect.android.javarosawrapper.FormController;
import org.odk.collect.android.listeners.DiskSyncListener;
import org.odk.collect.android.preferences.keys.GeneralKeys;
import org.odk.collect.android.preferences.source.SettingsProvider;
import org.odk.collect.android.provider.InstanceProviderAPI;
import org.odk.collect.android.storage.StoragePathProvider;
import org.odk.collect.android.storage.StorageSubdirectory;
import org.odk.collect.android.utilities.EncryptionUtils;
import org.odk.collect.android.utilities.FormsRepositoryProvider;
import org.odk.collect.android.utilities.InstancesRepositoryProvider;
import org.odk.collect.android.utilities.TranslationHandler;
import org.odk.collect.forms.Form;
import org.odk.collect.forms.instances.Instance;
import org.odk.collect.shared.Md5;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import timber.log.Timber;

/**
 * Background task for syncing form instances from the instances folder to the instances table.
 * Returns immediately if it detects an error.
 */
public class InstanceSyncTask extends AsyncTask<Void, String, String> {

    private static int counter;

    private String currentStatus = "";
    private DiskSyncListener diskSyncListener;
    private final SettingsProvider settingsProvider;
    StoragePathProvider storagePathProvider = new StoragePathProvider();

    public String getStatusMessage() {
        return currentStatus;
    }

    public void setDiskSyncListener(DiskSyncListener diskSyncListener) {
        this.diskSyncListener = diskSyncListener;
    }

    public InstanceSyncTask(SettingsProvider settingsProvider) {
        this.settingsProvider = settingsProvider;
    }

    @Override
    protected String doInBackground(Void... params) {
        int currentInstance = ++counter;
        Timber.i("[%d] doInBackground begins!", currentInstance);
        try {
            List<String> candidateInstances = new LinkedList<>();
            File instancesPath = new File(storagePathProvider.getOdkDirPath(StorageSubdirectory.INSTANCES));
            if (instancesPath.exists() && instancesPath.isDirectory()) {
                File[] instanceFolders = instancesPath.listFiles();
                if (instanceFolders == null || instanceFolders.length == 0) {
                    Timber.i("[%d] Empty instance folder. Stopping scan process.", currentInstance);
                    Timber.d("Instance scan completed");
                    return currentStatus;
                }

                // Build the list of potential path that we need to add to the content provider
                for (File instanceDir : instanceFolders) {
                    File instanceFile = new File(instanceDir, instanceDir.getName() + ".xml");
                    if (!instanceFile.exists()) {
                        // Look for submission file that might have been manually copied from e.g. Briefcase
                        File submissionFile = new File(instanceDir, "submission.xml");
                        if (submissionFile.exists()) {
                            submissionFile.renameTo(instanceFile);
                        }
                    }
                    if (instanceFile.exists() && instanceFile.canRead()) {
                        candidateInstances.add(instanceFile.getAbsolutePath());
                    } else {
                        Timber.i("[%d] Ignoring: %s", currentInstance, instanceDir.getAbsolutePath());
                    }
                }
                Collections.sort(candidateInstances);

                List<Instance> instancesToRemove = new ArrayList<>();

                // Remove all the path that's already in the content provider
                List<Instance> instances = new InstancesRepositoryProvider().get().getAllNotDeleted();

                for (Instance instance : instances) {
                    String instanceFilename = instance.getInstanceFilePath();

                    if (candidateInstances.contains(instanceFilename) || instance.getStatus().equals(Instance.STATUS_SUBMITTED)) {
                        candidateInstances.remove(instanceFilename);
                    } else {
                        instancesToRemove.add(instance);
                    }
                }

                for (Instance instance : instancesToRemove) {
                    new InstanceDeleter(new InstancesRepositoryProvider().get(), new FormsRepositoryProvider(Collect.getInstance()).get()).delete(instance.getDbId());
                }

                final boolean instanceSyncFlag = settingsProvider.getGeneralSettings().getBoolean(GeneralKeys.KEY_INSTANCE_SYNC);

                int counter = 0;
                // Begin parsing and add them to the content provider
                for (String candidateInstance : candidateInstances) {
                    String instanceFormId = getFormIdFromInstance(candidateInstance);
                    // only process if we can find the id from the instance file
                    if (instanceFormId != null) {
                        try {
                            // TODO: optimize this by caching the previously found form definition
                            // TODO: optimize this by caching unavailable form definition to skip
                            List<Form> forms = new FormsRepositoryProvider(Collect.getInstance()).get().getAllByFormId(instanceFormId);

                            if (!forms.isEmpty()) {
                                Form form = forms.get(0);
                                String jrFormId = form.getFormId();
                                String jrVersion = form.getVersion();
                                String formName = form.getDisplayName();
                                String submissionUri = form.getSubmissionUri();

                                Instance instance = new InstancesRepositoryProvider().get().save(new Instance.Builder()
                                        .instanceFilePath(candidateInstance)
                                        .submissionUri(submissionUri)
                                        .displayName(formName)
                                        .formId(jrFormId)
                                        .formVersion(jrVersion)
                                        .status(instanceSyncFlag ? Instance.STATUS_COMPLETE : Instance.STATUS_INCOMPLETE)
                                        .canEditWhenComplete(true)
                                        .build()
                                );
                                counter++;

                                encryptInstanceIfNeeded(form, instance);
                            }
                        } catch (IOException | EncryptionException e) {
                            Timber.w(e);
                        }
                    }
                }
                if (counter > 0) {
                    currentStatus += TranslationHandler.getString(Collect.getInstance(), R.string.instance_scan_count, counter);
                }
            }
        } finally {
            Timber.i("[%d] doInBackground ends!", currentInstance);
        }
        return currentStatus;
    }

    private String getFormIdFromInstance(final String instancePath) {
        String instanceFormId = null;
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new File(instancePath));
            Element element = document.getDocumentElement();
            instanceFormId = element.getAttribute("id");
        } catch (Exception | Error e) {
            Timber.w("Unable to read form id from %s", instancePath);
        }
        return instanceFormId;
    }

    private String getInstanceIdFromInstance(final String instancePath) {
        String instanceId = null;
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new File(instancePath));
            Element element = document.getDocumentElement();
            instanceId = element.getAttribute("instanceID");
        } catch (Exception | Error e) {
            Timber.w("Unable to read form instanceID from %s", instancePath);
        }
        return instanceId;
    }

    private void encryptInstanceIfNeeded(Form form, Instance instance) throws EncryptionException, IOException {
        if (instance != null) {
            if (shouldInstanceBeEncrypted(form)) {
                logImportAndEncrypt(form);
                encryptInstance(instance);
            }
        }
    }

    private void logImportAndEncrypt(Form form) {
        String id = form.getFormId();
        String title = form.getDisplayName();
        String formIdHash = Md5.getMd5Hash(new ByteArrayInputStream((id + " " + title).getBytes()));
        DaggerUtils.getComponent(Collect.getInstance()).analytics().logFormEvent(AnalyticsEvents.IMPORT_AND_ENCRYPT_INSTANCE, formIdHash);
    }

    private void encryptInstance(Instance instance) throws EncryptionException, IOException {
        String instancePath = instance.getInstanceFilePath();
        File instanceXml = new File(instancePath);
        if (!new File(instanceXml.getParentFile(), "submission.xml.enc").exists()) {
            Uri uri = Uri.parse(InstanceProviderAPI.CONTENT_URI + "/" + instance.getDbId());
            FormController.InstanceMetadata instanceMetadata = new FormController.InstanceMetadata(getInstanceIdFromInstance(instancePath), null, null);
            EncryptionUtils.EncryptedFormInformation formInfo = EncryptionUtils.getEncryptedFormInformation(uri, instanceMetadata);

            if (formInfo != null) {
                File submissionXml = new File(instanceXml.getParentFile(), "submission.xml");
                FileUtils.copyFile(instanceXml, submissionXml);

                EncryptionUtils.generateEncryptedSubmission(instanceXml, submissionXml, formInfo);

                new InstancesRepositoryProvider().get().save(new Instance.Builder(instance)
                        .canEditWhenComplete(false)
                        .geometryType(null)
                        .geometry(null)
                        .build()
                );

                SaveFormToDisk.manageFilesAfterSavingEncryptedForm(instanceXml, submissionXml);
                if (!EncryptionUtils.deletePlaintextFiles(instanceXml, null)) {
                    Timber.e("Error deleting plaintext files for %s", instanceXml.getAbsolutePath());
                }
            }
        }
    }

    private boolean shouldInstanceBeEncrypted(Form form) {
        return form.getBASE64RSAPublicKey() != null;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        if (diskSyncListener != null) {
            diskSyncListener.syncComplete(result);
        }
    }
}
