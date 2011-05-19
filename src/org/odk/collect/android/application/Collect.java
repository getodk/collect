package org.odk.collect.android.application;

import org.apache.http.client.CookieStore;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.SyncBasicHttpContext;
import org.odk.collect.android.utilities.AgingCredentialsProvider;

import android.app.Application;

public class Collect extends Application {
    
    private HttpContext localContext = null;
    private static Collect singleton = null;


    public static Collect getInstance() {
        return singleton;
    }
    
    public synchronized HttpContext getHttpContext() {
        if (localContext == null) {
            // set up one context for all HTTP requests so that authentication
            // and cookies can be retained.
            localContext = new SyncBasicHttpContext(new BasicHttpContext());

            // establish a local cookie store for this attempt at downloading...
            CookieStore cookieStore = new BasicCookieStore();
            localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);

            // and establish a credentials provider...
            CredentialsProvider credsProvider = new AgingCredentialsProvider(7 * 60 * 1000);
            localContext.setAttribute(ClientContext.CREDS_PROVIDER, credsProvider);
        }
        return localContext;
    }

    @Override
    public void onCreate() {
        singleton = this;
        super.onCreate();
    }
    
    

}
