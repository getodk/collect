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

import android.widget.Toast;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.SelectChoice;
import org.javarosa.core.model.condition.EvaluationContext;
import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.form.api.FormEntryCaption;
import org.javarosa.form.api.FormEntryPrompt;
import org.javarosa.model.xform.XPathReference;
import org.javarosa.xpath.XPathNodeset;
import org.javarosa.xpath.XPathParseTool;
import org.javarosa.xpath.expr.XPathExpression;
import org.javarosa.xpath.expr.XPathFuncExpr;
import org.javarosa.xpath.expr.XPathPathExpr;
import org.javarosa.xpath.parser.XPathSyntaxException;
import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.exception.ExternalDataException;
import org.odk.collect.android.exception.InvalidSyntaxException;
import org.odk.collect.android.external.handler.ExternalDataHandlerSearch;
import org.odk.collect.android.external.handler.SmapRemoteDataHandlerSearch;
import org.odk.collect.android.logic.FormInfo;
import org.odk.collect.android.javarosawrapper.FormController;
import org.odk.collect.android.utilities.TranslationHandler;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import timber.log.Timber;
/**
 * Author: Meletis Margaritis
 * Date: 30/04/13
 * Time: 09:29
 */

public final class ExternalDataUtil {

    public static final String EXTERNAL_DATA_TABLE_NAME = "externalData";
    public static final String EXTERNAL_METADATA_TABLE_NAME = "externalMetadata";
    public static final String SORT_COLUMN_NAME = "c_sortby";
    public static final String LOCAL_COLUMN_NAME = "_local";
    public static final String COLUMN_DATASET_FILENAME = "dataSetFilename";
    public static final String COLUMN_MD5_HASH = "md5Hash";

    public static final Pattern REMOTE_SEARCH_FUNCTION_REGEX = Pattern.compile("lookup_choices\\(.+\\)");     // smap
    public static final Pattern SEARCH_FUNCTION_REGEX = Pattern.compile("search\\(.+\\)");
    private static final String COLUMN_SEPARATOR = ",";
    private static final String FALLBACK_COLUMN_SEPARATOR = " ";
    public static final String JR_IMAGES_PREFIX = "jr://images/";

    private ExternalDataUtil() {

    }

    public static String toSafeColumnName(String columnName, Map<String, String> cache) {
        String cachedName = cache.get(columnName);
        if (cachedName == null) {
            String safeColumnName = toSafeColumnName(columnName);
            cache.put(columnName, safeColumnName);
            return safeColumnName;
        } else {
            return cachedName;
        }
    }

    public static String toSafeColumnName(String columnName) {
        // SCTO-567 - begin all column names with "c_" to avoid possible conflicts with
        // reserved keywords; also, escape any potentially-illegal characters
        return "c_" + columnName.trim().replaceAll("[^A-Za-z0-9_]", "_").toLowerCase(
                Locale.ENGLISH);
    }

    public static List<String> findMatchingColumnsAfterSafeningNames(String[] columnNames) {
        // key is the safe, value is the unsafe
        Map<String, String> map = new HashMap<>();
        for (String columnName : columnNames) {
            if (columnName.trim().length() > 0) {
                String safeColumn = toSafeColumnName(columnName);
                if (!map.containsKey(safeColumn)) {
                    map.put(safeColumn, columnName);
                } else {
                    return Arrays.asList(map.get(safeColumn), columnName);
                }
            }
        }
        return null;
    }

