package org.odk.collect.android.utilities;

import android.app.Application;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;

import androidx.annotation.Nullable;

import org.odk.collect.android.R;
import org.odk.collect.android.formmanagement.ServerFormsDetailsFetcher;
import org.odk.collect.android.forms.DatabaseFormRepository;
import org.odk.collect.android.forms.DatabaseMediaFileRepository;
import org.odk.collect.android.forms.FormRepository;
import org.odk.collect.android.forms.MediaFileRepository;
import org.odk.collect.android.formmanagement.ServerFormDetails;
import org.odk.collect.android.openrosa.OpenRosaXmlFetcher;
import org.odk.collect.android.openrosa.api.FormApiException;
import org.odk.collect.android.openrosa.api.OpenRosaFormListApi;
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
    private final OpenRosaXmlFetcher openRosaXMLFetcher;
    private final Application application;
    private final FormRepository formRepository;
    private final MediaFileRepository mediaFileRepository;

    public FormListDownloader(
            Application application,
            OpenRosaXmlFetcher openRosaXMLFetcher,
            WebCredentialsUtils webCredentialsUtils) {
        this.application = application;
        this.openRosaXMLFetcher = openRosaXMLFetcher;
        this.webCredentialsUtils = webCredentialsUtils;

        formRepository = new DatabaseFormRepository();
        mediaFileRepository = new DatabaseMediaFileRepository();
    }

    public HashMap<String, ServerFormDetails> downloadFormList(@Nullable String url, @Nullable String username,
                                                               @Nullable String password) {
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

        OpenRosaFormListApi formAPI = new OpenRosaFormListApi(openRosaXMLFetcher, downloadListUrl, downloadPath);
        // We populate this with available forms from the specified server.
        // <formname, details>
        HashMap<String, ServerFormDetails> formList = new HashMap<>();

        try {
            ServerFormsDetailsFetcher serverFormsDetailsFetcher = new ServerFormsDetailsFetcher(formRepository, mediaFileRepository, formAPI, new FormsDirDiskFormsSynchronizer());
            List<ServerFormDetails> serverFormDetailsList = serverFormsDetailsFetcher.fetchFormDetails();
            for (ServerFormDetails serverFormDetails : serverFormDetailsList) {
                formList.put(serverFormDetails.getFormId(), serverFormDetails);
            }
        } catch (FormApiException formApiException) {
            Timber.w(formApiException);

            switch (formApiException.getType()) {
                case AUTH_REQUIRED:
                    formList.put(DL_AUTH_REQUIRED, new ServerFormDetails(formApiException.getMessage()));
                    break;
                case PARSE_ERROR:
                    formList.put(DL_ERROR_MSG, new ServerFormDetails(application.getString(R.string.parse_openrosa_formlist_failed, formApiException.getMessage())));
                    break;
                case LEGACY_PARSE_ERROR:
                    formList.put(DL_ERROR_MSG, new ServerFormDetails(application.getString(R.string.parse_legacy_formlist_failed, formApiException.getMessage())));
                    break;
                default:
                    formList.put(DL_ERROR_MSG, new ServerFormDetails(formApiException.getMessage()));
            }
        }

        HashMap<String, ServerFormDetails> result = formList;
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
