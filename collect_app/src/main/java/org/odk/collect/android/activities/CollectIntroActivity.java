package org.odk.collect.android.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.heinrichreimersoftware.materialintro.app.IntroActivity;
import com.heinrichreimersoftware.materialintro.slide.FragmentSlide;
import com.heinrichreimersoftware.materialintro.slide.SimpleSlide;

import org.odk.collect.android.R;

/**
 * Created by ash on 18/11/17.
 */

public class CollectIntroActivity  extends IntroActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addSlide(new SimpleSlide.Builder()
                .title(R.string.app_name)
                .description("Welcome to ODK Collect.\nData collection made easier")
                .background(R.color.grey)
                .scrollable(false)
                .build());

        addSlide(new SimpleSlide.Builder()
                .title("Step intro_1.")
                .description("Setup server configuration\nGo to Settings->General settings->Server")
                .image(R.drawable.intro_1)
                .background(R.color.red)
                .scrollable(false)
                .build());

        addSlide(new SimpleSlide.Builder()
                .title("Step 2.\nDownload Forms")
                .description("Select the forms and store on phone.")
                .image(R.drawable.intro_2)
                .background(R.color.tintColor)
                .scrollable(false)
                .build());


        addSlide(new SimpleSlide.Builder()
                .title("Step 3.")
                .description("Fill forms")
                .image(R.drawable.intro_3)
                .background(R.color.light_green)
                .scrollable(false)
                .build());


        addSlide(new SimpleSlide.Builder()
                .title("Step 4.")
                .description("Send Forms")
                .image(R.drawable.intro_4)
                .background(R.color.chrome)
                .scrollable(false)
                .build());


        addSlide(new SimpleSlide.Builder()
                .title("Participate")
                .description("Slack : https://opendatakit.slack.com/messages\nForum : https://forum.opendatakit.org/\nGithub : https://github.com/opendatakit/collect")
                .background(R.color.yellow)
                .scrollable(false)
                .build());

        addSlide(new FragmentSlide.Builder()
                .background(R.color.yellow)
                .fragment(R.layout.intro_layout_1,R.style.AppThemeBase)
                .build());
    }


}
