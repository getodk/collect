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
    private HttpEntity httpEntity;
    private final String MESSAGE_XML_TAG = "message";
    public Boolean isValid = false;
    public String messageResponse;

    public ResponseMessageParser(HttpEntity httpEntity) {
        this.httpEntity = httpEntity;
        this.messageResponse = parseXMLMessage();
        if (messageResponse != null) {
            this.isValid = true;
        }
    }

    private HttpEntity getHttpEntity() {
        return httpEntity;
    }

    public Boolean isValid() {
        return this.isValid;
    }

    public String getMessageResponse() {
        return this.messageResponse;
    }


    public String parseXMLMessage() {
        String message = null;
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder;
        try {
            dBuilder = dbFactory.newDocumentBuilder();
            Document doc = null;
            try {
                String httpEntityString = EntityUtils.toString(httpEntity);
                doc = dBuilder.parse(new ByteArrayInputStream(httpEntityString.getBytes()));
                doc.getDocumentElement().normalize();

                if (doc.getElementsByTagName(MESSAGE_XML_TAG).item(0) != null) {
                    message = doc.getElementsByTagName(MESSAGE_XML_TAG).item(0).getTextContent();
                } else {
                    isValid = false;
                }
                return message;

            } catch (SAXException | IOException e) {
                Timber.e(e, "Error parsing XML message due to %s ", e.getMessage());
                isValid = false;
            }

            return message;

        } catch (ParserConfigurationException e) {
            Timber.e(e, "Error parsing XML message due to %s ", e.getMessage());
            isValid = false;
        }

        return message;
    }
}
