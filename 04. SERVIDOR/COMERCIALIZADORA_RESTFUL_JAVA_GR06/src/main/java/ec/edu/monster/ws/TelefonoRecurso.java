package ec.edu.monster.ws;

import ec.edu.monster.modelo.Telefono;
import ec.edu.monster.servicio.TelefonoServicio;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/** Mantenimiento (CRUD) del catálogo de teléfonos celulares. */
@Path("telefonos")
@Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
@Consumes(MediaType.APPLICATION_JSON)
public class TelefonoRecurso {

    private final TelefonoServicio servicio = new TelefonoServicio();

    /** ?todos=true incluye los dados de baja. */
    @GET
    public Response listar(@QueryParam("todos") @DefaultValue("false") boolean todos) throws Exception {
        return Response.ok(servicio.listar(todos)).build();
    }

    @GET
    @Path("{cod}")
    public Response buscar(@PathParam("cod") int cod) throws Exception {
        return Response.ok(servicio.buscar(cod)).build();
    }

    @POST
    public Response crear(Telefono t) throws Exception {
        return Response.status(Response.Status.CREATED).entity(servicio.crear(t)).build();
    }

    @PUT
    @Path("{cod}")
    public Response actualizar(@PathParam("cod") int cod, Telefono t) throws Exception {
        return Response.ok(servicio.actualizar(cod, t)).build();
    }

    @DELETE
    @Path("{cod}")
    public Response eliminar(@PathParam("cod") int cod) throws Exception {
        servicio.eliminar(cod);
        return Response.noContent().build();
    }
}
