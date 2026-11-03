package ec.edu.monster.modelo;

import ec.edu.monster.modelo.banco.Credito;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Factura {

    public int codFactura;
    public String numFactura;
    public LocalDateTime fecha;
    public String cedula;
    public String nombreCliente;
    public String formaPago;          // EFECTIVO | CREDITO
    public BigDecimal subtotal;
    public BigDecimal descuento;
    public BigDecimal total;
    public Integer codCredito;        // crédito otorgado por BanQuito (solo CREDITO)
    public Integer plazoMeses;
    public List<DetalleFactura> detalles = new ArrayList<>();
    /** Crédito con tabla de amortización; se devuelve al facturar con crédito directo. */
    public Credito credito;
}
