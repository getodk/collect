package org.odk.collect.android.formhierarchy;

import static android.app.Activity.RESULT_OK;
import static org.odk.collect.android.formentry.repeats.DeleteRepeatDialogFragment.REQUEST_DELETE_REPEAT;
import static org.odk.collect.android.javarosawrapper.FormIndexUtils.getPreviousLevel;
import static org.odk.collect.androidshared.ui.SnackbarUtils.showSnackbar;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuHost;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
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
import org.odk.collect.android.R;
import org.odk.collect.android.databinding.FormHierarchyLayoutBinding;
import org.odk.collect.android.exception.JavaRosaException;
import org.odk.collect.android.formentry.FormEntryViewModel;
import org.odk.collect.android.formentry.ODKView;
import org.odk.collect.android.formentry.repeats.DeleteRepeatDialogFragment;
import org.odk.collect.android.formmanagement.FormFillingIntentFactory;
import org.odk.collect.android.instancemanagement.InstanceEditResult;
import org.odk.collect.android.instancemanagement.InstanceExtKt;
import org.odk.collect.android.instancemanagement.InstancesDataService;
import org.odk.collect.android.javarosawrapper.FormController;
import org.odk.collect.android.javarosawrapper.JavaRosaFormController;
import org.odk.collect.android.utilities.FormEntryPromptUtils;
import org.odk.collect.android.utilities.HtmlUtils;
import org.odk.collect.androidshared.ui.DialogFragmentUtils;
import org.odk.collect.androidshared.ui.SnackbarUtils;
import org.odk.collect.androidshared.ui.multiclicksafe.MultiClickGuard;
import org.odk.collect.async.Scheduler;
import org.odk.collect.forms.instances.Instance;
import org.odk.collect.material.MaterialProgressDialogFragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import timber.log.Timber;

public class FormHierarchyFragment extends Fragment {

    private final boolean viewOnly;
    private final ViewModelProvider.Factory viewModelFactory;
    private final MenuHost menuHost;
    private FormHiearchyMenuProvider menuProvider;
    private FormEntryViewModel formEntryViewModel;
    private FormHierarchyViewModel formHierarchyViewModel;
    /**
     * The index of the question or the field list the FormController was set to when the hierarchy
     * was accessed. Used to jump the user back to where they were if applicable.
     */
    private FormIndex startIndex;
    private final Scheduler scheduler;
    private final InstancesDataService instancesDataService;
    private final String currentProjectId;

    public FormHierarchyFragment(
            boolean viewOnly,
            ViewModelProvider.Factory viewModelFactory,
            MenuHost menuHost,
            Scheduler scheduler,
            InstancesDataService instancesDataService,
            String currentProjectId
    ) {
        super(R.layout.form_hierarchy_layout);
        this.viewOnly = viewOnly;
        this.viewModelFactory = viewModelFactory;
        this.menuHost = menuHost;
        this.scheduler = scheduler;
        this.instancesDataService = instancesDataService;
        this.currentProjectId = currentProjectId;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        formEntryViewModel = new ViewModelProvider(requireActivity(), viewModelFactory).get(FormEntryViewModel.class);
        formHierarchyViewModel = new ViewModelProvider(
                this,
                new FormHierarchyViewModel.Factory(scheduler)
        ).get(FormHierarchyViewModel.class);
        requireActivity().setTitle(formEntryViewModel.getFormController().getFormTitle());
        startIndex = formEntryViewModel.getFormController().getFormIndex();

        formHierarchyViewModel.getInstanceEditResult().observe(this, instanceEditResult -> {
            if (!instanceEditResult.isConsumed()) {
                instanceEditResult.consume();
                handleInstanceEditResult(instanceEditResult.getValue());
            }
        });
        MaterialProgressDialogFragment.showOn(this, formHierarchyViewModel.isEditingInstance(), getParentFragmentManager(), () -> {
            MaterialProgressDialogFragment dialog = new MaterialProgressDialogFragment();
            dialog.setMessage(getString(org.odk.collect.strings.R.string.preparing_form_edit));
            return dialog;
        });

        menuProvider = new FormHiearchyMenuProvider(formEntryViewModel, formHierarchyViewModel, viewOnly, context.getString(R.string.form_entry_screen), new FormHiearchyMenuProvider.OnClickListener() {
            @Override
            public void onEditClicked() {
                formHierarchyViewModel.editInstance(
                        formEntryViewModel.getFormController().getAbsoluteInstancePath(),
                        instancesDataService,
                        currentProjectId
                );
            }

            @Override
            public void onGoUpClicked() {
                FormController formController = formEntryViewModel.getFormController();

                // If `repeatGroupPickerIndex` is set it means we're currently displaying
                // a list of repeat instances. If we unset `repeatGroupPickerIndex`,
                // we will go back up to the previous screen.
                if (formHierarchyViewModel.shouldShowRepeatGroupPicker()) {
                    // Exit the picker.
                    formHierarchyViewModel.setRepeatGroupPickerIndex(null);
                } else {
                    // Enter the picker if coming from a repeat group.
                    FormIndex screenIndex = formHierarchyViewModel.getScreenIndex();
                    int event = formController.getEvent(screenIndex);
                    if (event == FormEntryController.EVENT_REPEAT || event == FormEntryController.EVENT_PROMPT_NEW_REPEAT) {
                        formHierarchyViewModel.setRepeatGroupPickerIndex(screenIndex);
                    }

                    formController.stepToOuterScreenEvent();
                }

                refreshView(true);
            }

            @Override
            public void onAddRepeatClicked() {
                formEntryViewModel.getFormController().jumpToIndex(formHierarchyViewModel.getRepeatGroupPickerIndex());
                formEntryViewModel.jumpToNewRepeat();
                formEntryViewModel.addRepeat();

                requireActivity().finish();
            }

            @Override
            public void onDeleteRepeatClicked() {
                DialogFragmentUtils.showIfNotShowing(DeleteRepeatDialogFragment.class, getChildFragmentManager());
            }
        });
    }

