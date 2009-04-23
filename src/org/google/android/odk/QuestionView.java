/*
 * Copyright (C) 2009 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.google.android.odk;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Bitmap.CompressFormat;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.InputFilter;
import android.text.method.DigitsKeyListener;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.RadioGroup.OnCheckedChangeListener;

import org.javarosa.core.model.Constants;
import org.javarosa.core.model.data.DateData;
import org.javarosa.core.model.data.DecimalData;
import org.javarosa.core.model.data.GeoPointData;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.IntegerData;
import org.javarosa.core.model.data.PointerAnswerData;
import org.javarosa.core.model.data.SelectMultiData;
import org.javarosa.core.model.data.SelectOneData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.core.model.data.helper.BasicDataPointer;
import org.javarosa.core.model.data.helper.Selection;
import org.javarosa.core.util.OrderedHashtable;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;


/**
 * Responsible for using a {@link PromptElement} and based on the question type
 * and answer type, displaying the appropriate widget. The class also sets (but
 * does not save) and gets the answers to questions and manages GPS and Camera
 * if appropriate.
 * 
 * @author Yaw Anokwa
 */

// TODO select multi, select one
public class QuestionView extends LinearLayout {

    private final static String t = "QuestionView";

    // identify a group of checkboxes
    private final static int CHECKBOX_ID = 100;

    // convert from j2me date to android date
    private final static int YEARSHIFT = 1900;

    // layout question, group and answer widgets.
    private ScrollView mView;

    private PromptElement mPrompt;
    private IAnswerData mAnswer;

    private LocationManager mLocationManager;
    private LocationListener mLocationListener;
    private Location mLocation;

    private ProgressDialog mLocationDialog;
    private DatePicker mDateAnswer;
    private DatePicker.OnDateChangedListener mDateListener;
    private RadioGroup mRadioAnswer;
    private ImageView mImageAnswer;

    // view for decimal, integer, geopoint, string and untyped answer.
    private TextView mStringAnswer;

    // store image data.
    private byte[] mImageData;

    // start camera or start GPS.
    private Button mActionButton;

    // is answer readonly
    private boolean mReadOnly = false;

    // first time displaying radio or checkbox
    private int mRadioSelected = -1;
    // private boolean mRadioInit = true;
    private boolean mCheckboxInit = true;


    public QuestionView(Context context, PromptElement mPrompt) {
        super(context);
        setPrompt(mPrompt);
    }


    public PromptElement getPrompt() {
        return mPrompt;
    }


    private void setPrompt(PromptElement mPrompt) {
        this.mPrompt = mPrompt;
        mReadOnly = mPrompt.isReadonly();
    }


    /**
     * Cancel dialogs when view is terminated
     */
    @Override
    protected Parcelable onSaveInstanceState() {
        if (mLocationDialog != null && mLocationDialog.isShowing()) {
            mLocationDialog.dismiss();
        }
        return super.onSaveInstanceState();
    }


    /**
     * Create the appropriate view given your prompt.
     */
    public void buildView() {

        mView = new ScrollView(getContext());

        setOrientation(LinearLayout.VERTICAL);
        setPadding(10, 10, 10, 10);

        // display which group you are in as well as the question
        GroupTextView();
        QuestionTextView();

        // if question or answer type is not supported, use text widget
        switch (mPrompt.getQuestionType()) {
            case Constants.CONTROL_INPUT:
                switch (mPrompt.getAnswerType()) {
                    case Constants.DATATYPE_DATE:
                        DateView();
                        break;
                    case Constants.DATATYPE_DECIMAL:
                        DecimalView();
                        break;
                    case Constants.DATATYPE_INTEGER:
                        IntegerView();
                        break;
                    case Constants.DATATYPE_GEOPOINT:
                        GeoPointView();
                        break;
                    default:
                        StringView();
                        break;
                }
                break;
            case Constants.CONTROL_IMAGE_CHOOSE:
                ImageView();
                break;
            case Constants.CONTROL_SELECT_ONE:
                SelectOneView();
                break;
            case Constants.CONTROL_SELECT_MULTI:
                SelectMultiView();
                break;
            default:
                StringView();
                break;
        }

        // add our view to parentview
        addView(mView);
    }


