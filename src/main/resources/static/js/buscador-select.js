// Helpers reutilizables para los buscadores en vivo de selects/listas de formularios.

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

// Combobox de un clic: reemplaza un <select> largo por un campo de texto que abre una lista
// filtrable; al hacer clic en una fila se guarda su valor en un <input type="hidden">.
// Estructura esperada dentro de #wrapperId:
//   .combo-input   (input de texto, SIN name, es solo visual/búsqueda)
//   .combo-value   (input hidden con el name real que se envía en el form)
//   .combo-panel   (contenedor de las filas, con .combo-row hijos con data-valor/data-texto/data-buscar)
//   .combo-clear   (botón opcional para limpiar la selección)
//   .combo-empty   (mensaje opcional cuando ningún resultado coincide)
// data-selected en el wrapper precarga un valor (modo edición).
function initCombo(wrapperId) {
    var wrapper = document.getElementById(wrapperId);
    if (!wrapper) return;
    var input = wrapper.querySelector('.combo-input');
    var hidden = wrapper.querySelector('.combo-value');
    var panel = wrapper.querySelector('.combo-panel');
    var clearBtn = wrapper.querySelector('.combo-clear');
    var empty = wrapper.querySelector('.combo-empty');
    var rows = Array.prototype.slice.call(wrapper.querySelectorAll('.combo-row'));
    if (!input || !hidden || !panel) return;

    function actualizarValidez() {
        if (input.hasAttribute('required')) {
            input.setCustomValidity(hidden.value ? '' : 'Selecciona una opción de la lista.');
        }
    }

    function marcarValor(valor, texto) {
        hidden.value = valor || '';
        input.value = texto || '';
        input.classList.toggle('has-value', !!valor);
        wrapper.classList.toggle('has-value', !!valor);
        actualizarValidez();
    }

    function filtrar() {
        var q = input.value.trim().toLowerCase();
        var visibles = 0;
        rows.forEach(function (r) {
            var coincide = q === '' || (r.getAttribute('data-buscar') || '').indexOf(q) !== -1;
            r.style.display = coincide ? '' : 'none';
            if (coincide) visibles++;
        });
        if (empty) empty.style.display = visibles === 0 ? 'block' : 'none';
    }

    function abrir() { panel.classList.add('open'); filtrar(); }
    function cerrar() { panel.classList.remove('open'); }

    function filasVisibles() {
        return rows.filter(function (r) { return r.style.display !== 'none'; });
    }

    function marcarActiva(fila) {
        rows.forEach(function (r) { r.classList.remove('active'); });
        if (fila) {
            fila.classList.add('active');
            fila.scrollIntoView({ block: 'nearest' });
        }
    }

    input.addEventListener('focus', function () { abrir(); input.select(); });
    input.addEventListener('click', abrir);
    input.addEventListener('input', function () {
        if (hidden.value) marcarValor('', input.value);
        abrir();
        filtrar();
        marcarActiva(null);
    });

    rows.forEach(function (r) {
        r.addEventListener('click', function () {
            marcarValor(r.getAttribute('data-valor'), r.getAttribute('data-texto'));
            cerrar();
        });
    });

    if (clearBtn) {
        clearBtn.addEventListener('click', function (e) {
            e.stopPropagation();
            marcarValor('', '');
            input.focus();
        });
    }

    document.addEventListener('click', function (e) {
        if (!wrapper.contains(e.target)) cerrar();
    });

    input.addEventListener('keydown', function (e) {
        if (e.key === 'Escape') { cerrar(); input.blur(); return; }
        if (!panel.classList.contains('open')) return;
        var visibles = filasVisibles();
        if (!visibles.length) return;
        var idxActual = visibles.findIndex(function (r) { return r.classList.contains('active'); });

        if (e.key === 'ArrowDown') {
            e.preventDefault();
            marcarActiva(visibles[(idxActual + 1) % visibles.length]);
        } else if (e.key === 'ArrowUp') {
            e.preventDefault();
            var idxAnterior = idxActual === -1 ? 0 : idxActual;
            marcarActiva(visibles[(idxAnterior - 1 + visibles.length) % visibles.length]);
        } else if (e.key === 'Enter') {
            e.preventDefault();
            var activa = idxActual !== -1 ? visibles[idxActual] : visibles[0];
            marcarValor(activa.getAttribute('data-valor'), activa.getAttribute('data-texto'));
            cerrar();
        }
    });

    var preId = wrapper.getAttribute('data-selected');
    if (preId) {
        var match = rows.filter(function (r) { return r.getAttribute('data-valor') === preId; })[0];
        if (match) marcarValor(preId, match.getAttribute('data-texto'));
    }
    actualizarValidez();
}