    private void handleInstanceEditResult(InstanceEditResult result) {
        Instance instance = result.getInstance();

        if (result instanceof InstanceEditResult.EditCompleted) {
            openEditedInstance(instance.getDbId());
        } else if (result instanceof InstanceEditResult.EditBlockedByNewerExistingEdit) {
            if (InstanceExtKt.isDraft(instance)) {
                showOpenDraftEditDialog(instance);
            } else {
                showOpenFinalizedEditDialog(instance);
            }
        }
    }

    private void openEditedInstance(long dbId) {
        Intent intent = FormFillingIntentFactory.editDraftFormIntent(requireContext(), currentProjectId, dbId);
        startActivity(intent);
        requireActivity().getOnBackPressedDispatcher().onBackPressed();
    }

    private void showOpenDraftEditDialog(Instance instance) {
        String dialogMessage = new SimpleDateFormat(
                getString(org.odk.collect.strings.R.string.newer_draft_edit_found_dialog_message),
                Locale.getDefault()
        ).format(instance.getLastStatusChangeDate());

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(org.odk.collect.strings.R.string.newer_draft_edit_found_dialog_title)
                .setMessage(dialogMessage)
                .setPositiveButton(org.odk.collect.strings.R.string.newer_draft_edit_found_dialog_positive_button, (dialog, which) -> {
                    Intent intent = FormFillingIntentFactory.editDraftFormIntent(requireContext(), currentProjectId, instance.getDbId());
                    startActivity(intent);
                    requireActivity().getOnBackPressedDispatcher().onBackPressed();
                })
                .setNegativeButton(org.odk.collect.strings.R.string.cancel, (dialog, which) -> {})
                .setCancelable(false)
                .show();
    }

