package com.smartmobilefactory.epubreader.display

import android.os.Build
import android.support.annotation.MainThread
import android.webkit.WebView

import com.google.gson.Gson

internal class WebViewHelper(private val webView: WebView) {

    var gson = Gson()

    fun callJavaScriptMethod(method: String, vararg args: Any?) {
        executeCommand(createJsMethodCall(method, *args))
    }

    /**
     * creates javascript method call
     */
    private fun createJsMethodCall(method: String, vararg args: Any?): String {
        val builder = StringBuilder(method.length + args.size * 32)
                .append(method).append('(')

        args.forEachIndexed { index, arg ->
            if (index != 0) {
                builder.append(',')
            }
            builder.append(gson.toJson(arg))
        }

        builder.append(')')
        return builder.toString()
    }

    @MainThread
    private fun executeCommand(javascriptCommand: String) {
        @Suppress("NAME_SHADOWING")
        var javascriptCommand = javascriptCommand
        javascriptCommand = "javascript:" + javascriptCommand
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            webView.evaluateJavascript(javascriptCommand, null)
        } else {
            webView.loadUrl(javascriptCommand)
        }
    }


}
