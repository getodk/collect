package org.odk.collect.android.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

public class DecoratedBarcodeView extends com.journeyapps.barcodescanner.DecoratedBarcodeView {

    public static boolean test;

    public DecoratedBarcodeView(Context context) {
        super(context);
        initialize();
    }

    public DecoratedBarcodeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public DecoratedBarcodeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
    }

    private void initialize() {
        if (test) {
            setVisibility(View.INVISIBLE);
        }
    }
}
