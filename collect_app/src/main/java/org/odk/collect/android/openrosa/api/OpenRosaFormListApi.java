package org.odk.collect.android.openrosa.api;

import org.javarosa.xform.parse.XFormParser;
import org.jetbrains.annotations.NotNull;
import org.kxml2.kdom.Element;
import org.odk.collect.android.openrosa.OpenRosaHttpInterface;
import org.odk.collect.android.openrosa.OpenRosaXmlFetcher;
import org.odk.collect.android.utilities.DocumentFetchResult;
import org.odk.collect.android.utilities.WebCredentialsUtils;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import static org.odk.collect.android.openrosa.api.FormApiException.Type.AUTH_REQUIRED;
import static org.odk.collect.android.openrosa.api.FormApiException.Type.FETCH_ERROR;
import static org.odk.collect.android.openrosa.api.FormApiException.Type.UNKNOWN_HOST;

public class OpenRosaFormListApi implements FormListApi {

    private static final String NAMESPACE_OPENROSA_ORG_XFORMS_XFORMS_LIST =
            "http://openrosa.org/xforms/xformsList";

    private static final String NAMESPACE_OPENROSA_ORG_XFORMS_XFORMS_MANIFEST =
            "http://openrosa.org/xforms/xformsManifest";

    private final OpenRosaXmlFetcher openRosaXMLFetcher;
    private final String serverURL;
    private final String formListPath;

    public OpenRosaFormListApi(String serverURL, String formListPath, OpenRosaHttpInterface openRosaHttpInterface, WebCredentialsUtils webCredentialsUtils) {
        this.openRosaXMLFetcher = new OpenRosaXmlFetcher(openRosaHttpInterface, webCredentialsUtils);
        this.serverURL = serverURL;
        this.formListPath = formListPath;
    }

    /**
     * @deprecated this is only to provide a way to use this implementation for fetching manifests
     * and forms when downloading a form in older code.
     */
    @Deprecated
    public OpenRosaFormListApi(OpenRosaXmlFetcher openRosaXmlFetcher) {
        this.openRosaXMLFetcher = openRosaXmlFetcher;
        serverURL = null;
        formListPath = null;
    }

    @Override
    public List<FormListItem> fetchFormList() throws FormApiException {
        if (serverURL == null) {
            throw new UnsupportedOperationException("Using deprecated constructor!");
        }

        String downloadListUrl = getURL();
        DocumentFetchResult result = null;
        try {
            result = openRosaXMLFetcher.getXML(downloadListUrl);
        } catch (UnknownHostException e) {
            throw new FormApiException(UNKNOWN_HOST, serverURL);
        }

        // If we can't get the document, return the error, cancel the task
        if (result.errorMessage != null) {
            if (result.responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                throw new FormApiException(AUTH_REQUIRED);
            } else {
                throw new FormApiException(FETCH_ERROR);
            }
        }

        List<FormListItem> formList = new ArrayList<>();

        if (result.isOpenRosaResponse) {
            // Attempt OpenRosa 1.0 parsing
            Element xformsElement = result.doc.getRootElement();
            if (!xformsElement.getName().equals("xforms")) {
                throw new FormApiException(FETCH_ERROR);
            }
            if (!isXformsListNamespacedElement(xformsElement)) {
                throw new FormApiException(FETCH_ERROR);
            }

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
                            if (version != null && version.length() == 0) {
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
                    throw new FormApiException(FETCH_ERROR);
                }

                formList.add(new FormListItem(downloadUrl, formId, version, hash, formName, manifestUrl));
            }
        } else {
            // Aggregate 0.9.x mode...
            // populate HashMap with form names and urls
            Element formsElement = result.doc.getRootElement();
            int formsCount = formsElement.getChildCount();
            String formId = null;
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
                        throw new FormApiException(FETCH_ERROR);
                    }

                    formList.add(new FormListItem(downloadUrl, formId, null, null, formName, null));
                    formId = null;
                }
            }
        }

        return formList;
    }

    @Override
    public ManifestFile fetchManifest(String manifestURL) throws FormApiException {
        if (manifestURL == null) {
            return null;
        }

        DocumentFetchResult result = null;

        try {
            result = openRosaXMLFetcher.getXML(manifestURL);
        } catch (UnknownHostException e) {
            throw new FormApiException(UNKNOWN_HOST, serverURL);
        }

        if (result.errorMessage != null) {
            throw new FormApiException(FETCH_ERROR);
        }

        if (!result.isOpenRosaResponse) {
            throw new FormApiException(FETCH_ERROR);
        }

        // Attempt OpenRosa 1.0 parsing
        Element manifestElement = result.doc.getRootElement();

        if (!manifestElement.getName().equals("manifest")) {
            throw new FormApiException(FETCH_ERROR);
        }

        if (!isXformsManifestNamespacedElement(manifestElement)) {
            throw new FormApiException(FETCH_ERROR);
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
                    throw new FormApiException(FETCH_ERROR);
                }

                files.add(new MediaFile(filename, hash, downloadUrl));
            }
        }

        return new ManifestFile(result.getHash(), files);
    }

    @Override
    public InputStream fetchForm(String formURL) throws FormApiException {
        return fetchFile(formURL);
    }

    @Override
    public InputStream fetchMediaFile(String mediaFileURL) throws FormApiException {
        return fetchFile(mediaFileURL);
    }

    private InputStream fetchFile(String formURL) throws FormApiException {
        try {
            return openRosaXMLFetcher.getFile(formURL, null);
        } catch (Exception e) {
            throw new FormApiException(FETCH_ERROR);
        }
    }

    @NotNull
    private String getURL() {
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
