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

package org.odk.collect.android.gdrive.sheets;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.Permission;

import org.odk.collect.android.exception.MultipleFoldersFoundException;
import org.odk.collect.android.utilities.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DriveHelper {
    public static final String ODK_GOOGLE_DRIVE_ROOT_FOLDER_NAME = "Open Data Kit";
    public static final String ODK_GOOGLE_DRIVE_SUBMISSION_FOLDER_NAME = "Submissions";

    public static final String FOLDER_MIME_TYPE = "application/vnd.google-apps.folder";
    private final DriveApi driveApi;

    public DriveHelper(DriveApi driveApi) {
        this.driveApi = driveApi;
    }

    /**
     * Returns id of the root folder or null
     */
    public String getRootFolderId() throws IOException {
        return driveApi.getFileId("root", "id");
    }

    @Nullable
    public Drive.Files.List buildRequest(String query, String fields) throws IOException {
        if (query != null && fields != null) {
            return driveApi.generateRequest(query, fields);
        }
        return null;
    }

    public void downloadFile(@NonNull String fileId, @NonNull File file) throws IOException {
        driveApi.downloadFile(fileId, file);
    }

    public String createOrGetIDOfSubmissionsFolder()
            throws IOException, MultipleFoldersFoundException {
        String rootFolderId = getIDOfFolderWithName(ODK_GOOGLE_DRIVE_ROOT_FOLDER_NAME, null, true);
        return getIDOfFolderWithName(ODK_GOOGLE_DRIVE_SUBMISSION_FOLDER_NAME, rootFolderId, true);
    }

    /**
     * Searches for the folder saved in google drive with the given name and returns it's id
     *
     * @param name                   The name of the folder saved in google drive
     * @param inFolder               The id of the folder containing the given folder
     * @param shouldCreateIfNotFound If the given folder is not found then this parameter decides
     *                               whether to create a new folder with the given name or not
     */
    @Nullable
    public String getIDOfFolderWithName(@NonNull String name, @Nullable String inFolder, boolean shouldCreateIfNotFound)
            throws IOException, MultipleFoldersFoundException {

        String id = null;

        // check if the folder exists
        List<com.google.api.services.drive.model.File> files = getFilesFromDrive(name, inFolder);

        if (files.size() > 1) {
            throw new MultipleFoldersFoundException("Multiple \"" + name + "\" folders found");
        }

        if (files.size() == 1) {
            id = files.get(0).getId();
        } else if (shouldCreateIfNotFound) {
            id = createFolderInDrive(name, inFolder);
        }

        return id;
    }

    /**
     * Upload a file to google drive
     *
     * @param mediaName           The name of the uploaded file
     * @param destinationFolderID Id of the folder into which the file has to be uploaded
     * @param toUpload            The file which is to be uploaded
     * @return id of the file if uploaded successfully
     */
    public String uploadFileToDrive(String mediaName, String destinationFolderID, File toUpload)
            throws IOException {

        //adding meta-data to the file
        com.google.api.services.drive.model.File fileMetadata = createNewFile(
                mediaName,
                null,
                destinationFolderID);
        String mimeType = FileUtils.getMimeType(toUpload);
        String fields = "id, parents";
        FileContent mediaContent = new FileContent(mimeType, toUpload);

        return driveApi.uploadFile(fileMetadata, mediaContent, fields);
    }

    /**
     * Creates a new folder in google drive
     *
     * @param folderName The name of the new folder
     * @param parentId   The id of the folder in which we want to create the new folder
     * @return id of the folder object if created successfully
     */
    public String createFolderInDrive(@NonNull String folderName,
                                      @Nullable String parentId)
            throws IOException {
        com.google.api.services.drive.model.File fileMetadata;

        //creating a new folder object using the data
        fileMetadata = createNewFile(folderName, FOLDER_MIME_TYPE, parentId);

        // make api call using drive service to create the folder on google drive
        String newFolderId = driveApi.createFile(fileMetadata, "id");

        //adding the permissions to folder
        setSharingPermissions(newFolderId);

        return newFolderId;
    }

    /**
     * Create a new {@link com.google.api.services.drive.model.File} object
     *
     * @param name     the name of the file
     * @param mimeType mime type of the file
     * @param parentId the id of the parent directory
     * @return new {@link com.google.api.services.drive.model.File} object
     */
    public com.google.api.services.drive.model.File createNewFile(@NonNull String name,
                                                                  @Nullable String mimeType,
                                                                  @Nullable String parentId) {
        com.google.api.services.drive.model.File file;
        file = new com.google.api.services.drive.model.File()
                .setName(name)
                .setViewersCanCopyContent(true);

        if (mimeType != null) {
            file.setMimeType(mimeType);
        }
        if (parentId != null) {
            file.setParents(Collections.singletonList(parentId));
        }
        return file;
    }

    /**
     * Sets read permission for anyone to the drive folder so that anyone who has the link
     * to the file can access it
     */
    private void setSharingPermissions(String folderId) throws IOException {
        Permission sharePermission = new Permission()
                .setType("anyone")
                .setRole("reader");

        driveApi.setPermission(folderId, "id", sharePermission);
    }

    /**
     * Fetches the list of files from google drive for a given folder.
     *
     * @param folderName (optional) The name of folder whose files are to be fetched
     *                   If folderName is null then all files are fetched from the drive
     * @param parentId   (optional) The id of the parent folder containing the given folder
     */
    public List<com.google.api.services.drive.model.File> getFilesFromDrive(
            @Nullable String folderName,
            @Nullable String parentId) throws IOException {

        List<com.google.api.services.drive.model.File> files = new ArrayList<>();

        String mimeType = folderName != null ? FOLDER_MIME_TYPE : null;
        String requestString = generateSearchQuery(folderName, parentId, mimeType);
        String fields = "nextPageToken, files(modifiedTime, id, name, mimeType)";
        Drive.Files.List request = buildRequest(requestString, fields);

        if (request != null) {
            driveApi.fetchAllFiles(request, files);
        }
        return files;
    }

    @Nullable
    public String generateSearchQuery(@Nullable String folderName,
                                      @Nullable String parentId,
                                      @Nullable String mimeType) {
        List<String> queryList = new ArrayList<>();
        if (folderName != null) {
            queryList.add(String.format("name = '%s'", folderName));
        }
        if (parentId != null) {
            queryList.add(String.format("'%s' in parents", parentId));
        }
        if (mimeType != null) {
            queryList.add(String.format("mimeType = '%s'", mimeType));
        }

        if (queryList.isEmpty()) {
            return null;
        }

        // this query prevents from searching the deleted files
        queryList.add("trashed = false");

        StringBuilder query = new StringBuilder(queryList.get(0));
        for (int i = 1; i < queryList.size(); i++) {
            query.append(" and ").append(queryList.get(i));
        }

        return query.toString();
    }

    public void fetchFilesForCurrentPage(Drive.Files.List request, List<com.google.api.services.drive.model.File> files)
            throws IOException {
        driveApi.fetchFilesForCurrentPage(request, files);
    }

}
