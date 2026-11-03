package ec.edu.monster.modelo.banco;

import java.math.BigDecimal;

/** Respuesta del servicio que calcula el monto máximo de crédito. */
public class MontoMaximo {

    public int codCliente;
    public String nombre;
    public BigDecimal promedioDepositos;
    public BigDecimal promedioRetiros;
    /** ((promedioDepositos - promedioRetiros) * 30%) * 6, nunca negativo. */
    public BigDecimal montoMaximo;
}
