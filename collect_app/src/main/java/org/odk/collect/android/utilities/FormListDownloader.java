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

package org.odk.collect.android.utilities;

import android.app.Application;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;

import androidx.annotation.Nullable;

import org.odk.collect.android.R;
import org.odk.collect.android.dao.FormsDaoFormRepository;
import org.odk.collect.android.dao.FormsDaoMediaFileRepository;
import org.odk.collect.android.logic.FormDetails;
import org.odk.collect.android.logic.ManifestFile;
import org.odk.collect.android.logic.MediaFile;
import org.odk.collect.android.openrosa.OpenRosaXMLFetcher;
import org.odk.collect.android.openrosa.api.FormAPI;
import org.odk.collect.android.openrosa.api.FormAPIError;
import org.odk.collect.android.openrosa.api.FormListItem;
import org.odk.collect.android.openrosa.api.OpenRosaFormAPI;
import org.odk.collect.android.preferences.GeneralKeys;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import timber.log.Timber;

public class FormListDownloader {

    // used to store error message if one occurs
    public static final String DL_ERROR_MSG = "dlerrormessage";
    public static final String DL_AUTH_REQUIRED = "dlauthrequired";

    private final WebCredentialsUtils webCredentialsUtils;
    private final OpenRosaXMLFetcher openRosaXMLFetcher;
    private final Application application;
    private final FormsDaoFormRepository formRepository;
    private final FormsDaoMediaFileRepository mediaFileRepository;

    public FormListDownloader(
            Application application,
            OpenRosaXMLFetcher openRosaXMLFetcher,
            WebCredentialsUtils webCredentialsUtils) {
        this.application = application;
        this.openRosaXMLFetcher = openRosaXMLFetcher;
        this.webCredentialsUtils = webCredentialsUtils;

        formRepository = new FormsDaoFormRepository();
        mediaFileRepository = new FormsDaoMediaFileRepository();
    }

    public HashMap<String, FormDetails> downloadFormList(boolean alwaysCheckMediaFiles) {
        return downloadFormList(null, null, null, alwaysCheckMediaFiles);
    }

    public HashMap<String, FormDetails> downloadFormList(@Nullable String url, @Nullable String username,
                                                         @Nullable String password, boolean alwaysCheckMediaFiles) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(
                application);

        String downloadListUrl = url != null ? url :
                settings.getString(GeneralKeys.KEY_SERVER_URL,
                        application.getString(R.string.default_server_url));

        while (downloadListUrl.endsWith("/")) {
            downloadListUrl = downloadListUrl.substring(0, downloadListUrl.length() - 1);
        }

        String formListPath = application.getString(R.string.default_odk_formlist);
        String downloadPath = (url != null) ? formListPath : settings.getString(GeneralKeys.KEY_FORMLIST_URL, formListPath);

        // We populate this with available forms from the specified server.
        // <formname, details>
        HashMap<String, FormDetails> formList = new HashMap<>();

        if (url != null) {
            String host = Uri.parse(url).getHost();

            if (host != null) {
                if (username != null && password != null) {
                    webCredentialsUtils.saveCredentials(url, username, password);
                } else {
                    webCredentialsUtils.clearCredentials(url);
                }
            }
        }

        OpenRosaFormAPI formAPI = new OpenRosaFormAPI(openRosaXMLFetcher, downloadListUrl, downloadPath);

        try {
            List<FormListItem> formListItems = formAPI.fetchFormList();
            for (FormListItem listItem : formListItems) {
                boolean isNewerFormVersionAvailable = false;
                boolean areNewerMediaFilesAvailable = false;
                ManifestFile manifestFile = null;

                if (isThisFormAlreadyDownloaded(listItem.getFormID())) {
                    isNewerFormVersionAvailable = isNewerFormVersionAvailable(FormDownloader.getMd5Hash(listItem.getHashWithPrefix()));
                    if ((!isNewerFormVersionAvailable || alwaysCheckMediaFiles) && listItem.getManifestURL() != null) {
                        manifestFile = getManifestFile(formAPI, listItem.getManifestURL());
                        if (manifestFile != null) {
                            List<MediaFile> newMediaFiles = manifestFile.getMediaFiles();
                            if (newMediaFiles != null && !newMediaFiles.isEmpty()) {
                                areNewerMediaFilesAvailable = areNewerMediaFilesAvailable(listItem.getFormID(), listItem.getVersion(), newMediaFiles);
                            }
                        }
                    }
                }

                FormDetails formDetails = FormDetails.toFormDetails(
                        listItem,
                        manifestFile != null ? manifestFile.getHash() : null,
                        isNewerFormVersionAvailable,
                        areNewerMediaFilesAvailable
                );

                formList.put(listItem.getFormID(), formDetails);
            }
        } catch (FormAPIError formAPIError) {
            Timber.e(formAPIError);

            switch (formAPIError.getType()) {
                case AUTH_REQUIRED:
                    formList.put(DL_AUTH_REQUIRED, new FormDetails(formAPIError.getMessage()));
                    break;
                case PARSE_ERROR:
                    formList.put(DL_ERROR_MSG, new FormDetails(application.getString(R.string.parse_openrosa_formlist_failed, formAPIError.getMessage())));
                    break;
                case LEGACY_PARSE_ERROR:
                    formList.put(DL_ERROR_MSG, new FormDetails(application.getString(R.string.parse_legacy_formlist_failed, formAPIError.getMessage())));
                    break;
                default:
                    formList.put(DL_ERROR_MSG, new FormDetails(formAPIError.getMessage()));
            }
        }

        clearTemporaryCredentials(url);
        return formList;
    }

    private void clearTemporaryCredentials(@Nullable String url) {
        if (url != null) {
            String host = Uri.parse(url).getHost();

            if (host != null) {
                webCredentialsUtils.clearCredentials(url);
            }
        }
    }

    private boolean isThisFormAlreadyDownloaded(String formId) {
        return formRepository.contains(formId);
    }

    private ManifestFile getManifestFile(FormAPI formAPI, String manifestUrl) {
        if (manifestUrl == null) {
            return null;
        }

        try {
            return formAPI.fetchManifest(manifestUrl);
        } catch (FormAPIError formAPIError) {
            Timber.e(formAPIError);
            return null;
        }
    }

    private boolean isNewerFormVersionAvailable(String md5Hash) {
        if (md5Hash == null) {
            return false;
        }

        return formRepository.getAll().stream().noneMatch(f -> f.getMD5Hash().equals(md5Hash));
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
