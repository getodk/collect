/*
 * Copyright (C) 2011 University of Washington
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.odk.collect.android.utilities;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.zip.GZIPInputStream;

import org.apache.http.HttpStatus;
import org.kxml2.io.KXmlParser;
import org.kxml2.kdom.Document;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.preferences.PreferencesActivity;
import org.opendatakit.httpclientandroidlib.Header;
import org.opendatakit.httpclientandroidlib.HttpEntity;
import org.opendatakit.httpclientandroidlib.HttpHost;
import org.opendatakit.httpclientandroidlib.HttpRequest;
import org.opendatakit.httpclientandroidlib.HttpResponse;
import org.opendatakit.httpclientandroidlib.auth.AuthScope;
import org.opendatakit.httpclientandroidlib.auth.Credentials;
import org.opendatakit.httpclientandroidlib.auth.UsernamePasswordCredentials;
import org.opendatakit.httpclientandroidlib.auth.params.AuthPNames;
import org.opendatakit.httpclientandroidlib.client.AuthCache;
import org.opendatakit.httpclientandroidlib.client.CredentialsProvider;
import org.opendatakit.httpclientandroidlib.client.HttpClient;
import org.opendatakit.httpclientandroidlib.client.methods.HttpGet;
import org.opendatakit.httpclientandroidlib.client.methods.HttpHead;
import org.opendatakit.httpclientandroidlib.client.methods.HttpPost;
import org.opendatakit.httpclientandroidlib.client.params.AuthPolicy;
import org.opendatakit.httpclientandroidlib.client.params.ClientPNames;
import org.opendatakit.httpclientandroidlib.client.params.CookiePolicy;
import org.opendatakit.httpclientandroidlib.client.params.HttpClientParams;
import org.opendatakit.httpclientandroidlib.client.protocol.ClientContext;
import org.opendatakit.httpclientandroidlib.conn.ClientConnectionManager;
import org.opendatakit.httpclientandroidlib.impl.auth.BasicScheme;
import org.opendatakit.httpclientandroidlib.impl.client.BasicAuthCache;
import org.opendatakit.httpclientandroidlib.impl.client.DefaultHttpClient;
import org.opendatakit.httpclientandroidlib.params.BasicHttpParams;
import org.opendatakit.httpclientandroidlib.params.HttpConnectionParams;
import org.opendatakit.httpclientandroidlib.params.HttpParams;
import org.opendatakit.httpclientandroidlib.protocol.HttpContext;
import org.xmlpull.v1.XmlPullParser;

import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;
import android.util.Log;

/**
 * Common utility methods for managing the credentials associated with the
 * request context and constructing http context, client and request with the
 * proper parameters and OpenRosa headers.
 *
 * @author mitchellsundt@gmail.com
 */
public final class WebUtils {
	public static final String t = "WebUtils";

	public static final String OPEN_ROSA_VERSION_HEADER = "X-OpenRosa-Version";
	public static final String OPEN_ROSA_VERSION = "1.0";
	private static final String DATE_HEADER = "Date";

	public static final String HTTP_CONTENT_TYPE_TEXT_XML = "text/xml";
	public static final int CONNECTION_TIMEOUT = 30000;

	public static final String ACCEPT_ENCODING_HEADER = "Accept-Encoding";
	public static final String GZIP_CONTENT_ENCODING = "gzip";

	private static ClientConnectionManager httpConnectionManager = null;

	public static final List<AuthScope> buildAuthScopes(String host) {
		List<AuthScope> asList = new ArrayList<AuthScope>();

		AuthScope a;
		// allow digest auth on any port...
		a = new AuthScope(host, -1, null, AuthPolicy.DIGEST);
		asList.add(a);
		// and allow basic auth on the standard TLS/SSL ports...
		a = new AuthScope(host, 443, null, AuthPolicy.BASIC);
		asList.add(a);
		a = new AuthScope(host, 8443, null, AuthPolicy.BASIC);
		asList.add(a);

		return asList;
	}

