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

import android.net.Uri;

import org.javarosa.core.reference.ReferenceManager;
import org.javarosa.core.reference.RootTranslator;
import org.kxml2.kdom.Element;
import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.formmanagement.ServerFormDetails;
import org.odk.collect.android.database.DatabaseFormsRepository;
import org.odk.collect.android.forms.Form;
import org.odk.collect.android.forms.FormsRepository;
import org.odk.collect.android.listeners.FormDownloaderListener;
import org.odk.collect.android.logic.FileReferenceFactory;
import org.odk.collect.android.openrosa.OpenRosaXmlFetcher;
import org.odk.collect.android.openrosa.api.FormListApi;
import org.odk.collect.android.openrosa.api.MediaFile;
import org.odk.collect.android.openrosa.api.OpenRosaFormListApi;
import org.odk.collect.android.provider.FormsProviderAPI.FormsColumns;
import org.odk.collect.android.storage.StoragePathProvider;
import org.odk.collect.android.storage.StorageSubdirectory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

import static org.odk.collect.android.utilities.FileUtils.LAST_SAVED_FILENAME;
import static org.odk.collect.android.utilities.FileUtils.STUB_XML;
import static org.odk.collect.android.utilities.FileUtils.write;

@Deprecated
public class MultiFormDownloader {

    private static final String MD5_COLON_PREFIX = "md5:";
    private static final String TEMP_DOWNLOAD_EXTENSION = ".tempDownload";

    private final FormListApi formListApi;
    private final FormsRepository formsRepository;

    @Deprecated
    public MultiFormDownloader(OpenRosaXmlFetcher openRosaXmlFetcher) {
        this.formsRepository = new DatabaseFormsRepository();
        formListApi = new OpenRosaFormListApi(openRosaXmlFetcher);
    }

    private static final String NAMESPACE_OPENROSA_ORG_XFORMS_XFORMS_MANIFEST =
            "http://openrosa.org/xforms/xformsManifest";

    static boolean isXformsManifestNamespacedElement(Element e) {
        return e.getNamespace().equalsIgnoreCase(NAMESPACE_OPENROSA_ORG_XFORMS_XFORMS_MANIFEST);
    }

    private static class TaskCancelledException extends Exception {
        private final File file;

        TaskCancelledException(File file) {
            super("Task was cancelled during processing of " + file);
            this.file = file;
        }

        TaskCancelledException() {
            super("Task was cancelled");
            this.file = null;
        }
    }

    @Deprecated
    public HashMap<ServerFormDetails, String> downloadForms(List<ServerFormDetails> toDownload, FormDownloaderListener stateListener) {
        int total = toDownload.size();
        int count = 1;

        final HashMap<ServerFormDetails, String> result = new HashMap<>();

        for (ServerFormDetails fd : toDownload) {
            try {
                String message = processOneForm(total, count++, fd, stateListener);
                result.put(fd, message.isEmpty() ? Collect.getInstance().getString(R.string.success) : message);
            } catch (TaskCancelledException cd) {
                break;
            }
        }

        return result;
    }

