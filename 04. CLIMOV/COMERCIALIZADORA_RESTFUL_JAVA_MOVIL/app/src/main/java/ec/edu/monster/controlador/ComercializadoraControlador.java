package ec.edu.monster.controlador;

import ec.edu.monster.modelo.Credito;
import ec.edu.monster.modelo.EvaluacionCredito;
import ec.edu.monster.modelo.Factura;
import ec.edu.monster.modelo.SolicitudVenta;
import ec.edu.monster.modelo.Telefono;
import ec.edu.monster.servicio.ComercializadoraServicio;
import ec.edu.monster.servicio.LoginServicio;
import ec.edu.monster.servicio.ServicioException;
import java.math.BigDecimal;
import java.util.List;

/**
 * Controlador: valida lo que ingresa el usuario en la vista y delega al servicio REST.
 * Lanza ServicioException con un mensaje listo para mostrar.
 */
public class ComercializadoraControlador {

    private final ComercializadoraServicio servicio;
    private final LoginServicio login = new LoginServicio();

    public ComercializadoraControlador(String urlServidor) {
        this.servicio = new ComercializadoraServicio(urlServidor);
    }

    public boolean login(String usuario, String clave) {
        return login.login(usuario, clave);
    }

    // ------------------------------------------------------------ catálogo
    public List<Telefono> listarTelefonos(boolean incluirInactivos) {
        return servicio.listarTelefonos(incluirInactivos);
    }

    public Telefono guardarTelefono(Integer codTelefono, String marca, String modelo, String precioTexto, boolean activo) {
        if (vacio(marca) || vacio(modelo)) {
            throw new ServicioException("Marca y modelo son obligatorios");
        }
        Telefono t = new Telefono();
        t.codTelefono = codTelefono;
        t.marca = marca.trim();
        t.modelo = modelo.trim();
        t.precio = numeroPositivo(precioTexto, "El precio");
        t.activo = activo;
        return codTelefono == null ? servicio.crearTelefono(t) : servicio.actualizarTelefono(t);
    }

    public void eliminarTelefono(int codTelefono) {
        servicio.eliminarTelefono(codTelefono);
    }

    // ------------------------------------------------------------ facturación
    public EvaluacionCredito evaluarCredito(String cedula) {
        return servicio.evaluarCredito(cedula(cedula));
    }

    /** formaPago: EFECTIVO o CREDITO. nombre solo es obligatorio para clientes nuevos en efectivo. */
    public Factura facturar(String cedula, String nombre, String formaPago, Integer plazoMeses,
            List<SolicitudVenta.Item> items) {
        SolicitudVenta v = new SolicitudVenta();
        v.cedula = cedula(cedula);
        v.nombre = vacio(nombre) ? null : nombre.trim();
        v.formaPago = formaPago;
        if ("CREDITO".equals(formaPago)) {
            if (plazoMeses == null || plazoMeses < 3 || plazoMeses > 18) {
                throw new ServicioException("El plazo debe estar entre 3 y 18 meses");
            }
            v.plazoMeses = plazoMeses;
        }
        if (items == null || items.isEmpty()) {
            throw new ServicioException("Agregue al menos un teléfono");
        }
        v.items = items;
        return servicio.facturar(v);
    }

    public List<Factura> listarFacturas() {
        return servicio.listarFacturas();
    }

    // ------------------------------------------------------------ amortización
    public List<Credito> creditosCliente(String cedula) {
        return servicio.creditosCliente(cedula(cedula));
    }

    public Credito amortizacion(int codCredito) {
        return servicio.amortizacion(codCredito);
    }

    // ------------------------------------------------------------ validaciones
    private static String cedula(String cedula) {
        if (cedula == null || !cedula.trim().matches("\\d{10}")) {
            throw new ServicioException("La cédula debe tener 10 dígitos");
        }
        return cedula.trim();
    }

    private static BigDecimal numeroPositivo(String texto, String campo) {
        try {
            BigDecimal v = new BigDecimal(texto.trim().replace(',', '.'));
            if (v.signum() <= 0) {
                throw new ServicioException(campo + " debe ser mayor a 0");
            }
            return v;
        } catch (NumberFormatException | NullPointerException e) {
            throw new ServicioException(campo + " debe ser un número válido");
        }
    }

    private static boolean vacio(String s) {
        return s == null || s.trim().isEmpty();
    }
}
