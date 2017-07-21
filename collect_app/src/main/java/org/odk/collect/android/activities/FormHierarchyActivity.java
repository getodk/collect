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

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.javarosa.core.model.FormIndex;
import org.javarosa.form.api.FormEntryCaption;
import org.javarosa.form.api.FormEntryController;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.R;
import org.odk.collect.android.adapters.HierarchyListAdapter;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.exception.JavaRosaException;
import org.odk.collect.android.logic.FormController;
import org.odk.collect.android.logic.HierarchyElement;
import org.odk.collect.android.utilities.ApplicationConstants;
import org.odk.collect.android.utilities.FormEntryPromptUtils;

import java.util.ArrayList;

import timber.log.Timber;

public class FormHierarchyActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private static final int ITEM = 1;
    private static final int GROUP = 2;
    private static final int QUESTION = 3;
    private static final String FORM_LIST = "formlist";
    ArrayList<HierarchyElement> formList;
    TextView path;

    FormIndex startIndex;
    private Button jumpPreviousButton;
    private FormIndex currentIndex;
    private ListView listView;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hierarchy_layout);

        listView = (ListView) findViewById(android.R.id.list);
        listView.setOnItemClickListener(this);
        TextView emptyView = (TextView) findViewById(android.R.id.empty);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FormController formController = Collect.getInstance().getFormController();
        // https://github.com/opendatakit/collect/issues/998
        if (formController == null) {
            finish();
            return;
        }

        // We use a static FormEntryController to make jumping faster.
        startIndex = formController.getFormIndex();

        setTitle(formController.getFormTitle());

        path = (TextView) findViewById(R.id.pathtext);

        jumpPreviousButton = (Button) findViewById(R.id.jumpPreviousButton);
        jumpPreviousButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Collect.getInstance().getActivityLogger().logInstanceAction(this, "goUpLevelButton",
                        "click");
                goUpLevel(formList.get(0).getParent());
            }
        });

        Button jumpBeginningButton = (Button) findViewById(R.id.jumpBeginningButton);
        jumpBeginningButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Collect.getInstance().getActivityLogger().logInstanceAction(this, "jumpToBeginning",
                        "click");
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
                Collect.getInstance().getActivityLogger().logInstanceAction(this, "jumpToEnd",
                        "click");
                Collect.getInstance().getFormController().jumpToIndex(
                        FormIndex.createEndOfFormIndex());
                setResult(RESULT_OK);
                finish();
            }
        });

        String formMode = getIntent().getStringExtra(ApplicationConstants.BundleKeys.FORM_MODE);
        if (ApplicationConstants.FormModes.VIEW_SENT.equalsIgnoreCase(formMode)) {
            Collect.getInstance().getFormController().stepToOuterScreenEvent();

            Button exitButton = (Button) findViewById(R.id.exitButton);
            exitButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Collect.getInstance().getActivityLogger().logInstanceAction(this, "exit",
                            "click");
                    setResult(RESULT_OK);
                    finish();
                }
            });

            exitButton.setVisibility(View.VISIBLE);
            jumpBeginningButton.setVisibility(View.GONE);
            jumpEndButton.setVisibility(View.GONE);
        }

        if (savedInstanceState != null && savedInstanceState.containsKey(FORM_LIST)) {
            formList = savedInstanceState.getParcelableArrayList(FORM_LIST);
            if (formList == null || formList.size() == 0) {
                formList = new ArrayList<>();
                refreshView(null);
            } else {
                goUpLevel(formList.get(0));
            }
        } else {
            formList = new ArrayList<>();
            refreshView(null);
        }
        // kinda slow, but works.
        // this scrolls to the last question the user was looking at
        if (getListAdapter() != null && listView != null) {
            emptyView.setVisibility(View.GONE);
            listView.post(new Runnable() {
                @Override
                public void run() {
                    int position = 0;
                    for (int i = 0; i < getListAdapter().getCount(); i++) {
                        HierarchyElement he = (HierarchyElement) getListAdapter().getItem(i);
                        if (startIndex.equals(he.getFormIndex())) {
                            position = i;
                            break;
                        }
                    }
                    listView.setSelection(position);
                }
            });
        }
    }

    private ListAdapter getListAdapter() {
        return listView.getAdapter();
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

    private void goUpLevel(HierarchyElement parent) {
        Collect.getInstance().getFormController().stepToOuterScreenEvent();

        HierarchyListAdapter itla = new HierarchyListAdapter(this);
        formList = parent.getList();
        itla.setListItems(formList);
        listView.setAdapter(itla);
        listView.setSelection(formList.indexOf(parent));

        if (parent.getParent() == null) {
            jumpPreviousButton.setEnabled(false);
            path.setVisibility(View.GONE);
        } else {
            path.setVisibility(View.VISIBLE);
            path.setText(getCurrentPath());
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Timber.i("onSaveInstanceState");
        outState.putParcelableArrayList(FORM_LIST, formList);
        super.onSaveInstanceState(outState);
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
        if (path.equals("")) {
            return path;
        } else {
            return path.substring(0, path.length() - 2);
        }
    }

    public void refreshView(HierarchyElement parent) {
        try {
            FormController formController = Collect.getInstance().getFormController();

            if (parent != null) {
                formList = new ArrayList<>();
                formController.jumpToIndex(parent.getFormIndex());
            }

            // Record the current index so we can return to the same place if the user hits 'back'.
            currentIndex = formController.getFormIndex();

            // If we're not at the first level, we're inside a repeated group so we want to only
            // display
            // everything enclosed within that group.
            FormIndex contextGroupRef = formController.getFormIndex();
            FormIndex repeatGroupRef = null;

            // If we're currently at a repeat node, record the name of the node and step to the next
            // node to display.
            if (formController.getEvent() == FormEntryController.EVENT_GROUP || (parent != null && parent.getType() == ITEM)) {
                contextGroupRef = parent.getFormIndex();
                formController.stepToNextEvent();
            } else if (formController.getEvent() == FormEntryController.EVENT_REPEAT) {
                formController.stepToPreviousEvent();
                contextGroupRef = parent.getFormIndex();
                formController.jumpToIndex(parent.getFormIndex());
                repeatGroupRef = formController.getFormIndex();
            } else {
                FormIndex startTest = formController.stepIndexOut(currentIndex);
                // If we have a 'group' tag, we want to step back until we hit a repeat or the
                // beginning.
                while (startTest != null
                        && formController.getEvent(startTest) == FormEntryController.EVENT_GROUP) {
                    startTest = formController.stepIndexOut(startTest);
                }
                if (startTest == null) {
                    // check to see if the question is at the first level of the hierarchy. If it
                    // is,
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
                    contextGroupRef = formController.getFormIndex();
                    formController.stepToNextEvent(FormController.STEP_INTO_GROUP);
                }
            }

            int event = formController.getEvent();
            if (event == FormEntryController.EVENT_BEGINNING_OF_FORM) {
                // The beginning of form has no valid prompt to display.
                formController.stepToNextEvent(FormController.STEP_INTO_GROUP);
                contextGroupRef = null;
                path.setVisibility(View.GONE);
                jumpPreviousButton.setEnabled(false);
            } else {
                path.setVisibility(View.VISIBLE);
                path.setText(getCurrentPath());
                jumpPreviousButton.setEnabled(true);
            }

            // Refresh the current event in case we did step forward.
            event = formController.getEvent();

            // Big change from prior implementation:
            //
            // The ref strings now include the instance number designations
            // i.e., [0], [1], etc. of the repeat groups (and also [1] for
            // non-repeat elements).
            //
            // The contextGroupRef is now also valid for the top-level form.
            //
            // The repeatGroupRef is null if we are not skipping a repeat
            // section.
            //

            event_search:
            while (event != FormEntryController.EVENT_END_OF_FORM) {

                // get the ref to this element
                FormIndex currentRef = formController.getFormIndex();

                // retrieve the current group
                FormIndex curGroup = (repeatGroupRef == null) ? contextGroupRef : repeatGroupRef;


                if (contextGroupRef != null && !formController.isWithinGroup(currentRef, curGroup)) {
                    // We have left the current group
                    // We are done.
                    break;
                }

                while (repeatGroupRef != null) {
                    if (event == FormEntryController.EVENT_REPEAT) {
                        FormEntryCaption fc = formController.getCaptionPrompt();
                        HierarchyElement item = new HierarchyElement(fc.getLongText() + " "
                                + (fc.getMultiplicity() + 1), null, 1, Color.WHITE, ITEM,
                                fc.getIndex(), parent, formList);
                        formList.add(item);
                        event = formController.stepOverGroupInHierarchy();
                    } else if (event == FormEntryController.EVENT_PROMPT_NEW_REPEAT) {
                        break event_search;
                    } else {
                        formController.stepToNextScreenEvent();
                        repeatGroupRef = null;
                    }
                }

                switch (event) {
                    case FormEntryController.EVENT_QUESTION:

                        FormEntryPrompt fp = formController.getQuestionPrompt();
                        String label = fp.getLongText();
                        if (!fp.isReadOnly() || (label != null && label.length() > 0)) {
                            // show the question if it is an editable field.
                            // or if it is read-only and the label is not blank.
                            String answerDisplay = FormEntryPromptUtils.getAnswerText(fp);
                            formList.add(new HierarchyElement(fp.getLongText(), answerDisplay, 0,
                                    Color.WHITE, QUESTION, fp.getIndex(), parent, formList));
                        }
                        break;
                    case FormEntryController.EVENT_GROUP:
                        FormEntryCaption fc = formController.getCaptionPrompt();
                        label = fc.getLongText();
                        if (label != null && !label.trim().equals("")) {
                            formList.add(new HierarchyElement(label, null, 1,
                                    Color.WHITE, GROUP, fc.getIndex(), parent, formList));
                            event = formController.stepOverGroupInHierarchy();
                            continue event_search;
                        }
                        break;
                    case FormEntryController.EVENT_PROMPT_NEW_REPEAT:
                        // this would display the 'add new repeat' dialog
                        // ignore it.
                        break;
                    case FormEntryController.EVENT_REPEAT:
                        fc = formController.getCaptionPrompt();
                        // push this repeat onto the stack.
                        // Because of the guard conditions above, we will skip
                        // everything until we exit this repeat.
                        //
                        // Note that currentRef includes the multiplicity of the
                        // repeat (e.g., [0], [1], ...), so every repeat will be
                        // detected as different and reach this case statement.
                        // Only the [0] emits the repeat header.
                        // Every one displays the descend-into action element.

                        if (fc.getMultiplicity() == 0) {
                            // Display the repeat header for the group.
                            HierarchyElement group =
                                    new HierarchyElement(fc.getLongText(), null, 1,
                                            Color.WHITE, GROUP, fc.getIndex(), parent, formList);
                            formList.add(group);
                        }
                        event = formController.stepOverGroupInHierarchy();
                        continue event_search;
                }
                event = formController.stepToNextEvent();
            }

            HierarchyListAdapter itla = new HierarchyListAdapter(this);
            itla.setListItems(formList);
            listView.setAdapter(itla);

            // set the controller back to the current index in case the user hits 'back'
            formController.jumpToIndex(currentIndex);
        } catch (Exception e) {
            Timber.e(e);
            createErrorDialog(e.getMessage());
        }
    }

    /**
     * Creates and displays dialog with the given errorMsg.
     */
    private void createErrorDialog(String errorMsg) {
        Collect.getInstance()
                .getActivityLogger()
                .logInstanceAction(this, "createErrorDialog", "show.");

        AlertDialog alertDialog = new AlertDialog.Builder(this).create();

        alertDialog.setIcon(android.R.drawable.ic_dialog_info);
        alertDialog.setTitle(getString(R.string.error_occured));
        alertDialog.setMessage(errorMsg);
        DialogInterface.OnClickListener errorListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                switch (i) {
                    case DialogInterface.BUTTON_POSITIVE:
                        Collect.getInstance().getActivityLogger()
                                .logInstanceAction(this, "createErrorDialog", "OK");
                        FormController formController = Collect.getInstance().getFormController();
                        formController.jumpToIndex(currentIndex);
                        break;
                }
            }
        };
        alertDialog.setCancelable(false);
        alertDialog.setButton(getString(R.string.ok), errorListener);
        alertDialog.show();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        HierarchyElement h = (HierarchyElement) listView.getItemAtPosition(position);
        FormIndex index = h.getFormIndex();
        if (index == null) {
            goUpLevel(h.getParent());
            return;
        }

        switch (h.getType()) {
            case QUESTION:
                Collect.getInstance().getActivityLogger().logInstanceAction(this, "onListItemClick",
                        "QUESTION-JUMP", index);
                Collect.getInstance().getFormController().jumpToIndex(index);
                if (Collect.getInstance().getFormController().indexIsInFieldList()) {
                    try {
                        Collect.getInstance().getFormController().stepToPreviousScreenEvent();
                    } catch (JavaRosaException e) {
                        Timber.e(e);
                        createErrorDialog(e.getCause().getMessage());
                        return;
                    }
                }
                setResult(RESULT_OK);
                String formMode = getIntent().getStringExtra(ApplicationConstants.BundleKeys.FORM_MODE);
                if (formMode == null || ApplicationConstants.FormModes.EDIT_SAVED.equalsIgnoreCase(formMode)) {
                    finish();
                }
                break;
            case GROUP:
            case ITEM:
                Collect.getInstance().getActivityLogger().logInstanceAction(this, "onListItemClick",
                        "GROUP", h.getFormIndex());
                Collect.getInstance().getFormController().jumpToIndex(h.getFormIndex());
                setResult(RESULT_OK);
                refreshView(h);
                break;
        }
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                Collect.getInstance().getActivityLogger().logInstanceAction(this, "onKeyDown",
                        "KEYCODE_BACK.JUMP", startIndex);
                Collect.getInstance().getFormController().jumpToIndex(startIndex);
        }
        return super.onKeyDown(keyCode, event);
    }
}
