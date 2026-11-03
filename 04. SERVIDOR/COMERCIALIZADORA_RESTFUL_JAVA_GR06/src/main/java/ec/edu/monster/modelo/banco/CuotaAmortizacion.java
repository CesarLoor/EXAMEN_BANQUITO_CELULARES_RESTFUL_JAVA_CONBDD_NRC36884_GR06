package ec.edu.monster.modelo.banco;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Una fila de la tabla de amortización. */
public class CuotaAmortizacion {

    public int numCuota;
    public BigDecimal valorCuota;
    public BigDecimal interesPagado;
    public BigDecimal capitalPagado;
    public BigDecimal saldo;
    public LocalDate fechaVencimiento;

    public CuotaAmortizacion() {
    }

    public CuotaAmortizacion(int numCuota, BigDecimal valorCuota, BigDecimal interesPagado,
            BigDecimal capitalPagado, BigDecimal saldo, LocalDate fechaVencimiento) {
        this.numCuota = numCuota;
        this.valorCuota = valorCuota;
        this.interesPagado = interesPagado;
        this.capitalPagado = capitalPagado;
        this.saldo = saldo;
        this.fechaVencimiento = fechaVencimiento;
    }
}
