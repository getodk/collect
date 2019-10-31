package org.odk.collect.android.widgets;
import java.io.File;
import java.util.HashMap;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatRadioButton;

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
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.utilities.FileUtils;
import org.odk.collect.android.utilities.ViewIds;

import timber.log.Timber;

@SuppressLint("ViewConstructor")
public class LikertWidget extends ItemsWidget {


    private LinearLayout view;
    private RadioButton checkedButton;
    private final LinearLayout.LayoutParams LINEARLAYOUT_PARAMS = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1);
    private final LayoutParams TEXTVIEW_PARAMS = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
    private LayoutParams IMAGEVIEW_PARAMS = new LayoutParams(LayoutParams.WRAP_CONTENT , LayoutParams.WRAP_CONTENT);
    private  LayoutParams RADIOBUTTON_PARAMS = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    private LayoutParams BUTTONVIEW_PARAMS = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
    private LayoutParams LEFT_LINEVIEW_PARAMS = new LayoutParams(80, 2);
    private LayoutParams RIGHT_LINEVIEW_PARAMS = new LayoutParams(80, 2);

    HashMap<RadioButton, String> buttonsToName;

    public LikertWidget(Context context, QuestionDetails questionDetails) {
        super(context, questionDetails);

        setMainViewLayoutParameters();
        setStructures();

        setButtonListener();
        setSavedButton();
        addAnswerView(view);
    }

    public void setMainViewLayoutParameters(){
        view = new LinearLayout(getContext());
        view.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        view.setLayoutParams(params);
    }

    // Inserts the selected button from a saved state
    public void setSavedButton(){
        if (getFormEntryPrompt().getAnswerValue() != null) {
            String name = ((Selection) getFormEntryPrompt().getAnswerValue()
                    .getValue()).getValue();
            for(RadioButton bu: buttonsToName.keySet()){
                if(buttonsToName.get(bu).equals(name)){
                    checkedButton = bu;
                    checkedButton.setChecked(true);
                }
            }
        }
    }

    @Override
    public IAnswerData getAnswer() {
        if (checkedButton == null) {
            return null;
        } else {
            int selectedIndex = -1;
            for(int i = 0; i < items.size(); i++){
                if(items.get(i).getValue().equals(buttonsToName.get(checkedButton))){
                    selectedIndex = i;
                }
            }
            if(selectedIndex == -1){
                return null;
            }
            SelectChoice sc = items.get(selectedIndex);
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

    public void setStructures() {
        buttonsToName = new HashMap<>();
        for (int i = 0; i < items.size(); i++) {
            LinearLayout optionView = getLinearLayout();
            RelativeLayout buttonView = new RelativeLayout(this.getContext());
            BUTTONVIEW_PARAMS.addRule(CENTER_IN_PARENT, TRUE);
            buttonView.setLayoutParams(BUTTONVIEW_PARAMS);
            View leftLineView = new View(this.getContext());
            LEFT_LINEVIEW_PARAMS.addRule(ALIGN_PARENT_LEFT, TRUE);
            LEFT_LINEVIEW_PARAMS.addRule(CENTER_IN_PARENT, TRUE);
            leftLineView.setLayoutParams(LEFT_LINEVIEW_PARAMS);
            leftLineView.setBackgroundColor(Color.parseColor("#CCCCCC"));
            if (i != 0) {
                buttonView.addView(leftLineView);
            }

            RadioButton button = getRadioButton();
            buttonsToName.put(button, items.get(i).getValue());
            buttonView.addView(button);
            ImageView imgView = getImageAsImageView(i);
            // checks if image is set or valid
            if (imgView != null) {
                optionView.addView(imgView);
            }
            TextView choice = getTextView();
            choice.setText(getFormEntryPrompt().getSelectChoiceText(items.get(i)));

            View rightLineView = new View(this.getContext());
            RIGHT_LINEVIEW_PARAMS.addRule(ALIGN_PARENT_RIGHT, TRUE);
            RIGHT_LINEVIEW_PARAMS.addRule(CENTER_IN_PARENT, TRUE);
            rightLineView.setLayoutParams(RIGHT_LINEVIEW_PARAMS);
            rightLineView.setBackgroundColor(Color.parseColor("#CCCCCC"));

            if (i != items.size() - 1) {
                buttonView.addView(rightLineView);
            }

            optionView.addView(buttonView);
            optionView.addView(choice);
            view.addView(optionView);
        }
    }

    // Creates image view for choice
    public ImageView getImageView(){
        ImageView view = new ImageView(getContext());
        view.setLayoutParams(IMAGEVIEW_PARAMS);
        return view;
    }

    public RadioButton getRadioButton(){
        AppCompatRadioButton button = new AppCompatRadioButton(getContext());
        button.setId(ViewIds.generateViewId());
        button.setEnabled(!getFormEntryPrompt().isReadOnly());
        button.setFocusable(!getFormEntryPrompt().isReadOnly());
        RADIOBUTTON_PARAMS.addRule(CENTER_HORIZONTAL, TRUE);
        button.setLayoutParams(RADIOBUTTON_PARAMS);
        button.setGravity(Gravity.CENTER);
        return button;
    }

    // Creates text view for choice
    public TextView getTextView(){
        TextView view = new TextView(getContext());
        view.setGravity(Gravity.CENTER);
        view.setPadding(2,2,2,2);
        view.setLayoutParams(TEXTVIEW_PARAMS);
        view.setTextColor(Color.BLACK);
        return view;
    }

    // Linear Layout for new choice
    public LinearLayout getLinearLayout(){
        LinearLayout optionView = new LinearLayout(getContext());
        optionView.setGravity(Gravity.CENTER);
        optionView.setLayoutParams(LINEARLAYOUT_PARAMS);
        optionView.setOrientation(LinearLayout.VERTICAL);
        return optionView;
    }

    public void setButtonListener(){
        for (RadioButton button: buttonsToName.keySet()) {
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

    public ImageView getImageAsImageView(int index){
        ImageView view = getImageView();
        String imageURI;
        if (items.get(index) instanceof ExternalSelectChoice) {
            imageURI = ((ExternalSelectChoice) items.get(index)).getImage();
        } else {
            imageURI = getFormEntryPrompt().getSpecialFormSelectChoiceText(items.get(index),
                    FormEntryCaption.TEXT_FORM_IMAGE);
        }
        if(imageURI != null){
            String error = setImageFromOtherSource(imageURI, view);
            if(error != null){
                return null;
            }
            return view;
        }else{
            return null;
        }
    }

    public String setImageFromOtherSource(String imageURI, ImageView imageView){
        String errorMsg = null;
        try {
            String imageFilename =
                    ReferenceManager.instance().DeriveReference(imageURI).getLocalURI();
            final File imageFile = new File(imageFilename);
            if (imageFile.exists()) {
                Bitmap b = null;
                try {
                    DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();
                    int screenWidth = metrics.widthPixels;
                    int screenHeight = metrics.heightPixels;
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
                System.out.println(imageFile + " cannot be found ");
            }
            if (errorMsg != null) {
                Timber.e(errorMsg);
            }

        } catch (InvalidReferenceException e) {
            Timber.e(e, "Invalid image reference due to %s ", e.getMessage());
        }
        return errorMsg;
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        for (RadioButton r : buttonsToName.keySet()) {
            r.setOnLongClickListener(l);
        }
    }

    @Override
    public void cancelLongPress() {
        super.cancelLongPress();
        for (RadioButton r : buttonsToName.keySet()) {
            r.cancelLongPress();
        }
    }

    @Override
    public void clearAnswer() {
        if(checkedButton != null){
            checkedButton.setChecked(false);
        }
        checkedButton = null;
    }
}