    public static XPathFuncExpr getSearchXPathExpression(String appearance) {
        if (appearance == null) {
            appearance = "";
        }
        appearance = appearance.trim();

        Matcher matcher = SEARCH_FUNCTION_REGEX.matcher(appearance);
        boolean found = matcher.find(); // smap
        if(!found) {           // smap try lookup
            matcher = REMOTE_SEARCH_FUNCTION_REGEX.matcher(appearance);
            found = matcher.find();
        }
        if (found) {
            /*
            Collect.getInstance().getDefaultTracker()
                    .send(new HitBuilders.EventBuilder()
                            .setCategory("ExternalData")
                            .setAction("search()")
                            .setLabel(Collect.getCurrentFormIdentifierHash())
                            .build());
                            */

            String function = matcher.group(0);
            try {
                XPathExpression xpathExpression = XPathParseTool.parseXPath(function);
                if (XPathFuncExpr.class.isAssignableFrom(xpathExpression.getClass())) {
                    XPathFuncExpr xpathFuncExpr = (XPathFuncExpr) xpathExpression;
                    if (xpathFuncExpr.id.name.equalsIgnoreCase(
                            ExternalDataHandlerSearch.HANDLER_NAME) ||
                            xpathFuncExpr.id.name.equalsIgnoreCase(                 // smap
                                    SmapRemoteDataHandlerSearch.HANDLER_NAME)) {
                        // also check that the args are either 1, 4 or 6.
                        if (xpathFuncExpr.args.length == 1 || xpathFuncExpr.args.length == 3    // smap add 3 params as an option
                                || xpathFuncExpr.args.length == 4 || xpathFuncExpr.args.length == 6) {
                            return xpathFuncExpr;
                        } else {
                            Toast.makeText(Collect.getInstance(),
                                    TranslationHandler.getString(Collect.getInstance(), R.string.ext_search_wrong_arguments_error),
                                    Toast.LENGTH_SHORT).show();
                            Timber.i(TranslationHandler.getString(Collect.getInstance(), R.string.ext_search_wrong_arguments_error));
                            return null;
                        }
                    } else {
                        // this might mean a problem in the regex above. Unit tests required.
                        Toast.makeText(Collect.getInstance(),
                                TranslationHandler.getString(Collect.getInstance(), R.string.ext_search_wrong_function_error, xpathFuncExpr.id.name),
                                Toast.LENGTH_SHORT).show();
                        Timber.i(TranslationHandler.getString(Collect.getInstance(), R.string.ext_search_wrong_function_error, xpathFuncExpr.id.name));
                        return null;
                    }
                } else {
                    // this might mean a problem in the regex above. Unit tests required.
                    Toast.makeText(Collect.getInstance(),
                            TranslationHandler.getString(Collect.getInstance(), R.string.ext_search_bad_function_error, function),
                            Toast.LENGTH_SHORT).show();
                    Timber.i(TranslationHandler.getString(Collect.getInstance(), R.string.ext_search_bad_function_error, function));
                    return null;
                }
            } catch (XPathSyntaxException e) {
                Toast.makeText(Collect.getInstance(),
                        TranslationHandler.getString(Collect.getInstance(), R.string.ext_search_generic_error, appearance),
                        Toast.LENGTH_SHORT).show();
                Timber.i(TranslationHandler.getString(Collect.getInstance(), R.string.ext_search_generic_error, appearance));
                return null;
            }
        } else {
            return null;
        }
    }

