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

import com.google.api.client.googleapis.xml.atom.AtomPatchRelativeToOriginalContent;
import com.google.api.client.googleapis.xml.atom.GoogleAtom;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.xml.atom.AtomContent;
import com.google.api.client.xml.XmlNamespaceDictionary;
import com.google.api.client.xml.XmlObjectParser;

/**
 * GData XML client.
 *
 * @author Yaniv Inbar
 */
public abstract class GDataXmlClient extends GDataClient {

  private final XmlNamespaceDictionary namespaceDictionary;

  private boolean partialResponse = true;

  protected GDataXmlClient(String gdataVersion, HttpRequestFactory requestFactory,
      XmlNamespaceDictionary namespaceDictionary) {
    super(gdataVersion, requestFactory);
    this.namespaceDictionary = namespaceDictionary;
  }

  public XmlNamespaceDictionary getNamespaceDictionary() {
    return namespaceDictionary;
  }

  @Override
  protected void prepare(HttpRequest request) throws IOException {
    super.prepare(request);
    request.setParser(new XmlObjectParser(namespaceDictionary));
  }

  public final boolean getPartialResponse() {
    return partialResponse;
  }

  public final void setPartialResponse(boolean partialResponse) {
    this.partialResponse = partialResponse;
  }

  @Override
  protected void prepareUrl(GenericUrl url, Class<?> parseAsType) {
    super.prepareUrl(url, parseAsType);
    if (partialResponse && parseAsType != null) {
      url.put("fields", GoogleAtom.getFieldsFor(parseAsType));
    }
  }

  protected final <T> T executePatchRelativeToOriginal(
      GenericUrl url, T original, T updated, String etag) throws IOException {
    AtomPatchRelativeToOriginalContent content =
        new AtomPatchRelativeToOriginalContent(namespaceDictionary, original, updated);
    @SuppressWarnings("unchecked")
    Class<T> parseAsType = (Class<T>) updated.getClass();
    return executePatchRelativeToOriginal(url, content, parseAsType, etag);
  }

  protected final <T> T executePost(GenericUrl url, boolean isFeed, T content) throws IOException {
    AtomContent atomContent = isFeed ? AtomContent.forFeed(namespaceDictionary, content)
        : AtomContent.forEntry(namespaceDictionary, content);
    @SuppressWarnings("unchecked")
    Class<T> parseAsType = (Class<T>) content.getClass();
    return executePost(url, atomContent, parseAsType);
  }
}
