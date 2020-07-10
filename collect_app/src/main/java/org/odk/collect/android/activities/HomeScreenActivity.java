package org.odk.collect.android.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import org.odk.collect.android.R;


public class HomeScreenActivity extends CollectAbstractActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_screen);

    }





    public void SurveysIn(View view) {
        startActivity(new Intent(this, MainMenuActivity.class));

    }
}


