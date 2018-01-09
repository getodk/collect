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

package org.odk.collect.android.utilities.gdrive;

import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.Permission;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * This class only makes API calls using the drives API and does not contain any business logic
 *
 * @author Shobhit Agarwal
 */

public class DriveService {

    private final Drive drive;

    DriveService(Drive drive) {
        this.drive = drive;
    }

    public String getFileId(String fileId, String fields) throws IOException {
        return drive.files()
                .get(fileId)
                .setFields(fields)
                .execute()
                .getId();
    }

    public Drive.Files.List generateRequest(String query, String fields) throws IOException {
        return drive.files()
                .list()
                .setQ(query)
                .setFields(fields);
    }

    public void downloadFile(String fileId, FileOutputStream fileOutputStream) throws IOException {
        drive.files()
                .get(fileId)
                .executeMediaAndDownloadTo(fileOutputStream);
    }

    String uploadFile(File metadata, FileContent fileContent, String fields) throws IOException {
        return drive.files()
                .create(metadata, fileContent)
                .setFields(fields)
                .setIgnoreDefaultVisibility(true)
                .execute()
                .getId();
    }

    public String createFile(File file, String fields) throws IOException {
        return drive.files()
                .create(file)
                .setFields(fields)
                .execute()
                .getId();
    }

    public void setPermission(String folderId, String fields, Permission permission) throws IOException {
        drive.permissions()
                .create(folderId, permission)
                .setFields(fields)
                .execute();
    }

    public void fetchAllFiles(Drive.Files.List request, List<File> files) throws IOException {
        do {
            fetchFilesForCurrentPage(request, files);
        } while (request.getPageToken() != null && request.getPageToken().length() > 0);
    }

    void fetchFilesForCurrentPage(Drive.Files.List request, List<File> files) throws IOException {
        FileList fileList = request.execute();
        files.addAll(fileList.getFiles());
        request.setPageToken(fileList.getNextPageToken());
    }
}
