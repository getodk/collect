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
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TableLayout;

import org.javarosa.core.model.SelectChoice;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.SelectOneData;
import org.javarosa.core.model.data.helper.Selection;
import org.javarosa.form.api.FormEntryPrompt;
import org.javarosa.xpath.expr.XPathFuncExpr;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.external.ExternalDataUtil;
import org.odk.collect.android.listeners.AudioPlayListener;
import org.odk.collect.android.utilities.TextUtils;
import org.odk.collect.android.views.MediaLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * SelectOneSearchWidget allows the user to enter a value in an editable text box and based on
 * input, the searched
 * options only appear which can then be chosen. This is used to narrow down the Select One options
 * For now, audio/video/image etc will be ignored
 *
 * @author Raghu Mittal (raghu.mittal@handsrel.com)
 */
public class SelectOneSearchWidget extends QuestionWidget implements
        OnCheckedChangeListener, AudioPlayListener {

    List<SelectChoice> mItems; // may take a while to compute
    ArrayList<RadioButton> buttons;
    protected EditText mSearchStr;

    protected LinearLayout buttonLayout;
    protected FormEntryPrompt prompt;
    protected Integer selectedTag = -1;

    public SelectOneSearchWidget(Context context, FormEntryPrompt prompt) {
        super(context, prompt);
        this.prompt = prompt;

        // SurveyCTO-added support for dynamic select content (from .csv files)
        XPathFuncExpr xPathFuncExpr = ExternalDataUtil.getSearchXPathExpression(
                prompt.getAppearanceHint());
        if (xPathFuncExpr != null) {
            mItems = ExternalDataUtil.populateExternalChoices(prompt, xPathFuncExpr);
        } else {
            mItems = prompt.getSelectChoices();
        }

        mSearchStr = new EditText(context);
        mSearchStr.setId(QuestionWidget.newUniqueId());
        mSearchStr.setTextSize(TypedValue.COMPLEX_UNIT_DIP, mAnswerFontsize);

        TableLayout.LayoutParams params = new TableLayout.LayoutParams();
        params.setMargins(7, 5, 7, 5);
        mSearchStr.setLayoutParams(params);
        setupChangeListener();
        addAnswerView(mSearchStr);

        doSearch("");
    }

    public void doSearch(String searchStr) {

        // First check if there is nothing on search
        if (searchStr == null || searchStr.trim().length() == 0) {
            createOptions(mItems, null);
        } else { // Create a List with items that are relevant to the search text
            List<SelectChoice> mSearchedItems = new ArrayList<SelectChoice>();
            List<Integer> tagList = new ArrayList<Integer>();
            searchStr = searchStr.toLowerCase();
            for (int i = 0; i < mItems.size(); i++) {
                String choiceText = prompt.getSelectChoiceText(mItems.get(i)).toLowerCase();
                if (choiceText.contains(searchStr)) {
                    mSearchedItems.add(mItems.get(i));
                    tagList.add(i);
                }

            }
            createOptions(mSearchedItems, tagList);
        }
    }

    public void createOptions(List<SelectChoice> mSearchedItems, List<Integer> tagList) {
        removeView(buttonLayout);
        buttons = new ArrayList<RadioButton>();

        // Layout holds the vertical list of buttons
        buttonLayout = new LinearLayout(getContext());

        String s = null;
        if (prompt.getAnswerValue() != null) {
            s = ((Selection) prompt.getAnswerValue().getValue()).getValue();
        }

        if (mSearchedItems != null && mSearchedItems.size() > 0) {
            for (int i = 0; i < mSearchedItems.size(); i++) {
                RadioButton r = new RadioButton(getContext());
                r.setTextSize(TypedValue.COMPLEX_UNIT_DIP, mAnswerFontsize);
                r.setText(TextUtils.textToHtml(prompt.getSelectChoiceText(mSearchedItems.get(i))));

                if (tagList == null) {
                    r.setTag(Integer.valueOf(i));
                } else {
                    r.setTag(tagList.get(i));
                }
                r.setId(QuestionWidget.newUniqueId());
                r.setEnabled(!prompt.isReadOnly());
                r.setFocusable(!prompt.isReadOnly());

                buttons.add(r);

                if (selectedTag == -1 && mSearchedItems.get(i).getValue().equals(s)) {
                    r.setChecked(true);
                    selectedTag = (Integer) r.getTag();
                } else if (selectedTag.equals(r.getTag())) {
                    r.setChecked(true);
                }

                r.setOnCheckedChangeListener(this);

                MediaLayout mediaLayout = new MediaLayout(getContext(), mPlayer);
                mediaLayout.setAVT(prompt.getIndex(), "." + Integer.toString(i), r, null, null,
                        null, null);
                mediaLayout.setPlayTextColor(mPlayColor);
                mediaLayout.setPlayTextBackgroundColor(mPlayBackgroundColor);

                if (i != mSearchedItems.size() - 1) {
                    // Last, add the dividing line (except for the last element)
                    ImageView divider = new ImageView(getContext());
                    divider.setBackgroundResource(android.R.drawable.divider_horizontal_bright);
                    mediaLayout.addDivider(divider);
                }
                buttonLayout.addView(mediaLayout);
            }
        }

        buttonLayout.setOrientation(LinearLayout.VERTICAL);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        params.addRule(RelativeLayout.BELOW, mSearchStr.getId());
        params.setMargins(10, 0, 10, 0);
        addView(buttonLayout, params);
    }


    protected void setupChangeListener() {
        mSearchStr.addTextChangedListener(new TextWatcher() {
            private String oldText = "";

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().equals(oldText)) {
                    Collect.getInstance().getActivityLogger()
                            .logInstanceAction(this, "searchTextChanged", s.toString(),
                                    getPrompt().getIndex());
                    doSearch(s.toString());
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                    int after) {
                oldText = s.toString();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                    int count) {
            }
        });
    }

    @Override
    public void clearAnswer() {
        for (RadioButton button : this.buttons) {
            if (button.isChecked()) {
                button.setChecked(false);
                return;
            }
        }
    }

    @Override
    public IAnswerData getAnswer() {
        int i = getCheckedId();
        if (i == -1) {
            return null;
        } else {
            SelectChoice sc = mItems.get(i);
            return new SelectOneData(new Selection(sc));
        }
    }

    @Override
    public void setFocus(Context context) {
        // Put focus on text input field and display soft keyboard if appropriate.
        mSearchStr.requestFocus();
        InputMethodManager inputManager =
                (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.showSoftInput(mSearchStr, 0);
            /*
             * If you do a multi-question screen after a "add another group" dialog, this won't
             * automatically pop up. It's an Android issue.
             *
             * That is, if I have an edit text in an activity, and pop a dialog, and in that
             * dialog's button's OnClick() I call edittext.requestFocus() and
             * showSoftInput(edittext, 0), showSoftinput() returns false. However, if the edittext
             * is focused before the dialog pops up, everything works fine. great.
             */
    }


    public int getCheckedId() {
        for (int i = 0; i < buttons.size(); ++i) {
            RadioButton button = buttons.get(i);
            if (button.isChecked()) {
                return (Integer) button.getTag();
            }
        }
        return -1;
    }


    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (!isChecked) {
            // If it got unchecked, we don't care.
            return;
        }

        for (RadioButton button : buttons) {
            if (button.isChecked() && !(buttonView == button)) {
                button.setChecked(false);
            }
        }

        selectedTag = (Integer) buttonView.getTag();
        SelectChoice choice = mItems.get(selectedTag);

        if (choice != null) {
            Collect.getInstance().getActivityLogger().logInstanceAction(this, "onCheckedChanged",
                    choice.getValue(), mPrompt.getIndex());
        } else {
            Collect.getInstance().getActivityLogger().logInstanceAction(this, "onCheckedChanged",
                    "<no matching choice>", mPrompt.getIndex());
        }
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
        for (RadioButton button : this.buttons) {
            button.cancelLongPress();
        }
    }

}