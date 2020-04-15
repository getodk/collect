package org.odk.collect.android.preferences.qr;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.testing.FragmentScenario;
import static org.junit.Assert.*;

public class ShowQrFragmentAction implements FragmentScenario.FragmentAction<ShowQRCodeFragment> {

    @Override
    public void perform(@NonNull ShowQRCodeFragment fragment) {
        assertNotNull(fragment.ivQRCode.getDrawable());
        assertEquals(fragment.ivQRCode.getVisibility(), View.VISIBLE);
    }
}