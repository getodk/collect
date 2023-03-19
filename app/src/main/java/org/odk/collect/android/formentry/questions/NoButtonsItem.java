package org.odk.collect.android.formentry.questions;

import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import org.odk.collect.android.databinding.NoButtonsItemLayoutBinding;
import org.odk.collect.android.utilities.QuestionFontSizeUtils;
import org.odk.collect.imageloader.ImageLoader;

import java.io.File;

public class NoButtonsItem extends FrameLayout {
    NoButtonsItemLayoutBinding binding;
    private final ImageLoader imageLoader;

    public NoButtonsItem(Context context, boolean enabled, ImageLoader imageLoader) {
        super(context);
        binding = NoButtonsItemLayoutBinding.inflate(LayoutInflater.from(context), this, true);
        this.imageLoader = imageLoader;

        setLongClickable(true);
        setEnabled(enabled);
    }

    public void setUpNoButtonsItem(File imageFile, String choiceText, String errorMsg, boolean isInGridView) {
        if (imageFile != null && imageFile.exists()) {
            binding.imageView.setVisibility(View.VISIBLE);
            if (isInGridView) {
                imageLoader.loadImage(binding.imageView, imageFile, ImageView.ScaleType.FIT_CENTER, null);
            } else {
                imageLoader.loadImage(binding.imageView, imageFile, ImageView.ScaleType.CENTER_INSIDE, null);
            }
        } else {
            binding.label.setVisibility(View.VISIBLE);
            binding.label.setTextSize(TypedValue.COMPLEX_UNIT_DIP, QuestionFontSizeUtils.getQuestionFontSize());
            binding.label.setText(choiceText == null || choiceText.isEmpty() ? errorMsg : choiceText);
        }
    }
}
