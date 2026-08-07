(function () {
    document.querySelectorAll('[data-view-switch]').forEach(function (switcher) {
        var group = switcher.getAttribute('data-view-switch');
        var storageKey = 'vista:' + group;
        var buttons = switcher.querySelectorAll('[data-view]');
        var panels = document.querySelectorAll('[data-view-panel][data-view-group="' + group + '"]');

        function aplicar(vista) {
            buttons.forEach(function (b) {
                b.classList.toggle('active', b.getAttribute('data-view') === vista);
            });
            panels.forEach(function (p) {
                p.hidden = p.getAttribute('data-view-panel') !== vista;
            });
            try { localStorage.setItem(storageKey, vista); } catch (e) { /* almacenamiento no disponible */ }
        }

        var guardada = null;
        try { guardada = localStorage.getItem(storageKey); } catch (e) { /* almacenamiento no disponible */ }
        aplicar(guardada === 'grid' ? 'grid' : 'list');

        buttons.forEach(function (b) {
            b.addEventListener('click', function () { aplicar(b.getAttribute('data-view')); });
        });
    });
})();
