package ec.edu.monster.servicio;

import ec.edu.monster.db.Conexion;
import ec.edu.monster.modelo.Credito;
import ec.edu.monster.modelo.CuotaAmortizacion;
import ec.edu.monster.modelo.MontoMaximo;
import ec.edu.monster.modelo.SolicitudCredito;
import ec.edu.monster.modelo.SujetoCredito;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

/** Lógica del Módulo de Crédito de BanQuito (JDBC sobre la BDD del Core). */
public class CreditoServicio {

    private static final BigDecimal PORCENTAJE = new BigDecimal("0.30");
    private static final BigDecimal MESES = new BigDecimal("6");
    private static final int EDAD_MINIMA_HOMBRES = 25;

    // ------------------------------------------------------------------ 1. SUJETO DE CRÉDITO
    public SujetoCredito verificarSujetoCredito(String cedula) throws SQLException {
        try (Connection cn = Conexion.obtener()) {
            return verificarSujetoCredito(cn, buscarClientePorCedula(cn, cedula, false), cedula);
        }
    }

    private SujetoCredito verificarSujetoCredito(Connection cn, ClienteBanco c, String cedula) throws SQLException {
        SujetoCredito r = new SujetoCredito();
        r.cedula = cedula;
        // Regla 1: debe ser cliente del banco
        if (c == null) {
            r.motivos.add("La persona no es cliente del banco");
            return r;
        }
        r.codCliente = c.codigo;
        r.nombre = c.nombre;
        // Regla 2: al menos un depósito en el último mes
        if (contar(cn, "SELECT COUNT(*) FROM MOVIMIENTO m JOIN CUENTA cu ON cu.NUM_CUENTA = m.NUM_CUENTA "
                + "WHERE cu.COD_CLIENTE = ? AND m.TIPO = 'DEP' AND m.FECHA >= DATE_SUB(CURDATE(), INTERVAL 1 MONTH)",
                c.codigo) == 0) {
            r.motivos.add("No registra depósitos en el último mes");
        }
        // Regla 3: si es hombre, mínimo 25 años
        int edad = Period.between(c.fechaNacimiento, LocalDate.now()).getYears();
        if ("M".equals(c.genero) && edad < EDAD_MINIMA_HOMBRES) {
            r.motivos.add("Cliente de género masculino menor de 25 años (" + edad + " años)");
        }
        // Regla 4: sin crédito activo
        if (contar(cn, "SELECT COUNT(*) FROM CREDITO WHERE COD_CLIENTE = ? AND ESTADO = 'ACTIVO'", c.codigo) > 0) {
            r.motivos.add("Tiene un crédito activo en el banco");
        }
        r.sujetoCredito = r.motivos.isEmpty();
        return r;
    }

    // ------------------------------------------------------------------ 2. MONTO MÁXIMO
    public MontoMaximo calcularMontoMaximo(int codCliente) throws SQLException {
        try (Connection cn = Conexion.obtener()) {
            return calcularMontoMaximo(cn, codCliente);
        }
    }

    private MontoMaximo calcularMontoMaximo(Connection cn, int codCliente) throws SQLException {
        ClienteBanco c = buscarClientePorCodigo(cn, codCliente);
        if (c == null) {
            throw new NegocioException(404, "No existe el cliente con código " + codCliente);
        }
        MontoMaximo m = new MontoMaximo();
        m.codCliente = codCliente;
        m.nombre = c.nombre;
        BigDecimal depositos = promedio(cn, codCliente, "DEP");
        BigDecimal retiros = promedio(cn, codCliente, "RET");
        // ((Promedio Depósitos – Promedio Retiros) * 30%) * 6  (se redondea solo el resultado final)
        BigDecimal monto = depositos.subtract(retiros).multiply(PORCENTAJE).multiply(MESES);
        m.montoMaximo = monto.max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
        m.promedioDepositos = depositos.setScale(2, RoundingMode.HALF_UP);
        m.promedioRetiros = retiros.setScale(2, RoundingMode.HALF_UP);
        return m;
    }

