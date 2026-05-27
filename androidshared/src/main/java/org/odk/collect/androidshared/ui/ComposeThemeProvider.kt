package org.odk.collect.androidshared.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView

/**
 * Interface that allows a [android.content.Context] (such as an [android.app.Activity]) to
 * provide a Compose Theme to any child [ComposeView] instances:
 *
 * ```kotlin
 * class MyActivity : AppCompatActivity(), ComposeThemeProvider {
 *
 *     override fun onCreate(savedInstanceState: Bundle?) {
 *         super.onCreate(savedInstanceState)
 *         setContentView(R.layout.my_activity_layout)
 *         findViewById<ComposeView>(R.id.compose_view).setContextThemedContent {
 *             Text("Hello, world!")
 *         }
 *     }
 *
 *     @Composable
 *     override fun Theme(content: @Composable (() -> Unit)) {
 *         MyTheme { content() }
 *     }
 * }
 */
interface ComposeThemeProvider {
    @Composable
    fun Theme(content: @Composable () -> Unit)

    companion object {
        fun ComposeView.setContextThemedContent(content: @Composable () -> Unit) {
            setContent {
                (context as ComposeThemeProvider).Theme {
                    content()
                }
            }
        }
    }
}
