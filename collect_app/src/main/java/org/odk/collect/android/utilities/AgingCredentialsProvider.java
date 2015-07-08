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
 * 
 * Original License from BasicCredentialsProvider:
 * ====================================================================
 *
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

package org.odk.collect.android.utilities;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.opendatakit.httpclientandroidlib.annotation.ThreadSafe;
import org.opendatakit.httpclientandroidlib.auth.AuthScope;
import org.opendatakit.httpclientandroidlib.auth.Credentials;
import org.opendatakit.httpclientandroidlib.client.CredentialsProvider;

/**
 * Modified BasicCredentialsProvider that will clear the provider
 * after 'expiryInterval' milliseconds of inactivity.  Use the WebUtils
 * methods to manipulate the credentials in the local context.  You should
 * first check that the credentials exist (which will reset the expiration
 * date), then set them if they are missing.
 * 
 * Largely a cut-and-paste of BasicCredentialsProvider.
 * 
 * @author mitchellsundt@gmail.com
 *
 */
@ThreadSafe
public class AgingCredentialsProvider implements CredentialsProvider {

    private final ConcurrentHashMap<AuthScope, Credentials> credMap;
    private final long expiryInterval;

    private long nextClearTimestamp;

    /**
     * Default constructor.
     */
    public AgingCredentialsProvider(int expiryInterval) {
        super();
        this.credMap = new ConcurrentHashMap<AuthScope, Credentials>();
        this.expiryInterval = expiryInterval;
        nextClearTimestamp = System.currentTimeMillis() + expiryInterval;
    }

    public void setCredentials(
            final AuthScope authscope,
            final Credentials credentials) {
        if (authscope == null) {
            throw new IllegalArgumentException("Authentication scope may not be null");
        }
        if (nextClearTimestamp < System.currentTimeMillis()) {
            clear();
        }
        nextClearTimestamp = System.currentTimeMillis() + expiryInterval;
        if ( credentials == null ) {
        	credMap.remove(authscope);
        } else {
        	credMap.put(authscope, credentials);
        }
    }

    /**
     * Find matching {@link Credentials credentials} for the given authentication scope.
     *
     * @param map the credentials hash map
     * @param authscope the {@link AuthScope authentication scope}
     * @return the credentials
     *
     */
    private static Credentials matchCredentials(
            final Map<AuthScope, Credentials> map,
            final AuthScope authscope) {
        // see if we get a direct hit
        Credentials creds = map.get(authscope);
        if (creds == null) {
            // Nope.
            // Do a full scan
            int bestMatchFactor  = -1;
            AuthScope bestMatch  = null;
            for (AuthScope current: map.keySet()) {
                int factor = authscope.match(current);
                if (factor > bestMatchFactor) {
                    bestMatchFactor = factor;
                    bestMatch = current;
                }
            }
            if (bestMatch != null) {
                creds = map.get(bestMatch);
            }
        }
        return creds;
    }

    public Credentials getCredentials(final AuthScope authscope) {
        if (authscope == null) {
            throw new IllegalArgumentException("Authentication scope may not be null");
        }
        if (nextClearTimestamp < System.currentTimeMillis()) {
            clear();
        }
        nextClearTimestamp = System.currentTimeMillis() + expiryInterval;
        Credentials c = matchCredentials(this.credMap, authscope);
        return c;
    }

    public void clear() {
        this.credMap.clear();
    }

    @Override
    public String toString() {
        return credMap.toString();
    }

}
