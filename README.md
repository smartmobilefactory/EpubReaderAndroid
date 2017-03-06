# EpubReaderAndroid

ePub-Reader for Android to make it easy integrate and customize epub-Reader functionality in any Android App


## Simple Integrate

```xml

<com.smartmobilefactory.epubreader.EpubView
    android:id="@+id/epubView"
    android:layout_width="match_parent"
    android:layout_height="match_parent"/>

```

```java

EpubView epubView = findViewById(R.id.epubView);
epubView.setEpub(epub);

```


## Display Modes

The Epub can be displayed in different modes:

- horizontal chapters + vertical content
```java

epubView.setScrollDirection(EpubScrollDirection.HORIZONTAL_WITH_VERTICAL_CONTENT);

```
- vertical chapters + vertical content
```java

epubView.setScrollDirection(EpubScrollDirection.VERTICAL_WITH_VERTICAL_CONTENT);

```
- single chapters + vertical content
```java

epubView.setScrollDirection(EpubScrollDirection.SINGLE_CHAPTER_VERTICAL);

```

More modes may be implemented later.

## Customization Settings

```java

EpubViewSettings settings = epubView.getSettings();
settings.setFont(EpubFont.fromFontFamiliy("Monospace"));
settings.setFontSizeSp(30);

// inject code into chapters
settings.setJavascriptBridge(bridge);
settings.setCustomChapterScript(...);
settings.setCustomChapterCss(...);

```

## Observe current status

observation is implemented using RxJava2

- Current Chapter
- Current Location


## Installation

    
```groovy

repositories {    
    // ...    
    maven { url "https://jitpack.io" }   
}   

dependencies {    
    compile 'com.github.smartmobilefactory:EpubReaderAndroid:XXX'
}

```

