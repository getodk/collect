package org.odk.collect.android.openrosa;

import org.javarosa.xform.parse.XFormParser;
import org.jetbrains.annotations.NotNull;
import org.kxml2.kdom.Element;
import org.odk.collect.android.analytics.Analytics;
import org.odk.collect.android.forms.FormListItem;
import org.odk.collect.android.forms.FormSource;
import org.odk.collect.android.forms.FormSourceException;
import org.odk.collect.android.forms.ManifestFile;
import org.odk.collect.android.forms.MediaFile;
import org.odk.collect.android.utilities.DocumentFetchResult;
import org.odk.collect.android.utilities.FileUtils;
import org.odk.collect.android.utilities.WebCredentialsUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import javax.net.ssl.SSLException;

import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.HttpURLConnection.HTTP_UNAUTHORIZED;
import static org.odk.collect.android.analytics.AnalyticsEvents.LEGACY_FORM_LIST;
import static org.odk.collect.android.forms.FormSourceException.Type.AUTH_REQUIRED;
import static org.odk.collect.android.forms.FormSourceException.Type.FETCH_ERROR;
import static org.odk.collect.android.forms.FormSourceException.Type.SECURITY_ERROR;
import static org.odk.collect.android.forms.FormSourceException.Type.UNREACHABLE;

public class OpenRosaFormSource implements FormSource {

    private static final String NAMESPACE_OPENROSA_ORG_XFORMS_XFORMS_LIST =
            "http://openrosa.org/xforms/xformsList";

    private static final String NAMESPACE_OPENROSA_ORG_XFORMS_XFORMS_MANIFEST =
            "http://openrosa.org/xforms/xformsManifest";

    private final OpenRosaXmlFetcher openRosaXMLFetcher;
    private String serverURL;
    private final String formListPath;

    private final Analytics analytics;

    public OpenRosaFormSource(String serverURL, String formListPath, OpenRosaHttpInterface openRosaHttpInterface, WebCredentialsUtils webCredentialsUtils, Analytics analytics) {
        this.openRosaXMLFetcher = new OpenRosaXmlFetcher(openRosaHttpInterface, webCredentialsUtils);
        this.serverURL = serverURL;
        this.formListPath = formListPath;

        this.analytics = analytics;
    }

    @Override
    public List<FormListItem> fetchFormList() throws FormSourceException {
        if (serverURL == null) {
            throw new UnsupportedOperationException("Using deprecated constructor!");
        }

        DocumentFetchResult result = mapException(() -> openRosaXMLFetcher.getXML(getFormListURL()));

        // If we can't get the document, return the error, cancel the task
        if (result.errorMessage != null) {
            if (result.responseCode == HTTP_UNAUTHORIZED) {
                throw new FormSourceException(AUTH_REQUIRED);
            } else if (result.responseCode == HTTP_NOT_FOUND) {
                throw new FormSourceException(UNREACHABLE, serverURL);
            } else {
                throw new FormSourceException(FETCH_ERROR);
            }
        }

        if (result.isOpenRosaResponse) {
            return parseFormList(result);
        } else {
            String serverHash = FileUtils.getMd5Hash(new ByteArrayInputStream(serverURL.getBytes()));
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
            throw new FormSourceException(FETCH_ERROR);
        }

        if (!result.isOpenRosaResponse) {
            throw new FormSourceException(FETCH_ERROR);
        }

        return parseManifest(result);
    }

    @Override
    @NotNull
    public InputStream fetchForm(String formURL) throws FormSourceException {
        return mapException(() -> openRosaXMLFetcher.getFile(formURL, null));
    }

    @Override
    @NotNull
    public InputStream fetchMediaFile(String mediaFileURL) throws FormSourceException {
        return mapException(() -> openRosaXMLFetcher.getFile(mediaFileURL, null));
    }

    @Override
    public void updateUrl(String url) {
        this.serverURL = url;
    }

    @Override
    public void updateWebCredentialsUtils(WebCredentialsUtils webCredentialsUtils) {
        this.openRosaXMLFetcher.updateWebCredentialsUtils(webCredentialsUtils);
    }