    public static ArrayList<SelectChoice> populateExternalChoices(FormEntryPrompt formEntryPrompt,
            XPathFuncExpr xpathfuncexpr) throws FileNotFoundException, InvalidSyntaxException {     // Smap add InvalidSyntax Exception
        ArrayList<SelectChoice> returnedChoices = new ArrayList<>();        // smap
        try {
            List<SelectChoice> selectChoices = formEntryPrompt.getSelectChoices();
            //ArrayList<SelectChoice> returnedChoices = new ArrayList<>();  // smap
            for (SelectChoice selectChoice : selectChoices) {
                String value = selectChoice.getValue();
                if (isAnInteger(value)) {
                    // treat this as a static choice
                    returnedChoices.add(selectChoice);
                } else {
                    String displayColumns = formEntryPrompt.getSelectChoiceText(selectChoice);
                    String imageColumn = formEntryPrompt.getSpecialFormSelectChoiceText(
                            selectChoice, FormEntryCaption.TEXT_FORM_IMAGE);
                    if (imageColumn != null && imageColumn.startsWith(JR_IMAGES_PREFIX)) {
                        imageColumn = imageColumn.substring(JR_IMAGES_PREFIX.length());
                    }
                    //                    if (displayColumns == null || displayColumns.trim().length() == 0) {
                    //                        throw new InvalidSyntaxException("The label column in the choices sheet
                    // appears to be empty (or has been calculated as empty).");
                    //                    }

                    FormInstance formInstance =
                            Collect.getInstance().getFormController().getFormDef().getInstance();

                    EvaluationContext baseEvaluationContext = new EvaluationContext(formInstance);
                    EvaluationContext evaluationContext = new EvaluationContext(
                            baseEvaluationContext, formEntryPrompt.getIndex().getReference());
                    // we can only add only the appropriate by querying the xPathFuncExpr.id.name
                    if (xpathfuncexpr.id.name.equalsIgnoreCase(
                            ExternalDataHandlerSearch.HANDLER_NAME)) {
                        ExternalDataManager externalDataManager =
                                Collect.getInstance().getExternalDataManager();
                        evaluationContext.addFunctionHandler(
                                new ExternalDataHandlerSearch(externalDataManager, displayColumns,
                                        value, imageColumn));
                    } else if (xpathfuncexpr.id.name.equalsIgnoreCase(SmapRemoteDataHandlerSearch.HANDLER_NAME)){       // smap
                        evaluationContext.addFunctionHandler(
                                new SmapRemoteDataHandlerSearch(Collect.getInstance().getFormId(), displayColumns,
                                        value, imageColumn));
                    }

                    Object eval = xpathfuncexpr.eval(formInstance, evaluationContext);
                    if (eval.getClass().isAssignableFrom(ArrayList.class)) {
                        @SuppressWarnings("unchecked")
                        List<SelectChoice> dynamicChoices = (ArrayList<SelectChoice>) eval;
                        for (SelectChoice dynamicChoice : dynamicChoices) {
                            returnedChoices.add(dynamicChoice);
                        }
                    } else {
                        throw new ExternalDataException(
                                TranslationHandler.getString(Collect.getInstance(), R.string.ext_search_return_error,
                                        eval.getClass().getName()));
                    }
                }
            }
            return returnedChoices;
        } catch (Exception e) {

            String fileName = String.valueOf(xpathfuncexpr.args[0].eval(null, null));

            String msg = e.getMessage();        // Smap throw SQL errors
            if(msg != null) {                   // Smap throw SQL errors
                if(msg.contains("no such column")) {
                    int idx1 = msg.indexOf("column:");
                    int idx2 = msg.indexOf('(');
                    idx1 += 7;
                    if(idx2 > idx1) {
                        String columnTitle = msg.substring(idx1, idx2).trim();
                        if(columnTitle.startsWith("c_")) {
                            columnTitle = columnTitle.substring(2);
                        }
                        throw (new InvalidSyntaxException(Collect.getInstance().getString(R.string.smap_csv_column_nf, columnTitle, fileName)));
                    }
                }
            }

            if(fileName.startsWith("linked_s")) {    // smap
                throw(e);
            } else if (!fileName.endsWith(".csv")) {
                fileName = fileName + ".csv";
            }
            FormController formController = Collect.getInstance().getFormController();
            String filePath = fileName;
            if (formController != null) {
                filePath = Collect.getInstance().getFormController().getMediaFolder() + File.separator + fileName;
            }
            if (!new File(filePath).exists()) {
                filePath += ".imported";    // smap
                if (!new File(filePath).exists()) {
                    throw new FileNotFoundException(filePath);
                }
            }

            return returnedChoices;  // smap
            // throw new ExternalDataException(e.getMessage(), e);  // smap
        }
    }

    /**
     * We could simple return new String(displayColumns + "," + valueColumn) but we want to handle
     * the cases
     * where the displayColumns (valueColumn) contain more commas than needed, in the middle, start
     * or end.
     *
     * @param valueColumn    single string to appear first.
     * @param displayColumns comma-separated string
     * @return A {@link java.util.LinkedHashMap} that contains the SQL columns as keys, and the CSV
     * columns as values
     */
    public static void createMapWithDisplayingColumns(      // smap  change signature
            LinkedHashMap<String, String> selectColumnMap,
            List<String> columnsToFetch,
            String valueColumn,
            String displayColumns) {
        valueColumn = valueColumn.trim();

        selectColumnMap.put(toSafeColumnName(valueColumn), valueColumn);    // smap
        columnsToFetch.add(toSafeColumnName(valueColumn));

        if (displayColumns != null && displayColumns.trim().length() > 0) {
            displayColumns = displayColumns.trim();

            List<String> commaSplitParts = splitTrimmed(displayColumns, COLUMN_SEPARATOR,
                    FALLBACK_COLUMN_SEPARATOR);

            for (String commaSplitPart : commaSplitParts) {
                selectColumnMap.put(toSafeColumnName(commaSplitPart), commaSplitPart);    // smap
                columnsToFetch.add(toSafeColumnName(commaSplitPart));
            }
        }

    }

