package ec.edu.monster.servicio;

import ec.edu.monster.db.Conexion;
import ec.edu.monster.modelo.DetalleFactura;
import ec.edu.monster.modelo.EvaluacionCredito;
import ec.edu.monster.modelo.Factura;
import ec.edu.monster.modelo.SolicitudVenta;
import ec.edu.monster.modelo.Telefono;
import ec.edu.monster.modelo.banco.Credito;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Facturación de la comercializadora.
 *  - EFECTIVO: 42% de descuento sobre el precio de venta.
 *  - CREDITO : invoca a BanQuito (sujeto de crédito -> monto máximo -> otorgar crédito).
 */
public class FacturaServicio {

    public static final String EFECTIVO = "EFECTIVO";
    public static final String CREDITO = "CREDITO";

    private final BanQuitoCliente banco = new BanQuitoCliente();

    public Factura facturar(SolicitudVenta s) throws SQLException {
        validar(s);
        String formaPago = s.formaPago.trim().toUpperCase();

        try (Connection cn = Conexion.obtener()) {
            cn.setAutoCommit(false);
            try {
                // 1. Detalle con precios del catálogo (nunca se confía en precios enviados por el cliente)
                Factura f = new Factura();
                f.cedula = s.cedula;
                f.formaPago = formaPago;
                f.subtotal = BigDecimal.ZERO;
                for (SolicitudVenta.Item it : s.items) {
                    Telefono t = telefonoActivo(cn, it.codTelefono);
                    DetalleFactura d = new DetalleFactura();
                    d.codTelefono = t.codTelefono;
                    d.descripcion = t.marca + " " + t.modelo;
                    d.cantidad = it.cantidad;
                    d.precioUnitario = t.precio;
                    d.subtotal = t.precio.multiply(BigDecimal.valueOf(it.cantidad));
                    f.detalles.add(d);
                    f.subtotal = f.subtotal.add(d.subtotal);
                }
                BigDecimal porcDescuento = porcentajeDescuento(cn, formaPago);
                f.descuento = f.subtotal.multiply(porcDescuento).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                f.total = f.subtotal.subtract(f.descuento);

                // 2. Crédito directo: el banco decide
                EvaluacionCredito ev = null;
                if (CREDITO.equals(formaPago)) {
                    ev = banco.evaluar(s.cedula);
                    if (!ev.sujetoCredito) {
                        throw new NegocioException(422, "El cliente no es sujeto de crédito en BanQuito: "
                                + String.join("; ", ev.motivos));
                    }
                    if (f.total.compareTo(ev.montoMaximo) > 0) {
                        throw new NegocioException(422, "El valor $" + f.total
                                + " supera el monto máximo de crédito aprobado $" + ev.montoMaximo);
                    }
                    f.plazoMeses = s.plazoMeses;
                }

                // 3. Cliente de la comercializadora (para crédito el nombre oficial viene del banco)
                String nombre = ev != null ? ev.nombre : s.nombre;
                int codCliente = clienteId(cn, s.cedula, nombre);

                // 4. Factura + detalle (aún sin confirmar)
                f.codFactura = insertarFactura(cn, f, codCliente);
                f.numFactura = String.format("001-001-%09d", f.codFactura);
                for (DetalleFactura d : f.detalles) {
                    insertarDetalle(cn, f.codFactura, d);
                }

                // 5. Se pide el crédito al final: si algo falló antes no queda un crédito huérfano en el banco.
                //    shortcut: si el COMMIT falla después de que el banco aprobó, el crédito quedaría sin factura;
                //    en producción se usaría un proceso de compensación/reintento.
                if (ev != null) {
                    Credito credito = banco.otorgarCredito(ev.codCliente, f.total, s.plazoMeses);
                    f.codCredito = credito.codCredito;
                    f.credito = credito;
                }
                try (PreparedStatement ps = cn.prepareStatement(
                        "UPDATE FACTURA SET NUM_FACTURA = ?, COD_CREDITO = ? WHERE COD_FACTURA = ?")) {
                    ps.setString(1, f.numFactura);
                    ps.setObject(2, f.codCredito);
                    ps.setInt(3, f.codFactura);
                    ps.executeUpdate();
                }
                cn.commit();
                Factura guardada = buscar(cn, f.codFactura);
                guardada.credito = f.credito;
                return guardada;
            } catch (SQLException | RuntimeException e) {
                cn.rollback();
                throw e;
            }
        }
    }

