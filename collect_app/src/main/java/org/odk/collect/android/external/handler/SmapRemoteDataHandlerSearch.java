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
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.javarosa.core.model.SelectChoice;
import org.javarosa.core.model.condition.EvaluationContext;
import org.javarosa.core.model.condition.IFunctionHandler;
import org.javarosa.xpath.expr.XPathFuncExpr;
import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.exception.ExternalDataException;
import org.odk.collect.android.external.ExternalDataManager;
import org.odk.collect.android.external.ExternalDataUtil;
import org.odk.collect.android.external.ExternalSQLiteOpenHelper;
import org.odk.collect.android.external.ExternalSelectChoice;
import org.odk.collect.android.preferences.PreferenceKeys;
import org.odk.collect.android.tasks.SmapRemoteWebServiceTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import static org.odk.collect.android.external.handler.ExternalDataSearchType.IN;

/**
 * Get choices from the server
 */
public class SmapRemoteDataHandlerSearch implements IFunctionHandler {

    public static final String HANDLER_NAME = "lookup_choices";

    private final String displayColumns;
    private final String valueColumn;
    private final String imageColumn;

    public String mServerUrlBase = null;

    public SmapRemoteDataHandlerSearch(String ident, String displayColumns,
                                       String valueColumn, String imageColumn) {

        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(Collect.getInstance().getBaseContext());
        mServerUrlBase = sharedPreferences.getString(PreferenceKeys.KEY_SERVER_URL, null) +
                "/lookup/choices/" + ident + "/";

        this.displayColumns = displayColumns;
        this.valueColumn = valueColumn;
        this.imageColumn = imageColumn;
    }

    public String getDisplayColumns() {
        return displayColumns;
    }

    public String getValueColumn() {
        return valueColumn;
    }

    public String getImageColumn() {
        return imageColumn;
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
        if (args == null || (args.length != 1 && args.length != 4 && args.length != 6)) {
            // we should never get here since it is already handled in ExternalDataUtil
            // .getSearchXPathExpression(String appearance)
            throw new ExternalDataException(
                    Collect.getInstance().getString(R.string.ext_search_wrong_arguments_error));
        }

        String searchType = null;

        String queriedColumnsParam = null;
        List<String> queriedColumns = null;
        List<String> queriedValues = null;      // smap
        String queriedValue = null;
        if (args.length >= 4) {
            searchType = XPathFuncExpr.toString(args[1]);
            queriedColumnsParam = XPathFuncExpr.toString(args[2]);
            queriedValue = XPathFuncExpr.toString(args[3]);
        }

        ExternalDataSearchType externalDataSearchType = ExternalDataSearchType.getByKeyword(
                searchType, ExternalDataSearchType.CONTAINS);

        boolean searchRows = false;
        boolean useFilter = false;

        if (queriedColumnsParam != null && queriedColumnsParam.trim().length() > 0) {
            searchRows = true;
            queriedColumns = ExternalDataUtil.createListOfColumns(queriedColumnsParam);
        }

        // smap
        if(queriedValue != null) {
            queriedValues = ExternalDataUtil.createListOfValues(queriedValue, externalDataSearchType.getKeyword().trim());
        }

        String filterColumn = null;
        String filterValue = null;
        if (args.length == 6) {
            filterColumn = XPathFuncExpr.toString(args[4]);
            filterValue = XPathFuncExpr.toString(args[5]);
            useFilter = true;
        }

        String timeoutValue = "0";
        Collect app = Collect.getInstance();
        String dataSetName = XPathFuncExpr.toString(args[0]);

        LinkedHashMap<String, String> selectColumnMap =
                ExternalDataUtil.createMapWithDisplayingColumns(getValueColumn(),
                        getDisplayColumns());

        List<String> columnsToFetch = new ArrayList<String>(selectColumnMap.keySet());
        String safeImageColumn = null;
        if (getImageColumn() != null && getImageColumn().trim().length() > 0) {
            safeImageColumn = ExternalDataUtil.toSafeColumnName(getImageColumn());
            columnsToFetch.add(safeImageColumn);
        }

        String[] sqlColumns = columnsToFetch.toArray(new String[columnsToFetch.size()]);

        String selection;
        String[] selectionArgs;




        // Get the url which doubles as the cache key
        StringBuffer url = new StringBuffer(mServerUrlBase).append(dataSetName).append("/").append(valueColumn).append("/").append(displayColumns);

        // Add the parameters
        boolean hasParam = false;
        if(searchType != null && searchType.trim().length() > 0) {
            url.append(hasParam ? "&" : "?");
            url.append("search_type=").append(searchType);
        }
        if(queriedColumnsParam != null && queriedColumnsParam.trim().length() > 0) {
            url.append(hasParam ? "&" : "?");
            url.append("q_column=").append(queriedColumnsParam);
        }
        if(queriedValue != null && queriedValue.trim().length() > 0) {
            url.append(hasParam ? "&" : "?");
            url.append("q_value=").append(queriedValue);
        }
        if(filterColumn != null && filterColumn.trim().length() > 0) {
            url.append(hasParam ? "&" : "?");
            url.append("f_column=").append(filterColumn);
        }
        if(filterValue != null && filterValue.trim().length() > 0) {
            url.append(hasParam ? "&" : "?");
            url.append("f_value=").append(filterValue);
        }

        // Get the cache results if they exist
        String urlString = url.toString();
        String data = app.getRemoteData(urlString);
        ArrayList<SelectChoice> choices = null;
        try {
            choices =
                    new Gson().fromJson(data, new TypeToken<ArrayList<SelectChoice>>() {
                    }.getType());
        } catch (Exception e) {
            return data;            // Assume the data contains the error message
        }
        if (choices == null) {
            // Call a webservice to get the remote record
            app.startRemoteCall(urlString);
            SmapRemoteWebServiceTask task = new SmapRemoteWebServiceTask();
            task.setSmapRemoteListener(app.getFormEntryActivity());
            task.execute(urlString, timeoutValue, "true");
            return null;
        } else {
            return choices;
        }

    }




}
