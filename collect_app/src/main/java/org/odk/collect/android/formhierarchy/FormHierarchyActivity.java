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

package org.odk.collect.android.formhierarchy;

import static org.odk.collect.android.javarosawrapper.FormIndexUtils.getPreviousLevel;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.GroupDef;
import org.javarosa.core.model.IFormElement;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.form.api.FormEntryCaption;
import org.javarosa.form.api.FormEntryController;
import org.javarosa.form.api.FormEntryModel;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.analytics.Analytics;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.FormEntryViewModelFactory;
import org.odk.collect.android.entities.EntitiesRepositoryProvider;
import org.odk.collect.android.exception.JavaRosaException;
import org.odk.collect.android.formentry.FormEntryViewModel;
import org.odk.collect.android.formentry.FormSessionRepository;
import org.odk.collect.android.formentry.ODKView;
import org.odk.collect.android.formentry.repeats.DeleteRepeatDialogFragment;
import org.odk.collect.android.injection.DaggerUtils;
import org.odk.collect.android.instancemanagement.InstancesDataService;
import org.odk.collect.android.instancemanagement.autosend.AutoSendSettingsProvider;
import org.odk.collect.android.javarosawrapper.FormController;
import org.odk.collect.android.javarosawrapper.JavaRosaFormController;
import org.odk.collect.android.projects.ProjectsDataService;
import org.odk.collect.android.utilities.ApplicationConstants;
import org.odk.collect.android.utilities.ChangeLockProvider;
import org.odk.collect.android.utilities.CollectStrictMode;
import org.odk.collect.android.utilities.FormEntryPromptUtils;
import org.odk.collect.android.utilities.FormsRepositoryProvider;
import org.odk.collect.android.utilities.HtmlUtils;
import org.odk.collect.android.utilities.InstancesRepositoryProvider;
import org.odk.collect.android.utilities.MediaUtils;
import org.odk.collect.android.utilities.SavepointsRepositoryProvider;
import org.odk.collect.androidshared.ui.DialogFragmentUtils;
import org.odk.collect.androidshared.ui.FragmentFactoryBuilder;
import org.odk.collect.androidshared.ui.multiclicksafe.MultiClickGuard;
import org.odk.collect.async.Scheduler;
import org.odk.collect.audiorecorder.recording.AudioRecorder;
import org.odk.collect.lists.EmptyListView;
import org.odk.collect.location.LocationClient;
import org.odk.collect.permissions.PermissionsChecker;
import org.odk.collect.permissions.PermissionsProvider;
import org.odk.collect.printer.HtmlPrinter;
import org.odk.collect.qrcode.QRCodeCreatorImpl;
import org.odk.collect.settings.SettingsProvider;
import org.odk.collect.strings.localization.LocalizedActivity;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import timber.log.Timber;

public class FormHierarchyActivity extends LocalizedActivity implements DeleteRepeatDialogFragment.DeleteRepeatDialogCallback {

    public static final int RESULT_ADD_REPEAT = 2;
    public static final String EXTRA_SESSION_ID = "session_id";
    /**
     * The questions and repeats at the current level.
     * Recreated every time {@link #refreshView()} is called.
     */
    private List<HierarchyItem> elementsToDisplay;

    /**
     * The icon shown at the top of a hierarchy screen for groups of questions.
     */
    private ImageView groupIcon;

