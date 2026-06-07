(function () {
    'use strict';

    var toggle = document.getElementById('stock-notify-toggle');
    var panel = document.getElementById('stock-notify-panel');
    if (!toggle || !panel) {
        return;
    }

    function closePanel() {
        panel.hidden = true;
        toggle.setAttribute('aria-expanded', 'false');
    }

    function openPanel() {
        panel.hidden = false;
        toggle.setAttribute('aria-expanded', 'true');
    }

    toggle.addEventListener('click', function (ev) {
        ev.stopPropagation();
        if (panel.hidden) {
            openPanel();
        } else {
            closePanel();
        }
    });

    document.addEventListener('click', function (ev) {
        if (!panel.hidden && !panel.contains(ev.target) && ev.target !== toggle && !toggle.contains(ev.target)) {
            closePanel();
        }
    });

    document.addEventListener('keydown', function (ev) {
        if (ev.key === 'Escape') {
            closePanel();
        }
    });
})();
