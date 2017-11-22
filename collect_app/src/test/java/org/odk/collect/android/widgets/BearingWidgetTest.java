package org.odk.collect.android.widgets;

import android.support.annotation.NonNull;

import net.bytebuddy.utility.RandomString;

import org.javarosa.core.model.data.StringData;
import org.junit.Before;
import org.odk.collect.android.widgets.base.BinaryWidgetTest;
import org.robolectric.RuntimeEnvironment;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Random;

/**
 * @author James Knight
 */
public class BearingWidgetTest extends BinaryWidgetTest<BearingWidget, StringData> {

    private String bearingData;

    @NonNull
    @Override
    public BearingWidget createWidget() {
        return new BearingWidget(RuntimeEnvironment.application, formEntryPrompt);
    }

    @Override
    public Object createBinaryData(StringData answerData) {
        return bearingData;
    }

    @NonNull
    @Override
    public StringData getNextAnswer() {
        return new StringData(bearingData);
    }

    @Override
    public StringData getInitialAnswer() {
        double d = random.nextDouble();
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
        nf.setMaximumFractionDigits(3); // The Widget internally truncatest this further.
        nf.setMaximumIntegerDigits(3);
        nf.setGroupingUsed(false);

        String formattedValue = nf.format(d);
        return new StringData(formattedValue);
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        bearingData = RandomString.make();
    }
}
