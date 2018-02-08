package com.smartmobilefactory.epubreader

enum class EpubScrollDirection {

    /**
     * make chapters internally vertical scrollable
     * but each chapter has its own page
     * the user can swipe horizontally through the chapters
     *
     * | chapter1 | | chapter2 | | chapter3 | | chapter4 |
     * | chapter1 | | chapter2 | | chapter3 | | chapter4 |
     * | chapter1 | | chapter2 |              | chapter4 |
     * | chapter1 |
     * | chapter1 |
     */
    HORIZONTAL_WITH_VERTICAL_CONTENT,

    /**
     * make all chapters vertical scrollable
     * WARNING: this mode is not well tested and experimental
     *
     * | chapter1 |
     * | chapter1 |
     * | chapter1 |
     * | chapter1 |
     * | chapter1 |
     * | chapter2 |
     * | chapter2 |
     * | chapter2 |
     * | chapter3 |
     * | chapter4 |
     */
    VERTICAL_WITH_VERTICAL_CONTENT,

    /**
     * show only a single chapter which is vertically scrollable
     *
     * | chapter1 |
     * | chapter1 |
     * | chapter1 |
     * | chapter1 |
     * | chapter1 |
     */
    SINGLE_CHAPTER_VERTICAL
}
