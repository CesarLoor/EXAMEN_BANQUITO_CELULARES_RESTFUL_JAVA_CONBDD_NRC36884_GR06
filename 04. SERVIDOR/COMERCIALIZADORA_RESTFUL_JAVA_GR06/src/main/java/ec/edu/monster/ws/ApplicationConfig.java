package ec.edu.monster.ws;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

/** Todos los recursos REST de la Comercializadora quedan bajo /COMERCIALIZADORA/api/... */
@ApplicationPath("api")
public class ApplicationConfig extends Application {
}
