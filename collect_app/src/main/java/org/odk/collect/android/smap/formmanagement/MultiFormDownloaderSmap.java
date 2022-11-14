
package org.odk.collect.android.smap.formmanagement;

import android.net.Uri;

import org.javarosa.core.reference.ReferenceManager;
import org.javarosa.core.reference.RootTranslator;
import org.kxml2.kdom.Element;
import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.database.DatabaseFormsRepository;
import org.odk.collect.android.formmanagement.ServerFormDetails;
import org.odk.collect.android.forms.Form;
import org.odk.collect.android.forms.FormsRepository;
import org.odk.collect.android.forms.MediaFile;
import org.odk.collect.android.listeners.FormDownloaderListener;
import org.odk.collect.android.logic.FileReferenceFactory;
import org.odk.collect.android.logic.PropertyManager;
import org.odk.collect.android.openrosa.OpenRosaXmlFetcher;
import org.odk.collect.android.provider.FormsProviderAPI.FormsColumns;
import org.odk.collect.android.smap.database.DatabaseFormsRepositorySmap;
import org.odk.collect.android.smap.forms.FormsRepositorySmap;
import org.odk.collect.android.smap.openrosa.api.FormListApiSmap;
import org.odk.collect.android.smap.openrosa.api.OpenRosaFormListApiSmap;
import org.odk.collect.android.storage.StoragePathProvider;
import org.odk.collect.android.storage.StorageSubdirectory;
import org.odk.collect.android.utilities.FileUtils;
import org.odk.collect.android.utilities.FormNameUtils;
import org.odk.collect.android.utilities.STFileUtils;
import org.odk.collect.android.utilities.Utilities;
import org.odk.collect.android.utilities.Validator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

import static org.odk.collect.android.utilities.FileUtils.LAST_SAVED_FILENAME;
import static org.odk.collect.android.utilities.FileUtils.STUB_XML;
import static org.odk.collect.android.utilities.FileUtils.deleteOldFile;
import static org.odk.collect.android.utilities.FileUtils.write;
import static org.odk.collect.utilities.PathUtils.getAbsoluteFilePath;

public class MultiFormDownloaderSmap {

    private static final String MD5_COLON_PREFIX = "md5:";
    private static final String TEMP_DOWNLOAD_EXTENSION = ".tempDownload";

    private final FormListApiSmap formListApi;
    private final FormsRepositorySmap formsRepository;

