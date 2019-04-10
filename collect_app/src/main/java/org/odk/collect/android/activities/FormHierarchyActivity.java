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
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.GroupDef;
import org.javarosa.core.model.IFormElement;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.form.api.FormEntryCaption;
import org.javarosa.form.api.FormEntryController;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.R;
import org.odk.collect.android.adapters.HierarchyListAdapter;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.exception.JavaRosaException;
import org.odk.collect.android.logic.FormController;
import org.odk.collect.android.logic.HierarchyElement;
import org.odk.collect.android.utilities.DialogUtils;
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
 * - Repeat groups are initially shown as a "header" which takes you to a "picker" when tapped,
 *   revealing instances of that repeat
 * - Repeat instances are displayed with their label and index (e.g. `My group (1)`)
 *
 * Tapping on a repeat instance shows all the questions in that repeat instance using the display
 * rules above.
 *
 * Tapping on a question sets the app-wide current question to that question and terminates the
 * activity, returning to {@link FormEntryActivity}.
 *
 * Although the user gets the impression of navigating "into" a repeat, the view is refreshed in
 * {@link #refreshView()} rather than another activity/fragment being added to the back stack.
 *
 * Buttons at the bottom of the screen allow users to navigate the form.
 */
public class FormHierarchyActivity extends CollectAbstractActivity {
    /**
     * The questions and repeats at the current level.
     * Recreated every time {@link #refreshView()} is called.
     */
    private List<HierarchyElement> elementsToDisplay;

    /**
     * The label shown at the top of a hierarchy screen for a repeat instance. Set by
     * {@link #getCurrentPath()}.
     */
    private TextView groupPathTextView;

    /**
     * A ref to the current context group.
     * Useful to make sure we only render items inside of the group.
     */
    private TreeReference contextGroupRef;

    /**
     * If this index is non-null, we will render an intermediary "picker" view
     * showing the instances of the given repeat group.
     */
    private FormIndex repeatGroupPickerIndex;
    private static final String REPEAT_GROUP_PICKER_INDEX_KEY = "REPEAT_GROUP_PICKER_INDEX_KEY";

    /**
     * The index of the question or the field list the FormController was set to when the hierarchy
     * was accessed. Used to jump the user back to where they were if applicable.
     */
    private FormIndex startIndex;

    /**
     * The index of the question that is being displayed in the hierarchy. On first launch, it is
     * the same as {@link #startIndex}. It can then become the index of a repeat instance.
     */
    private FormIndex currentIndex;

    /**
     * The index of the screen that is being displayed in the hierarchy
     * (either the root of the form or a repeat group).
     */
    private FormIndex screenIndex;

    /**
     * The toolbar menu.
     */
    private Menu optionsMenu;

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
            Collect.getInstance().logNullFormControllerEvent("FormHierarchyActivity");
            return;
        }

        startIndex = formController.getFormIndex();

        setTitle(formController.getFormTitle());

        groupPathTextView = findViewById(R.id.pathtext);

        jumpBeginningButton = findViewById(R.id.jumpBeginningButton);
        jumpEndButton = findViewById(R.id.jumpEndButton);

        configureButtons(formController);

        restoreInstanceState(savedInstanceState);

        refreshView();

        // Scroll to the last question the user was looking at
        // TODO: avoid another iteration through all displayed elements
        if (recyclerView != null && recyclerView.getAdapter() != null && recyclerView.getAdapter().getItemCount() > 0) {
            emptyView.setVisibility(View.GONE);
            recyclerView.post(() -> {
                int position = 0;
                // Iterate over all the elements currently displayed looking for a match with the
                // startIndex which can either represent a question or a field list.
                for (HierarchyElement hierarchyElement : elementsToDisplay) {
                    FormIndex indexToCheck = hierarchyElement.getFormIndex();
                    if (startIndex.equals(indexToCheck)
                            || (formController.indexIsInFieldList(startIndex) && indexToCheck.toString().startsWith(startIndex.toString()))) {
                        position = elementsToDisplay.indexOf(hierarchyElement);
                        break;
                    }
                }
                ((LinearLayoutManager) recyclerView.getLayoutManager()).scrollToPositionWithOffset(position, 0);
            });
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(REPEAT_GROUP_PICKER_INDEX_KEY, repeatGroupPickerIndex);
        super.onSaveInstanceState(outState);
    }

    private void restoreInstanceState(Bundle state) {
        if (state != null) {
            repeatGroupPickerIndex = (FormIndex) state.getSerializable(REPEAT_GROUP_PICKER_INDEX_KEY);
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.form_hierarchy_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        optionsMenu = menu;
        updateOptionsMenu();

        return true;
    }

    private void updateOptionsMenu() {
        FormController formController = Collect.getInstance().getFormController();

        // Not ready yet. Menu will be updated automatically once it's been prepared.
        if (optionsMenu == null || formController == null) {
            return;
        }

        boolean isAtBeginning = screenIndex.isBeginningOfFormIndex() && !shouldShowRepeatGroupPicker();
        boolean shouldShowPicker = shouldShowRepeatGroupPicker();
        boolean isInRepeat = formController.indexContainsRepeatableGroup();
        boolean isGroupSizeLocked = shouldShowPicker
                ? isGroupSizeLocked(repeatGroupPickerIndex) : isGroupSizeLocked(screenIndex);

        boolean shouldShowDelete = isInRepeat && !shouldShowPicker && !isGroupSizeLocked;
        showDeleteButton(shouldShowDelete);

        boolean shouldShowAdd = shouldShowPicker && !isGroupSizeLocked;
        showAddButton(shouldShowAdd);

        boolean shouldShowGoUp = !isAtBeginning;
        showGoUpButton(shouldShowGoUp);
    }

    /**
     * Returns true if the current index is a group that's designated as `noAddRemove`
     * (e.g. if `jr:count` is explicitly set).
     */
    private boolean isGroupSizeLocked(FormIndex index) {
        FormController formController = Collect.getInstance().getFormController();
        IFormElement element = formController.getCaptionPrompt(index).getFormElement();
        return element instanceof GroupDef && ((GroupDef) element).noAddRemove;
    }

    /** Override to disable this button. */
    protected void showDeleteButton(boolean shouldShow) {
        optionsMenu.findItem(R.id.menu_delete_child).setVisible(shouldShow);
    }

    /** Override to disable this button. */
    protected void showAddButton(boolean shouldShow) {
        optionsMenu.findItem(R.id.menu_add_child).setVisible(shouldShow);
    }

    /** Override to disable this button. */
    protected void showGoUpButton(boolean shouldShow) {
        optionsMenu.findItem(R.id.menu_go_up).setVisible(shouldShow);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_delete_child:
                DialogUtils.showDeleteRepeatConfirmDialog(this, () -> {
                    if (didDeleteLastRepeatItem()) {
                        // goUpLevel would put us in a weird state after deleting the last item;
                        // just go back one event instead.
                        //
                        // TODO: This works well in most cases, but if there are 2 repeats in a row,
                        //   and you delete an item from the second repeat, it will send you into the
                        //   first repeat instead of going back a level as expected.
                        goToPreviousEvent();
                    } else {
                        goUpLevel();
                    }
                }, null);
                return true;

            case R.id.menu_add_child:
                FormIndex repeatPromptIndex = getRepeatPromptIndex(repeatGroupPickerIndex);
                exitToIndex(repeatPromptIndex);
                return true;

            case R.id.menu_go_up:
                goUpLevel();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Configure the navigation buttons at the bottom of the screen.
     */
    void configureButtons(FormController formController) {
        jumpBeginningButton.setOnClickListener(v -> {
            formController.getAuditEventLogger().exitView();
            formController.jumpToIndex(FormIndex.createBeginningOfFormIndex());

            setResult(RESULT_OK);
            finish();
        });

        jumpEndButton.setOnClickListener(v -> {
            formController.getAuditEventLogger().exitView();
            formController.jumpToIndex(FormIndex.createEndOfFormIndex());

            setResult(RESULT_OK);
            finish();
        });
    }

    /**
     * After having deleted the current index,
     * returns true if the current index was the only item in the repeat group.
     */
    private boolean didDeleteLastRepeatItem() {
        FormController formController = Collect.getInstance().getFormController();
        FormIndex index = formController.getFormIndex();
        int event = formController.getEvent(index);

        // If we're on item 0, but we will be prompted to add another item next,
        // it must be the last remaining item.
        return event == FormEntryController.EVENT_PROMPT_NEW_REPEAT
                && index.getElementMultiplicity() == 0;
    }

    /**
     * Similar to {@link #goUpLevel}, but makes a less significant step backward.
     * This is only used when the caller knows where to go back to,
     * e.g. after deleting the final remaining item in a repeat group.
     */
    private void goToPreviousEvent() {
        FormController formController = Collect.getInstance().getFormController();
        try {
            formController.stepToPreviousScreenEvent();
        } catch (JavaRosaException e) {
            Timber.d(e);
            createErrorDialog(e.getCause().getMessage());
            return;
        }

        refreshView();
    }

    /**
     * Navigates "up" in the form hierarchy.
     */
    protected void goUpLevel() {
        FormController formController = Collect.getInstance().getFormController();

        // If `repeatGroupPickerIndex` is set it means we're currently displaying
        // a list of repeat instances. If we unset `repeatGroupPickerIndex`,
        // we will go back up to the previous screen.
        if (shouldShowRepeatGroupPicker()) {
            // Exit the picker.
            repeatGroupPickerIndex = null;
        } else {
            // Enter the picker if coming from a repeat group.
            int event = formController.getEvent(screenIndex);
            if (event == FormEntryController.EVENT_REPEAT || event == FormEntryController.EVENT_PROMPT_NEW_REPEAT) {
                repeatGroupPickerIndex = screenIndex;
            }

            formController.stepToOuterScreenEvent();
        }

        refreshView(true);
    }

    /**
     * Returns a string representing the 'path' of the current screen.
     * Each level is separated by `>`.
     */
    private String getCurrentPath() {
        FormController formController = Collect.getInstance().getFormController();
        FormIndex index = formController.getFormIndex();

        // Step out to the enclosing group if the current index is something
        // we don't want to display in the path (e.g. a question name or the
        // very first group in a form which is auto-entered).
        if (formController.getEvent(index) == FormEntryController.EVENT_QUESTION
                || formController.stepIndexOut(index) == null) {
            index = formController.stepIndexOut(index);
        }

        List<FormEntryCaption> groups = new ArrayList<>();

        if (shouldShowRepeatGroupPicker()) {
            groups.add(formController.getCaptionPrompt(repeatGroupPickerIndex));
        }

        while (index != null) {
            groups.add(0, formController.getCaptionPrompt(index));
            index = formController.stepIndexOut(index);
        }

        // If the repeat picker is showing, don't show an item number for the current index.
        boolean hideLastMultiplicity = shouldShowRepeatGroupPicker();

        return ODKView.getGroupsPath(groups.toArray(new FormEntryCaption[groups.size()]), hideLastMultiplicity);
    }

    /**
     * Return the index of the "prompt" to add a new child to the given repeat group,
     * without changing the current index.
     */
    private FormIndex getRepeatPromptIndex(FormIndex repeatIndex) {
        FormController formController = Collect.getInstance().getFormController();
        FormIndex originalIndex = formController.getFormIndex();

        // Temporarily jump to the specified repeat group.
        formController.jumpToIndex(repeatIndex);
        String repeatRef = getGroupRef(repeatIndex).toString(false);
        String testRef = "";

        // There may be nested repeat groups within this group; skip over those.
        while (!repeatRef.equals(testRef)) {
            int event = formController.stepToNextEventType(FormEntryController.EVENT_PROMPT_NEW_REPEAT);

            if (event == FormEntryController.EVENT_END_OF_FORM) {
                Timber.w("Failed to find repeat prompt, got end of form instead.");
                break;
            }

            testRef = getGroupRef(formController.getFormIndex()).toString(false);
        }

        FormIndex result = formController.getFormIndex();

        // Reset to where we started from.
        formController.jumpToIndex(originalIndex);

        return result;
    }

    /**
     * Goes to the start of the hierarchy view based on where the user came from.
     * Backs out until the index is at the beginning of a repeat group or the beginning of the form.
     */
    private void jumpToHierarchyStartIndex() {
        FormController formController = Collect.getInstance().getFormController();
        FormIndex startIndex = formController.getFormIndex();

        // If we're not at the first level, we're inside a repeated group so we want to only
        // display everything enclosed within that group.
        contextGroupRef = null;

        // Save the index to the screen itself, before potentially moving into it.
        screenIndex = startIndex;

        // If we're currently at a displayable group, record the name of the node and step to the next
        // node to display.
        if (formController.isDisplayableGroup(startIndex)) {
            contextGroupRef = getGroupRef(formController);
            formController.stepToNextEvent(FormController.STEP_INTO_GROUP);
        } else {
            FormIndex potentialStartIndex = formController.stepIndexOut(startIndex);
            // Step back until we hit a displayable group or the beginning.
            while (!isScreenEvent(formController, potentialStartIndex)) {
                potentialStartIndex = formController.stepIndexOut(potentialStartIndex);
            }

            screenIndex = potentialStartIndex;

            // Check to see if the question is at the first level of the hierarchy.
            // If it is, display the root level from the beginning.
            // Otherwise we're at a displayable group.
            if (screenIndex == null) {
                screenIndex = FormIndex.createBeginningOfFormIndex();
            }

            formController.jumpToIndex(screenIndex);

            // Now test again. This should be true at this point or we're at the beginning.
            if (formController.isDisplayableGroup(formController.getFormIndex())) {
                contextGroupRef = getGroupRef(formController);
                formController.stepToNextEvent(FormController.STEP_INTO_GROUP);
            } else {
                // Let contextGroupRef be null.
            }
        }
    }

    /**
     * Returns true if the event is a displayable group or the start of the form.
     * See {@link FormController#stepToOuterScreenEvent} for more context.
     */
    private boolean isScreenEvent(FormController formController, FormIndex index) {
        // Beginning of form.
        if (index == null) {
            return true;
        }

        return formController.isDisplayableGroup(index);
    }

    private TreeReference getGroupRef(FormController formController) {
        return getGroupRef(formController.getFormIndex());
    }

    private TreeReference getGroupRef(FormIndex index) {
        return index.getReference();
    }

    private boolean shouldShowRepeatGroupPicker() {
        return repeatGroupPickerIndex != null;
    }

    /**
     * Rebuilds the view to reflect the elements that should be displayed based on the
     * FormController's current index. This index is either set prior to the activity opening or
     * mutated by {@link #onElementClick(HierarchyElement)} if a repeat instance was tapped.
     */
    public void refreshView() {
        refreshView(false);
    }

    /**
     * @see #refreshView()
     */
    private void refreshView(boolean isGoingUp) {
        try {
            FormController formController = Collect.getInstance().getFormController();

            // Save the current index so we can return to the problematic question
            // in the event of an error.
            currentIndex = formController.getFormIndex();

            elementsToDisplay = new ArrayList<>();

            jumpToHierarchyStartIndex();
            updateOptionsMenu();

            int event = formController.getEvent();

            if (event == FormEntryController.EVENT_BEGINNING_OF_FORM && !shouldShowRepeatGroupPicker()) {
                // The beginning of form has no valid prompt to display.
                groupPathTextView.setVisibility(View.GONE);
            } else {
                groupPathTextView.setVisibility(View.VISIBLE);
                groupPathTextView.setText(getCurrentPath());
            }

            // Refresh the current event in case we did step forward.
            event = formController.getEvent();

            // Ref to the parent group that's currently being displayed.
            //
            // Because of the guard conditions below, we will skip
            // everything until we exit this group.
            TreeReference visibleGroupRef = null;

            while (event != FormEntryController.EVENT_END_OF_FORM) {
                // get the ref to this element
                TreeReference currentRef = getGroupRef(formController);

                // retrieve the current group
                TreeReference curGroup = (visibleGroupRef == null) ? contextGroupRef : visibleGroupRef;

                if (curGroup != null && !curGroup.isParentOf(currentRef, false)) {
                    // We have left the current group
                    if (visibleGroupRef == null) {
                        // We are done.
                        break;
                    } else {
                        // exit the inner group
                        visibleGroupRef = null;
                    }
                }

                if (visibleGroupRef != null) {
                    // We're in a group within the one we want to list
                    // skip this question/group/repeat and move to the next index.
                    event =
                            formController.stepToNextEvent(FormController.STEP_INTO_GROUP);
                    continue;
                }

                switch (event) {
                    case FormEntryController.EVENT_QUESTION: {
                        // Nothing but repeat group instances should show up in the picker.
                        if (shouldShowRepeatGroupPicker()) {
                            break;
                        }

                        FormEntryPrompt fp = formController.getQuestionPrompt();
                        String label = fp.getShortText();
                        String answerDisplay = FormEntryPromptUtils.getAnswerText(fp, this, formController);
                        elementsToDisplay.add(
                                new HierarchyElement(FormEntryPromptUtils.markQuestionIfIsRequired(label, fp.isRequired()), answerDisplay, null,
                                        HierarchyElement.Type.QUESTION, fp.getIndex()));
                        break;
                    }
                    case FormEntryController.EVENT_GROUP: {
                        if (!formController.isGroupRelevant()) {
                            break;
                        }
                        // Nothing but repeat group instances should show up in the picker.
                        if (shouldShowRepeatGroupPicker()) {
                            break;
                        }

                        FormIndex index = formController.getFormIndex();

                        // Only display groups with a specific appearance attribute.
                        if (!formController.isDisplayableGroup(index)) {
                            break;
                        }

                        // Don't render other groups' children.
                        if (contextGroupRef != null && !contextGroupRef.isParentOf(currentRef, false)) {
                            break;
                        }

                        visibleGroupRef = currentRef;

                        FormEntryCaption caption = formController.getCaptionPrompt();
                        HierarchyElement groupElement = new HierarchyElement(
                                caption.getShortText(), getString(R.string.group_label),
                                ContextCompat.getDrawable(this, R.drawable.ic_folder_open),
                                HierarchyElement.Type.VISIBLE_GROUP, caption.getIndex());
                        elementsToDisplay.add(groupElement);

                        // Skip to the next item outside the group.
                        event = formController.stepOverGroup();
                        continue;
                    }
                    case FormEntryController.EVENT_PROMPT_NEW_REPEAT: {
                        // this would display the 'add new repeat' dialog
                        // ignore it.
                        break;
                    }
                    case FormEntryController.EVENT_REPEAT: {
                        if (!formController.isGroupRelevant()) {
                            break;
                        }

                        visibleGroupRef = currentRef;

                        FormEntryCaption fc = formController.getCaptionPrompt();

                        // Don't render other groups' children.
                        if (contextGroupRef != null && !contextGroupRef.isParentOf(currentRef, false)) {
                            break;
                        }

                        if (shouldShowRepeatGroupPicker()) {
                            // Don't render other groups' instances.
                            String repeatGroupPickerRef = getGroupRef(repeatGroupPickerIndex).toString(false);
                            if (!currentRef.toString(false).equals(repeatGroupPickerRef)) {
                                break;
                            }

                            int itemNumber = fc.getMultiplicity() + 1;

                            // e.g. `friends > 1`
                            String repeatLabel = fc.getShortText() + " > " + itemNumber;

                            // If the child of the group has a more descriptive label, use that instead.
                            if (fc.getFormElement().getChildren().size() == 1 && fc.getFormElement().getChild(0) instanceof GroupDef) {
                                formController.stepToNextEvent(FormController.STEP_INTO_GROUP);
                                String itemLabel = formController.getCaptionPrompt().getShortText();
                                if (itemLabel != null) {
                                    // e.g. `1. Alice`
                                    repeatLabel = itemNumber + ".\u200E " + itemLabel;
                                }
                            }

                            HierarchyElement instance = new HierarchyElement(
                                    repeatLabel, null,
                                    null, HierarchyElement.Type.REPEAT_INSTANCE, fc.getIndex());
                            elementsToDisplay.add(instance);
                        } else if (fc.getMultiplicity() == 0) {
                            // Display the repeat header for the group.
                            HierarchyElement group = new HierarchyElement(
                                    fc.getShortText(), getString(R.string.repeatable_group_label),
                                    ContextCompat.getDrawable(this, R.drawable.ic_repeat),
                                    HierarchyElement.Type.REPEATABLE_GROUP, fc.getIndex());
                            elementsToDisplay.add(group);
                        }

                        break;
                    }
                }

                event = formController.stepToNextEvent(FormController.STEP_INTO_GROUP);
            }

            recyclerView.setAdapter(new HierarchyListAdapter(elementsToDisplay, this::onElementClick));

            formController.jumpToIndex(currentIndex);

            // Prevent a redundant middle screen (common on many forms
            // that use presentation groups to display labels).
            if (isDisplayingSingleGroup() && !screenIndex.isBeginningOfFormIndex()) {
                if (isGoingUp) {
                    // Back out once more.
                    goUpLevel();
                } else {
                    // Enter automatically.
                    formController.jumpToIndex(elementsToDisplay.get(0).getFormIndex());
                    refreshView();
                }
            }
        } catch (Exception e) {
            Timber.e(e);
            createErrorDialog(e.getMessage());
        }
    }

    /**
     * Returns true if there's only one item being displayed, and it's a group.
     * Groups like this are often used to display a label in the hierarchy path.
     */
    private boolean isDisplayingSingleGroup() {
        return elementsToDisplay.size() == 1
                && elementsToDisplay.get(0).getType() == HierarchyElement.Type.VISIBLE_GROUP;
    }

    /**
     * Handles clicks on a specific row in the hierarchy view.
     */
    public void onElementClick(HierarchyElement element) {
        FormIndex index = element.getFormIndex();

        switch (element.getType()) {
            case QUESTION:
                onQuestionClicked(index);
                break;
            case REPEATABLE_GROUP:
                // Show the picker.
                repeatGroupPickerIndex = index;
                refreshView();
                break;
            case VISIBLE_GROUP:
            case REPEAT_INSTANCE:
                // Hide the picker.
                repeatGroupPickerIndex = null;
                Collect.getInstance().getFormController().jumpToIndex(index);
                setResult(RESULT_OK);
                refreshView();
                break;
        }
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
     * Jumps to the form filling view with the given index shown.
     */
    void exitToIndex(FormIndex index) {
        Collect.getInstance().getFormController().jumpToIndex(index);
        setResult(RESULT_OK);
        finish();
    }

    /**
     * When the device back button is pressed, go back to the previous activity, NOT the previous
     * level in the hierarchy as the "Go Up" button does.
     */
    @Override
    public void onBackPressed() {
        FormController formController = Collect.getInstance().getFormController();
        if (formController != null) {
            formController.getAuditEventLogger().exitView();
            formController.jumpToIndex(startIndex);
        }

        onBackPressedWithoutLogger();
    }

    protected void onBackPressedWithoutLogger() {
        super.onBackPressed();
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
}
