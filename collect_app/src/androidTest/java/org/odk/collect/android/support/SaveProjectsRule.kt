package org.odk.collect.android.support

import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import org.odk.collect.android.application.Collect
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.android.projects.Project

class SaveProjectsRule(val projects: List<Project>) : TestRule {

    override fun apply(base: Statement, description: Description?): Statement {
        return SaveProjectsStatement(base)
    }

    inner class SaveProjectsStatement(private val base: Statement) : Statement() {
        override fun evaluate() {
            val projectsRepository = DaggerUtils.getComponent(Collect.getInstance()).projectsRepository()
            val currentProjectProvider = DaggerUtils.getComponent(Collect.getInstance()).currentProjectProvider()
            for (project in projects) {
                projectsRepository.add(Project(project.uuid, project.name, project.icon, project.color))
            }
            currentProjectProvider.setCurrentProject(projectsRepository.getAll()[0].uuid)
            base.evaluate()
        }
    }
}
