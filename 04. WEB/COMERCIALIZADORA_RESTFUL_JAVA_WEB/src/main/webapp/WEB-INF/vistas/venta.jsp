<%@ include file="_cabecera.jspf" %>
<h1>Facturación de teléfonos celulares</h1>

<c:if test="${not empty factura}">
  <div class="tarjeta">
    <h2>Factura ${factura.numFactura} emitida</h2>
    <div class="resumen">
      <span>Fecha <b>${fn:replace(factura.fecha, 'T', ' ')}</b></span>
      <span>Cliente <b><c:out value="${factura.nombreCliente}"/></b> (${factura.cedula})</span>
      <span>Pago <b>${factura.formaPago}<c:if test="${not empty factura.codCredito}"> · Crédito #${factura.codCredito} a ${factura.plazoMeses} meses</c:if></b></span>
    </div>
    <div class="tabla-scroll"><table>
      <tr><th>Cant.</th><th>Descripción</th><th class="num">P. Unitario</th><th class="num">Subtotal</th></tr>
      <c:forEach var="d" items="${factura.detalles}">
        <tr><td>${d.cantidad}</td><td><c:out value="${d.descripcion}"/></td>
          <td class="num"><fmt:formatNumber value="${d.precioUnitario}" pattern="#,##0.00"/></td>
          <td class="num"><fmt:formatNumber value="${d.subtotal}" pattern="#,##0.00"/></td></tr>
      </c:forEach>
    </table></div>
    <div class="totales">
      Subtotal: $<fmt:formatNumber value="${factura.subtotal}" pattern="#,##0.00"/><br>
      Descuento: $<fmt:formatNumber value="${factura.descuento}" pattern="#,##0.00"/><br>
      TOTAL: <b>$<fmt:formatNumber value="${factura.total}" pattern="#,##0.00"/></b>
    </div>
  </div>
  <c:if test="${not empty factura.credito}">
    <div class="tarjeta">
      <h2>Tabla de amortización del crédito</h2>
      <c:set var="cr" value="${factura.credito}"/>
      <%@ include file="_tablaAmortizacion.jspf" %>
    </div>
  </c:if>
</c:if>

