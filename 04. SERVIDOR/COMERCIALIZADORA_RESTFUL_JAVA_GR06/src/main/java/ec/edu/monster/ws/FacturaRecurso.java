package ec.edu.monster.ws;

import ec.edu.monster.modelo.SolicitudVenta;
import ec.edu.monster.servicio.FacturaServicio;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/** Facturación: venta en EFECTIVO (42% de descuento) o con CREDITO directo de BanQuito. */
@Path("facturas")
@Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
@Consumes(MediaType.APPLICATION_JSON)
public class FacturaRecurso {

    private final FacturaServicio servicio = new FacturaServicio();

    @POST
    public Response facturar(SolicitudVenta solicitud) throws Exception {
        return Response.status(Response.Status.CREATED).entity(servicio.facturar(solicitud)).build();
    }

    @GET
    public Response listar() throws Exception {
        return Response.ok(servicio.listar()).build();
    }

    @GET
    @Path("{cod}")
    public Response buscar(@PathParam("cod") int cod) throws Exception {
        return Response.ok(servicio.buscar(cod)).build();
    }
}
