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
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.GroupDef;
import org.javarosa.form.api.FormEntryCaption;
import org.javarosa.form.api.FormEntryController;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.R;
import org.odk.collect.android.adapters.HierarchyListAdapter;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.exception.JavaRosaException;
import org.odk.collect.android.logic.FormController;
import org.odk.collect.android.logic.HierarchyElement;
import org.odk.collect.android.utilities.FormEntryPromptUtils;
import org.odk.collect.android.views.ODKView;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

/**
 * Displays the structure of a form along with the answers for the current instance. Different form
 * elements are displayed in the following ways:
 * - Questions each take up a row with their full label shown and their answers below
 * - Non-repeat groups are not represented at all
 * - Repeat groups are initially shown as collapsed and are expanded when tapped, revealing instances
 * of that repeat
 * - Repeat instances are displayed with their label and a count after (e.g. My group (1))
 *
 * Tapping on a repeat instance shows all the questions in that repeat instance using the display
 * rules above.
 *
 * Although the user gets the impression of navigating "into" a repeat, the view is refreshed in
 * {@link #refreshView()} rather than another activity/fragment being added to the backstack.
 *
 * Buttons at the bottom of the screen allow users to navigate the form.
 */
public class FormHierarchyActivity extends CollectAbstractActivity {
    List<HierarchyElement> formList;
    TextView path;

