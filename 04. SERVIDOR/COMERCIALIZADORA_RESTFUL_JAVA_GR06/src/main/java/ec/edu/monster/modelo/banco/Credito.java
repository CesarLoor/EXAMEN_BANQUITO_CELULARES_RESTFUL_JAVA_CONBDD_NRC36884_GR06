package ec.edu.monster.modelo.banco;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Crédito otorgado con su tabla de amortización. */
public class Credito {

    public int codCredito;
    public int codCliente;
    public String cedula;
    public String nombre;
    public BigDecimal monto;
    public int plazoMeses;
    public BigDecimal tasaAnual;
    public BigDecimal cuotaFija;
    public LocalDate fechaCredito;
    public String estado;
    public BigDecimal totalIntereses;
    public BigDecimal totalPagar;
    public List<CuotaAmortizacion> tablaAmortizacion = new ArrayList<>();
}
