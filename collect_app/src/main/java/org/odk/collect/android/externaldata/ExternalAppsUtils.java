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

package org.odk.collect.android.externaldata;

import android.content.Intent;

import org.javarosa.core.model.FormDef;
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
import org.javarosa.xpath.parser.XPathSyntaxException;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.exception.ExternalParamsException;
import org.odk.collect.forms.instances.Instance;
import org.odk.collect.android.utilities.InstancesRepositoryProvider;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Author: Meletis Margaritis
 * Date: 30/07/13
 * Time: 10:44
 */
public class ExternalAppsUtils {

    private static final String LEFT_PARENTHESIS = "(";
    private static final String RIGHT_PARENTHESIS = ")";

    private ExternalAppsUtils() {
    }

    public static String extractIntentName(String exString) {
        if (!exString.contains(LEFT_PARENTHESIS)) {
            return exString.contains(RIGHT_PARENTHESIS)
                    ? exString.substring(0, exString.indexOf(RIGHT_PARENTHESIS)).trim()
                    : exString;
        }

        return exString.substring(0, exString.indexOf(LEFT_PARENTHESIS)).trim();
    }

    public static Map<String, String> extractParameters(String exString) {
        exString = exString.trim();

        int leftParIndex = exString.indexOf(LEFT_PARENTHESIS);
        if (leftParIndex == -1) {
            return Collections.emptyMap();
        }

        String paramsStr = exString.endsWith(")")
                ? exString.substring(leftParIndex + 1, exString.lastIndexOf(')'))
                : exString.substring(leftParIndex + 1);

        Map<String, String> parameters = new LinkedHashMap<>();
        List<String> paramsPairs = getParamPairs(paramsStr.trim());
        for (String paramsPair : paramsPairs) {
            String[] keyValue = paramsPair.trim().split("=");
            if (keyValue.length == 2) {
                parameters.put(keyValue[0].trim(), keyValue[1].trim());
            }
        }
        return parameters;
    }

    private static List<String> getParamPairs(String paramsStr) {
        List<String> paramPairs = new ArrayList<>();
        int startPos = 0;
        boolean inQuotes = false;
        for (int current = 0; current < paramsStr.length(); current++) {
            if (paramsStr.charAt(current) == '\'') {
                inQuotes = !inQuotes;
            }

            if (current == paramsStr.length() - 1) {
                paramPairs.add(paramsStr.substring(startPos));
            } else if (paramsStr.charAt(current) == ',' && !inQuotes) {
                paramPairs.add(paramsStr.substring(startPos, current));
                startPos = current + 1;
            }
        }

        return paramPairs;
    }

    public static void populateParameters(Intent intent, Map<String, String> exParams,
            TreeReference reference) throws ExternalParamsException {
        if (exParams != null) {
            for (Map.Entry<String, String> paramEntry : exParams.entrySet()) {
                String paramEntryValue = paramEntry.getValue();
                try {
                    Object result = getValueRepresentedBy(paramEntry.getValue(), reference);

                    if (result != null && result instanceof Serializable) {
                        intent.putExtra(paramEntry.getKey(), (Serializable) result);
                    }
                } catch (Exception e) {
                    throw new ExternalParamsException(
                            "Could not evaluate '" + paramEntryValue + "'", e);
                }
            }
        }
    }

    public static Object getValueRepresentedBy(String text, TreeReference reference) throws XPathSyntaxException {
        if (text.startsWith("'")) {
            // treat this as a constant parameter but not require an ending quote
            return text.endsWith("'") ? text.substring(1, text.length() - 1) : text.substring(1);
        }

        FormDef formDef = Collect.getInstance().getFormController().getFormDef();
        FormInstance formInstance = formDef.getInstance();
        EvaluationContext evaluationContext = new EvaluationContext(formDef.getEvaluationContext(), reference);
        if (text.startsWith("/")) {
            // treat this is an xpath
            XPathPathExpr pathExpr = XPathReference.getPathExpr(text);
            XPathNodeset xpathNodeset = pathExpr.eval(formInstance, evaluationContext);
            return XPathFuncExpr.unpack(xpathNodeset);
        } else if (text.equals("instanceProviderID()")) {
            // instanceProviderID returns -1 if the current instance has not been saved to disk already
            String path = Collect.getInstance().getFormController().getInstanceFile().getAbsolutePath();

            String instanceProviderID = "-1";
            Instance instance = new InstancesRepositoryProvider(Collect.getInstance()).get().getOneByPath(path);
            if (instance != null) {
                instanceProviderID = instance.getDbId().toString();
            }

            return instanceProviderID;
        } else {
            // treat this as a function
            XPathExpression xpathExpression = XPathParseTool.parseXPath(text);
            return xpathExpression.eval(formInstance, evaluationContext);
        }
    }

    public static StringData asStringData(Object value) {
        return value == null ? null : new StringData(value.toString());
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
