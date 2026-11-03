package ec.edu.monster.modelo.banco;

import java.math.BigDecimal;

/** Cuerpo del POST que otorga el crédito: código de cliente, precio del celular y número de cuotas. */
public class SolicitudCredito {

    public Integer codCliente;
    public BigDecimal precioTelefono;
    public Integer numeroCuotas;
}
