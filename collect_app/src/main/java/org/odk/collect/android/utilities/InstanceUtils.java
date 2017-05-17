/*
 * Copyright 2017 Nafundi
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

package org.odk.collect.android.utilities;

import android.content.Context;

import org.odk.collect.android.R;
import org.odk.collect.android.provider.InstanceProviderAPI;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class InstanceUtils {

    public static String getDisplaySubtext(Context context, String state, Date date) {
        if (state == null) {
            return new SimpleDateFormat(context.getString(R.string.added_on_date_at_time),
                    Locale.getDefault()).format(date);
        } else if (InstanceProviderAPI.STATUS_INCOMPLETE.equalsIgnoreCase(state)) {
            return new SimpleDateFormat(context.getString(R.string.saved_on_date_at_time),
                    Locale.getDefault()).format(date);
        } else if (InstanceProviderAPI.STATUS_COMPLETE.equalsIgnoreCase(state)) {
            return new SimpleDateFormat(context.getString(R.string.finalized_on_date_at_time),
                    Locale.getDefault()).format(date);
        } else if (InstanceProviderAPI.STATUS_SUBMITTED.equalsIgnoreCase(state)) {
            return new SimpleDateFormat(context.getString(R.string.sent_on_date_at_time),
                    Locale.getDefault()).format(date);
        } else if (InstanceProviderAPI.STATUS_SUBMISSION_FAILED.equalsIgnoreCase(state)) {
            return new SimpleDateFormat(
                    context.getString(R.string.sending_failed_on_date_at_time),
                    Locale.getDefault()).format(date);
        } else {
            return new SimpleDateFormat(context.getString(R.string.added_on_date_at_time),
                    Locale.getDefault()).format(date);
        }
    }
}