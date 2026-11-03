<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Acceso · Comercializadora de Celulares</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/estilos.css">
</head>
<body>
<div class="login">
  <div class="cab"><b>Comercializadora de Celulares</b>Crédito directo BanQuito · GR06</div>
  <form class="tarjeta" method="post" action="${pageContext.request.contextPath}/login">
    <c:if test="${not empty error}"><div class="alerta error"><c:out value="${error}"/></div></c:if>
    <label for="usuario">Usuario</label>
    <input id="usuario" name="usuario" required autofocus value="<c:out value='${param.usuario}'/>">
    <label for="clave">Contraseña</label>
    <input id="clave" name="clave" type="password" required>
    <p><button class="btn primario" style="width:100%">Ingresar</button></p>
  </form>
</div>
</body>
</html>