    public List<Factura> listar() throws SQLException {
        try (Connection cn = Conexion.obtener()) {
            List<Integer> ids = new ArrayList<>();
            try (PreparedStatement ps = cn.prepareStatement("SELECT COD_FACTURA FROM FACTURA ORDER BY COD_FACTURA DESC");
                    ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ids.add(rs.getInt(1));
                }
            }
            List<Factura> lista = new ArrayList<>();
            for (int id : ids) {
                lista.add(buscar(cn, id));
            }
            return lista;
        }
    }

    public Factura buscar(int codFactura) throws SQLException {
        try (Connection cn = Conexion.obtener()) {
            Factura f = buscar(cn, codFactura);
            if (f == null) {
                throw new NegocioException(404, "No existe la factura " + codFactura);
            }
            if (f.codCredito != null) {
                f.credito = banco.amortizacion(f.codCredito);
            }
            return f;
        }
    }

    // ------------------------------------------------------------------ validaciones y SQL
    private void validar(SolicitudVenta s) {
        if (s == null) {
            throw new NegocioException(400, "Debe enviar los datos de la venta");
        }
        if (s.cedula == null || !s.cedula.trim().matches("\\d{10}")) {
            throw new NegocioException(400, "La cédula debe tener 10 dígitos");
        }
        s.cedula = s.cedula.trim();
        if (s.formaPago == null || !(EFECTIVO.equalsIgnoreCase(s.formaPago.trim()) || CREDITO.equalsIgnoreCase(s.formaPago.trim()))) {
            throw new NegocioException(400, "Forma de pago inválida: use EFECTIVO o CREDITO");
        }
        if (CREDITO.equalsIgnoreCase(s.formaPago.trim()) && (s.plazoMeses == null || s.plazoMeses < 3 || s.plazoMeses > 18)) {
            throw new NegocioException(400, "Para crédito directo el plazo debe estar entre 3 y 18 meses");
        }
        if (s.items == null || s.items.isEmpty()) {
            throw new NegocioException(400, "La factura debe tener al menos un teléfono");
        }
        for (SolicitudVenta.Item it : s.items) {
            if (it == null || it.codTelefono == null || it.cantidad == null || it.cantidad < 1) {
                throw new NegocioException(400, "Cada ítem necesita codTelefono y una cantidad mayor a 0");
            }
        }
    }

    private Telefono telefonoActivo(Connection cn, int cod) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement("SELECT * FROM TELEFONO WHERE COD_TELEFONO = ? AND ACTIVO = TRUE")) {
            ps.setInt(1, cod);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new NegocioException(404, "El teléfono " + cod + " no existe o no está a la venta");
                }
                return TelefonoServicio.mapear(rs);
            }
        }
    }

    private BigDecimal porcentajeDescuento(Connection cn, String formaPago) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement("SELECT PORC_DESCUENTO FROM FORMA_PAGO WHERE COD_FORMA_PAGO = ?")) {
            ps.setString(1, formaPago);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new NegocioException(400, "Forma de pago no configurada: " + formaPago);
                }
                return rs.getBigDecimal(1);
            }
        }
    }

    private int clienteId(Connection cn, String cedula, String nombre) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement("SELECT COD_CLIENTE FROM CLIENTE WHERE CEDULA = ?")) {
            ps.setString(1, cedula);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        if (nombre == null || nombre.isBlank()) {
            throw new NegocioException(400, "Cliente nuevo: debe ingresar el nombre");
        }
        try (PreparedStatement ps = cn.prepareStatement("INSERT INTO CLIENTE (CEDULA, NOMBRE) VALUES (?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, cedula);
            ps.setString(2, nombre.trim());
            ps.executeUpdate();
            try (ResultSet k = ps.getGeneratedKeys()) {
                k.next();
                return k.getInt(1);
            }
        }
    }

    private int insertarFactura(Connection cn, Factura f, int codCliente) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement("INSERT INTO FACTURA (NUM_FACTURA, COD_CLIENTE, COD_FORMA_PAGO, "
                + "SUBTOTAL, DESCUENTO, TOTAL, PLAZO_MESES) VALUES (?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
            // número temporal único; se reemplaza por 001-001-<código> dentro de la misma transacción
            ps.setString(1, UUID.randomUUID().toString().substring(0, 17));
            ps.setInt(2, codCliente);
            ps.setString(3, f.formaPago);
            ps.setBigDecimal(4, f.subtotal);
            ps.setBigDecimal(5, f.descuento);
            ps.setBigDecimal(6, f.total);
            ps.setObject(7, f.plazoMeses);
            ps.executeUpdate();
            try (ResultSet k = ps.getGeneratedKeys()) {
                k.next();
                return k.getInt(1);
            }
        }
    }

    private void insertarDetalle(Connection cn, int codFactura, DetalleFactura d) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement("INSERT INTO DETALLE_FACTURA (COD_FACTURA, COD_TELEFONO, CANTIDAD, "
                + "PRECIO_UNITARIO, SUBTOTAL) VALUES (?, ?, ?, ?, ?)")) {
            ps.setInt(1, codFactura);
            ps.setInt(2, d.codTelefono);
            ps.setInt(3, d.cantidad);
            ps.setBigDecimal(4, d.precioUnitario);
            ps.setBigDecimal(5, d.subtotal);
            ps.executeUpdate();
        }
    }

    private Factura buscar(Connection cn, int codFactura) throws SQLException {
        Factura f;
        try (PreparedStatement ps = cn.prepareStatement("SELECT f.*, c.CEDULA, c.NOMBRE FROM FACTURA f "
                + "JOIN CLIENTE c ON c.COD_CLIENTE = f.COD_CLIENTE WHERE f.COD_FACTURA = ?")) {
            ps.setInt(1, codFactura);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                f = new Factura();
                f.codFactura = rs.getInt("COD_FACTURA");
                f.numFactura = rs.getString("NUM_FACTURA");
                f.fecha = rs.getTimestamp("FECHA").toLocalDateTime();
                f.cedula = rs.getString("CEDULA");
                f.nombreCliente = rs.getString("NOMBRE");
                f.formaPago = rs.getString("COD_FORMA_PAGO");
                f.subtotal = rs.getBigDecimal("SUBTOTAL");
                f.descuento = rs.getBigDecimal("DESCUENTO");
                f.total = rs.getBigDecimal("TOTAL");
                f.codCredito = (Integer) rs.getObject("COD_CREDITO");
                f.plazoMeses = (Integer) rs.getObject("PLAZO_MESES");
            }
        }
        try (PreparedStatement ps = cn.prepareStatement("SELECT d.*, t.MARCA, t.MODELO FROM DETALLE_FACTURA d "
                + "JOIN TELEFONO t ON t.COD_TELEFONO = d.COD_TELEFONO WHERE d.COD_FACTURA = ? ORDER BY d.COD_DETALLE")) {
            ps.setInt(1, codFactura);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DetalleFactura d = new DetalleFactura();
                    d.codTelefono = rs.getInt("COD_TELEFONO");
                    d.descripcion = rs.getString("MARCA") + " " + rs.getString("MODELO");
                    d.cantidad = rs.getInt("CANTIDAD");
                    d.precioUnitario = rs.getBigDecimal("PRECIO_UNITARIO");
                    d.subtotal = rs.getBigDecimal("SUBTOTAL");
                    f.detalles.add(d);
                }
            }
        }
        return f;
    }
}
