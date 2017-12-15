package org.odk.collect.android.introfragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.heinrichreimersoftware.materialintro.app.SlideFragment;

import org.odk.collect.android.R;

/**
 * Created on 15/12/17.
 */

public class FillBlankForm extends SlideFragment {

    public FillBlankForm() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.intro_layout_fill_blank_form,container,false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    public static FillBlankForm newInstance() {
        return new FillBlankForm();
    }

}
