/*
 * Copyright (C) 2018 Nafundi
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.odk.collect.android.upload

import android.net.Uri
import java.lang.Exception

/**
 * Thrown to indicate that a problem with submitting the current finalized form has occurred.
 *
 * Throwing an UploadException makes the submission attempt move on to the next finalized form to
 * send except in the case of an [FormUploadAuthRequestedException] thrown when the submission
 * attempt was triggered manually by the user. In that case, the finalized form that resulted in the
 * exception will be re-tried after the user provides credentials.
 */
open class FormUploadException : Exception {
    constructor(message: String) : super(message)
    constructor(cause: Throwable) : super(cause)

    override val message: String
        get() = super.message
            ?: cause?.message
            ?: ""
}

/**
 * Thrown to indicate that the server an upload attempt was made to is requesting authentication.
 * This may lead to a re-try attempt if the upload was triggered manually by the user (as opposed to
 * auto-send).
 *
 * @param authRequestingServer The URI for the server that requested authentication. This URI may
 * not match the server specified in the app settings or the blank form because there could have
 * been a redirect. See also [org.odk.collect.android.tasks.InstanceUploaderTask.Outcome]
 */
class FormUploadAuthRequestedException(
    message: String,
    val authRequestingServer: Uri
) : FormUploadException(message)
