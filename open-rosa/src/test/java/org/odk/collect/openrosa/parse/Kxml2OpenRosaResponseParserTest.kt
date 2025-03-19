package org.odk.collect.openrosa.parse

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.javarosa.xform.parse.XFormParser
import org.junit.Ignore
import org.junit.Test
import org.kxml2.kdom.Document

class Kxml2OpenRosaResponseParserTest {

    @Test
    fun `#parseFormList when document is empty, returns null`() {
        val formList = Kxml2OpenRosaResponseParser.parseFormList(Document())
        assertThat(formList, equalTo(null))
    }

    @Test
    fun `#parseFormList when xform hash is missing prefix, returns null hash for item`() {
        val response = """
            <?xml version='1.0' encoding='UTF-8' ?>
            <xforms xmlns="http://openrosa.org/xforms/xformsList">
                <xform>
                    <formID>id</formID>
                    <name>form name</name>
                    <version>1</version>
                    <hash>blahblah</hash>
                    <downloadUrl>http://example.com</downloadUrl>
                </xform>
            </xforms>
        """.trimIndent()

        val doc = XFormParser.getXMLDocument(response.reader())
        val formList = Kxml2OpenRosaResponseParser.parseFormList(doc)
        assertThat(formList!![0].hash, equalTo(null))
    }

    @Test
    fun `#parseManifest when media file hash is empty, returns null`() {
        val response = """
            <?xml version='1.0' encoding='UTF-8' ?>
            <manifest xmlns="http://openrosa.org/xforms/xformsManifest">
                <mediaFile>
                    <filename>badger.png</filename>
                    <hash></hash>
                    <downloadUrl>http://funk.appspot.com/binaryData?blobKey=%3A477e3</downloadUrl>
                </mediaFile>
            </manifest>
        """.trimIndent()

        val doc = XFormParser.getXMLDocument(response.reader())
        val mediaFiles = Kxml2OpenRosaResponseParser.parseManifest(doc)
        assertThat(mediaFiles, equalTo(null))
    }

    @Test
    fun `#parseManifest when document is empty, returns null`() {
        val formList = Kxml2OpenRosaResponseParser.parseManifest(Document())
        assertThat(formList, equalTo(null))
    }

    @Test
    fun `#parseManifest sanitizes media file names`() {
        val response = """
            <?xml version='1.0' encoding='UTF-8' ?>
            <manifest xmlns="http://openrosa.org/xforms/xformsManifest">
                <mediaFile>
                    <filename>/../badgers.csv</filename>
                    <hash>blah</hash>
                    <downloadUrl>http://funk.appspot.com/binaryData?blobKey=%3A477e3</downloadUrl>
                </mediaFile>
            </manifest>
        """.trimIndent()

        val doc = XFormParser.getXMLDocument(response.reader())
        val mediaFiles = Kxml2OpenRosaResponseParser.parseManifest(doc)!!
        assertThat(mediaFiles.size, equalTo(1))
        assertThat(mediaFiles[0].filename, equalTo("badgers.csv"))
    }

    @Test
    fun `#parseManifest when media file has type entityList returns isEntityList as true`() {
        val response = """
            <?xml version='1.0' encoding='UTF-8' ?>
            <manifest xmlns="http://openrosa.org/xforms/xformsManifest">
                <mediaFile type="entityList">
                    <filename>badgers.csv</filename>
                    <hash>blah</hash>
                    <downloadUrl>http://funk.appspot.com/binaryData?blobKey=%3A477e3</downloadUrl>
                    <integrityUrl>https://some.server/forms/12/integrity</integrityUrl>
                </mediaFile>
            </manifest>
        """.trimIndent()

        val doc = XFormParser.getXMLDocument(response.reader())
        val mediaFiles = Kxml2OpenRosaResponseParser.parseManifest(doc)!!
        assertThat(mediaFiles.size, equalTo(1))
        assertThat(mediaFiles[0].isEntityList, equalTo(true))
    }

    @Test
    fun `#parseManifest when media file does not have type returns isEntityList as false`() {
        val response = """
            <?xml version='1.0' encoding='UTF-8' ?>
            <manifest xmlns="http://openrosa.org/xforms/xformsManifest">
                <mediaFile>
                    <filename>badgers.csv</filename>
                    <hash>blah</hash>
                    <downloadUrl>http://funk.appspot.com/binaryData?blobKey=%3A477e3</downloadUrl>
                </mediaFile>
            </manifest>
        """.trimIndent()

        val doc = XFormParser.getXMLDocument(response.reader())
        val mediaFiles = Kxml2OpenRosaResponseParser.parseManifest(doc)!!
        assertThat(mediaFiles.size, equalTo(1))
        assertThat(mediaFiles[0].isEntityList, equalTo(false))
    }

