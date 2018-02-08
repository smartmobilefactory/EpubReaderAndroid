package com.smartmobilefactory.epubreader;

public interface UrlInterceptor {
    boolean shouldOverrideUrlLoading(String url);
}
