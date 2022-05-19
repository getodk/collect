package org.odk.collect.android.formlists.blankformlist

import org.odk.collect.forms.Form
import org.odk.collect.formstest.FormUtils

object BlankFormFixtures {
    val blankForm1: Form = Form.Builder()
        .dbId(1)
        .formId("1")
        .version("1")
        .displayName("Form 1")
        .date(0)
        .formFilePath(FormUtils.createXFormFile("1", "1").absolutePath)
        .build()

    val blankForm2: Form = Form.Builder()
        .dbId(2)
        .formId("2")
        .version("1")
        .displayName("Form 2")
        .date(1)
        .formFilePath(FormUtils.createXFormFile("2", "1").absolutePath)
        .build()

    val blankForm3: Form = Form.Builder()
        .dbId(3)
        .formId("3")
        .version("1")
        .displayName("Form 2x")
        .date(1)
        .formFilePath(FormUtils.createXFormFile("2", "2").absolutePath)
        .build()

    val blankForm4: Form = Form.Builder()
        .dbId(4)
        .formId("4")
        .version("1")
        .displayName("Form 4")
        .date(3)
        .deleted(true)
        .formFilePath(FormUtils.createXFormFile("4", "1").absolutePath)
        .build()

    val blankForm5: Form = Form.Builder()
        .dbId(5)
        .formId("5")
        .version("1")
        .displayName("Form 05")
        .date(4)
        .formFilePath(FormUtils.createXFormFile("5", "1").absolutePath)
        .build()
}
