
function initFiltroLista(inputId, listaId, filaSelector, emptyId, checkboxClass, contadorId, extraFiltroFn, etiqueta) {
    var buscar = document.getElementById(inputId);
    var lista = document.getElementById(listaId);
    if (!lista) return { refiltrar: function () {}, actualizarContador: function () {} };
    var vacio = emptyId ? document.getElementById(emptyId) : null;
    var filas = Array.prototype.slice.call(lista.querySelectorAll(filaSelector));
    var palabra = etiqueta || 'curso';
    var contador = contadorId ? document.getElementById(contadorId) : null;
    var checks = checkboxClass ? Array.prototype.slice.call(lista.querySelectorAll('.' + checkboxClass)) : [];

    function actualizarContador() {
        if (!contador) return;
        var n = checks.filter(function (c) { return c.checked; }).length;
        contador.textContent = n === 0 ? ('Ningún ' + palabra + ' seleccionado.')
            : (n === 1 ? ('1 ' + palabra + ' seleccionado.') : (n + ' ' + palabra + 's seleccionados.'));
    }

    function aplicarFiltro() {
        var q = buscar ? buscar.value.trim().toLowerCase() : '';
        var visibles = 0;
        filas.forEach(function (fila) {
            var coincideTexto = q === '' || (fila.getAttribute('data-buscar') || '').indexOf(q) !== -1;
            var coincideExtra = !extraFiltroFn || extraFiltroFn(fila);
            var coincide = coincideTexto && coincideExtra;
            fila.style.display = coincide ? '' : 'none';
            if (coincide) visibles++;
        });
        if (vacio) vacio.style.display = visibles === 0 ? 'block' : 'none';
    }

    if (buscar) buscar.addEventListener('input', aplicarFiltro);
    checks.forEach(function (c) { c.addEventListener('change', actualizarContador); });
    aplicarFiltro();
    actualizarContador();

    return { refiltrar: aplicarFiltro, actualizarContador: actualizarContador, filas: filas, checks: checks };
}

function initCombo(wrapperId, extraFiltroFn) {
    var wrapper = document.getElementById(wrapperId);
    if (!wrapper) return;
    var input = wrapper.querySelector('.combo-input');
    var hidden = wrapper.querySelector('.combo-value');
    var panel = wrapper.querySelector('.combo-panel');
    var clearBtn = wrapper.querySelector('.combo-clear');
    var empty = wrapper.querySelector('.combo-empty');
    var rows = Array.prototype.slice.call(wrapper.querySelectorAll('.combo-row'));
    if (!input || !hidden || !panel) return { refiltrar: function () {}, limpiar: function () {} };

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
            var coincideTexto = q === '' || (r.getAttribute('data-buscar') || '').indexOf(q) !== -1;
            var coincideExtra = !extraFiltroFn || extraFiltroFn(r);
            var coincide = coincideTexto && coincideExtra;
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
    filtrar();

    return {
        refiltrar: filtrar,
        limpiar: function () { marcarValor('', ''); }
    };
}
