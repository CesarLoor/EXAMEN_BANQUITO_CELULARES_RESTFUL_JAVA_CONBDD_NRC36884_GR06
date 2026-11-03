package ec.edu.monster.web;

import ec.edu.monster.modelo.SolicitudVenta;
import ec.edu.monster.servicio.ServicioException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/** Facturación: efectivo (42% desc.) o crédito directo BanQuito. */
@WebServlet("/app/venta")
public class VentaServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession s = req.getSession();
        req.setAttribute("factura", s.getAttribute("facturaEmitida")); // viene del redirect tras facturar
        s.removeAttribute("facturaEmitida");
        cargarCatalogo(req);
        Web.vista(req, resp, "venta");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String cedula = req.getParameter("cedula");
        String formaPago = "CREDITO".equals(req.getParameter("formaPago")) ? "CREDITO" : "EFECTIVO";
        try {
            if ("evaluar".equals(req.getParameter("accion"))) {
                req.setAttribute("evaluacion", Web.ver(Web.ctrl().evaluarCredito(cedula)));
            } else {
                req.getSession().setAttribute("facturaEmitida", Web.ver(Web.ctrl().facturar(cedula,
                        req.getParameter("nombre"), formaPago, Web.entero(req.getParameter("plazoMeses")), items(req))));
                // Post/Redirect/Get: recargar la página no vuelve a facturar
                resp.sendRedirect(req.getContextPath() + "/app/venta");
                return;
            }
        } catch (ServicioException e) {
            req.setAttribute("error", e.getMessage());
        }
        cargarCatalogo(req);
        Web.vista(req, resp, "venta");
    }

    /** Filas del carrito: parámetros repetidos codTelefono / cantidad. */
    private static List<SolicitudVenta.Item> items(HttpServletRequest req) {
        String[] cods = req.getParameterValues("codTelefono");
        String[] cants = req.getParameterValues("cantidad");
        List<SolicitudVenta.Item> items = new ArrayList<>();
        if (cods == null || cants == null) {
            return items;
        }
        for (int i = 0; i < Math.min(cods.length, cants.length); i++) {
            Integer cod = Web.entero(cods[i]);
            Integer cant = Web.entero(cants[i]);
            if (cod != null && cant != null && cant > 0) {
                items.add(new SolicitudVenta.Item(cod, cant));
            }
        }
        return items;
    }

    private static void cargarCatalogo(HttpServletRequest req) {
        try {
            req.setAttribute("telefonos", Web.ver(Web.ctrl().listarTelefonos(false)));
        } catch (ServicioException e) {
            req.setAttribute("error", e.getMessage());
        }
    }
}
