package ec.edu.monster.ws;

import ec.edu.monster.servicio.BanQuitoCliente;
import ec.edu.monster.servicio.NegocioException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/** Consultas de crédito que la Comercializadora hace a BanQuito para sus clientes. */
@Path("creditos")
@Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
public class CreditoRecurso {

    private final BanQuitoCliente banco = new BanQuitoCliente();

    /** ¿Es sujeto de crédito? + monto máximo (antes de vender a crédito). */
    @GET
    @Path("evaluacion/{cedula}")
    public Response evaluar(@PathParam("cedula") String cedula) {
        return Response.ok(banco.evaluar(validarCedula(cedula))).build();
    }

    /** Préstamos del cliente con su tabla de amortización. */
    @GET
    @Path("cliente/{cedula}")
    public Response creditosCliente(@PathParam("cedula") String cedula) {
        return Response.ok(banco.creditosCliente(validarCedula(cedula))).build();
    }

    /** Tabla de amortización de un crédito otorgado. */
    @GET
    @Path("{codCredito}/amortizacion")
    public Response amortizacion(@PathParam("codCredito") int codCredito) {
        return Response.ok(banco.amortizacion(codCredito)).build();
    }

    private static String validarCedula(String cedula) {
        if (cedula == null || !cedula.matches("\\d{10}")) {
            throw new NegocioException(400, "La cédula debe tener 10 dígitos");
        }
        return cedula;
    }
}
