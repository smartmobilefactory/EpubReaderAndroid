package com.smartmobilefactory.epubreader.display

import android.net.Uri
import android.os.Build
import android.support.annotation.CheckResult
import android.webkit.WebView
import com.smartmobilefactory.epubreader.EpubViewSettings
import com.smartmobilefactory.epubreader.model.Epub
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import nl.siegmann.epublib.domain.SpineReference
import nl.siegmann.epublib.util.IOUtil
import java.io.IOException
import java.lang.ref.WeakReference
import java.util.*

internal object EpubDisplayHelper {

    private val INJECT_CSS_FORMAT = "<link rel=\"stylesheet\" type=\"text/css\" href=\"%s\">\n"
    private val INJECT_JAVASCRIPT_FORMAT = "<script src=\"%s\"></script>\n"

    @CheckResult
    fun loadHtmlData(webView: WebView, epub: Epub, spineReference: SpineReference, settings: EpubViewSettings): Completable {

        val webViewWeakReference = WeakReference(webView)

        return Single.fromCallable { EpubDisplayHelper.getHtml(epub, spineReference, settings) }
                .doOnSubscribe { webViewWeakReference.get()?.tag = spineReference }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSuccess { html ->
                    val webViewReference = webViewWeakReference.get() ?: return@doOnSuccess

                    var isAttachedToWindow = true
                    if (Build.VERSION.SDK_INT >= 19) {
                        isAttachedToWindow = webViewReference.isAttachedToWindow
                    }
                    if (webViewReference.tag === spineReference && isAttachedToWindow) {
                        val basePath = Uri.fromFile(epub.opfPath).toString() + "//"
                        webViewReference.loadDataWithBaseURL(
                                basePath,
                                html,
                                "text/html",
                                "UTF-8",
                                "about:chapter"
                        )
                    }
                }
                .toCompletable()
    }

    @Throws(IOException::class)
    private fun getHtml(epub: Epub, reference: SpineReference, settings: EpubViewSettings): String {

        val inputStream = epub.getResourceContent(reference.resource)

        var rawHtml = inputStream.use {
            String(IOUtil.toByteArray(inputStream), Charsets.UTF_8)
        }

        val injectBeforeBody = (buildLibraryInternalInjections()
                + injectJavascriptConstants(epub, reference)
                + buildCustomCssString(settings)
                + injectJavascriptStartCode(settings))

        val injectAfterBody = buildCustomScripsString(settings)

        // add custom scripts at the end of the body
        // this makes sure the dom tree is already present when the scripts are executed
        rawHtml = rawHtml.replaceFirst("<body>".toRegex(), "<body>" + injectBeforeBody)
        rawHtml = rawHtml.replaceFirst("</body>".toRegex(), injectAfterBody + "</body>")

        return rawHtml
    }

    private fun injectJavascriptConstants(epub: Epub, reference: SpineReference): String {
        val chapterPosition = getChapterPosition(epub, reference)

        return ("<script type=\"text/javascript\">\n"
                + "var epubChapter = {\n"
                + " index: " + chapterPosition + ",\n"
                + " id: \"" + reference.resource.id + "\",\n"
                + " href: \"" + reference.resource.href + "\",\n"
                + " title: \"" + reference.resource.title + "\"\n"
                + "}\n"
                + "</script>\n")
    }

    private fun injectJavascriptStartCode(settings: EpubViewSettings): String {

        val builder = StringBuilder()
        builder.append("<script type=\"text/javascript\">\n")

        if (!(settings.font.uri() == null && settings.font.name() == null)) {
            if (settings.font.uri() == null) {
                builder.append("setFontFamily(\"")
                        .append(settings.font.name() ?: "")
                        .append("\");\n")
            } else {
                builder.append("setFont(\"")
                        .append(settings.font.name())
                        .append("\",\"")
                        .append(settings.font.uri())
                        .append("\");\n")
            }
        }

        builder.append("</script>\n")

        return builder.toString()
    }

    private fun getChapterPosition(epub: Epub, reference: SpineReference): Int {
        val spineReferences = epub.book.spine.spineReferences
        for (i in spineReferences.indices) {
            val spineReference = spineReferences[i]

            if (spineReference.resourceId === reference.resourceId) {
                return i
            }
            if (spineReference.resourceId == reference.resourceId) {
                return i
            }
        }
        return 0
    }

    private fun buildLibraryInternalInjections(): String {
        return String.format(Locale.US, INJECT_JAVASCRIPT_FORMAT, "file:///android_asset/epubreaderandroid/helper_functions.js") +
                String.format(Locale.US, INJECT_JAVASCRIPT_FORMAT, "file:///android_asset/epubreaderandroid/script.js") +
                String.format(Locale.US, INJECT_CSS_FORMAT, "file:///android_asset/epubreaderandroid/style.css")
    }

    private fun buildCustomCssString(settings: EpubViewSettings): String {
        val builder = StringBuilder()
        for (script in settings.customChapterCss) {
            builder.append(String.format(Locale.US, INJECT_CSS_FORMAT, script))
        }
        return builder.toString()
    }

    private fun buildCustomScripsString(settings: EpubViewSettings): String {
        val builder = StringBuilder()

        for (script in settings.customChapterScripts) {
            builder.append(String.format(Locale.US, INJECT_JAVASCRIPT_FORMAT, script))
        }
        return builder.toString()
    }

}