    /**
     * The label shown at the top of a hierarchy screen for groups of questions. Set by
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

    private FormEntryViewModel formEntryViewModel;

    @Inject
    Scheduler scheduler;

    @Inject
    FormSessionRepository formSessionRepository;

    @Inject
    MediaUtils mediaUtils;

    @Inject
    Analytics analytics;

    @Inject
    AudioRecorder audioRecorder;

    @Inject
    ProjectsDataService projectsDataService;

    @Inject
    EntitiesRepositoryProvider entitiesRepositoryProvider;

    @Inject
    PermissionsChecker permissionsChecker;

    @Inject
    LocationClient fusedLocationClient;

    @Inject
    SettingsProvider settingsProvider;

    @Inject
    PermissionsProvider permissionsProvider;

    @Inject
    public AutoSendSettingsProvider autoSendSettingsProvider;

    @Inject
    public InstancesRepositoryProvider instancesRepositoryProvider;

    @Inject
    public FormsRepositoryProvider formsRepositoryProvider;

    @Inject
    public SavepointsRepositoryProvider savepointsRepositoryProvider;

    @Inject
    public InstancesDataService instancesDataService;

    @Inject
    public ChangeLockProvider changeLockProvider;

    protected final OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            FormController formController = formEntryViewModel.getFormController();
            if (formController != null) {
                formController.getAuditEventLogger().flush();
                navigateToTheLastRelevantIndex(formController);
            }
            finish();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        CollectStrictMode.disable();

        DaggerUtils.getComponent(this).inject(this);

        String sessionId = getIntent().getStringExtra(EXTRA_SESSION_ID);
        FormEntryViewModelFactory viewModelFactory = new FormEntryViewModelFactory(this,
                ApplicationConstants.FormModes.EDIT_SAVED,
                sessionId,
                scheduler,
                formSessionRepository,
                mediaUtils,
                audioRecorder,
                projectsDataService,
                entitiesRepositoryProvider,
                settingsProvider,
                permissionsChecker,
                fusedLocationClient,
                permissionsProvider,
                autoSendSettingsProvider,
                formsRepositoryProvider,
                instancesRepositoryProvider,
                savepointsRepositoryProvider,
                new QRCodeCreatorImpl(),
                new HtmlPrinter(),
                instancesDataService,
                changeLockProvider
        );

        this.getSupportFragmentManager().setFragmentFactory(new FragmentFactoryBuilder()
                .forClass(DeleteRepeatDialogFragment.class, () -> new DeleteRepeatDialogFragment(viewModelFactory))
                .build());

        super.onCreate(savedInstanceState);
        setContentView(R.layout.hierarchy_layout);

        recyclerView = findViewById(R.id.list);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        EmptyListView emptyView = findViewById(android.R.id.empty);
        Toolbar toolbar = findViewById(org.odk.collect.androidshared.R.id.toolbar);
        setSupportActionBar(toolbar);

        formEntryViewModel = new ViewModelProvider(this, viewModelFactory).get(FormEntryViewModel.class);

        FormController formController = formEntryViewModel.getFormController();
        if (formController == null) {
            finish();
            return;
        }

        startIndex = formController.getFormIndex();

        setTitle(formController.getFormTitle());

        groupIcon = findViewById(R.id.group_icon);
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
                for (HierarchyItem hierarchyItem : elementsToDisplay) {
                    FormIndex indexToCheck = hierarchyItem.getFormIndex();
                    if (startIndex.equals(indexToCheck)
                            || (formController.indexIsInFieldList(startIndex) && indexToCheck.toString().startsWith(startIndex.toString()))) {
                        position = elementsToDisplay.indexOf(hierarchyItem);
                        break;
                    }
                }
                ((LinearLayoutManager) recyclerView.getLayoutManager()).scrollToPositionWithOffset(position, 0);
            });
        }

        getOnBackPressedDispatcher().addCallback(onBackPressedCallback);
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
        FormController formController = formEntryViewModel.getFormController();

        // Not ready yet. Menu will be updated automatically once it's been prepared.
        if (optionsMenu == null || formController == null) {
            return;
        }

        boolean isAtBeginning = screenIndex.isBeginningOfFormIndex() && !shouldShowRepeatGroupPicker();
        boolean shouldShowPicker = shouldShowRepeatGroupPicker();
        boolean isInRepeat = formController.indexContainsRepeatableGroup(screenIndex);
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
        FormController formController = formEntryViewModel.getFormController();
        IFormElement element = formController.getCaptionPrompt(index).getFormElement();
        return element instanceof GroupDef && ((GroupDef) element).noAddRemove;
    }

    /**
     * Override to disable this button.
     */
    protected void showDeleteButton(boolean shouldShow) {
        optionsMenu.findItem(R.id.menu_delete_child).setVisible(shouldShow);
    }

