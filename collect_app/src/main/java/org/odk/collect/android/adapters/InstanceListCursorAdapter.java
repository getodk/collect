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
import org.odk.collect.android.dao.FormsDao;
import org.odk.collect.android.provider.FormsProviderAPI;
import org.odk.collect.android.provider.InstanceProvider;
import org.odk.collect.android.provider.InstanceProviderAPI;

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
        if (!shouldCheckDisabled) {
            return view;
        }

        String formId = getCursor().getString(getCursor().getColumnIndex(InstanceProviderAPI.InstanceColumns.JR_FORM_ID));
        Cursor cursor = new FormsDao().getFormsCursorForFormId(formId);

        boolean formExists = false;
        boolean isFormEncrypted = false;
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    int base64RSAPublicKeyColumnIndex = cursor.getColumnIndex(FormsProviderAPI.FormsColumns.BASE64_RSA_PUBLIC_KEY);
                    String base64RSAPublicKey = cursor.getString(base64RSAPublicKeyColumnIndex);
                    isFormEncrypted = base64RSAPublicKey != null && !base64RSAPublicKey.isEmpty();
                    formExists = true;
                }
            } finally {
                cursor.close();
            }
        }

        long date = getCursor().getLong(getCursor().getColumnIndex(InstanceProviderAPI.InstanceColumns.DELETED_DATE));

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
        long lastStatusChangeDate = getCursor().getLong(getCursor().getColumnIndex(InstanceProviderAPI.InstanceColumns.LAST_STATUS_CHANGE_DATE));
        String status = getCursor().getString(getCursor().getColumnIndex(InstanceProviderAPI.InstanceColumns.STATUS));
        String subtext = InstanceProvider.getDisplaySubtext(context, status, new Date(lastStatusChangeDate));

        final TextView formSubtitle = view.findViewById(R.id.form_subtitle);
        formSubtitle.setText(subtext);
    }

    private void setImageFromStatus(ImageView imageView) {
        String formStatus = getCursor().getString(getCursor().getColumnIndex(InstanceProviderAPI.InstanceColumns.STATUS));

        switch (formStatus) {
            case InstanceProviderAPI.STATUS_INCOMPLETE:
                imageView.setImageResource(R.drawable.form_state_saved);
                break;
            case InstanceProviderAPI.STATUS_COMPLETE:
                imageView.setImageResource(R.drawable.form_state_finalized);
                break;
            case InstanceProviderAPI.STATUS_SUBMITTED:
                imageView.setImageResource(R.drawable.form_state_submitted);
                break;
            case InstanceProviderAPI.STATUS_SUBMISSION_FAILED:
                imageView.setImageResource(R.drawable.form_state_submission_failed);
                break;
        }
    }
}
