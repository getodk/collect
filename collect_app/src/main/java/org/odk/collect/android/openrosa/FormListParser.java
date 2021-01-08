package org.odk.collect.android.openrosa;

import org.javarosa.xform.parse.XFormParser;
import org.jetbrains.annotations.Nullable;
import org.kxml2.kdom.Document;
import org.kxml2.kdom.Element;
import org.odk.collect.android.forms.FormListItem;

import java.util.ArrayList;
import java.util.List;

public class FormListParser {

    private static final String NAMESPACE_OPENROSA_ORG_XFORMS_XFORMS_LIST =
            "http://openrosa.org/xforms/xformsList";

    @Nullable
    public List<FormListItem> parse(Document document) {
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
                return null;
            }

            FormListItem formListItem = new FormListItem(downloadUrl, formId, version, hash, formName, manifestUrl);
            formList.add(formListItem);
        }

        return formList;
    }

    private static boolean isXformsListNamespacedElement(Element e) {
        return e.getNamespace().equalsIgnoreCase(NAMESPACE_OPENROSA_ORG_XFORMS_XFORMS_LIST);
    }
}