    public static List<String> createListOfColumns(String columnString) {
        List<String> values = new ArrayList<>();

        List<String> commaSplitParts = splitTrimmed(columnString, COLUMN_SEPARATOR,
                FALLBACK_COLUMN_SEPARATOR);

        for (String commaSplitPart : commaSplitParts) {
            values.add(toSafeColumnName(commaSplitPart));
        }

        return values;
    }

    // smap
    public static List<String> createListOfValues(String valueString, String searchType) {
        List<String> values = new ArrayList<String>();

        // Only split values for "in" and "not in" type otherwise values that contain spaces will not work
        if(searchType.equals("in") || searchType.equals("not in")) {
            List<String> commaSplitParts = splitTrimmed(valueString, COLUMN_SEPARATOR,
                    FALLBACK_COLUMN_SEPARATOR);

            for (String commaSplitPart : commaSplitParts) {
                values.add(commaSplitPart);
            }
        } else {
            values.add(valueString);
        }

        return values;
    }

    protected static List<String> splitTrimmed(String displayColumns, String separator,
            String fallbackSeparator) {
        List<String> commaSplitParts = splitTrimmed(displayColumns, separator);

        // SCTO-584: Fall back to a space-separated list
        if (commaSplitParts.size() == 1 && displayColumns.contains(fallbackSeparator)) {
            commaSplitParts = splitTrimmed(displayColumns, fallbackSeparator);
        }
        return commaSplitParts;
    }

    protected static List<String> splitTrimmed(String text, String separator) {
        List<String> parts = new ArrayList<>();
        StringTokenizer st = new StringTokenizer(text, separator);
        while (st.hasMoreTokens()) {
            String token = st.nextToken().trim();
            if (token.length() > 0) {
                parts.add(token);
            }
        }
        return parts;
    }

    public static boolean containsAnyData(String[] row) {
        if (row == null || row.length == 0) {
            return false;
        }
        for (String value : row) {
            if (value != null && value.trim().length() > 0) {
                return true;
            }
        }
        return false;
    }

    public static String[] fillUpNullValues(String[] row, String[] headerRow) {
        String[] fullRow = new String[headerRow.length];

        for (int i = 0; i < fullRow.length; i++) {
            if (i < row.length) {
                String value = row[i];
                if (value == null) {
                    value = "";
                }
                fullRow[i] = value;
            } else {
                fullRow[i] = "";
            }
        }

        return fullRow;
    }

    public static String nullSafe(String value) {
        return value == null ? "" : value;
    }

    public static boolean isAnInteger(String value) {
        if (value == null) {
            return false;
        }

        value = value.trim();

        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /*
     * Convert placeholders for quesions into the answers of those questions
     */
    public static String evaluateExpressionNodes(String in, EvaluationContext ec) {
        StringBuilder expression = new StringBuilder("");
        if(in != null) {
            FormDef formDef = Collect.getInstance().getFormController().getFormDef();
            FormInstance formInstance = formDef.getInstance();

            String [] eList = in.split("\\s+");
            for(String s : eList) {
                if(s.startsWith("/main")) {
                    XPathPathExpr pathExpr = XPathReference.getPathExpr(s);
                    XPathNodeset xpathNodeset = pathExpr.eval(formInstance, ec);
                    Object o = XPathFuncExpr.unpack(xpathNodeset);

                    if (o.getClass() == String.class) {
                        s = XPathFuncExpr.toString(o);
                        s = "'" + s + "'";
                    } else if (o.getClass() == Date.class) {
                        Date d = (Date) o;
                        SimpleDateFormat sdf;
                        sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
                        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));   // Dates on server will be in UTC
                        s = "'" + sdf.format(d) + "'::timestamptz";
                    } else {
                        s = XPathFuncExpr.toString(o);
                    }
                } else if(s.startsWith("#{") && s.endsWith("}")) {
                    s = s.substring(2, s.length() - 1); // Convert to column name
                }
                if(expression.length() > 0) {
                    expression.append(" ");
                }
                expression.append(s);
            }
        }
        return expression.toString();
    }
}
