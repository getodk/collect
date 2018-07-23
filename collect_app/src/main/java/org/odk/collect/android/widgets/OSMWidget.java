package org.odk.collect.android.widgets;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.core.model.osm.OSMTag;
import org.javarosa.core.model.osm.OSMTagItem;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.logic.FormController;
import org.odk.collect.android.utilities.ViewIds;
import org.odk.collect.android.widgets.interfaces.BinaryWidget;
import org.opendatakit.httpclientandroidlib.entity.ContentType;

import java.util.ArrayList;
import java.util.List;

import static org.odk.collect.android.utilities.ApplicationConstants.RequestCodes;

/**
 * Widget that allows the user to launch OpenMapKit to get an OSM Feature with a
 * predetermined set of tags that are edited in the application.
 *
 * @author Nicholas Hallahan nhallahan@spatialdev.com
 */
@SuppressLint("ViewConstructor")
public class OSMWidget extends QuestionWidget implements BinaryWidget {

    // button colors
    private static final int OSM_GREEN = Color.rgb(126, 188, 111);
    private static final int OSM_BLUE = Color.rgb(112, 146, 255);

    private final Button launchOpenMapKitButton;
    private final String instanceDirectory;
    private final TextView errorTextView;
    private final TextView osmFileNameHeaderTextView;
    private final TextView osmFileNameTextView;

    private final List<OSMTag> osmRequiredTags;
    private final String instanceId;
    private final int formId;
    private final String formFileName;
    private String osmFileName;

    public OSMWidget(Context context, FormEntryPrompt prompt) {
        super(context, prompt);

        FormController formController = Collect.getInstance().getFormController();

        /*
         * NH: I'm trying to find the form xml file name, but this is neither
         * in the formController nor the formDef. In fact, it doesn't seem to
         * be saved into any object in JavaRosa. However, the mediaFolder
         * has the substring of the file name in it, so I extract the file name
         * from here. Awkward...
         */
        formFileName = formController.getMediaFolder().getName().split("-media")[0];

        instanceDirectory = formController.getInstanceFile().getParent();
        instanceId = formController.getSubmissionMetadata().instanceId;
        formId = formController.getFormDef().getID();

        errorTextView = new TextView(context);
        errorTextView.setId(ViewIds.generateViewId());
        errorTextView.setText(R.string.invalid_osm_data);

        // Determine the tags required
        osmRequiredTags = prompt.getQuestion().getOsmTags();

        // If an OSM File has already been saved, get the name.
        osmFileName = prompt.getAnswerText();

        // Setup Launch OpenMapKit Button
        launchOpenMapKitButton = getSimpleButton(ViewIds.generateViewId());

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
        launchOpenMapKitButton.setEnabled(!prompt.isReadOnly());

        osmFileNameHeaderTextView = new TextView(context);
        osmFileNameHeaderTextView.setId(ViewIds.generateViewId());
        osmFileNameHeaderTextView.setTextSize(20);
        osmFileNameHeaderTextView.setTypeface(null, Typeface.BOLD);
        osmFileNameHeaderTextView.setPadding(10, 0, 0, 10);
        osmFileNameHeaderTextView.setText(R.string.edited_osm_file);

        // text view showing the resulting OSM file name
        osmFileNameTextView = new TextView(context);
        osmFileNameTextView.setId(ViewIds.generateViewId());
        osmFileNameTextView.setTextSize(18);
        osmFileNameTextView.setTypeface(null, Typeface.ITALIC);
        if (osmFileName != null) {
            osmFileNameTextView.setText(osmFileName);
        } else {
            osmFileNameHeaderTextView.setVisibility(View.GONE);
        }
        TableLayout.LayoutParams params = new TableLayout.LayoutParams();
        params.setMargins(35, 30, 30, 35);
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
            boolean isIntentSafe = !activities.isEmpty();

            //launch activity if it is safe
            if (isIntentSafe) {
                // notify that the form is waiting for data
                waitForData();

                // launch
                ((Activity) ctx).startActivityForResult(launchIntent, RequestCodes.OSM_CAPTURE);
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
    }

    @Override
    public IAnswerData getAnswer() {
        String s = osmFileNameTextView.getText().toString();

        return !s.isEmpty()
                ? new StringData(s)
                : null;
    }

    @Override
    public void clearAnswer() {
        osmFileNameTextView.setText(null);
    }

    @Override
    public void onButtonClick(int buttonId) {
        launchOpenMapKitButton.setBackgroundColor(OSM_BLUE);
        Collect.getInstance().getActivityLogger().logInstanceAction(this,
                "launchOpenMapKitButton",
                "click", getFormEntryPrompt().getIndex());
        errorTextView.setVisibility(View.GONE);
        launchOpenMapKit();
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
        ArrayList<String> tagKeys = new ArrayList<>();
        for (OSMTag tag : osmRequiredTags) {
            tagKeys.add(tag.key);
            if (tag.label != null) {
                intent.putExtra("TAG_LABEL." + tag.key, tag.label);
            }
            ArrayList<String> tagValues = new ArrayList<>();
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