    /**
     * Override to disable this button.
     */
    protected void showAddButton(boolean shouldShow) {
        optionsMenu.findItem(R.id.menu_add_repeat).setVisible(shouldShow);
    }

    /**
     * Override to disable this button.
     */
    protected void showGoUpButton(boolean shouldShow) {
        optionsMenu.findItem(R.id.menu_go_up).setVisible(shouldShow);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (!MultiClickGuard.allowClickFast(item.toString())) {
            return true;
        }

        if (item.getItemId() == R.id.menu_delete_child) {
            DialogFragmentUtils.showIfNotShowing(DeleteRepeatDialogFragment.class, getSupportFragmentManager());
            return true;
        } else if (item.getItemId() == R.id.menu_add_repeat) {
            formEntryViewModel.getFormController().jumpToIndex(repeatGroupPickerIndex);
            formEntryViewModel.jumpToNewRepeat();
            formEntryViewModel.addRepeat();

            finish();
            return true;
        } else if (item.getItemId() == R.id.menu_go_up) {
            goUpLevel();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Configure the navigation buttons at the bottom of the screen.
     */
    void configureButtons(FormController formController) {
        jumpBeginningButton.setOnClickListener(v -> {
            formController.getAuditEventLogger().flush();
            formController.jumpToIndex(FormIndex.createBeginningOfFormIndex());

            setResult(RESULT_OK);
            finish();
        });

        jumpEndButton.setOnClickListener(v -> {
            formController.getAuditEventLogger().flush();
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
        FormController formController = formEntryViewModel.getFormController();
        FormIndex index = formController.getFormIndex();
        int event = formController.getEvent(index);

        // If we're on item 0, but we will be prompted to add another item next,
        // it must be the last remaining item.
        return event == FormEntryController.EVENT_PROMPT_NEW_REPEAT
                && index.getElementMultiplicity() == 0;
    }

    private boolean didDeleteFirstRepeatItem() {
        return formEntryViewModel
                .getFormController()
                .getFormIndex()
                .getElementMultiplicity() == 0;
    }

    /**
     * Similar to {@link #goUpLevel}, but makes a less significant step backward.
     * This is only used when the caller knows where to go back to,
     * e.g. after deleting the final remaining item in a repeat group.
     */
    private void goToPreviousEvent() {
        FormController formController = formEntryViewModel.getFormController();
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
        FormController formController = formEntryViewModel.getFormController();

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
    private CharSequence getCurrentPath() {
        FormController formController = formEntryViewModel.getFormController();
        FormIndex index = screenIndex;

        List<FormEntryCaption> groups = new ArrayList<>();

        if (shouldShowRepeatGroupPicker()) {
            groups.add(formController.getCaptionPrompt(repeatGroupPickerIndex));
        }

        while (index != null) {
            groups.add(0, formController.getCaptionPrompt(index));
            index = getPreviousLevel(index);
        }

        // If the repeat picker is showing, don't show an item number for the current index.
        boolean hideLastMultiplicity = shouldShowRepeatGroupPicker();

        return ODKView.getGroupsPath(groups.toArray(new FormEntryCaption[0]), hideLastMultiplicity);
    }

    /**
     * Goes to the start of the hierarchy view based on where the user came from.
     * Backs out until the index is at the beginning of a repeat group or the beginning of the form.
     */
    private void jumpToHierarchyStartIndex() {
        FormController formController = formEntryViewModel.getFormController();
        FormIndex startIndex = formController.getFormIndex();

        // If we're not at the first level, we're inside a repeated group so we want to only
        // display everything enclosed within that group.
        contextGroupRef = null;

        // Save the index to the screen itself, before potentially moving into it.
        screenIndex = startIndex;

        // If we're currently at a displayable group, record the name of the node and step to the next
        // node to display.
        if (formController.isDisplayableGroup(startIndex)) {
            contextGroupRef = formController.getFormIndex().getReference();
            formController.stepToNextEvent(JavaRosaFormController.STEP_INTO_GROUP);
        } else {
            FormIndex potentialStartIndex = getPreviousLevel(startIndex);
            // Step back until we hit a displayable group or the beginning.
            while (!isScreenEvent(formController, potentialStartIndex)) {
                potentialStartIndex = getPreviousLevel(potentialStartIndex);
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
                contextGroupRef = formController.getFormIndex().getReference();
                formController.stepToNextEvent(JavaRosaFormController.STEP_INTO_GROUP);
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

    private boolean shouldShowRepeatGroupPicker() {
        return repeatGroupPickerIndex != null;
    }

    /**
     * Rebuilds the view to reflect the elements that should be displayed based on the
     * FormController's current index. This index is either set prior to the activity opening or
     * mutated by {@link #onElementClick(HierarchyItem)} if a repeat instance was tapped.
     */
    public void refreshView() {
        refreshView(false);
    }

    /**
     * @see #refreshView()
     */
    private void refreshView(boolean isGoingUp) {
        try {
            FormController formController = formEntryViewModel.getFormController();

            // Save the current index so we can return to the problematic question
            // in the event of an error.
            currentIndex = formController.getFormIndex();

            elementsToDisplay = new ArrayList<>();

            jumpToHierarchyStartIndex();
            updateOptionsMenu();

            int event = formController.getEvent();

            if (event == FormEntryController.EVENT_BEGINNING_OF_FORM && !shouldShowRepeatGroupPicker()) {
                // The beginning of form has no valid prompt to display.
                groupIcon.setVisibility(View.GONE);
                groupPathTextView.setVisibility(View.GONE);
            } else {
                groupIcon.setVisibility(View.VISIBLE);
                groupPathTextView.setVisibility(View.VISIBLE);
                groupPathTextView.setText(getCurrentPath());

                if (formController.indexContainsRepeatableGroup(screenIndex) || shouldShowRepeatGroupPicker()) {
                    groupIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_repeat));
                } else {
                    groupIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_folder_open));
                }
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
                TreeReference currentRef = formController.getFormIndex().getReference();

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
                            formController.stepToNextEvent(JavaRosaFormController.STEP_INTO_GROUP);
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
                        String answerDisplay = QuestionAnswerProcessor.getQuestionAnswer(fp, this, formController);
                        elementsToDisplay.add(
                            new HierarchyItem(
                                fp.getIndex(),
                                HierarchyItemType.QUESTION,
                                FormEntryPromptUtils.styledQuestionText(label, fp.isRequired()),
                                answerDisplay
                            )
                        );
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

                        elementsToDisplay.add(
                            new HierarchyItem(
                                caption.getIndex(),
                                HierarchyItemType.VISIBLE_GROUP,
                                HtmlUtils.textToHtml(caption.getShortText())
                            )
                        );

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
                        boolean forPicker = shouldShowRepeatGroupPicker();
                        // Only break to exclude non-relevant repeat from picker
                        if (!formController.isGroupRelevant() && forPicker) {
                            break;
                        }

                        visibleGroupRef = currentRef;

                        // Don't render other groups' children.
                        if (contextGroupRef != null && !contextGroupRef.isParentOf(currentRef, false)) {
                            break;
                        }

                        FormEntryCaption fc = formController.getCaptionPrompt();

                        if (forPicker) {
                            // Don't render other groups' instances.
                            String repeatGroupPickerRef = repeatGroupPickerIndex.getReference().toString(false);
                            if (!currentRef.toString(false).equals(repeatGroupPickerRef)) {
                                break;
                            }

                            int itemNumber = fc.getMultiplicity() + 1;

                            // e.g. `friends > 1`
                            String repeatLabel = fc.getShortText() + " > " + itemNumber;

                            // If the child of the group has a more descriptive label, use that instead.
                            if (fc.getFormElement().getChildren().size() == 1 && fc.getFormElement().getChild(0) instanceof GroupDef) {
                                formController.stepToNextEvent(JavaRosaFormController.STEP_INTO_GROUP);
                                String itemLabel = formController.getCaptionPrompt().getShortText();
                                if (itemLabel != null) {
                                    // e.g. `1. Alice`
                                    repeatLabel = itemNumber + ".\u200E " + itemLabel;
                                }
                            }

                            elementsToDisplay.add(
                                new HierarchyItem(
                                    fc.getIndex(),
                                    HierarchyItemType.REPEAT_INSTANCE,
                                    HtmlUtils.textToHtml(repeatLabel)
                                )
                            );
                        } else if (fc.getMultiplicity() == 0) {
                            elementsToDisplay.add(
                                new HierarchyItem(
                                    fc.getIndex(),
                                    HierarchyItemType.REPEATABLE_GROUP,
                                    HtmlUtils.textToHtml(fc.getShortText())
                                )
                            );
                        }

                        break;
                    }
                }

                event = formController.stepToNextEvent(JavaRosaFormController.STEP_INTO_GROUP);
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
                && elementsToDisplay.get(0).getHierarchyItemType() == HierarchyItemType.VISIBLE_GROUP;
    }

    /**
     * Handles clicks on a specific row in the hierarchy view.
     */
    public void onElementClick(HierarchyItem item) {
        FormIndex index = item.getFormIndex();

        switch (item.getHierarchyItemType()) {
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
                formEntryViewModel.getFormController().jumpToIndex(index);
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
        formEntryViewModel.getFormController().jumpToIndex(index);
        if (formEntryViewModel.getFormController().indexIsInFieldList()) {
            try {
                formEntryViewModel.getFormController().stepToPreviousScreenEvent();
            } catch (JavaRosaException e) {
                Timber.d(e);
                createErrorDialog(e.getCause().getMessage());
                return;
            }
        }
        setResult(RESULT_OK);
        finish();
    }

    private void navigateToTheLastRelevantIndex(FormController formController) {
        FormEntryController fec = new FormEntryController(new FormEntryModel(formController.getFormDef()));
        formController.jumpToIndex(startIndex);

        // startIndex might no longer exist if it was a part of repeat group that has been removed
        while (true) {
            boolean isBeginningOfFormIndex = formController.getFormIndex().isBeginningOfFormIndex();
            boolean isEndOfFormIndex = formController.getFormIndex().isEndOfFormIndex();
            boolean isIndexRelevant = isBeginningOfFormIndex
                    || isEndOfFormIndex
                    || fec.getModel().isIndexRelevant(formController.getFormIndex());
            boolean isPromptNewRepeatEvent = formController.getEvent() == FormEntryController.EVENT_PROMPT_NEW_REPEAT;

            boolean shouldNavigateBack = !isIndexRelevant || isPromptNewRepeatEvent;

            if (shouldNavigateBack) {
                formController.stepToPreviousEvent();
            } else {
                break;
            }
        }
    }

    /**
     * Creates and displays dialog with the given errorMsg.
     */
    protected void createErrorDialog(String errorMsg) {
        AlertDialog alertDialog = new MaterialAlertDialogBuilder(this).create();

        alertDialog.setTitle(getString(org.odk.collect.strings.R.string.error_occured));
        alertDialog.setMessage(errorMsg);
        DialogInterface.OnClickListener errorListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                switch (i) {
                    case DialogInterface.BUTTON_POSITIVE:
                        FormController formController = formEntryViewModel.getFormController();
                        formController.jumpToIndex(currentIndex);
                        break;
                }
            }
        };
        alertDialog.setCancelable(false);
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(org.odk.collect.strings.R.string.ok), errorListener);
        alertDialog.show();
    }

    @Override
    public void deleteGroup() {
        if (didDeleteLastRepeatItem()) {
            // goUpLevel would put us in a weird state after deleting the last item;
            // just go back one event instead.
            //
            // TODO: This works well in most cases, but if there are 2 repeats in a row,
            //   and you delete an item from the second repeat, it will send you into the
            //   first repeat instead of going back a level as expected.
            goToPreviousEvent();
        } else if (didDeleteFirstRepeatItem()) {
            goUpLevel();
        } else {
            goToPreviousEvent();
            goUpLevel();
        }
    }

    @Override
    protected void onDestroy() {
        CollectStrictMode.enable();
        super.onDestroy();
    }
}
