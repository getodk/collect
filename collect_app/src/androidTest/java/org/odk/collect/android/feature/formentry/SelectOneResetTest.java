package org.odk.collect.android.feature.formentry;

import org.jetbrains.annotations.NotNull;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.odk.collect.android.support.CollectTestRule;
import org.odk.collect.android.support.CopyFormRule;
import org.odk.collect.android.support.ResetStateRule;
import org.odk.collect.android.support.pages.FormEntryPage;
import org.odk.collect.android.support.pages.FormHierarchyPage;
import org.odk.collect.android.support.pages.MainMenuPage;

import java.util.Collections;

import timber.log.Timber;

import static org.odk.collect.android.feature.formentry.SelectOneResetTest.Appearance.Minimal;
import static org.odk.collect.android.feature.formentry.SelectOneResetTest.Appearance.Plain;
import static org.odk.collect.android.feature.formentry.SelectOneResetTest.Block.A;
import static org.odk.collect.android.feature.formentry.SelectOneResetTest.Block.B;
import static org.odk.collect.android.feature.formentry.SelectOneResetTest.Block.C;
import static org.odk.collect.android.feature.formentry.SelectOneResetTest.Block.D;
import static org.odk.collect.android.feature.formentry.SelectOneResetTest.Block.E;
import static org.odk.collect.android.feature.formentry.SelectOneResetTest.ItemsetType.FastExternal;
import static org.odk.collect.android.feature.formentry.SelectOneResetTest.ItemsetType.Internal;

public class SelectOneResetTest {

    enum ItemsetType {
        Internal,
        FastExternal
    }

    enum Appearance {
        Plain,
        Autocomplete,
        Minimal,
        MinimalAutocomplete;

        public boolean isMinimal() {
            return this == Minimal || this == MinimalAutocomplete;
        }
    }

    enum SectionVariant {
        Internal_Plain(Internal, Plain),
        Internal_Minimal(Internal, Minimal),
        FastExternal_Plain(FastExternal, Plain),
        FastExternal_Minimal(FastExternal, Minimal),
        /*Internal_MinimalAutocomplete(Internal, MinimalAutocomplete),
        FastExternal_MinimalAutocomplete(FastExternal, MinimalAutocomplete),
        Internal_Autocomplete(Internal, Autocomplete),
        FastExternal_Autocomplete(FastExternal, Autocomplete)*/;

        final ItemsetType itemsetType;
        final Appearance appearance;

        SectionVariant(ItemsetType itemsetType, Appearance appearance) {
            this.itemsetType = itemsetType;
            this.appearance = appearance;
        }
    }

    enum Block {
        A, B, C, D, E;

        @NotNull String groupLabel(@NotNull SectionVariant variant) {
            return "group_" + name() + "-" + variant.ordinal();
        }

        @NotNull String stateLabel(@NotNull SectionVariant variant) {
            return "state_" + name() + "-" + variant.ordinal();
        }

        @NotNull String countyLabel(@NotNull SectionVariant variant) {
            return "county_" + name() + "-" + variant.ordinal();
        }

        @NotNull String cityLabel(@NotNull SectionVariant variant) {
            return "city_" + name() + "-" + variant.ordinal();
        }

        @NotNull String wardLabel(@NotNull SectionVariant variant) {
            return "ward_" + name() + "-" + variant.ordinal();
        }

        @NotNull String showWardLabel(@NotNull SectionVariant variant) {
            return "show-ward_" + name() + "-" + variant.ordinal();
        }

        @NotNull String stateAfterLabel(@NotNull SectionVariant variant) {
            String name = name();
            return "state_" + name + name + "-" + variant.ordinal();
        }

        @NotNull String countyAfterLabel(@NotNull SectionVariant variant) {
            String name = name();
            return "county_" + name + name + "-" + variant.ordinal();
        }
    }

    static final String TEXT_FORM = "selectOneReset";
    static final String TEXT_NO = "no";
    static final String TEXT_YES = "yes";
    static final String TEXT_SELECT_ANSWER = "Select Answer";
    static final String TEXT_HARLINGEN = "Harlingen";
    static final String TEXT_BROWNSVILLE = "Brownsville";
    static final String TEXT_TEXAS = "Texas";
    static final String TEXT_CAMERON = "Cameron";
    static final String TEXT_WASHINGTON = "Washington";
    static final String TEXT_NORTH = "North";
    static final String TEXT_SOUTH = "South";
    static final String TEXT_WEST = "West";
    static final String TEXT_EAST = "East";

