package ec.edu.monster.web;

import ec.edu.monster.servicio.ServicioException;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Consultas: tabla de amortización por cédula y listado de facturas. */
@WebServlet(urlPatterns = {"/app/amortizacion", "/app/facturas"})
public class ConsultaServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        boolean amortizacion = "/app/amortizacion".equals(req.getServletPath());
        try {
            if (amortizacion) {
                String cedula = req.getParameter("cedula");
                if (cedula != null && !cedula.trim().isEmpty()) {
                    req.setAttribute("creditos", Web.ver(Web.ctrl().creditosCliente(cedula)));
                }
            } else {
                req.setAttribute("facturas", Web.ver(Web.ctrl().listarFacturas()));
            }
        } catch (ServicioException e) {
            req.setAttribute("error", e.getMessage());
        }
        Web.vista(req, resp, amortizacion ? "amortizacion" : "facturas");
    }
}
