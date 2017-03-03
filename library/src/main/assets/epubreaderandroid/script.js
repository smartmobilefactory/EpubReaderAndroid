
var customFonts = {};

function setFont(name, fontUri) {
    var fontName = "custom_font" + name;

    if (customFonts[fontName]) {
        setFontFamily(fontName);
        return;
    }

    var newStyle = document.createElement('style');
    newStyle.appendChild(document.createTextNode("\
        @font-face {\
            font-family: '"+ fontName + "';\
            src: url('" + fontUri + "');\
        }\
    "));

    document.head.appendChild(newStyle);
    customFonts[fontName] = true;
    setFontFamily(fontName);
}

function setFontFamily(font_family) {
    document.body.style.fontFamily = font_family;
}

function scrollToElementById(element){
    var element = document.getElementById(element);
    scrollElementIntoView(element);
}

function scrollToElementByXPath(xpath){
    var element = document.evaluate(xpath, document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue;
    scrollElementIntoView(element);
}

function scrollToRangeStart(start) {
    var element = getElementFromRangeStart(start);
    scrollElementIntoView(element);
}

function scrollElementIntoView(element) {
    if (element.children.length > 0) {
        // in some cases it is more accurate to scroll to the first children
        element = element.children[0];
    }
    // scrollIntoView(TOP)
    element.scrollIntoView(true);
}

function updateFirstVisibleElementByTopPosition(top) {
    var element = document.elementFromPoint(100, top);
    if (!element) {
        return;
    }
    var xpath = getXPathTo(element);
    internalBridge.onLocationChanged(xpath);
}

function updateFirstVisibleElement() {
    var element = getFirstVisibleElement(0);
    if (!element) {
        return;
    }
    var xpath = getXPathTo(element);
    internalBridge.onLocationChanged(xpath);
}

function getYPositionOfElementWithId(id) {
    var element = document.getElementById(element);
    publishResultGetYPositionOfElement(element);
}

function getYPositionOfElementWithXPath(xpath) {
    var element = document.evaluate(xpath, document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue;
    publishResultGetYPositionOfElement(element);
}

function getYPositionOfElementFromRangeStart(start) {
    var element = getElementFromRangeStart(start);
    publishResultGetYPositionOfElement(element);
}

function publishResultGetYPositionOfElement(element) {
    if (!element) {
        console.log("element not found");
       return;
    }
    var rect = element.getBoundingClientRect();
    if (!rect) {
        console.log("element position not found");
       return;
    }
    internalBridge.resultGetYPositionOfElement(rect.top);
}