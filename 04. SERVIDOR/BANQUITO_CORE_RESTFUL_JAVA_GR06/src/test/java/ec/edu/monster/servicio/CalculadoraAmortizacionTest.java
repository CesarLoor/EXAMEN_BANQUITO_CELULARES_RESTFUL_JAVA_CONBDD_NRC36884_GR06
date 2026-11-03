package ec.edu.monster.servicio;

import static org.junit.Assert.assertEquals;

import ec.edu.monster.modelo.CuotaAmortizacion;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.Test;

/** Compara contra la tabla de ejemplo del enunciado: $450 a 6 meses, cuota 78.65. */
public class CalculadoraAmortizacionTest {

    @Test
    public void reproduceLaTablaDelEnunciado() {
        List<CuotaAmortizacion> t = CalculadoraAmortizacion.generar(new BigDecimal("450"), 6, LocalDate.of(2026, 1, 15));
        String[][] esperado = {
            {"78.65", "6.19", "72.46", "377.54"},
            {"78.65", "5.19", "73.46", "304.08"},
            {"78.65", "4.18", "74.47", "229.61"},
            {"78.65", "3.16", "75.49", "154.12"},
            {"78.65", "2.12", "76.53", "77.59"},
            {"78.66", "1.07", "77.59", "0.00"}, // la última cuota absorbe 1 centavo de redondeo
        };
        assertEquals(6, t.size());
        for (int i = 0; i < 6; i++) {
            CuotaAmortizacion c = t.get(i);
            assertEquals(i + 1, c.numCuota);
            assertEquals(esperado[i][0], c.valorCuota.toPlainString());
            assertEquals(esperado[i][1], c.interesPagado.toPlainString());
            assertEquals(esperado[i][2], c.capitalPagado.toPlainString());
            assertEquals(esperado[i][3], c.saldo.toPlainString());
        }
        assertEquals(LocalDate.of(2026, 2, 15), t.get(0).fechaVencimiento);
    }

    @Test
    public void cuotaFijaIgualAPagoDeExcel() {
        assertEquals("78.65", CalculadoraAmortizacion.cuotaFija(new BigDecimal("450"), 6).toPlainString());
        // =PAGO(16.5%/12; 18; -1199) -> 75.65
        assertEquals("75.65", CalculadoraAmortizacion.cuotaFija(new BigDecimal("1199"), 18).toPlainString());
    }

    @Test
    public void elCapitalPagadoSumaElMontoPrestado() {
        BigDecimal suma = CalculadoraAmortizacion.generar(new BigDecimal("1099.99"), 17, LocalDate.now()).stream()
                .map(c -> c.capitalPagado).reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals("1099.99", suma.toPlainString());
    }
}
