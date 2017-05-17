package org.odk.collect.android.widgets;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.core.model.osm.OSMTag;
import org.javarosa.core.model.osm.OSMTagItem;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.logic.FormController;
import org.opendatakit.httpclientandroidlib.entity.ContentType;

import java.util.ArrayList;
import java.util.List;

/**
 * Widget that allows the user to launch OpenMapKit to get an OSM Feature with a
 * predetermined set of tags that are edited in the application.
 *
 * @author Nicholas Hallahan nhallahan@spatialdev.com
 */
public class OSMWidget extends QuestionWidget implements IBinaryWidget {

    // button colors
    private static final int OSM_GREEN = Color.rgb(126, 188, 111);
    private static final int OSM_BLUE = Color.rgb(112, 146, 255);

    private Button launchOpenMapKitButton;
    private String binaryName;
    private String instanceDirectory;
    private TextView errorTextView;
    private TextView osmFileNameHeaderTextView;
    private TextView osmFileNameTextView;

    private List<OSMTag> osmRequiredTags;
    private String instanceId;
    private int formId;
    private String formFileName;
    private String osmFileName;

    public OSMWidget(Context context, FormEntryPrompt prompt) {
        super(context, prompt);

        FormController formController = Collect.getInstance().getFormController();

        /**
         * NH: I'm trying to find the form xml file name, but this is neither
         * in the formController nor the formDef. In fact, it doesn't seem to
         * be saved into any object in JavaRosa. However, the mediaFolder
         * has the substring of the file name in it, so I extract the file name
         * from here. Awkward...
         */
        formFileName = formController.getMediaFolder().getName().split("-media")[0];

        instanceDirectory = formController.getInstancePath().getParent();
        instanceId = formController.getSubmissionMetadata().instanceId;
        formId = formController.getFormDef().getID();

        errorTextView = new TextView(context);
        errorTextView.setId(QuestionWidget.newUniqueId());
        errorTextView.setText(R.string.invalid_osm_data);

        // Determine the tags required
        QuestionDef question = prompt.getQuestion();
        osmRequiredTags = prompt.getQuestion().getOsmTags();

        // If an OSM File has already been saved, get the name.
        osmFileName = prompt.getAnswerText();

        // Setup Launch OpenMapKit Button
        launchOpenMapKitButton = new Button(getContext());
        launchOpenMapKitButton.setId(QuestionWidget.newUniqueId());

        // Button Styling
        if (osmFileName != null) {
            launchOpenMapKitButton.setBackgroundColor(OSM_BLUE);
        } else {
            launchOpenMapKitButton.setBackgroundColor(OSM_GREEN);
        }
        launchOpenMapKitButton.setTextColor(Color.WHITE); // White text
        if (osmFileName != null) {
            launchOpenMapKitButton.setText(getContext().getString(R.string.recapture_osm));
        } else {
            launchOpenMapKitButton.setText(getContext().getString(R.string.capture_osm));
        }
        launchOpenMapKitButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, answerFontsize);
        launchOpenMapKitButton.setPadding(20, 20, 20, 20);
        launchOpenMapKitButton.setEnabled(!prompt.isReadOnly());
        TableLayout.LayoutParams params = new TableLayout.LayoutParams();
        params.setMargins(35, 30, 30, 35);
        launchOpenMapKitButton.setLayoutParams(params);

