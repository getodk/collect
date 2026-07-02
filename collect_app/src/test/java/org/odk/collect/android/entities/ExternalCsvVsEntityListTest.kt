package org.odk.collect.android.entities

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.javarosa.core.model.FormDef
import org.javarosa.core.reference.ReferenceManager
import org.javarosa.form.api.FormEntryController
import org.javarosa.form.api.FormEntryModel
import org.javarosa.test.BindBuilderXFormsElement.bind
import org.javarosa.test.Scenario
import org.javarosa.test.XFormsElement.body
import org.javarosa.test.XFormsElement.head
import org.javarosa.test.XFormsElement.html
import org.javarosa.test.XFormsElement.mainInstance
import org.javarosa.test.XFormsElement.model
import org.javarosa.test.XFormsElement.select1Dynamic
import org.javarosa.test.XFormsElement.t
import org.javarosa.test.XFormsElement.title
import org.javarosa.xform.parse.ExternalInstanceParser
import org.javarosa.xform.util.XFormUtils
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.odk.collect.android.javarosawrapper.ReferenceManagerMediaFileRepository
import org.odk.collect.android.utilities.FormUtils
import org.odk.collect.entities.javarosa.filter.LocalEntitiesFilterStrategy
import org.odk.collect.entities.javarosa.intance.LocalEntitiesExternalInstanceParserFactory
import org.odk.collect.entities.storage.Entity
import org.odk.collect.entities.storage.InMemEntitiesRepository
import org.odk.collect.shared.TempFiles
import java.io.File

/**
 * Regression test for https://github.com/getodk/collect/issues/6425.
 *
 * When an attached CSV has the same name as an entity list in the project, an external select
 * backed by that CSV must read from the CSV - not from the entity list. A select with no such
 * attachment still falls back to the entity list of the same name.
 */

class ExternalCsvVsEntityListTest {
    private val entitiesRepository = InMemEntitiesRepository()

    private val controllerSupplier: (FormDef) -> FormEntryController = { formDef ->
        FormEntryController(FormEntryModel(formDef)).also {
            it.addFilterStrategy(LocalEntitiesFilterStrategy(entitiesRepository))
        }
    }

    @Before
    fun setup() {
        XFormUtils.setExternalInstanceParserFactory(
            LocalEntitiesExternalInstanceParserFactory(
                { entitiesRepository },
                ReferenceManagerMediaFileRepository(ReferenceManager.instance())
            )
        )
    }

    @After
    fun teardown() {
        XFormUtils.setExternalInstanceParserFactory { ExternalInstanceParser() }
        ReferenceManager.instance().reset()
    }

    @Test
    fun `an external select reads from its attached CSV even when an entity list of the same name exists`() {
        entitiesRepository.save("households", Entity.New("springfield", "Springfield"))
        entitiesRepository.save("people", Entity.New("alice", "Alice"))

        val projectRootDir = TempFiles.createTempDir()
        val formMediaDir = File(projectRootDir, "forms/clashing-names-media").also { it.mkdirs() }
        File(formMediaDir, "people.csv").writeText("name,label\njohn,John\n")

        FormUtils.setupReferenceManagerForForm(ReferenceManager.instance(), projectRootDir, formMediaDir)

        val form = html(
            head(
                title("Select form"),
                model(
                    mainInstance(
                        t("data id=\"select-form\"", t("household"), t("person"))
                    ),
                    t("instance id=\"households\" src=\"jr://file-csv/households.csv\""),
                    t("instance id=\"people\" src=\"jr://file-csv/people.csv\""),
                    bind("/data/household").type("string"),
                    bind("/data/person").type("string")
                )
            ),
            body(
                select1Dynamic("/data/household", "instance('households')/root/item", "name", "label"),
                select1Dynamic("/data/person", "instance('people')/root/item", "name", "label")
            )
        )

        val scenario = Scenario.init(form, controllerSupplier)

        // The household choices come from the entity list.
        assertThat(scenario.choicesOf("/data/household").map { it.value }, equalTo(listOf("springfield")))
        // The person choices come from the attached CSV, not the entity list of the same name.
        assertThat(scenario.choicesOf("/data/person").map { it.value }, equalTo(listOf("john")))
    }
}
