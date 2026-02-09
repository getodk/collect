package org.odk.collect.openrosa.http.okhttp;

import androidx.annotation.NonNull;

import com.burgstaller.okhttp.AuthenticationCacheInterceptor;
import com.burgstaller.okhttp.CachingAuthenticatorDecorator;
import com.burgstaller.okhttp.DispatchingAuthenticator;
import com.burgstaller.okhttp.basic.BasicAuthenticator;
import com.burgstaller.okhttp.digest.CachingAuthenticator;
import com.burgstaller.okhttp.digest.Credentials;
import com.burgstaller.okhttp.digest.DigestAuthenticator;

import org.odk.collect.openrosa.http.HttpCredentialsInterface;
import org.odk.collect.openrosa.http.OpenRosaConstants;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import kotlin.Pair;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class OkHttpOpenRosaServerClientProvider implements OpenRosaServerClientProvider {

    private static final int CONNECTION_TIMEOUT = 30000;
    private static final int WRITE_CONNECTION_TIMEOUT = 60000; // it can take up to 27 seconds to spin up an Aggregate
    private static final int READ_CONNECTION_TIMEOUT = 60000; // it can take up to 27 seconds to spin up an Aggregate
    private static final String USER_AGENT_HEADER = "User-Agent";
    private static final String OPEN_ROSA_VERSION_HEADER = OpenRosaConstants.VERSION_HEADER;
    private static final String OPEN_ROSA_VERSION = "1.0";
    private static final String DATE_HEADER = "Date";

    private final OkHttpClient baseClient;
    private final String cacheDir;

    private final Map<Pair<String, HttpCredentialsInterface>, OkHttpOpenRosaServerClient> clients = new HashMap<>();

    public OkHttpOpenRosaServerClientProvider(@NonNull OkHttpClient baseClient, String cacheDir) {
        this.baseClient = baseClient;
        this.cacheDir = cacheDir;
    }

    public OkHttpOpenRosaServerClientProvider(String cacheDir) {
        this(new OkHttpClient(), cacheDir);
    }

    @Override
    public synchronized OpenRosaServerClient get(String scheme, String userAgent, @NonNull HttpCredentialsInterface credentials) {
        OkHttpOpenRosaServerClient existingClient = clients.get(new Pair<>(scheme, credentials));

        if (existingClient == null) {
            OkHttpOpenRosaServerClient newClient = createNewClient(scheme, userAgent, credentials);
            clients.put(new Pair<>(scheme, credentials), newClient);
            return newClient;
        } else {
            return existingClient;
        }
    }

    @NonNull
    private OkHttpOpenRosaServerClient createNewClient(String scheme, String userAgent, @NonNull HttpCredentialsInterface credentials) {
        OkHttpClient.Builder builder = baseClient.newBuilder()
                .connectTimeout(CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS)
                .writeTimeout(WRITE_CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS)
                .readTimeout(READ_CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS)
                .followRedirects(true);

        if (cacheDir != null && new File(cacheDir).exists()) {
            builder.cache(new Cache(
                    new File(cacheDir, "http_" + credentials.hashCode()),
                    50L * 1024L * 1024L // 50 MiB
            ));
        }

        if (credentials != null) {
            Credentials cred = new Credentials(credentials.getUsername(), credentials.getPassword());

            DispatchingAuthenticator.Builder daBuilder = new DispatchingAuthenticator.Builder();
            daBuilder.with("digest", new DigestAuthenticator(cred));
            if (scheme.equalsIgnoreCase("https")) {
                daBuilder.with("basic", new BasicAuthenticator(cred));
            }

            DispatchingAuthenticator authenticator = daBuilder.build();
            ConcurrentHashMap<String, CachingAuthenticator> authCache = new ConcurrentHashMap<>();
            builder.authenticator(new CachingAuthenticatorDecorator(authenticator, authCache))
                    .addInterceptor(new AuthenticationCacheInterceptor(authCache)).build();
        }

        return new OkHttpOpenRosaServerClient(builder.build(), userAgent);
    }

    private static class OkHttpOpenRosaServerClient implements OpenRosaServerClient {

        private final OkHttpClient client;
        private final String userAgent;

        OkHttpOpenRosaServerClient(OkHttpClient client, String userAgent) {
            this.client = client;
            this.userAgent = userAgent;
        }

        @Override
        public Response makeRequest(Request request, Date currentTime) throws IOException {
            return client.newCall(request.newBuilder()
                    .addHeader(USER_AGENT_HEADER, userAgent)
                    .addHeader(OPEN_ROSA_VERSION_HEADER, OPEN_ROSA_VERSION)
                    .addHeader(DATE_HEADER, getHeaderDate(currentTime))
                    .build()).execute();
        }

        private static String getHeaderDate(Date currentTime) {
            SimpleDateFormat dateFormatGmt = new SimpleDateFormat("E, dd MMM yyyy hh:mm:ss zz", Locale.US);
            dateFormatGmt.setTimeZone(TimeZone.getTimeZone("GMT"));
            return dateFormatGmt.format(currentTime);
        }
    }
}
