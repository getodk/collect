package org.odk.collect.android.gdrive.sheets;

import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.Permission;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface DriveApi {

    String getFileId(String fileId, String fields) throws IOException;

    Drive.Files.List generateRequest(String query, String fields) throws IOException;

    void downloadFile(String fileId, File file) throws IOException;

    String uploadFile(com.google.api.services.drive.model.File metadata, FileContent fileContent, String fields) throws IOException;

    String createFile(com.google.api.services.drive.model.File file, String fields) throws IOException;

    void setPermission(String folderId, String fields, Permission permission) throws IOException;

    void fetchAllFiles(Drive.Files.List request, List<com.google.api.services.drive.model.File> files) throws IOException;

    void fetchFilesForCurrentPage(Drive.Files.List request, List<com.google.api.services.drive.model.File> files) throws IOException;
}
