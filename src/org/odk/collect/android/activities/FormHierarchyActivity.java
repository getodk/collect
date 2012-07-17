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

package org.odk.collect.android.activities;

import org.javarosa.core.model.FormIndex;
import org.javarosa.form.api.FormEntryCaption;
import org.javarosa.form.api.FormEntryController;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.R;
import org.odk.collect.android.adapters.HierarchyListAdapter;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.logic.FormController;
import org.odk.collect.android.logic.HierarchyElement;

import android.app.ListActivity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class FormHierarchyActivity extends ListActivity {

    private static final String t = "FormHierarchyActivity";

    private static final int CHILD = 1;
    private static final int EXPANDED = 2;
    private static final int COLLAPSED = 3;
    private static final int QUESTION = 4;

    private static final String mIndent = "     ";

    private Button jumpPreviousButton;

    List<HierarchyElement> formList;
    TextView mPath;

    FormIndex mStartIndex;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hierarchy_layout);

        FormController formController = Collect.getInstance().getFormController();
        
        // We use a static FormEntryController to make jumping faster.
        mStartIndex = formController.getFormIndex();

        setTitle(getString(R.string.app_name) + " > "
                + formController.getFormTitle());

        mPath = (TextView) findViewById(R.id.pathtext);

        jumpPreviousButton = (Button) findViewById(R.id.jumpPreviousButton);
        jumpPreviousButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Collect.getInstance().getActivityLogger().logInstanceAction(this, "goUpLevelButton", "click");
                goUpLevel();
            }
        });

        Button jumpBeginningButton = (Button) findViewById(R.id.jumpBeginningButton);
        jumpBeginningButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Collect.getInstance().getActivityLogger().logInstanceAction(this, "jumpToBeginning", "click");
                Collect.getInstance().getFormController().jumpToIndex(FormIndex
                        .createBeginningOfFormIndex());
                setResult(RESULT_OK);
                finish();
            }
        });

        Button jumpEndButton = (Button) findViewById(R.id.jumpEndButton);
        jumpEndButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Collect.getInstance().getActivityLogger().logInstanceAction(this, "jumpToEnd", "click");
                Collect.getInstance().getFormController().jumpToIndex(FormIndex.createEndOfFormIndex());
                setResult(RESULT_OK);
                finish();
            }
        });

        // kinda slow, but works.
        // this scrolls to the last question the user was looking at
        getListView().post(new Runnable() {
            @Override
            public void run() {
                int position = 0;
                for (int i = 0; i < getListAdapter().getCount(); i++) {
                    HierarchyElement he = (HierarchyElement) getListAdapter().getItem(i);
                    if (mStartIndex.equals(he.getFormIndex())) {
                        position = i;
                        break;
                    }
                }
                getListView().setSelection(position);
            }
        });

        refreshView();
    }
	
    @Override
    protected void onStart() {
    	super.onStart();
		Collect.getInstance().getActivityLogger().logOnStart(this); 
    }
    
    @Override
    protected void onStop() {
		Collect.getInstance().getActivityLogger().logOnStop(this); 
    	super.onStop();
    }

    private void goUpLevel() {
    	Collect.getInstance().getFormController().stepToOuterScreenEvent();

        refreshView();
    }


    private String getCurrentPath() {
    	FormController formController = Collect.getInstance().getFormController();
        FormIndex index = formController.getFormIndex();
        // move to enclosing group...
        index = formController.stepIndexOut(index);

        String path = "";
        while (index != null) {

            path =
            		formController.getCaptionPrompt(index).getLongText()
                        + " ("
                        + (formController.getCaptionPrompt(index)
                                .getMultiplicity() + 1) + ") > " + path;

            index = formController.stepIndexOut(index);
        }
        // return path?
        return path.substring(0, path.length() - 2);
    }


    public void refreshView() {
    	FormController formController = Collect.getInstance().getFormController();
        // Record the current index so we can return to the same place if the user hits 'back'.
        FormIndex currentIndex = formController.getFormIndex();

        // If we're not at the first level, we're inside a repeated group so we want to only display
        // everything enclosed within that group.
        String enclosingGroupRef = "";
        formList = new ArrayList<HierarchyElement>();

        // If we're currently at a repeat node, record the name of the node and step to the next
        // node to display.
        if (formController.getEvent() == FormEntryController.EVENT_REPEAT) {
            enclosingGroupRef =
            		formController.getFormIndex().getReference().toString(false);
            formController.stepToNextEvent(FormController.STEP_INTO_GROUP);
        } else {
            FormIndex startTest = formController.stepIndexOut(currentIndex);
            // If we have a 'group' tag, we want to step back until we hit a repeat or the
            // beginning.
            while (startTest != null
                    && formController.getEvent(startTest) == FormEntryController.EVENT_GROUP) {
                startTest = formController.stepIndexOut(startTest);
            }
            if (startTest == null) {
                // check to see if the question is at the first level of the hierarchy. If it is,
                // display the root level from the beginning.
            	formController.jumpToIndex(FormIndex
                        .createBeginningOfFormIndex());
            } else {
                // otherwise we're at a repeated group
            	formController.jumpToIndex(startTest);
            }

            // now test again for repeat. This should be true at this point or we're at the
            // beginning
            if (formController.getEvent() == FormEntryController.EVENT_REPEAT) {
                enclosingGroupRef =
                		formController.getFormIndex().getReference().toString(false);
                formController.stepToNextEvent(FormController.STEP_INTO_GROUP);
            }
        }

        int event = formController.getEvent();
        if (event == FormEntryController.EVENT_BEGINNING_OF_FORM) {
            // The beginning of form has no valid prompt to display.
        	formController.stepToNextEvent(FormController.STEP_INTO_GROUP);
            mPath.setVisibility(View.GONE);
            jumpPreviousButton.setEnabled(false);
        } else {
            mPath.setVisibility(View.VISIBLE);
            mPath.setText(getCurrentPath());
            jumpPreviousButton.setEnabled(true);
        }

        // Refresh the current event in case we did step forward.
        event = formController.getEvent();

        // There may be repeating Groups at this level of the hierarchy, we use this variable to
        // keep track of them.
        String repeatedGroupRef = "";

        event_search: while (event != FormEntryController.EVENT_END_OF_FORM) {
            switch (event) {
                case FormEntryController.EVENT_QUESTION:
                    if (!repeatedGroupRef.equalsIgnoreCase("")) {
                        // We're in a repeating group, so skip this question and move to the next
                        // index.
                        event =
                        		formController.stepToNextEvent(FormController.STEP_INTO_GROUP);
                        continue;
                    }

                    FormEntryPrompt fp = formController.getQuestionPrompt();
                    String label = fp.getLongText();
                    if ( !fp.isReadOnly() || (label != null && label.length() > 0) ) {
                    	// show the question if it is an editable field.
                    	// or if it is read-only and the label is not blank.
	                    formList.add(new HierarchyElement(fp.getLongText(), fp.getAnswerText(), null,
	                            Color.WHITE, QUESTION, fp.getIndex()));
                    }
                    break;
                case FormEntryController.EVENT_GROUP:
                    // ignore group events
                    break;
                case FormEntryController.EVENT_PROMPT_NEW_REPEAT:
                    if (enclosingGroupRef.compareTo(formController
                            .getFormIndex().getReference().toString(false)) == 0) {
                        // We were displaying a set of questions inside of a repeated group. This is
                        // the end of that group.
                        break event_search;
                    }

                    if (repeatedGroupRef.compareTo(formController.getFormIndex()
                            .getReference().toString(false)) != 0) {
                        // We're in a repeating group, so skip this repeat prompt and move to the
                        // next event.
                        event =
                        		formController.stepToNextEvent(FormController.STEP_INTO_GROUP);
                        continue;
                    }

                    if (repeatedGroupRef.compareTo(formController.getFormIndex()
                            .getReference().toString(false)) == 0) {
                        // This is the end of the current repeating group, so we reset the
                        // repeatedGroupName variable
                        repeatedGroupRef = "";
                    }
                    break;
                case FormEntryController.EVENT_REPEAT:
                    FormEntryCaption fc = formController.getCaptionPrompt();
                    if (enclosingGroupRef.compareTo(formController
                            .getFormIndex().getReference().toString(false)) == 0) {
                        // We were displaying a set of questions inside a repeated group. This is
                        // the end of that group.
                        break event_search;
                    }
                    if (repeatedGroupRef.equalsIgnoreCase("") && fc.getMultiplicity() == 0) {
                        // This is the start of a repeating group. We only want to display
                        // "Group #", so we mark this as the beginning and skip all of its children
                        HierarchyElement group =
                            new HierarchyElement(fc.getLongText(), null, getResources()
                                    .getDrawable(R.drawable.expander_ic_minimized), Color.WHITE,
                                    COLLAPSED, fc.getIndex());
                        repeatedGroupRef =
                        		formController.getFormIndex().getReference()
                                    .toString(false);
                        formList.add(group);
                    }

                    if (repeatedGroupRef.compareTo(formController.getFormIndex()
                            .getReference().toString(false)) == 0) {
                        // Add this group name to the drop down list for this repeating group.
                        HierarchyElement h = formList.get(formList.size() - 1);
                        h.addChild(new HierarchyElement(mIndent + fc.getLongText() + " "
                                + (fc.getMultiplicity() + 1), null, null, Color.WHITE, CHILD, fc
                                .getIndex()));
                    }
                    break;
            }
            event =
            		formController.stepToNextEvent(FormController.STEP_INTO_GROUP);
        }

        HierarchyListAdapter itla = new HierarchyListAdapter(this);
        itla.setListItems(formList);
        setListAdapter(itla);

        // set the controller back to the current index in case the user hits 'back'
        formController.jumpToIndex(currentIndex);
    }

    
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        HierarchyElement h = (HierarchyElement) l.getItemAtPosition(position);
        FormIndex index = h.getFormIndex();
        if (index == null) {
            goUpLevel();
            return;
        }

        switch (h.getType()) {
            case EXPANDED:
                Collect.getInstance().getActivityLogger().logInstanceAction(this, "onListItemClick", "COLLAPSED", h.getFormIndex());
                h.setType(COLLAPSED);
                ArrayList<HierarchyElement> children = h.getChildren();
                for (int i = 0; i < children.size(); i++) {
                    formList.remove(position + 1);
                }
                h.setIcon(getResources().getDrawable(R.drawable.expander_ic_minimized));
                h.setColor(Color.WHITE);
                break;
            case COLLAPSED:
                Collect.getInstance().getActivityLogger().logInstanceAction(this, "onListItemClick", "EXPANDED", h.getFormIndex());
                h.setType(EXPANDED);
                ArrayList<HierarchyElement> children1 = h.getChildren();
                for (int i = 0; i < children1.size(); i++) {
                    Log.i(t, "adding child: " + children1.get(i).getFormIndex());
                    formList.add(position + 1 + i, children1.get(i));

                }
                h.setIcon(getResources().getDrawable(R.drawable.expander_ic_maximized));
                h.setColor(Color.WHITE);
                break;
            case QUESTION:
                Collect.getInstance().getActivityLogger().logInstanceAction(this, "onListItemClick", "QUESTION-JUMP", index);
                Collect.getInstance().getFormController().jumpToIndex(index);
            	if ( Collect.getInstance().getFormController().indexIsInFieldList() ) {
            		Collect.getInstance().getFormController().stepToPreviousScreenEvent();
            	}
                setResult(RESULT_OK);
                finish();
                return;
            case CHILD:
                Collect.getInstance().getActivityLogger().logInstanceAction(this, "onListItemClick", "REPEAT-JUMP", h.getFormIndex());
                Collect.getInstance().getFormController().jumpToIndex(h.getFormIndex());
                setResult(RESULT_OK);
                refreshView();
                return;
        }

        // Should only get here if we've expanded or collapsed a group
        HierarchyListAdapter itla = new HierarchyListAdapter(this);
        itla.setListItems(formList);
        setListAdapter(itla);
        getListView().setSelection(position);
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                Collect.getInstance().getActivityLogger().logInstanceAction(this, "onKeyDown", "KEYCODE_BACK.JUMP", mStartIndex);
                Collect.getInstance().getFormController().jumpToIndex(mStartIndex);
        }
        return super.onKeyDown(keyCode, event);
    }

}
