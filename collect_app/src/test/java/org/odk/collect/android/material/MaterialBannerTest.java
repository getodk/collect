package org.odk.collect.android.material;

import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.utilities.ThemeUtils;
import org.robolectric.RobolectricTestRunner;

import static android.view.View.inflate;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.odk.collect.android.support.RobolectricHelpers.buildThemedActivity;
import static org.robolectric.shadows.ShadowView.innerText;

@RunWith(RobolectricTestRunner.class)
public class MaterialBannerTest {

    private MaterialBanner banner;
    private AppCompatActivity activity;

    @Before
    public void setup() {
        activity = buildThemedActivity(AppCompatActivity.class).get();
        banner = new MaterialBanner(activity);
    }

    @Test
    public void hasCustomAttrs() {
        MaterialBanner banner = (MaterialBanner) inflate(activity, R.layout.material_banner_attr_test, null);

        assertThat(innerText(banner.findViewById(R.id.text)), is("text attribute value"));

        Button button = banner.findViewById(R.id.button);
        assertThat(button.getText(), is("actionText attribute value"));

        int textColor = button.getCurrentTextColor();
        assertThat(textColor, is(new ThemeUtils(activity).getColorSecondary()));
    }

    @Test
    public void setText_displaysText() {
        banner.setText("I am text");
        assertThat(innerText(banner.findViewById(R.id.text)), is("I am text"));
    }

    @Test
    public void setActionText_setsButtonText() {
        banner.setActionText("DO IT");

        Button button = banner.findViewById(R.id.button);
        assertThat(button.getText(), is("DO IT"));
    }

    @Test
    public void setAction_setsButtonClickListener() {
        MaterialBanner.OnActionListener listener = mock(MaterialBanner.OnActionListener.class);
        banner.setAction(listener);

        Button button = banner.findViewById(R.id.button);
        button.performClick();
        verify(listener).onAction();
    }
}