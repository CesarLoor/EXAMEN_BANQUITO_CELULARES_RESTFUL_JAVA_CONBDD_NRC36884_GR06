package ec.edu.monster.ws;

import ec.edu.monster.modelo.Credito;
import ec.edu.monster.modelo.SolicitudCredito;
import ec.edu.monster.servicio.CreditoServicio;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Servicios web REST del Módulo de Crédito de BanQuito.
 * Los errores de negocio y de BDD los traduce {@link ErrorMapper} a {"mensaje": "..."}.
 */
@Path("creditos")
@Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
@Consumes(MediaType.APPLICATION_JSON)
public class CreditoRecurso {

    private final CreditoServicio servicio = new CreditoServicio();

    /** WS 1: ¿la persona es sujeto de crédito? (por cédula) */
    @GET
    @Path("sujeto/{cedula}")
    public Response sujetoCredito(@PathParam("cedula") String cedula) throws Exception {
        return Response.ok(servicio.verificarSujetoCredito(cedula)).build();
    }

    /** WS 2: monto máximo de crédito (por código de cliente) */
    @GET
    @Path("monto-maximo/{codCliente}")
    public Response montoMaximo(@PathParam("codCliente") int codCliente) throws Exception {
        return Response.ok(servicio.calcularMontoMaximo(codCliente)).build();
    }

    /** WS 3: otorga el crédito y registra la tabla de amortización */
    @POST
    public Response otorgar(SolicitudCredito solicitud) throws Exception {
        Credito credito = servicio.otorgarCredito(solicitud);
        return Response.status(Response.Status.CREATED).entity(credito).build();
    }

    /** WS 4: tabla de amortización de un crédito otorgado */
    @GET
    @Path("{codCredito}/amortizacion")
    public Response amortizacion(@PathParam("codCredito") int codCredito) throws Exception {
        return Response.ok(servicio.consultarCredito(codCredito)).build();
    }

    /** Créditos (con su tabla) de un cliente, por cédula: lo usa la Comercializadora para consultar */
    @GET
    @Path("cliente/{cedula}")
    public Response creditosCliente(@PathParam("cedula") String cedula) throws Exception {
        return Response.ok(servicio.creditosPorCedula(cedula)).build();
    }
}
