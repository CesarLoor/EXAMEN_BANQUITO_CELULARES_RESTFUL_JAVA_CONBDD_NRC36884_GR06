package ec.edu.monster.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** Conexión JDBC a la BDD de la Comercializadora (MySQL en Docker, puerto 3307). */
public final class Conexion {

    // Se puede cambiar sin recompilar con variables de entorno del servidor.
    private static final String URL = env("COMERCIALIZADORA_DB_URL",
            "jdbc:mysql://localhost:3307/comercializadora?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Guayaquil");
    private static final String USUARIO = env("COMERCIALIZADORA_DB_USER", "gr06");
    private static final String CLAVE = env("COMERCIALIZADORA_DB_PASS", "gr06pass");

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver"); // necesario dentro de Payara (classloader del WAR)
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private Conexion() {
    }

    public static Connection obtener() throws SQLException {
        return DriverManager.getConnection(URL, USUARIO, CLAVE);
    }

    private static String env(String clave, String porDefecto) {
        String valor = System.getenv(clave);
        return valor == null || valor.isBlank() ? porDefecto : valor;
    }
}
