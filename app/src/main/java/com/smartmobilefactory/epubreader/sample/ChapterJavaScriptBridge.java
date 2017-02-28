package com.smartmobilefactory.epubreader.sample;

import android.util.Log;
import android.util.SparseArray;
import android.webkit.JavascriptInterface;

import org.json.JSONException;
import org.json.JSONObject;

public class ChapterJavaScriptBridge {

    private static final String TAG = ChapterJavaScriptBridge.class.getSimpleName();

    // in memory storage of all highlights
    private SparseArray<String> highlights = new SparseArray<>();

    /**
     * @return
     * [
     *   {
     *     id : ...,
     *     chapterId: ...,
     *     start: ...,
     *     end: ...,
     *     color: ...
     *   }
     *  ]
     */
    @JavascriptInterface
    public String getHighlights(int chapter) {
        String value = highlights.get(chapter);
        if (value == null) {
            return "[]";
        }
        return value;
    }

    @JavascriptInterface
    public void onHighlightAdded(int chapter, String data) {
        try {
            JSONObject object = new JSONObject(data);
            highlights.put(chapter, object.getJSONArray("highlights").toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "onHighlightAdded() called with " + "data = [" + data + "]");
    }

    @JavascriptInterface
    public void onHighlightClicked(String json) {
        Log.d(TAG, "onHighlightClicked() called with " + "json = [" + json + "]");
    }

    @JavascriptInterface
    public void onSelectionChanged(int length) {
        Log.d(TAG, "onSelectionChanged() called with " + "length = [" + length + "]");
    }

    public String[] getCustomChapterScripts() {
        return new String[]{
                "file:///android_asset/books/javascript/rangy/rangy-core.min.js",
                "file:///android_asset/books/javascript/rangy/rangy-classapplier.min.js",
                "file:///android_asset/books/javascript/rangy/rangy-highlighter.min.js",
                "file:///android_asset/books/javascript/rangy/rangy-serializer.min.js",
                "file:///android_asset/books/javascript/highlight.js"
        };
    }
}
