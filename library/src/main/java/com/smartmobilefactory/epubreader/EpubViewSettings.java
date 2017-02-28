package com.smartmobilefactory.epubreader;

import android.support.annotation.NonNull;

import com.smartmobilefactory.epubreader.model.EpubFont;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

public class EpubViewSettings {

    public enum Setting {
        FONT,
        FONT_SIZE,
        JAVASCRIPT_BRIDGE,
        // custom scripts, custom css
        CUSTOM_FILES
    }

    private EpubFont font = EpubFont.fromUri(null, null);
    private int fontSizeSp = 18;

    private Object javascriptBridge = new Object();
    private String[] customChapterScripts = new String[0];
    private String[] customChapterCss = new String[0];

    private PublishSubject<Setting> settingsChangedSubject = PublishSubject.create();

    EpubViewSettings() {
        // only the EpubView should construct settings
    }

    public void setFont(EpubFont font) {
        if (font == null) {
            font = EpubFont.fromUri(null, null);
        }
        if (this.font.equals(font)) {
            return;
        }
        this.font = font;
        onSettingHasChanged(Setting.FONT);
    }

    public EpubFont getFont() {
        return font;
    }

    public void setFontSizeSp(int sp) {
        if (sp == fontSizeSp) {
            return;
        }
        this.fontSizeSp = sp;
        onSettingHasChanged(Setting.FONT_SIZE);
    }

    public int getFontSizeSp() {
        return fontSizeSp;
    }

    public void setCustomChapterScript(String... customChapterScripts) {
        this.customChapterScripts = customChapterScripts;
        onSettingHasChanged(Setting.CUSTOM_FILES);
    }

    public String[] getCustomChapterScripts() {
        return customChapterScripts;
    }

    public void setJavascriptBridge(Object javascriptBridge) {
        if(javascriptBridge == this.javascriptBridge) {
            return;
        }
        this.javascriptBridge = javascriptBridge;
        onSettingHasChanged(Setting.JAVASCRIPT_BRIDGE);
    }

    public Object getJavascriptBridge() {
        return javascriptBridge;
    }

    public void setCustomChapterCss(String[] css) {
        this.customChapterCss = css;
        onSettingHasChanged(Setting.CUSTOM_FILES);
    }

    public String[] getCustomChapterCss() {
        return customChapterCss;
    }

    private void onSettingHasChanged(@NonNull Setting setting) {
        settingsChangedSubject.onNext(setting);
    }

    public Observable<Setting> anySettingHasChanged() {
        return settingsChangedSubject;
    }

}
