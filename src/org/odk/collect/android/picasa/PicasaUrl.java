/*
 * Copyright (c) 2010 Google Inc.
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

import com.google.api.client.http.GenericUrl;
import com.google.api.client.util.Key;

/**
 * @author Yaniv Inbar
 */
public class PicasaUrl extends GenericUrl {

  /** Whether to pretty print HTTP requests and responses. */   
  private static final boolean PRETTY_PRINT = true;
  
  public static final String ROOT_URL = "https://picasaweb.google.com/data/";

  @Key("max-results")
  public Integer maxResults;

  @Key
  public String kinds;

  public PicasaUrl(String url) {
    super(url);
    this.set("prettyPrint", PRETTY_PRINT);
  }

  /**
   * Constructs a new Picasa Web Albums URL based on the given relative path.
   *
   * @param relativePath encoded path relative to the {@link #ROOT_URL}
   * @return new Picasa URL
   */
  public static PicasaUrl relativeToRoot(String relativePath) {
    return new PicasaUrl(ROOT_URL + relativePath);
  }
}
