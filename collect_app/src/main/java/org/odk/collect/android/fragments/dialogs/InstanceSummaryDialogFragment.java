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
import org.odk.collect.android.provider.InstanceProviderAPI;
import org.odk.collect.android.utilities.ApplicationConstants;

import butterknife.BindView;
import butterknife.ButterKnife;

public class InstanceSummaryDialogFragment extends BottomSheetDialogFragment {
    public static final String TAG = "InstanceSummaryDialogFragment";

    private static final String CAN_EDIT ="canEdit";
    private static final String INSTANCE_NAME ="instanceName";
    private static final String INSTANCE_STATUS ="instanceStatus";
    private static final String INSTANCE_LAST_STATUS_CHANGE_DATE ="instanceLastStatusChangeDate";
    private static final String INSTANCE_ID ="instanceId";

    private boolean canEdit;
    private String instanceName;
    private String instanceStatus;
    private String instanceLastStatusChangeDate;
    private long instanceId;

    @BindView(R.id.submission_name)
    TextView submissionName;

    @BindView(R.id.status_icon)
    ImageView statusIcon;

    @BindView(R.id.status_text)
    TextView statusText;

    @BindView(R.id.openFormChip)
    Chip openFormChip;

    public static InstanceSummaryDialogFragment newInstance(boolean canEdit, String instanceName,
                                                            String instanceStatus,
                                                            String instanceLastStatusChangeDate,
                                                            long instanceId) {
        InstanceSummaryDialogFragment dialog = new InstanceSummaryDialogFragment();
        Bundle args = new Bundle();
        args.putBoolean(CAN_EDIT, canEdit);
        args.putString(INSTANCE_NAME, instanceName);
        args.putString(INSTANCE_STATUS, instanceStatus);
        args.putString(INSTANCE_LAST_STATUS_CHANGE_DATE, instanceLastStatusChangeDate);
        args.putLong(INSTANCE_ID, instanceId);
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
        outState.putString(INSTANCE_NAME, instanceName);
        outState.putString(INSTANCE_STATUS, instanceStatus);
        outState.putString(INSTANCE_LAST_STATUS_CHANGE_DATE, instanceLastStatusChangeDate);
        outState.putLong(INSTANCE_ID, instanceId);
        super.onSaveInstanceState(outState);
    }

    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        readProperties(savedInstanceState);
        setUpElements();
    }

    private void readProperties(Bundle savedInstanceState) {
        canEdit = savedInstanceState == null
                ? getArguments().getBoolean(CAN_EDIT)
                : savedInstanceState.getBoolean(CAN_EDIT);

        instanceName = savedInstanceState == null
                ? getArguments().getString(INSTANCE_NAME)
                : savedInstanceState.getString(INSTANCE_NAME);

        instanceStatus = savedInstanceState == null
                ? getArguments().getString(INSTANCE_STATUS)
                : savedInstanceState.getString(INSTANCE_STATUS);

        instanceLastStatusChangeDate = savedInstanceState == null
                ? getArguments().getString(INSTANCE_LAST_STATUS_CHANGE_DATE)
                : savedInstanceState.getString(INSTANCE_LAST_STATUS_CHANGE_DATE);

        instanceId = savedInstanceState == null
                ? getArguments().getLong(INSTANCE_ID)
                : savedInstanceState.getLong(INSTANCE_ID);
    }

    private void setUpElements() {
        submissionName.setText(instanceName);
        statusText.setText(instanceLastStatusChangeDate);
        statusIcon.setImageDrawable(getStatusIcon(instanceStatus));
        statusIcon.setBackground(null);

        openFormChip.setText(canEdit ? R.string.review_data : R.string.view_sent_forms);
        openFormChip.setChipIcon(ContextCompat.getDrawable(getActivity(), canEdit ? R.drawable.ic_edit : R.drawable.ic_visibility));
        openFormChip.setOnClickListener(v -> {
            startActivity(canEdit
                    ? getEditFormInstanceIntentFor()
                    : getViewOnlyFormInstanceIntentFor());
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
        Uri uri = ContentUris.withAppendedId(InstanceProviderAPI.InstanceColumns.CONTENT_URI, instanceId);
        return new Intent(Intent.ACTION_EDIT, uri);
    }
}