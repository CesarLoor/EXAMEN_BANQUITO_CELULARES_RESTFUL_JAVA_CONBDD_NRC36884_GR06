package ec.edu.monster.modelo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** Crédito otorgado por BanQuito con su tabla de amortización. */
public class Credito {

    public int codCredito;
    public int codCliente;
    public String cedula;
    public String nombre;
    public BigDecimal monto;
    public int plazoMeses;
    public BigDecimal tasaAnual;
    public BigDecimal cuotaFija;
    public String fechaCredito;
    public String estado;
    public BigDecimal totalIntereses;
    public BigDecimal totalPagar;
    public List<CuotaAmortizacion> tablaAmortizacion = new ArrayList<>();

    @Override
    public String toString() {
        return "Crédito #" + codCredito + " - $" + monto + " a " + plazoMeses + " meses (" + estado + ")";
    }
}
