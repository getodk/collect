package org.odk.collect.android.forms;

import android.content.ContentValues;
import android.database.Cursor;

import androidx.annotation.NonNull;

import org.javarosa.core.reference.ReferenceManager;
import org.javarosa.core.reference.RootTranslator;
import org.odk.collect.android.database.DatabaseFormColumns;
import org.odk.collect.android.logic.FileReferenceFactory;
import org.odk.collect.android.storage.StoragePathProvider;
import org.odk.collect.android.utilities.FileUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static android.provider.BaseColumns._ID;
import static org.odk.collect.android.database.DatabaseFormColumns.AUTO_DELETE;
import static org.odk.collect.android.database.DatabaseFormColumns.AUTO_SEND;
import static org.odk.collect.android.database.DatabaseFormColumns.BASE64_RSA_PUBLIC_KEY;
import static org.odk.collect.android.database.DatabaseFormColumns.DATE;
import static org.odk.collect.android.database.DatabaseFormColumns.DELETED_DATE;
import static org.odk.collect.android.database.DatabaseFormColumns.DESCRIPTION;
import static org.odk.collect.android.database.DatabaseFormColumns.DISPLAY_NAME;
import static org.odk.collect.android.database.DatabaseFormColumns.FORM_FILE_PATH;
import static org.odk.collect.android.database.DatabaseFormColumns.FORM_MEDIA_PATH;
import static org.odk.collect.android.database.DatabaseFormColumns.GEOMETRY_XPATH;
import static org.odk.collect.android.database.DatabaseFormColumns.JRCACHE_FILE_PATH;
import static org.odk.collect.android.database.DatabaseFormColumns.JR_FORM_ID;
import static org.odk.collect.android.database.DatabaseFormColumns.JR_VERSION;
import static org.odk.collect.android.database.DatabaseFormColumns.LANGUAGE;
import static org.odk.collect.android.database.DatabaseFormColumns.MD5_HASH;
import static org.odk.collect.android.database.DatabaseFormColumns.SUBMISSION_URI;

public class FormUtils {

    private FormUtils() {

    }

    public static List<File> getMediaFiles(Form form) {
        FileUtil fileUtil = new FileUtil();

        String formMediaPath = form.getFormMediaPath();
        return formMediaPath == null
                ? new ArrayList<>()
                : fileUtil.listFiles(fileUtil.getFileAtPath(formMediaPath));
    }

    /**
     * Configures the given reference manager to resolve jr:// URIs to a folder in the root ODK forms
     * directory with name matching the name of the directory represented by {@code formMediaDir}.
     * <p>
     * E.g. if /foo/bar/baz is passed in as {@code formMediaDir}, jr:// URIs will be resolved to
     * /odk/root/forms/baz.
     */
    public static void setupReferenceManagerForForm(ReferenceManager referenceManager, File formMediaDir) {
        // Clear mappings to the media dir for the previous form that was configured
        referenceManager.clearSession();

        // This should get moved to the Application Class
        if (referenceManager.getFactories().length == 0) {
            // Always build URIs against the ODK root, regardless of the absolute path of formMediaDir
            referenceManager.addReferenceFactory(new FileReferenceFactory(new StoragePathProvider().getOdkRootDirPath()));
        }

        addSessionRootTranslators(referenceManager,
                buildSessionRootTranslators(formMediaDir.getName(), enumerateHostStrings()));
    }

    public static String[] enumerateHostStrings() {
        return new String[]{"images", "image", "audio", "video", "file-csv", "file"};
    }

    public static List<RootTranslator> buildSessionRootTranslators(String formMediaDir, String[] hostStrings) {
        List<RootTranslator> rootTranslators = new ArrayList<>();
        // Set jr://... to point to /sdcard/odk/forms/formBasename-media/
        final String translatedPrefix = String.format("jr://file/forms/" + formMediaDir + "/");
        for (String t : hostStrings) {
            rootTranslators.add(new RootTranslator(String.format("jr://%s/", t), translatedPrefix));
        }
        return rootTranslators;
    }

    public static void addSessionRootTranslators(ReferenceManager referenceManager, List<RootTranslator> rootTranslators) {
        for (RootTranslator rootTranslator : rootTranslators) {
            referenceManager.addSessionRootTranslator(rootTranslator);
        }
    }

    public static ContentValues getValuesFromForm(@NonNull Form form, StoragePathProvider storagePathProvider) {
        ContentValues values = new ContentValues();
        values.put(_ID, form.getDbId());
        values.put(DatabaseFormColumns.DISPLAY_NAME, form.getDisplayName());
        values.put(DatabaseFormColumns.DESCRIPTION, form.getDescription());
        values.put(DatabaseFormColumns.JR_FORM_ID, form.getFormId());
        values.put(DatabaseFormColumns.JR_VERSION, form.getVersion());
        values.put(DatabaseFormColumns.FORM_FILE_PATH, storagePathProvider.getRelativeFormPath(form.getFormFilePath()));
        values.put(DatabaseFormColumns.SUBMISSION_URI, form.getSubmissionUri());
        values.put(DatabaseFormColumns.BASE64_RSA_PUBLIC_KEY, form.getBASE64RSAPublicKey());
        values.put(DatabaseFormColumns.MD5_HASH, form.getMD5Hash());
        values.put(DatabaseFormColumns.FORM_MEDIA_PATH, storagePathProvider.getRelativeFormPath(form.getFormMediaPath()));
        values.put(DatabaseFormColumns.LANGUAGE, form.getLanguage());
        values.put(DatabaseFormColumns.AUTO_SEND, form.getAutoSend());
        values.put(DatabaseFormColumns.AUTO_DELETE, form.getAutoDelete());
        values.put(DatabaseFormColumns.GEOMETRY_XPATH, form.getGeometryXpath());

        return values;
    }

