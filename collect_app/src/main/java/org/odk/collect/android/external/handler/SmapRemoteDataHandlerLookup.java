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
import org.odk.collect.android.preferences.PreferenceKeys;
import org.odk.collect.android.tasks.SmapRemoteWebServiceTask;
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
        mServerUrlBase = sharedPreferences.getString(PreferenceKeys.KEY_SERVER_URL, null) +
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
        if (args.length != 5) {
            Timber.e("5 arguments are needed to evaluate the %s function", HANDLER_NAME);
            return "";
        }

        String dataSetName = XPathFuncExpr.toString(args[0]);
        String queriedColumn = XPathFuncExpr.toString(args[1]);
        String referenceColumn = XPathFuncExpr.toString(args[2]);
        String referenceValue = XPathFuncExpr.toString(args[3]);
        String timeoutValue = XPathFuncExpr.toString(args[4]);
        int timeout = 0;
        try {
            timeout = Integer.valueOf(timeoutValue);
        } catch (Exception e) {

        }
        if(referenceValue.length() > 0) {
            // Get the url which doubles as the cache key
            String url = mServerUrlBase + dataSetName + "/" + referenceColumn + "/" + referenceValue;

            // Get the cache results if they exist
            String data = Collect.getInstance().getRemoteData(url);
            HashMap<String, String> record = null;
            try {
                record =
                        new Gson().fromJson(data, new TypeToken<HashMap<String, String>>() {
                        }.getType());
            } catch (Exception e) {
                // no op
            }
            if (record == null) {
                // Call a webservice to get the remote record
                SmapRemoteWebServiceTask task = new SmapRemoteWebServiceTask();
                task.setSmapRemoteListener(Collect.getInstance().getFormEntryActivity());
                task.execute(url, timeoutValue);
                return "";
            } else {
                return ExternalDataUtil.nullSafe(record.get(queriedColumn));
            }
        } else {
            // No data to lookup
            return "";
        }

    }
}
