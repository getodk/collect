package org.odk.collect.android.gdrive;

import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.Permission;

import org.odk.collect.android.gdrive.sheets.DriveApi;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * This class only makes API calls using the drives API and does not contain any business logic
 *
 * @author Shobhit Agarwal
 */

public class GoogleDriveApi implements DriveApi {

    private final Drive drive;

    GoogleDriveApi(Drive drive) {
        this.drive = drive;
    }

    @Override
    public String getFileId(String fileId, String fields) throws IOException {
        return drive.files()
                .get(fileId)
                .setFields(fields)
                .execute()
                .getId();
    }

    @Override
    public Drive.Files.List generateRequest(String query, String fields) throws IOException {
        return drive.files()
                .list()
                .setQ(query)
                .setFields(fields);
    }

    @Override
    public void downloadFile(String fileId, File file) throws IOException {
        try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            drive.files()
                    .get(fileId)
                    .executeMediaAndDownloadTo(fileOutputStream);
        }
    }

    @Override
    public String uploadFile(com.google.api.services.drive.model.File metadata, FileContent fileContent, String fields) throws IOException {
        return drive.files()
                .create(metadata, fileContent)
                .setFields(fields)
                .setIgnoreDefaultVisibility(true)
                .execute()
                .getId();
    }

    @Override
    public String createFile(com.google.api.services.drive.model.File file, String fields) throws IOException {
        return drive.files()
                .create(file)
                .setFields(fields)
                .execute()
                .getId();
    }

    @Override
    public void setPermission(String folderId, String fields, Permission permission) throws IOException {
        drive.permissions()
                .create(folderId, permission)
                .setFields(fields)
                .execute();
    }

    @Override
    public void fetchAllFiles(Drive.Files.List request, List<com.google.api.services.drive.model.File> files) throws IOException {
        do {
            fetchFilesForCurrentPage(request, files);
        } while (request.getPageToken() != null && request.getPageToken().length() > 0);
    }

    @Override
    public void fetchFilesForCurrentPage(Drive.Files.List request, List<com.google.api.services.drive.model.File> files) throws IOException {
        FileList fileList = request.execute();
        files.addAll(fileList.getFiles());
        request.setPageToken(fileList.getNextPageToken());
    }
}