    public static Form getFormFromValues(ContentValues values, StoragePathProvider storagePathProvider) {
        return new Form.Builder()
                .dbId(values.getAsLong(_ID))
                .displayName(values.getAsString(DISPLAY_NAME))
                .description(values.getAsString(DESCRIPTION))
                .formId(values.getAsString(JR_FORM_ID))
                .version(values.getAsString(JR_VERSION))
                .formFilePath(storagePathProvider.getAbsoluteFormFilePath(values.getAsString(FORM_FILE_PATH)))
                .submissionUri(values.getAsString(SUBMISSION_URI))
                .base64RSAPublicKey(values.getAsString(BASE64_RSA_PUBLIC_KEY))
                .md5Hash(values.getAsString(MD5_HASH))
                .date(values.getAsLong(DATE))
                .jrCacheFilePath(storagePathProvider.getAbsoluteCacheFilePath(values.getAsString(JRCACHE_FILE_PATH)))
                .formMediaPath(storagePathProvider.getAbsoluteFormFilePath(values.getAsString(FORM_MEDIA_PATH)))
                .language(values.getAsString(LANGUAGE))
                .autoSend(values.getAsString(AUTO_SEND))
                .autoDelete(values.getAsString(AUTO_DELETE))
                .geometryXpath(values.getAsString(GEOMETRY_XPATH))
                .deleted(values.getAsLong(DELETED_DATE) != null)
                .build();
    }

    public static Form getFormFromCurrentCursorPosition(Cursor cursor, StoragePathProvider storagePathProvider) {
        int idColumnIndex = cursor.getColumnIndex(_ID);
        int displayNameColumnIndex = cursor.getColumnIndex(DISPLAY_NAME);
        int descriptionColumnIndex = cursor.getColumnIndex(DatabaseFormColumns.DESCRIPTION);
        int jrFormIdColumnIndex = cursor.getColumnIndex(JR_FORM_ID);
        int jrVersionColumnIndex = cursor.getColumnIndex(JR_VERSION);
        int formFilePathColumnIndex = cursor.getColumnIndex(FORM_FILE_PATH);
        int submissionUriColumnIndex = cursor.getColumnIndex(SUBMISSION_URI);
        int base64RSAPublicKeyColumnIndex = cursor.getColumnIndex(BASE64_RSA_PUBLIC_KEY);
        int md5HashColumnIndex = cursor.getColumnIndex(DatabaseFormColumns.MD5_HASH);
        int dateColumnIndex = cursor.getColumnIndex(DATE);
        int jrCacheFilePathColumnIndex = cursor.getColumnIndex(DatabaseFormColumns.JRCACHE_FILE_PATH);
        int formMediaPathColumnIndex = cursor.getColumnIndex(FORM_MEDIA_PATH);
        int languageColumnIndex = cursor.getColumnIndex(DatabaseFormColumns.LANGUAGE);
        int autoSendColumnIndex = cursor.getColumnIndex(AUTO_SEND);
        int autoDeleteColumnIndex = cursor.getColumnIndex(AUTO_DELETE);
        int geometryXpathColumnIndex = cursor.getColumnIndex(GEOMETRY_XPATH);
        int deletedDateColumnIndex = cursor.getColumnIndex(DELETED_DATE);

        return new Form.Builder()
                .dbId(cursor.getLong(idColumnIndex))
                .displayName(cursor.getString(displayNameColumnIndex))
                .description(cursor.getString(descriptionColumnIndex))
                .formId(cursor.getString(jrFormIdColumnIndex))
                .version(cursor.getString(jrVersionColumnIndex))
                .formFilePath(storagePathProvider.getAbsoluteFormFilePath(cursor.getString(formFilePathColumnIndex)))
                .submissionUri(cursor.getString(submissionUriColumnIndex))
                .base64RSAPublicKey(cursor.getString(base64RSAPublicKeyColumnIndex))
                .md5Hash(cursor.getString(md5HashColumnIndex))
                .date(cursor.getLong(dateColumnIndex))
                .jrCacheFilePath(storagePathProvider.getAbsoluteCacheFilePath(cursor.getString(jrCacheFilePathColumnIndex)))
                .formMediaPath(storagePathProvider.getAbsoluteFormFilePath(cursor.getString(formMediaPathColumnIndex)))
                .language(cursor.getString(languageColumnIndex))
                .autoSend(cursor.getString(autoSendColumnIndex))
                .autoDelete(cursor.getString(autoDeleteColumnIndex))
                .geometryXpath(cursor.getString(geometryXpathColumnIndex))
                .deleted(!cursor.isNull(deletedDateColumnIndex))
                .build();
    }
}