    /**
     * Processes one form download.
     *
     * @param total the total number of forms being downloaded by this task
     * @param count the number of this form
     * @param fd    the FormDetails
     * @return an empty string for success, or a nonblank string with one or more error messages
     * @throws TaskCancelledException to signal that form downloading is to be canceled
     */
    private String processOneForm(int total, int count, ServerFormDetails fd, FormDownloaderListener stateListener) throws TaskCancelledException {
        if (stateListener != null) {
            stateListener.progressUpdate(fd.getFormName(), String.valueOf(count), String.valueOf(total));
        }
        String message = "";
        if (stateListener != null && stateListener.isTaskCanceled()) {
            throw new TaskCancelledException();
        }

        // use a temporary media path until everything is ok.
        String tempMediaPath = new File(new StoragePathProvider().getDirPath(StorageSubdirectory.CACHE),
                String.valueOf(System.currentTimeMillis())).getAbsolutePath();
        final String finalMediaPath;
        FileResult fileResult = null;
        try {
            // get the xml file
            // if we've downloaded a duplicate, this gives us the file
            fileResult = downloadXform(fd.getFormName(), fd.getDownloadUrl(), stateListener);

            if (fd.getManifestUrl() != null) {
                finalMediaPath = FileUtils.constructMediaPath(
                        fileResult.getFile().getAbsolutePath());
                String error = downloadManifestAndMediaFiles(tempMediaPath, finalMediaPath, fd,
                        count, total, stateListener);
                if (error != null) {
                    message += error;
                }
            } else {
                Timber.i("No Manifest for: %s", fd.getFormName());
            }
        } catch (TaskCancelledException e) {
            Timber.i(e);
            cleanUp(fileResult, e.file, tempMediaPath);

            // do not download additional forms.
            throw e;
        } catch (Exception e) {
            return message + getExceptionMessage(e);
        }

        if (stateListener != null && stateListener.isTaskCanceled()) {
            cleanUp(fileResult, null, tempMediaPath);
            fileResult = null;
        }

        if (fileResult == null) {
            return message + "Downloading Xform failed.";
        }

        Map<String, String> parsedFields = null;
        if (fileResult.isNew) {
            try {
                final long start = System.currentTimeMillis();
                Timber.w("Parsing document %s", fileResult.file.getAbsolutePath());

                // Add a stub last-saved instance to the tmp media directory so it will be resolved
                // when parsing a form definition with last-saved reference
                File tmpLastSaved = new File(tempMediaPath, LAST_SAVED_FILENAME);
                write(tmpLastSaved, STUB_XML.getBytes(Charset.forName("UTF-8")));
                ReferenceManager.instance().reset();
                ReferenceManager.instance().addReferenceFactory(new FileReferenceFactory(tempMediaPath));
                ReferenceManager.instance().addSessionRootTranslator(new RootTranslator("jr://file-csv/", "jr://file/"));

                parsedFields = FileUtils.getMetadataFromFormDefinition(fileResult.file);
                ReferenceManager.instance().reset();
                FileUtils.deleteAndReport(tmpLastSaved);

                Timber.i("Parse finished in %.3f seconds.",
                        (System.currentTimeMillis() - start) / 1000F);
            } catch (RuntimeException e) {
                return message + e.getMessage();
            }
        }

        boolean installed = false;

        if ((stateListener == null || !stateListener.isTaskCanceled()) && message.isEmpty()) {
            if (!fileResult.isNew || isSubmissionOk(parsedFields)) {
                installed = installEverything(tempMediaPath, fileResult, parsedFields);
            } else {
                message += Collect.getInstance().getString(R.string.xform_parse_error,
                        fileResult.file.getName(), "submission url");
            }
        }
        if (!installed) {
            message += Collect.getInstance().getString(R.string.copying_media_files_failed);
            cleanUp(fileResult, null, tempMediaPath);
        }
        return message;
    }

    private boolean isSubmissionOk(Map<String, String> parsedFields) {
        String submission = parsedFields.get(FileUtils.SUBMISSIONURI);
        return submission == null || Validator.isUrlValid(submission);
    }

    boolean installEverything(String tempMediaPath, FileResult fileResult, Map<String, String> parsedFields) {
        UriResult uriResult = null;
        try {
            uriResult = findExistingOrCreateNewUri(fileResult.file, parsedFields);
            if (uriResult != null) {
                Timber.w("Form uri = %s, isNew = %b", uriResult.getUri().toString(), uriResult.isNew());

                // move the media files in the media folder
                if (tempMediaPath != null) {
                    File formMediaPath = new File(uriResult.getMediaPath());
                    FileUtils.moveMediaFiles(tempMediaPath, formMediaPath);
                }
                return true;
            } else {
                Timber.w("Form uri = null");
            }
        } catch (IOException e) {
            Timber.e(e);

            if (uriResult.isNew() && fileResult.isNew()) {
                // this means we should delete the entire form together with the metadata
                Uri uri = uriResult.getUri();
                Timber.w("The form is new. We should delete the entire form.");
                int deletedCount = Collect.getInstance().getContentResolver().delete(uri,
                        null, null);
                Timber.w("Deleted %d rows using uri %s", deletedCount, uri.toString());
            }
        }
        return false;
    }

