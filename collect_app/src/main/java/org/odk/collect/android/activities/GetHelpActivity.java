package org.odk.collect.android.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import org.odk.collect.android.R;

public class GetHelpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_help);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setTitle(getString(R.string.get_help));
        setSupportActionBar(toolbar);

        String htmlAsString = getString(R.string.get_help_text);
        Spanned htmlAsSpanned = Html.fromHtml(htmlAsString);

        TextView textView = (TextView) findViewById(R.id.get_help_text);
        textView.setText(htmlAsSpanned);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
    }
}
