package org.odk.collect.android.utilities;

import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
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

    public void setMessageResponse(String response, String instanceName) {
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
            } else {        // smap
                messageResponse = response;
                isValid = true;
                String [] parts = messageResponse.split("::");
                if(parts.length > 1) {
                    if (parts[0].equals("blocked")) {
                        messageResponse = Collect.getInstance().getBaseContext().getString(R.string.smap_survey_blocked, String.valueOf(parts[1]));
                    } else if (parts[0].equals("deleted")) {
                        messageResponse = Collect.getInstance().getBaseContext().getString(R.string.smap_survey_deleted, String.valueOf(parts[1]));
                    }
                } else if(parts.length == 1) {
                    if(parts[0].equals("Forbidden")) {
                        messageResponse = Collect.getInstance().getBaseContext().getString(R.string.smap_forbidden, instanceName);
                    }
                }
            }

        } catch (SAXException | IOException | ParserConfigurationException e) {
            Timber.e(e, "Error parsing XML message due to %s ", e.getMessage());
        }
    }

}
