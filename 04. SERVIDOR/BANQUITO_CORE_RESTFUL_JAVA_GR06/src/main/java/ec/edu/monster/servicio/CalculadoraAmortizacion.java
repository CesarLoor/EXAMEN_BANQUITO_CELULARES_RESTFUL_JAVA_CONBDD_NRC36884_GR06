package ec.edu.monster.servicio;

import ec.edu.monster.modelo.CuotaAmortizacion;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Tabla de amortización con cuota fija (sistema francés), pago mensual, 16.5% anual.
 *
 *                Valor Teléfono
 * Cuota = ---------------------------------- , TasaPeriodo = 16.5% / 12
 *          1 - (1 + TasaPeriodo)^-NúmeroCuotas
 *          -----------------------------------
 *                     TasaPeriodo
 *
 * Cada fila se redondea a 2 decimales (como se cobra en la realidad) y la última cuota absorbe
 * los centavos de redondeo para que el saldo termine exactamente en 0.00.
 */
public final class CalculadoraAmortizacion {

    public static final BigDecimal TASA_ANUAL = new BigDecimal("16.50");
    public static final int PLAZO_MINIMO = 3;
    public static final int PLAZO_MAXIMO = 18;

    private static final MathContext MC = MathContext.DECIMAL64;
    private static final BigDecimal TASA_PERIODO = TASA_ANUAL
            .divide(BigDecimal.valueOf(100), MC)
            .divide(BigDecimal.valueOf(12), MC);

    private CalculadoraAmortizacion() {
    }

    public static BigDecimal cuotaFija(BigDecimal valor, int numeroCuotas) {
        // (1 + i)^-n = 1 / (1 + i)^n
        BigDecimal factor = BigDecimal.ONE.divide(BigDecimal.ONE.add(TASA_PERIODO).pow(numeroCuotas, MC), MC);
        BigDecimal denominador = BigDecimal.ONE.subtract(factor).divide(TASA_PERIODO, MC);
        return dinero(valor.divide(denominador, MC));
    }

    public static List<CuotaAmortizacion> generar(BigDecimal valor, int numeroCuotas, LocalDate fechaCredito) {
        BigDecimal cuota = cuotaFija(valor, numeroCuotas);
        BigDecimal saldo = dinero(valor);
        List<CuotaAmortizacion> tabla = new ArrayList<>();
        for (int n = 1; n <= numeroCuotas; n++) {
            BigDecimal interes = dinero(saldo.multiply(TASA_PERIODO));
            BigDecimal capital = n == numeroCuotas ? saldo : cuota.subtract(interes);
            saldo = saldo.subtract(capital);
            tabla.add(new CuotaAmortizacion(n, capital.add(interes), interes, capital, saldo,
                    fechaCredito.plusMonths(n)));
        }
        return tabla;
    }

    private static BigDecimal dinero(BigDecimal v) {
        return v.setScale(2, RoundingMode.HALF_UP);
    }
}