    private void cleanUp(FileResult fileResult, File fileOnCancel, String tempMediaPath) {
        if (fileResult == null) {
            Timber.w("The user cancelled (or an exception happened) the download of a form at the "
                    + "very beginning.");
        } else {
            String md5Hash = FileUtils.getMd5Hash(fileResult.file);
            if (md5Hash != null) {
                formsRepository.deleteFormsByMd5Hash(md5Hash);
            }
            FileUtils.deleteAndReport(fileResult.getFile());
        }

        FileUtils.deleteAndReport(fileOnCancel);

        if (tempMediaPath != null) {
            FileUtils.purgeMediaPath(tempMediaPath);
        }
    }

    private String getExceptionMessage(Exception e) {
        String msg = e.getMessage();
        if (msg == null) {
            msg = e.toString();
        }
        Timber.e(msg);

        if (e.getCause() != null) {
            msg = e.getCause().getMessage();
            if (msg == null) {
                msg = e.getCause().toString();
            }
        }
        return msg;
    }

    /**
     * Creates a new form in the database, if none exists with the same absolute path. Returns
     * information with the URI, media path, and whether the form is new.
     *
     * @param formFile the form definition file
     * @param formInfo certain fields extracted from the parsed XML form, such as title and form ID
     * @return a {@link UriResult} object
     */
    private UriResult findExistingOrCreateNewUri(File formFile, Map<String, String> formInfo) {
        final Uri uri;
        final String formFilePath = formFile.getAbsolutePath();
        String mediaPath = FileUtils.constructMediaPath(formFilePath);
        final boolean isNew;

        FileUtils.checkMediaPath(new File(mediaPath));


        Form form = formsRepository.getByPath(formFile.getAbsolutePath());
        isNew = form == null;

        if (isNew) {
            uri = saveNewForm(formInfo, formFile, mediaPath);
        } else {
            uri = Uri.withAppendedPath(FormsColumns.CONTENT_URI, form.getId().toString());
            mediaPath = new StoragePathProvider().getAbsoluteFormFilePath(form.getFormMediaPath());
        }

        return new UriResult(uri, mediaPath, isNew);
    }

    private Uri saveNewForm(Map<String, String> formInfo, File formFile, String mediaPath) {
        Form form = new Form.Builder()
                .formFilePath(new StoragePathProvider().getFormDbPath(formFile.getAbsolutePath()))
                .formMediaPath(new StoragePathProvider().getFormDbPath(mediaPath))
                .displayName(formInfo.get(FileUtils.TITLE))
                .jrVersion(formInfo.get(FileUtils.VERSION))
                .jrFormId(formInfo.get(FileUtils.FORMID))
                .submissionUri(formInfo.get(FileUtils.SUBMISSIONURI))
                .base64RSAPublicKey(formInfo.get(FileUtils.BASE64_RSA_PUBLIC_KEY))
                .autoDelete(formInfo.get(FileUtils.AUTO_DELETE))
                .autoSend(formInfo.get(FileUtils.AUTO_SEND))
                .geometryXpath(formInfo.get(FileUtils.GEOMETRY_XPATH))
                .build();

        return formsRepository.save(form);
    }

