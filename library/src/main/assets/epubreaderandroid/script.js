
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
    var contents = document.querySelector("body");

    var sprint = function(root, func) {
        var node;
        // iterate over all text nodes
        var treeWalker = document.createTreeWalker(root, NodeFilter.SHOW_TEXT, null, false);
        while ((node = treeWalker.nextNode())) {
            if (!func(node)) {
                break;
            }
        }
    };

    var counter = 0;
    sprint(contents, function(node) {
        var len = node.length;
        var dist;
        var pos = 0;
        if ((counter + len) > start) {
            console.log("range found");

            var element = node.parentElement.parentElement;
            while(element.children[0]) {
                element = element.children[0];
            }
            scrollElementIntoView(element);
            return false;
        }
        counter = counter + len;
        return true;
    });
}

function scrollElementIntoView(element) {
    if (element.children.length > 0) {
        // in some cases it is more accurate to scroll to the first children
        element = element.children[0];
    }
    // scrollIntoView(TOP)
    element.scrollIntoView(true);
}

function updateFirstVisibleElement() {
    var element = getFirstVisibleElement();
    var xpath = getXPathTo(element);
    internalBridge.onLocationChanged(xpath);
}