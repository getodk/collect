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

package org.odk.collect.android.externaldata.handler;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import org.javarosa.core.model.condition.EvaluationContext;
import org.javarosa.xpath.expr.XPathFuncExpr;
import org.odk.collect.android.externaldata.ExternalDataManager;
import org.odk.collect.android.externaldata.ExternalDataUtil;
import org.odk.collect.android.externaldata.ExternalSQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

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
            Timber.e("4 arguments are needed to evaluate the %s function", HANDLER_NAME);
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
            ExternalSQLiteOpenHelper sqLiteOpenHelper = getExternalDataManager().getDatabase(
                    dataSetName, false);
            if (sqLiteOpenHelper == null) {
                return "";
            }

            SQLiteDatabase db = sqLiteOpenHelper.getReadableDatabase();
            String[] columns = {ExternalDataUtil.toSafeColumnName(queriedColumn)};
            String selection = ExternalDataUtil.toSafeColumnName(referenceColumn) + "=?";
            String[] selectionArgs = {referenceValue};

            c = db.query(ExternalDataUtil.EXTERNAL_DATA_TABLE_NAME, columns, selection,
                    selectionArgs, null, null, null);
            if (c.getCount() > 0) {
                c.moveToFirst();
                return ExternalDataUtil.nullSafe(c.getString(0));
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
}
