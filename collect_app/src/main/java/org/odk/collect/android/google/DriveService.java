package org.odk.collect.android.google;

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

class DriveService {

    private final Drive drive;

    DriveService(Drive drive) {
        this.drive = drive;
    }

    String getFileId(String fileId, String fields) throws IOException {
        return drive.files()
                .get(fileId)
                .setFields(fields)
                .execute()
                .getId();
    }

    Drive.Files.List generateRequest(String query, String fields) throws IOException {
        return drive.files()
                .list()
                .setQ(query)
                .setFields(fields);
    }

    void downloadFile(String fileId, FileOutputStream fileOutputStream) throws IOException {
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

    String createFile(File file, String fields) throws IOException {
        return drive.files()
                .create(file)
                .setFields(fields)
                .execute()
                .getId();
    }

    void setPermission(String folderId, String fields, Permission permission) throws IOException {
        drive.permissions()
                .create(folderId, permission)
                .setFields(fields)
                .execute();
    }

    void fetchAllFiles(Drive.Files.List request, List<File> files) throws IOException {
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