    /**
     * Takes the formName and the URL and attempts to download the specified file. Returns a file
     * object representing the downloaded file.
     */
    FileResult downloadXform(String formName, String url, FormDownloaderListener stateListener) throws Exception {
        // clean up friendly form name...
        String rootName = FormNameUtils.formatFilenameFromFormName(formName);

        // proposed name of xml file...
        StoragePathProvider storagePathProvider = new StoragePathProvider();
        String path = storagePathProvider.getDirPath(StorageSubdirectory.FORMS) + File.separator + rootName + ".xml";
        int i = 2;
        File f = new File(path);
        while (f.exists()) {
            path = storagePathProvider.getDirPath(StorageSubdirectory.FORMS) + File.separator + rootName + "_" + i + ".xml";
            f = new File(path);
            i++;
        }

        InputStream file = formListApi.fetchForm(url);
        writeFile(f, stateListener, file);

        boolean isNew = true;

        // we've downloaded the file, and we may have renamed it
        // make sure it's not the same as a file we already have
        Form form = formsRepository.getByMd5Hash(FileUtils.getMd5Hash(f));
        if (form != null) {
            isNew = false;

            // delete the file we just downloaded, because it's a duplicate
            Timber.w("A duplicate file has been found, we need to remove the downloaded file "
                    + "and return the other one.");
            FileUtils.deleteAndReport(f);

            // set the file returned to the file we already had
            String existingPath = storagePathProvider.getAbsoluteFormFilePath(form.getFormFilePath());
            f = new File(existingPath);
            Timber.w("Will use %s", existingPath);
        }

        return new FileResult(f, isNew);
    }

    /**
     * Common routine to take a downloaded document save the contents in the file
     * 'file'. Shared by media file download and form file download.
     * <p>
     * SurveyCTO: The file is saved into a temp folder and is moved to the final place if everything
     * is okay, so that garbage is not left over on cancel.
     *
     */
    private void writeFile(File file, FormDownloaderListener stateListener, InputStream inputStream)
            throws IOException, TaskCancelledException, URISyntaxException, Exception {

        File tempFile = File.createTempFile(file.getName(), TEMP_DOWNLOAD_EXTENSION,
                new File(new StoragePathProvider().getDirPath(StorageSubdirectory.CACHE)));

        // WiFi network connections can be renegotiated during a large form download sequence.
        // This will cause intermittent download failures.  Silently retry once after each
        // failure.  Only if there are two consecutive failures do we abort.
        boolean success = false;
        int attemptCount = 0;
        final int MAX_ATTEMPT_COUNT = 2;
        while (!success && ++attemptCount <= MAX_ATTEMPT_COUNT) {
            if (stateListener != null && stateListener.isTaskCanceled()) {
                throw new TaskCancelledException(tempFile);
            }

            // write connection to file
            InputStream is = null;
            OutputStream os = null;

            try {
                is = inputStream;
                os = new FileOutputStream(tempFile);

                byte[] buf = new byte[4096];
                int len;
                while ((len = is.read(buf)) > 0 && (stateListener == null || !stateListener.isTaskCanceled())) {
                    os.write(buf, 0, len);
                }
                os.flush();
                success = true;

            } catch (Exception e) {
                Timber.e(e.toString());
                // silently retry unless this is the last attempt,
                // in which case we rethrow the exception.

                FileUtils.deleteAndReport(tempFile);

                if (attemptCount == MAX_ATTEMPT_COUNT) {
                    throw e;
                }
            } finally {
                if (os != null) {
                    try {
                        os.close();
                    } catch (Exception e) {
                        Timber.e(e);
                    }
                }
                if (is != null) {
                    try {
                        // ensure stream is consumed...
                        final long count = 1024L;
                        while (is.skip(count) == count) {
                            // skipping to the end of the http entity
                        }
                    } catch (Exception e) {
                        // no-op
                    }
                    try {
                        is.close();
                    } catch (Exception e) {
                        Timber.e(e);
                    }
                }
            }

            if (stateListener != null && stateListener.isTaskCanceled()) {
                FileUtils.deleteAndReport(tempFile);
                throw new TaskCancelledException(tempFile);
            }
        }

        Timber.d("Completed downloading of %s. It will be moved to the proper path...",
                tempFile.getAbsolutePath());

        FileUtils.deleteAndReport(file);

        String errorMessage = FileUtils.copyFile(tempFile, file);

        if (file.exists()) {
            Timber.w("Copied %s over %s", tempFile.getAbsolutePath(), file.getAbsolutePath());
            FileUtils.deleteAndReport(tempFile);
        } else {
            String msg = Collect.getInstance().getString(R.string.fs_file_copy_error,
                    tempFile.getAbsolutePath(), file.getAbsolutePath(), errorMessage);
            Timber.w(msg);
            throw new RuntimeException(msg);
        }
    }

