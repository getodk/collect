package org.odk.collect.android.widgets;

import android.support.annotation.NonNull;

import org.javarosa.core.model.data.DecimalData;
import org.odk.collect.android.widgets.base.GeneralExStringWidgetTest;
import org.robolectric.RuntimeEnvironment;

import java.text.NumberFormat;
import java.util.Locale;

import static org.mockito.Mockito.when;

/**
 * @author James Knight
 */

public class ExDecimalWidgetTest extends GeneralExStringWidgetTest<ExDecimalWidget, DecimalData> {

    @NonNull
    @Override
    public ExDecimalWidget createWidget() {
        return new ExDecimalWidget(RuntimeEnvironment.application, formEntryPrompt);
    }

    @NonNull
    @Override
    public DecimalData getNextAnswer() {
        // Need to keep under 15 digits:
        double d = random.nextDouble();
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
        nf.setMaximumFractionDigits(13); // The Widget internally truncatest this further.
        nf.setMaximumIntegerDigits(13);
        nf.setGroupingUsed(false);

        String formattedValue = nf.format(d);
        return new DecimalData(Double.parseDouble(formattedValue));
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        when(formEntryPrompt.getAppearanceHint()).thenReturn("");
    }
}