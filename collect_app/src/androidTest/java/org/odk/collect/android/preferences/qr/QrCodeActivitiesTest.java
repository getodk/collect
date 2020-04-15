package org.odk.collect.android.preferences.qr;

import android.os.Bundle;

import androidx.fragment.app.FragmentFactory;
import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;


@RunWith(AndroidJUnit4.class)
public class QrCodeActivitiesTest {
    @Test
    public void test() {
        FragmentScenario<QRScannerFragment> fs =
                FragmentScenario.launchInContainer(QRScannerFragment.class,
                        new Bundle(),
                        R.style.Theme_MaterialComponents,
                        new FragmentFactory());

        fs.onFragment(new QrCodeFragmentAction());

    }


}
