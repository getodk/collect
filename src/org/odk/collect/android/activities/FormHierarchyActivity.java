/*
 * Copyright (C) 2009 University of Washington
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

package org.odk.collect.android.activities;

import java.util.ArrayList;
import java.util.List;

import org.javarosa.core.model.FormIndex;
import org.javarosa.form.api.FormEntryCaption;
import org.javarosa.form.api.FormEntryController;
import org.javarosa.form.api.FormEntryModel;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.R;
import org.odk.collect.android.adapters.HierarchyListAdapter;
import org.odk.collect.android.logic.HierarchyElement;

import android.app.ListActivity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;


public class FormHierarchyActivity extends ListActivity {

    private static final String t = "FormHierarchyActivity";
    private FormEntryController mFormEntryController;
    private FormEntryModel mFormEntryModel;
    int state;

    private static final int CHILD = 1;
    private static final int EXPANDED = 2;
    private static final int COLLAPSED = 3;
    private static final int QUESTION = 4;

    private final String mIndent = "     ";

    List<HierarchyElement> formList;

    TextView mPath;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hierarchy_layout);

        // We use a static FormEntryController to make jumping faster.
        mFormEntryController = FormEntryActivity.mFormEntryController;
        mFormEntryModel = mFormEntryController.getModel();

        setTitle(getString(R.string.app_name) + " > " + mFormEntryModel.getFormTitle());

        mPath = (TextView) findViewById(R.id.pathtext);

        Button jumpBeginningButton = (Button) findViewById(R.id.jumpBeginningButton);
        jumpBeginningButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                mFormEntryController.jumpToIndex(FormIndex.createBeginningOfFormIndex());
                finish();
            }
        });

        Button jumpEndButton = (Button) findViewById(R.id.jumpEndButton);
        jumpEndButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                mFormEntryController.jumpToIndex(FormIndex.createEndOfFormIndex());
                finish();
            }
        });
        refreshView();
    }


    private void goUpLevel() {
        FormIndex index = stepIndexOut(mFormEntryModel.getFormIndex());
        int currentEvent = mFormEntryModel.getEvent();

        if (index == null) {
            mFormEntryController.jumpToIndex(FormIndex.createBeginningOfFormIndex());
        } else {
            if (currentEvent == FormEntryController.EVENT_REPEAT) {
                // We were at a repeat, so stepping back brought us to the
                // previous level
                mFormEntryController.jumpToIndex(index);
            } else {
                // We were at a question, so stepping back brought us to either:
                // The beginning. or The start of a repeat. So we need to step
                // out again to go passed the repeat.
                index = stepIndexOut(index);
                if (index == null) {
                    mFormEntryController.jumpToIndex(FormIndex.createBeginningOfFormIndex());
                } else {
                    mFormEntryController.jumpToIndex(index);
                }
            }
        }

        refreshView();
    }


    private String getCurrentPath() {
        FormIndex index = stepIndexOut(mFormEntryModel.getFormIndex());

        String path = "";
        while (index != null) {
            path = mFormEntryModel.getCaptionPrompt(index).getLongText() + "/" + path;
            index = stepIndexOut(index);
        }

        return path;
    }


    public void refreshView() {

        // Record the current index so we can return to the same place if the
        // user hits 'back'.
        FormIndex currentIndex = mFormEntryModel.getFormIndex();

        // If we're not at the first level, we're inside a repeated group so we
        // want to only display everything enclosed within that group.
        String enclosingGroupName = "";
        formList = new ArrayList<HierarchyElement>();

        // If we're currently at a repeat node, record the name of the node and
        // step to the next node to display.
        if (mFormEntryModel.getEvent() == FormEntryController.EVENT_REPEAT) {
            enclosingGroupName = mFormEntryModel.getCaptionPrompt().getLongText();
            mFormEntryController.stepToNextEvent();
        } else {
            // Otherwise, check to see if the question is at the first level of
            // the hierarchy. If it is, display the root level from the
            // beginning.
            FormIndex startTest = stepIndexOut(currentIndex);
            if (startTest == null) {
                // We were at the root
                mFormEntryController.jumpToIndex(FormIndex.createBeginningOfFormIndex());
            } else {
                Log.e("carl", "not at root, what does this mean?");
                // We weren't at the root, so after stepIndexOut we're

                mFormEntryController.jumpToIndex(startTest);
            }

            // now test again for repeat
            if (mFormEntryModel.getEvent() == FormEntryController.EVENT_REPEAT) {
                enclosingGroupName = mFormEntryModel.getCaptionPrompt().getLongText();
                mFormEntryController.stepToNextEvent();
            }
        }



        int event = mFormEntryModel.getEvent();
        if (event == FormEntryController.EVENT_BEGINNING_OF_FORM) {
            // The beginning of form has no valid prompt to display.
            mFormEntryController.stepToNextEvent();
            mPath.setText("Form Path: /");
        } else {
            // Create a ".." entry so user can go back.
            formList.add(new HierarchyElement("..", "Go to previous level", null, Color.WHITE,
                    QUESTION, null));
            mPath.setText("Form Path: /" + getCurrentPath());
        }

        // Refresh the current event in case we did step forward.
        event = mFormEntryModel.getEvent();

        // There may be repeating Groups at this level of the hierarchy, we use
        // this variable to keep track of them.
        String repeatedGroupName = "";

        event_search: while (event != FormEntryController.EVENT_END_OF_FORM) {
            FormEntryPrompt fp = null;
            FormEntryCaption fc = null;

            switch (event) {
                case FormEntryController.EVENT_QUESTION:
                    if (!repeatedGroupName.equalsIgnoreCase("")) {
                        // We're in a repeating group, so skip this question and
                        // move to the next index.
                        event = mFormEntryController.stepToNextEvent();
                        continue;
                    }

                    fp = mFormEntryModel.getQuestionPrompt();
                    formList.add(new HierarchyElement(fp.getLongText(), fp.getAnswerText(), null,
                            Color.WHITE, QUESTION, fp.getIndex()));
                    break;
                case FormEntryController.EVENT_GROUP:
                    // ignore group events
                    break;
                case FormEntryController.EVENT_PROMPT_NEW_REPEAT:
                    fc = mFormEntryModel.getCaptionPrompt();
                    if (enclosingGroupName.compareTo(fc.getLongText()) == 0) {
                        // We were displaying a set of questions inside of a
                        // repeated group. This is the end of that group.
                        break event_search;
                    }

                    if (repeatedGroupName.compareTo(fc.getLongText()) != 0) {
                        // We're in a repeating group, so skip this repeat
                        // prompt and move to the next event.
                        event = mFormEntryController.stepToNextEvent();
                        continue;
                    }

                    if (repeatedGroupName.compareTo(fc.getLongText()) == 0) {
                        // This is the end of the current repeating group, so we
                        // reset the repeatedGroupName variable
                        repeatedGroupName = "";
                    }
                    break;
                case FormEntryController.EVENT_REPEAT:
                    fc = mFormEntryModel.getCaptionPrompt();
                    if (enclosingGroupName.compareTo(fc.getLongText()) == 0) {
                        // We were displaying a set of questions inside a
                        // repeated group. This is the end of that group.
                        break event_search;
                    }
                    if (repeatedGroupName.equalsIgnoreCase("") && fc.getMultiplicity() == 0) {
                        // This is the start of a repeating group. We only want
                        // to display "Group #", so we mark this as the
                        // beginning and skip all of its children
                        HierarchyElement group =
                                new HierarchyElement(fc.getLongText(),
                                        getString(R.string.collapsed_group), getResources()
                                                .getDrawable(R.drawable.expander_ic_minimized),
                                        Color.WHITE, COLLAPSED, fc.getIndex());
                        repeatedGroupName = fc.getLongText();
                        formList.add(group);
                    }

                    if (repeatedGroupName.compareTo(fc.getLongText()) == 0) {
                        // Add this group name to the drop down list for this
                        // repeating group.
                        HierarchyElement h = formList.get(formList.size() - 1);
                        h.AddChild(new HierarchyElement(mIndent + fc.getLongText() + " "
                                + fc.getMultiplicity(), mIndent
                                + "Select to see repeated element: " + fc.getLongText() + " "
                                + fc.getMultiplicity(), null, Color.WHITE, CHILD, fc.getIndex()));
                    }
                    break;
            }
            event = mFormEntryController.stepToNextEvent();
        }


        HierarchyListAdapter itla = new HierarchyListAdapter(this);
        itla.setListItems(formList);
        setListAdapter(itla);

        // set the controller back to the current index in case the user hits
        // 'back'
        mFormEntryController.jumpToIndex(currentIndex);
    }


    /**
     * used to go up one level in the formIndex. That is, if you're at 5_0, 1
     * (the second question in a repeating group), this method will return a
     * FormInex of 5_0 (the start of the repeating group). If your at index 16
     * or 5_0, this will return null;
     * 
     * @param index
     * @return
     */
    public FormIndex stepIndexOut(FormIndex index) {
        if (index.isTerminal()) {
            return null;
        } else {
            return new FormIndex(stepIndexOut(index.getNextLevel()), index);
        }
    }


    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        HierarchyElement h = (HierarchyElement) l.getItemAtPosition(position);
        if (h.getFormIndex() == null) {
            goUpLevel();
            return;
        }

        switch (h.getType()) {
            case EXPANDED:
                h.setType(COLLAPSED);
                ArrayList<HierarchyElement> children = h.getChildren();
                for (int i = 0; i < children.size(); i++) {
                    formList.remove(position + 1);
                }
                h.setIcon(getResources().getDrawable(R.drawable.expander_ic_minimized));
                h.setSecondaryText(getString(R.string.collapsed_group));
                h.setColor(Color.WHITE);
                break;
            case COLLAPSED:
                h.setType(EXPANDED);
                ArrayList<HierarchyElement> children1 = h.getChildren();
                for (int i = 0; i < children1.size(); i++) {
                    Log.i(t, "adding child: " + children1.get(i).getFormIndex());
                    formList.add(position + 1 + i, children1.get(i));

                }
                h.setIcon(getResources().getDrawable(R.drawable.expander_ic_maximized));
                h.setSecondaryText(getString(R.string.expanded_group));
                h.setColor(Color.LTGRAY);
                break;
            case QUESTION:
                mFormEntryController.jumpToIndex(h.getFormIndex());
                finish();
                return;
            case CHILD:
                mFormEntryController.jumpToIndex(h.getFormIndex());
                refreshView();
                return;
        }

        // Should only get here if we've expanded or collapsed a group
        HierarchyListAdapter itla = new HierarchyListAdapter(this);
        itla.setListItems(formList);
        setListAdapter(itla);
        this.getListView().setSelection(position);
    }

}
