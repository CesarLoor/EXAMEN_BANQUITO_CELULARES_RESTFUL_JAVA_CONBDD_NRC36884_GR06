<%@ include file="_cabecera.jspf" %>
<h1>Tabla de amortización de un préstamo</h1>

<form class="tarjeta fila" method="get">
  <div><label for="cedula">Cédula del cliente</label>
    <input id="cedula" name="cedula" required pattern="\d{10}" maxlength="10" value="<c:out value='${param.cedula}'/>"></div>
  <div style="flex:0"><button class="btn primario">Consultar</button></div>
</form>

<c:if test="${not empty param.cedula and empty error and empty creditos}">
  <div class="alerta ok">El cliente no tiene préstamos registrados en BanQuito.</div>
</c:if>
<c:forEach var="cr" items="${creditos}">
  <div class="tarjeta">
    <%@ include file="_tablaAmortizacion.jspf" %>
  </div>
</c:forEach>
<%@ include file="_pie.jspf" %>
