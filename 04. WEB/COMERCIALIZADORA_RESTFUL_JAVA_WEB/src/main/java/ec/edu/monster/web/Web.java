package ec.edu.monster.web;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.ToNumberPolicy;
import ec.edu.monster.controlador.ComercializadoraControlador;
import ec.edu.monster.servicio.ComercializadoraServicio;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Utilitarios compartidos por los servlets. */
final class Web {

    // URL del servicio REST de la Comercializadora (variable de entorno opcional)
    private static final ComercializadoraControlador CTRL = new ComercializadoraControlador(
            System.getenv().getOrDefault("COMERCIALIZADORA_URL", ComercializadoraServicio.URL_POR_DEFECTO));

    private Web() {
    }

    static ComercializadoraControlador ctrl() {
        return CTRL;
    }

    private static final Gson GSON = new GsonBuilder().setObjectToNumberStrategy(ToNumberPolicy.LAZILY_PARSED_NUMBER).create();

    /** Los modelos usan campos públicos y EL solo lee getters: se pasan a la JSP como Map/List. */
    static Object ver(Object modelo) {
        return GSON.fromJson(GSON.toJson(modelo), Object.class);
    }

    static void vista(HttpServletRequest req, HttpServletResponse resp, String jsp) throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/vistas/" + jsp + ".jsp").forward(req, resp);
    }

    /** Mensaje de una sola vez que sobrevive al redirect (patrón Post/Redirect/Get). */
    static void flash(HttpServletRequest req, String tipo, String mensaje) {
        req.getSession().setAttribute("flashTipo", tipo);
        req.getSession().setAttribute("flash", mensaje);
    }

    static Integer entero(String v) {
        try {
            return v == null || v.trim().isEmpty() ? null : Integer.valueOf(v.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
