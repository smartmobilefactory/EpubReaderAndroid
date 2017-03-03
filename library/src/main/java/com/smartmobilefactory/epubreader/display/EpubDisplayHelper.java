package com.smartmobilefactory.epubreader.display;

import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.webkit.WebView;

import com.smartmobilefactory.epubreader.EpubViewSettings;
import com.smartmobilefactory.epubreader.model.Epub;
import com.smartmobilefactory.epubreader.utils.BaseDisposableObserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Locale;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import nl.siegmann.epublib.domain.SpineReference;
import nl.siegmann.epublib.util.IOUtil;

public class EpubDisplayHelper {

    private static String INJECT_CSS_FORMAT = "<link rel=\"stylesheet\" type=\"text/css\" href=\"%s\">\n";
    private static String INJECT_JAVASCRIPT_FORMAT = "<script src=\"%s\"></script>\n";

    public static BaseDisposableObserver loadHtmlData(WebView webView, Epub epub, SpineReference spineReference, EpubViewSettings settings) {

        WeakReference<WebView> webViewWeakReference = new WeakReference<>(webView);

        return Single.fromCallable(() -> EpubDisplayHelper.getHtml(epub, spineReference, settings))
                .doOnSubscribe(disposable -> {
                    webViewWeakReference.get().setTag(spineReference);
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSuccess(html -> {

                    WebView webViewReference = webViewWeakReference.get();
                    if (webViewReference != null && webViewReference.getTag() == spineReference) {
                        String basePath = Uri.fromFile(epub.getOpfPath()).toString() + "//";
                        webViewReference.loadDataWithBaseURL(
                                basePath,
                                html,
                                "text/html",
                                "UTF-8",
                                null
                        );
                    }
                })
                .subscribeWith(new BaseDisposableObserver<>());
    }

    private static String getHtml(Epub epub, SpineReference reference, EpubViewSettings settings) throws IOException {

        File file = new File(epub.getOpfPath(), reference.getResource().getHref());
        FileInputStream inputStream = new FileInputStream(file);

        String rawHtml;
        try {
            rawHtml = new String(IOUtil.toByteArray(inputStream), "UTF-8");
        } finally {
            inputStream.close();
        }

        String injectBeforeBody = buildLibraryInternalInjections()
                + injectJavascriptConstants(epub, reference)
                + buildCustomCssString(settings)
                + injectJavascriptStartCode(settings);

        String injectAfterBody = buildCustomScripsString(settings);

        // add custom scripts at the end of the body
        // this makes sure the dom tree is already present when the scripts are executed
        rawHtml = rawHtml.replaceFirst("<body>", "<body>" + injectBeforeBody);
        rawHtml = rawHtml.replaceFirst("</body>", injectAfterBody + "</body>");

        return rawHtml;
    }

    private static String injectJavascriptConstants(Epub epub, SpineReference reference) {
        int chapterPosition = getChapterPosition(epub, reference);

        return "<script type=\"text/javascript\">\n"
                + "var epubChapter = {\n"
                + " index: " + chapterPosition + ",\n"
                + " id: \"" + reference.getResource().getId() + "\",\n"
                + " href: \"" + reference.getResource().getHref() + "\",\n"
                + " title: \"" + reference.getResource().getTitle() + "\"\n"
                + "}\n"
                + "</script>\n";
    }

    private static String injectJavascriptStartCode(EpubViewSettings settings) {

        StringBuilder builder = new StringBuilder();
        builder.append("<script type=\"text/javascript\">\n");

        if (!(settings.getFont().uri() == null && settings.getFont().name() == null)) {
            if (settings.getFont().uri() == null) {
                builder.append("setFontFamily(\"")
                        .append(settings.getFont().name())
                        .append("\");\n");
            } else {
                builder.append("setFont(\"")
                        .append(settings.getFont().name())
                        .append("\",\"")
                        .append(settings.getFont().uri())
                        .append("\");\n");
            }
        }

        builder.append("</script>\n");

        return builder.toString();
    }

    private static int getChapterPosition(Epub epub, SpineReference reference) {
        List<SpineReference> spineReferences = epub.getBook().getSpine().getSpineReferences();
        for (int i = 0; i < spineReferences.size(); i++) {
            SpineReference spineReference = spineReferences.get(i);
            //noinspection StringEquality
            if (spineReference.getResourceId() == reference.getResourceId()) {
                return i;
            }
            if (spineReference.getResourceId().equals(reference.getResourceId())) {
                return i;
            }
        }
        return 0;
    }

    private static String buildLibraryInternalInjections() {
        return String.format(Locale.US, INJECT_JAVASCRIPT_FORMAT, "file:///android_asset/epubreaderandroid/helper_functions.js") +
                String.format(Locale.US, INJECT_JAVASCRIPT_FORMAT, "file:///android_asset/epubreaderandroid/script.js") +
                String.format(Locale.US, INJECT_CSS_FORMAT, "file:///android_asset/epubreaderandroid/style.css");
    }

    private static String buildCustomCssString(EpubViewSettings settings) {
        StringBuilder builder = new StringBuilder();
        for (String script : settings.getCustomChapterCss()) {
            builder.append(String.format(Locale.US, INJECT_CSS_FORMAT, script));
        }
        return builder.toString();
    }

    private static String buildCustomScripsString(EpubViewSettings settings) {
        StringBuilder builder = new StringBuilder();

        for (String script : settings.getCustomChapterScripts()) {
            builder.append(String.format(Locale.US, INJECT_JAVASCRIPT_FORMAT, script));
        }
        return builder.toString();
    }

}