    public CollectTestRule rule = new CollectTestRule();
    @Rule
    public RuleChain copyFormChain = RuleChain
            .outerRule(new ResetStateRule())
            .around(new CopyFormRule(TEXT_FORM + ".xml",
                    Collections.singletonList(TEXT_FORM + "-media/itemsets.csv")))
            .around(rule);

    private SectionVariant variantNow;

    @Test
    public void testAllVariants() {
        FormHierarchyPage hierarchy = new MainMenuPage()
                .startBlankForm(TEXT_FORM)
                .clickGoToArrow();
        for (SectionVariant variant : SectionVariant.values()) {
            variantNow = variant;
            int ordinal = variant.ordinal();
            Timber.i("testing " + variant + "=" + ordinal);
            testBlockABC(A, hierarchy)
                    .clickOnGroup(B.groupLabel(variant));
            testBlockABC(B, hierarchy)
                    .clickOnGroup(C.groupLabel(variant));
            testBlockABC(C, hierarchy)
                    .clickGoUpIcon()
                    .clickGoUpIcon()
                    .clickOnGroup(D.groupLabel(variant));
            testBlocksDE(hierarchy, true)
                    .clickGoUpIcon();
            Timber.i("passed " + variant + "=" + ordinal);
        }
    }

    FormHierarchyPage testBlockABC(Block block, FormHierarchyPage hierarchy) {
        Timber.i(newBlockMsg(block, variantNow));
        String showWardLabel = block.showWardLabel(variantNow);
        String cityLabel = block.cityLabel(variantNow);
        String wardLabel = block.wardLabel(variantNow);
        String stateLabel = block.stateLabel(variantNow);
        boolean minimal = variantNow.appearance.isMinimal();
        FormEntryPage entry = hierarchy
                .clickOnQuestion(showWardLabel)
                .clickOnText(TEXT_NO)
                .swipeToPreviousQuestion(cityLabel);
        if (minimal) {
            entry.openSelectMinimalDialog();
        }
        entry.clickOnText(TEXT_HARLINGEN)
                .swipeToNextQuestion(showWardLabel)
                .clickOnText(TEXT_YES)
                .clickGoToArrow();
        //BC1h
        if (block != A) {
            hierarchy.assertTextDoesNotExist(TEXT_NORTH);
        }
        hierarchy.clickOnQuestion(wardLabel)
                //ABC1e
                .assertTextDoesNotExist();
        if (minimal) {
            entry.openSelectMinimalDialog();
        }
        entry.clickOnText(TEXT_EAST)
                .swipeToPreviousQuestion(showWardLabel)
                .swipeToPreviousQuestion(cityLabel);
        if (minimal) {
            entry.openSelectMinimalDialog();
        }
        entry.clickOnText(TEXT_BROWNSVILLE)
                .clickGoToArrow();
        //BC2h
        if (block != A) {
            hierarchy.assertTextDoesNotExist(TEXT_EAST);
        }
        hierarchy.clickOnQuestion(wardLabel)
                //ABC2e
                .assertTextDoesNotExist(TEXT_EAST);
        if (minimal) {
            entry.openSelectMinimalDialog();
        }
        entry.clickOnText(TEXT_NORTH)
                .clickGoToArrow()
                .clickOnQuestion(stateLabel);
        if (minimal) {
            entry.openSelectMinimalDialog();
        }
        entry.clickOnText(TEXT_WASHINGTON)
                .clickGoToArrow();
        //BC3h
        if (block != A) {
            hierarchy.assertText(TEXT_WASHINGTON, TEXT_YES)
                    .assertTextDoesNotExist(TEXT_NORTH);
        }
        //ABC3e
        hierarchy.clickOnQuestion(stateLabel).swipeToNextQuestion(block.countyLabel(variantNow))
                .assertTextDoesNotExist()
                .swipeToNextQuestion(block.cityLabel(variantNow))
                .assertTextDoesNotExist()
                .swipeToNextQuestion(block.showWardLabel(variantNow))
                .swipeToNextQuestion(block.wardLabel(variantNow))
                .assertTextDoesNotExist();
        if (block == B) {
            entry.swipeToNextQuestion(block.stateAfterLabel(variantNow))
                    .swipeToNextQuestion(block.countyAfterLabel(variantNow))
                    //BB4e
                    .assertText(TEXT_CAMERON);
        }
        return entry.clickGoToArrow();
    }

