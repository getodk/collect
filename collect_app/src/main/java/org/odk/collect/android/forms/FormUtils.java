package org.odk.collect.android.forms;

import android.content.ContentValues;
import android.database.Cursor;

import org.javarosa.core.reference.ReferenceManager;
import org.javarosa.core.reference.RootTranslator;
import org.odk.collect.android.logic.FileReferenceFactory;
import org.odk.collect.android.provider.FormsProviderAPI;
import org.odk.collect.android.storage.StoragePathProvider;
import org.odk.collect.android.utilities.FileUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static android.provider.BaseColumns._ID;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.AUTO_DELETE;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.AUTO_SEND;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.BASE64_RSA_PUBLIC_KEY;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.DATE;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.DELETED_DATE;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.DESCRIPTION;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.DISPLAY_NAME;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.FORM_FILE_PATH;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.FORM_MEDIA_PATH;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.GEOMETRY_XPATH;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.JRCACHE_FILE_PATH;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.JR_FORM_ID;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.JR_VERSION;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.LANGUAGE;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.MD5_HASH;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.SUBMISSION_URI;

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

    public static ContentValues getValuesFromForm(Form form, StoragePathProvider storagePathProvider) {
        ContentValues values = new ContentValues();
        values.put(_ID, form.getId());
        values.put(FormsProviderAPI.FormsColumns.DISPLAY_NAME, form.getDisplayName());
        values.put(FormsProviderAPI.FormsColumns.DESCRIPTION, form.getDescription());
        values.put(FormsProviderAPI.FormsColumns.JR_FORM_ID, form.getJrFormId());
        values.put(FormsProviderAPI.FormsColumns.JR_VERSION, form.getJrVersion());
        values.put(FormsProviderAPI.FormsColumns.FORM_FILE_PATH, storagePathProvider.getRelativeFormPath(form.getFormFilePath()));
        values.put(FormsProviderAPI.FormsColumns.SUBMISSION_URI, form.getSubmissionUri());
        values.put(FormsProviderAPI.FormsColumns.BASE64_RSA_PUBLIC_KEY, form.getBASE64RSAPublicKey());
        values.put(FormsProviderAPI.FormsColumns.MD5_HASH, form.getMD5Hash());
        values.put(FormsProviderAPI.FormsColumns.FORM_MEDIA_PATH, storagePathProvider.getRelativeFormPath(form.getFormMediaPath()));
        values.put(FormsProviderAPI.FormsColumns.LANGUAGE, form.getLanguage());
        values.put(FormsProviderAPI.FormsColumns.AUTO_SEND, form.getAutoSend());
        values.put(FormsProviderAPI.FormsColumns.AUTO_DELETE, form.getAutoDelete());
        values.put(FormsProviderAPI.FormsColumns.GEOMETRY_XPATH, form.getGeometryXpath());

        return values;
    }

    public static Form getFormFromValues(ContentValues values, StoragePathProvider storagePathProvider) {
        return new Form.Builder()
                .id(values.getAsLong(_ID))
                .displayName(values.getAsString(DISPLAY_NAME))
                .description(values.getAsString(DESCRIPTION))
                .jrFormId(values.getAsString(JR_FORM_ID))
                .jrVersion(values.getAsString(JR_VERSION))
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
        int descriptionColumnIndex = cursor.getColumnIndex(FormsProviderAPI.FormsColumns.DESCRIPTION);
        int jrFormIdColumnIndex = cursor.getColumnIndex(JR_FORM_ID);
        int jrVersionColumnIndex = cursor.getColumnIndex(JR_VERSION);
        int formFilePathColumnIndex = cursor.getColumnIndex(FORM_FILE_PATH);
        int submissionUriColumnIndex = cursor.getColumnIndex(SUBMISSION_URI);
        int base64RSAPublicKeyColumnIndex = cursor.getColumnIndex(BASE64_RSA_PUBLIC_KEY);
        int md5HashColumnIndex = cursor.getColumnIndex(FormsProviderAPI.FormsColumns.MD5_HASH);
        int dateColumnIndex = cursor.getColumnIndex(DATE);
        int jrCacheFilePathColumnIndex = cursor.getColumnIndex(FormsProviderAPI.FormsColumns.JRCACHE_FILE_PATH);
        int formMediaPathColumnIndex = cursor.getColumnIndex(FORM_MEDIA_PATH);
        int languageColumnIndex = cursor.getColumnIndex(FormsProviderAPI.FormsColumns.LANGUAGE);
        int autoSendColumnIndex = cursor.getColumnIndex(AUTO_SEND);
        int autoDeleteColumnIndex = cursor.getColumnIndex(AUTO_DELETE);
        int geometryXpathColumnIndex = cursor.getColumnIndex(GEOMETRY_XPATH);
        int deletedDateColumnIndex = cursor.getColumnIndex(DELETED_DATE);

        return new Form.Builder()
                .id(cursor.getLong(idColumnIndex))
                .displayName(cursor.getString(displayNameColumnIndex))
                .description(cursor.getString(descriptionColumnIndex))
                .jrFormId(cursor.getString(jrFormIdColumnIndex))
                .jrVersion(cursor.getString(jrVersionColumnIndex))
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