	public static final void clearAllCredentials() {
		CredentialsProvider credsProvider = Collect.getInstance()
				.getCredentialsProvider();
		Log.i(t, "clearAllCredentials");
		credsProvider.clear();
	}

	public static final boolean hasCredentials(String userEmail, String host) {
		CredentialsProvider credsProvider = Collect.getInstance()
				.getCredentialsProvider();
		List<AuthScope> asList = buildAuthScopes(host);
		boolean hasCreds = true;
		for (AuthScope a : asList) {
			Credentials c = credsProvider.getCredentials(a);
			if (c == null) {
				hasCreds = false;
				continue;
			}
		}
		return hasCreds;
	}

	/**
	 * Remove all credentials for accessing the specified host.
	 *
	 * @param host
	 */
	private static final void clearHostCredentials(String host) {
		CredentialsProvider credsProvider = Collect.getInstance()
				.getCredentialsProvider();
		Log.i(t, "clearHostCredentials: " + host);
		List<AuthScope> asList = buildAuthScopes(host);
		for (AuthScope a : asList) {
			credsProvider.setCredentials(a, null);
		}
	}

	/**
	 * Remove all credentials for accessing the specified host and, if the
	 * username is not null or blank then add a (username, password) credential
	 * for accessing this host.
	 *
	 * @param username
	 * @param password
	 * @param host
	 */
	public static final void addCredentials(String username, String password,
			String host) {
		// to ensure that this is the only authentication available for this
		// host...
		clearHostCredentials(host);
		if (username != null && username.trim().length() != 0) {
			Log.i(t, "adding credential for host: " + host + " username:"
					+ username);
			Credentials c = new UsernamePasswordCredentials(username, password);
			addCredentials(c, host);
		}
	}

	private static final void addCredentials(Credentials c, String host) {
		CredentialsProvider credsProvider = Collect.getInstance()
				.getCredentialsProvider();
		List<AuthScope> asList = buildAuthScopes(host);
		for (AuthScope a : asList) {
			credsProvider.setCredentials(a, c);
		}
	}

	public static final void enablePreemptiveBasicAuth(
			HttpContext localContext, String host) {
		AuthCache ac = (AuthCache) localContext
				.getAttribute(ClientContext.AUTH_CACHE);
		HttpHost h = new HttpHost(host);
		if (ac == null) {
			ac = new BasicAuthCache();
			localContext.setAttribute(ClientContext.AUTH_CACHE, ac);
		}
		List<AuthScope> asList = buildAuthScopes(host);
		for (AuthScope a : asList) {
			if (a.getScheme() == AuthPolicy.BASIC) {
				ac.put(h, new BasicScheme());
			}
		}
	}

	private static final void setOpenRosaHeaders(HttpRequest req) {
		req.setHeader(OPEN_ROSA_VERSION_HEADER, OPEN_ROSA_VERSION);
		GregorianCalendar g = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
		g.setTime(new Date());
		req.setHeader(DATE_HEADER,
				DateFormat.format("E, dd MMM yyyy hh:mm:ss zz", g).toString());
	}

	public static final HttpHead createOpenRosaHttpHead(Uri u) {
		HttpHead req = new HttpHead(URI.create(u.toString()));
		setOpenRosaHeaders(req);
		return req;
	}

	public static final HttpGet createOpenRosaHttpGet(URI uri) {
		HttpGet req = new HttpGet();
		setOpenRosaHeaders(req);
		setGoogleHeaders(req);
		req.setURI(uri);
		return req;
	}

	public static final void setGoogleHeaders(HttpRequest req) {
		SharedPreferences settings =
                PreferenceManager.getDefaultSharedPreferences(Collect.getInstance().getApplicationContext());
		String protocol = settings.getString(PreferencesActivity.KEY_PROTOCOL, PreferencesActivity.PROTOCOL_ODK_DEFAULT);

		if ( protocol.equals(PreferencesActivity.PROTOCOL_GOOGLE) ) {
	        String auth = settings.getString(PreferencesActivity.KEY_AUTH, "");
			if ((auth != null) && (auth.length() > 0)) {
				req.setHeader("Authorization", "GoogleLogin auth=" + auth);
			}
		}
	}

