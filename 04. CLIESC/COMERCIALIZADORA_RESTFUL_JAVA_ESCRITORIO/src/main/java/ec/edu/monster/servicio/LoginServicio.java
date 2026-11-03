package ec.edu.monster.servicio;

/** Acceso al sistema (mismas credenciales de los proyectos anteriores del GR06). */
public class LoginServicio {

    private static final String USUARIO = "MONSTER";
    private static final String CLAVE = "MONSTER69";

    public boolean login(String usuario, String clave) {
        return USUARIO.equals(usuario) && CLAVE.equals(clave);
    }
}
