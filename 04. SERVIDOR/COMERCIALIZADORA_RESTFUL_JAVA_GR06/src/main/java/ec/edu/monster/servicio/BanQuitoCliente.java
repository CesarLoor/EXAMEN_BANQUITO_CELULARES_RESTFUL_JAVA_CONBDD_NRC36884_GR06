package ec.edu.monster.servicio;

import ec.edu.monster.modelo.EvaluacionCredito;
import ec.edu.monster.modelo.Mensaje;
import ec.edu.monster.modelo.banco.Credito;
import ec.edu.monster.modelo.banco.MontoMaximo;
import ec.edu.monster.modelo.banco.SolicitudCredito;
import ec.edu.monster.modelo.banco.SujetoCredito;
import java.math.BigDecimal;
import java.util.List;
import java.util.function.Function;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/** Cliente REST (JAX-RS Client API) de los servicios web del Banco BanQuito. */
public class BanQuitoCliente {

    private static final String URL_BANQUITO = System.getenv().getOrDefault(
            "BANQUITO_URL", "http://localhost:8080/BANQUITO_CORE_RESTFUL_JAVA_GR06/webresources");
    private static final Client CLIENTE = ClientBuilder.newClient();

    private WebTarget creditos() {
        return CLIENTE.target(URL_BANQUITO).path("creditos");
    }

    public SujetoCredito sujetoCredito(String cedula) {
        return invocar(creditos().path("sujeto").path(cedula), t -> t.request(MediaType.APPLICATION_JSON).get(),
                r -> r.readEntity(SujetoCredito.class));
    }

    public MontoMaximo montoMaximo(int codCliente) {
        return invocar(creditos().path("monto-maximo").path(String.valueOf(codCliente)),
                t -> t.request(MediaType.APPLICATION_JSON).get(), r -> r.readEntity(MontoMaximo.class));
    }

    public Credito otorgarCredito(int codCliente, BigDecimal precio, int numeroCuotas) {
        SolicitudCredito s = new SolicitudCredito();
        s.codCliente = codCliente;
        s.precioTelefono = precio;
        s.numeroCuotas = numeroCuotas;
        return invocar(creditos(), t -> t.request(MediaType.APPLICATION_JSON).post(Entity.json(s)),
                r -> r.readEntity(Credito.class));
    }

    public Credito amortizacion(int codCredito) {
        return invocar(creditos().path(String.valueOf(codCredito)).path("amortizacion"),
                t -> t.request(MediaType.APPLICATION_JSON).get(), r -> r.readEntity(Credito.class));
    }

    public List<Credito> creditosCliente(String cedula) {
        return invocar(creditos().path("cliente").path(cedula), t -> t.request(MediaType.APPLICATION_JSON).get(),
                r -> r.readEntity(new GenericType<List<Credito>>() { }));
    }

    /** Sujeto de crédito + monto máximo: lo que la comercializadora necesita antes de vender a crédito. */
    public EvaluacionCredito evaluar(String cedula) {
        SujetoCredito s = sujetoCredito(cedula);
        EvaluacionCredito e = new EvaluacionCredito();
        e.cedula = s.cedula;
        e.codCliente = s.codCliente;
        e.nombre = s.nombre;
        e.sujetoCredito = s.sujetoCredito;
        e.motivos = s.motivos;
        e.montoMaximo = s.sujetoCredito ? montoMaximo(s.codCliente).montoMaximo : BigDecimal.ZERO;
        return e;
    }

    private <T> T invocar(WebTarget destino, Function<WebTarget, Response> llamada, Function<Response, T> lector) {
        Response r;
        try {
            r = llamada.apply(destino);
        } catch (ProcessingException e) {
            throw new NegocioException(503, "BanQuito no está disponible (" + URL_BANQUITO + ")");
        }
        try {
            if (r.getStatus() >= 400) {
                // El banco siempre responde errores en JSON; HTML = página del servidor (app deshabilitada/no desplegada)
                if (!MediaType.APPLICATION_JSON_TYPE.isCompatible(r.getMediaType())) {
                    throw new NegocioException(503, "BanQuito no está disponible (" + URL_BANQUITO + ")");
                }
                String msg = r.hasEntity() ? r.readEntity(Mensaje.class).mensaje : "Error HTTP " + r.getStatus();
                // 4xx del banco = regla de negocio (se reenvía igual); 5xx = falla del banco (502 Bad Gateway)
                throw new NegocioException(r.getStatus() < 500 ? r.getStatus() : 502, "BanQuito: " + msg);
            }
            return lector.apply(r);
        } finally {
            r.close();
        }
    }
}