    public MultiFormDownloaderSmap(OpenRosaXmlFetcher openRosaXmlFetcher) {
        this.formsRepository = new DatabaseFormsRepositorySmap();
        formListApi = new OpenRosaFormListApiSmap(openRosaXmlFetcher);
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

    public HashMap<ServerFormDetailsSmap, String> downloadForms(List<ServerFormDetailsSmap> toDownload, FormDownloaderListener stateListener) {
        int total = toDownload.size();
        int count = 1;

        final HashMap<ServerFormDetailsSmap, String> result = new HashMap<>();

        for (ServerFormDetailsSmap fd : toDownload) {
            boolean downloaded = false;
            try {
                downloaded = processOneForm(total, count++, fd, stateListener);
                if (downloaded) {
                    result.put(fd, Collect.getInstance().getString(R.string.success));
                }
            } catch (TaskCancelledException cd) {
                break;
            } catch(Exception e) {
                Timber.e(e);
                result.put(fd, Collect.getInstance().getString(R.string.failure) + ": " + e.getMessage());
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
    private boolean processOneForm(int total, int count, ServerFormDetailsSmap fd, FormDownloaderListener stateListener) throws Exception {

        //if (stateListener != null) {
        //    stateListener.progressUpdate(fd.getFormName(), String.valueOf(count), String.valueOf(total));
        //}

        if (stateListener != null && stateListener.isTaskCancelled()) {
            throw new TaskCancelledException();
        }

        String tempMediaPath = new File(new StoragePathProvider().getDirPath(StorageSubdirectory.CACHE),
                String.valueOf(System.currentTimeMillis())).getAbsolutePath();
        String orgTempMediaPath = new File(tempMediaPath + "_org").getAbsolutePath();      // smap
        String orgMediaPath = Utilities.getOrgMediaPath();  ;          // smap
        String finalMediaPath = null;
        FileResult fileResult = null;

        try {
            String deviceId = new PropertyManager(Collect.getInstance().getApplicationContext())
                    .getSingularProperty(PropertyManager.PROPMGR_DEVICE_ID);        // smap

            // get the xml file - either download or use the existing one
            fileResult = downloadXform(fd.getProject(), fd.getFormName(), fd.getDownloadUrl() + "&deviceID=" +
                            URLEncoder.encode(deviceId != null ? deviceId : "", "UTF-8"),
                    stateListener,
                    fd.isFormDownloaded(),
                    fd.getFormPath());

            if (fileResult == null || !fileResult.file.exists()) {
                throw new Exception("Downloaded xml file does not exist");
            } else {

                // Add a stub last-saved instance to the tmp media directory so it will be resolved
                // when parsing a form definition with last-saved reference
                File tmpLastSaved = new File(tempMediaPath, LAST_SAVED_FILENAME);
                write(tmpLastSaved, STUB_XML.getBytes(Charset.forName("UTF-8")));
                ReferenceManager.instance().reset();
                ReferenceManager.instance().addReferenceFactory(new FileReferenceFactory(tempMediaPath));
                ReferenceManager.instance().addSessionRootTranslator(new RootTranslator("jr://file-csv/", "jr://file/"));

                Map<String, String> parsedFields = FileUtils.getMetadataFromFormDefinition(fileResult.file);

                ReferenceManager.instance().reset();
                FileUtils.deleteAndReport(tmpLastSaved);

                /*
                 * Store result in database
                 */
                UriResult uriResult = findExistingOrCreateNewUri(fileResult.file, parsedFields,
                        STFileUtils.getSource(fd.getDownloadUrl()),
                        fd.getTasksOnly(),
                        fd.getReadOnly(),
                        fd.getSearchLocalData(),
                        fd.getProject());  // smap add source, tasks_only, searchLocalData,project
            }

            /*
             * Get Manifest files
             */
            if (fd.getManifestUrl() != null || fd.getMediaFiles() != null) {
                String mediaPath = fd.getFormMediaPath();
                if(mediaPath == null) {
                    // A newly downloaded form will have a null media path
                    mediaPath = FileUtils.constructMediaPath(fileResult.getFile().getAbsolutePath());
                }

                String error = downloadManifestAndMediaFiles(tempMediaPath, mediaPath, fd,
                        count, total, stateListener, orgTempMediaPath, orgMediaPath);                              // smap added org paths
                if (error != null && !error.isEmpty()) {
                    throw new Exception("Error: " + error);
                }
            }

            cleanUp(null, null, null, tempMediaPath, orgTempMediaPath);     // clear temp directories only

        } catch (TaskCancelledException e) {
            Timber.i(e);
            ReferenceManager.instance().reset();    // smap ensure reference manager reset after error
            cleanUp(fileResult, null, finalMediaPath, tempMediaPath, orgTempMediaPath);             // clear all directories
            throw e;        // do not download additional forms.
        }

        if (stateListener != null && stateListener.isTaskCancelled()) {
            cleanUp(fileResult, null, finalMediaPath, tempMediaPath, orgTempMediaPath);     // smap
        }


        return !fd.isFormDownloaded();
    }

    private void cleanUp(FileResult fileResult, File fileOnCancel, String mediaPath, String tempMediaPath, String orgTempMediaPath) {     // smap add org
        // Delete the XML File
        if (fileResult != null && fileResult.file.exists()) {
            String md5Hash = FileUtils.getMd5Hash(fileResult.file);
            if (md5Hash != null) {
                formsRepository.deleteByMd5Hash(md5Hash);
            }
        }

        FileUtils.deleteAndReport(fileOnCancel);

        // Delete the media folder
        if (mediaPath != null) {
            FileUtils.purgeMediaPath(tempMediaPath);
        }

        // Delete the temporart folders
        if (tempMediaPath != null) {
            FileUtils.purgeMediaPath(tempMediaPath);
        }
        if (orgTempMediaPath != null) {     // smap
            FileUtils.purgeMediaPath(orgTempMediaPath);
        }
    }

    /**
     * Creates a new form in the database, if none exists with the same absolute path. Returns
     * information with the URI, media path, and whether the form is new.
     *
     * @param formFile the form definition file
     * @param formInfo certain fields extracted from the parsed XML form, such as title and form ID
     * @return a {@link UriResult} object
     */
    private UriResult findExistingOrCreateNewUri(File formFile, Map<String, String> formInfo,
                                                 String source, boolean tasks_only, boolean read_only,
                                                 boolean searchLocalData, String project) {   // smap add source, tasks_only, project
        final Uri uri;
        final String formFilePath = formFile.getAbsolutePath();
        String mediaPath = FileUtils.constructMediaPath(formFilePath);

        FileUtils.checkMediaPath(new File(mediaPath));


        Form form = formsRepository.getOneByPath(formFile.getAbsolutePath());

        if (form == null) {
            uri = saveNewForm(formInfo, formFile, mediaPath, tasks_only, read_only, searchLocalData, source, project);       // smap add tasks_only and source
            return new UriResult(uri, mediaPath, true);
        } else {
            uri = Uri.withAppendedPath(FormsColumns.CONTENT_URI, form.getId().toString());
            mediaPath = new StoragePathProvider().getAbsoluteFormFilePath(form.getFormMediaPath());

            if (form.isDeleted()) {
                formsRepository.restore(form.getId());
            }

            return new UriResult(uri, mediaPath, false);
        }
    }

    private Uri saveNewForm(Map<String, String> formInfo, File formFile, String mediaPath,
                            boolean tasks_only, boolean read_only,
                            boolean searchLocalData, String source, String project) {    // smap add tasks_only, searchLocalData, source project
        Form form = new Form.Builder()
                .formFilePath(formFile.getAbsolutePath())
                .formMediaPath(mediaPath)
                .displayName(formInfo.get(FileUtils.TITLE))
                .jrVersion(formInfo.get(FileUtils.VERSION))
                .jrFormId(formInfo.get(FileUtils.FORMID))
                .project(project)      // smap
                .tasksOnly(tasks_only ? "yes" : "no")   // smap
                .readOnly(read_only ? "yes" : "no")   // smap
                .searchLocalData(searchLocalData ? "yes" : "no")   // smap
                .source(source)       // smap
                .submissionUri(formInfo.get(FileUtils.SUBMISSIONURI))
                .base64RSAPublicKey(formInfo.get(FileUtils.BASE64_RSA_PUBLIC_KEY))
                .autoDelete(formInfo.get(FileUtils.AUTO_DELETE))
                .autoSend(formInfo.get(FileUtils.AUTO_SEND))
                //.geometryXpath(formInfo.get(FileUtils.GEOMETRY_XPATH))     // smap
                .build();

        return formsRepository.save(form);
    }

    /**
     * Takes the formName and the URL and attempts to download the specified file. Returns a file
     * object representing the downloaded file.
     */
    FileResult downloadXform(String project, String formName, String url, FormDownloaderListener stateListener, boolean isFormDownloaded, String formPath) throws Exception {  // smap add project, download and formPath
        // clean up friendly form name...
        String rootName = FormNameUtils.formatFilenameFromFormName(project, formName);  // smap add project

        File f;
        boolean isNew = false;

        StoragePathProvider storagePathProvider = new StoragePathProvider();
        if(!isFormDownloaded || formPath == null) {
            // Download
            String path = storagePathProvider.getDirPath(StorageSubdirectory.FORMS) + File.separator + rootName + ".xml";
            f = new File(path);

            InputStream file = formListApi.fetchForm(url, true);    // add credentials flag
            writeFile(f, stateListener, file);

            isNew = true;       // smap now declared outside

        } else {
            // return the existing file
            f = new File(getAbsoluteFilePath(storagePathProvider.getDirPath(StorageSubdirectory.FORMS), formPath));
        }
        return new FileResult(f, isNew);    // smap

    }

    /**
     * Common routine to take a downloaded document save the contents in the file
     * 'file'. Shared by media file download and form file download.
     * <p>
     * SurveyCTO: The file is saved into a temp folder and is moved to the final place if everything
     * is okay, so that garbage is not left over on cancel.
     *
     */
    public void writeFile(File file, FormDownloaderListener stateListener, InputStream inputStream)   // smap made public
            throws IOException, TaskCancelledException, URISyntaxException, Exception {

        File tempFile = File.createTempFile(file.getName(), TEMP_DOWNLOAD_EXTENSION,
                new File(new StoragePathProvider().getDirPath(StorageSubdirectory.CACHE)));

        OutputStream os = null;

        try {
                if (stateListener != null && stateListener.isTaskCancelled()) {
                    throw new TaskCancelledException(tempFile);
                }

                os = new FileOutputStream(tempFile);

                byte[] buf = new byte[4096];
                int len;
                while ((len = inputStream.read(buf)) > 0 && (stateListener == null || !stateListener.isTaskCancelled())) {
                    os.write(buf, 0, len);
                }
                os.flush();

        } catch (Exception e) {
            Timber.e(e.toString());

            FileUtils.deleteAndReport(tempFile);

        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (Exception e) {
                    Timber.e(e);
                }
            }
            if (inputStream != null) {
                try {
                    // ensure stream is consumed...
                    final long count = 1024L;
                    while (inputStream.skip(count) == count) {
                        // skipping to the end of the http entity
                    }
                } catch (Exception e) {
                    // no-op
                }
                try {
                    inputStream.close();
                } catch (Exception e) {
                    Timber.e(e);
                }
            }
        }

        if (stateListener != null && stateListener.isTaskCancelled()) {
            FileUtils.deleteAndReport(tempFile);
            throw new TaskCancelledException(tempFile);
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

    public static class FileResult {   // smap make public

        private final File file;
        private final boolean isNew;

        public FileResult(File file, boolean isNew) {   // smap make public
            this.file = file;
            this.isNew = isNew;
        }

        public File getFile() {    // smap make public
            return file;
        }

        public boolean isNew() {    // smap make public
            return isNew;
        }
    }

    String downloadManifestAndMediaFiles(String tempMediaPath, String finalMediaPath,
                                         ServerFormDetailsSmap fd, int count,
                                         int total, FormDownloaderListener stateListener,
                                         String orgTempMediaPath,   // smap
                                         String orgMediaPath) throws Exception {        // smap

        List<MediaFile> files = fd.getMediaFiles();
        if(files == null && fd.getManifestUrl() != null) {  // Old server - request files from the server
            files = formListApi.fetchManifest(fd.getManifestUrl()).getMediaFiles();
        }

        StringBuffer downloadMsg = new StringBuffer("");      //smap

        // OK we now have the full set of files to download...
        Timber.i("Downloading %d media files.", files.size());
        int mediaCount = 0;
        if (files != null && !files.isEmpty()) {
            File tempMediaDir = new File(tempMediaPath);
            File finalMediaDir = new File(finalMediaPath);
            File orgTempMediaDir = new File(orgTempMediaPath);  // smap temp organisational media
            File orgMediaDir = new File (orgMediaPath);         // smap final organisational media

            FileUtils.checkMediaPath(tempMediaDir);
            FileUtils.checkMediaPath(finalMediaDir);
            FileUtils.checkMediaPath(orgTempMediaDir);          // smap
            FileUtils.checkMediaPath(orgMediaDir);              // smap

            for (MediaFile toDownload : files) {

                File finalMediaFile = null;
                File tempMediaFile = null;
                if(toDownload.getDownloadUrl().endsWith("organisation")) {
                    finalMediaFile = new File(orgMediaDir, toDownload.getFilename());
                    tempMediaFile = new File(orgTempMediaDir, toDownload.getFilename());
                } else {
                    finalMediaFile = new File(finalMediaDir, toDownload.getFilename());
                    tempMediaFile = new File(tempMediaDir, toDownload.getFilename());
                }

                /*
                 * Test to see if we need to re-download this file
                 */
                boolean needToDownload;
                if (!finalMediaFile.exists()) {
                    needToDownload = true;
                } else {
                    String currentFileHash = FileUtils.getMd5Hash(finalMediaFile);
                    String downloadFileHash = getMd5Hash(toDownload.getHash());

                    if (currentFileHash != null && downloadFileHash != null && !currentFileHash.contentEquals(downloadFileHash)) {
                        needToDownload = true;
                    } else {
                        needToDownload = false;

                        // exists, and the hash is the same no need to download it again
                        Timber.i("Skipping media file fetch -- file hashes identical: %s",
                                finalMediaFile.getAbsolutePath());
                    }
                }

                /*
                 * Download the media
                 */
                if(needToDownload) {

                    // Report progress
                    ++mediaCount;
                    if (stateListener != null) {
                        stateListener.progressUpdate(
                                Collect.getInstance().getString(R.string.form_download_progress,
                                        fd.getFormName(),
                                        String.valueOf(mediaCount), String.valueOf(files.size())),
                                String.valueOf(count), String.valueOf(total));
                    }

                    // Delete existing
                    if (finalMediaFile.exists()) {
                        FileUtils.deleteAndReport(finalMediaFile);
                    }

                    // Download
                    InputStream mediaFile = formListApi.fetchMediaFile(toDownload.getDownloadUrl(), true);  // smap add credentials file
                    writeFile(tempMediaFile, stateListener, mediaFile);

                    deleteOldFile(tempMediaFile.getName(), finalMediaDir);
                    if (toDownload.getDownloadUrl().endsWith("organisation")) {
                        org.apache.commons.io.FileUtils.copyFileToDirectory(tempMediaFile, finalMediaDir, false);
                        org.apache.commons.io.FileUtils.moveFileToDirectory(tempMediaFile, orgMediaDir, true);  // Save copy in org directory
                    } else {
                        org.apache.commons.io.FileUtils.moveFileToDirectory(tempMediaFile, finalMediaDir, true);
                    }
                } else if (toDownload.getDownloadUrl().endsWith("organisation")) {
                    // Get latest copy of file in organsiation shared directory
                    org.apache.commons.io.FileUtils.copyFileToDirectory(finalMediaFile, finalMediaDir, false);
                }
            }
        }
        if(downloadMsg.length() > 0) {      // smap
            return downloadMsg.toString();
        } else {
            return null;
        }
    }

    public static String getMd5Hash(String hash) {
        return hash == null || hash.isEmpty() ? null : hash.substring(MD5_COLON_PREFIX.length());
    }

}
