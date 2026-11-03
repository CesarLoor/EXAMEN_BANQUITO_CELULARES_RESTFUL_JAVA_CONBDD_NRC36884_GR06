<%@ include file="_cabecera.jspf" %>
<h1>Catálogo de teléfonos celulares</h1>

<div class="tarjeta">
  <p><button class="btn primario" onclick="editar()">+ Nuevo teléfono</button></p>
  <div class="tabla-scroll">
  <table>
    <tr><th>Código</th><th>Marca</th><th>Modelo</th><th class="num">Precio ($)</th><th>Estado</th><th></th></tr>
    <c:forEach var="t" items="${telefonos}">
      <tr class="${t.activo ? '' : 'inactivo'}">
        <td>${t.codTelefono}</td><td><c:out value="${t.marca}"/></td><td><c:out value="${t.modelo}"/></td>
        <td class="num"><fmt:formatNumber value="${t.precio}" pattern="#,##0.00"/></td>
        <td><span class="etiqueta ${t.activo ? 'verde' : 'roja'}">${t.activo ? 'A la venta' : 'De baja'}</span></td>
        <td style="white-space:nowrap">
          <button class="btn chico" data-cod="${t.codTelefono}" data-marca="<c:out value='${t.marca}'/>"
                  data-modelo="<c:out value='${t.modelo}'/>" data-precio="${t.precio}" data-activo="${t.activo}"
                  onclick="editar(this.dataset)">Editar</button>
          <c:if test="${t.activo}">
            <form method="post" style="display:inline" onsubmit="return confirm('¿Dar de baja este teléfono?')">
              <input type="hidden" name="accion" value="baja"><input type="hidden" name="codTelefono" value="${t.codTelefono}">
              <button class="btn chico peligro">Dar de baja</button>
            </form>
          </c:if>
        </td>
      </tr>
    </c:forEach>
  </table>
  </div>
</div>

<dialog id="dlg">
  <form method="post">
    <h2 id="dlgTitulo">Nuevo teléfono</h2>
    <input type="hidden" name="codTelefono" id="fCod">
    <label for="fMarca">Marca</label><input id="fMarca" name="marca" required maxlength="50">
    <label for="fModelo">Modelo</label><input id="fModelo" name="modelo" required maxlength="100">
    <label for="fPrecio">Precio ($)</label><input id="fPrecio" name="precio" type="number" step="0.01" min="0.01" required>
    <div class="opciones"><label><input type="checkbox" name="activo" id="fActivo" checked> A la venta</label></div>
    <p style="text-align:right"><button type="button" class="btn" onclick="dlg.close()">Cancelar</button>
       <button class="btn primario">Guardar</button></p>
  </form>
</dialog>
<script>
  const dlg = document.getElementById('dlg');
  function editar(d) {
    d = d || {};
    document.getElementById('dlgTitulo').textContent = d.cod ? 'Editar teléfono #' + d.cod : 'Nuevo teléfono';
    document.getElementById('fCod').value = d.cod || '';
    document.getElementById('fMarca').value = d.marca || '';
    document.getElementById('fModelo').value = d.modelo || '';
    document.getElementById('fPrecio').value = d.precio || '';
    document.getElementById('fActivo').checked = d.activo !== 'false';
    dlg.showModal();
  }
</script>
<%@ include file="_pie.jspf" %>
