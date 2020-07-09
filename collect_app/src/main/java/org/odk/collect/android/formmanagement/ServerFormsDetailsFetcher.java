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

import org.odk.collect.android.forms.FormRepository;
import org.odk.collect.android.forms.MediaFileRepository;
import org.odk.collect.android.openrosa.api.FormListApi;
import org.odk.collect.android.openrosa.api.FormApiException;
import org.odk.collect.android.openrosa.api.FormListItem;
import org.odk.collect.android.openrosa.api.ManifestFile;
import org.odk.collect.android.openrosa.api.MediaFile;
import org.odk.collect.android.utilities.FileUtils;
import org.odk.collect.android.utilities.MultiFormDownloader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class ServerFormsDetailsFetcher {

    private final FormRepository formRepository;
    private final MediaFileRepository mediaFileRepository;
    private final FormListApi formListAPI;

    public ServerFormsDetailsFetcher(FormRepository formRepository,
                                     MediaFileRepository mediaFileRepository,
                                     FormListApi formListAPI) {
        this.formRepository = formRepository;
        this.mediaFileRepository = mediaFileRepository;
        this.formListAPI = formListAPI;
    }

    public List<ServerFormDetails> fetchFormDetails() throws FormApiException {
        return fetchFormDetails(true);
    }

    public List<ServerFormDetails> fetchFormDetails(boolean checkMediaFiles) throws FormApiException {
        List<FormListItem> formListItems = formListAPI.fetchFormList();
        List<ServerFormDetails> serverFormDetailsList = new ArrayList<>();

        for (FormListItem listItem : formListItems) {
            boolean isNewerFormVersionAvailable = false;
            boolean areNewerMediaFilesAvailable = false;
            ManifestFile manifestFile = null;

            if (isThisFormAlreadyDownloaded(listItem.getFormID())) {
                isNewerFormVersionAvailable = isNewerFormVersionAvailable(MultiFormDownloader.getMd5Hash(listItem.getHashWithPrefix()));
                if ((!isNewerFormVersionAvailable || checkMediaFiles) && listItem.getManifestURL() != null) {
                    manifestFile = getManifestFile(formListAPI, listItem.getManifestURL());
                    if (manifestFile != null) {
                        List<MediaFile> newMediaFiles = manifestFile.getMediaFiles();
                        if (newMediaFiles != null && !newMediaFiles.isEmpty()) {
                            areNewerMediaFilesAvailable = areNewerMediaFilesAvailable(listItem.getFormID(), listItem.getVersion(), newMediaFiles);
                        }
                    }
                }
            }

            String manifestFileHash = manifestFile != null ? manifestFile.getHash() : null;
            ServerFormDetails serverFormDetails = new ServerFormDetails(
                    listItem.getName(),
                    listItem.getDownloadURL(),
                    listItem.getManifestURL(),
                    listItem.getFormID(),
                    listItem.getVersion(),
                    listItem.getHashWithPrefix(),
                    manifestFileHash,
                    isNewerFormVersionAvailable,
                    areNewerMediaFilesAvailable
            );

            serverFormDetailsList.add(serverFormDetails);
        }
        return serverFormDetailsList;
    }

    private boolean isThisFormAlreadyDownloaded(String formId) {
        return formRepository.contains(formId);
    }

    private ManifestFile getManifestFile(FormListApi formListAPI, String manifestUrl) {
        if (manifestUrl == null) {
            return null;
        }

        try {
            return formListAPI.fetchManifest(manifestUrl);
        } catch (FormApiException formApiException) {
            Timber.w(formApiException);
            return null;
        }
    }

    private boolean isNewerFormVersionAvailable(String md5Hash) {
        if (md5Hash == null) {
            return false;
        }

        return formRepository.getByMd5Hash(md5Hash) == null;
    }

    private boolean areNewerMediaFilesAvailable(String formId, String formVersion, List<MediaFile> newMediaFiles) {
        List<File> localMediaFiles = mediaFileRepository.getAll(formId, formVersion);

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
            if (mediaFileHash.equals(FileUtils.getMd5Hash(localMediaFile))) {
                return true;
            }
        }
        return false;
    }
}
