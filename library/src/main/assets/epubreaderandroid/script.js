
var customFonts = {};

function setFont(name, fontUri) {
    var fontName = "custom_font" + name;

    if (fonts[fontName]) {
        setFontFamily(fontName);
        return;
    }

    var newStyle = document.createElement('style');
    newStyle.appendChild(document.createTextNode("\
        @font-face {\
            font-family: '"+ font.name + "';\
            src: url('" + font.src + "');\
        }\
    "));

    document.head.appendChild(newStyle);
    fonts[fontName] = true;
    setFontFamily(fontName);
}

function setFontFamily(font_family) {
    document.body.style.fontFamily = font_family;
}

function scrollToElementById(element){
    document.getElementById(element).scrollIntoView();
}

function scrollToElementByXPath(xpath){
    var element = document.evaluate(path, document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue;
    element.scrollIntoView();
}

function updateFirstVisibleElement() {
    var element = getFirstVisibleElement();
    var xpath = getXPathTo(element);
    internalBridge.onLocationChanged(xpath);
}

function init() {
    document.body.onscroll = function () {
        updateFirstVisibleElement();
    }
}
init();