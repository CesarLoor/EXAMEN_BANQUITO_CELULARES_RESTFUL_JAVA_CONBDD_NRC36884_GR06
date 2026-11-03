package ec.edu.monster.servicio;

import ec.edu.monster.db.Conexion;
import ec.edu.monster.modelo.Telefono;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** Mantenimiento del catálogo de teléfonos celulares. */
public class TelefonoServicio {

    public List<Telefono> listar(boolean incluirInactivos) throws SQLException {
        String sql = "SELECT * FROM TELEFONO" + (incluirInactivos ? "" : " WHERE ACTIVO = TRUE") + " ORDER BY MARCA, MODELO";
        List<Telefono> lista = new ArrayList<>();
        try (Connection cn = Conexion.obtener(); PreparedStatement ps = cn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public Telefono buscar(int cod) throws SQLException {
        try (Connection cn = Conexion.obtener();
                PreparedStatement ps = cn.prepareStatement("SELECT * FROM TELEFONO WHERE COD_TELEFONO = ?")) {
            ps.setInt(1, cod);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new NegocioException(404, "No existe el teléfono " + cod);
                }
                return mapear(rs);
            }
        }
    }

    public Telefono crear(Telefono t) throws SQLException {
        validar(t);
        try (Connection cn = Conexion.obtener(); PreparedStatement ps = cn.prepareStatement(
                "INSERT INTO TELEFONO (MARCA, MODELO, PRECIO, ACTIVO) VALUES (?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, t.marca.trim());
            ps.setString(2, t.modelo.trim());
            ps.setBigDecimal(3, t.precio);
            ps.setBoolean(4, t.activo == null || t.activo);
            ps.executeUpdate();
            try (ResultSet k = ps.getGeneratedKeys()) {
                k.next();
                return buscar(k.getInt(1));
            }
        }
    }

    public Telefono actualizar(int cod, Telefono t) throws SQLException {
        validar(t);
        Telefono actual = buscar(cod); // 404 si no existe
        try (Connection cn = Conexion.obtener(); PreparedStatement ps = cn.prepareStatement(
                "UPDATE TELEFONO SET MARCA = ?, MODELO = ?, PRECIO = ?, ACTIVO = ? WHERE COD_TELEFONO = ?")) {
            ps.setString(1, t.marca.trim());
            ps.setString(2, t.modelo.trim());
            ps.setBigDecimal(3, t.precio);
            ps.setBoolean(4, t.activo == null ? actual.activo : t.activo);
            ps.setInt(5, cod);
            ps.executeUpdate();
        }
        return buscar(cod);
    }

    /** Baja lógica: el teléfono deja de venderse pero las facturas antiguas siguen íntegras. */
    public void eliminar(int cod) throws SQLException {
        try (Connection cn = Conexion.obtener();
                PreparedStatement ps = cn.prepareStatement("UPDATE TELEFONO SET ACTIVO = FALSE WHERE COD_TELEFONO = ?")) {
            ps.setInt(1, cod);
            if (ps.executeUpdate() == 0) {
                throw new NegocioException(404, "No existe el teléfono " + cod);
            }
        }
    }

    private void validar(Telefono t) {
        if (t == null || vacio(t.marca) || vacio(t.modelo) || t.precio == null) {
            throw new NegocioException(400, "Marca, modelo y precio son obligatorios");
        }
        if (t.precio.signum() <= 0) {
            throw new NegocioException(400, "El precio debe ser mayor a 0");
        }
        if (t.marca.trim().length() > 50 || t.modelo.trim().length() > 100) {
            throw new NegocioException(400, "Marca (máx. 50) o modelo (máx. 100) demasiado largos");
        }
        t.precio = t.precio.setScale(2, RoundingMode.HALF_UP);
    }

    private static boolean vacio(String s) {
        return s == null || s.isBlank();
    }

    static Telefono mapear(ResultSet rs) throws SQLException {
        Telefono t = new Telefono();
        t.codTelefono = rs.getInt("COD_TELEFONO");
        t.marca = rs.getString("MARCA");
        t.modelo = rs.getString("MODELO");
        t.precio = rs.getBigDecimal("PRECIO");
        t.activo = rs.getBoolean("ACTIVO");
        return t;
    }
}
