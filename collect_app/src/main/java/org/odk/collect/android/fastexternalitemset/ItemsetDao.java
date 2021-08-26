/*
 * Copyright 2018 Nafundi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.odk.collect.android.fastexternalitemset;

import android.database.Cursor;
import android.database.sqlite.SQLiteException;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.SelectChoice;
import org.javarosa.core.model.condition.EvaluationContext;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.form.api.FormEntryPrompt;
import org.javarosa.xpath.XPathNodeset;
import org.javarosa.xpath.expr.XPathExpression;
import org.javarosa.xpath.parser.XPathSyntaxException;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.javarosawrapper.FormController;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class ItemsetDao {
    private static final String QUOTATION_MARK = "\"";

    private final ItemsetDbAdapter adapter;

    public ItemsetDao(ItemsetDbAdapter adapter) {
        this.adapter = adapter;
    }

    public String getItemLabel(String itemName, String mediaFolderPath, String language) {
        String itemLabel = null;

        File itemsetFile = getItemsetFile(mediaFolderPath);
        if (itemsetFile.exists()) {
            adapter.open();

            // name of the itemset table for this form
            String pathHash = ItemsetDbAdapter.getMd5FromString(itemsetFile.getAbsolutePath());
            try {
                String selection = "name=?";
                String[] selectionArgs = {itemName};

                Cursor c = adapter.query(pathHash, selection, selectionArgs);
                if (c != null) {
                    c.move(-1);
                    while (c.moveToNext()) {
                        // apparently you only need the double quotes in the
                        // column name when creating the column with a : included
                        String labelLang = "label" + "::" + language;
                        int langCol = c.getColumnIndex(labelLang);
                        if (langCol == -1) {
                            itemLabel = c.getString(c.getColumnIndex("label"));
                        } else {
                            itemLabel = c.getString(c.getColumnIndex(labelLang));
                        }

                    }
                    c.close();
                }
            } catch (SQLiteException e) {
                Timber.i(e);
            } finally {
                adapter.close();
            }
        }

        return itemLabel;
    }

    public List<SelectChoice> getItems(FormEntryPrompt formEntryPrompt, XPathParseTool pathParseTool) throws FileNotFoundException, XPathSyntaxException {
        String nodesetString = getNodesetString(formEntryPrompt);

        List<String> arguments = new ArrayList<>();
        String selectionString = getSelectionStringAndPopulateArguments(getQueryString(nodesetString), arguments);

        FormController formController = Collect.getInstance().getFormController();
        String[] selectionArgs = getSelectionArgs(arguments, nodesetString, formController, pathParseTool, formEntryPrompt);

        return selectionArgs == null ? null : getItemsFromDatabase(selectionString, selectionArgs, formController, adapter);
    }

    private String getNodesetString(FormEntryPrompt formEntryPrompt) {
        // the format of the query should be something like this:
        // query="instance('cities')/root/item[state=/data/state and county=/data/county]"
        // "query" is what we're using to notify that this is an itemset widget.
        return formEntryPrompt.getQuestion().getAdditionalAttribute(null, "query");
    }

    private String getQueryString(String nodesetStr) {
        // isolate the string between between the [ ] characters
        return nodesetStr.substring(nodesetStr.indexOf('[') + 1, nodesetStr.lastIndexOf(']'));
    }

    private String getSelectionStringAndPopulateArguments(String queryString, List<String> arguments) {
        StringBuilder selectionString = new StringBuilder();
        // add the list name as the first argument, which will always be there
        selectionString.append("list_name=?");

        // check to see if there are any arguments
        if (queryString.indexOf('=') != -1) {
            selectionString.append(" and ");
        }

        // can't just split on 'and' or 'or' because they have different
        // behavior, so loop through and break them off until we don't have any more
        // must include the spaces in indexOf so we don't match words like "land"
        int andIndex;
        int orIndex = -1;

        while ((andIndex = queryString.indexOf(" and ")) != -1 || (orIndex = queryString.indexOf(" or ")) != -1) {
            if (andIndex != -1) {
                String[] pair = queryString
                        .substring(0, andIndex)
                        .split("=");

                if (pair.length == 2) {
                    selectionString
                            .append(QUOTATION_MARK)
                            .append(pair[0].trim())
                            .append(QUOTATION_MARK)
                            .append("=? and ");

                    arguments
                            .add(pair[1]
                                    .trim());
                }
                // move string forward to after " and "
                queryString = queryString.substring(andIndex + 5, queryString.length());
            } else {
                String subString = queryString.substring(0, orIndex);
                String[] pair = subString.split("=");

                if (pair.length == 2) {
                    selectionString
                            .append(QUOTATION_MARK)
                            .append(pair[0].trim())
                            .append(QUOTATION_MARK)
                            .append("=? or ");
                    arguments.add(pair[1].trim());
                }
                // move string forward to after " or "
                queryString = queryString.substring(orIndex + 4, queryString.length());
            }
        }

        // parse the last segment (or only segment if there are no 'and' or 'or' clauses
        String[] pair = queryString.split("=");
        if (pair.length == 2) {
            selectionString
                    .append(QUOTATION_MARK)
                    .append(pair[0].trim())
                    .append(QUOTATION_MARK)
                    .append("=?");
            arguments.add(pair[1].trim());
        }
        return selectionString.toString();
    }

    @SuppressWarnings("PMD.AvoidThrowingNewInstanceOfSameException")
    private String[] getSelectionArgs(List<String> arguments, String nodesetStr, FormController formController, XPathParseTool pathParseTool, FormEntryPrompt formEntryPrompt) throws XPathSyntaxException {
        // +1 is for the list_name
        String[] selectionArgs = new String[arguments.size() + 1];

        // parse out the list name, between the ''
        String listName = nodesetStr.substring(nodesetStr.indexOf('\'') + 1, nodesetStr.lastIndexOf('\''));

        selectionArgs[0] = listName; // first argument is always listname

        if (formController == null) {
            Timber.w("Can't instantiate ItemsetWidget with a null FormController.");
            return null;
        }

        // loop through the arguments, evaluate any expressions and build the query string for the DB
        for (int i = 0; i < arguments.size(); i++) {
            XPathExpression xpr;
            try {
                xpr = pathParseTool.parseXPath(arguments.get(i));
            } catch (XPathSyntaxException e) {
                throw new XPathSyntaxException(arguments.get(i));
            }

            if (xpr != null) {
                FormDef form = formController.getFormDef();
                TreeElement treeElement = form.getMainInstance().resolveReference(
                        formEntryPrompt.getIndex().getReference());
                EvaluationContext ec = new EvaluationContext(form.getEvaluationContext(),
                        treeElement.getRef());
                Object value = xpr.eval(form.getMainInstance(), ec);

                if (value == null) {
                    return null;
                } else {
                    if (value instanceof XPathNodeset) {
                        value = ((XPathNodeset) value).getValAt(0);
                    }
                    selectionArgs[i + 1] = value.toString();
                }
            }
        }
        return selectionArgs;
    }

    private List<SelectChoice> getItemsFromDatabase(String selection, String[] selectionArgs, FormController formController, ItemsetDbAdapter adapter) throws FileNotFoundException {
        List<SelectChoice> items = new ArrayList<>();

        File itemsetFile = getItemsetFile(formController.getMediaFolder().getAbsolutePath());

        if (itemsetFile.exists()) {
            adapter.open();

            // name of the itemset table for this form
            String pathHash = ItemsetDbAdapter.getMd5FromString(itemsetFile.getAbsolutePath());
            try {
                Cursor c = adapter.query(pathHash, selection, selectionArgs);
                if (c != null) {
                    c.move(-1);
                    int index = 0;
                    while (c.moveToNext()) {
                        String label;
                        String val;

                        // try to get the value associated with the label:lang
                        // string if that doen't exist, then just use label
                        String lang = "";
                        if (formController.getLanguages() != null && formController.getLanguages().length > 0) {
                            lang = formController.getLanguage();
                        }

                        // apparently you only need the double quotes in the
                        // column name when creating the column with a : included
                        String labelLang = "label" + "::" + lang;
                        int langCol = c.getColumnIndex(labelLang);
                        if (langCol == -1) {
                            label = c.getString(c.getColumnIndex("label"));
                        } else {
                            label = c.getString(c.getColumnIndex(labelLang));
                        }

                        val = c.getString(c.getColumnIndex("name"));
                        SelectChoice selectChoice = new SelectChoice(null, label, val, false);
                        selectChoice.setIndex(index);
                        items.add(selectChoice);
                        index++;
                    }
                    c.close();
                }
            } catch (SQLiteException e) {
                Timber.i(e);
            } finally {
                adapter.close();
            }
        } else {
            throw new FileNotFoundException(itemsetFile.getAbsolutePath());
        }
        return items;
    }

    public File getItemsetFile(String mediaFolderPath) {
        return new File(mediaFolderPath + "/itemsets.csv");
    }
}
