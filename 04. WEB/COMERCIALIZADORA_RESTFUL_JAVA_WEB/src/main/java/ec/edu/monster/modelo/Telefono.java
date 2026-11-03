package ec.edu.monster.modelo;

import java.math.BigDecimal;

public class Telefono {

    public Integer codTelefono;
    public String marca;
    public String modelo;
    public BigDecimal precio;
    public Boolean activo;

    @Override
    public String toString() { // usado por combos/listas en escritorio y móvil
        return marca + " " + modelo + "  -  $" + precio;
    }
}
