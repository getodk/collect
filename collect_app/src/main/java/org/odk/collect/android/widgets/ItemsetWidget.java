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

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.graphics.BitmapFactory;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
import org.odk.collect.android.listeners.AdvanceToNextListener;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import timber.log.Timber;

/**
 * The most basic widget that allows for entry of any text.
 *
 * @author Carl Hartung (carlhartung@gmail.com)
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */
public class ItemsetWidget extends QuestionWidget implements
        CompoundButton.OnCheckedChangeListener, View.OnClickListener {

    private static final String QUOTATION_MARK = "\"";

    boolean readOnly;
    private boolean autoAdvanceToNext;

    private ArrayList<RadioButton> buttons;
    private String answer = null;
    // Hashmap linking label:value
    private HashMap<String, String> answers;
    private AdvanceToNextListener autoAdvanceToNextListener;

    protected ItemsetWidget(Context context, FormEntryPrompt prompt, boolean readOnlyOverride,
                            boolean autoAdvanceToNext) {
        super(context, prompt);

        readOnly = prompt.isReadOnly() || readOnlyOverride;
        answers = new HashMap<String, String>();

        buttons = new ArrayList<>();
        this.autoAdvanceToNext = autoAdvanceToNext;
        if (autoAdvanceToNext) {
            autoAdvanceToNextListener = (AdvanceToNextListener) context;
        }

        // Layout holds the vertical list of buttons
        LinearLayout allOptionsLayout = new LinearLayout(context);

        String currentAnswer = prompt.getAnswerText();

        // the format of the query should be something like this:
        // query="instance('cities')/root/item[state=/data/state and county=/data/county]"

        // "query" is what we're using to notify that this is an
        // itemset widget.
        String nodesetStr = prompt.getQuestion().getAdditionalAttribute(null, "query");

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
                String[] pair = subString.split("=");
                if (pair.length == 2) {
                    selection
                            .append(QUOTATION_MARK)
                            .append(pair[0].trim())
                            .append(QUOTATION_MARK)
                            .append("=? and ");
                    arguments.add(pair[1].trim());
                } else {
                    // parse error
                }
                // move string forward to after " and "
                queryString = queryString.substring(andIndex + 5, queryString.length());
                andIndex = -1;
            } else if (orIndex != -1) {
                String subString = queryString.substring(0, orIndex);
                String[] pair = subString.split("=");
                if (pair.length == 2) {
                    selection
                            .append(QUOTATION_MARK)
                            .append(pair[0].trim())
                            .append(QUOTATION_MARK)
                            .append("=? or ");
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
        String[] pair = queryString.split("=");
        if (pair.length == 2) {
            selection
                    .append(QUOTATION_MARK)
                    .append(pair[0].trim())
                    .append(QUOTATION_MARK)
                    .append("=?");
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

        // parse out the list name, between the ''
        String listName = nodesetStr.substring(nodesetStr.indexOf("'") + 1,
                nodesetStr.lastIndexOf("'"));


        boolean nullArgs = false; // can't have any null arguments
        selectionArgs[0] = listName; // first argument is always listname

        // loop through the arguments, evaluate any expressions
        // and build the query string for the DB
        for (int i = 0; i < arguments.size(); i++) {
            XPathExpression xpr = null;
            try {
                xpr = XPathParseTool.parseXPath(arguments.get(i));
            } catch (XPathSyntaxException e) {
                Timber.e(e);
                TextView error = new TextView(context);
                error.setText(String.format(getContext().getString(R.string.parser_exception), arguments.get(i)));
                addAnswerView(error);
                break;
            }

            if (xpr != null) {
                FormDef form = Collect.getInstance().getFormController().getFormDef();
                TreeElement treeElement = form.getMainInstance().resolveReference(
                        prompt.getIndex().getReference());
                EvaluationContext ec = new EvaluationContext(form.getEvaluationContext(),
                        treeElement.getRef());
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

        File itemsetFile = new File(
                Collect.getInstance().getFormController().getMediaFolder().getAbsolutePath()
                        + "/itemsets.csv");
        if (nullArgs) {
            // we can't try to query with null values else it blows up
            // so just leave the screen blank
            // TODO: put an error?
        } else if (itemsetFile.exists()) {
            ItemsetDbAdapter ida = new ItemsetDbAdapter();
            ida.open();

            // name of the itemset table for this form
            String pathHash = ItemsetDbAdapter.getMd5FromString(itemsetFile.getAbsolutePath());
            try {
                Cursor c = ida.query(pathHash, selection.toString(), selectionArgs);
                if (c != null) {
                    c.move(-1);
                    int index = 0;
                    while (c.moveToNext()) {
                        String label = "";
                        String val = "";
                        // try to get the value associated with the label:lang
                        // string if that doen't exist, then just use label
                        String lang = "";
                        if (Collect.getInstance().getFormController().getLanguages() != null
                                && Collect.getInstance().getFormController().getLanguages().length
                                > 0) {
                            lang = Collect.getInstance().getFormController().getLanguage();
                        }

                        // apparently you only need the double quotes in the
                        // column name when creating the column with a :
                        // included
                        String labelLang = "label" + "::" + lang;
                        int langCol = c.getColumnIndex(labelLang);
                        if (langCol == -1) {
                            label = c.getString(c.getColumnIndex("label"));
                        } else {
                            label = c.getString(c.getColumnIndex(labelLang));
                        }

                        // the actual value is stored in name
                        val = c.getString(c.getColumnIndex("name"));
                        answers.put(label, val);

                        RadioButton rb = new RadioButton(context);

                        rb.setOnCheckedChangeListener(this);
                        rb.setOnClickListener(this);
                        rb.setTextSize(answerFontsize);
                        rb.setText(label);
                        rb.setTag(index);
                        rb.setId(QuestionWidget.newUniqueId());

                        buttons.add(rb);

                        // have to add it to the radiogroup before checking it,
                        // else it lets two buttons be checked...
                        if (currentAnswer != null
                                && val.compareTo(currentAnswer) == 0) {
                            rb.setChecked(true);
                        }

                        RelativeLayout.LayoutParams textParams =
                                new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                                        LayoutParams.WRAP_CONTENT);
                        textParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                        textParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                        textParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                        textParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                        RelativeLayout singleOptionLayout = new RelativeLayout(getContext());
                        singleOptionLayout.addView(rb, textParams);

                        if (this.autoAdvanceToNext) {
                            ImageView rightArrow = new ImageView(getContext());
                            rightArrow.setImageBitmap(
                                    BitmapFactory.decodeResource(getContext().getResources(),
                                    R.drawable.expander_ic_right));

                            RelativeLayout.LayoutParams arrowParams =
                                    new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                                            LayoutParams.WRAP_CONTENT);
                            arrowParams.addRule(RelativeLayout.CENTER_VERTICAL);
                            arrowParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                            singleOptionLayout.addView(rightArrow, arrowParams);
                        }

                        if (!c.isLast()) {
                            // Last, add the dividing line (except for the last element)
                            ImageView divider = new ImageView(getContext());
                            divider.setBackgroundResource(android.R.drawable.divider_horizontal_bright);

                            RelativeLayout.LayoutParams dividerParams =
                                    new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                                            LayoutParams.WRAP_CONTENT);

                            dividerParams.addRule(RelativeLayout.BELOW, rb.getId());
                            singleOptionLayout.addView(divider, dividerParams);
                        }

                        allOptionsLayout.addView(singleOptionLayout);
                        index++;
                    }
                    allOptionsLayout.setOrientation(LinearLayout.VERTICAL);
                    c.close();
                }
            } catch (SQLiteException e) {
                Timber.i(e);
            } finally {
                ida.close();
            }

            addAnswerView(allOptionsLayout);
        } else {
            TextView error = new TextView(context);
            error.setText(
                    getContext().getString(R.string.file_missing, itemsetFile.getAbsolutePath()));
            addAnswerView(error);
        }

    }

    @Override
    public void clearAnswer() {
        answer = null;
        for (RadioButton button : buttons) {
            if (button.isChecked()) {
                button.setChecked(false);
                clearNextLevelsOfCascadingSelect();
                break;
            }
        }
    }

    @Override
    public IAnswerData getAnswer() {
        if (answer == null) {
            return null;
        } else {
            return new StringData(answer);
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
        return !event.isAltPressed() && super.onKeyDown(keyCode, event);
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        for (RadioButton r : buttons) {
            r.setOnLongClickListener(l);
        }
    }

    @Override
    public void cancelLongPress() {
        super.cancelLongPress();
        for (RadioButton button : buttons) {
            button.cancelLongPress();
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            for (RadioButton button : buttons) {
                if (button.isChecked() && !(buttonView == button)) {
                    button.setChecked(false);
                    clearNextLevelsOfCascadingSelect();
                } else {
                    answer = answers.get(buttonView.getText().toString());
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (autoAdvanceToNext) {
            autoAdvanceToNextListener.advance();
        }
    }
}