    /**
     * Reset the answer in your view. Text will be removed. Checkboxes and radio
     * buttons will be deselected. Dates will be set to now.
     */
    public void clearAnswer() {
        switch (mPrompt.getQuestionType()) {
            case Constants.CONTROL_IMAGE_CHOOSE:
                ImageReset();
                break;
            case Constants.CONTROL_INPUT:
                switch (mPrompt.getAnswerType()) {
                    case Constants.DATATYPE_DATE:
                        DateReset();
                        break;
                    case Constants.DATATYPE_INTEGER:
                    case Constants.DATATYPE_DECIMAL:
                    case Constants.DATATYPE_GEOPOINT:
                    default:
                        StringReset();
                        break;
                }
                break;
            case Constants.CONTROL_SELECT_ONE:
                SelectOneReset();
                break;
            case Constants.CONTROL_SELECT_MULTI:
                SelectMultiReset();
                break;
            default:
                StringReset();
                break;
        }
    }


    /**
     * Store date answer as {@link DateData}.
     */
    private void DateAnswer() {
        Date d =
                new Date(mDateAnswer.getYear() - YEARSHIFT, mDateAnswer.getMonth(), mDateAnswer
                        .getDayOfMonth());
        mAnswer = new DateData(d);
    }


    /**
     * Set date to now.
     */
    private void DateReset() {
        final Calendar c = Calendar.getInstance();
        mDateAnswer.init(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH),
                mDateListener);
    }


    /**
     * Build view for date answer. Includes retrieving existing answer.
     */
    private void DateView() {
        final Calendar c = Calendar.getInstance();
        mDateAnswer = new DatePicker(getContext());
        mDateListener = new DatePicker.OnDateChangedListener() {
            public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                if (mReadOnly) {
                    if (mPrompt.getAnswerValue() != null) {
                        Date d = (Date) mPrompt.getAnswerObject();
                        view.updateDate(d.getYear() + YEARSHIFT, d.getMonth(), d.getDate());
                    } else {
                        view.updateDate(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c
                                .get(Calendar.DAY_OF_MONTH));
                    }
                } else {
                    c.set(Calendar.MONTH, monthOfYear);
                    int maxDays = c.getActualMaximum(Calendar.DAY_OF_MONTH);
                    if (dayOfMonth > maxDays) {
                        view.updateDate(year, monthOfYear, maxDays);
                    }
                }
            }
        };

        if (mPrompt.getAnswerValue() != null) {
            Date d = (Date) mPrompt.getAnswerObject();
            mDateAnswer.init(d.getYear() + YEARSHIFT, d.getMonth(), d.getDate(), mDateListener);
        } else {
            // create date widget with now
            DateReset();
        }

        mView.addView(mDateAnswer);

    }


    /**
     * Store decimal answer as {@link DecimalData}.
     */
    private void DecimalAnswer() {
        String s = GenericAnswer();
        if (s != null) {
            try {
                mAnswer = new DecimalData(Double.valueOf(s).doubleValue());
            } catch (Exception NumberFormatException) {
                mAnswer = null;
            }
        }
    }


    /**
     * Build view for decimal answer. Includes retrieving existing answer.
     */
    private void DecimalView() {
        GenericView(mReadOnly);
        mStringAnswer.setWidth(200);
        mStringAnswer.setKeyListener(new DigitsKeyListener(true, true));
        mView.addView(mStringAnswer);
    }


    /**
     * Store geopoint answer as {@link GeoPointData}.
     */
    private void GeoPointAnswer() {

        String s = GenericAnswer();
        if (s != null) {
            try {
                // segment lat and lon
                String[] sga = s.replace("lat: ", "").replace("\nlon: ", ",").split("[,]");
                double gp[] = new double[2];
                gp[0] = Double.valueOf(sga[0]).doubleValue();
                gp[1] = Double.valueOf(sga[1]).doubleValue();
                mAnswer = new GeoPointData(gp);
            } catch (Exception NumberFormatException) {
                mAnswer = null;
            }
        }

        // once we have a geopoint answer, turn off gps
        stopGPS();
    }


    /**
     * Build view for geopoint answer. Includes retrieving existing answer and
     * GPS management.
     */
    private void GeoPointView() {

        // view is too complex for scrollview
        LinearLayout ll = new LinearLayout(getContext());
        ll.setOrientation(LinearLayout.VERTICAL);

        mActionButton = new Button(getContext());
        mActionButton.setPadding(20, 20, 20, 20);
        mActionButton.setText(getContext().getString(R.string.get_location));
        mActionButton.setTextSize(TypedValue.COMPLEX_UNIT_PT, (float) 10);
        mActionButton.setEnabled(!mReadOnly);

        // gps has to readonly
        GenericView(true);

        // string manipulation for readability
        String s = (String) mStringAnswer.getText();
        if (!s.equals("")) {
            mStringAnswer.setText("lat: " + s.replace(",", "\nlon: "));
        }
        mStringAnswer.setGravity(Gravity.CENTER);

        // when you press the button
        mActionButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startGPS();

                // dialog displayed while fetching gps location
                mLocationDialog = new ProgressDialog(getContext());
                DialogInterface.OnClickListener geopointButtonListener =
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                // on cancel, stop gps
                                stopGPS();
                            }
                        };

                // back button doesn't cancel
                mLocationDialog.setCancelable(false);

                mLocationDialog.setIndeterminate(true);
                mLocationDialog.setMessage(getContext().getString(R.string.getting_location));
                mLocationDialog.setButton(getContext().getString(R.string.cancel),
                        geopointButtonListener);
                mLocationDialog.show();


            }
        });

        // finish complex layout
        ll.addView(mActionButton);
        ll.addView(mStringAnswer);

        mView.addView(ll);
    }


    /**
     * Based on question type and answer type, store the answer in the correct
     * {@link IAnswerData} object.
     */
    public IAnswerData getAnswer() {
        switch (mPrompt.getQuestionType()) {
            case Constants.CONTROL_IMAGE_CHOOSE:
                ImageAnswer();
                break;
            case Constants.CONTROL_INPUT:
                switch (mPrompt.getAnswerType()) {
                    case Constants.DATATYPE_DATE:
                        DateAnswer();
                        break;
                    case Constants.DATATYPE_INTEGER:
                        IntegerAnswer();
                        break;
                    case Constants.DATATYPE_DECIMAL:
                        DecimalAnswer();
                        break;
                    case Constants.DATATYPE_GEOPOINT:
                        GeoPointAnswer();
                        break;
                    default:
                        StringAnswer();
                        break;
                }
                break;
            case Constants.CONTROL_SELECT_ONE:
                SelectOneAnswer();
                break;
            case Constants.CONTROL_SELECT_MULTI:
                SelectMultiAnswer();
                break;
            default:
                StringAnswer();
                break;
        }

        return mAnswer;
    }



    /**
     * Build view for hierarchy of groups the question belongs.
     */
    private void GroupTextView() {
        String s = "";

        // list all groups in one string
        for (GroupElement g : mPrompt.getGroups()) {
            int i = g.getRepeatCount() + 1;
            s += g.getGroupText();
            if (i > 1) {
                s += " (" + i + ")";
            }
            s += " > ";
        }

        // build view
        if (!s.equals("")) {
            TextView tv = new TextView(getContext());
            tv.setText(s.substring(0, s.length() - 3));
            tv.setTextColor(Color.LTGRAY);
            tv.setTextSize(TypedValue.COMPLEX_UNIT_PT, (float) 7.0);
            tv.setPadding(0, 0, 0, 5);
            addView(tv);
        }

    }


    /**
     * Store image answer as {@link PointerAnswerData}.
     */
    private void ImageAnswer() {

        long ts = Calendar.getInstance().getTimeInMillis();
        BasicDataPointer bdp = new BasicDataPointer("image_" + ts, mImageData);
        if (mImageData != null) {
            mAnswer = new PointerAnswerData(bdp);
        } else {
            mAnswer = null;
        }
    }


    /**
     * Set image to null. Cleans image array and image view.
     */
    private void ImageReset() {
        mImageData = null;
        mImageAnswer.setVisibility(GONE);
    }


    /**
     * Build view for image answer. Includes retrieving existing answer and
     * camera management.
     */
    private void ImageView() {

        // view is too complex for scrollview
        LinearLayout ll = new LinearLayout(getContext());
        ll.setOrientation(LinearLayout.VERTICAL);

        mActionButton = new Button(getContext());
        mActionButton.setText(getContext().getString(R.string.get_image));
        mActionButton.setTextSize(TypedValue.COMPLEX_UNIT_PT, (float) 10);
        mActionButton.setPadding(20, 20, 20, 20);
        mActionButton.setEnabled(!mReadOnly);

        // launch image capture intent on click
        mActionButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent("android.media.action.IMAGE_CAPTURE");
                ((Activity) getContext()).startActivityForResult(i, SharedConstants.IMAGE_CAPTURE);
            }
        });

        // for showing the image
        mImageAnswer = new ImageView(getContext());
        mImageAnswer.setPadding(0, 10, 0, 0);
        if (mPrompt.getAnswerObject() != null) {
            // always use the image from the imageAnswer array
            InputStream is = ((BasicDataPointer) mPrompt.getAnswerObject()).getDataStream();
            setImageAnswer(BitmapFactory.decodeStream(is));
        }

        // finish complex layout
        ll.addView(mActionButton);
        ll.addView(mImageAnswer);
        mView.addView(ll);
    }


    /**
     * Store integer answer as {@link IntegerData}.
     */
    private void IntegerAnswer() {
        String s = GenericAnswer();
        if (s != null) {
            try {
                mAnswer = new IntegerData(Integer.parseInt(s));
            } catch (Exception NumberFormatException) {
                mAnswer = null;
            }
        }
    }


    /**
     * Build view for integer answer. Includes retrieving existing answer.
     */
    private void IntegerView() {
        GenericView(mReadOnly);

        // only allows numbers and no periods
        mStringAnswer.setKeyListener(new DigitsKeyListener(true, false));

        // ints can only hold 2,147,483,648. we allow 999,999,999
        InputFilter[] fa = new InputFilter[1];
        fa[0] = new InputFilter.LengthFilter(9);
        mStringAnswer.setFilters(fa);

        // restrict the size of the widget
        LayoutParams lp = new LayoutParams(110, LayoutParams.FILL_PARENT);
        mView.addView(mStringAnswer, lp);
    }


    /**
     * Build view for question text.
     */
    private void QuestionTextView() {
        TextView tv = new TextView(getContext());
        tv.setText(mPrompt.getQuestionText());
        tv.setTextColor(Color.WHITE);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_PT, (float) 10);
        tv.setPadding(0, 0, 0, 5);

        // wrap to the widget of view
        tv.setHorizontallyScrolling(false);
        this.addView(tv);
    }


    /**
     * Store select multiple answer as {@link SelectMultiData}.
     */
    private void SelectMultiAnswer() {

        Vector v = new Vector();

        for (int i = 0; i < mPrompt.getSelectItems().size(); i++) {
            // no checkbox group so find by id + offset
            CheckBox c = ((CheckBox) findViewById(CHECKBOX_ID + i));
            c.setPadding(0, 0, 0, 5);
            if (c.isChecked()) {
                // use {@link Selection} for selections
                v.add(new Selection(i, mPrompt.getQuestionDef()));
            }
        }

        if (v.size() == 0) {
            mAnswer = null;
        } else {
            mAnswer = new SelectMultiData(v);
        }
    }


    /**
     * Set all checkboxes to unchecked.
     */
    private void SelectMultiReset() {
        for (int i = 0; i < mPrompt.getSelectItems().size(); i++) {

            // no checkbox group so find by id + offset
            CheckBox c = ((CheckBox) findViewById(CHECKBOX_ID + i));
            if (c.isChecked()) {
                c.setChecked(false);
            }
        }
    }


    /**
     * Build view for multi select answer. Includes retrieving existing answer.
     */
    private void SelectMultiView() {

        // view is too complex for scrollview
        LinearLayout ll = new LinearLayout(getContext());
        ll.setOrientation(LinearLayout.VERTICAL);

        Vector ve = new Vector();
        if (mPrompt.getAnswerValue() != null) {
            ve = (Vector) mPrompt.getAnswerObject();
        }

        if (mPrompt.getSelectItems() != null) {
            OrderedHashtable h = mPrompt.getSelectItems();
            Enumeration en = h.keys();
            String k = null;
            String v = null;

            // counter for offset
            int i = 0;

            while (en.hasMoreElements()) {

                k = (String) en.nextElement();
                v = (String) h.get(k);

                // no checkbox group so id by answer + offset
                CheckBox c = new CheckBox(getContext());

                // when clicked, check for readonly before toggling`
                c.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (!mCheckboxInit && mReadOnly) {
                            if (buttonView.isChecked()) {
                                buttonView.setChecked(false);
                            } else {
                                buttonView.setChecked(true);
                            }
                        }
                    }
                });

                c.setId(CHECKBOX_ID + i);
                c.setText(k);
                c.setTextSize(TypedValue.COMPLEX_UNIT_PT, (float) 10);

                for (int vi = 0; vi < ve.size(); vi++) {
                    // match based on value, not key
                    if (v.equals(((Selection) ve.elementAt(vi)).getValue())) {
                        c.setChecked(true);
                        break;
                    }
                }

                ll.addView(c);
                i++;
            }
        }

        mCheckboxInit = false;
        mView.addView(ll);
    }



    /**
     * Store select one answer as {@link SelectOneData}.
     */
    private void SelectOneAnswer() {
        int i = mRadioAnswer.getCheckedRadioButtonId();
        if (i == -1) {
            mAnswer = null;
        } else {
            // javarosa indexes start at 0, but android radio ids start at 1.
            i--;
            mAnswer = new SelectOneData(new Selection(i, mPrompt.getQuestionDef()));
        }
    }


    /**
     * Set all radio buttons to unselected.
     */
    private void SelectOneReset() {
        mRadioAnswer.clearCheck();
    }


    /**
     * Build view for select one answer. Includes retrieving existing answer.
     */
    private void SelectOneView() {

        mRadioAnswer = new RadioGroup(getContext());
        mRadioAnswer.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (mRadioSelected != -1 && mReadOnly) {
                    mRadioAnswer.check(mRadioSelected);
                } else {
                    mRadioAnswer.check(checkedId);
                }
            }
        });

        String s = null;
        if (mPrompt.getAnswerValue() != null) {
            s = mPrompt.getAnswerText();
        }

        if (mPrompt.getSelectItems() != null) {
            OrderedHashtable h = mPrompt.getSelectItems();
            Enumeration e = h.keys();
            String k = null;
            String v = null;

            // android radio ids start at 1, not 0
            int i = 1;
            while (e.hasMoreElements()) {
                k = (String) e.nextElement();
                v = (String) h.get(k);

                RadioButton r = new RadioButton(getContext());
                r.setText(k);
                r.setTextSize(TypedValue.COMPLEX_UNIT_PT, (float) 10);
                r.setId(i);
                mRadioAnswer.addView(r);

                if (k.equals(s)) {
                    r.setChecked(true);
                    mRadioSelected = i;
                }

                i++;
            }
            mView.addView(mRadioAnswer);
        }
    }


    /**
     * Store image as bitmap in {@link ImageView} and ByteArray.
     * 
     * @param mBitmap bitmap from camera
     */
    public void setImageAnswer(Bitmap mBitmap) {
        mImageAnswer.setImageBitmap(mBitmap);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        mBitmap.compress(CompressFormat.PNG, 100, bos); // lossless
        mImageData = bos.toByteArray();
    }


    /**
     * Create location manager and listener.
     */
    private void startGPS() {
        mLocationManager =
                (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
        if (mLocationManager != null) {
            mLocationListener = new LocationListener() {

                // if location has changed, update location
                public void onLocationChanged(Location location) {
                    mLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                }


                // close gps dialogs, alert user, stop gps
                public void onProviderDisabled(String provider) {
                    if (mLocationDialog != null && mLocationDialog.isShowing()) {
                        mLocationDialog.dismiss();
                        Toast.makeText(getContext(),
                                getContext().getString(R.string.gps_disabled_error),
                                Toast.LENGTH_SHORT).show();
                    }
                    stopGPS();
                }


                public void onProviderEnabled(String provider) {
                }


                // check for only valid gps lock
                public void onStatusChanged(String provider, int status, Bundle b) {
                    switch (status) {
                        case LocationProvider.AVAILABLE:
                            // update location
                            mLocation =
                                    mLocationManager
                                            .getLastKnownLocation(LocationManager.GPS_PROVIDER);

                            // close gps dialogs, alert user, stop gps
                            if (mLocationDialog != null && mLocationDialog.isShowing()) {
                                mLocationDialog.dismiss();
                                mStringAnswer.setText("lat: " + mLocation.getLatitude() + "\nlon: "
                                        + mLocation.getLongitude());
                                stopGPS();
                            }
                    }

                }
            };
        }

        // start listening for changes
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3, 3,
                mLocationListener);
    }


    /**
     * Stop listening to any updates from GPS
     */
    private void stopGPS() {
        if (mLocationManager != null) {
            mLocationManager.removeUpdates(mLocationListener);

        }
    }


    /**
     * Store select text answer as {@link StringData}.
     */
    private void StringAnswer() {
        String s = GenericAnswer();
        if (s != null) {
            mAnswer = new StringData(s);
        }
    }


    /**
     * Build view for string answer. Includes retrieving existing answer.
     */
    private void StringView() {
        GenericView(mReadOnly);
        mView.addView(mStringAnswer);
    }


    /**
     * Build view for generic answer. Includes retrieving existing answer.
     */
    private void GenericView(boolean readonly) {
        if (readonly) {
            mStringAnswer = new TextView(getContext());
        } else {
            mStringAnswer = new EditText(getContext());
        }
        String s = mPrompt.getAnswerText();
        if (s != null) {
            mStringAnswer.setText(s);
        }
        mStringAnswer.setTextSize(TypedValue.COMPLEX_UNIT_PT, (float) 10);
    }


    /**
     * Check string for answer. If empty, return false.
     */
    private String GenericAnswer() {
        String s = mStringAnswer.getText().toString();
        if (s == null || s.equals("")) {
            mAnswer = null;
            return null;
        }
        return s;
    }


    /**
     * Set answer to null.
     */
    private void StringReset() {
        mStringAnswer.setText(null);
    }



}
