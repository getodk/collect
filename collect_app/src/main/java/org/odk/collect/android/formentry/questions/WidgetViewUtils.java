package org.odk.collect.android.formentry.questions;

import static android.view.View.GONE;

import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TextView;

import androidx.annotation.IdRes;

import com.google.android.material.button.MaterialButton;

import org.odk.collect.android.R;
import org.odk.collect.android.utilities.ThemeUtils;
import org.odk.collect.android.widgets.QuestionWidget;
import org.odk.collect.android.widgets.interfaces.ButtonClickListener;
import org.odk.collect.androidshared.ui.multiclicksafe.MultiClickGuard;

public final class WidgetViewUtils {
    private WidgetViewUtils() {

    }

    public static TextView createAnswerTextView(Context context, int answerFontSize) {
        return createAnswerTextView(context, "", answerFontSize);
    }

    public static TextView createAnswerTextView(Context context, CharSequence text, int answerFontSize) {
        TextView textView = new TextView(context);

        textView.setId(R.id.answer_text);
        textView.setTextColor(new ThemeUtils(context).getColorOnSurface());
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, answerFontSize);
        textView.setPadding(20, 20, 20, 20);
        textView.setText(text);

        return textView;
    }

    public static ImageView createAnswerImageView(Context context) {
        final ImageView imageView = new ImageView(context);
        imageView.setId(View.generateViewId());
        imageView.setTag("ImageView");
        imageView.setPadding(10, 10, 10, 10);
        imageView.setAdjustViewBounds(true);
        return imageView;
    }

    public static Button createSimpleButton(Context context, @IdRes final int withId, boolean readOnly, String text, ButtonClickListener listener, boolean addMargin) {
        final MaterialButton button = (MaterialButton) LayoutInflater
                .from(context)
                .inflate(R.layout.widget_answer_button, null, false);

        if (readOnly) {
            button.setVisibility(GONE);
        } else {
            if (withId != -1) {
                button.setId(withId);
            }
            button.setText(text);
            button.setContentDescription(text);

            if (addMargin) {
                TableLayout.LayoutParams params = new TableLayout.LayoutParams();

                int marginStandard = (int) context.getResources().getDimension(org.odk.collect.androidshared.R.dimen.margin_standard);
                params.setMargins(0, marginStandard, 0, 0);

                button.setLayoutParams(params);
            }

            button.setOnClickListener(v -> {
                if (MultiClickGuard.allowClick(QuestionWidget.class.getName())) {
                    listener.onButtonClick(withId);
                }
            });
        }

        return button;
    }
}
