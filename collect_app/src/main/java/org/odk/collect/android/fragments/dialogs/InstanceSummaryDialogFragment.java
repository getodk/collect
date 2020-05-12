package org.odk.collect.android.fragments.dialogs;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.Chip;

import org.odk.collect.android.R;
import org.odk.collect.android.activities.viewmodels.FormMapViewModel;
import org.odk.collect.android.preferences.AdminKeys;
import org.odk.collect.android.preferences.AdminSharedPreferences;
import org.odk.collect.android.provider.InstanceProvider;
import org.odk.collect.android.provider.InstanceProviderAPI;
import org.odk.collect.android.utilities.ApplicationConstants;

import java.text.SimpleDateFormat;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class InstanceSummaryDialogFragment extends BottomSheetDialogFragment {
    public static final String TAG = "InstanceSummaryDialogFragment";

    private static final String FEATURE_ID = "featureId";

    private int featureId;
    private long instanceId;

    private FormMapViewModel viewModel;

    @BindView(R.id.submission_name)
    TextView submissionName;

    @BindView(R.id.status_icon)
    ImageView statusIcon;

    @BindView(R.id.status_text)
    TextView statusText;

    @BindView(R.id.info)
    TextView infoText;

    @BindView(R.id.openFormChip)
    Chip openFormChip;

    public static InstanceSummaryDialogFragment newInstance(int featureId) {
        InstanceSummaryDialogFragment dialog = new InstanceSummaryDialogFragment();
        Bundle args = new Bundle();
        args.putInt(FEATURE_ID, featureId);
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        viewModel = ViewModelProviders.of(requireActivity()).get(FormMapViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.submission_summary_layout, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(FEATURE_ID, featureId);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        readProperties(savedInstanceState);
        setUpElements();
    }

    private void readProperties(Bundle savedInstanceState) {
        featureId = savedInstanceState == null
                ? getArguments().getInt(FEATURE_ID)
                : savedInstanceState.getInt(FEATURE_ID);
    }

    private void setUpElements() {
        FormMapViewModel.MappableFormInstance mappableFormInstance = viewModel.getInstanceByFeatureId(featureId);

        String instanceLastStatusChangeDate = InstanceProvider.getDisplaySubtext(requireActivity(), mappableFormInstance.getStatus(), mappableFormInstance.getLastStatusChangeDate());
        instanceId = mappableFormInstance.getDatabaseId();

        submissionName.setText(mappableFormInstance.getInstanceName());
        statusText.setText(instanceLastStatusChangeDate);
        statusIcon.setImageDrawable(getStatusIcon(mappableFormInstance.getStatus()));
        statusIcon.setBackground(null);

        switch (getAction()) {
            case DELETED_TOAST:
                infoText.setVisibility(View.VISIBLE);
                String deletedTime = getString(R.string.deleted_on_date_at_time);
                String disabledMessage = new SimpleDateFormat(deletedTime,
                        Locale.getDefault()).format(viewModel.getDeletedDateOf(instanceId));
                infoText.setText(disabledMessage);
                break;
            case NOT_VIEWABLE_TOAST:
                infoText.setVisibility(View.VISIBLE);
                infoText.setText(R.string.cannot_edit_completed_form);
                break;
            case OPEN_READ_ONLY:
                openFormChip.setVisibility(View.VISIBLE);
                setUpChip(false);
                break;
            case OPEN_EDIT:
                openFormChip.setVisibility(View.VISIBLE);
                boolean canEditSaved = (Boolean) AdminSharedPreferences.getInstance().get(AdminKeys.KEY_EDIT_SAVED);
                setUpChip(canEditSaved);
                break;
        }
    }

    private void setUpChip(boolean canEdit) {
        openFormChip.setVisibility(View.VISIBLE);
        openFormChip.setText(canEdit ? R.string.review_data : R.string.view_sent_forms);
        openFormChip.setChipIcon(ContextCompat.getDrawable(requireActivity(), canEdit ? R.drawable.ic_edit : R.drawable.ic_visibility));
        openFormChip.setOnClickListener(v -> {
            startActivity(canEdit
                    ? getEditFormInstanceIntentFor()
                    : getViewOnlyFormInstanceIntentFor());
            dismiss();
        });
    }

    private Drawable getStatusIcon(String instanceStatus) {
        switch (instanceStatus) {
            case InstanceProviderAPI.STATUS_INCOMPLETE:
                return ContextCompat.getDrawable(requireActivity(), R.drawable.form_state_saved);
            case InstanceProviderAPI.STATUS_COMPLETE:
                return ContextCompat.getDrawable(requireActivity(), R.drawable.form_state_finalized);
            case InstanceProviderAPI.STATUS_SUBMITTED:
                return ContextCompat.getDrawable(requireActivity(), R.drawable.form_state_submited);
            case InstanceProviderAPI.STATUS_SUBMISSION_FAILED:
                return ContextCompat.getDrawable(requireActivity(), R.drawable.form_state_submission_failed);
        }
        return null;
    }

    private Intent getViewOnlyFormInstanceIntentFor() {
        Intent intent = getEditFormInstanceIntentFor();
        intent.putExtra(ApplicationConstants.BundleKeys.FORM_MODE, ApplicationConstants.FormModes.VIEW_SENT);
        return intent;
    }

    private Intent getEditFormInstanceIntentFor() {
        Uri uri = ContentUris.withAppendedId(InstanceProviderAPI.InstanceColumns.CONTENT_URI, instanceId);
        return new Intent(Intent.ACTION_EDIT, uri);
    }

    private FormMapViewModel.ClickAction getAction() {
        FormMapViewModel.MappableFormInstance instance = viewModel.getInstanceByFeatureId(featureId);
        return instance == null ? FormMapViewModel.ClickAction.NONE : instance.getClickAction();
    }
}