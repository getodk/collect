/*
 * Copyright (c) 2011 Google Inc.
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

package org.odk.collect.android.picasa;

import java.io.IOException;

import com.google.api.client.googleapis.MethodOverride;
import com.google.api.client.http.AbstractHttpContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;

/**
 * GData client.
 * 
 * @author Yaniv Inbar
 */
public abstract class GDataClient {

  private HttpRequestFactory requestFactory;

  private final String gdataVersion;

  private String applicationName;

  private boolean prettyPrint;

  /** Method override needed for PATCH. */
  final MethodOverride override = new MethodOverride();

  protected GDataClient(String gdataVersion, HttpRequestFactory requestFactory) {
    this.gdataVersion = gdataVersion;
    this.requestFactory = requestFactory;
  }

  protected final HttpRequestFactory getRequestFactory() {
    return requestFactory;
  }

  public final String getApplicationName() {
    return applicationName;
  }

  public final void setApplicationName(String applicationName) {
    this.applicationName = applicationName;
  }

  public final boolean getPrettyPrint() {
    return prettyPrint;
  }


  public final void setPrettyPrint(boolean prettyPrint) {
    this.prettyPrint = prettyPrint;
  }

  protected final HttpTransport getTransport() {
    return getRequestFactory().getTransport();
  }

  protected void prepareUrl(GenericUrl url, Class<?> parseAsType){
    url.set("prettyPrint", this.prettyPrint);
  }

  protected void prepare(HttpRequest request) throws IOException {
    request.getHeaders().setUserAgent(applicationName);
    request.getHeaders().put("GData-Version", gdataVersion);
    override.intercept(request);
  }

  protected final HttpResponse execute(HttpRequest request) throws IOException {
    prepare(request);
    return request.execute();
  }

  protected final <T> T executeGet(GenericUrl url, Class<T> parseAsType) throws IOException {
    prepareUrl(url, parseAsType);
    HttpRequest request = getRequestFactory().buildGetRequest(url);
    return execute(request).parseAs(parseAsType);
  }

  protected final void executeDelete(GenericUrl url, String etag) throws IOException {
    prepareUrl(url, null);
    HttpRequest request = getRequestFactory().buildDeleteRequest(url);
    setIfMatch(request, etag);
    execute(request).ignore();
  }

  protected final <T> T executePost(
      GenericUrl url, AbstractHttpContent content, Class<T> parseAsType) throws IOException {
    prepareUrl(url, parseAsType);
    HttpRequest request = getRequestFactory().buildPostRequest(url, content);
    return execute(request).parseAs(parseAsType);
  }

  protected final <T> T executePatchRelativeToOriginal(
      GenericUrl url, AbstractHttpContent patchContent, Class<T> parseAsType, String etag)
      throws IOException {
    prepareUrl(url, parseAsType);
    HttpRequest request = getRequestFactory().buildRequest("PATCH", url, patchContent);
    setIfMatch(request, etag);
    return execute(request).parseAs(parseAsType);
  }

  private void setIfMatch(HttpRequest request, String etag) {
    if (etag != null && !etag.startsWith("W/")) {
      request.getHeaders().setIfMatch(etag);
    }
  }
}
