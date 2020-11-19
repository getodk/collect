package org.odk.collect.android.formentry.questions;

import android.content.Context;
import android.util.TypedValue;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.odk.collect.android.R;
import org.odk.collect.android.utilities.QuestionFontSizeUtils;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;

public class NoButtonsItem extends FrameLayout {

    @BindView(R.id.imageView)
    ImageView imageView;

    @BindView(R.id.label)
    TextView label;

    public NoButtonsItem(Context context, boolean enabled) {
        super(context);

        View.inflate(context, R.layout.no_buttons_item_layout, this);
        ButterKnife.bind(this);

        setLongClickable(true);
        setEnabled(enabled);
    }

    public void setUpNoButtonsItem(File imageFile, String choiceText, String errorMsg, boolean isInGridView) {
        if (imageFile != null && imageFile.exists()) {
            imageView.setVisibility(View.VISIBLE);
            if (isInGridView) {
                Glide.with(this)
                        .load(imageFile)
                        .fitCenter()
                        .into(imageView);
            } else {
                Glide.with(this)
                        .load(imageFile)
                        .centerInside()
                        .into(imageView);
            }
        } else {
            label.setVisibility(View.VISIBLE);
            label.setTextSize(TypedValue.COMPLEX_UNIT_DIP, QuestionFontSizeUtils.getQuestionFontSize());
            label.setText(choiceText == null || choiceText.isEmpty() ? errorMsg : choiceText);
        }
    }
}
