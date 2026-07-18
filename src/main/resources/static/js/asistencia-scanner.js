(function () {
    var horarioId = window.ASISTENCIA_HORARIO_ID;
    var statusEl = document.getElementById('scan-status');
    var tbody = document.getElementById('lista-registrados');
    var csrfMeta = document.querySelector('meta[name="_csrf"]');
    var csrfHeaderMeta = document.querySelector('meta[name="_csrf_header"]');
    var csrfToken = csrfMeta ? csrfMeta.getAttribute('content') : null;
    var csrfHeader = csrfHeaderMeta ? csrfHeaderMeta.getAttribute('content') : null;

    var procesando = false;

    function mostrarEstado(ok, mensaje) {
        statusEl.style.display = 'block';
        statusEl.textContent = mensaje;
        statusEl.className = ok ? 'alert-success' : 'alert-error';
    }

    function agregarFila(nombre, hora) {
        var emptyRow = document.getElementById('empty-row');
        if (emptyRow) emptyRow.remove();
        var tr = document.createElement('tr');
        var tdNombre = document.createElement('td');
        tdNombre.textContent = nombre;
        var tdHora = document.createElement('td');
        tdHora.textContent = hora;
        tr.appendChild(tdNombre);
        tr.appendChild(tdHora);
        tbody.insertBefore(tr, tbody.firstChild);
    }

    function onScanSuccess(decodedText) {
        if (procesando) return;
        procesando = true;

        var headers = { 'Content-Type': 'application/json' };
        if (csrfHeader && csrfToken) headers[csrfHeader] = csrfToken;

        fetch('/asistencias/registrar', {
            method: 'POST',
            headers: headers,
            body: JSON.stringify({ horarioId: horarioId, codigo: decodedText })
        })
            .then(function (resp) { return resp.json(); })
            .then(function (data) {
                mostrarEstado(data.ok, data.mensaje);
                if (data.ok) {
                    var ahora = new Date();
                    var hh = String(ahora.getHours()).padStart(2, '0');
                    var mm = String(ahora.getMinutes()).padStart(2, '0');
                    agregarFila(data.alumnoNombre, hh + ':' + mm);
                }
            })
            .catch(function () {
                mostrarEstado(false, 'Error de conexión al registrar la asistencia.');
            })
            .finally(function () {
                setTimeout(function () { procesando = false; }, 2000);
            });
    }

    function onScanFailure() {
        // Se ignoran los frames en los que no se detecta ningún QR.
    }

    var scanner = new Html5QrcodeScanner('reader', { fps: 10, qrbox: 250 }, false);
    scanner.render(onScanSuccess, onScanFailure);
})();
