package org.odk.collect.android.preferences;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.ListPreference;

import org.odk.collect.android.R;
import org.odk.collect.android.injection.DaggerUtils;
import org.odk.collect.android.preferences.dialogs.ReferenceLayerPreferenceDialog;
import org.odk.collect.settings.SettingsProvider;

import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

/** A ListPreference where each item has a caption **/
public class CaptionedListPreference extends ListPreference {

    private CharSequence[] captions;
    private int clickedIndex = -1;

    @Inject
    SettingsProvider settingsProvider;

    public CaptionedListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        DaggerUtils.getComponent(context).inject(this);
    }

    @Override
    public int getDialogLayoutResource() {
        return R.layout.captioned_list_dialog;
    }

    /** Sets the values, labels, and captions for the items in the dialog. */
    public void setItems(List<Item> items) {
        int count = items.size();
        String[] values = new String[count];
        String[] labels = new String[count];
        String[] captions = new String[count];
        for (int i = 0; i < count; i++) {
            values[i] = items.get(i).value;
            labels[i] = items.get(i).label;
            captions[i] = items.get(i).caption;
        }
        setEntryValues(values);
        setEntries(labels);
        setCaptions(captions);
    }

    /** Sets the list of items to offer as choices in the dialog. */
    public void setCaptions(CharSequence[] captions) {
        this.captions = captions;
    }

    /** Updates the contents of the dialog to show the items passed in by setItems etc.*/
    public void updateContent() {
        CharSequence[] values = getEntryValues();
        CharSequence[] labels = getEntries();

        if (ReferenceLayerPreferenceDialog.listView != null && values != null && labels != null && captions != null) {
            ReferenceLayerPreferenceDialog.listView.removeAllViews();
            for (int i = 0; i < values.length; i++) {
                inflateItem(ReferenceLayerPreferenceDialog.listView, i, values[i], labels[i], captions[i]);
            }
        }
    }

    /** Creates the view for one item in the list. */
    protected void inflateItem(ViewGroup parent, final int i, Object value, Object label, Object caption) {
        View item = LayoutInflater.from(getContext()).inflate(R.layout.captioned_item, null);
        RadioButton button = item.findViewById(R.id.button);
        TextView labelView = item.findViewById(R.id.label);
        TextView captionView = item.findViewById(R.id.caption);
        labelView.setText(String.valueOf(label));
        captionView.setText(String.valueOf(caption));
        button.setOnClickListener(view -> onItemClicked(i));
        item.setOnClickListener(view -> onItemClicked(i));
        parent.addView(item);
        if (Objects.equals(value, settingsProvider.getUnprotectedSettings().getString(getKey()))) {
            button.setChecked(true);
            item.post(() -> item.requestRectangleOnScreen(new Rect(0, 0, item.getWidth(), item.getHeight())));
        }
    }

    /** Saves the selected value to the preferences when the dialog is closed. */
    protected void onDialogClosed() {
        CharSequence[] values = getEntryValues();
        if (clickedIndex >= 0 && values != null) {
            Object value = values[clickedIndex];
            if (callChangeListener(value)) {
                setValue(value != null ? value.toString() : null);
            }
        }
    }

    /** When an item is clicked, record which item and then dismiss the dialog. */
    protected void onItemClicked(int index) {
        clickedIndex = index;
        onDialogClosed();
    }

    public static class Item {
        public final @Nullable
        String value;
        public final @NonNull
        String label;
        public final @NonNull String caption;

        public Item(@Nullable String value, @Nullable String label, @Nullable String caption) {
            this.value = value;
            this.label = label != null ? label : "";
            this.caption = caption != null ? caption : "";
        }
    }
}