    @Test
    fun `#parseManifest includes integrityUrl when there is one`() {
        val response = """
            <?xml version='1.0' encoding='UTF-8' ?>
            <manifest xmlns="http://openrosa.org/xforms/xformsManifest">
                <mediaFile type="entityList">
                    <filename>badgers.csv</filename>
                    <hash>blah</hash>
                    <downloadUrl>http://funk.appspot.com/binaryData?blobKey=%3A477e3</downloadUrl>
                    <integrityUrl>https://some.server/forms/12/integrity</integrityUrl>
                </mediaFile>
            </manifest>
        """.trimIndent()

        val doc = XFormParser.getXMLDocument(response.reader())
        val mediaFiles = Kxml2OpenRosaResponseParser.parseManifest(doc)!!
        assertThat(mediaFiles.size, equalTo(1))
        assertThat(mediaFiles[0].integrityUrl, equalTo("https://some.server/forms/12/integrity"))
    }

    @Test
    fun `#parseManifest does not include integrityUrl when there isn't one`() {
        val response = """
            <?xml version='1.0' encoding='UTF-8' ?>
            <manifest xmlns="http://openrosa.org/xforms/xformsManifest">
                <mediaFile>
                    <filename>badgers.csv</filename>
                    <hash>blah</hash>
                    <downloadUrl>http://funk.appspot.com/binaryData?blobKey=%3A477e3</downloadUrl>
                </mediaFile>
            </manifest>
        """.trimIndent()

        val doc = XFormParser.getXMLDocument(response.reader())
        val mediaFiles = Kxml2OpenRosaResponseParser.parseManifest(doc)!!
        assertThat(mediaFiles.size, equalTo(1))
        assertThat(mediaFiles[0].integrityUrl, equalTo(null))
    }

    @Test
    @Ignore("This would break servers that had implemented type before integrityUrl was added to the spec. https://forum.getodk.org/t/openrosa-spec-proposal-support-offline-entities/48052/2")
    fun `#parseManifest returns null if a media file with type entityList is missing integrityUrl`() {
        val response = """
            <?xml version='1.0' encoding='UTF-8' ?>
            <manifest xmlns="http://openrosa.org/xforms/xformsManifest">
                <mediaFile type="entityList">
                    <filename>badgers.csv</filename>
                    <hash>blah</hash>
                    <downloadUrl>http://funk.appspot.com/binaryData?blobKey=%3A477e3</downloadUrl>
                </mediaFile>
            </manifest>
        """.trimIndent()

        val doc = XFormParser.getXMLDocument(response.reader())
        assertThat(Kxml2OpenRosaResponseParser.parseManifest(doc), equalTo(null))
    }

    @Test
    fun `#parseIntegrityResponse returns null when the response structure is incorrect`() {
        val noData = """
            <?xml version="1.0" encoding="UTF-8"?>
            <blah>
                <entities>
                    <entity id="958e00b2-43f5-4d21-8adb-3d27aaa045f5">
                        <deleted>true</deleted>
                    </entity>
                </entities>
            </blah>
        """.trimIndent()

        val noEntities = """
            <?xml version="1.0" encoding="UTF-8"?>
            <data>
                <stuff>
                    <entity id="958e00b2-43f5-4d21-8adb-3d27aaa045f5">
                        <deleted>true</deleted>
                    </entity>
                </stuff>
            </data>
        """.trimIndent()

        val noEntity = """
            <?xml version="1.0" encoding="UTF-8"?>
            <data>
                <entities>
                    <thing id="958e00b2-43f5-4d21-8adb-3d27aaa045f5">
                        <deleted>true</deleted>
                    </thing>
                </entities>
            </data>
        """.trimIndent()

        val noDeleted = """
            <?xml version="1.0" encoding="UTF-8"?>
            <data>
                <entities>
                    <entity id="958e00b2-43f5-4d21-8adb-3d27aaa045f5">
                        <blah>true</blah>
                    </entity>
                </entities>
            </data>
        """.trimIndent()

        val emptyDeleted = """
            <?xml version="1.0" encoding="UTF-8"?>
            <data>
                <entities>
                    <entity id="958e00b2-43f5-4d21-8adb-3d27aaa045f5">
                        <deleted></deleted>
                    </entity>
                </entities>
            </data>
        """.trimIndent()

        val invalidDeleted = """
            <?xml version="1.0" encoding="UTF-8"?>
            <data>
                <entities>
                    <entity id="958e00b2-43f5-4d21-8adb-3d27aaa045f5">
                        <deleted>yes</deleted>
                    </entity>
                </entities>
            </data>
        """.trimIndent()

        listOf(noData, noEntities, noEntity, noDeleted, emptyDeleted, invalidDeleted).forEach {
            val doc = XFormParser.getXMLDocument(it.reader())
            val response = Kxml2OpenRosaResponseParser.parseIntegrityResponse(doc)
            assertThat("The following should not be parsed:\n$it\n\n", response, equalTo(null))
        }
    }
}
