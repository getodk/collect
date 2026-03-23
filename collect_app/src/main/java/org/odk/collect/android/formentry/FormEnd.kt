package org.odk.collect.android.formentry

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.odk.collect.android.R
import org.odk.collect.android.application.CollectTheme
import org.odk.collect.androidshared.ui.compose.marginExtraSmall
import org.odk.collect.androidshared.ui.compose.marginSmall
import org.odk.collect.androidshared.ui.compose.marginStandard
import org.odk.collect.androidshared.ui.multiclicksafe.MultiClickGuard
import org.odk.collect.strings.R.string

@Composable
fun FormEnd(
    formTitle: String,
    isEditableAfterFinalization: Boolean,
    shouldBeSentAutomatically: Boolean,
    saveAsDraftEnabled: Boolean,
    finalizeEnabled: Boolean,
    onSave: (Boolean) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(marginStandard()),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(string.save_enter_data_description, formTitle),
            style = MaterialTheme.typography.titleLarge
        )

        if (finalizeEnabled) {
            val (icon, title, message) = getWarning(
                isEditableAfterFinalization,
                shouldBeSentAutomatically,
                saveAsDraftEnabled
            )

            EditWarning(icon = icon, title = title, message = message)
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 64.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val screeName = stringResource(org.odk.collect.android.R.string.form_end_screen)

            if (saveAsDraftEnabled) {
                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        if (MultiClickGuard.allowClick(screeName)) {
                            onSave(false)
                        }
                    }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(R.drawable.ic_save_menu_24),
                            contentDescription = null
                        )

                        Text(
                            text = stringResource(string.save_as_draft),
                            modifier = Modifier.padding(start = marginExtraSmall())
                        )
                    }
                }
            }

            if (finalizeEnabled) {
                val finalizeText = if (shouldBeSentAutomatically) {
                    string.send
                } else {
                    string.finalize
                }

                Button(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        if (MultiClickGuard.allowClick(screeName)) {
                            onSave(true)
                        }
                    }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(R.drawable.ic_send_24),
                            contentDescription = null
                        )

                        Text(
                            text = stringResource(finalizeText),
                            modifier = Modifier.padding(start = marginExtraSmall())
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EditWarning(
    @DrawableRes icon: Int,
    @StringRes title: Int,
    @StringRes message: Int?
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = marginStandard())
            .testTag(EditWarningSemantics.TAG)
            .semantics {
                set(EditWarningSemantics.iconProperty, icon)
                set(EditWarningSemantics.titleProperty, title)
                set(EditWarningSemantics.messageProperty, message)
            }
    ) {
        Row(modifier = Modifier.padding(marginStandard())) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null
            )

            Column(modifier = Modifier.padding(start = marginStandard())) {
                Text(
                    text = stringResource(title),
                    style = MaterialTheme.typography.titleMedium
                )

                if (message != null) {
                    Text(
                        text = stringResource(message),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(top = marginSmall())
                    )
                }
            }
        }
    }
}

private fun getWarning(
    isEditableAfterFinalization: Boolean,
    shouldBeSentAutomatically: Boolean,
    saveAsDraftEnabled: Boolean
): Triple<Int, Int, Int?> {
    return if (isEditableAfterFinalization) {
        if (shouldBeSentAutomatically) {
            Triple(
                R.drawable.ic_edit_24,
                string.form_editing_enabled_after_sending,
                string.form_editing_enabled_after_sending_hint
            )
        } else {
            Triple(
                R.drawable.ic_edit_24,
                string.form_editing_enabled_after_finalizing,
                string.form_editing_enabled_after_finalizing_hint
            )
        }
    } else {
        val icon = R.drawable.ic_edit_off_24
        val message = if (saveAsDraftEnabled) {
            string.form_editing_disabled_hint
        } else {
            null
        }

        if (shouldBeSentAutomatically) {
            Triple(
                icon,
                string.form_editing_disabled_after_sending,
                message
            )
        } else {
            Triple(
                icon,
                string.form_editing_disabled_after_finalizing,
                message
            )
        }
    }
}

object EditWarningSemantics {
    const val TAG = "EditWarning"
    val iconProperty = SemanticsPropertyKey<Int>("icon")
    val titleProperty = SemanticsPropertyKey<Int>("title")
    val messageProperty = SemanticsPropertyKey<Int?>("message")
}

@Preview
@Composable
private fun PreviewFormEnd(saveAsDraftEnabled: Boolean = true, finalizeEnabled: Boolean = true) {
    CollectTheme {
        FormEnd(
            formTitle = "My form",
            isEditableAfterFinalization = false,
            shouldBeSentAutomatically = false,
            saveAsDraftEnabled = saveAsDraftEnabled,
            finalizeEnabled = finalizeEnabled
        )
    }
}

@Preview
@Composable
private fun PreviewFormEndSaveAsDraftDisabled() {
    PreviewFormEnd(saveAsDraftEnabled = false)
}

@Preview
@Composable
private fun PreviewFormEndFinalizeDisabled() {
    PreviewFormEnd(finalizeEnabled = false)
}