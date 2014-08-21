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

import java.util.List;

import com.google.api.client.util.Key;

/**
 * @author Yaniv Inbar
 */
public class Feed {

  @Key
  public Author author;

  @Key("openSearch:totalResults")
  public int totalResults;

  @Key("link")
  public List<Link> links;

  public String getPostLink() {
    return Link.find(links, "http://schemas.google.com/g/2005#post");
  }

  public String getNextLink() {
    return Link.find(links, "next");
  }
}
