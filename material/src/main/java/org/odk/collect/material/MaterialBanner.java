package org.odk.collect.material;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import static org.odk.collect.material.ContextUtils.getAttributeValue;

/**
 * Provides an implementation of Material's "Banner" (https://material.io/components/banners) as no
 * implementation currently exists in the Material Components framework
 */
public class MaterialBanner extends FrameLayout {
    public MaterialBanner(@NonNull Context context) {
        super(context);
        init(context, null);
    }

    public MaterialBanner(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public MaterialBanner(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        View.inflate(context, R.layout.material_banner, this);

        if (attrs != null) {
            applyAttributes(context, attrs);
        }
    }

    public void setText(String text) {
        TextView textView = findViewById(R.id.text);
        textView.setText(text);
    }

    public void setActionText(String actionTitle) {
        Button button = findViewById(R.id.button);
        button.setText(actionTitle);
    }

    public void setAction(OnActionListener listener) {
        Button button = findViewById(R.id.button);
        button.setOnClickListener(v -> listener.onAction());
    }

    private void applyAttributes(Context context, AttributeSet attrs) {
        TypedArray array = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.MaterialBanner,
                0, 0);


        if (array.getString(R.styleable.MaterialBanner_text) != null) {
            setText(array.getString(R.styleable.MaterialBanner_text));
        }

        if (array.getString(R.styleable.MaterialBanner_actionText) != null) {
            setActionText(array.getString(R.styleable.MaterialBanner_actionText));
        }

        if (array.getBoolean(R.styleable.MaterialBanner_secondaryActionColor, false)) {
            Button button = findViewById(R.id.button);
            button.setTextColor(getAttributeValue(getContext(), R.attr.colorSecondary));
        }

        array.recycle();
    }

    public interface OnActionListener {
        void onAction();
    }
}
