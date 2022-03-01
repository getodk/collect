package org.odk.collect.android.support.pages;

import androidx.annotation.NonNull;
import androidx.test.espresso.contrib.RecyclerViewActions;

import org.odk.collect.android.R;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

public class FormHierarchyPage extends Page<FormHierarchyPage> {

    private final String formName;

    public FormHierarchyPage(String formName) {
        this.formName = formName;
    }

    @NonNull
    @Override
    public FormHierarchyPage assertOnPage() {
        assertToolbarTitle(formName);
        assertText(R.string.jump_to_beginning);
        assertText(R.string.jump_to_end);
        return this;
    }

    public FormHierarchyPage clickGoUpIcon() {
        onView(withId(R.id.menu_go_up)).perform(click());
        return this;
    }

    public FormEntryPage clickGoToStart() {
        onView(withId(R.id.jumpBeginningButton)).perform(click());
        return new FormEntryPage(formName).assertOnPage();
    }

    public FormEntryPage addGroup() {
        onView(withId(R.id.menu_add_repeat)).perform(click());
        return new FormEntryPage(formName).assertOnPage();
    }

    public FormHierarchyPage deleteGroup() {
        onView(withId(R.id.menu_delete_child)).perform(click());
        return clickOnButtonInDialog(R.string.delete_repeat, this);
    }

    public FormEndPage clickJumpEndButton() {
        onView(withId(R.id.jumpEndButton)).perform(click());
        return new FormEndPage(formName).assertOnPage();
    }

    public FormEntryPage clickOnQuestion(String questionLabel) {
        onView(withId(R.id.list)).perform(RecyclerViewActions.scrollTo(hasDescendant(withText(questionLabel))));
        clickOnText(questionLabel);
        return new FormEntryPage(formName);
    }

    public FormHierarchyPage clickOnGroup(String groupLabel) {
        onView(withId(R.id.list)).perform(RecyclerViewActions.scrollTo(hasDescendant(withText(groupLabel))));
        clickOnText(groupLabel);
        return this;
    }
}
