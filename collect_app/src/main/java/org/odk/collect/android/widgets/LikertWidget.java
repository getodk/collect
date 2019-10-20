package org.odk.collect.android.widgets;
import java.io.File;
import java.util.HashMap;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.javarosa.core.model.SelectChoice;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.SelectOneData;
import org.javarosa.core.model.data.helper.Selection;
import org.javarosa.core.reference.InvalidReferenceException;
import org.javarosa.core.reference.ReferenceManager;
import org.javarosa.form.api.FormEntryCaption;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.R;
import org.odk.collect.android.external.ExternalSelectChoice;
import org.odk.collect.android.utilities.FileUtils;
import org.w3c.dom.Text;

import java.util.ArrayList;

import timber.log.Timber;

@SuppressLint("ViewConstructor")
public class LikertWidget extends ItemsWidget {

    private int iconDimension = 35;
    private LinearLayout view;
    private RadioGroup radioGroup;
    private RadioButton checkedButton;
    private boolean error = false;

    ArrayList<TextView> textViews;
    ArrayList<ImageView> imageViews;
    HashMap<RadioButton, String> buttonsToWeight;
    public LikertWidget(Context context, FormEntryPrompt prompt, boolean displayIcons) {
        super(context, prompt);

        view = (LinearLayout) getLayoutInflater().inflate(R.layout.likert_layout, this, false);
        radioGroup = view.findViewById(R.id.likert_scale);

        if(items == null || items.size() < 5){
            if(items == null){
                Timber.e("ERROR: Type does not exist");
            }else{
                Timber.e("ERROR: You need 5 choices." + items.size() + " choices were given.");
            }
            return;
        }

        setStructures();
        setButtonListener();
        if(displayIcons){
            showImages();
            hideTextViews();
        }

        // Inserts the selected button from a saved state
        if (prompt.getAnswerValue() != null) {
            String weight = ((Selection) prompt.getAnswerValue().getValue()).getValue();
            for(RadioButton bu: buttonsToWeight.keySet()){
                if(buttonsToWeight.get(bu).equals(weight)){
                    checkedButton = bu;
                    checkedButton.setChecked(true);
                }
            }
        }
        if(!error){addAnswerView(view);}
    }

    @Override
    public IAnswerData getAnswer() {
        if (checkedButton == null) {
            return null;
        } else {
            SelectChoice sc = items.get(Integer.parseInt(buttonsToWeight.get(checkedButton)));
            return new SelectOneData(new Selection(sc));
        }
    }

    /**
     * Default place to put the answer
     * (below the help text or question text if there is no help text)
     * If you have many elements, use this first
     * and use the standard addView(view, params) to place the rest
     */
    //TODO: Maybe try to call it from somewhere
    protected void addAnswerView(View v) {
        if (v == null) {
            Timber.e("cannot add a null view as an answerView");
            return;
        }
        // default place to add answer
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        params.addRule(RelativeLayout.BELOW, getHelpTextLayout().getId());

        params.setMargins(10, 0, 10, 0);
        addView(v, params);
    }

    public void setStructures(){
        buttonsToWeight = new HashMap<>();
        imageViews = new ArrayList<>();
        textViews = new ArrayList<>();

        for (int i=0;i < radioGroup.getChildCount(); i++) {
            View v = radioGroup.getChildAt(i);
            if (v instanceof LinearLayout) {
                for(int j = 0; j < ((LinearLayout) v).getChildCount(); j++){
                    View v2 = ((LinearLayout) v).getChildAt(j);
                    if(v2 instanceof RadioButton){
                        buttonsToWeight.put((RadioButton) v2, "" + i);
                    }else if(v2 instanceof TextView){
                        TextView textView = (TextView) v2;
                        textView.setText(getFormEntryPrompt().getSelectChoiceText(items.get(i)));
                        textViews.add((TextView) v2);
                    }else if(v2 instanceof ImageView){
                        imageViews.add((ImageView) v2);
                    }
                }
            }
        }
    }

    public void setButtonListener(){
        for (RadioButton button: buttonsToWeight.keySet()) {
            button.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    RadioButton r = (RadioButton) v;
                    if(checkedButton != null){
                        checkedButton.setChecked(false);
                    }
                    checkedButton = r;
                    checkedButton.setChecked(true);
                }
            });
        }
    }

    public void showImages(){
        for(int i = 0; i < imageViews.size(); i++){
            ImageView view = imageViews.get(i);
            String imageURI;
            if (items.get(i) instanceof ExternalSelectChoice) {
                imageURI = ((ExternalSelectChoice) items.get(i)).getImage();
            } else {
                imageURI = getFormEntryPrompt().getSpecialFormSelectChoiceText(items.get(i),
                        FormEntryCaption.TEXT_FORM_IMAGE);
            }
            if(imageURI != null){
                setImageFromOtherSource(imageURI, view);
            }
            view.setVisibility(View.VISIBLE);
        }
    }

    public void setImageFromOtherSource(String imageURI, ImageView imageView){
        try {
            String errorMsg = null;
            String imageFilename =
                    ReferenceManager.instance().DeriveReference(imageURI).getLocalURI();
            final File imageFile = new File(imageFilename);
            if (imageFile.exists()) {
                Bitmap b = null;
                try {
                    int screenWidth = iconDimension;
                    int screenHeight = iconDimension;
                    b = FileUtils.getBitmapScaledToDisplay(imageFile, screenHeight, screenWidth);
                } catch (OutOfMemoryError e) {
                    errorMsg = "ERROR: " + e.getMessage();
                }

                if (b != null) {
                    imageView.setAdjustViewBounds(true);
                    imageView.setImageBitmap(b);
                } else if (errorMsg == null) {
                    // Loading the image failed. The image work when in .jpg format
                    errorMsg = getContext().getString(R.string.file_invalid, imageFile);

                }
            } else {
                // The file does not exist
                errorMsg = getContext().getString(R.string.file_missing, imageFile);
            }
            if (errorMsg != null) {
                Timber.e(errorMsg);
                error = true;
            }

        } catch (InvalidReferenceException e) {
            Timber.e(e, "Invalid image reference due to %s ", e.getMessage());
        }
    }

    public void hideTextViews(){
        for(TextView view: textViews){
            view.setVisibility(View.GONE);
        }
    }

    private LayoutInflater layoutInflater;

    private LayoutInflater getLayoutInflater() {

        // Only for testing purposes, this shouldn't actually be cached:
        if (this.layoutInflater != null) {
            return layoutInflater;
        }

        return LayoutInflater.from(getContext());
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        /** TODO */
    }

    @Override
    public void clearAnswer() {
        if(checkedButton != null){
            checkedButton.setChecked(false);
        }
        checkedButton = null;
    }


}
