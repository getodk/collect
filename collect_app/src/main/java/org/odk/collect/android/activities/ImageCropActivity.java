/*
 * Copyright 2018 Yizheng Huang
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.odk.collect.android.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import org.odk.collect.android.R;
import org.odk.collect.android.views.CropImageView;

import timber.log.Timber;

public class ImageCropActivity extends AppCompatActivity implements View.OnClickListener {

    private CropImageView cropImageView;
    private Bitmap bitmapBefore, bitmapCroped;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_crop);
        initViews();
    }

    private void initViews() {
        cropImageView = findViewById(R.id.cropview);
        cropImageView.setVisibility(View.GONE);
        Button btnCrop = findViewById(R.id.btn_crop);
        Button btnRedo = findViewById(R.id.btn_redo);
        Button btnSave = findViewById(R.id.btn_save);

        btnSave.setOnClickListener(this);
        btnRedo.setOnClickListener(this);
        btnCrop.setOnClickListener(this);

        Intent intent = getIntent();
        String imageUrl = intent.getStringExtra("crop_path");
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        bitmapBefore = BitmapFactory.decodeFile(imageUrl, options);
        cropImageView.setDrawable(bitmapBefore, 200, 200);
        cropImageView.setVisibility(View.VISIBLE);
    }


    @Override
    public void onClick(View v) {
        cropImageView.setVisibility(View.GONE);
        switch (v.getId()) {
            case R.id.btn_crop:
                cropImageView.setDrawable(cropImageView.getCropImage(), 200, 200);
                cropImageView.setVisibility(View.VISIBLE);
                break;
            case R.id.btn_redo:
                cropImageView.setDrawable(bitmapBefore, 200, 200);
                cropImageView.setVisibility(View.VISIBLE);
                break;
            case R.id.btn_save:
                break;
            default:
                break;
        }
    }
}
