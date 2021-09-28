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

import org.javarosa.core.model.SelectChoice;
import org.javarosa.core.model.condition.EvaluationContext;
import org.javarosa.core.model.condition.IFunctionHandler;
import org.javarosa.xpath.expr.XPathFuncExpr;
import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.exception.ExternalDataException;
import org.odk.collect.android.external.ExternalDataUtil;
import org.odk.collect.android.external.ExternalSelectChoice;
import org.odk.collect.android.preferences.GeneralKeys;
import org.odk.collect.android.tasks.SmapRemoteWebServiceTask;
import org.odk.collect.android.utilities.ToastUtils;

import java.util.ArrayList;
import java.util.List;
import java.net.URLEncoder;

import timber.log.Timber;

/**
 * Get choices from the server
 */
public class SmapRemoteDataHandlerSearch implements IFunctionHandler {

    public static final String HANDLER_NAME = "lookup_choices";

    private String displayColumns;
    private final String valueColumn;
    private final String imageColumn;

    public String mServerUrlBase = null;

    public SmapRemoteDataHandlerSearch(String ident, String dColumns,
                                       String valueColumn, String imageColumn) {

        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(Collect.getInstance().getBaseContext());
        mServerUrlBase = sharedPreferences.getString(GeneralKeys.KEY_SERVER_URL, null) +
                "/lookup/choices/" + ident + "/";

        this.valueColumn = valueColumn;
        this.imageColumn = imageColumn;

        // Remove any spaces around comma separated display columns
        displayColumns = "";
        if(dColumns != null && dColumns.trim().length() > 0) {
            String[] a = dColumns.split(",");
            int idx = 0;
            for(String v : a) {
                if(idx++ > 0) {
                    displayColumns += ",";
                }
                displayColumns += v.trim();
            }
        } else {
            throw new ExternalDataException(
                    Collect.getInstance().getString(R.string.smap_no_label));
        }
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

        if (args == null || (args.length != 1 && args.length != 3 && args.length != 4 && args.length != 6)) {
            // we should never get here since it is already handled in ExternalDataUtil
            // .getSearchXPathExpression(String appearance)
            throw new ExternalDataException(
                    Collect.getInstance().getString(R.string.ext_search_wrong_arguments_error));
        }

        String searchType = null;
        String queriedColumnsParam = null;
        String queriedValue = null;
        String expression = null;
        if (args.length == 3) {
            searchType = XPathFuncExpr.toString(args[1]);
            if(!searchType.equals("eval")) {
                throw new ExternalDataException(
                        Collect.getInstance().getString(R.string.smap_eval_required, searchType));
            }
            expression = ExternalDataUtil.evaluateExpressionNodes(XPathFuncExpr.toString(args[2]), ec);
        }
        if (args.length >= 4) {
            searchType = XPathFuncExpr.toString(args[1]);
            queriedColumnsParam = XPathFuncExpr.toString(args[2]);
            queriedValue = XPathFuncExpr.toString(args[3]);
        }

        String filterColumn = null;
        String filterValue = null;
        if (args.length == 6) {
            filterColumn = XPathFuncExpr.toString(args[4]);
            filterValue = XPathFuncExpr.toString(args[5]);
        }

        String timeoutValue = "0";
        Collect app = Collect.getInstance();
        String dataSetName = XPathFuncExpr.toString(args[0]);
        ArrayList<SelectChoice> choices = new ArrayList<>();

        try {
            // Get the url which doubles as the cache key
            StringBuffer url = new StringBuffer(mServerUrlBase)
                    .append(dataSetName).append("/")
                    .append(valueColumn).append("/")
                    .append(URLEncoder.encode(displayColumns, "UTF-8"));

            // Add the parameters

            if (expression != null && expression.trim().length() > 0) {
                expression = expression.replace("##", "'");
                url.append("?expression=").append(URLEncoder.encode(expression, "UTF-8"));
            } else {
                boolean hasParam = false;
                if (searchType != null && searchType.trim().length() > 0) {
                    url.append(hasParam ? "&" : "?");
                    url.append("search_type=").append(searchType);
                    hasParam = true;
                }
                if (queriedColumnsParam != null && queriedColumnsParam.trim().length() > 0) {
                    url.append(hasParam ? "&" : "?");
                    url.append("q_column=").append(URLEncoder.encode(queriedColumnsParam, "UTF-8"));
                    hasParam = true;
                }
                if (queriedValue != null && queriedValue.trim().length() > 0) {
                    url.append(hasParam ? "&" : "?");
                    url.append("q_value=").append(URLEncoder.encode(queriedValue, "UTF-8"));
                    hasParam = true;
                }
                if (filterColumn != null && filterColumn.trim().length() > 0) {
                    url.append(hasParam ? "&" : "?");
                    url.append("f_column=").append(URLEncoder.encode(filterColumn, "UTF-8"));
                    hasParam = true;
                }
                if (filterValue != null && filterValue.trim().length() > 0) {
                    url.append(hasParam ? "&" : "?");
                    url.append("f_value=").append(URLEncoder.encode(filterValue, "UTF-8"));
                    hasParam = true;
                }
            }

            Timber.i("++++ Remote Search: %s", url.toString());
            // Get the cache results if they exist
            String urlString = url.toString();
            String data = app.getRemoteData(urlString);
            if(data != null) {
                try {
                    ArrayList<SelectChoice> serverChoices =
                            new Gson().fromJson(data, new TypeToken<ArrayList<SelectChoice>>() {
                            }.getType());
                    // Recreate the actual choice list as their is weird constructor stuff here
                    if (serverChoices != null) {
                        choices = new ArrayList<SelectChoice>();
                        for (SelectChoice sc : serverChoices) {
                            ExternalSelectChoice extChoice = new ExternalSelectChoice(sc.getLabelInnerText(), sc.getValue(), false);
                            extChoice.setIndex(sc.getIndex());
                            choices.add(extChoice);
                        }
                    }
                } catch (Exception e) {
                    ToastUtils.showLongToast(data);
                }
            } else {
                // Call a webservice to get the remote record
                Timber.i("++++ Make the call");
                app.startRemoteCall();
                SmapRemoteWebServiceTask task = new SmapRemoteWebServiceTask();
                task.setSmapRemoteListener(app.getFormEntryActivity());
                task.execute(urlString, timeoutValue, "true", null, null, "true");
            }
        } catch (Exception e) {
            Timber.e(e);
        }

        return choices;

    }

}
