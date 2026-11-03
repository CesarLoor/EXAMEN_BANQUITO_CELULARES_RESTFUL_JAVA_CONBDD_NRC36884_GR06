package ec.edu.monster.servicio;

/** Error listo para mostrar al usuario (mensaje del servidor o de conexión). */
public class ServicioException extends RuntimeException {

    public ServicioException(String mensaje) {
        super(mensaje);
    }
}
