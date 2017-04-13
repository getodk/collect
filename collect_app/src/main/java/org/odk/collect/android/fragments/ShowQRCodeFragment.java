/*
 * Copyright (C) 2017 Shobhit
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.odk.collect.android.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ShareActionProvider;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;
import org.odk.collect.android.R;
import org.odk.collect.android.listeners.QRCodeListener;
import org.odk.collect.android.tasks.GenerateQRCode;
import org.odk.collect.android.utilities.CompressionUtils;
import org.odk.collect.android.utilities.ImportSettings;
import org.odk.collect.android.utilities.QRCodeUtils;
import org.odk.collect.android.utilities.ToastUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.DataFormatException;

import timber.log.Timber;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;


/**
 * Created by shobhit on 6/4/17.
 */

public class ShowQRCodeFragment extends Fragment implements View.OnClickListener, QRCodeListener {

    private static final int SELECT_PHOTO = 111;
    private ShareActionProvider mShareActionProvider;
    private ImageView qrImageView;
    private ProgressBar progressBar;
    private Intent mShareIntent;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.show_qrcode_fragment, container, false);
        setHasOptionsMenu(true);
        qrImageView = (ImageView) view.findViewById(R.id.qr_iv);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        Button scan = (Button) view.findViewById(R.id.btnScan);
        scan.setOnClickListener(this);
        Button select = (Button) view.findViewById(R.id.btnSelect);
        select.setOnClickListener(this);
        generateCode();
        return view;
    }

    public void generateCode() {
        new GenerateQRCode(this, getActivity(), qrImageView).execute();
    }

    private void updateShareIntent(Bitmap qrCode) throws IOException {

        //Save the bitmap to a file
        File cache = getActivity().getApplicationContext().getExternalCacheDir();
        File shareFile = new File(cache, "shareImage.jpeg");
        FileOutputStream out = new FileOutputStream(shareFile);
        qrCode.compress(Bitmap.CompressFormat.JPEG, 100, out);
        out.close();

        // Sent a intent to share saved image
        mShareIntent = new Intent();
        mShareIntent.setAction(Intent.ACTION_SEND);
        mShareIntent.setType("image/*");
        mShareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + shareFile));
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnScan:
                IntentIntegrator integrator = IntentIntegrator.forFragment(this);
                integrator.setBeepEnabled(true);
                integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
                integrator.initiateScan();
                break;

            case R.id.btnSelect:
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, SELECT_PHOTO);
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if (result != null) {
            if (result.getContents() == null) {
                // request was canceled...
                ToastUtils.showShortToast("Scanning Cancelled");
            } else {
                applySettings(result.getContents());
                return;
            }
        }

        if (requestCode == SELECT_PHOTO) {
            if (resultCode == Activity.RESULT_OK) {
                try {
                    final Uri imageUri = data.getData();
                    final InputStream imageStream = getActivity().getContentResolver()
                            .openInputStream(imageUri);

                    final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                    String response = QRCodeUtils.decodeFromBitmap(selectedImage);
                    if (response != null) {
                        applySettings(response);
                    }
                } catch (FileNotFoundException e) {
                    Timber.e(e);
                }
            } else {
                ToastUtils.showShortToast("Cancelled");
            }
        }
    }


    private void applySettings(String content) {
        String decompressedData;
        try {
            decompressedData = CompressionUtils.decompress(content);
            JSONObject jsonObject = new JSONObject(decompressedData);
            ImportSettings.fromJSON(jsonObject);
        } catch (DataFormatException e) {
            Timber.e(e);
            ToastUtils.showShortToast("QR Code does not contains valid settings");
            return;
        } catch (IOException | JSONException e) {
            Timber.e(e);
            return;
        }

        // update the QR Code
        generateCode();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.share_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_item_share) {
            getActivity().startActivity(Intent.createChooser(mShareIntent, "Share QR Code"));
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void preExecute() {
        progressBar.setVisibility(VISIBLE);
        qrImageView.setVisibility(GONE);
    }

    @Override
    public void bitmapGenerated(Bitmap bitmap) {
        progressBar.setVisibility(GONE);
        qrImageView.setVisibility(VISIBLE);

        if (bitmap != null) {
            qrImageView.setImageBitmap(bitmap);
            try {
                updateShareIntent(bitmap);
            } catch (IOException e) {
                Timber.e(e);
            }
        }
    }
}
