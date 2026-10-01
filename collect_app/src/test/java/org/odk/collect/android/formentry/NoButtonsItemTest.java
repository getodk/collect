package org.odk.collect.android.formentry;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;

import android.view.View;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.formentry.questions.NoButtonsItem;
import org.odk.collect.imageloader.ImageLoader;
import org.odk.collect.shared.TempFiles;

@RunWith(AndroidJUnit4.class)
public class NoButtonsItemTest {
    @Test
    public void settingUpAChoiceWithoutAnImage_hidesTheImageOfThePreviousChoice() {
        NoButtonsItem item = new NoButtonsItem(ApplicationProvider.getApplicationContext(), true, mock(ImageLoader.class));
        item.setUpNoButtonsItem(TempFiles.createTempFile(".jpg"), "AAA", "", false);

        item.setUpNoButtonsItem(null, "BBB", "", false);

        assertThat(item.findViewById(R.id.imageView).getVisibility(), is(View.GONE));
    }

    @Test
    public void settingUpAChoiceWithAnImage_hidesTheTextOfThePreviousChoice() {
        NoButtonsItem item = new NoButtonsItem(ApplicationProvider.getApplicationContext(), true, mock(ImageLoader.class));
        item.setUpNoButtonsItem(null, "AAA", "", false);

        item.setUpNoButtonsItem(TempFiles.createTempFile(".jpg"), "BBB", "", false);

        assertThat(item.findViewById(R.id.label).getVisibility(), is(View.GONE));
    }
}