        // Launch OpenMapKit intent on click
        launchOpenMapKitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchOpenMapKitButton.setBackgroundColor(OSM_BLUE);
                Collect.getInstance().getActivityLogger().logInstanceAction(this,
                        "launchOpenMapKitButton",
                        "click", formEntryPrompt.getIndex());
                errorTextView.setVisibility(View.GONE);
                launchOpenMapKit();
            }
        });

        osmFileNameHeaderTextView = new TextView(context);
        osmFileNameHeaderTextView.setId(QuestionWidget.newUniqueId());
        osmFileNameHeaderTextView.setTextSize(20);
        osmFileNameHeaderTextView.setTypeface(null, Typeface.BOLD);
        osmFileNameHeaderTextView.setPadding(10, 0, 0, 10);
        osmFileNameHeaderTextView.setText(R.string.edited_osm_file);

        // text view showing the resulting OSM file name
        osmFileNameTextView = new TextView(context);
        osmFileNameTextView.setId(QuestionWidget.newUniqueId());
        osmFileNameTextView.setTextSize(18);
        osmFileNameTextView.setTypeface(null, Typeface.ITALIC);
        if (osmFileName != null) {
            osmFileNameTextView.setText(osmFileName);
        } else {
            osmFileNameHeaderTextView.setVisibility(View.GONE);
        }
        osmFileNameTextView.setLayoutParams(params);


        // finish complex layout
        LinearLayout answerLayout = new LinearLayout(getContext());
        answerLayout.setOrientation(LinearLayout.VERTICAL);
        answerLayout.addView(launchOpenMapKitButton);
        answerLayout.addView(errorTextView);
        answerLayout.addView(osmFileNameHeaderTextView);
        answerLayout.addView(osmFileNameTextView);
        addAnswerView(answerLayout);

        // Hide Launch button if read-only
        if (prompt.isReadOnly()) {
            launchOpenMapKitButton.setVisibility(View.GONE);
        }
        errorTextView.setVisibility(View.GONE);
    }

    private void launchOpenMapKit() {
        try {
            //launch with intent that sends plain text
            Intent launchIntent = new Intent(Intent.ACTION_SEND);
            launchIntent.setType(ContentType.TEXT_PLAIN.getMimeType());

            //send form id
            launchIntent.putExtra("FORM_ID", String.valueOf(formId));

            //send instance id
            launchIntent.putExtra("INSTANCE_ID", instanceId);

            //send instance directory
            launchIntent.putExtra("INSTANCE_DIR", instanceDirectory);

            //send form file name
            launchIntent.putExtra("FORM_FILE_NAME", formFileName);

            //send OSM file name if there was a previous edit
            if (osmFileName != null) {
                launchIntent.putExtra("OSM_EDIT_FILE_NAME", osmFileName);
            }

            //send encode tag data structure to intent
            writeOsmRequiredTagsToExtras(launchIntent);

            //verify the package resolves before starting activity
            Context ctx = getContext();
            PackageManager packageManager = ctx.getPackageManager();
            List<ResolveInfo> activities = packageManager.queryIntentActivities(launchIntent, 0);
            boolean isIntentSafe = activities.size() > 0;

            //launch activity if it is safe
            if (isIntentSafe) {
                // notify that the form is waiting for data
                Collect.getInstance().getFormController().setIndexWaitingForData(
                        formEntryPrompt.getIndex());
                // launch
                ((Activity) ctx).startActivityForResult(launchIntent,
                        FormEntryActivity.OSM_CAPTURE);
            } else {
                errorTextView.setVisibility(View.VISIBLE);
            }
        } catch (Exception ex) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle(R.string.alert);
            builder.setMessage(R.string.install_openmapkit);
            DialogInterface.OnClickListener okClickListener = new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    //TODO: launch to app store?
                }
            };

            builder.setPositiveButton("Ok", okClickListener);
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    @Override
    public void setBinaryData(Object answer) {
        // show file name of saved osm data
        osmFileName = (String) answer;
        osmFileNameTextView.setText(osmFileName);
        osmFileNameHeaderTextView.setVisibility(View.VISIBLE);
        osmFileNameTextView.setVisibility(View.VISIBLE);

        Collect.getInstance().getFormController().setIndexWaitingForData(null);
    }

    @Override
    public void cancelWaitingForBinaryData() {
        Collect.getInstance().getFormController().setIndexWaitingForData(null);
    }

    @Override
    public boolean isWaitingForBinaryData() {
        return formEntryPrompt.getIndex().equals(
                Collect.getInstance().getFormController()
                        .getIndexWaitingForData());
    }

    @Override
    public IAnswerData getAnswer() {
        String s = osmFileNameTextView.getText().toString();
        if (s == null || s.equals("")) {
            return null;
        } else {
            return new StringData(s);
        }
    }

    @Override
    public void clearAnswer() {
        osmFileNameTextView.setText(null);
    }

    @Override
    public void setFocus(Context context) {
        // Hide the soft keyboard if it's showing.
        InputMethodManager inputManager = (InputMethodManager) context
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(this.getWindowToken(), 0);
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        osmFileNameTextView.setOnLongClickListener(l);
        launchOpenMapKitButton.setOnLongClickListener(l);
    }

    /**
     * See: https://github.com/AmericanRedCross/openmapkit/wiki/ODK-Collect-Tag-Intent-Extras
     */
    private void writeOsmRequiredTagsToExtras(Intent intent) {
        ArrayList<String> tagKeys = new ArrayList<String>();
        for (OSMTag tag : osmRequiredTags) {
            tagKeys.add(tag.key);
            if (tag.label != null) {
                intent.putExtra("TAG_LABEL." + tag.key, tag.label);
            }
            ArrayList<String> tagValues = new ArrayList<String>();
            if (tag.items != null) {
                for (OSMTagItem item : tag.items) {
                    tagValues.add(item.value);
                    if (item.label != null) {
                        intent.putExtra("TAG_VALUE_LABEL." + tag.key + "." + item.value,
                                item.label);
                    }
                }
            }
            intent.putStringArrayListExtra("TAG_VALUES." + tag.key, tagValues);
        }
        intent.putStringArrayListExtra("TAG_KEYS", tagKeys);
    }
}
