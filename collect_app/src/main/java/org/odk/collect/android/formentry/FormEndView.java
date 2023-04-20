package org.odk.collect.android.formentry;

import android.content.Context;
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
