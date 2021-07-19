/*
 * Copyright 2017 SDRC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.odk.collect.android.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import org.odk.collect.android.R;
import org.odk.collect.forms.Form;
import org.odk.collect.forms.instances.Instance;
import org.odk.collect.android.external.InstanceProvider;
import org.odk.collect.android.database.instances.DatabaseInstanceColumns;
import org.odk.collect.android.utilities.FormsRepositoryProvider;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import timber.log.Timber;

public class InstanceListCursorAdapter extends SimpleCursorAdapter {
    private final Context context;
    private final boolean shouldCheckDisabled;

    public InstanceListCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to, boolean shouldCheckDisabled) {
        super(context, layout, c, from, to);
        this.context = context;
        this.shouldCheckDisabled = shouldCheckDisabled;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);

        ImageView imageView = view.findViewById(R.id.image);
        setImageFromStatus(imageView);

        setUpSubtext(view);

        // Some form lists never contain disabled items; if so, we're done.
        // Update: This only seems to be the case in Edit Saved Forms and it's not clear why...
        if (!shouldCheckDisabled) {
            return view;
        }

        boolean formExists = false;
        boolean isFormEncrypted = false;

        String formId = getCursor().getString(getCursor().getColumnIndex(DatabaseInstanceColumns.JR_FORM_ID));
        String formVersion = getCursor().getString(getCursor().getColumnIndex(DatabaseInstanceColumns.JR_VERSION));
        Form form = new FormsRepositoryProvider(context.getApplicationContext()).get().getLatestByFormIdAndVersion(formId, formVersion);

        if (form != null) {
            String base64RSAPublicKey = form.getBASE64RSAPublicKey();
            formExists = true;
            isFormEncrypted = base64RSAPublicKey != null;
        }

        long date = getCursor().getLong(getCursor().getColumnIndex(DatabaseInstanceColumns.DELETED_DATE));

        if (date != 0 || !formExists || isFormEncrypted) {
            String disabledMessage;

            if (date != 0) {
                try {
                    String deletedTime = context.getString(R.string.deleted_on_date_at_time);
                    disabledMessage = new SimpleDateFormat(deletedTime, Locale.getDefault()).format(new Date(date));
                } catch (IllegalArgumentException e) {
                    Timber.e(e);
                    disabledMessage = context.getString(R.string.submission_deleted);
                }
            } else if (!formExists) {
                disabledMessage = context.getString(R.string.deleted_form);
            } else {
                disabledMessage = context.getString(R.string.encrypted_form);
            }

            setDisabled(view, disabledMessage);
        } else {
            setEnabled(view);
        }

        return view;
    }

    private void setEnabled(View view) {
        final TextView formTitle = view.findViewById(R.id.form_title);
        final TextView formSubtitle = view.findViewById(R.id.form_subtitle);
        final TextView disabledCause = view.findViewById(R.id.form_subtitle2);
        final ImageView imageView = view.findViewById(R.id.image);

        view.setEnabled(true);
        disabledCause.setVisibility(View.GONE);

        formTitle.setAlpha(1f);
        formSubtitle.setAlpha(1f);
        disabledCause.setAlpha(1f);
        imageView.setAlpha(1f);
    }

    private void setDisabled(View view, String disabledMessage) {
        final TextView formTitle = view.findViewById(R.id.form_title);
        final TextView formSubtitle = view.findViewById(R.id.form_subtitle);
        final TextView disabledCause = view.findViewById(R.id.form_subtitle2);
        final ImageView imageView = view.findViewById(R.id.image);

        view.setEnabled(false);
        disabledCause.setVisibility(View.VISIBLE);
        disabledCause.setText(disabledMessage);

        // Material design "disabled" opacity is 38%.
        formTitle.setAlpha(0.38f);
        formSubtitle.setAlpha(0.38f);
        disabledCause.setAlpha(0.38f);
        imageView.setAlpha(0.38f);
    }

    private void setUpSubtext(View view) {
        long lastStatusChangeDate = getCursor().getLong(getCursor().getColumnIndex(DatabaseInstanceColumns.LAST_STATUS_CHANGE_DATE));
        String status = getCursor().getString(getCursor().getColumnIndex(DatabaseInstanceColumns.STATUS));
        String subtext = InstanceProvider.getDisplaySubtext(context, status, new Date(lastStatusChangeDate));

        final TextView formSubtitle = view.findViewById(R.id.form_subtitle);
        formSubtitle.setText(subtext);
    }

    private void setImageFromStatus(ImageView imageView) {
        String formStatus = getCursor().getString(getCursor().getColumnIndex(DatabaseInstanceColumns.STATUS));

        int imageResourceId = getFormStateImageResourceIdForStatus(formStatus);
        imageView.setImageResource(imageResourceId);
        imageView.setTag(imageResourceId);
    }

    public static int getFormStateImageResourceIdForStatus(String formStatus) {
        switch (formStatus) {
            case Instance.STATUS_INCOMPLETE:
                return R.drawable.form_state_saved_circle;
            case Instance.STATUS_COMPLETE:
                return R.drawable.form_state_finalized_circle;
            case Instance.STATUS_SUBMITTED:
                return R.drawable.form_state_submitted_circle;
            case Instance.STATUS_SUBMISSION_FAILED:
                return R.drawable.form_state_submission_failed_circle;
        }

        return -1;
    }
}
