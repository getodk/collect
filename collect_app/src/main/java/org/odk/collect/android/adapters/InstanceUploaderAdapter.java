package org.odk.collect.android.adapters;

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
import org.odk.collect.android.events.SmsEvent;
import org.odk.collect.android.provider.InstanceProviderAPI;
import org.odk.collect.android.tasks.sms.SmsService;
import org.odk.collect.android.tasks.sms.contracts.SmsSubmissionManagerContract;
import org.odk.collect.android.tasks.sms.models.MessageStatus;
import org.odk.collect.android.tasks.sms.models.SmsProgress;
import org.odk.collect.android.tasks.sms.models.SmsSubmission;
import org.odk.collect.android.utilities.ThemeUtils;
import org.odk.collect.android.views.ProgressBar;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

import static org.odk.collect.android.provider.InstanceProviderAPI.STATUS_SUBMISSION_FAILED;
import static org.odk.collect.android.provider.InstanceProviderAPI.STATUS_SUBMITTED;

public class InstanceUploaderAdapter extends CursorAdapter {

    @Inject
    RxEventBus eventBus;
    @Inject
    SmsSubmissionManagerContract submissionManager;
    @Inject
    SmsService smsService;

    private Context context;
    private CompositeDisposable compositeDisposable;

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

        boolean isSmsSubmission = model != null;

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
        } else {
            if (status.equals(STATUS_SUBMITTED)) {
                viewHolder.progressBar.setProgressPercent(100, false);
            } else if (status.equals(STATUS_SUBMISSION_FAILED)) {
                viewHolder.progressBar.setProgressPercent(50, false);
            }
        }

        if (isSmsSubmission) {
            MessageStatus messageStatus = submissionManager.checkNextMessageStatus(String.valueOf(instanceId));

            if (messageStatus != null) {
                setSmsSubmissionStateIcons(messageStatus, viewHolder);
            }

            SmsEvent currentStatus = new SmsEvent();
            currentStatus.setStatus(messageStatus);
            currentStatus.setLastUpdated(model.getLastUpdated());
            currentStatus.setProgress(model.getCompletion());

            setDisplaySubTextView(currentStatus, viewHolder);

            setupCloseButton(viewHolder, messageStatus);
            viewHolder.closeButton.setOnClickListener(v -> smsService.cancelFormSubmission(String.valueOf(instanceId)));
        }

        compositeDisposable.add(eventBus.register(SmsEvent.class)
                .filter(event -> event.getInstanceId().equals(String.valueOf(instanceId)))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(event -> {
                    viewHolder.progressBar.setProgressPercent((int) event.getProgress().getPercentage(), true);
                    setSmsSubmissionStateIcons(event.getStatus(), viewHolder);
                    setDisplaySubTextView(event, viewHolder);
                    setupCloseButton(viewHolder, event.getStatus());
                }));
    }

    private void setupCloseButton(ViewHolder viewHolder, MessageStatus status) {

        if (status == null) {
            return;
        }

        if (status.equals(MessageStatus.Sending)) {
            viewHolder.closeButton.setVisibility(View.VISIBLE);
            viewHolder.checkbox.setVisibility(View.GONE);
        } else {
            viewHolder.closeButton.setVisibility(View.GONE);
            viewHolder.checkbox.setVisibility(View.VISIBLE);
        }
    }

    private void setSmsSubmissionStateIcons(MessageStatus messageStatus, ViewHolder viewHolder) {

        if (messageStatus == null) {
            return;
        }

        switch (messageStatus) {
            case Delivered:
            case Sent:
                viewHolder.statusIcon.setImageResource(R.drawable.check);
                break;

            case Queued:
            case Sending:
                viewHolder.statusIcon.setImageResource(R.drawable.message_text_outline);
                break;

            case Ready:
                viewHolder.statusIcon.setImageResource(R.drawable.pencil);
                break;

            default:
                viewHolder.statusIcon.setImageResource(R.drawable.message_alert);
                break;
        }
    }

    private void setDisplaySubTextView(SmsEvent progress, ViewHolder viewHolder) {
        String text = getDisplaySubtext(progress);
        if (text != null) {
            viewHolder.displaySubtext.setText(text);
        }
    }

    private String getDisplaySubtext(SmsEvent event) {
        Date date = event.getLastUpdated();
        SmsProgress progress = event.getProgress();

        if (event.getStatus() == null) {
            return null;
        }

        switch (event.getStatus()) {
            case NoReception:
                return new SimpleDateFormat(context.getString(R.string.sms_no_reception), Locale.getDefault()).format(date);
            case AirplaneMode:
                return new SimpleDateFormat(context.getString(R.string.sms_airplane_mode), Locale.getDefault()).format(date);
            case FatalError:
                return new SimpleDateFormat(context.getString(R.string.sms_fatal_error), Locale.getDefault()).format(date);
            case Sending:
                return context.getResources().getQuantityString(R.plurals.sms_sending, (int) progress.getTotalCount(), progress.getCompletedCount(), progress.getTotalCount());
            case Queued:
                return context.getString(R.string.sms_submission_queued);
            case Sent:
                return new SimpleDateFormat(context.getString(R.string.sms_sent_on_date_at_time),
                        Locale.getDefault()).format(date);
            case Delivered:
                return new SimpleDateFormat(context.getString(R.string.sms_delivered_on_date_at_time),
                        Locale.getDefault()).format(date);
            case NotDelivered:
                return new SimpleDateFormat(context.getString(R.string.sms_not_delivered_on_date_at_time),
                        Locale.getDefault()).format(date);
            case NoMessage:
                return context.getString(R.string.sms_no_message);
            case Canceled:
                return new SimpleDateFormat(context.getString(R.string.sms_last_submission_on_date_at_time),
                        Locale.getDefault()).format(date);
        }

        return "";
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
