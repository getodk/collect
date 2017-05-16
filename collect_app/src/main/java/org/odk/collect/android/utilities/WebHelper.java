package org.odk.collect.android.utilities;

import org.opendatakit.httpclientandroidlib.client.HttpClient;
import org.opendatakit.httpclientandroidlib.protocol.HttpContext;

public class WebHelper {

    public DocumentFetchResult getXmlDocument(String downloadListUrl, HttpContext localContext, HttpClient httpclient) {
        return WebUtils.getXmlDocument(downloadListUrl, localContext, httpclient);
    }

}
