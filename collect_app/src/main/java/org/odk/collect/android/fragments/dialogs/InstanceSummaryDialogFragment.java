package org.odk.collect.android.fragments.dialogs;

import android.content.ContentUris;
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

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.Chip;

import org.odk.collect.android.R;
import org.odk.collect.android.activities.viewmodels.FormMapViewModel;
import org.odk.collect.android.provider.InstanceProvider;
import org.odk.collect.android.provider.InstanceProviderAPI;
import org.odk.collect.android.utilities.ApplicationConstants;

import java.io.Serializable;

import butterknife.BindView;
import butterknife.ButterKnife;

public class InstanceSummaryDialogFragment extends BottomSheetDialogFragment {
    public static final String TAG = "InstanceSummaryDialogFragment";

    private static final String CAN_EDIT ="canEdit";
    private static final String MAPPABLE_FROM_INSTANCE ="mappableFormInstance";

    private boolean canEdit;
    private FormMapViewModel.MappableFormInstance mappableFormInstance;

    @BindView(R.id.submission_name)
    TextView submissionName;

    @BindView(R.id.status_icon)
    ImageView statusIcon;

    @BindView(R.id.status_text)
    TextView statusText;

    @BindView(R.id.openFormChip)
    Chip openFormChip;

    public static InstanceSummaryDialogFragment newInstance(boolean canEdit, FormMapViewModel.MappableFormInstance mappableFormInstance) {
        InstanceSummaryDialogFragment dialog = new InstanceSummaryDialogFragment();
        Bundle args = new Bundle();
        args.putBoolean(CAN_EDIT, canEdit);
        args.putSerializable(MAPPABLE_FROM_INSTANCE, mappableFormInstance);
        dialog.setArguments(args);
        return dialog;
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
        outState.putBoolean(CAN_EDIT, canEdit);
        outState.putSerializable(MAPPABLE_FROM_INSTANCE, mappableFormInstance);
        super.onSaveInstanceState(outState);
    }

    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        canEdit = savedInstanceState == null
                ? getArguments().getBoolean(CAN_EDIT)
                : savedInstanceState.getBoolean(CAN_EDIT);

        mappableFormInstance = (FormMapViewModel.MappableFormInstance) (savedInstanceState == null
                        ? getArguments().getSerializable(MAPPABLE_FROM_INSTANCE)
                        : savedInstanceState.getSerializable(MAPPABLE_FROM_INSTANCE));

        String instanceName = mappableFormInstance.getInstanceName();
        String instanceStatus = mappableFormInstance.getStatus();
        String instanceLastStatusChangeDate = InstanceProvider.getDisplaySubtext(getActivity(), instanceStatus, mappableFormInstance.getLastStatusChangeDate());

        submissionName.setText(instanceName);
        statusText.setText(instanceLastStatusChangeDate);
        statusIcon.setImageDrawable(getStatusIcon(instanceStatus));
        statusIcon.setBackground(null);

        openFormChip.setText(canEdit ? R.string.review_data : R.string.view_sent_forms);
        openFormChip.setChipIcon(ContextCompat.getDrawable(getActivity(), canEdit ? R.drawable.ic_edit : R.drawable.ic_visibility));
        openFormChip.setOnClickListener(v -> {
            if (canEdit) {
                startActivity(getEditFormInstanceIntentFor());
            } else {
                startActivity(getViewOnlyFormInstanceIntentFor());
            }
            dismiss();
        });
    }

    private Drawable getStatusIcon(String instanceStatus) {
        switch (instanceStatus) {
            case InstanceProviderAPI.STATUS_INCOMPLETE:
                return ContextCompat.getDrawable(getActivity(), R.drawable.form_state_saved);
            case InstanceProviderAPI.STATUS_COMPLETE:
                return ContextCompat.getDrawable(getActivity(), R.drawable.form_state_finalized);
            case InstanceProviderAPI.STATUS_SUBMITTED:
                return ContextCompat.getDrawable(getActivity(), R.drawable.form_state_submited);
            case InstanceProviderAPI.STATUS_SUBMISSION_FAILED:
                return ContextCompat.getDrawable(getActivity(), R.drawable.form_state_submission_failed);
        }
        return null;
    }

    private Intent getViewOnlyFormInstanceIntentFor() {
        Intent intent = getEditFormInstanceIntentFor();
        intent.putExtra(ApplicationConstants.BundleKeys.FORM_MODE, ApplicationConstants.FormModes.VIEW_SENT);
        return intent;
    }

    private Intent getEditFormInstanceIntentFor() {
        Uri uri = ContentUris.withAppendedId(InstanceProviderAPI.InstanceColumns.CONTENT_URI, mappableFormInstance.getDatabaseId());
        return new Intent(Intent.ACTION_EDIT, uri);
    }
}