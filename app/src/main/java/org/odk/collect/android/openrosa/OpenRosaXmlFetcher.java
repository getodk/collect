package org.odk.collect.android.openrosa;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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

import timber.log.Timber;

/**
 * This is only used inside {@link OpenRosaFormSource} and could potentially be absorbed there. Some
 * of the parsing logic here might be better broken out somewhere else however if it can be used
 * in other scenarios.
 */
class OpenRosaXmlFetcher {

    private static final String HTTP_CONTENT_TYPE_TEXT_XML = "text/xml";

    private final OpenRosaHttpInterface httpInterface;
    private WebCredentialsUtils webCredentialsUtils;

    OpenRosaXmlFetcher(OpenRosaHttpInterface httpInterface, WebCredentialsUtils webCredentialsUtils) {
        this.httpInterface = httpInterface;
        this.webCredentialsUtils = webCredentialsUtils;
    }

    /**
     * Gets an XML document for a given url
     *
     * @param urlString - url of the XML document
     * @return DocumentFetchResult - an object that contains the results of the "get" operation
     */

    @SuppressWarnings("PMD.AvoidRethrowingException")
    public DocumentFetchResult getXML(String urlString) throws Exception {

        // parse response
        Document doc;
        HttpGetResult inputStreamResult;

        try {
            inputStreamResult = fetch(urlString, HTTP_CONTENT_TYPE_TEXT_XML);

            if (inputStreamResult.getStatusCode() != HttpURLConnection.HTTP_OK) {
                String error = "getXML failed while accessing "
                        + urlString + " with status code: " + inputStreamResult.getStatusCode();
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
            throw e;
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

    @NonNull
    public HttpGetResult fetch(@NonNull String downloadUrl, @Nullable final String contentType) throws Exception {
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
            Timber.e(new Error("Invalid server URL (no hostname): " + downloadUrl));
            throw new Exception("Invalid server URL (no hostname): " + downloadUrl);
        }

        return httpInterface.executeGetRequest(uri, contentType, webCredentialsUtils.getCredentials(uri));
    }

    public WebCredentialsUtils getWebCredentialsUtils() {
        return webCredentialsUtils;
    }

    public void updateWebCredentialsUtils(WebCredentialsUtils webCredentialsUtils) {
        this.webCredentialsUtils = webCredentialsUtils;
    }
}
