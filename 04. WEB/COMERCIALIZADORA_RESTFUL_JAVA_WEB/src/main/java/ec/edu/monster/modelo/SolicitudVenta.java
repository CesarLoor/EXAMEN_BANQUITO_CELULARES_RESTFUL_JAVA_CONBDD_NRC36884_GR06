package ec.edu.monster.modelo;

import java.util.ArrayList;
import java.util.List;

/** Datos que se envían al servidor para facturar. */
public class SolicitudVenta {

    public String cedula;
    public String nombre;
    public String formaPago;   // EFECTIVO | CREDITO
    public Integer plazoMeses; // solo CREDITO (3 - 18)
    public List<Item> items = new ArrayList<>();

    public static class Item {

        public Integer codTelefono;
        public Integer cantidad;

        public Item(Integer codTelefono, Integer cantidad) {
            this.codTelefono = codTelefono;
            this.cantidad = cantidad;
        }
    }
}
