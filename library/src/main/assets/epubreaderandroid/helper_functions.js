if (!document.caretRangeFromPoint) {
    // implementation when not supported in this webView version
    document.caretRangeFromPoint = function(x, y) {
        var log = "";

        function inRect(x, y, rect) {
            return x >= rect.left && x <= rect.right && y >= rect.top && y <= rect.bottom;
        }

        function inObject(x, y, object) {
            var rects = object.getClientRects();
            for (var i = rects.length; i--;)
                if (inRect(x, y, rects[i]))
                    return true;
            return false;
        }

        function getTextNodes(node, x, y) {
            if (!inObject(x, y, node))
                return [];

            var result = [];
            node = node.firstChild;
            while (node) {
                if (node.nodeType == 3)
                    result.push(node);
                if (node.nodeType == 1)
                    result = result.concat(getTextNodes(node, x, y));

                node = node.nextSibling;
            }

            return result;
        }

        var element = document.elementFromPoint(x, y);
        var nodes = getTextNodes(element, x, y);
        if (!nodes.length)
            return null;
        var node = nodes[0];

        var range = document.createRange();
        range.setStart(node, 0);
        range.setEnd(node, 1);

        for (var i = nodes.length; i--;) {
            var node = nodes[i],
                text = node.nodeValue;


            range = document.createRange();
            range.setStart(node, 0);
            range.setEnd(node, text.length);

            if (!inObject(x, y, range))
                continue;

            for (var j = text.length; j--;) {
                if (text.charCodeAt(j) <= 32)
                    continue;

                range = document.createRange();
                range.setStart(node, j);
                range.setEnd(node, j + 1);

                if (inObject(x, y, range)) {
                    range.setEnd(node, j);
                    return range;
                }
            }
        }

        return range;
    };
}

function getFirstVisibleElement() {
    // TODO check if this works on all webview versions
    var range = document.caretRangeFromPoint(0, 0);
    return element = range.startContainer.parentNode;
}

function getXPathTo(element) {

    if (element.id!=='') {
        return 'id("'+element.id+'")';
    }

    if (element===document.body) {
        return element.tagName;
    }

    var ix= 0;
    var siblings= element.parentNode.childNodes;
    for (var i= 0; i<siblings.length; i++) {
        var sibling= siblings[i];

        if (sibling===element) {
            return getXPathTo(element.parentNode)+'/'+element.tagName+'['+(ix+1)+']';
        }

        if (sibling.nodeType===1 && sibling.tagName===element.tagName) {
            ix++;
        }
    }
}