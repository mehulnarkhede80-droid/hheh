package com.example.data.gemini

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.json.JSONObject
import java.util.concurrent.atomic.AtomicBoolean

object PuterApiClient {
    private const val REQUEST_TIMEOUT_MILLIS = 120_000L

    private val bridgePage = """
        <!doctype html>
        <html>
        <head>
          <meta name="viewport" content="width=device-width, initial-scale=1">
          <script src="https://js.puter.com/v2/"></script>
          <script>
            window.transitPuterChat = async (prompt, model) => {
              try {
                const response = await puter.ai.chat(prompt, { model });
                const content = response?.message?.content;
                const text = typeof response === 'string' ? response
                  : typeof content === 'string' ? content
                  : Array.isArray(content) ? content.map(part => part?.text || '').join('')
                  : typeof response?.text === 'string' ? response.text
                  : JSON.stringify(response);
                TransitPuterBridge.onSuccess(text || 'Puter returned an empty response.');
              } catch (error) {
                TransitPuterBridge.onError(error?.message || String(error));
              }
            };
          </script>
        </head>
        <body></body>
        </html>
    """.trimIndent()

    suspend fun chat(context: Context, prompt: String, model: String): String =
        withTimeout(REQUEST_TIMEOUT_MILLIS) {
            withContext(Dispatchers.Main.immediate) {
                suspendCancellableCoroutine { continuation ->
                    val mainHandler = Handler(Looper.getMainLooper())
                    val completed = AtomicBoolean(false)
                    var webView: WebView? = null

                    fun disposeWebView() {
                        webView?.let { browser ->
                            browser.stopLoading()
                            browser.removeJavascriptInterface("TransitPuterBridge")
                            browser.destroy()
                        }
                        webView = null
                    }

                    fun finish(result: Result<String>) {
                        if (!completed.compareAndSet(false, true)) return
                        mainHandler.post {
                            disposeWebView()
                            if (continuation.isActive) continuation.resumeWith(result)
                        }
                    }

                    val browser = WebView(context)
                    webView = browser
                    browser.settings.javaScriptEnabled = true
                    browser.settings.domStorageEnabled = true
                    browser.settings.allowFileAccess = false
                    browser.settings.allowContentAccess = false
                    browser.addJavascriptInterface(object {
                        @JavascriptInterface
                        fun onSuccess(text: String) = finish(Result.success(text))

                        @JavascriptInterface
                        fun onError(message: String) = finish(Result.failure(IllegalStateException(message)))
                    }, "TransitPuterBridge")
                    browser.webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView, url: String) {
                            val call = "window.transitPuterChat(${JSONObject.quote(prompt)}, ${JSONObject.quote(model)})"
                            view.evaluateJavascript(call, null)
                        }
                    }

                    continuation.invokeOnCancellation {
                        completed.set(true)
                        mainHandler.post { disposeWebView() }
                    }
                    browser.loadDataWithBaseURL(
                        "https://puter.com/",
                        bridgePage,
                        "text/html",
                        "UTF-8",
                        null
                    )
                }
            }
        }
}