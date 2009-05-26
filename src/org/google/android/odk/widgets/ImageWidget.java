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

package org.google.android.odk.widgets;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;

import org.google.android.odk.PromptElement;
import org.google.android.odk.R;
import org.google.android.odk.SharedConstants;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.PointerAnswerData;
import org.javarosa.core.model.data.helper.BasicDataPointer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;


/**
 * Widget that allows user to take pictures and add them to the form.
 * 
 * @author Carl Hartung
 * @author Yaw Anokwa
 */
public class ImageWidget extends LinearLayout implements IQuestionWidget, IBinaryWidget {

    private Button mActionButton;
    private ImageView mImageView;
    private byte[] mImageData;


    public ImageWidget(Context context) {
        super(context);
    }


    public void clearAnswer() {
        mImageData = null;
        mImageView.setVisibility(GONE);
    }


    public IAnswerData getAnswer() {
        long ts = Calendar.getInstance().getTimeInMillis();
        BasicDataPointer bdp = new BasicDataPointer("image_" + ts, mImageData);
        if (mImageData != null) {
            return new PointerAnswerData(bdp);
        } else {
            return null;
        }
    }


    public void buildView(PromptElement prompt) {
        this.setOrientation(LinearLayout.VERTICAL);

        mActionButton = new Button(getContext());
        mActionButton.setText(getContext().getString(R.string.get_image));
        mActionButton.setTextSize(TypedValue.COMPLEX_UNIT_PT, SharedConstants.TEXTSIZE);
        mActionButton.setPadding(20, 20, 20, 20);
        mActionButton.setEnabled(!prompt.isReadonly());

        // launch image capture intent on click
        mActionButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent("android.media.action.IMAGE_CAPTURE");
                Uri u = Uri.fromFile(new File(SharedConstants.TMPFILE_PATH));
                i.putExtra("output", u);
                ((Activity) getContext()).startActivityForResult(i, SharedConstants.IMAGE_CAPTURE);
            }
        });

        // for showing the image
        mImageView = new android.widget.ImageView(getContext());
        mImageView.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
                LayoutParams.FILL_PARENT));

        mImageView.setPadding(0, 10, 0, 0);
        mImageView.setScaleType(android.widget.ImageView.ScaleType.CENTER_CROP);
        if (prompt.getAnswerObject() != null) {
            // always use the image from the imageAnswer array
            mImageData = ((BasicDataPointer) prompt.getAnswerObject()).getData();
            mImageView.setImageBitmap(BitmapFactory.decodeByteArray(mImageData, 0,
                    mImageData.length));
        }

        // finish complex layout
        this.addView(mActionButton);
        this.addView(mImageView);
    }


    public void setBinaryData(Object answer) {
        Bitmap bitmap = (Bitmap) answer;
        mImageView.setImageBitmap(bitmap);
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(CompressFormat.JPEG, 100, bos);
            mImageData = bos.toByteArray();
            bos.close();
        } catch (IOException e) {
            // TODO (yanokwa): Auto-generated catch block
            e.printStackTrace();
        }
    }
}
