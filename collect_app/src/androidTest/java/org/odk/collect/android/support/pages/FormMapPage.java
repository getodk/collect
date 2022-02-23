package org.odk.collect.android.support.pages;

import org.odk.collect.android.R;
import org.odk.collect.android.support.FakeClickableMapFragment;

public class FormMapPage extends Page<FormMapPage> {

    private final String formName;

    public FormMapPage(String formName) {
        this.formName = formName;
    }

    @Override
    public FormMapPage assertOnPage() {
        assertText(formName);
        checkIsIdDisplayed(R.id.geometry_status);
        return this;
    }

    public FormEntryPage clickFillBlankFormButton(String formName) {
        clickOnId(R.id.new_instance);
        return new FormEntryPage(formName).assertOnPage();
    }

    public FormMapPage selectForm(FakeClickableMapFragment mapFragment, int index) {
        mapFragment.clickOnFeature(index);

        waitForText(getTranslatedString(R.string.review_data)); // Wait for animation to end
        return this;
    }

    public FormHierarchyPage clickEditSavedForm(String formName) {
        clickOnString(R.string.review_data);
        return new FormHierarchyPage(formName).assertOnPage();
    }
}
