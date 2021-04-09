package org.odk.collect.android.feature.projects

import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.odk.collect.android.R
import org.odk.collect.android.application.Collect
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.android.projects.Project
import org.odk.collect.android.support.CollectTestRule
import org.odk.collect.android.support.TestRuleChain

class SwitchProjectTest {

    private val projects = listOf(
        Project("Turtle nesting", "T", "#00FF00", "1"),
        Project("Polio - Banadir", "P", "#FF0000", "2")
    )

    val rule = CollectTestRule()

    @get:Rule
    var chain: RuleChain = TestRuleChain
        .chain()
        .around(rule)

    @Before
    fun setup() {
        val projectsRepository = DaggerUtils.getComponent(Collect.getInstance()).projectsRepository()
        val currentProjectProvider = DaggerUtils.getComponent(Collect.getInstance()).currentProjectProvider()
        for (project in projects) {
            projectsRepository.add(Project(project.name, project.icon, project.color, project.uuid))
        }
        currentProjectProvider.setCurrentProject(projectsRepository.getAll()[0].uuid)
    }

    @After
    fun teardown() {
        val projectsRepository = DaggerUtils.getComponent(Collect.getInstance()).projectsRepository()
        projectsRepository.deleteAll()
    }

    @Test
    fun switchProjectTest() {
        rule.mainMenu()
            .openProjectSettingsDialog()
            .assertCurrentProject(projects[0])
            .assertInactiveProject(projects[1])
            .clickOnText("Polio - Banadir")
            .checkIsToastWithMessageDisplayed(R.string.switched_project, "Polio - Banadir")

        rule.mainMenu()
            .openProjectSettingsDialog()
            .assertCurrentProject(projects[1])
            .assertInactiveProject(projects[0])
    }
}