    private BigDecimal promedio(Connection cn, int codCliente, String tipo) throws SQLException {
        String sql = "SELECT COALESCE(AVG(m.VALOR), 0) FROM MOVIMIENTO m JOIN CUENTA cu ON cu.NUM_CUENTA = m.NUM_CUENTA "
                + "WHERE cu.COD_CLIENTE = ? AND m.TIPO = ? AND m.FECHA >= DATE_SUB(CURDATE(), INTERVAL 3 MONTH)";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, codCliente);
            ps.setString(2, tipo);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getBigDecimal(1);
            }
        }
    }

    // ------------------------------------------------------------------ 3. OTORGAR CRÉDITO
    public Credito otorgarCredito(SolicitudCredito s) throws SQLException {
        if (s == null || s.codCliente == null || s.precioTelefono == null || s.numeroCuotas == null) {
            throw new NegocioException(400, "Debe enviar codCliente, precioTelefono y numeroCuotas");
        }
        if (s.precioTelefono.signum() <= 0) {
            throw new NegocioException(400, "El precio del teléfono debe ser mayor a 0");
        }
        if (s.numeroCuotas < CalculadoraAmortizacion.PLAZO_MINIMO || s.numeroCuotas > CalculadoraAmortizacion.PLAZO_MAXIMO) {
            throw new NegocioException(400, "El plazo debe estar entre 3 y 18 meses");
        }
        BigDecimal monto = s.precioTelefono.setScale(2, RoundingMode.HALF_UP);

        try (Connection cn = Conexion.obtener()) {
            cn.setAutoCommit(false);
            try {
                // FOR UPDATE: bloquea al cliente para que dos solicitudes simultáneas no generen 2 créditos activos
                ClienteBanco c = buscarClientePorCodigo(cn, s.codCliente, true);
                if (c == null) {
                    throw new NegocioException(404, "No existe el cliente con código " + s.codCliente);
                }
                SujetoCredito sujeto = verificarSujetoCredito(cn, c, c.cedula);
                if (!sujeto.sujetoCredito) {
                    throw new NegocioException(422, "No es sujeto de crédito: " + String.join("; ", sujeto.motivos));
                }
                MontoMaximo max = calcularMontoMaximo(cn, s.codCliente);
                if (monto.compareTo(max.montoMaximo) > 0) {
                    throw new NegocioException(422, "El precio $" + monto + " supera el monto máximo aprobado $" + max.montoMaximo);
                }

                LocalDate hoy = LocalDate.now();
                List<CuotaAmortizacion> tabla = CalculadoraAmortizacion.generar(monto, s.numeroCuotas, hoy);
                int codCredito;
                try (PreparedStatement ps = cn.prepareStatement(
                        "INSERT INTO CREDITO (COD_CLIENTE, MONTO, PLAZO_MESES, TASA_ANUAL, CUOTA_FIJA, FECHA_CREDITO, ESTADO) "
                        + "VALUES (?, ?, ?, ?, ?, ?, 'ACTIVO')", Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, s.codCliente);
                    ps.setBigDecimal(2, monto);
                    ps.setInt(3, s.numeroCuotas);
                    ps.setBigDecimal(4, CalculadoraAmortizacion.TASA_ANUAL);
                    ps.setBigDecimal(5, tabla.get(0).valorCuota);
                    ps.setDate(6, Date.valueOf(hoy));
                    ps.executeUpdate();
                    try (ResultSet k = ps.getGeneratedKeys()) {
                        k.next();
                        codCredito = k.getInt(1);
                    }
                }
                try (PreparedStatement ps = cn.prepareStatement(
                        "INSERT INTO AMORTIZACION (COD_CREDITO, NUM_CUOTA, VALOR_CUOTA, INTERES_PAGADO, CAPITAL_PAGADO, SALDO, FECHA_VENCIMIENTO) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?)")) {
                    for (CuotaAmortizacion q : tabla) {
                        ps.setInt(1, codCredito);
                        ps.setInt(2, q.numCuota);
                        ps.setBigDecimal(3, q.valorCuota);
                        ps.setBigDecimal(4, q.interesPagado);
                        ps.setBigDecimal(5, q.capitalPagado);
                        ps.setBigDecimal(6, q.saldo);
                        ps.setDate(7, Date.valueOf(q.fechaVencimiento));
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
                cn.commit();
                return consultarCredito(cn, codCredito);
            } catch (SQLException | RuntimeException e) {
                cn.rollback();
                throw e;
            }
        }
    }

    // ------------------------------------------------------------------ 4. CONSULTAS DE AMORTIZACIÓN
    public Credito consultarCredito(int codCredito) throws SQLException {
        try (Connection cn = Conexion.obtener()) {
            Credito c = consultarCredito(cn, codCredito);
            if (c == null) {
                throw new NegocioException(404, "No existe el crédito " + codCredito);
            }
            return c;
        }
    }

    public List<Credito> creditosPorCedula(String cedula) throws SQLException {
        try (Connection cn = Conexion.obtener()) {
            if (buscarClientePorCedula(cn, cedula, false) == null) {
                throw new NegocioException(404, "La cédula " + cedula + " no pertenece a un cliente del banco");
            }
            List<Integer> codigos = new ArrayList<>();
            try (PreparedStatement ps = cn.prepareStatement("SELECT cr.COD_CREDITO FROM CREDITO cr "
                    + "JOIN CLIENTE c ON c.COD_CLIENTE = cr.COD_CLIENTE WHERE c.CEDULA = ? ORDER BY cr.COD_CREDITO DESC")) {
                ps.setString(1, cedula);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        codigos.add(rs.getInt(1));
                    }
                }
            }
            List<Credito> creditos = new ArrayList<>();
            for (int cod : codigos) {
                creditos.add(consultarCredito(cn, cod));
            }
            return creditos;
        }
    }

    private Credito consultarCredito(Connection cn, int codCredito) throws SQLException {
        Credito c = null;
        try (PreparedStatement ps = cn.prepareStatement("SELECT cr.*, cl.CEDULA, cl.NOMBRE FROM CREDITO cr "
                + "JOIN CLIENTE cl ON cl.COD_CLIENTE = cr.COD_CLIENTE WHERE cr.COD_CREDITO = ?")) {
            ps.setInt(1, codCredito);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                c = new Credito();
                c.codCredito = rs.getInt("COD_CREDITO");
                c.codCliente = rs.getInt("COD_CLIENTE");
                c.cedula = rs.getString("CEDULA");
                c.nombre = rs.getString("NOMBRE");
                c.monto = rs.getBigDecimal("MONTO");
                c.plazoMeses = rs.getInt("PLAZO_MESES");
                c.tasaAnual = rs.getBigDecimal("TASA_ANUAL");
                c.cuotaFija = rs.getBigDecimal("CUOTA_FIJA");
                c.fechaCredito = rs.getDate("FECHA_CREDITO").toLocalDate();
                c.estado = rs.getString("ESTADO");
            }
        }
        try (PreparedStatement ps = cn.prepareStatement(
                "SELECT * FROM AMORTIZACION WHERE COD_CREDITO = ? ORDER BY NUM_CUOTA")) {
            ps.setInt(1, codCredito);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    c.tablaAmortizacion.add(new CuotaAmortizacion(rs.getInt("NUM_CUOTA"), rs.getBigDecimal("VALOR_CUOTA"),
                            rs.getBigDecimal("INTERES_PAGADO"), rs.getBigDecimal("CAPITAL_PAGADO"),
                            rs.getBigDecimal("SALDO"), rs.getDate("FECHA_VENCIMIENTO").toLocalDate()));
                }
            }
        }
        c.totalIntereses = c.tablaAmortizacion.stream().map(q -> q.interesPagado).reduce(BigDecimal.ZERO, BigDecimal::add);
        c.totalPagar = c.monto.add(c.totalIntereses);
        return c;
    }

    // ------------------------------------------------------------------ utilitarios
    private static final class ClienteBanco {
        int codigo;
        String cedula;
        String nombre;
        String genero;
        LocalDate fechaNacimiento;
    }

    private ClienteBanco buscarClientePorCedula(Connection cn, String cedula, boolean bloquear) throws SQLException {
        return buscarCliente(cn, "CEDULA = ?", cedula, bloquear);
    }

    private ClienteBanco buscarClientePorCodigo(Connection cn, int codigo) throws SQLException {
        return buscarClientePorCodigo(cn, codigo, false);
    }

    private ClienteBanco buscarClientePorCodigo(Connection cn, int codigo, boolean bloquear) throws SQLException {
        return buscarCliente(cn, "COD_CLIENTE = ?", codigo, bloquear);
    }

    private ClienteBanco buscarCliente(Connection cn, String filtro, Object valor, boolean bloquear) throws SQLException {
        String sql = "SELECT COD_CLIENTE, CEDULA, NOMBRE, GENERO, FECHA_NACIMIENTO FROM CLIENTE WHERE " + filtro
                + (bloquear ? " FOR UPDATE" : "");
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setObject(1, valor);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                ClienteBanco c = new ClienteBanco();
                c.codigo = rs.getInt(1);
                c.cedula = rs.getString(2);
                c.nombre = rs.getString(3);
                c.genero = rs.getString(4);
                c.fechaNacimiento = rs.getDate(5).toLocalDate();
                return c;
            }
        }
    }

    private int contar(Connection cn, String sql, int codCliente) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, codCliente);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }
}
