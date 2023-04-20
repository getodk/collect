package org.odk.collect.android.formentry;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;

import org.odk.collect.android.R;

public class FormEndView extends SwipeHandler.View {

    private final Listener listener;
    private final String formTitle;

    public FormEndView(Context context, String formTitle, Listener listener) {
        super(context);
        this.formTitle = formTitle;
        this.listener = listener;
        init(context);
    }

    private void init(Context context) {
        inflate(context, R.layout.form_entry_end, this);

        ((TextView) findViewById(R.id.description)).setText(context.getString(R.string.save_enter_data_description, formTitle));

        findViewById(R.id.save_exit_button).setOnClickListener(v -> {
            listener.onSaveClicked(true);
        });

        findViewById(R.id.instance_name_learn_more).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setData(Uri.parse("https://forum.getodk.org/t/collect-manual-instance-naming-will-be-removed-in-v2023-2/40313"));
            context.startActivity(intent);
        });
    }

    @Override
    public boolean shouldSuppressFlingGesture() {
        return false;
    }

    @Nullable
    @Override
    public NestedScrollView getVerticalScrollView() {
        return findViewById(R.id.scroll_view);
    }

    public interface Listener {
        void onSaveClicked(boolean markAsFinalized);
    }
}
