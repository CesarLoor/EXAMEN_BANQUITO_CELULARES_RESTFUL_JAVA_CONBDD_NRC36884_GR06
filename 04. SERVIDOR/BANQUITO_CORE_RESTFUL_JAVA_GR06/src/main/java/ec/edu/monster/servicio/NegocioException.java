package ec.edu.monster.servicio;

/** Regla de negocio incumplida; el recurso REST la devuelve con el código HTTP indicado. */
public class NegocioException extends RuntimeException {

    private final int status;

    public NegocioException(int status, String mensaje) {
        super(mensaje);
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}
