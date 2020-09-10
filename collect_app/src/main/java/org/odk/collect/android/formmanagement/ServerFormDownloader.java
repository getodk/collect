package org.odk.collect.android.formmanagement;

import android.net.Uri;

import org.javarosa.core.reference.ReferenceManager;
import org.javarosa.core.reference.RootTranslator;
import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.listeners.FormDownloaderListener;
import org.odk.collect.android.logic.FileReferenceFactory;
import org.odk.collect.android.provider.FormsProviderAPI;
import org.odk.collect.android.storage.StoragePathProvider;
import org.odk.collect.android.storage.StorageSubdirectory;
import org.odk.collect.android.utilities.FileUtils;
import org.odk.collect.android.utilities.FormNameUtils;
import org.odk.collect.android.utilities.Validator;
import org.odk.collect.forms.Form;
import org.odk.collect.forms.FormsRepository;
import org.odk.collect.server.FormApiException;
import org.odk.collect.server.FormListApi;
import org.odk.collect.server.MediaFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import timber.log.Timber;

import static org.odk.collect.android.utilities.FileUtils.LAST_SAVED_FILENAME;
import static org.odk.collect.android.utilities.FileUtils.STUB_XML;
import static org.odk.collect.android.utilities.FileUtils.write;

/**
 * Provides a sarcophagus for {@link MultiFormDownloader} so it
 * can eventually be disposed of.
 */
public class ServerFormDownloader implements FormDownloader {

    private final MultiFormDownloader multiFormDownloader;
    private final FormsRepository formsRepository;

    public ServerFormDownloader(FormListApi formListApi, FormsRepository formsRepository, StoragePathProvider storagePathProvider) {
        this.multiFormDownloader = new MultiFormDownloader(formsRepository, formListApi, storagePathProvider);
        this.formsRepository = formsRepository;
    }

    @Override
    public void downloadForm(ServerFormDetails form, @Nullable ProgressReporter progressReporter, @Nullable Supplier<Boolean> isCancelled) throws FormDownloadException, InterruptedException {
        Form formOnDevice = formsRepository.get(form.getFormId(), form.getFormVersion());
        if (formOnDevice != null && formOnDevice.isDeleted()) {
            formsRepository.restore(formOnDevice.getId());
        }

        FormDownloaderListener stateListener = new ProgressReporterAndSupplierStateListener(progressReporter, isCancelled);

        boolean result = multiFormDownloader.processOneForm(form, stateListener);
        if (!result) {
            throw new FormDownloadException();
        }
    }

    private static class ProgressReporterAndSupplierStateListener implements FormDownloaderListener {
        private final ProgressReporter progressReporter;
        private final Supplier<Boolean> isCancelled;

        ProgressReporterAndSupplierStateListener(ProgressReporter progressReporter, Supplier<Boolean> isCancelled) {
            this.progressReporter = progressReporter;
            this.isCancelled = isCancelled;
        }

        @Override
        public void progressUpdate(String currentFile, String progress, String total) {
            if (progressReporter != null) {
                progressReporter.onDownloadingMediaFile(Integer.parseInt(progress));
            }
        }

        @Override
        public boolean isTaskCanceled() {
            if (isCancelled != null) {
                return isCancelled.get();
            } else {
                return false;
            }
        }
    }

    @Deprecated
    private static class MultiFormDownloader {

        private static final String TEMP_DOWNLOAD_EXTENSION = ".tempDownload";

        private final FormListApi formListApi;
        private final StoragePathProvider storagePathProvider;
        private final FormsRepository formsRepository;

        @Deprecated
        MultiFormDownloader(FormsRepository formsRepository, FormListApi formListApi, StoragePathProvider storagePathProvider) {
            this.formsRepository = formsRepository;
            this.formListApi = formListApi;
            this.storagePathProvider = storagePathProvider;
        }

        @Deprecated
        public boolean processOneForm(ServerFormDetails fd, FormDownloaderListener stateListener) throws InterruptedException {
            boolean success = true;

            // use a temporary media path until everything is ok.
            String tempMediaPath = new File(storagePathProvider.getDirPath(StorageSubdirectory.CACHE),
                    String.valueOf(System.currentTimeMillis())).getAbsolutePath();
            final String finalMediaPath;
            FileResult fileResult = null;
            try {
                // get the xml file
                // if we've downloaded a duplicate, this gives us the file
                fileResult = downloadXform(fd.getFormName(), fd.getDownloadUrl(), stateListener);

                if (fd.getManifest() != null) {
                    finalMediaPath = FileUtils.constructMediaPath(
                            fileResult.getFile().getAbsolutePath());
                    String error = downloadManifestAndMediaFiles(tempMediaPath, finalMediaPath, fd,
                            stateListener, fd.getManifest().getMediaFiles());
                    if (error != null && !error.isEmpty()) {
                        success = false;
                    }
                } else {
                    Timber.i("No Manifest for: %s", fd.getFormName());
                }
            } catch (InterruptedException e) {
                Timber.i(e);
                cleanUp(fileResult, tempMediaPath);

                // do not download additional forms.
                throw e;
            } catch (FormApiException | IOException e) {
                return false;
            }

            if (stateListener != null && stateListener.isTaskCanceled()) {
                cleanUp(fileResult, tempMediaPath);
                fileResult = null;
            }

            if (fileResult == null) {
                return false;
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
                    return false;
                }
            }

