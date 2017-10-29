/*
 * Copyright (C) 2017 Shobhit
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

package org.odk.collect.android.google;


import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.Permission;

import org.odk.collect.android.application.Collect;
import org.odk.collect.android.exception.MultipleFoldersFoundException;
import org.odk.collect.android.logic.DriveListItem;
import org.odk.collect.android.utilities.FileUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import timber.log.Timber;

import static org.odk.collect.android.tasks.InstanceGoogleSheetsUploader.GOOGLE_DRIVE_ROOT_FOLDER;
import static org.odk.collect.android.tasks.InstanceGoogleSheetsUploader.GOOGLE_DRIVE_SUBFOLDER;

public class DriveHelper {

    private Drive drive;

    DriveHelper(GoogleAccountCredential credential, HttpTransport transport, JsonFactory jsonFactory) {
        drive = new Drive.Builder(transport, jsonFactory, credential)
                .setApplicationName("ODK-Collect")
                .build();
    }

    /**
     * Returns id of the root folder or null
     */
    public String getRootFolderId() throws IOException {
        return drive.files()
                .get("root")
                .setFields("id")
                .execute().getId();
    }

    public Drive.Files.List buildRequest(String query) {
        try {
            return drive.files()
                    .list()
                    .setQ(query)
                    .setFields("nextPageToken, files(modifiedTime, id, name, mimeType)");

        } catch (IOException e) {
            Timber.e(e);
        }
        return null;
    }

    public boolean downloadFile(DriveListItem fileItem, HashMap<String, Object> results) {
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(new File(Collect.FORMS_PATH + File.separator + fileItem.getName()));
            downloadFile(fileItem.getDriveId()).writeTo(fileOutputStream);
        } catch (IOException e) {
            Timber.e(e);
            results.put(fileItem.getName(), e.getMessage());
            return false;
        } finally {
            try {
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
            } catch (IOException e) {
                Timber.e(e, "Unable to close the file output stream");
            }
        }
        return true;
    }

    private ByteArrayOutputStream downloadFile(String fileId) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        drive.files().get(fileId).executeMediaAndDownloadTo(outputStream);
        return outputStream;
    }

    public String createOrGetIDOfFolderWithName(String jrFormId)
            throws IOException, MultipleFoldersFoundException {
        String submissionsFolderId = createOrGetIDOfSubmissionsFolder();
        return getIDOfFolderWithName(jrFormId, submissionsFolderId);
    }

    private String createOrGetIDOfSubmissionsFolder()
            throws IOException, MultipleFoldersFoundException {
        String rootFolderId = getIDOfFolderWithName(GOOGLE_DRIVE_ROOT_FOLDER, null);
        return getIDOfFolderWithName(GOOGLE_DRIVE_SUBFOLDER, rootFolderId);
    }

    public String getIDOfFolderWithName(String name, String inFolder) throws IOException, MultipleFoldersFoundException {

        com.google.api.services.drive.model.File folder = null;

        // check if the folder exists
        ArrayList<com.google.api.services.drive.model.File> files = getFilesFromDrive(name, inFolder);

        for (com.google.api.services.drive.model.File file : files) {
            if (folder == null) {
                folder = file;
            } else {
                throw new MultipleFoldersFoundException("Multiple \"" + name + "\" folders found");
            }
        }

        // if the folder is not found then create a new one
        if (folder == null) {
            folder = createFolderInDrive(name, inFolder);
        }
        return folder.getId();

    }

    public String uploadFileToDrive(String mediaName, String destinationFolderID,
                                    File toUpload) throws IOException {

        //adding meta-data to the file
        com.google.api.services.drive.model.File fileMetadata =
                new com.google.api.services.drive.model.File()
                        .setName(mediaName)
                        .setViewersCanCopyContent(true)
                        .setParents(Collections.singletonList(destinationFolderID));

        String type = FileUtils.getMimeType(toUpload.getPath());
        FileContent mediaContent = new FileContent(type, toUpload);

        return drive.files()
                .create(fileMetadata, mediaContent)
                .setFields("id, parents")
                .setIgnoreDefaultVisibility(true)
                .execute()
                .getId();
    }

    private com.google.api.services.drive.model.File createFolderInDrive(String folderName,
                                                                         String parentId)
            throws IOException {

        //creating a new folder
        com.google.api.services.drive.model.File fileMetadata = new
                com.google.api.services.drive.model.File();
        fileMetadata.setName(folderName);
        fileMetadata.setMimeType("application/vnd.google-apps.folder");
        if (parentId != null) {
            fileMetadata.setParents(Collections.singletonList(parentId));
        }

        com.google.api.services.drive.model.File folder;
        folder = drive.files().create(fileMetadata)
                .setFields("id")
                .execute();

        //adding the permissions to folder
        Permission sharePermission = new Permission()
                .setType("anyone")
                .setRole("reader");

        drive.permissions().create(folder.getId(), sharePermission)
                .setFields("id")
                .execute();

        return folder;
    }

    private ArrayList<com.google.api.services.drive.model.File> getFilesFromDrive(
            String folderName,
            String parentId) throws IOException {

        ArrayList<com.google.api.services.drive.model.File> files = new ArrayList<>();
        FileList fileList;
        String pageToken;
        do {
            if (parentId == null) {
                fileList = drive.files().list()
                        .setQ("name = '" + folderName + "' and "
                                + "mimeType = 'application/vnd.google-apps.folder'"
                                + " and trashed=false")
                        .execute();
            } else {
                fileList = drive.files().list()
                        .setQ("name = '" + folderName + "' and "
                                + "mimeType = 'application/vnd.google-apps.folder'"
                                + " and '" + parentId + "' in parents" + " and trashed=false")
                        .execute();
            }
            files.addAll(fileList.getFiles());
            pageToken = fileList.getNextPageToken();
        } while (pageToken != null);
        return files;
    }
}
