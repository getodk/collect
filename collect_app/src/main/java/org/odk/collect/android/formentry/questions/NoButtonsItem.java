package org.odk.collect.android.formentry.questions;

import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.bumptech.glide.Glide;

import org.odk.collect.android.databinding.NoButtonsItemLayoutBinding;
import org.odk.collect.android.utilities.QuestionFontSizeUtils;

import java.io.File;

public class NoButtonsItem extends FrameLayout {
    NoButtonsItemLayoutBinding binding;

    public NoButtonsItem(Context context, boolean enabled) {
        super(context);
        binding = NoButtonsItemLayoutBinding.inflate(LayoutInflater.from(context), this, true);

        setLongClickable(true);
        setEnabled(enabled);
    }

    public void setUpNoButtonsItem(File imageFile, String choiceText, String errorMsg, boolean isInGridView) {
        if (imageFile != null && imageFile.exists()) {
            binding.imageView.setVisibility(View.VISIBLE);
            if (isInGridView) {
                Glide.with(this)
                        .load(imageFile)
                        .fitCenter()
                        .into(binding.imageView);
            } else {
                Glide.with(this)
                        .load(imageFile)
                        .centerInside()
                        .into(binding.imageView);
            }
        } else {
            binding.label.setVisibility(View.VISIBLE);
            binding.label.setTextSize(TypedValue.COMPLEX_UNIT_DIP, QuestionFontSizeUtils.getQuestionFontSize());
            binding.label.setText(choiceText == null || choiceText.isEmpty() ? errorMsg : choiceText);
        }
    }
}
