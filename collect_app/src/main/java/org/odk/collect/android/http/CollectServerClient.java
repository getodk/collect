package org.odk.collect.android.http;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.kxml2.io.KXmlParser;
import org.kxml2.kdom.Document;
import org.odk.collect.android.utilities.DocumentFetchResult;
import org.odk.collect.android.utilities.WebCredentialsUtils;
import org.xmlpull.v1.XmlPullParser;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.inject.Inject;

import timber.log.Timber;

public class CollectServerClient {

    public static final String HTTP_CONTENT_TYPE_TEXT_XML = "text/xml";

    protected OpenRosaHttpInterface httpInterface;
    private final WebCredentialsUtils webCredentialsUtils;

    @Inject
    public CollectServerClient(OpenRosaHttpInterface httpInterface, WebCredentialsUtils webCredentialsUtils) {
        this.httpInterface = httpInterface;
        this.webCredentialsUtils = webCredentialsUtils;
    }

    /**
     * Gets an XML document for a given url
     *
     * @param urlString - url of the XML document
     * @return DocumentFetchResult - an object that contains the results of the "get" operation
     */
    public DocumentFetchResult getXmlDocument(String urlString) {

        // parse response
        Document doc;
        HttpGetResult inputStreamResult;

        try {
            inputStreamResult = getHttpInputStream(urlString, HTTP_CONTENT_TYPE_TEXT_XML);

            if (inputStreamResult.getStatusCode() != HttpURLConnection.HTTP_OK) {
                String error = "getXmlDocument failed while accessing "
                        + urlString + " with status code: " + inputStreamResult.getStatusCode();
                Timber.e(error);
                return new DocumentFetchResult(error, inputStreamResult.getStatusCode());
            }

            try (InputStream resultInputStream = inputStreamResult.getInputStream();
                 InputStreamReader streamReader = new InputStreamReader(resultInputStream, "UTF-8")) {

                doc = new Document();
                KXmlParser parser = new KXmlParser();
                parser.setInput(streamReader);
                parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
                doc.parse(parser);
            }
        } catch (Exception e) {
            String error = "Parsing failed with " + e.getMessage() + " while accessing " + urlString;
            Timber.e(error);
            return new DocumentFetchResult(error, 0);
        }

        return new DocumentFetchResult(doc, inputStreamResult.isOpenRosaResponse(), inputStreamResult.getHash());
    }

    /**
     * Creates a Http connection and input stream
     *
     * @param downloadUrl uri of the stream
     * @param contentType check the returned Mime Type to ensure it matches. "text/xml" causes a Hash to be calculated
     * @return HttpGetResult - An object containing the Stream, Hash and Headers
     * @throws Exception - Can throw a multitude of Exceptions, such as MalformedURLException or IOException
     */
    public @NonNull
    HttpGetResult getHttpInputStream(@NonNull String downloadUrl, @Nullable final String contentType) throws Exception {
        URI uri;
        try {
            // assume the downloadUrl is escaped properly
            URL url = new URL(downloadUrl);
            uri = url.toURI();
        } catch (MalformedURLException | URISyntaxException e) {
            Timber.e(e, "Unable to get a URI for download URL : %s  due to %s : ", downloadUrl, e.getMessage());
            throw e;
        }

        if (uri.getHost() == null) {
            Timber.e("Invalid server URL (no hostname): %s", downloadUrl);
            throw new Exception("Invalid server URL (no hostname): " + downloadUrl);
        }

        return httpInterface.executeGetRequest(uri, contentType, webCredentialsUtils.getCredentials(uri));
    }

    public static String getPlainTextMimeType() {
        return "text/plain";
    }

}