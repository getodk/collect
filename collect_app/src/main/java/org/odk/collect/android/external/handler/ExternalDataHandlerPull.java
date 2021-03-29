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

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import org.javarosa.core.model.condition.EvaluationContext;
import org.javarosa.xpath.expr.XPathFuncExpr;
import org.odk.collect.android.external.ExternalDataManager;
import org.odk.collect.android.external.ExternalDataUtil;
import org.odk.collect.android.external.ExternalSQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

import static org.odk.collect.android.external.handler.ExternalDataSearchType.IN;
import static org.odk.collect.android.external.handler.ExternalDataSearchType.NOT_IN;

/**
 * Author: Meletis Margaritis
 * Date: 25/04/13
 * Time: 13:50
 */
public class ExternalDataHandlerPull extends ExternalDataHandlerBase {

    public static final String HANDLER_NAME = "pulldata";

    public ExternalDataHandlerPull(ExternalDataManager externalDataManager) {
        super(externalDataManager);
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
        /* smap
        Collect.getInstance().getDefaultTracker()
                .send(new HitBuilders.EventBuilder()
                        .setCategory("ExternalData")
                        .setAction("pulldata()")
                        .setLabel(Collect.getCurrentFormIdentifierHash())
                        .build());
                        */

        if (args.length != 4 && args.length != 6) {     // smap add support for 5th and 6th parameter
            Timber.e("4 or 6 arguments are needed to evaluate the %s function", HANDLER_NAME);  // smap 5th, 6th parameter
            return "";
        }

        String dataSetName = XPathFuncExpr.toString(args[0]);
        String queriedColumn = XPathFuncExpr.toString(args[1]);
        String referenceColumn = XPathFuncExpr.toString(args[2]);
        String referenceValue = XPathFuncExpr.toString(args[3]);

        // start smap
        boolean multiSelect = (args.length == 6);
        int index = 0;
        String searchType = null;
        if(multiSelect) {
            try {
                index = Integer.valueOf(XPathFuncExpr.toString(args[4]));
            } catch (Exception e) {
            }
            searchType = XPathFuncExpr.toString(args[5]);
        }
        // smap

        // SCTO-545
        dataSetName = normalize(dataSetName);

        Cursor c = null;
        try {
            ExternalSQLiteOpenHelper sqLiteOpenHelper = getExternalDataManager().getDatabase(
                    dataSetName, false);
            if (sqLiteOpenHelper == null) {
                return "";
            }

            SQLiteDatabase db = sqLiteOpenHelper.getReadableDatabase();
            String[] columns = {ExternalDataUtil.toSafeColumnName(queriedColumn)};
            String selection = ExternalDataUtil.toSafeColumnName(referenceColumn) + "=?";
            String[] selectionArgs = {referenceValue};
            String sortBy = ExternalDataUtil.SORT_COLUMN_NAME; // smap add sorting

            // smap start - Add user specified selection if it is not matches
            if(multiSelect && !searchType.equals("matches")) {
                ExternalDataSearchType externalDataSearchType = ExternalDataSearchType.getByKeyword(
                        searchType, ExternalDataSearchType.CONTAINS);
                List<String> referenceValues = ExternalDataUtil.createListOfValues(referenceValue, externalDataSearchType.getKeyword().trim());
                List<String> referenceColumns = ExternalDataUtil.createListOfColumns(referenceColumn);
                selection = createMultiSelectExpression(referenceColumns, referenceValues, externalDataSearchType);
                selectionArgs = externalDataSearchType.constructLikeArguments(referenceValues);
            }
            // smap end

            c = db.query(ExternalDataUtil.EXTERNAL_DATA_TABLE_NAME, columns, selection,
                    selectionArgs, null, null, sortBy);
            if (c.getCount() > 0) {
                if(!multiSelect) {  // smap - use original processing if the   original 4 parameter format is used
                    c.moveToFirst();
                    return ExternalDataUtil.nullSafe(c.getString(0));
                } else {  // smap
                    StringBuilder result = new StringBuilder("");
                    if(index < 0) {
                        result.append(c.getCount());
                    } else if(index == 0) {   // Get all
                        c.moveToPosition(-1);
                        int count = 0;
                        while (c.moveToNext()) {
                            if(count++ > 0) {
                                result.append(" ");
                            }
                            result.append(ExternalDataUtil.nullSafe(c.getString(0)));
                        }
                    } else {    // Get 1
                        c.moveToPosition(index - 1);        // If index is 1 get the first
                        result.append(ExternalDataUtil.nullSafe(c.getString(0)));
                    }
                    return result.toString();
                }
            } else {
                Timber.i("Could not find a value in %s where the column %s has the value %s",
                        queriedColumn, referenceColumn, referenceValue);
                return "";
            }
        } catch (SQLiteException e) {
            Timber.i(e);
            return "";
        } finally {
            if (c != null) {
                c.close();
            }
        }
    }

    /*
     * smap
     */
    protected String createMultiSelectExpression(List<String> queriedColumns,
                                          List<String> queriedValues, ExternalDataSearchType type) {
        StringBuilder sb = new StringBuilder();
        if(type.equals(IN) && queriedColumns.size() > 0) {    // smap
            sb.append(queriedColumns.get(0)).append(" in (");
            int idx = 0;
            for (String queriedValue : queriedValues) {
                if (idx++ > 0) {
                    sb.append(", ");
                }
                sb.append("?");
            }
            sb.append(")");
        } else if(type.equals(NOT_IN) && queriedColumns.size() > 0) {    // smap
            sb.append(queriedColumns.get(0)).append(" not in (");
            int idx = 0;
            for (String queriedValue : queriedValues) {
                if (idx++ > 0) {
                    sb.append(", ");
                }
                sb.append("?");
            }
            sb.append(")");
        } else {
            for (String queriedColumn : queriedColumns) {
                if (sb.length() > 0) {
                    sb.append(" OR ");
                }
                sb.append(queriedColumn).append(" LIKE ? ");
            }
        }
        return sb.toString();
    }
}
