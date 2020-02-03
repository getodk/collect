package org.odk.collect.android.openrosa.support;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.mockwebserver.MockWebServer;
import okhttp3.tls.internal.TlsUtil;

public class MockWebServerRule implements TestRule {

    private final List<MockWebServer> mockWebServers = new ArrayList<>();

    @Override
    public Statement apply(Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                try {
                    base.evaluate();
                } finally {
                    teardown();
                }
            }
        };
    }

    public MockWebServer start() throws Exception {
        MockWebServer mockWebServer = new MockWebServer();
        mockWebServers.add(mockWebServer);

        mockWebServer.start();
        return mockWebServer;
    }

    public MockWebServer startHTTPS() throws IOException {
        MockWebServer httpsMockWebServer = new MockWebServer();
        mockWebServers.add(httpsMockWebServer);

        httpsMockWebServer.useHttps(TlsUtil.localhost().sslSocketFactory(), false);
        httpsMockWebServer.start(8443);
        return httpsMockWebServer;
    }

    public void teardown() throws IOException {
        for (MockWebServer mockWebServer : mockWebServers) {
            mockWebServer.shutdown();
        }
    }
}
