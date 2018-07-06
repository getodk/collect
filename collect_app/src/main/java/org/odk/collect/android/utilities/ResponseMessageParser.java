package org.odk.collect.android.utilities;

import org.opendatakit.httpclientandroidlib.HttpEntity;
import org.opendatakit.httpclientandroidlib.util.EntityUtils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import timber.log.Timber;

/**
 * Created by Jon Nordling on 2/21/17.
 * The purpose of this class is to handle the XML parsing
 * of the server responses
 */

public class ResponseMessageParser {
    private final HttpEntity httpEntity;
    private static final String MESSAGE_XML_TAG = "message";
    public boolean isValid;
    public String messageResponse;

    public ResponseMessageParser(HttpEntity httpEntity) {
        this.httpEntity = httpEntity;
        this.messageResponse = parseXMLMessage();
        if (messageResponse != null) {
            this.isValid = true;
        }
    }

    public boolean isValid() {
        return this.isValid;
    }

    public String getMessageResponse() {
        return this.messageResponse;
    }

    public String parseXMLMessage() {
        String message = null;
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        try {
            builder = dbFactory.newDocumentBuilder();
            Document doc = null;

            String httpEntityString = EntityUtils.toString(httpEntity);

            if (httpEntityString.contains("OpenRosaResponse")) {
                doc = builder.parse(new ByteArrayInputStream(httpEntityString.getBytes()));
                doc.getDocumentElement().normalize();

                if (doc.getElementsByTagName(MESSAGE_XML_TAG).item(0) != null) {
                    message = doc.getElementsByTagName(MESSAGE_XML_TAG).item(0).getTextContent();
                } else {
                    isValid = false;
                }
            }

            return message;

        } catch (SAXException | IOException | ParserConfigurationException e) {
            Timber.e(e, "Error parsing XML message due to %s ", e.getMessage());
            isValid = false;
        }

        return message;
    }


}
