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

import org.apache.http.auth.AuthSchemeRegistry;
import org.apache.http.client.params.AuthPolicy;
import org.apache.http.impl.auth.BasicSchemeFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpParams;

/**
 * Boilerplate needed to replace DigestSchemeFactory implementation 
 * with that from version 4.1.2 (EnhancedDigestSchemeFactory).
 * 
 * @author mitchellsundt@gmail.com
 *
 */
public class EnhancedHttpClient extends DefaultHttpClient {
	public EnhancedHttpClient(HttpParams params) {
		super(params);
	}

	@Override
    protected AuthSchemeRegistry createAuthSchemeRegistry() {
        AuthSchemeRegistry registry = new AuthSchemeRegistry();
        registry.register(
                AuthPolicy.BASIC,
                new BasicSchemeFactory());
        registry.register(
                AuthPolicy.DIGEST,
                new EnhancedDigestSchemeFactory());
        return registry;
    }

}