    private List<FormListItem> parseFormList(DocumentFetchResult result) throws FormSourceException {
        // Attempt OpenRosa 1.0 parsing
        Element xformsElement = result.doc.getRootElement();
        if (!xformsElement.getName().equals("xforms")) {
            throw new FormSourceException(FETCH_ERROR);
        }
        if (!isXformsListNamespacedElement(xformsElement)) {
            throw new FormSourceException(FETCH_ERROR);
        }

        List<FormListItem> formList = new ArrayList<>();

        int elements = xformsElement.getChildCount();
        for (int i = 0; i < elements; ++i) {
            if (xformsElement.getType(i) != Element.ELEMENT) {
                // e.g., whitespace (text)
                continue;
            }
            Element xformElement = xformsElement.getElement(i);
            if (!isXformsListNamespacedElement(xformElement)) {
                // someone else's extension?
                continue;
            }
            String name = xformElement.getName();
            if (!name.equalsIgnoreCase("xform")) {
                // someone else's extension?
                continue;
            }

            // this is something we know how to interpret
            String formId = null;
            String formName = null;
            String version = null;
            String majorMinorVersion = null;
            String description = null;
            String downloadUrl = null;
            String manifestUrl = null;
            String hash = null;
            // don't process descriptionUrl
            int fieldCount = xformElement.getChildCount();
            for (int j = 0; j < fieldCount; ++j) {
                if (xformElement.getType(j) != Element.ELEMENT) {
                    // whitespace
                    continue;
                }
                Element child = xformElement.getElement(j);
                if (!isXformsListNamespacedElement(child)) {
                    // someone else's extension?
                    continue;
                }
                String tag = child.getName();
                switch (tag) {
                    case "formID":
                        formId = XFormParser.getXMLText(child, true);
                        if (formId != null && formId.length() == 0) {
                            formId = null;
                        }
                        break;
                    case "name":
                        formName = XFormParser.getXMLText(child, true);
                        if (formName != null && formName.length() == 0) {
                            formName = null;
                        }
                        break;
                    case "version":
                        version = XFormParser.getXMLText(child, true);
                        if (version != null && version.trim().isEmpty()) {
                            version = null;
                        }
                        break;
                    case "majorMinorVersion":
                        majorMinorVersion = XFormParser.getXMLText(child, true);
                        if (majorMinorVersion != null && majorMinorVersion.length() == 0) {
                            majorMinorVersion = null;
                        }
                        break;
                    case "descriptionText":
                        description = XFormParser.getXMLText(child, true);
                        if (description != null && description.length() == 0) {
                            description = null;
                        }
                        break;
                    case "downloadUrl":
                        downloadUrl = XFormParser.getXMLText(child, true);
                        if (downloadUrl != null && downloadUrl.length() == 0) {
                            downloadUrl = null;
                        }
                        break;
                    case "manifestUrl":
                        manifestUrl = XFormParser.getXMLText(child, true);
                        if (manifestUrl != null && manifestUrl.length() == 0) {
                            manifestUrl = null;
                        }
                        break;
                    case "hash":
                        hash = XFormParser.getXMLText(child, true);
                        if (hash != null && hash.length() == 0) {
                            hash = null;
                        }
                        break;
                }
            }

            if (formId == null || downloadUrl == null || formName == null) {
                formList.clear();
                throw new FormSourceException(FETCH_ERROR);
            }

            FormListItem formListItem = new FormListItem(downloadUrl, formId, version, hash, formName, manifestUrl);
            formList.add(formListItem);
        }

        return formList;
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
                    throw new FormSourceException(FETCH_ERROR);
                }

                formList.add(new FormListItem(downloadUrl, formId, null, null, formName, null));
                formId = null;
            }
        }

        return formList;
    }

    private ManifestFile parseManifest(DocumentFetchResult result) throws FormSourceException {
        // Attempt OpenRosa 1.0 parsing
        Element manifestElement = result.doc.getRootElement();

        if (!manifestElement.getName().equals("manifest")) {
            throw new FormSourceException(FETCH_ERROR);
        }

        if (!isXformsManifestNamespacedElement(manifestElement)) {
            throw new FormSourceException(FETCH_ERROR);
        }

        int elements = manifestElement.getChildCount();
        List<MediaFile> files = new ArrayList<>();
        for (int i = 0; i < elements; ++i) {
            if (manifestElement.getType(i) != Element.ELEMENT) {
                // e.g., whitespace (text)
                continue;
            }
            Element mediaFileElement = manifestElement.getElement(i);
            if (!isXformsManifestNamespacedElement(mediaFileElement)) {
                // someone else's extension?
                continue;
            }
            String name = mediaFileElement.getName();
            if (name.equalsIgnoreCase("mediaFile")) {
                String filename = null;
                String hash = null;
                String downloadUrl = null;
                // don't process descriptionUrl
                int childCount = mediaFileElement.getChildCount();
                for (int j = 0; j < childCount; ++j) {
                    if (mediaFileElement.getType(j) != Element.ELEMENT) {
                        // e.g., whitespace (text)
                        continue;
                    }
                    Element child = mediaFileElement.getElement(j);
                    if (!isXformsManifestNamespacedElement(child)) {
                        // someone else's extension?
                        continue;
                    }
                    String tag = child.getName();
                    switch (tag) {
                        case "filename":
                            filename = XFormParser.getXMLText(child, true);
                            if (filename != null && filename.length() == 0) {
                                filename = null;
                            }
                            break;
                        case "hash":
                            hash = XFormParser.getXMLText(child, true);
                            if (hash != null && hash.length() == 0) {
                                hash = null;
                            }
                            break;
                        case "downloadUrl":
                            downloadUrl = XFormParser.getXMLText(child, true);
                            if (downloadUrl != null && downloadUrl.length() == 0) {
                                downloadUrl = null;
                            }
                            break;
                    }
                }

                if (filename == null || downloadUrl == null || hash == null) {
                    throw new FormSourceException(FETCH_ERROR);
                }

                files.add(new MediaFile(filename, hash, downloadUrl));
            }
        }

        return new ManifestFile(result.getHash(), files);
    }

    @NotNull
    private <T> T mapException(Callable<T> callable) throws FormSourceException {
        try {
            T result = callable.call();

            if (result != null) {
                return result;
            } else {
                throw new FormSourceException(FETCH_ERROR, serverURL);
            }
        } catch (UnknownHostException e) {
            throw new FormSourceException(UNREACHABLE, serverURL);
        } catch (SSLException e) {
            throw new FormSourceException(SECURITY_ERROR, serverURL);
        } catch (Exception e) {
            throw new FormSourceException(FETCH_ERROR, serverURL);
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

    private static boolean isXformsListNamespacedElement(Element e) {
        return e.getNamespace().equalsIgnoreCase(NAMESPACE_OPENROSA_ORG_XFORMS_XFORMS_LIST);
    }

    private static boolean isXformsManifestNamespacedElement(Element e) {
        return e.getNamespace().equalsIgnoreCase(NAMESPACE_OPENROSA_ORG_XFORMS_XFORMS_MANIFEST);
    }
}
