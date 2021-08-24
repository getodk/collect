package org.odk.collect.android.openrosa;

import org.javarosa.xform.parse.XFormParser;
import org.jetbrains.annotations.Nullable;
import org.kxml2.kdom.Document;
import org.kxml2.kdom.Element;
import org.odk.collect.forms.FormListItem;
import org.odk.collect.forms.MediaFile;
import org.odk.collect.shared.strings.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class OpenRosaResponseParserImpl implements OpenRosaResponseParser {

    private static final String NAMESPACE_OPENROSA_ORG_XFORMS_XFORMS_LIST = "http://openrosa.org/xforms/xformsList";
    private static final String NAMESPACE_OPENROSA_ORG_XFORMS_XFORMS_MANIFEST = "http://openrosa.org/xforms/xformsManifest";

    @Override
    @Nullable
    public List<FormListItem> parseFormList(Document document) {
        // Attempt OpenRosa 1.0 parsing
        Element xformsElement = document.getRootElement();

        if (!xformsElement.getName().equals("xforms")) {
            return null;
        }
        if (!isXformsListNamespacedElement(xformsElement)) {
            return null;
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
                        if (version != null && StringUtils.isBlank(version)) {
                            version = null;
                        }
                        break;
                    case "majorMinorVersion":
                        break;
                    case "descriptionText":
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
                return null;
            }

            FormListItem formListItem = new FormListItem(downloadUrl, formId, version, hash, formName, manifestUrl);
            formList.add(formListItem);
        }

        return formList;
    }

    @Nullable
    public List<MediaFile> parseManifest(Document document) {
        // Attempt OpenRosa 1.0 parsing
        Element manifestElement = document.getRootElement();

        if (!manifestElement.getName().equals("manifest")) {
            return null;
        }

        if (!isXformsManifestNamespacedElement(manifestElement)) {
            return null;
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
                    return null;
                }

                files.add(new MediaFile(filename, hash, downloadUrl));
            }
        }

        return files;
    }

    private static boolean isXformsListNamespacedElement(Element e) {
        return e.getNamespace().equalsIgnoreCase(NAMESPACE_OPENROSA_ORG_XFORMS_XFORMS_LIST);
    }

    private static boolean isXformsManifestNamespacedElement(Element e) {
        return e.getNamespace().equalsIgnoreCase(NAMESPACE_OPENROSA_ORG_XFORMS_XFORMS_MANIFEST);
    }
}
