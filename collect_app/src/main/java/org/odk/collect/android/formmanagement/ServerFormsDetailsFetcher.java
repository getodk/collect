/*
 * Copyright 2018 Nafundi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.odk.collect.android.formmanagement;

import org.odk.collect.android.openrosa.OpenRosaFormSource;
import org.odk.collect.forms.Form;
import org.odk.collect.forms.FormListItem;
import org.odk.collect.forms.FormSource;
import org.odk.collect.forms.FormSourceException;
import org.odk.collect.android.utilities.FormUtils;
import org.odk.collect.forms.FormsRepository;
import org.odk.collect.forms.ManifestFile;
import org.odk.collect.forms.MediaFile;
import org.odk.collect.android.utilities.WebCredentialsUtils;
import org.odk.collect.shared.strings.Md5;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class ServerFormsDetailsFetcher {

    private final FormsRepository formsRepository;
    private final FormSource formSource;
    private final DiskFormsSynchronizer diskFormsSynchronizer;

    public ServerFormsDetailsFetcher(FormsRepository formsRepository,
                                     FormSource formSource,
                                     DiskFormsSynchronizer diskFormsSynchronizer) {
        this.formsRepository = formsRepository;
        this.formSource = formSource;
        this.diskFormsSynchronizer = diskFormsSynchronizer;
    }

    public void updateUrl(String url) {
        ((OpenRosaFormSource) formSource).updateUrl(url);
    }

    public void updateCredentials(WebCredentialsUtils webCredentialsUtils) {
        ((OpenRosaFormSource) formSource).updateWebCredentialsUtils(webCredentialsUtils);
    }

    public List<ServerFormDetails> fetchFormDetails() throws FormSourceException {
        diskFormsSynchronizer.synchronize();

        List<FormListItem> formListItems = formSource.fetchFormList();
        List<ServerFormDetails> serverFormDetailsList = new ArrayList<>();

        for (FormListItem listItem : formListItems) {
            ManifestFile manifestFile = null;

            if (listItem.getManifestURL() != null) {
                manifestFile = getManifestFile(formSource, listItem.getManifestURL());
            }

            List<Form> forms = formsRepository.getAllNotDeletedByFormId(listItem.getFormID());
            boolean thisFormAlreadyDownloaded = !forms.isEmpty();

            boolean isNewerFormVersionAvailable = false;
            if (thisFormAlreadyDownloaded) {
                if (isNewerFormVersionAvailable(listItem)) {
                    isNewerFormVersionAvailable = true;
                } else if (manifestFile != null) {
                    List<MediaFile> newMediaFiles = manifestFile.getMediaFiles();

                    if (newMediaFiles != null && !newMediaFiles.isEmpty()) {
                        isNewerFormVersionAvailable = areNewerMediaFilesAvailable(forms.get(0), newMediaFiles);
                    }
                }
            }

            ServerFormDetails serverFormDetails = new ServerFormDetails(
                    listItem.getName(),
                    listItem.getDownloadURL(),
                    listItem.getFormID(),
                    listItem.getVersion(),
                    listItem.getHashWithPrefix(),
                    !thisFormAlreadyDownloaded,
                    isNewerFormVersionAvailable,
                    manifestFile);

            serverFormDetailsList.add(serverFormDetails);
        }

        return serverFormDetailsList;
    }

    private ManifestFile getManifestFile(FormSource formSource, String manifestUrl) {
        if (manifestUrl == null) {
            return null;
        }

        try {
            return formSource.fetchManifest(manifestUrl);
        } catch (FormSourceException formSourceException) {
            Timber.w(formSourceException);
            return null;
        }
    }

    private boolean isNewerFormVersionAvailable(FormListItem formListItem) {
        if (formListItem.getHashWithPrefix() == null) {
            return false;
        }

        String hash = getMd5HashWithoutPrefix(formListItem.getHashWithPrefix());
        return formsRepository.getOneByMd5Hash(hash) == null;
    }

    private boolean areNewerMediaFilesAvailable(Form existingForm, List<MediaFile> newMediaFiles) {
        List<File> localMediaFiles = FormUtils.getMediaFiles(existingForm);

        if (localMediaFiles != null) {
            for (MediaFile newMediaFile : newMediaFiles) {
                if (!isMediaFileAlreadyDownloaded(localMediaFiles, newMediaFile)) {
                    return true;
                }
            }
        } else if (!newMediaFiles.isEmpty()) {
            return true;
        }

        return false;
    }

    private static boolean isMediaFileAlreadyDownloaded(List<File> localMediaFiles, MediaFile newMediaFile) {
        // TODO Zip files are ignored we should find a way to take them into account too
        if (newMediaFile.getFilename().endsWith(".zip")) {
            return true;
        }

        String mediaFileHash = newMediaFile.getHash();
        mediaFileHash = mediaFileHash.substring(4, mediaFileHash.length());
        for (File localMediaFile : localMediaFiles) {
            if (mediaFileHash.equals(Md5.getMd5Hash(localMediaFile))) {
                return true;
            }
        }
        return false;
    }

    private String getMd5HashWithoutPrefix(String hash) {
        return hash == null || hash.isEmpty() ? null : hash.substring("md5:".length());
    }
}
