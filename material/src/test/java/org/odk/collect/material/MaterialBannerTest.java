package org.odk.collect.material;

import android.content.Context;
import android.widget.Button;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static android.view.View.inflate;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.robolectric.shadows.ShadowView.innerText;

@RunWith(RobolectricTestRunner.class)
public class MaterialBannerTest {

    private MaterialBanner banner;
    private Context context;

    @Before
    public void setup() {
        context = ApplicationProvider.getApplicationContext();
        context.setTheme(R.style.Theme_MaterialComponents); // Needed for material theme attributes

        banner = new MaterialBanner(context);
    }

    @Test
    public void hasCustomAttrs() {
        MaterialBanner banner = (MaterialBanner) inflate(context, R.layout.material_banner_attr_test, null);

        assertThat(innerText(banner.findViewById(R.id.text)), is("text attribute value"));

        Button button = banner.findViewById(R.id.button);
        assertThat(button.getText(), is("actionText attribute value"));

        int textColor = button.getCurrentTextColor();
        assertThat(textColor, is(ContextUtils.getAttributeValue(context, R.attr.colorSecondary)));
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