	public static final HttpPost createOpenRosaHttpPost(Uri u) {
		HttpPost req = new HttpPost(URI.create(u.toString()));
		setOpenRosaHeaders(req);
		setGoogleHeaders(req);
		return req;
	}

	/**
	 * Create an httpClient with connection timeouts and other parameters set.
	 * Save and reuse the connection manager across invocations (this is what
	 * requires synchronized access).
	 *
	 * @param timeout
	 * @return HttpClient properly configured.
	 */
	public static final synchronized HttpClient createHttpClient(int timeout) {
		// configure connection
		HttpParams params = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(params, timeout);
		HttpConnectionParams.setSoTimeout(params, 2 * timeout);
		// support redirecting to handle http: => https: transition
		HttpClientParams.setRedirecting(params, true);
		// support authenticating
		HttpClientParams.setAuthenticating(params, true);
		HttpClientParams.setCookiePolicy(params,
				CookiePolicy.BROWSER_COMPATIBILITY);
		// if possible, bias toward digest auth (may not be in 4.0 beta 2)
		List<String> authPref = new ArrayList<String>();
		authPref.add(AuthPolicy.DIGEST);
		authPref.add(AuthPolicy.BASIC);
		// does this work in Google's 4.0 beta 2 snapshot?
		params.setParameter(AuthPNames.TARGET_AUTH_PREF, authPref);
		params.setParameter(ClientPNames.MAX_REDIRECTS, 1);
		params.setParameter(ClientPNames.ALLOW_CIRCULAR_REDIRECTS, true);

		// setup client
		DefaultHttpClient httpclient;

		// reuse the connection manager across all clients this ODK Collect
		// creates.
		if (httpConnectionManager == null) {
			// let Apache stack create a connection manager.
			httpclient = new DefaultHttpClient(params);
			httpConnectionManager = httpclient.getConnectionManager();
		} else {
			// reuse the connection manager we already got.
			httpclient = new DefaultHttpClient(httpConnectionManager, params);
		}

		return httpclient;
	}

