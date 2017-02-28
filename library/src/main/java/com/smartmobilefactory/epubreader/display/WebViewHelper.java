package com.smartmobilefactory.epubreader.display;

import android.os.Build;
import android.support.annotation.MainThread;
import android.webkit.WebView;

import com.google.gson.Gson;

public class WebViewHelper {

    private Gson gson = new Gson();
    private WebView webView;

    public WebViewHelper(WebView webView) {
        this.webView = webView;
    }

    public void setGson(Gson gson) {
        if (gson == null) {
            throw new IllegalArgumentException("gson is null");
        }
        this.gson = gson;
    }

    public void callJavaScriptMethod(String method, Object... args) {
        executeCommand(createJsMethodCall(method, args));
    }

    /**
     * creates javascript method call
     */
    private String createJsMethodCall(String method, Object... args) {
        StringBuilder builder = new StringBuilder(method.length() + args.length * 32)
                .append(method).append('(');
        for (int i = 0, argsLength = args.length; i < argsLength; i++) {
            if (i != 0) {
                builder.append(',');
            }
            Object arg = args[i];
            builder.append(gson.toJson(arg));
        }
        builder.append(')');
        return builder.toString();
    }

    @MainThread
    private void executeCommand(String javascriptCommand) {
        javascriptCommand = "javascript:" + javascriptCommand;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            webView.evaluateJavascript(javascriptCommand, null);
        } else {
            webView.loadUrl(javascriptCommand);
        }
    }


}
