/*
 * Copyright 2018 Nafundi
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
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import androidx.core.content.ContextCompat;

import org.odk.collect.android.R;
import org.odk.collect.forms.instances.Instance;

public final class IconUtils {

    private IconUtils() {
    }

    /** Renders a Drawable (such as a vector drawable) into a Bitmap. */
    public static Bitmap getBitmap(Context context, int drawableId) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);
        if (drawable instanceof BitmapDrawable) {  // shortcut if it's already a bitmap
            Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
            if (bitmap != null) {
                return bitmap;
            }
        }
        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();
        if (width <= 0 || height <= 0) {  // negative if Drawable is a solid colour
            width = height = 1;
        }
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    public static Drawable getSubmissionSummaryStatusIcon(Context context, String instanceStatus) {
        switch (instanceStatus) {
            case Instance.STATUS_INCOMPLETE:
                return ContextCompat.getDrawable(context, R.drawable.form_state_saved);
            case Instance.STATUS_COMPLETE:
                return ContextCompat.getDrawable(context, R.drawable.form_state_finalized);
            case Instance.STATUS_SUBMITTED:
                return ContextCompat.getDrawable(context, R.drawable.form_state_submited);
            case Instance.STATUS_SUBMISSION_FAILED:
                return ContextCompat.getDrawable(context, R.drawable.form_state_submission_failed);
        }
        return null;
    }
}
