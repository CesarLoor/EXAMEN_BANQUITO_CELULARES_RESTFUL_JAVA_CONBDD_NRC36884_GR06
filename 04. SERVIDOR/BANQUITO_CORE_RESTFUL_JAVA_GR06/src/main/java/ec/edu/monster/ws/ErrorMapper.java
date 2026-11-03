package ec.edu.monster.ws;

import ec.edu.monster.modelo.Mensaje;
import ec.edu.monster.servicio.NegocioException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.bind.JsonbException;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/** Convierte cualquier excepción en JSON {"mensaje": "..."} con el código HTTP adecuado. */
@Provider
public class ErrorMapper implements ExceptionMapper<Exception> {

    private static final Logger LOG = Logger.getLogger(ErrorMapper.class.getName());

    @Override
    public Response toResponse(Exception e) {
        int status;
        String mensaje;
        if (e instanceof NegocioException) {
            status = ((NegocioException) e).getStatus();
            mensaje = e.getMessage();
        } else if (e instanceof ProcessingException || e instanceof JsonbException) { // cuerpo JSON mal formado
            status = 400;
            mensaje = "El cuerpo de la solicitud no es un JSON válido";
        } else if (e instanceof WebApplicationException) { // 404 de ruta, JSON mal formado, etc.
            status = ((WebApplicationException) e).getResponse().getStatus();
            mensaje = status == 404 ? "Recurso no encontrado" : "Solicitud inválida";
        } else {
            LOG.log(Level.SEVERE, "Error interno", e);
            status = 500;
            mensaje = "Error interno del servidor BanQuito: " + e.getMessage();
        }
        return Response.status(status).entity(new Mensaje(mensaje))
                .type(MediaType.APPLICATION_JSON + ";charset=UTF-8").build();
    }
}