    FormIndex startIndex;
    private FormIndex currentIndex;
    protected Button jumpPreviousButton;
    protected Button jumpBeginningButton;
    protected Button jumpEndButton;
    protected RecyclerView recyclerView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hierarchy_layout);

        recyclerView = findViewById(R.id.list);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        TextView emptyView = findViewById(android.R.id.empty);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FormController formController = Collect.getInstance().getFormController();
        // https://github.com/opendatakit/collect/issues/998
        if (formController == null) {
            finish();
            Timber.w("FormController is null");
            return;
        }

        // We use a static FormEntryController to make jumping faster.
        startIndex = formController.getFormIndex();

        setTitle(formController.getFormTitle());

        path = findViewById(R.id.pathtext);

        jumpPreviousButton = findViewById(R.id.jumpPreviousButton);
        jumpBeginningButton = findViewById(R.id.jumpBeginningButton);
        jumpEndButton = findViewById(R.id.jumpEndButton);

        configureButtons(formController);
        refreshView();

        // Kinda slow, but works. This scrolls to the last question the user was looking at.
        if (recyclerView != null && recyclerView.getAdapter() != null && recyclerView.getAdapter().getItemCount() > 0) {
            emptyView.setVisibility(View.GONE);
            recyclerView.post(() -> {
                int position = 0;
                for (HierarchyElement hierarchyElement : formList) {
                    if (shouldScrollToTheGivenIndex(hierarchyElement.getFormIndex(), formController)) {
                        position = formList.indexOf(hierarchyElement);
                        break;
                    }
                }
                ((LinearLayoutManager) recyclerView.getLayoutManager()).scrollToPositionWithOffset(position, 0);
            });
        }
    }


    /**
     * Configure the navigation buttons at the bottom of the screen.
     */
    void configureButtons(FormController formController) {
        jumpPreviousButton.setOnClickListener(v -> goUpLevel());

        jumpBeginningButton.setOnClickListener(v -> {
            formController.getTimerLogger().exitView();
            formController.jumpToIndex(FormIndex.createBeginningOfFormIndex());

            setResult(RESULT_OK);
            finish();
        });

        jumpEndButton.setOnClickListener(v -> {
            formController.getTimerLogger().exitView();
            formController.jumpToIndex(FormIndex.createEndOfFormIndex());

            setResult(RESULT_OK);
            finish();
        });
    }

    private boolean shouldScrollToTheGivenIndex(FormIndex formIndex, FormController formController) {
        return startIndex.equals(formIndex)
                || (formController.indexIsInFieldList(startIndex) && formIndex.toString().startsWith(startIndex.toString()));
    }

    protected void goUpLevel() {
        Collect.getInstance().getFormController().stepToOuterScreenEvent();

        refreshView();
    }

    private String getCurrentPath() {
        FormController formController = Collect.getInstance().getFormController();
        FormIndex index = formController.getFormIndex();
        // move to enclosing group...
        index = formController.stepIndexOut(index);

        List<FormEntryCaption> groups = new ArrayList<>();
        while (index != null) {
            groups.add(0, formController.getCaptionPrompt(index));
            index = formController.stepIndexOut(index);
        }
        return ODKView.getGroupsPath(groups.toArray(new FormEntryCaption[groups.size()]));
    }

    public void refreshView() {
        try {
            FormController formController = Collect.getInstance().getFormController();
            // Record the current index so we can return to the same place if the user hits 'back'.
            currentIndex = formController.getFormIndex();

            // If we're not at the first level, we're inside a repeated group so we want to only
            // display
            // everything enclosed within that group.
            String contextGroupRef = "";
            formList = new ArrayList<>();

            // If we're currently at a repeat node, record the name of the node and step to the next
            // node to display.
            if (formController.getEvent() == FormEntryController.EVENT_REPEAT) {
                contextGroupRef =
                        formController.getFormIndex().getReference().toString(true);
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
                    contextGroupRef =
                            formController.getFormIndex().getReference().toString(true);
                    formController.stepToNextEvent(FormController.STEP_INTO_GROUP);
                }
            }

            int event = formController.getEvent();
            if (event == FormEntryController.EVENT_BEGINNING_OF_FORM) {
                // The beginning of form has no valid prompt to display.
                formController.stepToNextEvent(FormController.STEP_INTO_GROUP);
                contextGroupRef =
                        formController.getFormIndex().getReference().getParentRef().toString(true);
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
            String repeatGroupRef = null;

            event_search:
            while (event != FormEntryController.EVENT_END_OF_FORM) {

                // get the ref to this element
                String currentRef = formController.getFormIndex().getReference().toString(true);

                // retrieve the current group
                String curGroup = (repeatGroupRef == null) ? contextGroupRef : repeatGroupRef;

                if (!currentRef.startsWith(curGroup)) {
                    // We have left the current group
                    if (repeatGroupRef == null) {
                        // We are done.
                        break;
                    } else {
                        // exit the inner repeat group
                        repeatGroupRef = null;
                    }
                }

                if (repeatGroupRef != null) {
                    // We're in a repeat group within the one we want to list
                    // skip this question/group/repeat and move to the next index.
                    event =
                            formController.stepToNextEvent(FormController.STEP_INTO_GROUP);
                    continue;
                }

                switch (event) {
                    case FormEntryController.EVENT_QUESTION:

                        FormEntryPrompt fp = formController.getQuestionPrompt();
                        String label = getLabel(fp);
                        if (!fp.isReadOnly() || (label != null && label.length() > 0)) {
                            // show the question if it is an editable field.
                            // or if it is read-only and the label is not blank.
                            String answerDisplay = FormEntryPromptUtils.getAnswerText(fp, this, formController);
                            formList.add(
                                    new HierarchyElement(FormEntryPromptUtils.markQuestionIfIsRequired(label, fp.isRequired()), answerDisplay, null,
                                            HierarchyElement.Type.QUESTION, fp.getIndex()));
                        }
                        break;
                    case FormEntryController.EVENT_GROUP:
                        // ignore group events
                        break;
                    case FormEntryController.EVENT_PROMPT_NEW_REPEAT:
                        // this would display the 'add new repeat' dialog
                        // ignore it.
                        break;
                    case FormEntryController.EVENT_REPEAT:
                        FormEntryCaption fc = formController.getCaptionPrompt();
                        // push this repeat onto the stack.
                        repeatGroupRef = currentRef;
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
                                    new HierarchyElement(getLabel(fc), null, ContextCompat
                                            .getDrawable(this, R.drawable.expander_ic_minimized),
                                            HierarchyElement.Type.COLLAPSED, fc.getIndex());
                            formList.add(group);
                        }
                        String repeatLabel = getLabel(fc);
                        if (fc.getFormElement().getChildren().size() == 1 && fc.getFormElement().getChild(0) instanceof GroupDef) {
                            formController.stepToNextEvent(FormController.STEP_INTO_GROUP);
                            FormEntryCaption fc2 = formController.getCaptionPrompt();
                            if (getLabel(fc2) != null) {
                                repeatLabel = getLabel(fc2);
                            }
                        }
                        repeatLabel += " (" + (fc.getMultiplicity() + 1) + ")\u200E";
                        // Add this group name to the drop down list for this repeating group.
                        HierarchyElement h = formList.get(formList.size() - 1);
                        h.addChild(new HierarchyElement(repeatLabel, null, null, HierarchyElement.Type.CHILD, fc.getIndex()));
                        break;
                }
                event =
                        formController.stepToNextEvent(FormController.STEP_INTO_GROUP);
            }

            recyclerView.setAdapter(new HierarchyListAdapter(formList, this::onElementClick));

            // set the controller back to the current index in case the user hits 'back'
            formController.jumpToIndex(currentIndex);
        } catch (Exception e) {
            Timber.e(e);
            createErrorDialog(e.getMessage());
        }
    }

    /**
     * Handles clicks on a specific row in the hierarchy view. Clicking on a:
     * - group makes it toggle between expanded and collapsed
     * - question jumps to the form filling view with that question shown. If the question is in a
     * field list, shows that entire field list.
     * - group's child element causes this hierarchy view to be refreshed with that element's
     * questions shown
     */
    public void onElementClick(HierarchyElement element) {
        int position = formList.indexOf(element);
        FormIndex index = element.getFormIndex();
        if (index == null) {
            goUpLevel();
            return;
        }

        switch (element.getType()) {
            case EXPANDED:
                element.setType(HierarchyElement.Type.COLLAPSED);
                ArrayList<HierarchyElement> children = element.getChildren();
                for (int i = 0; i < children.size(); i++) {
                    formList.remove(position + 1);
                }
                element.setIcon(ContextCompat.getDrawable(this, R.drawable.expander_ic_minimized));
                break;
            case COLLAPSED:
                element.setType(HierarchyElement.Type.EXPANDED);
                ArrayList<HierarchyElement> children1 = element.getChildren();
                for (int i = 0; i < children1.size(); i++) {
                    Timber.i("adding child: %s", children1.get(i).getFormIndex());
                    formList.add(position + 1 + i, children1.get(i));

                }
                element.setIcon(ContextCompat.getDrawable(this, R.drawable.expander_ic_maximized));
                break;
            case QUESTION:
                onQuestionClicked(index);
                return;
            case CHILD:
                Collect.getInstance().getFormController().jumpToIndex(element.getFormIndex());
                setResult(RESULT_OK);
                refreshView();
                return;
        }

        recyclerView.setAdapter(new HierarchyListAdapter(formList, this::onElementClick));
        ((LinearLayoutManager) recyclerView.getLayoutManager()).scrollToPositionWithOffset(position, 0);
    }

    /**
     * Handles clicks on a question. Jumps to the form filling view with the selected question shown.
     * If the selected question is in a field list, show the entire field list.
     */
    void onQuestionClicked(FormIndex index) {
        Collect.getInstance().getFormController().jumpToIndex(index);
        if (Collect.getInstance().getFormController().indexIsInFieldList()) {
            try {
                Collect.getInstance().getFormController().stepToPreviousScreenEvent();
            } catch (JavaRosaException e) {
                Timber.d(e);
                createErrorDialog(e.getCause().getMessage());
                return;
            }
        }
        setResult(RESULT_OK);
        finish();
    }

    /**
     * When the device back button is pressed, go back to the previous activity, NOT the previous
     * level in the hierarchy as the "Go Up" button does.
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                FormController fc = Collect.getInstance().getFormController();
                if (fc != null) {
                    fc.getTimerLogger().exitView();
                    fc.jumpToIndex(startIndex);
                }
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * Creates and displays dialog with the given errorMsg.
     */
    protected void createErrorDialog(String errorMsg) {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();

        alertDialog.setIcon(android.R.drawable.ic_dialog_info);
        alertDialog.setTitle(getString(R.string.error_occured));
        alertDialog.setMessage(errorMsg);
        DialogInterface.OnClickListener errorListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                switch (i) {
                    case DialogInterface.BUTTON_POSITIVE:
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

    private String getLabel(FormEntryCaption formEntryCaption) {
        return formEntryCaption.getShortText() != null && !formEntryCaption.getShortText().isEmpty()
                ? formEntryCaption.getShortText() : formEntryCaption.getLongText();
    }
}
