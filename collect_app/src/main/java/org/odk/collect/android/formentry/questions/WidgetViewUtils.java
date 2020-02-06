package org.odk.collect.android.formentry.questions;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TextView;

import androidx.annotation.IdRes;

import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.utilities.ThemeUtils;
import org.odk.collect.android.utilities.ViewIds;
import org.odk.collect.android.widgets.QuestionWidget;
import org.odk.collect.android.widgets.interfaces.ButtonWidget;

import static android.view.View.GONE;

public class WidgetViewUtils {

    private WidgetViewUtils() {

    }

    public static TextView getCenteredAnswerTextView(Context context, int answerFontSize) {
        TextView textView = createAnswerTextView(context, answerFontSize);
        textView.setGravity(Gravity.CENTER);

        return textView;
    }

    public static TextView createAnswerTextView(Context context, int answerFontSize) {
        return createAnswerTextView(context, "", answerFontSize);
    }

    public static TextView createAnswerTextView(Context context, String text, int answerFontSize) {
        TextView textView = new TextView(context);

        textView.setId(R.id.answer_text);
        textView.setTextColor(new ThemeUtils(context).getColorOnSurface());
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, answerFontSize);
        textView.setPadding(20, 20, 20, 20);
        textView.setText(text);

        return textView;
    }

    public static ImageView createAnswerImageView(Context context, Bitmap bitmap) {
        final ImageView imageView = new ImageView(context);
        imageView.setId(ViewIds.generateViewId());
        imageView.setPadding(10, 10, 10, 10);
        imageView.setAdjustViewBounds(true);
        imageView.setImageBitmap(bitmap);
        return imageView;
    }

    public static Button createSimpleButton(Context context, @IdRes final int withId, boolean readOnly, String text, int answerFontSize, QuestionWidget listener) {
        final Button button = new Button(context);

        if (readOnly) {
            button.setVisibility(GONE);
        } else {
            button.setId(withId);
            button.setText(text);
            button.setTextSize(TypedValue.COMPLEX_UNIT_DIP, answerFontSize);
            button.setPadding(20, 20, 20, 20);

            TableLayout.LayoutParams params = new TableLayout.LayoutParams();
            params.setMargins(7, 5, 7, 5);

            button.setLayoutParams(params);

            button.setOnClickListener(v -> {
                if (Collect.allowClick(QuestionWidget.class.getName())) {
                    ((ButtonWidget) listener).onButtonClick(withId);
                }
            });
        }

        return button;
    }

    public static Button createSimpleButton(Context context, @IdRes int id, boolean readOnly, int answerFontSize, QuestionWidget listener) {
        return createSimpleButton(context, id, readOnly, null, answerFontSize, listener);
    }

    public static Button createSimpleButton(Context context, boolean readOnly, String text, int answerFontSize, QuestionWidget listener) {
        return createSimpleButton(context, R.id.simple_button, readOnly, text, answerFontSize, listener);
    }
}
