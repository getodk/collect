package org.odk.collect.android.utilities;

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

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import org.javarosa.core.reference.ReferenceManager;
import org.javarosa.core.reference.RootTranslator;
import org.javarosa.xform.parse.XFormParser;
import org.kxml2.kdom.Element;
import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.dao.FormsDao;
import org.odk.collect.android.listeners.FormDownloaderListener;
import org.odk.collect.android.logic.FileReferenceFactory;
import org.odk.collect.android.logic.FormDetails;
import org.odk.collect.android.logic.MediaFile;
import org.odk.collect.android.logic.PropertyManager;
import org.odk.collect.android.openrosa.OpenRosaAPIClient;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import timber.log.Timber;

import static org.odk.collect.android.utilities.FileUtils.LAST_SAVED_FILENAME;
import static org.odk.collect.android.utilities.FileUtils.STUB_XML;
import static org.odk.collect.android.utilities.FileUtils.write;

public class FormDownloader {

    private static final String MD5_COLON_PREFIX = "md5:";
    private static final String TEMP_DOWNLOAD_EXTENSION = ".tempDownload";

    private FormDownloaderListener stateListener;

    private FormsDao formsDao;

    @Inject
    OpenRosaAPIClient openRosaAPIClient;

    /**
     * Common routine to download a document from the downloadUrl and save the contents in the file
     * 'file'. Shared by media file download and form file download.
     * <p>
     * SurveyCTO: The file is saved into a temp folder and is moved to the final place if everything
     * is okay, so that garbage is not left over on cancel.
     *
     * @param file        the final file
     * @param downloadUrl the url to get the contents from.
     */
    public void downloadFile(File file, String downloadUrl, boolean credentials)     // smap made public, flag to include credentials
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
            Timber.i("Started downloading to %s from %s", tempFile.getAbsolutePath(), downloadUrl);

            // write connection to file
            InputStream is = null;
            OutputStream os = null;

            try {
                is = openRosaAPIClient.getFile(downloadUrl, null, credentials);    // smap add credentials
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

}

