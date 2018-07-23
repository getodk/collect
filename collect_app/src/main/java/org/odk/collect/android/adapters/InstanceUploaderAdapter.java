package org.odk.collect.android.adapters;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.events.RxEventBus;
import org.odk.collect.android.events.SmsRxEvent;
import org.odk.collect.android.preferences.GeneralSharedPreferences;
import org.odk.collect.android.provider.InstanceProviderAPI;
import org.odk.collect.android.tasks.sms.SmsService;
import org.odk.collect.android.tasks.sms.contracts.SmsSubmissionManagerContract;
import org.odk.collect.android.tasks.sms.models.SmsSubmission;
import org.odk.collect.android.utilities.ThemeUtils;
import org.odk.collect.android.views.ProgressBar;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

import static org.odk.collect.android.preferences.PreferenceKeys.KEY_SUBMISSION_TRANSPORT_TYPE;
import static org.odk.collect.android.provider.InstanceProviderAPI.STATUS_SUBMISSION_FAILED;
import static org.odk.collect.android.provider.InstanceProviderAPI.STATUS_SUBMITTED;
import static org.odk.collect.android.tasks.sms.SmsService.RESULT_MESSAGE_READY;
import static org.odk.collect.android.tasks.sms.SmsService.RESULT_OK_OTHERS_PENDING;
import static org.odk.collect.android.tasks.sms.SmsService.RESULT_QUEUED;
import static org.odk.collect.android.tasks.sms.SmsService.RESULT_SENDING;
import static org.odk.collect.android.tasks.sms.SmsService.getDisplaySubtext;

public class InstanceUploaderAdapter extends CursorAdapter {

    @Inject
    RxEventBus eventBus;
    @Inject
    SmsSubmissionManagerContract submissionManager;
    @Inject
    SmsService smsService;

    private final Context context;
    private final CompositeDisposable compositeDisposable;

    public InstanceUploaderAdapter(Context context, Cursor cursor) {
        super(context, cursor);
        this.context = context;
        Collect.getInstance().getComponent().inject(this);
        compositeDisposable = new CompositeDisposable();
    }

    public void onDestroy() {
        if (compositeDisposable != null) {
            compositeDisposable.dispose();
        }
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.instance_upload_list_item, parent, false);
        view.setTag(new ViewHolder(view));
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();

        GradientDrawable shapeDrawable = (GradientDrawable) viewHolder.imageBackground.getBackground();
        shapeDrawable.setColor(new ThemeUtils(context).getAccentColor());

        viewHolder.progressBar.setProgressPercent(0, false);

        viewHolder.displayName.setText(cursor.getString(cursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.DISPLAY_NAME)));
        viewHolder.displaySubtext.setText(cursor.getString(cursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.DISPLAY_SUBTEXT)));

        long instanceId = cursor.getLong(cursor.getColumnIndex(InstanceProviderAPI.InstanceColumns._ID));

        SmsSubmission model = submissionManager.getSubmissionModel(String.valueOf(instanceId));

        boolean smsTransportEnabled = ((String) GeneralSharedPreferences.getInstance().get(KEY_SUBMISSION_TRANSPORT_TYPE)).equalsIgnoreCase(context.getString(R.string.transport_type_value_sms));

        boolean isSmsSubmission = model != null && smsTransportEnabled;

        String status = cursor.getString(cursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.STATUS));

        switch (status) {

            case STATUS_SUBMISSION_FAILED:
                if (isSmsSubmission) {
                    viewHolder.statusIcon.setImageResource(R.drawable.message_alert);
                } else {
                    viewHolder.statusIcon.setImageResource(R.drawable.exclamation);
                }
                break;

            case STATUS_SUBMITTED:
                viewHolder.statusIcon.setImageResource(R.drawable.check);
                break;

            default:
                viewHolder.statusIcon.setImageResource(R.drawable.pencil);
        }

        if (isSmsSubmission) {
            viewHolder.progressBar.setProgressPercent((int) model.getCompletion().getPercentage(), false);

            int smsStatus = submissionManager.checkNextMessageResultCode(String.valueOf(instanceId));

            setSmsSubmissionStateIcons(smsStatus, viewHolder);

            SmsRxEvent currentStatus = new SmsRxEvent();
            currentStatus.setResultCode(smsStatus);
            currentStatus.setLastUpdated(model.getLastUpdated());
            currentStatus.setProgress(model.getCompletion());

            setDisplaySubTextView(currentStatus, viewHolder);

            setupCloseButton(viewHolder, smsStatus);
            viewHolder.closeButton.setOnClickListener(v -> smsService.cancelFormSubmission(String.valueOf(instanceId)));
        } else {
            if (status.equals(STATUS_SUBMITTED)) {
                viewHolder.progressBar.setProgressPercent(100, false);
            } else if (status.equals(STATUS_SUBMISSION_FAILED)) {
                viewHolder.progressBar.setProgressPercent(50, false);
            }
        }

        compositeDisposable.add(eventBus.register(SmsRxEvent.class)
                .filter(event -> event.getInstanceId().equals(String.valueOf(instanceId)))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(event -> {
                    viewHolder.progressBar.setProgressPercent((int) event.getProgress().getPercentage(), true);
                    setSmsSubmissionStateIcons(event.getResultCode(), viewHolder);
                    setDisplaySubTextView(event, viewHolder);
                    setupCloseButton(viewHolder, event.getResultCode());
                }));
    }

    private void setupCloseButton(ViewHolder viewHolder, int resultCode) {

        if (resultCode == RESULT_QUEUED || resultCode == RESULT_OK_OTHERS_PENDING) {
            viewHolder.closeButton.setVisibility(View.VISIBLE);
            viewHolder.checkbox.setVisibility(View.GONE);
        } else {
            viewHolder.closeButton.setVisibility(View.GONE);
            viewHolder.checkbox.setVisibility(View.VISIBLE);
        }
    }

    private void setSmsSubmissionStateIcons(int smsStatus, ViewHolder viewHolder) {

        switch (smsStatus) {
            case Activity.RESULT_OK:
                viewHolder.statusIcon.setImageResource(R.drawable.check);
                break;

            case RESULT_QUEUED:
            case RESULT_OK_OTHERS_PENDING:
            case RESULT_SENDING:
                viewHolder.statusIcon.setImageResource(R.drawable.message_text_outline);
                break;

            case RESULT_MESSAGE_READY:
                viewHolder.statusIcon.setImageResource(R.drawable.pencil);
                break;

            default:
                viewHolder.statusIcon.setImageResource(R.drawable.message_alert);
                break;
        }
    }

    private void setDisplaySubTextView(SmsRxEvent event, ViewHolder viewHolder) {
        String text = getDisplaySubtext(event.getResultCode(), event.getLastUpdated(), event.getProgress(), context);
        if (text != null) {
            viewHolder.displaySubtext.setText(text);
        }
    }

    static class ViewHolder {
        @BindView(R.id.image_background)
        LinearLayout imageBackground;
        @BindView(R.id.display_name)
        TextView displayName;
        @BindView(R.id.display_subtext)
        TextView displaySubtext;
        @BindView(R.id.update_info)
        TextView updateInfo;
        @BindView(R.id.checkbox)
        CheckBox checkbox;
        @BindView(R.id.progress_bar)
        ProgressBar progressBar;
        @BindView(R.id.status_icon)
        ImageView statusIcon;
        @BindView(R.id.close_box)
        ImageView closeButton;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
