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
import org.odk.collect.android.provider.InstanceProviderAPI;
import org.odk.collect.android.utilities.ApplicationConstants;

import java.text.SimpleDateFormat;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class InstanceSummaryDialogFragment extends BottomSheetDialogFragment {
    public static final String TAG = "InstanceSummaryDialogFragment";

    private static final String INSTANCE_NAME = "instanceName";
    private static final String INSTANCE_STATUS = "instanceStatus";
    private static final String INSTANCE_LAST_STATUS_CHANGE_DATE = "instanceLastStatusChangeDate";
    private static final String INSTANCE_ID = "instanceId";
    private static final String CLICK_ACTION = "clickAction";

    private String instanceName;
    private String instanceStatus;
    private String instanceLastStatusChangeDate;
    private long instanceId;
    private FormMapViewModel.ClickAction clickAction;

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

    public static InstanceSummaryDialogFragment newInstance(String instanceName, String instanceStatus,
                                                            String instanceLastStatusChangeDate,
                                                            long instanceId, FormMapViewModel.ClickAction clickAction) {
        InstanceSummaryDialogFragment dialog = new InstanceSummaryDialogFragment();
        Bundle args = new Bundle();
        args.putString(INSTANCE_NAME, instanceName);
        args.putString(INSTANCE_STATUS, instanceStatus);
        args.putString(INSTANCE_LAST_STATUS_CHANGE_DATE, instanceLastStatusChangeDate);
        args.putLong(INSTANCE_ID, instanceId);
        args.putSerializable(CLICK_ACTION, clickAction);
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
        outState.putString(INSTANCE_NAME, instanceName);
        outState.putString(INSTANCE_STATUS, instanceStatus);
        outState.putString(INSTANCE_LAST_STATUS_CHANGE_DATE, instanceLastStatusChangeDate);
        outState.putLong(INSTANCE_ID, instanceId);
        outState.putSerializable(CLICK_ACTION, clickAction);
        super.onSaveInstanceState(outState);
    }

    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        readProperties(savedInstanceState);
        setUpElements();
    }

    private void readProperties(Bundle savedInstanceState) {
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

        clickAction = (FormMapViewModel.ClickAction) (savedInstanceState == null
                        ? getArguments().getSerializable(CLICK_ACTION)
                        : savedInstanceState.getSerializable(CLICK_ACTION));
    }

    private void setUpElements() {
        submissionName.setText(instanceName);
        statusText.setText(instanceLastStatusChangeDate);
        statusIcon.setImageDrawable(getStatusIcon(instanceStatus));
        statusIcon.setBackground(null);

        switch (clickAction) {
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
}