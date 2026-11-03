<%@ include file="_cabecera.jspf" %>
<h1>Facturas emitidas</h1>
<div class="tarjeta tabla-scroll">
  <table>
    <tr><th>Número</th><th>Fecha</th><th>Cliente</th><th>Detalle</th><th>Pago</th>
        <th class="num">Subtotal</th><th class="num">Descuento</th><th class="num">Total</th></tr>
    <c:forEach var="f" items="${facturas}">
      <tr><td>${f.numFactura}</td><td>${fn:replace(f.fecha, 'T', ' ')}</td>
        <td><c:out value="${f.nombreCliente}"/><br><small>${f.cedula}</small></td>
        <td><c:forEach var="d" items="${f.detalles}">${d.cantidad} x <c:out value="${d.descripcion}"/><br></c:forEach></td>
        <td><span class="etiqueta ${f.formaPago == 'CREDITO' ? '' : 'verde'}">${f.formaPago}</span>
          <c:if test="${not empty f.codCredito}"><br><small>Crédito #${f.codCredito} · ${f.plazoMeses} meses</small></c:if></td>
        <td class="num"><fmt:formatNumber value="${f.subtotal}" pattern="#,##0.00"/></td>
        <td class="num"><fmt:formatNumber value="${f.descuento}" pattern="#,##0.00"/></td>
        <td class="num"><b><fmt:formatNumber value="${f.total}" pattern="#,##0.00"/></b></td></tr>
    </c:forEach>
    <c:if test="${empty facturas and empty error}"><tr><td colspan="8">Aún no hay facturas.</td></tr></c:if>
  </table>
</div>
<%@ include file="_pie.jspf" %>