            boolean installed = false;

            if ((stateListener == null || !stateListener.isTaskCanceled()) && success) {
                if (!fileResult.isNew || isSubmissionOk(parsedFields)) {
                    installed = installEverything(tempMediaPath, fileResult, parsedFields);
                } else {
                    success = false;
                }
            }
            if (!installed) {
                success = false;
                cleanUp(fileResult, tempMediaPath);
            }

            return success;
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

        private void cleanUp(FileResult fileResult, String tempMediaPath) {
            if (fileResult == null) {
                Timber.w("The user cancelled (or an exception happened) the download of a form at the very beginning.");
            } else {
                String md5Hash = FileUtils.getMd5Hash(fileResult.file);
                if (md5Hash != null) {
                    formsRepository.deleteFormsByMd5Hash(md5Hash);
                }
                FileUtils.deleteAndReport(fileResult.getFile());
            }

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

            FileUtils.checkMediaPath(new File(mediaPath));
            Form form = formsRepository.getByPath(formFile.getAbsolutePath());

            if (form == null) {
                uri = saveNewForm(formInfo, formFile, mediaPath);
                return new UriResult(uri, mediaPath, true);
            } else {
                uri = Uri.withAppendedPath(FormsProviderAPI.FormsColumns.CONTENT_URI, form.getId().toString());
                mediaPath = storagePathProvider.getAbsoluteFormFilePath(form.getFormMediaPath());
                return new UriResult(uri, mediaPath, false);
            }
        }

        private Uri saveNewForm(Map<String, String> formInfo, File formFile, String mediaPath) {
            Form form = new Form.Builder()
                    .formFilePath(storagePathProvider.getFormDbPath(formFile.getAbsolutePath()))
                    .formMediaPath(storagePathProvider.getFormDbPath(mediaPath))
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
        FileResult downloadXform(String formName, String url, FormDownloaderListener stateListener) throws FormApiException, IOException, InterruptedException {
            // clean up friendly form name...
            String rootName = FormNameUtils.formatFilenameFromFormName(formName);

            // proposed name of xml file...
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
                Timber.w("A duplicate file has been found, we need to remove the downloaded file and return the other one.");
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
         */
        private void writeFile(File file, FormDownloaderListener stateListener, InputStream inputStream)
                throws IOException, InterruptedException {

            File tempFile = File.createTempFile(
                    file.getName(),
                    TEMP_DOWNLOAD_EXTENSION,
                    new File(storagePathProvider.getDirPath(StorageSubdirectory.CACHE))
            );

            // WiFi network connections can be renegotiated during a large form download sequence.
            // This will cause intermittent download failures.  Silently retry once after each
            // failure.  Only if there are two consecutive failures do we abort.
            boolean success = false;
            int attemptCount = 0;
            final int MAX_ATTEMPT_COUNT = 2;
            while (!success && ++attemptCount <= MAX_ATTEMPT_COUNT) {
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
                    throw new InterruptedException();
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
                                             ServerFormDetails fd,
                                             FormDownloaderListener stateListener, List<MediaFile> files) throws FormApiException, IOException, InterruptedException {
            if (fd.getManifestUrl() == null) {
                return null;
            }

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
                                "",
                                String.valueOf(mediaCount),
                                "");
                    }

                    //try {
                    File finalMediaFile = new File(finalMediaDir, toDownload.getFilename());
                    File tempMediaFile = new File(tempMediaDir, toDownload.getFilename());

                    if (!finalMediaFile.exists()) {
                        InputStream mediaFile = formListApi.fetchMediaFile(toDownload.getDownloadUrl());
                        writeFile(tempMediaFile, stateListener, mediaFile);
                    } else {
                        String currentFileHash = FileUtils.getMd5Hash(finalMediaFile);
                        String downloadFileHash = getMd5HashWithoutPrefix(toDownload.getHash());

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
    }

    public static String getMd5HashWithoutPrefix(String hash) {
        return hash == null || hash.isEmpty() ? null : hash.substring("md5:".length());
    }
}
