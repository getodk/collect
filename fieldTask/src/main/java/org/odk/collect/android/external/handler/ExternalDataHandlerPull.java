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

import java.util.ArrayList;
import java.util.List;

import org.javarosa.core.model.condition.EvaluationContext;
import org.javarosa.xpath.expr.XPathFuncExpr;
import org.odk.collect.android.external.ExternalDataManager;
import org.odk.collect.android.external.ExternalDataUtil;
import org.odk.collect.android.external.ExternalSQLiteOpenHelper;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

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
        if (args.length != 4) {
            Log.e(ExternalDataUtil.LOGGER_NAME, "4 arguments are needed to evaluate the " + HANDLER_NAME + " function");
            return "";
        }

        String dataSetName = XPathFuncExpr.toString(args[0]);
        String queriedColumn = XPathFuncExpr.toString(args[1]);
        String referenceColumn = XPathFuncExpr.toString(args[2]);
        String referenceValue = XPathFuncExpr.toString(args[3]);

        // SCTO-545
        dataSetName = normalize(dataSetName);

        Cursor c = null;
        try {

            ExternalSQLiteOpenHelper sqLiteOpenHelper = getExternalDataManager().getDatabase(dataSetName, false);
            if (sqLiteOpenHelper == null) {
                return "";
            }

            SQLiteDatabase db = sqLiteOpenHelper.getReadableDatabase();
            String[] columns = {ExternalDataUtil.toSafeColumnName(queriedColumn)};
            String selection = ExternalDataUtil.toSafeColumnName(referenceColumn) + "=?";
            String[] selectionArgs = {referenceValue};

            c = db.query(ExternalDataUtil.EXTERNAL_DATA_TABLE_NAME, columns, selection, selectionArgs, null, null, null);
            if (c.getCount() > 0) {
                c.moveToFirst();
                return ExternalDataUtil.nullSafe(c.getString(0));
            } else {
                Log.e(ExternalDataUtil.LOGGER_NAME, "Could not find a value in " + queriedColumn + " where the column " + referenceColumn + " has the value " + referenceValue);
                return "";
            }
        } catch (Exception e) {
            Log.e(ExternalDataUtil.LOGGER_NAME, e.getMessage());
            return "";
        } finally {
            if (c != null) {
                c.close();
            }
        }
    }
}
