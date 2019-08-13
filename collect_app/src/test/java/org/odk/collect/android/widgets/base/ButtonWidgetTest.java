package org.odk.collect.android.widgets.base;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import com.karumi.dexter.DexterActivity;

import org.javarosa.core.model.data.IAnswerData;
import org.odk.collect.android.fakes.FakePermissionUtils;
import org.odk.collect.android.widgets.QuestionWidget;
import org.odk.collect.android.widgets.interfaces.ButtonWidget;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Shadows.shadowOf;


/**
 * @author Shobhit Agarwal
 */
public abstract class ButtonWidgetTest<W extends ButtonWidget, A extends IAnswerData>
        extends QuestionWidgetTest<W, A> {

    private final FakePermissionUtils permissionUtils;

    ButtonWidgetTest() {
        permissionUtils = new FakePermissionUtils();
    }

    protected void stubAllRuntimePermissionsGranted(boolean isGranted) {
        permissionUtils.setPermissionGranted(isGranted);
        ((QuestionWidget) getActualWidget()).setPermissionUtils(permissionUtils);
    }

    protected Intent getIntentLaunchedByClick(int buttonId) {
        ((QuestionWidget) getWidget()).findViewById(buttonId).performClick();
        return shadowOf(activity).getNextStartedActivity();
    }

    protected void assertComponentEquals(String pkg, String cls, Intent intent) {
        assertEquals(new ComponentName(pkg, cls), intent.getComponent());
    }

    protected void assertComponentEquals(Context context, Class<?> cls, Intent intent) {
        assertEquals(new ComponentName(context, cls), intent.getComponent());
    }

    protected void assertActionEquals(String expectedAction, Intent intent) {
        assertEquals(expectedAction, intent.getAction());
    }

    protected void assertTypeEquals(String type, Intent intent) {
        assertEquals(type, intent.getType());
    }

    protected void assertExtraEquals(String key, Object value, Intent intent) {
        assertEquals(intent.getExtras().get(key), value);
    }

    // After upgrading gradle and some dependencies if an intent can't be started because of not granted
    // permissions the null value or DexterActivity is returned. It works randomly and depends on the
    // order of tests but both results are ok.
    protected void assertIntentNotStarted(Context context, Intent intent) {
        assertTrue(intent == null || new ComponentName(context, DexterActivity.class).equals(intent.getComponent()));
    }
}
