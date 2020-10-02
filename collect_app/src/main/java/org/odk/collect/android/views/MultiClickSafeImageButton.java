package org.odk.collect.android.views;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;

import org.odk.collect.android.utilities.MultiClickGuard;

public class MultiClickSafeImageButton extends AppCompatImageButton {
    public MultiClickSafeImageButton(@NonNull Context context) {
        super(context);
    }

    public MultiClickSafeImageButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean performClick() {
        if (MultiClickGuard.allowClick(getClass().getName())) {
            return super.performClick();
        } else {
            return false;
        }
    }
}
