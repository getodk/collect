package org.odk.collect.entities.javarosa.intance

import org.javarosa.xform.parse.ExternalInstanceParser
import org.javarosa.xform.parse.ExternalInstanceParserFactory
import org.odk.collect.entities.storage.EntitiesRepository
import org.odk.collect.forms.FormMediaFileRepository

class LocalEntitiesExternalInstanceParserFactory(
    private val entitiesRepositoryProvider: () -> EntitiesRepository,
    private val mediaFileRepository: FormMediaFileRepository,
    private val enabled: () -> Boolean
) : ExternalInstanceParserFactory {
    override fun getExternalInstanceParser(): ExternalInstanceParser {
        val parser = ExternalInstanceParser()

        if (enabled()) {
            parser.addInstanceProvider(
                LocalEntitiesInstanceProvider(entitiesRepositoryProvider, mediaFileRepository)
            )
        }

        return parser
    }
}
