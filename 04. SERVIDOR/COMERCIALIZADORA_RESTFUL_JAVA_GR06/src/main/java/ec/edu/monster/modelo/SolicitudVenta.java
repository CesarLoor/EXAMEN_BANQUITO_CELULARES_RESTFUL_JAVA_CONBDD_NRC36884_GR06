package ec.edu.monster.modelo;

import java.util.ArrayList;
import java.util.List;

/**
 * Cuerpo del POST /facturas.
 * { "cedula":"1712345678", "nombre":"Carlos Andrade", "formaPago":"CREDITO", "plazoMeses":6,
 *   "items":[ {"codTelefono":1, "cantidad":1} ] }
 */
public class SolicitudVenta {

    public String cedula;
    public String nombre;
    public String formaPago;
    public Integer plazoMeses;
    public List<Item> items = new ArrayList<>();

    public static class Item {

        public Integer codTelefono;
        public Integer cantidad;
    }
}
