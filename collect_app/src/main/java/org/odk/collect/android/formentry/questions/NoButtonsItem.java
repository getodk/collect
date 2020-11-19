package org.odk.collect.android.formentry.questions;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.javarosa.core.model.SelectChoice;
import org.javarosa.core.reference.InvalidReferenceException;
import org.javarosa.core.reference.ReferenceManager;
import org.javarosa.form.api.FormEntryCaption;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.R;
import org.odk.collect.android.external.ExternalSelectChoice;
import org.odk.collect.android.utilities.QuestionFontSizeUtils;
import org.odk.collect.android.utilities.StringUtils;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class NoButtonsItem extends FrameLayout {

    @BindView(R.id.imageView)
    ImageView imageView;

    @BindView(R.id.label)
    TextView label;

    public NoButtonsItem(Context context) {
        super(context);

        View.inflate(context, R.layout.no_buttons_item_layout, this);
        ButterKnife.bind(this);
    }

    public NoButtonsItem(Context context, AttributeSet attrs) {
        super(context, attrs);

        View.inflate(context, R.layout.no_buttons_item_layout, this);
        ButterKnife.bind(this);
    }

    public void setUpView(SelectChoice selectChoice, FormEntryPrompt prompt, int numColumns) {
        String imageURI = selectChoice instanceof ExternalSelectChoice
                ? ((ExternalSelectChoice) selectChoice).getImage()
                : prompt.getSpecialFormSelectChoiceText(selectChoice, FormEntryCaption.TEXT_FORM_IMAGE);

        String errorMsg = null;
        if (imageURI != null) {
            try {
                final File imageFile = new File(ReferenceManager.instance().deriveReference(imageURI).getLocalURI());
                if (imageFile.exists()) {
                    imageView.setVisibility(View.VISIBLE);

                    Glide.with(this)
                            .load(imageFile)
                            .centerInside()
                            .into(imageView);
                } else {
                    errorMsg = getContext().getString(R.string.file_missing, imageFile);
                }
            } catch (InvalidReferenceException e) {
                Timber.e("Image invalid reference due to %s ", e.getMessage());
            }
        } else {
            errorMsg = "";
        }

        if (errorMsg != null) {
            label.setVisibility(View.VISIBLE);
            label.setTextSize(TypedValue.COMPLEX_UNIT_DIP, QuestionFontSizeUtils.getQuestionFontSize());
            label.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
            String choiceText = StringUtils.textToHtml(prompt.getSelectChoiceText(selectChoice)).toString();

            if (!choiceText.isEmpty()) {
                label.setText(choiceText);
            } else {
                Timber.e(errorMsg);
                label.setText(errorMsg);
            }

            label.setId(R.id.text_label);
        }
    }
}
