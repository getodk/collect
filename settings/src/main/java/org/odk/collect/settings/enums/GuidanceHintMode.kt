package org.odk.collect.settings.enums

import androidx.annotation.StringRes
import org.odk.collect.settings.R

enum class GuidanceHintMode(@StringRes override val stringId: Int) : StringIdEnum {
    YES(R.string.guidance_hint_yes),
    NO(R.string.guidance_hint_no),
    YES_COLLAPSED(R.string.guidance_hint_yes_collapsed)
}
