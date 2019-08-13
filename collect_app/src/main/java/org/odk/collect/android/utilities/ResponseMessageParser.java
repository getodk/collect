package org.odk.collect.android.utilities;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import timber.log.Timber;

public class ResponseMessageParser {

    private static final String MESSAGE_XML_TAG = "message";

    private boolean isValid;
    private String messageResponse;

    public boolean isValid() {
        return this.isValid;
    }

    public String getMessageResponse() {
        return messageResponse;
    }

    public void setMessageResponse(String response) {
        isValid = false;
        try {
            if (response.contains("OpenRosaResponse")) {
                DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                Document doc = builder.parse(new ByteArrayInputStream(response.getBytes()));
                doc.getDocumentElement().normalize();

                if (doc.getElementsByTagName(MESSAGE_XML_TAG).item(0) != null) {
                    messageResponse = doc.getElementsByTagName(MESSAGE_XML_TAG).item(0).getTextContent();
                    isValid = true;
                }
            }

        } catch (SAXException | IOException | ParserConfigurationException e) {
            Timber.e(e, "Error parsing XML message due to %s ", e.getMessage());
        }
    }

}
