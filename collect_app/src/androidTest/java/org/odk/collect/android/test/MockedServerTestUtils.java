package org.odk.collect.android.test;

import java.net.*;
import java.util.*;
import java.util.concurrent.*;

import org.junit.*;

import okhttp3.mockwebserver.*;

import static org.junit.Assert.*;

public final class MockedServerTestUtils {
	private MockedServerTestUtils() {}

	public static void willRespond(MockWebServer server, String... headersAndBody) {
		MockResponse response = new MockResponse();

		for(int i=0; i<headersAndBody.length-1; ++i) {
			String[] headerParts = headersAndBody[i].split(": ", 2);
			response.addHeader(headerParts[0], headerParts[1]);
		}

		response.setBody(headersAndBody[headersAndBody.length - 1]);

        	server.enqueue(response);
	}
}
