package org.odk.collect.android.openrosa;

import org.javarosa.xform.parse.XFormParser;
import org.jetbrains.annotations.NotNull;
import org.kxml2.kdom.Element;
import org.odk.collect.analytics.Analytics;
import org.odk.collect.android.utilities.DocumentFetchResult;
import org.odk.collect.android.utilities.WebCredentialsUtils;
import org.odk.collect.forms.FormListItem;
import org.odk.collect.forms.FormSource;
import org.odk.collect.forms.FormSourceException;
import org.odk.collect.forms.ManifestFile;
import org.odk.collect.forms.MediaFile;
import org.odk.collect.shared.Md5;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import javax.net.ssl.SSLException;

import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.HttpURLConnection.HTTP_UNAUTHORIZED;
import static org.odk.collect.android.analytics.AnalyticsEvents.LEGACY_FORM_LIST;

public class OpenRosaFormSource implements FormSource {

    private final OpenRosaXmlFetcher openRosaXMLFetcher;
    private final OpenRosaResponseParser openRosaResponseParser;
    private final WebCredentialsUtils webCredentialsUtils;

    private String serverURL;
    private final String formListPath;

    private final Analytics analytics;

    public OpenRosaFormSource(String serverURL, String formListPath, OpenRosaHttpInterface openRosaHttpInterface, WebCredentialsUtils webCredentialsUtils, Analytics analytics, OpenRosaResponseParser openRosaResponseParser) {
        this.openRosaResponseParser = openRosaResponseParser;
        this.webCredentialsUtils = webCredentialsUtils;
        this.openRosaXMLFetcher = new OpenRosaXmlFetcher(openRosaHttpInterface, this.webCredentialsUtils);
        this.serverURL = serverURL;
        this.formListPath = formListPath;

        this.analytics = analytics;
    }

    @Override
    public List<FormListItem> fetchFormList() throws FormSourceException {
        DocumentFetchResult result = mapException(() -> openRosaXMLFetcher.getXML(getFormListURL()));

        if (result.errorMessage != null) {
            if (result.responseCode == HTTP_UNAUTHORIZED) {
                throw new FormSourceException.AuthRequired();
            } else if (result.responseCode == HTTP_NOT_FOUND) {
                throw new FormSourceException.Unreachable(serverURL);
            } else {
                throw new FormSourceException.ServerError(result.responseCode, serverURL);
            }
        }

        if (result.isOpenRosaResponse) {
            List<FormListItem> formList = openRosaResponseParser.parseFormList(result.doc);

            if (formList != null) {
                return formList;
            } else {
                throw new FormSourceException.ParseError(serverURL);
            }
        } else {
            String serverHash = Md5.getMd5Hash(new ByteArrayInputStream(serverURL.getBytes()));
            analytics.logServerEvent(LEGACY_FORM_LIST, serverHash);
            return parseLegacyFormList(result);
        }
    }

    @Override
    public ManifestFile fetchManifest(String manifestURL) throws FormSourceException {
        if (manifestURL == null) {
            return null;
        }

        DocumentFetchResult result = mapException(() -> openRosaXMLFetcher.getXML(manifestURL));

        if (result.errorMessage != null) {
            if (result.responseCode != HttpURLConnection.HTTP_OK) {
                throw new FormSourceException.ServerError(result.responseCode, serverURL);
            } else {
                throw new FormSourceException.FetchError();
            }
        }

        if (!result.isOpenRosaResponse) {
            throw new FormSourceException.ParseError(serverURL);
        }

        List<MediaFile> mediaFiles = openRosaResponseParser.parseManifest(result.doc);
        if (mediaFiles != null) {
            return new ManifestFile(result.getHash(), mediaFiles);
        } else {
            throw new FormSourceException.ParseError(serverURL);
        }
    }

    @Override
    @NotNull
    public InputStream fetchForm(String formURL) throws FormSourceException {
        HttpGetResult result = mapException(() -> openRosaXMLFetcher.fetch(formURL, null));

        if (result.getInputStream() == null) {
            throw new FormSourceException.ServerError(result.getStatusCode(), serverURL);
        } else {
            return result.getInputStream();
        }
    }

    @Override
    @NotNull
    public InputStream fetchMediaFile(String mediaFileURL) throws FormSourceException {
        HttpGetResult result = mapException(() -> openRosaXMLFetcher.fetch(mediaFileURL, null));

        if (result.getInputStream() == null) {
            throw new FormSourceException.ServerError(result.getStatusCode(), serverURL);
        } else {
            return result.getInputStream();
        }
    }

    public void updateUrl(String url) {
        this.serverURL = url;
    }

    public void updateWebCredentialsUtils(WebCredentialsUtils webCredentialsUtils) {
        this.openRosaXMLFetcher.updateWebCredentialsUtils(webCredentialsUtils);
    }

    private List<FormListItem> parseLegacyFormList(DocumentFetchResult result) throws FormSourceException {
        // Aggregate 0.9.x mode...
        // populate HashMap with form names and urls
        Element formsElement = result.doc.getRootElement();
        int formsCount = formsElement.getChildCount();
        String formId = null;

        List<FormListItem> formList = new ArrayList<>();

        for (int i = 0; i < formsCount; ++i) {
            if (formsElement.getType(i) != Element.ELEMENT) {
                // whitespace
                continue;
            }
            Element child = formsElement.getElement(i);
            String tag = child.getName();
            if (tag.equals("formID")) {
                formId = XFormParser.getXMLText(child, true);
                if (formId != null && formId.length() == 0) {
                    formId = null;
                }
            }
            if (tag.equalsIgnoreCase("form")) {
                String formName = XFormParser.getXMLText(child, true);
                if (formName != null && formName.length() == 0) {
                    formName = null;
                }
                String downloadUrl = child.getAttributeValue(null, "url");
                downloadUrl = downloadUrl.trim();
                if (downloadUrl.length() == 0) {
                    downloadUrl = null;
                }
                if (formName == null) {
                    formList.clear();
                    throw new FormSourceException.FetchError();
                }

                formList.add(new FormListItem(downloadUrl, formId, null, null, formName, null));
                formId = null;
            }
        }

        return formList;
    }

    @NotNull
    private <T> T mapException(Callable<T> callable) throws FormSourceException {
        try {
            T result = callable.call();

            if (result != null) {
                return result;
            } else {
                throw new FormSourceException.FetchError();
            }
        } catch (UnknownHostException e) {
            throw new FormSourceException.Unreachable(serverURL);
        } catch (SSLException e) {
            throw new FormSourceException.SecurityError(serverURL);
        } catch (Exception e) {
            throw new FormSourceException.FetchError();
        }
    }

    @NotNull
    private String getFormListURL() {
        String downloadListUrl = serverURL;

        while (downloadListUrl.endsWith("/")) {
            downloadListUrl = downloadListUrl.substring(0, downloadListUrl.length() - 1);
        }

        downloadListUrl += formListPath;
        return downloadListUrl;
    }

    public String getServerURL() {
        return serverURL;
    }

    public String getFormListPath() {
        return formListPath;
    }

    public WebCredentialsUtils getWebCredentialsUtils() {
        return webCredentialsUtils;
    }
}
