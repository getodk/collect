package org.odk.collect.android.utilities;

import android.app.Application;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;

import androidx.annotation.Nullable;

import org.odk.collect.android.R;
import org.odk.collect.android.dao.FormsDaoFormRepository;
import org.odk.collect.android.dao.FormsDaoMediaFileRepository;
import org.odk.collect.android.formmanagement.ServerFormsDetailsFetcher;
import org.odk.collect.android.forms.FormRepository;
import org.odk.collect.android.forms.MediaFileRepository;
import org.odk.collect.android.logic.FormDetails;
import org.odk.collect.android.openrosa.OpenRosaXMLFetcher;
import org.odk.collect.android.openrosa.api.FormAPIError;
import org.odk.collect.android.openrosa.api.OpenRosaFormAPI;
import org.odk.collect.android.preferences.GeneralKeys;

import java.util.HashMap;
import java.util.List;

import timber.log.Timber;

/**
 * @deprecated Use {@link ServerFormsDetailsFetcher instead}
 */
@Deprecated
public class FormListDownloader {

    // used to store error message if one occurs
    public static final String DL_ERROR_MSG = "dlerrormessage";
    public static final String DL_AUTH_REQUIRED = "dlauthrequired";

    private final WebCredentialsUtils webCredentialsUtils;
    private final OpenRosaXMLFetcher openRosaXMLFetcher;
    private final Application application;
    private final FormRepository formRepository;
    private final MediaFileRepository mediaFileRepository;

    public FormListDownloader(
            Application application,
            OpenRosaXMLFetcher openRosaXMLFetcher,
            WebCredentialsUtils webCredentialsUtils) {
        this.application = application;
        this.openRosaXMLFetcher = openRosaXMLFetcher;
        this.webCredentialsUtils = webCredentialsUtils;

        formRepository = new FormsDaoFormRepository();
        mediaFileRepository = new FormsDaoMediaFileRepository();
    }

    public HashMap<String, FormDetails> downloadFormList(@Nullable String url, @Nullable String username,
                                                         @Nullable String password, boolean alwaysCheckMediaFiles) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(
                application);

        String downloadListUrl = url != null ? url :
                settings.getString(GeneralKeys.KEY_SERVER_URL,
                        application.getString(R.string.default_server_url));

        while (downloadListUrl.endsWith("/")) {
            downloadListUrl = downloadListUrl.substring(0, downloadListUrl.length() - 1);
        }

        String formListPath = application.getString(R.string.default_odk_formlist);
        String downloadPath = (url != null) ? formListPath : settings.getString(GeneralKeys.KEY_FORMLIST_URL, formListPath);

        if (url != null) {
            String host = Uri.parse(url).getHost();

            if (host != null) {
                if (username != null && password != null) {
                    webCredentialsUtils.saveCredentials(url, username, password);
                } else {
                    webCredentialsUtils.clearCredentials(url);
                }
            }
        }

        OpenRosaFormAPI formAPI = new OpenRosaFormAPI(openRosaXMLFetcher, downloadListUrl, downloadPath);
        // We populate this with available forms from the specified server.
        // <formname, details>
        HashMap<String, FormDetails> formList = new HashMap<>();

        try {
            ServerFormsDetailsFetcher serverFormsDetailsFetcher = new ServerFormsDetailsFetcher(formRepository, mediaFileRepository, formAPI);
            List<FormDetails> formDetailsList = serverFormsDetailsFetcher.fetchFormDetails(alwaysCheckMediaFiles);
            for (FormDetails formDetails : formDetailsList) {
                formList.put(formDetails.getFormId(), formDetails);
            }
        } catch (FormAPIError formAPIError) {
            Timber.e(formAPIError);

            switch (formAPIError.getType()) {
                case AUTH_REQUIRED:
                    formList.put(DL_AUTH_REQUIRED, new FormDetails(formAPIError.getMessage()));
                    break;
                case PARSE_ERROR:
                    formList.put(DL_ERROR_MSG, new FormDetails(application.getString(R.string.parse_openrosa_formlist_failed, formAPIError.getMessage())));
                    break;
                case LEGACY_PARSE_ERROR:
                    formList.put(DL_ERROR_MSG, new FormDetails(application.getString(R.string.parse_legacy_formlist_failed, formAPIError.getMessage())));
                    break;
                default:
                    formList.put(DL_ERROR_MSG, new FormDetails(formAPIError.getMessage()));
            }
        }

        HashMap<String, FormDetails> result = formList;
        clearTemporaryCredentials(url);
        return result;
    }

    private void clearTemporaryCredentials(@Nullable String url) {
        if (url != null) {
            String host = Uri.parse(url).getHost();

            if (host != null) {
                webCredentialsUtils.clearCredentials(url);
            }
        }
    }
}