	/**
	 * Utility to ensure that the entity stream of a response is drained of
	 * bytes.
	 *
	 * @param response
	 */
	public static final void discardEntityBytes(HttpResponse response) {
		// may be a server that does not handle
		HttpEntity entity = response.getEntity();
		if (entity != null) {
			try {
				// have to read the stream in order to reuse the connection
				InputStream is = response.getEntity().getContent();
				// read to end of stream...
				final long count = 1024L;
				while (is.skip(count) == count)
					;
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Common method for returning a parsed xml document given a url and the
	 * http context and client objects involved in the web connection.
	 *
	 * @param urlString
	 * @param localContext
	 * @param httpclient
	 * @return
	 */
	public static DocumentFetchResult getXmlDocument(String urlString,
			HttpContext localContext, HttpClient httpclient) {
		URI u = null;
		try {
			URL url = new URL(urlString);
			u = url.toURI();
		} catch (Exception e) {
			e.printStackTrace();
			return new DocumentFetchResult(e.getLocalizedMessage()
			// + app.getString(R.string.while_accessing) + urlString);
					+ ("while accessing") + urlString, 0);
		}

		if (u.getHost() == null ) {
			return new DocumentFetchResult("Invalid server URL (no hostname): " + urlString, 0);
		}

		// if https then enable preemptive basic auth...
		if (u.getScheme().equals("https")) {
			enablePreemptiveBasicAuth(localContext, u.getHost());
		}

		// set up request...
		HttpGet req = WebUtils.createOpenRosaHttpGet(u);
		req.addHeader(WebUtils.ACCEPT_ENCODING_HEADER, WebUtils.GZIP_CONTENT_ENCODING);

		HttpResponse response = null;
		try {
			response = httpclient.execute(req, localContext);
			int statusCode = response.getStatusLine().getStatusCode();

			HttpEntity entity = response.getEntity();

			if (statusCode != HttpStatus.SC_OK) {
				WebUtils.discardEntityBytes(response);
            	if (statusCode == HttpStatus.SC_UNAUTHORIZED) {
            		// clear the cookies -- should not be necessary?
            		Collect.getInstance().getCookieStore().clear();
            	}
				String webError = response.getStatusLine().getReasonPhrase()
						+ " (" + statusCode + ")";

				return new DocumentFetchResult(u.toString()
						+ " responded with: " + webError, statusCode);
			}

			if (entity == null) {
				String error = "No entity body returned from: " + u.toString();
				Log.e(t, error);
				return new DocumentFetchResult(error, 0);
			}

			if (!entity.getContentType().getValue().toLowerCase(Locale.ENGLISH)
					.contains(WebUtils.HTTP_CONTENT_TYPE_TEXT_XML)) {
				WebUtils.discardEntityBytes(response);
				String error = "ContentType: "
						+ entity.getContentType().getValue()
						+ " returned from: "
						+ u.toString()
						+ " is not text/xml.  This is often caused a network proxy.  Do you need to login to your network?";
				Log.e(t, error);
				return new DocumentFetchResult(error, 0);
			}
			// parse response
			Document doc = null;
			try {
				InputStream is = null;
				InputStreamReader isr = null;
				try {
					is = entity.getContent();
	                Header contentEncoding = entity.getContentEncoding();
	                if ( contentEncoding != null && contentEncoding.getValue().equalsIgnoreCase(WebUtils.GZIP_CONTENT_ENCODING) ) {
	                	is = new GZIPInputStream(is);
	                }
					isr = new InputStreamReader(is, "UTF-8");
					doc = new Document();
					KXmlParser parser = new KXmlParser();
					parser.setInput(isr);
					parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES,
							true);
					doc.parse(parser);
					isr.close();
					isr = null;
				} finally {
					if (isr != null) {
						try {
							// ensure stream is consumed...
							final long count = 1024L;
							while (isr.skip(count) == count)
								;
						} catch (Exception e) {
							// no-op
						}
						try {
							isr.close();
						} catch (Exception e) {
							// no-op
						}
					}
					if (is != null) {
						try {
							is.close();
						} catch (Exception e) {
							// no-op
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				String error = "Parsing failed with " + e.getMessage()
						+ "while accessing " + u.toString();
				Log.e(t, error);
				return new DocumentFetchResult(error, 0);
			}

			boolean isOR = false;
			Header[] fields = response
					.getHeaders(WebUtils.OPEN_ROSA_VERSION_HEADER);
			if (fields != null && fields.length >= 1) {
				isOR = true;
				boolean versionMatch = false;
				boolean first = true;
				StringBuilder b = new StringBuilder();
				for (Header h : fields) {
					if (WebUtils.OPEN_ROSA_VERSION.equals(h.getValue())) {
						versionMatch = true;
						break;
					}
					if (!first) {
						b.append("; ");
					}
					first = false;
					b.append(h.getValue());
				}
				if (!versionMatch) {
					Log.w(t, WebUtils.OPEN_ROSA_VERSION_HEADER
							+ " unrecognized version(s): " + b.toString());
				}
			}
			return new DocumentFetchResult(doc, isOR);
		} catch (Exception e) {
			clearHttpConnectionManager();
			e.printStackTrace();
			String cause;
			Throwable c = e;
			while (c.getCause() != null) {
				c = c.getCause();
			}
			cause = c.toString();
			String error = "Error: " + cause + " while accessing "
					+ u.toString();

			Log.w(t, error);
			return new DocumentFetchResult(error, 0);
		}
	}

	public static void clearHttpConnectionManager() {
		// If we get an unexpected exception, the safest thing is to close
		// all connections
		// so that if there is garbage on the connection we ensure it is
		// removed. This
		// is especially important if the connection times out.
		if ( httpConnectionManager != null ) {
			httpConnectionManager.shutdown();
			httpConnectionManager = null;
		}
	}
}
