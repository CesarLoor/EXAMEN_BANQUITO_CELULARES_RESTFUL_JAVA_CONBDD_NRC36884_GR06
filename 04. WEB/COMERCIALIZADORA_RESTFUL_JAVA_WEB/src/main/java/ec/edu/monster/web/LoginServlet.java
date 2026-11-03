package ec.edu.monster.web;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet(urlPatterns = {"/login", "/logout"})
public class LoginServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if ("/logout".equals(req.getServletPath())) {
            req.getSession().invalidate();
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        Web.vista(req, resp, "login");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String usuario = req.getParameter("usuario");
        if (Web.ctrl().login(usuario, req.getParameter("clave"))) {
            HttpSession anterior = req.getSession(false);
            if (anterior != null) {
                anterior.invalidate(); // sesión nueva al autenticarse: evita fijación de sesión
            }
            req.getSession(true).setAttribute("usuario", usuario);
            resp.sendRedirect(req.getContextPath() + "/app/venta");
        } else {
            req.setAttribute("error", "Usuario o contraseña incorrectos");
            Web.vista(req, resp, "login");
        }
    }
}
