/*
 * Copyright 2019 Nafundi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.odk.collect.android.formentry;

import android.content.Context;

import org.odk.collect.android.R;
import org.odk.collect.android.formentry.saving.SaveFormProgressDialogFragment;
import org.odk.collect.material.MaterialProgressDialogFragment;

public class FormLoadingDialogFragment extends MaterialProgressDialogFragment {

    public interface FormLoadingDialogFragmentListener {
        void onCancelFormLoading();
    }

    /**
     * Using a listener like this requires an Activity to implement the interface. We could
     * use a similar approach as that used in {@link SaveFormProgressDialogFragment} and grab
     * a ViewModel to cancel.
     */
    @Deprecated
    private FormLoadingDialogFragmentListener listener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        setTitle(getString(R.string.loading_form));
        setMessage(getString(R.string.please_wait));
        setCancelable(false);

        if (context instanceof FormLoadingDialogFragmentListener) {
            listener = (FormLoadingDialogFragmentListener) context;
        }
    }

    @Override
    protected String getCancelButtonText() {
        return getString(R.string.cancel_loading_form);
    }

    @Override
    protected OnCancelCallback getOnCancelCallback() {
        return () -> {
            listener.onCancelFormLoading();
            return true;
        };
    }
}
