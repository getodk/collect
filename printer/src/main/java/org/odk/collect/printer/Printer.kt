package org.odk.collect.printer

import android.content.Context
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient

class Printer {
    private var webView: WebView? = null

    fun print(context: Context, htmlDocument: String) {
        webView = WebView(context).apply {
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest) = false

                override fun onPageFinished(view: WebView, url: String) {
                    createWebPrintJob(context, view)
                    webView = null
                }
            }

            loadDataWithBaseURL(null, htmlDocument, "text/HTML", "UTF-8", null)
        }
    }

    private fun createWebPrintJob(context: Context, webView: WebView) {
        (context.getSystemService(Context.PRINT_SERVICE) as? PrintManager)?.let { printManager ->
            val jobName = "ODK print"
            val printAdapter = webView.createPrintDocumentAdapter(jobName)
            printManager.print(
                jobName,
                printAdapter,
                PrintAttributes.Builder().build()
            )
        }
    }
}
