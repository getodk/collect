package org.odk.collect.android.utilities;

import android.net.Uri;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
public class CustomTabHelperTest {

    @Test
    public void uriShouldBeNormalized() {
        CustomTabHelper customTabHelper = spy(new CustomTabHelper());
        doNothing().when(customTabHelper).openUriInChromeTabs(any(), any());
        doNothing().when(customTabHelper).openUriInWebView(any(), any());
        doNothing().when(customTabHelper).openUriInExternalBrowser(any(), any());

        Uri uri = mock(Uri.class);
        customTabHelper.openUri(ApplicationProvider.getApplicationContext(), uri);
        verify(uri).normalizeScheme();
    }
}