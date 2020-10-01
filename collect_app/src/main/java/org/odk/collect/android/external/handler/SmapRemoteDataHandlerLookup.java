/*
 * Copyright (C) 2014 University of Washington
 *
 * Originally developed by Dobility, Inc. (as part of SurveyCTO)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.odk.collect.android.external.handler;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.javarosa.core.model.condition.EvaluationContext;
import org.javarosa.core.model.condition.IFunctionHandler;
import org.javarosa.xpath.expr.XPathFuncExpr;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.external.ExternalDataUtil;
import org.odk.collect.android.preferences.GeneralKeys;
import org.odk.collect.android.tasks.SmapRemoteWebServiceTask;

import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import timber.log.Timber;

/**
 * Author:
 */
public class SmapRemoteDataHandlerLookup implements IFunctionHandler {

    public static final String HANDLER_NAME = "lookup";
    public String mIdent = null;
    public String mServerUrlBase = null;

    public SmapRemoteDataHandlerLookup(String ident) {
        this.mIdent = ident;

        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(Collect.getInstance().getBaseContext());
        mServerUrlBase = sharedPreferences.getString(GeneralKeys.KEY_SERVER_URL, null) +
                "/lookup/" + ident + "/";
    }

    @Override
    public String getName() {
        return HANDLER_NAME;
    }

    @Override
    public List<Class[]> getPrototypes() {
        return new ArrayList<Class[]>();
    }

    @Override
    public boolean rawArgs() {
        return true;
    }

    @Override
    public boolean realTime() {
        return false;
    }

    @Override
    public Object eval(Object[] args, EvaluationContext ec) {

        if (args.length != 6 && args.length != 4) {
            Timber.e("4 or 6 arguments are needed to evaluate the %s function", HANDLER_NAME);
            return "";
        }

        Collect app = Collect.getInstance();

        String dataSetName = XPathFuncExpr.toString(args[0]);
        String queriedColumn = XPathFuncExpr.toString(args[1]);
        String referenceColumn = XPathFuncExpr.toString(args[2]);
        String referenceValue = XPathFuncExpr.toString(args[3]);
        String searchType = "matches";
        int index = 0;

        if(args.length == 6) {
            String sIndex = XPathFuncExpr.toString(args[4]);
            try {
                index = Integer.parseInt(sIndex);
            } catch (Exception e) { }
            searchType = XPathFuncExpr.toString(args[5]);
        }

        if(referenceValue.length() > 0) {

            // Get the url which doubles as the cache key - url encode it by converting to a URI
            String url = mServerUrlBase + dataSetName + "/" + referenceColumn + "/" + referenceValue;
            if(args.length == 6) {
                url += "?index=" + index;
                url += "&searchType=" + searchType;
            }
            try {
                URL u = new URL(url);
                URI uri = new URI(u.getProtocol(), u.getUserInfo(), u.getHost(), u.getPort(), u.getPath(), u.getQuery(), u.getRef());
                u = uri.toURL();
                url = u.toString();
            } catch (Exception e) {}

            // Get the cache results if they exist
            String data = app.getRemoteData(url);
            HashMap<String, String> record = null;
            try {
                record =
                        new Gson().fromJson(data, new TypeToken<HashMap<String, String>>() {
                        }.getType());
            } catch (Exception e) {
                return data;            // Assume the data contains the error message
            }
            Timber.i("@@@@@@@@@@@@@@@@: " + url + " : " + data);
            if (record == null) {
                // Call a webservice to get the remote record
                app.startRemoteCall(url);
                SmapRemoteWebServiceTask task = new SmapRemoteWebServiceTask();
                task.setSmapRemoteListener(app.getFormEntryActivity());
                task.execute(url, "0", "false", null, null);
                return "";
            } else {
                if(index == -1) {
                    return ExternalDataUtil.nullSafe(record.get("_count"));
                } else {
                    return ExternalDataUtil.nullSafe(record.get(queriedColumn));
                }
            }
        } else {
            // No data to lookup
            return "";
        }

    }
}
