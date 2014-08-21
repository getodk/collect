
package org.odk.collect.android.activities;

import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.widget.TextView;

public class NotificationActivity extends Activity {

    public final static String NOTIFICATION_KEY = "message";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notification_layout);

        String note = this.getIntent().getStringExtra(NOTIFICATION_KEY);
        if (note == null) {
            note = getString(R.string.notification_error);
        }

        TextView notificationText = (TextView)findViewById(R.id.notification);
        notificationText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, Collect.getQuestionFontsize());
        notificationText.setTypeface(null, Typeface.BOLD);
        notificationText.setPadding(0, 0, 0, 7);
        notificationText.setText(note);
    }

}
