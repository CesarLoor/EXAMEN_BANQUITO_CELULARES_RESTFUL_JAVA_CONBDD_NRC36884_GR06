package ec.edu.monster.servicio;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import ec.edu.monster.modelo.Credito;
import ec.edu.monster.modelo.EvaluacionCredito;
import ec.edu.monster.modelo.Factura;
import ec.edu.monster.modelo.Mensaje;
import ec.edu.monster.modelo.SolicitudVenta;
import ec.edu.monster.modelo.Telefono;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Consumo del servicio REST de la Comercializadora (que a su vez usa los servicios de BanQuito).
 * Mismo código en los 4 clientes (consola, escritorio, web y móvil): solo usa HttpURLConnection + Gson.
 */
public class ComercializadoraServicio {

    public static final String URL_POR_DEFECTO = "http://localhost:8080/COMERCIALIZADORA/api";

    private final Gson gson = new Gson();
    private final String urlBase;

    public ComercializadoraServicio(String urlBase) {
        this.urlBase = urlBase.endsWith("/") ? urlBase.substring(0, urlBase.length() - 1) : urlBase;
    }

    // ------------------------------------------------------------ catálogo
    public List<Telefono> listarTelefonos(boolean incluirInactivos) {
        return enviar("GET", "/telefonos?todos=" + incluirInactivos, null, new TypeToken<List<Telefono>>() { }.getType());
    }

    public Telefono crearTelefono(Telefono t) {
        return enviar("POST", "/telefonos", t, Telefono.class);
    }

    public Telefono actualizarTelefono(Telefono t) {
        return enviar("PUT", "/telefonos/" + t.codTelefono, t, Telefono.class);
    }

    public void eliminarTelefono(int codTelefono) {
        enviar("DELETE", "/telefonos/" + codTelefono, null, Void.class);
    }

    // ------------------------------------------------------------ facturación
    public EvaluacionCredito evaluarCredito(String cedula) {
        return enviar("GET", "/creditos/evaluacion/" + cedula, null, EvaluacionCredito.class);
    }

    public Factura facturar(SolicitudVenta venta) {
        return enviar("POST", "/facturas", venta, Factura.class);
    }

    public List<Factura> listarFacturas() {
        return enviar("GET", "/facturas", null, new TypeToken<List<Factura>>() { }.getType());
    }

    // ------------------------------------------------------------ amortización
    public List<Credito> creditosCliente(String cedula) {
        return enviar("GET", "/creditos/cliente/" + cedula, null, new TypeToken<List<Credito>>() { }.getType());
    }

    public Credito amortizacion(int codCredito) {
        return enviar("GET", "/creditos/" + codCredito + "/amortizacion", null, Credito.class);
    }

    // ------------------------------------------------------------ HTTP
    private <T> T enviar(String metodo, String ruta, Object cuerpo, Type tipo) {
        HttpURLConnection cn = null;
        try {
            cn = (HttpURLConnection) new URL(urlBase + ruta).openConnection();
            cn.setRequestMethod(metodo);
            cn.setConnectTimeout(5000);
            cn.setReadTimeout(20000);
            cn.setRequestProperty("Accept", "application/json");
            if (cuerpo != null) {
                cn.setDoOutput(true);
                cn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                try (OutputStream os = cn.getOutputStream()) {
                    os.write(gson.toJson(cuerpo).getBytes(StandardCharsets.UTF_8));
                }
            }
            int status = cn.getResponseCode();
            String json = leer(status >= 400 ? cn.getErrorStream() : cn.getInputStream());
            if (status >= 400) {
                throw new ServicioException(mensajeDeError(json, status));
            }
            if (tipo == Void.class || json.isEmpty()) {
                return null;
            }
            return gson.fromJson(json, tipo);
        } catch (IOException e) {
            throw new ServicioException("No se pudo conectar con el servidor (" + urlBase + ")");
        } catch (JsonSyntaxException e) {
            throw new ServicioException("Respuesta inválida del servidor");
        } finally {
            if (cn != null) {
                cn.disconnect();
            }
        }
    }

    private String mensajeDeError(String json, int status) {
        try {
            Mensaje m = gson.fromJson(json, Mensaje.class);
            if (m != null && m.mensaje != null) {
                return m.mensaje;
            }
        } catch (JsonSyntaxException ignorada) {
            // el cuerpo no era JSON (p. ej. página de error del servidor)
        }
        return "Error HTTP " + status;
    }

    private static String leer(InputStream in) throws IOException {
        if (in == null) {
            return "";
        }
        try (InputStream is = in) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buf = new byte[4096];
            int n;
            while ((n = is.read(buf)) != -1) {
                out.write(buf, 0, n);
            }
            return new String(out.toByteArray(), StandardCharsets.UTF_8).trim();
        }
    }
}