<form method="post" action="${ctx}/app/venta" id="frmVenta">
  <div class="tarjeta">
    <h2>Teléfonos</h2>
    <div class="tabla-scroll">
    <table id="carrito">
      <tr><th>Teléfono</th><th style="width:110px">Cantidad</th><th style="width:60px"></th></tr>
      <%-- Se conservan las filas al verificar el crédito o si hubo un error --%>
      <c:if test="${empty factura}">
        <c:forEach var="cod" items="${paramValues.codTelefono}" varStatus="s">
          <tr><td><select name="codTelefono">
                <c:forEach var="t" items="${telefonos}">
                  <option value="${t.codTelefono}" ${t.codTelefono == cod ? 'selected' : ''}><c:out value="${t.marca} ${t.modelo}"/> - $${t.precio}</option>
                </c:forEach></select></td>
              <td><input name="cantidad" type="number" min="1" max="99" value="${paramValues.cantidad[s.index]}"></td>
              <td><button type="button" class="btn chico peligro" onclick="quitar(this)">✕</button></td></tr>
        </c:forEach>
      </c:if>
    </table>
    </div>
    <p><button type="button" class="btn" onclick="agregar()">+ Agregar teléfono</button></p>
    <template id="filaTpl">
      <tr><td><select name="codTelefono">
            <c:forEach var="t" items="${telefonos}">
              <option value="${t.codTelefono}" data-precio="${t.precio}"><c:out value="${t.marca} ${t.modelo}"/> - $${t.precio}</option>
            </c:forEach></select></td>
          <td><input name="cantidad" type="number" min="1" max="99" value="1"></td>
          <td><button type="button" class="btn chico peligro" onclick="quitar(this)">✕</button></td></tr>
    </template>
  </div>

  <div class="rejilla">
    <div class="tarjeta">
      <h2>Cliente</h2>
      <label for="cedula">Cédula</label>
      <input id="cedula" name="cedula" required pattern="\d{10}" maxlength="10" value="<c:out value='${empty factura ? param.cedula : ""}'/>">
      <label for="nombre">Nombre (solo cliente nuevo en efectivo)</label>
      <input id="nombre" name="nombre" value="<c:out value='${empty factura ? param.nombre : ""}'/>">
    </div>
    <div class="tarjeta">
      <h2>Forma de pago</h2>
      <c:set var="fp" value="${empty param.formaPago or not empty factura ? 'EFECTIVO' : param.formaPago}"/>
      <div class="opciones">
        <label><input type="radio" name="formaPago" value="EFECTIVO" ${fp == 'EFECTIVO' ? 'checked' : ''} onchange="modo()"> Efectivo (42% de descuento)</label>
        <label><input type="radio" name="formaPago" value="CREDITO" ${fp == 'CREDITO' ? 'checked' : ''} onchange="modo()"> Crédito directo BanQuito</label>
      </div>
      <div id="bloqueCredito">
        <label for="plazo">Plazo en meses (3 - 18)</label>
        <input id="plazo" name="plazoMeses" type="number" min="3" max="18" value="${empty param.plazoMeses or not empty factura ? 6 : param.plazoMeses}">
        <p><button class="btn" name="accion" value="evaluar" formnovalidate>Verificar sujeto de crédito</button></p>
        <c:if test="${not empty evaluacion}">
          <c:choose>
            <c:when test="${evaluacion.sujetoCredito}">
              <div class="alerta ok"><c:out value="${evaluacion.nombre}"/> es sujeto de crédito. Monto máximo aprobado:
                <b>$<fmt:formatNumber value="${evaluacion.montoMaximo}" pattern="#,##0.00"/></b></div>
            </c:when>
            <c:otherwise>
              <div class="alerta error">No es sujeto de crédito:
                <c:forEach var="m" items="${evaluacion.motivos}"><br>• <c:out value="${m}"/></c:forEach></div>
            </c:otherwise>
          </c:choose>
        </c:if>
      </div>
      <div class="totales" id="totales"></div>
      <p style="text-align:right"><button class="btn exito" name="accion" value="facturar">FACTURAR</button></p>
    </div>
  </div>
</form>

<script>
  const carrito = document.getElementById('carrito');
  function agregar() { carrito.appendChild(document.getElementById('filaTpl').content.cloneNode(true)); totales(); }
  function quitar(b) { b.closest('tr').remove(); totales(); }
  function credito() { return document.querySelector('input[name=formaPago]:checked').value === 'CREDITO'; }
  function modo() {
    document.getElementById('bloqueCredito').style.display = credito() ? '' : 'none';
    document.getElementById('nombre').disabled = credito();
    totales();
  }
  // Vista previa: el valor oficial lo calcula el servidor al facturar
  function totales() {
    let sub = 0;
    carrito.querySelectorAll('tr').forEach(tr => {
      const sel = tr.querySelector('select'), cant = tr.querySelector('input');
      if (sel && cant) sub += parseFloat(sel.selectedOptions[0].textContent.split('$').pop()) * (parseInt(cant.value) || 0);
    });
    const desc = credito() ? 0 : sub * 0.42, f = v => v.toLocaleString('en-US', {minimumFractionDigits: 2, maximumFractionDigits: 2});
    document.getElementById('totales').innerHTML = 'Subtotal: $' + f(sub) + '<br>Descuento: $' + f(desc) + '<br>TOTAL: <b>$' + f(sub - desc) + '</b>';
  }
  carrito.addEventListener('input', totales);
  carrito.addEventListener('change', totales);
  if (carrito.querySelectorAll('tr').length === 1) agregar();
  modo();
</script>
<%@ include file="_pie.jspf" %>
