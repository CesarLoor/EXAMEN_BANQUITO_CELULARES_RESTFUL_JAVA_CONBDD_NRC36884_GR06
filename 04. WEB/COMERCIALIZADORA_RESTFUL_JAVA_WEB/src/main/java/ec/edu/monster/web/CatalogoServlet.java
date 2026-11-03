package ec.edu.monster.web;

import ec.edu.monster.servicio.ServicioException;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Mantenimiento del catálogo de teléfonos. */
@WebServlet("/app/catalogo")
public class CatalogoServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            req.setAttribute("telefonos", Web.ver(Web.ctrl().listarTelefonos(true)));
        } catch (ServicioException e) {
            req.setAttribute("error", e.getMessage());
        }
        Web.vista(req, resp, "catalogo");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            if ("baja".equals(req.getParameter("accion"))) {
                Web.ctrl().eliminarTelefono(Web.entero(req.getParameter("codTelefono")));
                Web.flash(req, "ok", "Teléfono dado de baja");
            } else {
                Web.ctrl().guardarTelefono(Web.entero(req.getParameter("codTelefono")), req.getParameter("marca"),
                        req.getParameter("modelo"), req.getParameter("precio"), req.getParameter("activo") != null);
                Web.flash(req, "ok", "Teléfono guardado correctamente");
            }
        } catch (ServicioException | NullPointerException e) {
            Web.flash(req, "error", e instanceof ServicioException ? e.getMessage() : "Datos incompletos");
        }
        resp.sendRedirect(req.getContextPath() + "/app/catalogo");
    }
}
