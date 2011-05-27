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

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;

/**
 * Wrapper to a basic credentials provider that will clear the provider
 * after 'expiryInterval' milliseconds of inactivity.  Use the WebUtils
 * methods to manipulate the credentials in the local context.  You should
 * first check that the credentials exist (which will reset the expiration
 * date), then set them if they are missing.
 * 
 * @author mitchellsundt@gmail.com
 *
 */
public class AgingCredentialsProvider implements CredentialsProvider {

    private final BasicCredentialsProvider provider = new BasicCredentialsProvider();
    private final long expiryInterval;

    private long nextClearTimestamp;


    public AgingCredentialsProvider(int expiryInterval) {
        this.expiryInterval = expiryInterval;
        nextClearTimestamp = System.currentTimeMillis() + expiryInterval;
    }


    @Override
    public synchronized void clear() {
        provider.clear();
    }


    @Override
    public synchronized Credentials getCredentials(AuthScope authscope) {
        if (nextClearTimestamp < System.currentTimeMillis()) {
            clear();
        }
        nextClearTimestamp = System.currentTimeMillis() + expiryInterval;
        return provider.getCredentials(authscope);
    }


    @Override
    public synchronized void setCredentials(AuthScope authscope, Credentials credentials) {
        if (nextClearTimestamp < System.currentTimeMillis()) {
            clear();
        }
        nextClearTimestamp = System.currentTimeMillis() + expiryInterval;
        provider.setCredentials(authscope, credentials);
    }
}
