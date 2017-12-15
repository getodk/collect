package org.odk.collect.android.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.heinrichreimersoftware.materialintro.app.IntroActivity;
import com.heinrichreimersoftware.materialintro.slide.FragmentSlide;
import com.heinrichreimersoftware.materialintro.slide.SimpleSlide;
import com.heinrichreimersoftware.materialintro.slide.Slide;

import org.odk.collect.android.R;
import org.odk.collect.android.introfragments.FillBlankForm;
import org.odk.collect.android.introfragments.Welcome;

/**
 * Created on 18/11/17.
 */

public class CollectIntroActivity  extends IntroActivity {


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Slide welcomeSlide;
        welcomeSlide = new FragmentSlide.Builder()
                .background(R.color.grey)
                .fragment(Welcome.newInstance())
                .build();

        addSlide(welcomeSlide);

        addSlide(new SimpleSlide.Builder()
                .title("Step 1")
                .description("Setup server configuration\nGo to Settings->General settings->Server")
                .image(R.drawable.intro_1)
                .background(R.color.red)
                .scrollable(false)
                .build());

        addSlide(new SimpleSlide.Builder()
                .title("Step 2\nDownload Forms")
                .description("Select the forms and store on phone.")
                .image(R.drawable.intro_2)
                .background(R.color.tintColor)
                .scrollable(false)
                .build());


        final Slide fillBlankFormSlide;
        fillBlankFormSlide = new FragmentSlide.Builder()
                .background(R.color.grey)
                .fragment(FillBlankForm.newInstance())
                .background(R.color.light_green)
                .build();

        addSlide(fillBlankFormSlide);

        addSlide(new SimpleSlide.Builder()
                .title("Step 4")
                .description("Send Forms")
                .image(R.drawable.intro_4)
                .background(R.color.chrome)
                .scrollable(false)
                .build());


        addSlide(new SimpleSlide.Builder()
                .title("Participate")
                .description("Slack : https://opendatakit.slack.com/messages\nForum : https://forum.opendatakit.org/\nGithub : https://github.com/opendatakit/collect")
                .background(R.color.maroon)
                .scrollable(false)
                .build());


    }


}
