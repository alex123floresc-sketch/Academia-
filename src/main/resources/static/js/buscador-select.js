// Helpers reutilizables para los buscadores en vivo de selects/listas de formularios.

// Filtra las <option> de un <select> a partir de lo escrito en un <input type="search">.
function initFiltroSelect(inputId, selectId) {
    var buscar = document.getElementById(inputId);
    var select = document.getElementById(selectId);
    if (!buscar || !select) return;
    var opciones = Array.prototype.slice.call(select.options).filter(function (o) { return o.value !== ''; });
    buscar.addEventListener('input', function () {
        var q = buscar.value.trim().toLowerCase();
        opciones.forEach(function (o) { o.hidden = q !== '' && o.text.toLowerCase().indexOf(q) === -1; });
    });
}

// Filtra las filas de una lista (p.ej. un checklist de cursos) a partir de un atributo data-buscar,
// y opcionalmente mantiene un contador de cuántas están marcadas (checkboxes con la clase dada).
function initFiltroLista(inputId, listaId, filaSelector, emptyId, checkboxClass, contadorId) {
    var buscar = document.getElementById(inputId);
    var lista = document.getElementById(listaId);
    if (!lista) return;
    var vacio = emptyId ? document.getElementById(emptyId) : null;
    var filas = Array.prototype.slice.call(lista.querySelectorAll(filaSelector));

    if (buscar) {
        buscar.addEventListener('input', function () {
            var q = buscar.value.trim().toLowerCase();
            var visibles = 0;
            filas.forEach(function (fila) {
                var coincide = q === '' || (fila.getAttribute('data-buscar') || '').indexOf(q) !== -1;
                fila.style.display = coincide ? '' : 'none';
                if (coincide) visibles++;
            });
            if (vacio) vacio.style.display = visibles === 0 ? 'block' : 'none';
        });
    }

    if (checkboxClass && contadorId) {
        var contador = document.getElementById(contadorId);
        var checks = Array.prototype.slice.call(lista.querySelectorAll('.' + checkboxClass));
        function actualizarContador() {
            var n = checks.filter(function (c) { return c.checked; }).length;
            contador.textContent = n === 0 ? 'Ningún curso seleccionado.' : (n === 1 ? '1 curso seleccionado.' : n + ' cursos seleccionados.');
        }
        checks.forEach(function (c) { c.addEventListener('change', actualizarContador); });
        actualizarContador();
    }
}
