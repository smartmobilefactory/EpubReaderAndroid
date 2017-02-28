var highlighter;
var initialDoc;

var highlighter;

function init() {
    if (!highlighter) {
        rangy.init();
        highlighter = rangy.createHighlighter();
    }

    document.addEventListener("selectionchange", function(e) {
        bridge.onSelectionChanged(window.getSelection().toString().length);
    }, false);

}
init();

/**
 * deserialize and format the highlight from database to usable once for rangy
 */
function reloadHighlights() {
    var json = bridge.getHighlights(epubChapter.index);
    var data = JSON.parse(json);

    // remove all highlights before adding new once
    highlighter.removeAllHighlights();

    var serializedHighlights = ["type:textContent"]
    for (var i=0; i < data.length; i++){
        var highlight = data[i];

        if (highlight.chapterId != epubChapter.index) {
            continue;
        }

        var parts = [
            highlight.start,
            highlight.end,
            highlight.id,
            getColorClass(highlight.color),
            ''
        ];

        serializedHighlights.push( parts.join("$") );
    }

    var serializedData = serializedHighlights.join("|");
    highlighter.deserialize(serializedData);
}

/**
 * highlight the current selected text
 * @param color hex formatted rgb color
 */
function highlightSelectedText(color) {
    init();

    var colorClass = getColorClass(color);
    var newHighlight = highlighter.highlightSelection(colorClass)[0];

    var node = newHighlight.getHighlightElements()[0];

    var x = 0;
    var y = 0;
    if (node && node.getBoundingClientRect) {
        var position = node.getBoundingClientRect();
        x = position.left;
        y = position.top;
    }
    var data = {
        x: x,
        y: y,
        highlights: getAllHighlightsSerialized()
    };

    bridge.onHighlightAdded(epubChapter.index, JSON.stringify(data));
}

function getAllHighlightsSerialized() {

    var serializedHighlights = [];

    var highlights = highlighter.highlights;
    for (i = 0; i < highlights.length; i++) {
        var highlight = highlights[i];
        serializedHighlights.push(serializeHighlight(highlight));
    }

    return serializedHighlights;
}

function serializeHighlight(highlight) {
    var color = "#" + highlight.classApplier.className.replace("highlight_", "");

    var serialized = {
        id : highlight.id,
        chapterId: epubChapter.index,
        start: highlight.characterRange.start,
        end: highlight.characterRange.end,
        text: highlight.getText(),
        color: color
    }
    return serialized;
}

function getColorClass(color) {
    var className = "highlight_" + color.replace("#", "");

    // check if class already exists
    for (var i=0; i < document.styleSheets.length; i++){
        var styleSheet = document.styleSheets[i];
        var rules = styleSheet.rules || styleSheet.cssRules;
        for(var x in rules) {
            if(rules[x].selectorText == className) {
                return className;
            }
        }
    }

    // class does not exists

    var style = document.createElement('style');
    style.type = 'text/css';
    style.innerHTML = '.' + className + ' { background-color: ' + color +'; }';
    document.getElementsByTagName('head')[0].appendChild(style);

    highlighter.addClassApplier(rangy.createClassApplier(className, {
        ignoreWhiteSpace: true,
        tagNames: ["span", "a"],
        elementProperties: {
            href: "#",
            onclick: function(event) {
                var highlight = highlighter.getHighlightForElement(this);
                var position = event.target.getBoundingClientRect();
                var data = {
                    x: position.left,
                    y: position.top,
                    highlight: serializeHighlight(highlight)
                };
                bridge.onHighlightClicked(JSON.stringify(data));
                return false;
            }
        }
    }));

    return className;
}

reloadHighlights();