    private void showOpenFinalizedEditDialog(Instance instance) {
        String dialogMessage = new SimpleDateFormat(
                getString(org.odk.collect.strings.R.string.newer_finalized_edit_found_dialog_message),
                Locale.getDefault()
        ).format(instance.getLastStatusChangeDate());

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(org.odk.collect.strings.R.string.newer_finalized_edit_found_dialog_title)
                .setMessage(dialogMessage)
                .setPositiveButton(org.odk.collect.strings.R.string.newer_finalized_edit_found_dialog_positive_button, (dialog, which) -> {
                    formHierarchyViewModel.editInstance(
                            instance.getInstanceFilePath(),
                            instancesDataService,
                            currentProjectId
                    );
                })
                .setNegativeButton(org.odk.collect.strings.R.string.cancel, (dialog, which) -> {})
                .setCancelable(false)
                .show();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        requireActivity().getOnBackPressedDispatcher().addCallback(
                getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        if (viewOnly) {
                            formEntryViewModel.exit();
                        } else {
                            FormController formController = formEntryViewModel.getFormController();
                            if (formController != null) {
                                formController.getAuditEventLogger().flush();
                                navigateToTheLastRelevantIndex(formController);
                            }
                        }

                        requireActivity().finish();
                    }
                }
        );

        formHierarchyViewModel.setStartIndex(formEntryViewModel.getFormController().getFormIndex());

        menuHost.addMenuProvider(menuProvider, getViewLifecycleOwner());

        FormHierarchyLayoutBinding binding = FormHierarchyLayoutBinding.bind(view);

        RecyclerView recyclerView = binding.list;
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));

        configureButtons(binding, formEntryViewModel.getFormController());
        refreshView();

        // Scroll to the last question the user was looking at
        // TODO: avoid another iteration through all displayed elements
        if (recyclerView != null && recyclerView.getAdapter() != null && recyclerView.getAdapter().getItemCount() > 0) {
            binding.empty.setVisibility(View.GONE);
            recyclerView.post(() -> {
                int position = 0;
                // Iterate over all the elements currently displayed looking for a match with the
                // startIndex which can either represent a question or a field list.
                List<HierarchyItem> elementsToDisplay = formHierarchyViewModel.getElementsToDisplay();
                for (HierarchyItem hierarchyItem : elementsToDisplay) {
                    FormIndex startIndex = formHierarchyViewModel.getStartIndex();
                    FormIndex indexToCheck = hierarchyItem.getFormIndex();
                    boolean indexIsInFieldList = formEntryViewModel.getFormController().indexIsInFieldList(startIndex);
                    if (startIndex.equals(indexToCheck)
                            || (indexIsInFieldList && indexToCheck.toString().startsWith(startIndex.toString()))) {
                        position = elementsToDisplay.indexOf(hierarchyItem);
                        break;
                    }
                }
                ((LinearLayoutManager) recyclerView.getLayoutManager()).scrollToPositionWithOffset(position, 0);
            });
        }

        getChildFragmentManager().setFragmentResultListener(REQUEST_DELETE_REPEAT, getViewLifecycleOwner(), (requestKey, result) -> onRepeatDeleted());

        boolean newEdit = FormHierarchyFragmentArgs.fromBundle(getArguments()).getNewEdit();
        if (newEdit) {
            showSnackbar(
                    view,
                    getString(org.odk.collect.strings.R.string.finalized_form_edit_started),
                    SnackbarUtils.DURATION_LONG,
                    null,
                    null,
                    true
            );
        }
    }

    public void refreshView() {
        refreshView(false);
    }

    /**
     * @see #refreshView()
     */
    private void refreshView(boolean isGoingUp) {
        FormHierarchyLayoutBinding binding = FormHierarchyLayoutBinding.bind(requireView());
        ImageView groupIcon = binding.groupIcon;
        TextView groupPathTextView = binding.pathtext;
        RecyclerView recyclerView = binding.list;

        try {
            FormController formController = formEntryViewModel.getFormController();

            // Save the current index so we can return to the problematic question
            // in the event of an error.
            formHierarchyViewModel.setCurrentIndex(formController.getFormIndex());

            calculateElementsToDisplay(formController, groupIcon, groupPathTextView);
            recyclerView.setAdapter(new HierarchyListAdapter(formHierarchyViewModel.getElementsToDisplay(), this::onElementClick));

            formController.jumpToIndex(formHierarchyViewModel.getCurrentIndex());

            // Prevent a redundant middle screen (common on many forms
            // that use presentation groups to display labels).
            if (isDisplayingSingleGroup() && !formHierarchyViewModel.getScreenIndex().isBeginningOfFormIndex()) {
                if (isGoingUp) {
                    // Back out once more.
                    goUpLevel();
                } else {
                    // Enter automatically.
                    formController.jumpToIndex(formHierarchyViewModel.getElementsToDisplay().get(0).getFormIndex());
                    refreshView();
                }
            }
        } catch (Exception e) {
            Timber.e(e);
            createErrorDialog(e.getMessage());
        }
    }

    private void calculateElementsToDisplay(FormController formController, ImageView groupIcon, TextView groupPathTextView) {
        List<HierarchyItem> elementsToDisplay = new ArrayList<>();

        jumpToHierarchyStartIndex();

        int event = formController.getEvent();

        if (event == FormEntryController.EVENT_BEGINNING_OF_FORM && !formHierarchyViewModel.shouldShowRepeatGroupPicker()) {
            // The beginning of form has no valid prompt to display.
            groupIcon.setVisibility(View.GONE);
            groupPathTextView.setVisibility(View.GONE);
        } else {
            groupIcon.setVisibility(View.VISIBLE);
            groupPathTextView.setVisibility(View.VISIBLE);
            groupPathTextView.setText(getCurrentPath());

            if (formController.indexContainsRepeatableGroup(formHierarchyViewModel.getScreenIndex()) || formHierarchyViewModel.shouldShowRepeatGroupPicker()) {
                groupIcon.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_repeat));
            } else {
                groupIcon.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_folder_open));
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
            TreeReference curGroup = (visibleGroupRef == null) ? formHierarchyViewModel.getContextGroupRef() : visibleGroupRef;

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
                    if (formHierarchyViewModel.shouldShowRepeatGroupPicker()) {
                        break;
                    }

                    FormEntryPrompt fp = formController.getQuestionPrompt();
                    String label = fp.getShortText();
                    String answerDisplay = QuestionAnswerProcessor.getQuestionAnswer(fp, requireContext(), formController);
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
                    if (formHierarchyViewModel.shouldShowRepeatGroupPicker()) {
                        break;
                    }

                    FormIndex index = formController.getFormIndex();

                    // Only display groups with a specific appearance attribute.
                    if (!formController.isDisplayableGroup(index)) {
                        break;
                    }

                    // Don't render other groups' children.
                    TreeReference contextGroupRef = formHierarchyViewModel.getContextGroupRef();
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
                    boolean forPicker = formHierarchyViewModel.shouldShowRepeatGroupPicker();
                    // Only break to exclude non-relevant repeat from picker
                    if (!formController.isGroupRelevant() && forPicker) {
                        break;
                    }

                    visibleGroupRef = currentRef;

                    // Don't render other groups' children.
                    TreeReference contextGroupRef = formHierarchyViewModel.getContextGroupRef();
                    if (contextGroupRef != null && !contextGroupRef.isParentOf(currentRef, false)) {
                        break;
                    }

                    FormEntryCaption fc = formController.getCaptionPrompt();

                    if (forPicker) {
                        // Don't render other groups' instances.
                        String repeatGroupPickerRef = formHierarchyViewModel.getRepeatGroupPickerIndex().getReference().toString(false);
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

        formHierarchyViewModel.setElementsToDisplay(elementsToDisplay);
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
        formHierarchyViewModel.setContextGroupRef(null);

        // Save the index to the screen itself, before potentially moving into it.
        formHierarchyViewModel.setScreenIndex(startIndex);

        // If we're currently at a displayable group, record the name of the node and step to the next
        // node to display.
        if (formController.isDisplayableGroup(startIndex)) {
            formHierarchyViewModel.setContextGroupRef(formController.getFormIndex().getReference());
            formController.stepToNextEvent(JavaRosaFormController.STEP_INTO_GROUP);
        } else {
            FormIndex potentialStartIndex = getPreviousLevel(startIndex);
            // Step back until we hit a displayable group or the beginning.
            while (!isScreenEvent(formController, potentialStartIndex)) {
                potentialStartIndex = getPreviousLevel(potentialStartIndex);
            }

            formHierarchyViewModel.setScreenIndex(potentialStartIndex);

            // Check to see if the question is at the first level of the hierarchy.
            // If it is, display the root level from the beginning.
            // Otherwise we're at a displayable group.
            if (formHierarchyViewModel.getScreenIndex() == null) {
                formHierarchyViewModel.setScreenIndex(FormIndex.createBeginningOfFormIndex());
            }

            formController.jumpToIndex(formHierarchyViewModel.getScreenIndex());

            // Now test again. This should be true at this point or we're at the beginning.
            if (formController.isDisplayableGroup(formController.getFormIndex())) {
                formHierarchyViewModel.setContextGroupRef(formController.getFormIndex().getReference());
                formController.stepToNextEvent(JavaRosaFormController.STEP_INTO_GROUP);
            } else {
                // Let contextGroupRef be null.
            }
        }

        menuHost.invalidateMenu();
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

    /**
     * Navigates "up" in the form hierarchy.
     */
    protected void goUpLevel() {
        FormController formController = formEntryViewModel.getFormController();

        // If `repeatGroupPickerIndex` is set it means we're currently displaying
        // a list of repeat instances. If we unset `repeatGroupPickerIndex`,
        // we will go back up to the previous screen.
        if (formHierarchyViewModel.shouldShowRepeatGroupPicker()) {
            // Exit the picker.
            formHierarchyViewModel.setRepeatGroupPickerIndex(null);
        } else {
            // Enter the picker if coming from a repeat group.
            int event = formController.getEvent(formHierarchyViewModel.getScreenIndex());
            if (event == FormEntryController.EVENT_REPEAT || event == FormEntryController.EVENT_PROMPT_NEW_REPEAT) {
                formHierarchyViewModel.setRepeatGroupPickerIndex(formHierarchyViewModel.getScreenIndex());
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
        FormIndex index = formHierarchyViewModel.getScreenIndex();

        List<FormEntryCaption> groups = new ArrayList<>();

        if (formHierarchyViewModel.shouldShowRepeatGroupPicker()) {
            groups.add(formController.getCaptionPrompt(formHierarchyViewModel.getRepeatGroupPickerIndex()));
        }

        while (index != null) {
            groups.add(0, formController.getCaptionPrompt(index));
            index = getPreviousLevel(index);
        }

        // If the repeat picker is showing, don't show an item number for the current index.
        boolean hideLastMultiplicity = formHierarchyViewModel.shouldShowRepeatGroupPicker();

        return ODKView.getGroupsPath(groups.toArray(new FormEntryCaption[0]), hideLastMultiplicity);
    }

    /**
     * Handles clicks on a specific row in the hierarchy view.
     */
    private void onElementClick(HierarchyItem item) {
        FormIndex index = item.getFormIndex();

        switch (item.getHierarchyItemType()) {
            case QUESTION:
                onQuestionClicked(index);
                break;
            case REPEATABLE_GROUP:
                // Show the picker.
                formHierarchyViewModel.setRepeatGroupPickerIndex(index);
                refreshView();
                break;
            case VISIBLE_GROUP:
            case REPEAT_INSTANCE:
                // Hide the picker.
                formHierarchyViewModel.setRepeatGroupPickerIndex(null);
                formEntryViewModel.getFormController().jumpToIndex(index);
                requireActivity().setResult(RESULT_OK);
                refreshView();
                break;
        }
    }

    /**
     * Handles clicks on a question. Jumps to the form filling view with the selected question shown.
     * If the selected question is in a field list, show the entire field list.
     */
    void onQuestionClicked(FormIndex index) {
        if (viewOnly) {
            return;
        }

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
        requireActivity().setResult(RESULT_OK);
        requireActivity().finish();
    }

    /**
     * Creates and displays dialog with the given errorMsg.
     */
    protected void createErrorDialog(String errorMsg) {
        AlertDialog alertDialog = new MaterialAlertDialogBuilder(requireContext()).create();

        alertDialog.setTitle(getString(org.odk.collect.strings.R.string.error_occured));
        alertDialog.setMessage(errorMsg);
        DialogInterface.OnClickListener errorListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                switch (i) {
                    case DialogInterface.BUTTON_POSITIVE:
                        FormController formController = formEntryViewModel.getFormController();
                        formController.jumpToIndex(formHierarchyViewModel.getCurrentIndex());
                        break;
                }
            }
        };
        alertDialog.setCancelable(false);
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(org.odk.collect.strings.R.string.ok), errorListener);
        alertDialog.show();
    }

    /**
     * Returns true if there's only one item being displayed, and it's a group.
     * Groups like this are often used to display a label in the hierarchy path.
     */
    private boolean isDisplayingSingleGroup() {
        return formHierarchyViewModel.getElementsToDisplay().size() == 1
                && formHierarchyViewModel.getElementsToDisplay().get(0).getHierarchyItemType() == HierarchyItemType.VISIBLE_GROUP;
    }

    private void configureButtons(FormHierarchyLayoutBinding binding, FormController formController) {
        Button exitButton = binding.exitButton;
        Button jumpBeginningButton = binding.jumpBeginningButton;
        Button jumpEndButton = binding.jumpEndButton;

        if (viewOnly) {
            exitButton.setOnClickListener(v -> requireActivity().getOnBackPressedDispatcher().onBackPressed());
            exitButton.setVisibility(View.VISIBLE);
            jumpBeginningButton.setVisibility(View.GONE);
            jumpEndButton.setVisibility(View.GONE);
        } else {
            jumpBeginningButton.setOnClickListener(v -> {
                formController.getAuditEventLogger().flush();
                formController.jumpToIndex(FormIndex.createBeginningOfFormIndex());

                requireActivity().setResult(RESULT_OK);
                requireActivity().finish();
            });

            jumpEndButton.setOnClickListener(v -> {
                formController.getAuditEventLogger().flush();
                formController.jumpToIndex(FormIndex.createEndOfFormIndex());

                requireActivity().setResult(RESULT_OK);
                requireActivity().finish();
            });
        }

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

    private void onRepeatDeleted() {
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

    private static class FormHiearchyMenuProvider implements MenuProvider {

        private final FormEntryViewModel formEntryViewModel;
        private final FormHierarchyViewModel formHierarchyViewModel;
        private final boolean viewOnly;
        private final OnClickListener onClickListener;
        private final String screenName;

        FormHiearchyMenuProvider(FormEntryViewModel formEntryViewModel, FormHierarchyViewModel formHierarchyViewModel, boolean viewOnly, String screenName, OnClickListener goUpClicked) {
            this.formEntryViewModel = formEntryViewModel;
            this.formHierarchyViewModel = formHierarchyViewModel;
            this.viewOnly = viewOnly;
            this.onClickListener = goUpClicked;
            this.screenName = screenName;
        }

        @Override
        public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
            menuInflater.inflate(R.menu.form_hierarchy_menu, menu);
        }

        @Override
        public void onPrepareMenu(@NonNull Menu menu) {
            FormIndex screenIndex = formHierarchyViewModel.getScreenIndex();
            boolean isAtBeginning = screenIndex.isBeginningOfFormIndex() && !formHierarchyViewModel.shouldShowRepeatGroupPicker();
            boolean shouldShowPicker = formHierarchyViewModel.shouldShowRepeatGroupPicker();
            boolean isInRepeat = formEntryViewModel.getFormController().indexContainsRepeatableGroup(screenIndex);
            boolean isGroupSizeLocked = shouldShowPicker
                    ? isGroupSizeLocked(formHierarchyViewModel.getRepeatGroupPickerIndex()) : isGroupSizeLocked(screenIndex);

            menu.findItem(R.id.menu_edit).setVisible(viewOnly && formEntryViewModel.isFormEditableAfterFinalization());
            menu.findItem(R.id.menu_add_repeat).setVisible(shouldShowPicker && !isGroupSizeLocked && !viewOnly);
            menu.findItem(R.id.menu_delete_child).setVisible(isInRepeat && !shouldShowPicker && !isGroupSizeLocked && !viewOnly);
            menu.findItem(R.id.menu_go_up).setVisible(!isAtBeginning);
        }

        @Override
        public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
            if (!MultiClickGuard.allowClick(screenName)) {
                return false;
            }

            if (menuItem.getItemId() == R.id.menu_edit) {
                onClickListener.onEditClicked();
                return true;
            } else if (menuItem.getItemId() == R.id.menu_delete_child) {
                onClickListener.onDeleteRepeatClicked();
                return true;
            } else if (menuItem.getItemId() == R.id.menu_add_repeat) {
                onClickListener.onAddRepeatClicked();
                return true;
            } else if (menuItem.getItemId() == R.id.menu_go_up) {
                onClickListener.onGoUpClicked();
                return true;
            } else {
                return false;
            }
        }

        private boolean isGroupSizeLocked(FormIndex index) {
            FormController formController = formEntryViewModel.getFormController();
            IFormElement element = formController.getCaptionPrompt(index).getFormElement();
            return element instanceof GroupDef && ((GroupDef) element).noAddRemove;
        }

        interface OnClickListener {
            void onEditClicked();

            void onGoUpClicked();

            void onAddRepeatClicked();

            void onDeleteRepeatClicked();
        }
    }
}
