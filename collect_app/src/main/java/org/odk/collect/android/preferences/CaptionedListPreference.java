package org.odk.collect.android.preferences;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Rect;
import android.os.Parcelable;
import android.preference.ListPreference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import org.odk.collect.android.R;
import org.odk.collect.android.utilities.ObjectUtils;

import java.util.ArrayList;
import java.util.List;

/** A ListPreference where each item has a caption and the entire dialog also has a caption. */
public class CaptionedListPreference extends ListPreference {
    private final Context context;
    private CharSequence[] captions;
    private String dialogCaption;

    private List<RadioButton> radioButtons;
    private ViewGroup listView;
    private TextView captionView;
    private int clickedIndex = -1;

    public CaptionedListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        setDialogLayoutResource(R.layout.captioned_list_dialog);
    }

    /** Sets the values, labels, and captions for the items in the dialog. */
    public void setItems(CharSequence[] values, CharSequence[] labels, CharSequence[] captions) {
        setEntryValues(values != null ? values : new CharSequence[0]);
        setEntries(labels != null ? labels : values);
        setCaptions(captions != null ? captions : new CharSequence[values.length]);
    }

    /** Sets the list of items to offer as choices in the dialog. */
    public void setCaptions(CharSequence[] captions) {
        this.captions = captions;
    }

    /** Sets the caption to show at the bottom of the dialog. */
    public void setDialogCaption(String dialogCaption) {
        this.dialogCaption = dialogCaption;
    }

    @Override protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        // Selecting an item will close the dialog, so we don't need the "OK" button.
        builder.setPositiveButton(null, null);
    }

    /** Called just after the dialog's main view has been created. */
    @Override protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        listView = view.findViewById(R.id.list);
        captionView = view.findViewById(R.id.dialog_caption);
        updateContent();
    }

    /** Updates the contents of the dialog to show the items passed in by setItems etc. */
    public void updateContent() {
        CharSequence[] values = getEntryValues();
        CharSequence[] labels = getEntries();

        if (listView != null && values != null && labels != null && captions != null) {
            listView.removeAllViews();
            radioButtons = new ArrayList<>();
            for (int i = 0; i < values.length; i++) {
                radioButtons.add(inflateItem(listView, i, values[i], labels[i], captions[i]));
            }
        }
        if (captionView != null) {
            captionView.setText(dialogCaption);
        }
    }

    /** Creates the view for one item in the list. */
    protected RadioButton inflateItem(ViewGroup parent, final int i, Object value, Object label, Object caption) {
        View item = LayoutInflater.from(context).inflate(R.layout.captioned_item, null);
        RadioButton button = item.findViewById(R.id.button);
        TextView labelView = item.findViewById(R.id.label);
        TextView captionView = item.findViewById(R.id.caption);
        labelView.setText(String.valueOf(label));
        captionView.setText(String.valueOf(caption));
        button.setOnClickListener(view -> onItemClicked(i));
        item.setOnClickListener(view -> onItemClicked(i));
        parent.addView(item);
        if (ObjectUtils.equals(value, getSharedPreferences().getString(getKey(), null))) {
            button.setChecked(true);
            item.post(() -> item.requestRectangleOnScreen(new Rect(0, 0, item.getWidth(), item.getHeight())));
        }
        return button;
    }

    /** When an item is clicked, record which item and then dismiss the dialog. */
    protected void onItemClicked(int index) {
        clickedIndex = index;
        onClick(getDialog(), DialogInterface.BUTTON_POSITIVE);
        getDialog().dismiss();
    }

    /** Closes the dialog when the screen is rotated. */
    public Parcelable onSaveInstanceState() {
        // If the dialog is left open, it becomes empty when restored.  For now,
        // instead of building all the save/restore machinery, just close it.
        if (getDialog() != null) {
            getDialog().dismiss();
        }
        return super.onSaveInstanceState();
    }

    /** Saves the selected value to the preferences when the dialog is closed. */
    protected void onDialogClosed(boolean positiveResult) {
        CharSequence[] values = getEntryValues();
        if (positiveResult && clickedIndex >= 0 && values != null) {
            Object value = values[clickedIndex];
            if (callChangeListener(value)) {
                setValue(value != null ? value.toString() : null);
            }
        }
    }

    /** Opens the dialog programmatically, rather than by a click from the user. */
    public void showDialog() {
        showDialog(null);
    }
}
