/*
 * Copyright (C) 2009 University of Washington
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.odk.collect.android.widgets;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.condition.EvaluationContext;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.form.api.FormEntryPrompt;
import org.javarosa.xpath.XPathNodeset;
import org.javarosa.xpath.XPathParseTool;
import org.javarosa.xpath.expr.XPathExpression;
import org.javarosa.xpath.parser.XPathSyntaxException;
import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.database.ItemsetDbAdapter;

import android.content.Context;
import android.database.Cursor;
import android.view.KeyEvent;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

/**
 * The most basic widget that allows for entry of any text.
 *
 * @author Carl Hartung (carlhartung@gmail.com)
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */
public class ItemsetWidget extends QuestionWidget implements
        android.widget.CompoundButton.OnCheckedChangeListener {

    private static String tag = "ItemsetWidget";

    boolean mReadOnly;
    protected RadioGroup mButtons;
    private String mAnswer = null;
    // Hashmap linking label:value
    private HashMap<String, String> mAnswers;

    public ItemsetWidget(Context context, FormEntryPrompt prompt, boolean readOnlyOverride) {
        this(context, prompt, readOnlyOverride, true);
    }

    protected ItemsetWidget(Context context, FormEntryPrompt prompt, boolean readOnlyOverride,
            boolean derived) {
        super(context, prompt);
        mButtons = new RadioGroup(context);
        mButtons.setId(QuestionWidget.newUniqueId());
        mReadOnly = prompt.isReadOnly() || readOnlyOverride;
        mAnswers = new HashMap<String, String>();

        String currentAnswer = prompt.getAnswerText();

        // the format of the query should be something like this:
        // query="instance('cities')/root/item[state=/data/state and county=/data/county]"

        // "query" is what we're using to notify that this is an
        // itemset widget.
        String nodesetStr = prompt.getQuestion().getAdditionalAttribute(null, "query");

        // parse out the list name, between the ''
        String list_name = nodesetStr.substring(nodesetStr.indexOf("'") + 1,
                nodesetStr.lastIndexOf("'"));

        // isolate the string between between the [ ] characters
        String queryString = nodesetStr.substring(nodesetStr.indexOf("[") + 1,
                nodesetStr.lastIndexOf("]"));

        StringBuilder selection = new StringBuilder();
        // add the list name as the first argument, which will always be there
        selection.append("list_name=?");

        // check to see if there are any arguments
        if (queryString.indexOf("=") != -1) {
            selection.append(" and ");
        }

        // can't just split on 'and' or 'or' because they have different
        // behavior, so loop through and break them off until we don't have any
        // more
        // must include the spaces in indexOf so we don't match words like
        // "land"
        int andIndex = -1;
        int orIndex = -1;
        ArrayList<String> arguments = new ArrayList<String>();
        while ((andIndex = queryString.indexOf(" and ")) != -1
                || (orIndex = queryString.indexOf(" or ")) != -1) {
            if (andIndex != -1) {
                String subString = queryString.substring(0, andIndex);
                String pair[] = subString.split("=");
                if (pair.length == 2) {
                    selection.append(pair[0].trim() + "=? and ");
                    arguments.add(pair[1].trim());
                } else {
                    // parse error
                }
                // move string forward to after " and "
                queryString = queryString.substring(andIndex + 5, queryString.length());
                andIndex = -1;
            } else if (orIndex != -1) {
                String subString = queryString.substring(0, orIndex);
                String pair[] = subString.split("=");
                if (pair.length == 2) {
                    selection.append(pair[0].trim() + "=? or ");
                    arguments.add(pair[1].trim());
                } else {
                    // parse error
                }

                // move string forward to after " or "
                queryString = queryString.substring(orIndex + 4, queryString.length());
                orIndex = -1;
            }
        }

        // parse the last segment (or only segment if there are no 'and' or 'or'
        // clauses
        String pair[] = queryString.split("=");
        if (pair.length == 2) {
            selection.append(pair[0].trim() + "=?");
            arguments.add(pair[1].trim());
        }
        if (pair.length == 1) {
            // this is probably okay, because then you just list all items in
            // the list
        } else {
            // parse error
        }

        // +1 is for the list_name
        String[] selectionArgs = new String[arguments.size() + 1];

        boolean nullArgs = false; // can't have any null arguments
        selectionArgs[0] = list_name; // first argument is always listname

        // loop through the arguments, evaluate any expressions
        // and build the query string for the DB
        for (int i = 0; i < arguments.size(); i++) {
            XPathExpression xpr = null;
            try {
                xpr = XPathParseTool.parseXPath(arguments.get(i));
            } catch (XPathSyntaxException e) {
                e.printStackTrace();
                TextView error = new TextView(context);
                error.setText("XPathParser Exception:  \"" + arguments.get(i) + "\"");
                addView(error);
                break;
            }

            if (xpr != null) {
                FormDef form = Collect.getInstance().getFormController().getFormDef();
                TreeElement mTreeElement = form.getMainInstance().resolveReference(prompt.getIndex().getReference());
                EvaluationContext ec = new EvaluationContext(form.exprEvalContext,
                        mTreeElement.getRef());
                Object value = xpr.eval(form.getMainInstance(), ec);

                if (value == null) {
                    nullArgs = true;
                } else {
	                if (value instanceof XPathNodeset) {
	                    XPathNodeset xpn = (XPathNodeset) value;
	                    value = xpn.getValAt(0);
	                }

	                selectionArgs[i + 1] = value.toString();
                }
            }
        }

        if (nullArgs) {
            // we can't try to query with null values else it blows up
            // so just leave the screen blank
            // TODO: put an error?
        } else if (Collect.getInstance().getFormController().getItemsetHash() != null) {

            ItemsetDbAdapter ida = new ItemsetDbAdapter();
            ida.open();

            try {
                Cursor c = ida.query(Collect.getInstance().getFormController()
                        .getItemsetHash(), selection.toString(), selectionArgs);
                if (c != null) {
                    c.move(-1);
                    while (c.moveToNext()) {
                        String label = "";
                        String val = "";
                        // try to get the value associated with the label:lang
                        // string if that doen't exist, then just use label
                        String lang = Collect.getInstance().getFormController().getLanguage();

                        // apparently you only need the double quotes in the
                        // column name when creating the column with a :
                        // included
                        String labelLang = "label" + ":" + lang;
                        int langCol = c.getColumnIndex(labelLang);
                        if (langCol == -1) {
                            label = c.getString(c.getColumnIndex("label"));
                        } else {
                            label = c.getString(c.getColumnIndex(labelLang));
                        }

                        // the actual value is stored in name
                        val = c.getString(c.getColumnIndex("name"));
                        mAnswers.put(label, val);

                        RadioButton rb = new RadioButton(context);
                        rb.setOnCheckedChangeListener(this);
                        rb.setText(label);
                        rb.setTextSize(mAnswerFontsize);
                        mButtons.addView(rb);
                        // have to add it to the radiogroup before checking it,
                        // else it lets two buttons be checked...
                        if (currentAnswer != null
                                && val.compareTo(currentAnswer) == 0) {
                            rb.setChecked(true);
                        }
                    }
                    c.close();
                }
            } finally {
                ida.close();
            }

            addView(mButtons);
        } else {
            TextView error = new TextView(context);
            File csv = new File(Collect.getInstance().getFormController().getMediaFolder()
                    + "/itemsets.csv");
            error.setText(getContext().getString(R.string.file_missing, csv.getAbsolutePath()));
            addView(error);
        }

    }

    @Override
    public void clearAnswer() {
        mButtons.clearCheck();
        mAnswer = null;
    }

    @Override
    public IAnswerData getAnswer() {
        if (mAnswer == null) {
            return null;
        } else {
            return new StringData(mAnswer);
        }
    }

    @Override
    public void setFocus(Context context) {
        // Hide the soft keyboard if it's showing.
        InputMethodManager inputManager = (InputMethodManager) context
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(this.getWindowToken(), 0);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.isAltPressed() == true) {
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        mButtons.setOnLongClickListener(l);
        for (int i = 0; i < mButtons.getChildCount(); i++) {
            mButtons.getChildAt(i).setOnLongClickListener(l);
        }
    }

    @Override
    public void cancelLongPress() {
        super.cancelLongPress();
        mButtons.cancelLongPress();
        for (int i = 0; i < mButtons.getChildCount(); i++) {
            mButtons.getChildAt(i).cancelLongPress();
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            mAnswer = mAnswers.get((String) buttonView.getText());
        }
    }

}
