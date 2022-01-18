package org.odk.collect.android.formentry.questions;

import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import org.odk.collect.android.databinding.NoButtonsItemLayoutBinding;
import org.odk.collect.android.utilities.QuestionFontSizeUtils;
import org.odk.collect.glide.ImageLoader;

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
                ImageLoader.loadImage(binding.imageView, imageFile, ImageView.ScaleType.FIT_CENTER);
            } else {
                ImageLoader.loadImage(binding.imageView, imageFile, ImageView.ScaleType.CENTER_INSIDE);
            }
        } else {
            binding.label.setVisibility(View.VISIBLE);
            binding.label.setTextSize(TypedValue.COMPLEX_UNIT_DIP, QuestionFontSizeUtils.getQuestionFontSize());
            binding.label.setText(choiceText == null || choiceText.isEmpty() ? errorMsg : choiceText);
        }
    }
}
