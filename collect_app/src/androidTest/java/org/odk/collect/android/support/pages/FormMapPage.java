package org.odk.collect.android.support.pages;

import org.odk.collect.android.support.FakeClickableMapFragment;

public class FormMapPage extends Page<FormMapPage> {

    private final String formName;

    public FormMapPage(String formName) {
        this.formName = formName;
    }

    @Override
    public FormMapPage assertOnPage() {
        assertText(formName);
        checkIsIdDisplayed(org.odk.collect.geo.R.id.geometry_status);
        return this;
    }

    public FormEntryPage clickFillBlankFormButton(String formName) {
        clickOnId(org.odk.collect.geo.R.id.new_item);
        return new FormEntryPage(formName).assertOnPage();
    }

    public FormMapPage selectForm(FakeClickableMapFragment mapFragment, int index) {
        mapFragment.clickOnFeature(index);
        return this;
    }

    public FormHierarchyPage clickEditSavedForm(String formName) {
        clickOnString(org.odk.collect.strings.R.string.edit_data);
        return new FormHierarchyPage(formName).assertOnPage();
    }
}