    private static class UriResult {

        private final Uri uri;
        private final String mediaPath;
        private final boolean isNew;

        private UriResult(Uri uri, String mediaPath, boolean isNew) {
            this.uri = uri;
            this.mediaPath = mediaPath;
            this.isNew = isNew;
        }

        private Uri getUri() {
            return uri;
        }

        private String getMediaPath() {
            return mediaPath;
        }

        private boolean isNew() {
            return isNew;
        }
    }

    static class FileResult {

        private final File file;
        private final boolean isNew;

        FileResult(File file, boolean isNew) {
            this.file = file;
            this.isNew = isNew;
        }

        private File getFile() {
            return file;
        }

        private boolean isNew() {
            return isNew;
        }
    }

    String downloadManifestAndMediaFiles(String tempMediaPath, String finalMediaPath,
                                         ServerFormDetails fd, int count,
                                         int total, FormDownloaderListener stateListener) throws Exception {
        if (fd.getManifestUrl() == null) {
            return null;
        }

        if (stateListener != null) {
            stateListener.progressUpdate(Collect.getInstance().getString(R.string.fetching_manifest, fd.getFormName()),
                    String.valueOf(count), String.valueOf(total));
        }

        List<MediaFile> files = formListApi.fetchManifest(fd.getManifestUrl()).getMediaFiles();

        // OK we now have the full set of files to download...
        Timber.i("Downloading %d media files.", files.size());
        int mediaCount = 0;
        if (!files.isEmpty()) {
            File tempMediaDir = new File(tempMediaPath);
            File finalMediaDir = new File(finalMediaPath);

            FileUtils.checkMediaPath(tempMediaDir);
            FileUtils.checkMediaPath(finalMediaDir);

            for (MediaFile toDownload : files) {
                ++mediaCount;
                if (stateListener != null) {
                    stateListener.progressUpdate(
                            Collect.getInstance().getString(R.string.form_download_progress,
                                    fd.getFormName(),
                                    String.valueOf(mediaCount), String.valueOf(files.size())),
                            String.valueOf(count), String.valueOf(total));
                }

                //try {
                File finalMediaFile = new File(finalMediaDir, toDownload.getFilename());
                File tempMediaFile = new File(tempMediaDir, toDownload.getFilename());

                if (!finalMediaFile.exists()) {
                    InputStream mediaFile = formListApi.fetchMediaFile(toDownload.getDownloadUrl());
                    writeFile(tempMediaFile, stateListener, mediaFile);
                } else {
                    String currentFileHash = FileUtils.getMd5Hash(finalMediaFile);
                    String downloadFileHash = getMd5Hash(toDownload.getHash());

                    if (currentFileHash != null && downloadFileHash != null && !currentFileHash.contentEquals(downloadFileHash)) {
                        // if the hashes match, it's the same file
                        // otherwise delete our current one and replace it with the new one
                        FileUtils.deleteAndReport(finalMediaFile);
                        InputStream mediaFile = formListApi.fetchMediaFile(toDownload.getDownloadUrl());
                        writeFile(tempMediaFile, stateListener, mediaFile);
                    } else {
                        // exists, and the hash is the same
                        // no need to download it again
                        Timber.i("Skipping media file fetch -- file hashes identical: %s",
                                finalMediaFile.getAbsolutePath());
                    }
                }
                //  } catch (Exception e) {
                //  return e.getLocalizedMessage();
                //}
            }
        }
        return null;
    }

    public static String getMd5Hash(String hash) {
        return hash == null || hash.isEmpty() ? null : hash.substring(MD5_COLON_PREFIX.length());
    }
}
