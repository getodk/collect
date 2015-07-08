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

package org.odk.collect.android.external;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.javarosa.core.model.condition.EvaluationContext;
import org.javarosa.core.model.data.DecimalData;
import org.javarosa.core.model.data.IntegerData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.model.xform.XPathReference;
import org.javarosa.xpath.XPathNodeset;
import org.javarosa.xpath.XPathParseTool;
import org.javarosa.xpath.expr.XPathExpression;
import org.javarosa.xpath.expr.XPathFuncExpr;
import org.javarosa.xpath.expr.XPathPathExpr;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.exception.ExternalParamsException;
import org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns;

import android.content.Intent;
import android.database.Cursor;

/**
 *
 * Author: Meletis Margaritis
 * Date: 30/07/13
 * Time: 10:44
 */
public class ExternalAppsUtils {

    private static final String LEFT_PARENTHESIS = "(";
    private static final String RIGHT_PARENTHESIS = ")";

    public static String extractIntentName(String exString) {
        if (!exString.contains(LEFT_PARENTHESIS)) {
            if (exString.contains(RIGHT_PARENTHESIS)) {
                return exString.substring(0, exString.indexOf(RIGHT_PARENTHESIS)).trim();
            } else {
                return exString;
            }
        }

        int leftParIndex = exString.indexOf(LEFT_PARENTHESIS);
        return exString.substring(0, leftParIndex).trim();
    }

    public static Map<String, String> extractParameters(String exString) {
        exString = exString.trim();

        int leftParIndex = exString.indexOf(LEFT_PARENTHESIS);
        if (leftParIndex == -1) {
            return Collections.emptyMap();
        }

        String paramsStr;
        if (exString.endsWith(")")) {
            paramsStr = exString.substring(leftParIndex + 1, exString.lastIndexOf(")"));
        } else {
            paramsStr = exString.substring(leftParIndex + 1, exString.length());
        }

        Map<String, String> parameters = new LinkedHashMap<String, String>();
        String[] paramsPairs = paramsStr.trim().split(",");
        for (String paramsPair : paramsPairs) {
            String[] keyValue = paramsPair.trim().split("=");
            if (keyValue.length == 2) {
                parameters.put(keyValue[0].trim(), keyValue[1].trim());
            }
        }
        return parameters;
    }

    public static void populateParameters(Intent intent, Map<String, String> exParams, TreeReference reference) throws ExternalParamsException {
        FormInstance formInstance = Collect.getInstance().getFormController().getFormDef().getInstance();
        EvaluationContext baseEvaluationContext = new EvaluationContext(formInstance);
        EvaluationContext evaluationContext = new EvaluationContext(baseEvaluationContext, reference);

        if (exParams != null) {
            for (Map.Entry<String, String> paramEntry : exParams.entrySet()) {
                String paramEntryValue = paramEntry.getValue();

                try {
                    Object result;
                    if (paramEntryValue.startsWith("'")) {
                        // treat this as a constant parameter
                        // but not require an ending quote
                        if (paramEntryValue.endsWith("'")) {
                            result = paramEntryValue.substring(1, paramEntryValue.length() - 1);
                        } else {
                            result = paramEntryValue.substring(1, paramEntryValue.length());
                        }
                    } else if (paramEntryValue.startsWith("/")) {
                        // treat this is an xpath
                        XPathPathExpr pathExpr = XPathReference.getPathExpr(paramEntryValue);
                        XPathNodeset xPathNodeset = pathExpr.eval(formInstance, evaluationContext);
                        result = XPathFuncExpr.unpack(xPathNodeset);
                    } else if (paramEntryValue.equals("instanceProviderID()")) {
                        // instanceProviderID returns -1 if the current instance has not been
                        // saved to disk already
                        String path = Collect.getInstance().getFormController().getInstancePath().getAbsolutePath();
                        String selection = InstanceColumns.INSTANCE_FILE_PATH + "=?";
                        String selectionArgs[] = {path};
                        
                        String instanceProviderID = "-1";
                        Cursor c = Collect.getInstance().getContentResolver().query(InstanceColumns.CONTENT_URI, null, selection, selectionArgs, null);
                        if (c != null && c.getCount() > 0) {
                            // should only ever be one
                            c.moveToFirst();
                            instanceProviderID = c.getString(c.getColumnIndex(InstanceColumns._ID));
                        }
                        if (c != null) {
                            c.close();
                        }
                       
                        result = instanceProviderID;
                    } else {
                        // treat this is a function
                        XPathExpression xPathExpression = XPathParseTool.parseXPath(paramEntryValue);
                        result = xPathExpression.eval(formInstance, evaluationContext);
                    }
                    if (result != null && result instanceof Serializable) {
                        intent.putExtra(paramEntry.getKey(), (Serializable) result);
                    }
                } catch (Exception e) {
                    throw new ExternalParamsException("Could not evaluate '" + paramEntryValue + "'", e);
                }
            }
        }
    }

    public static StringData asStringData(Object value) {
        if (value == null) {
            return null;
        } else {
            return new StringData(value.toString());
        }
    }

    public static IntegerData asIntegerData(Object value) {
        if (value == null) {
            return null;
        } else {
            try {
                String s = value.toString();
                int i = Integer.parseInt(s);
                return new IntegerData(i);
            } catch (NumberFormatException e) {
                return null;
            }
        }
    }

    public static DecimalData asDecimalData(Object value) {
        if (value == null) {
            return null;
        } else {
            try {
                String s = value.toString();
                double d = Double.parseDouble(s);
                return new DecimalData(d);
            } catch (NumberFormatException e) {
                return null;
            }
        }
    }
}