    FormHierarchyPage testBlocksDE(FormHierarchyPage hierarchy, boolean testBlockE) {
        Block block = D;
        Timber.i(newBlockMsg(block, variantNow));
        String wardLabel = block.wardLabel(variantNow);
        FormEntryPage entry = hierarchy.clickOnQuestion(wardLabel);
        boolean minimal = variantNow.appearance.isMinimal();
        if (minimal) {
            entry.clickOnText(TEXT_NO, 0)
                    .clickOnText(TEXT_BROWNSVILLE, 0)
                    .clickOnText(TEXT_HARLINGEN)
                    .clickOnText(TEXT_YES, 0)
                    //DE1e
                    .clickOnText(TEXT_SELECT_ANSWER)
                    .clickOnText(TEXT_EAST)
                    .clickOnText(TEXT_HARLINGEN, 0)
                    .clickOnText(TEXT_BROWNSVILLE)
                    //DE2e
                    .clickOnText(TEXT_SELECT_ANSWER)
                    .clickOnText(TEXT_NORTH)
                    .scrollToAndClickText(TEXT_TEXAS, 0)
                    .clickOnText(TEXT_WASHINGTON);
        } else {
            entry.scrollToAndClickText(TEXT_NO, 0)
                    .clickOnText(TEXT_HARLINGEN, 0)
                    .scrollToAndClickText(TEXT_YES, 0)
                    .scrollToText(TEXT_WEST, 0)
                    //DE1e
                    .assertTextIsNotChecked(TEXT_WEST, 0)
                    .assertTextIsNotChecked(TEXT_EAST, 0)
                    .clickOnText(TEXT_EAST, 0)
                    .clickOnText(TEXT_BROWNSVILLE, 0)
                    .scrollToText(TEXT_SOUTH, 0)
                    //DE2e
                    .assertTextIsNotChecked(TEXT_SOUTH, 0)
                    .assertTextIsNotChecked(TEXT_NORTH, 0)
                    .clickOnText(TEXT_NORTH, 0)
                    .scrollToAndClickText(TEXT_WASHINGTON, 0);
        }
        entry.clickGoToArrow()
                //DE3h
                .assertText(TEXT_WASHINGTON, TEXT_YES)
                .assertTextDoesNotExist(TEXT_NORTH)
                //DE4h
                .clickOnGroup(E.groupLabel(variantNow))
                .assertText(TEXT_CAMERON)
                .clickGoUpIcon();
        block = E;
        Timber.i(newBlockMsg(block, variantNow));
        String groupLabel = block.groupLabel(variantNow);
        wardLabel = block.wardLabel(variantNow);
        entry = hierarchy.clickOnGroup(groupLabel)
                .clickOnQuestion(wardLabel)
                .scrollToText(wardLabel, 0)
                .clickOnText(TEXT_NO, 1);
        if (minimal) {
            entry.scrollToAndClickText(TEXT_BROWNSVILLE, 0)
                    .clickOnText(TEXT_HARLINGEN)
                    .clickOnText(TEXT_YES, 1)
                    //DE1e
                    .scrollToAndClickText(TEXT_SELECT_ANSWER, 3)
                    .clickOnText(TEXT_EAST)
                    .clickOnText(TEXT_HARLINGEN)
                    .clickOnText(TEXT_BROWNSVILLE)
                    //DE2e
                    .scrollToAndClickText(TEXT_SELECT_ANSWER, 3)
                    .clickOnText(TEXT_NORTH)
                    .scrollToAndClickText(TEXT_TEXAS, 0)
                    .clickOnText(TEXT_WASHINGTON);
        } else {
            entry.clickOnText(TEXT_HARLINGEN)
                    .scrollToAndClickText(TEXT_YES, 1)
                    .scrollToText(TEXT_WEST, 0)
                    //DE1e
                    .assertTextIsNotChecked(TEXT_WEST, 0)
                    .assertTextIsNotChecked(TEXT_EAST, 0)
                    .clickOnText(TEXT_EAST)
                    .clickOnText(TEXT_BROWNSVILLE)
                    .scrollToText(TEXT_SOUTH, 0)
                    //DE2e
                    .assertTextIsNotChecked(TEXT_SOUTH, 0)
                    .assertTextIsNotChecked(TEXT_NORTH, 0)
                    .clickOnText(TEXT_NORTH)
                    .scrollToAndClickText(TEXT_WASHINGTON, 1);
        }
        return entry.clickGoToArrow()
                .clickOnGroup(groupLabel)
                //DE3h
                .assertText(TEXT_WASHINGTON, TEXT_YES)
                .assertTextDoesNotExist(TEXT_NORTH)
                .clickGoUpIcon();
    }

    private String newBlockMsg(Block block, SectionVariant variant) {
        return "Block " + block.name() + "-" + variant.ordinal();
    }
}
