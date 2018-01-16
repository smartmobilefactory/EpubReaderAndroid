package com.smartmobilefactory.epubreader.display.view

import com.smartmobilefactory.epubreader.display.WebViewHelper

internal class JsApi(
        private val webViewHelper: WebViewHelper
) {

    fun scrollToElementById(id: String) {
        webViewHelper.callJavaScriptMethod("scrollToElementById", id)
    }

    fun scrollToElementByXPath(xPath: String) {
        webViewHelper.callJavaScriptMethod("scrollToElementByXPath", xPath)
    }

    fun scrollToRangeStart(start: Int) {
        webViewHelper.callJavaScriptMethod("scrollToRangeStart", start)
    }

    fun setFontFamily(name: String) {
        webViewHelper.callJavaScriptMethod("setFontFamily", name)
    }

    fun setFont(name: String?, uri: String?) {
        webViewHelper.callJavaScriptMethod("setFont", name, uri)
    }

    fun updateFirstVisibleElement() {
        webViewHelper.callJavaScriptMethod("updateFirstVisibleElement")
    }

    fun updateFirstVisibleElementByTopPosition(value: Float) {
        webViewHelper.callJavaScriptMethod("updateFirstVisibleElementByTopPosition", value)
    }

    fun getYPositionOfElementWithId(id: String) {
        webViewHelper.callJavaScriptMethod("getYPositionOfElementWithId", id)
    }

    fun getYPositionOfElementWithXPath(xPath: String) {
        webViewHelper.callJavaScriptMethod("getYPositionOfElementWithXPath", xPath)
    }

    fun getYPositionOfElementFromRangeStart(start: Int) {
        webViewHelper.callJavaScriptMethod("getYPositionOfElementFromRangeStart", start)
    }
}