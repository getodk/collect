package org.odk.collect.android.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.TextView;

import org.odk.collect.android.R;
import org.odk.collect.android.events.RxEventBus;
import org.odk.collect.android.events.SmsProgressEvent;
import org.odk.collect.android.provider.InstanceProviderAPI;
import org.odk.collect.android.views.ProgressBar;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.disposables.CompositeDisposable;

public class InstanceUploaderAdapter extends CursorAdapter {
    private RxEventBus eventBus;
    private CompositeDisposable compositeDisposable;

    public InstanceUploaderAdapter(Context context, Cursor cursor, RxEventBus eventBus) {
        super(context, cursor);
        this.eventBus = eventBus;
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

        viewHolder.displayName.setText(cursor.getString(cursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.DISPLAY_NAME)));
        viewHolder.displaySubtext.setText(cursor.getString(cursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.DISPLAY_SUBTEXT)));

        long instanceId = cursor.getLong(cursor.getColumnIndex(InstanceProviderAPI.InstanceColumns._ID));

        compositeDisposable.add(eventBus.register(SmsProgressEvent.class)
                .filter(event -> event.getInstanceId().equals(String.valueOf(instanceId)))
                .subscribe(smsProgressEvent -> {
                    smsProgressEvent.getProgress();
                }));
    }

    static class ViewHolder {
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

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
