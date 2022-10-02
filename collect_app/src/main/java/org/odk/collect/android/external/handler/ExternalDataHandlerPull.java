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
import org.odk.collect.android.database.SqlFrag;
import org.odk.collect.android.database.SqlFragParam;
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

    // Valid pulldata functions
    public static final String FN_COUNT = "count";
    public static final String FN_LIST = "list";
    public static final String FN_INDEX = "index";
    public static final String FN_SUM = "sum";
    public static final String FN_MAX = "max";
    public static final String FN_MIN = "min";
    public static final String FN_MEAN = "mean";



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

        /*
         * smap
         * There are 3 variants of pulldata with 3, 4 and 6 parameters
         * 3 parameters:  selection of a single record defined by sql like expression
         * 4 parameters: selection of single record defined by specification of a column and a value
         * 5 parameters: Selects multiple records or applies a dunction to multiple records filtered suing an sql like expression
         * 6 parameters: handles multiple records defined by specification of a column and a value
         */
        if (args.length < 3 || args.length > 6) {     // smap add support for additional parameter combinations
            Timber.e("3, 4, 5 or 6 arguments are needed to evaluate the %s function", HANDLER_NAME);  // smap 5th, 6th parameter
            return "";
        }

        // smap common parameters
        String dataSetName = XPathFuncExpr.toString(args[0]);
        String queriedColumn = XPathFuncExpr.toString(args[1]);

        String filter = null;
        String referenceColumn = null;
        String referenceValue = null;
        boolean multiSelect = (args.length == 5 || args.length == 6);
        int index = 0;
        String fn = null;    // count || list || index || sum || max || min || mean
        String searchType = null;  // matches || endswith || startswith || contains

        if(args.length == 3) {
            filter = ExternalDataUtil.evaluateExpressionNodes(XPathFuncExpr.toString(args[2]), ec);
        } else if(args.length == 4) {
            referenceColumn = XPathFuncExpr.toString(args[2]);
            referenceValue = XPathFuncExpr.toString(args[3]);
        } else if(args.length == 5) {
            filter = ExternalDataUtil.evaluateExpressionNodes(XPathFuncExpr.toString(args[2]), ec);
            fn = XPathFuncExpr.toString(args[3]).toLowerCase();
        } else if(args.length == 6) {
            referenceColumn = XPathFuncExpr.toString(args[2]);
            referenceValue = XPathFuncExpr.toString(args[3]);
            fn = XPathFuncExpr.toString(args[4]).toLowerCase();
            searchType = XPathFuncExpr.toString(args[5]);
        }

        SqlFrag filterFrag = null;
        if(filter != null && filter.length() > 0) {
            filterFrag = new SqlFrag();
            try {
                filterFrag.addSqlFragment(filter, false, null, 0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if(multiSelect) {
            try {

                // Support legacy function values
                if(fn.equals("-1")) { // legacy
                    fn = FN_COUNT;
                } else if(fn.equals("0")) { // legacy
                    fn = FN_LIST;
                }

                /*
                 * If the function is a number greater than 0 then set the function to "index",
                 * if less than 0 set it to "count" otherwise if equal to 0 then it should be "list"
                 * if it is not a number then it will not be changed
                 */
                try {
                    index = Integer.valueOf(fn);
                    if(index > 0) {
                        fn = FN_INDEX;
                    } else if(index < 0) {
                        fn = FN_COUNT;
                    } else {
                        fn = FN_LIST;
                    }
                } catch (Exception e) {

                }
            } catch (Exception e) {
                fn = FN_LIST;        // default
            }

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

            // Add the where clause
            String selection = null;
            String [] selectionArgs;
            if(filterFrag != null) {
                selection = filterFrag.sql.toString();
                selectionArgs = new String[filterFrag.params.size()];
                int idx = 0;
                for(SqlFragParam param : filterFrag.params) {
                    selectionArgs[idx++] = SqlFrag.getParamValue(param);
                }
            } else {  // Add default matches query
                selection = ExternalDataUtil.toSafeColumnName(referenceColumn) + "=?";
                selectionArgs = new String[1];
                selectionArgs[0] = referenceValue;
            }

            String sortBy = ExternalDataUtil.SORT_COLUMN_NAME; // smap add sorting

            // smap start - Add user specified selection if it is not matches
            if(multiSelect && searchType != null && !searchType.equals("eval")) {
                ExternalDataSearchType externalDataSearchType = ExternalDataSearchType.getByKeyword(
                        searchType, ExternalDataSearchType.CONTAINS);
                List<String> referenceValues = ExternalDataUtil.createListOfValues(referenceValue, externalDataSearchType.getKeyword().trim());
                List<String> referenceColumns = ExternalDataUtil.createListOfColumns(referenceColumn);
                selection = createMultiSelectExpression(referenceColumns, referenceValues, externalDataSearchType);
                selectionArgs = externalDataSearchType.constructLikeArguments(referenceValues);
            }
            // smap end

            // Prevent null values from being returned
            selection += " and " + ExternalDataUtil.toSafeColumnName(queriedColumn) + " != 'null' ";
            c = db.query(ExternalDataUtil.EXTERNAL_DATA_TABLE_NAME, columns, selection,
                    selectionArgs, null, null, sortBy);
            if (c.getCount() > 0) {
                if(!multiSelect) {  // smap - use original processing if the   original 4 parameter format is used
                    c.moveToFirst();
                    return ExternalDataUtil.nullSafe(c.getString(0));
                } else {  // smap
                    StringBuilder sResult = new StringBuilder("");
                    int count = 0;
                    Double dResult = 0.0;
                    if(fn.equals(FN_MIN)) {
                        dResult = Double.MAX_VALUE;
                    }

                    if(fn.equals(FN_COUNT)) {
                        sResult.append(c.getCount());
                    } else if(fn.equals(FN_INDEX)) {    // Get 1
                        c.moveToPosition(index - 1);        // If index is 1 get the first
                        sResult.append(ExternalDataUtil.nullSafe(c.getString(0)));
                    } else {   // Apply the function to the data
                        c.moveToPosition(-1);
                        while (c.moveToNext()) {
                            if(fn.equals(FN_LIST)) {
                                if (count++ > 0) {
                                    sResult.append(" ");
                                }
                                sResult.append(ExternalDataUtil.nullSafe(c.getString(0)));
                            } else {
                                // Numeric calculation
                                count++;
                                String sVal = ExternalDataUtil.nullSafe(c.getString(0));
                                Double dVal = 0.0;
                                try {
                                    dVal = Double.valueOf(sVal);
                                } catch (Exception e) {

                                }
                                if(fn.equals(FN_SUM) || fn.equals(FN_MEAN)) {
                                    dResult += dVal;
                                } else if(fn.equals(FN_MAX) && dVal > dResult) {
                                    dResult = dVal;
                                } else if(fn.equals(FN_MIN) && dVal < dResult) {
                                    dResult = dVal;
                                }
                            }
                        }
                    }
                    if(fn.equals(FN_COUNT) || fn.equals(FN_INDEX) || fn.equals(FN_LIST) ) {
                        return sResult.toString();
                    } else if(fn.equals(FN_MEAN)) {
                        if(count == 0) {
                            return "";
                        } else {
                            return String.valueOf(dResult / count);
                        }
                    } else {
                        return String.valueOf(dResult);
                    }
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
