package ec.edu.monster.modelo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Factura {

    public int codFactura;
    public String numFactura;
    public String fecha;
    public String cedula;
    public String nombreCliente;
    public String formaPago;
    public BigDecimal subtotal;
    public BigDecimal descuento;
    public BigDecimal total;
    public Integer codCredito;
    public Integer plazoMeses;
    public List<DetalleFactura> detalles = new ArrayList<>();
    public Credito credito;
}
