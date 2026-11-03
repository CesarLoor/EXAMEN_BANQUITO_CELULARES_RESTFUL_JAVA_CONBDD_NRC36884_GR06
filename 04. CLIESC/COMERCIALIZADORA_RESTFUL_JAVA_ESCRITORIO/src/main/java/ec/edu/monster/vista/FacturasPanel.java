package ec.edu.monster.vista;

import ec.edu.monster.controlador.ComercializadoraControlador;
import ec.edu.monster.modelo.Factura;
import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.table.DefaultTableModel;

/** Listado de facturas emitidas. */
class FacturasPanel extends JPanel {

    private final ComercializadoraControlador ctrl;
    private final DefaultTableModel modelo = Ui.modelo("Número", "Fecha", "Cédula", "Cliente", "Forma de pago",
            "Crédito", "Subtotal", "Descuento", "Total");

    FacturasPanel(ComercializadoraControlador ctrl) {
        super(new BorderLayout(8, 8));
        this.ctrl = ctrl;
        Ui.margen(this, 14);
        add(Ui.titulo("Facturas emitidas"), BorderLayout.NORTH);
        add(new JScrollPane(Ui.tabla(modelo, 6)), BorderLayout.CENTER);
    }

    void cargar() {
        Ui.async(this, ctrl::listarFacturas, lista -> {
            modelo.setRowCount(0);
            for (Factura f : lista) {
                modelo.addRow(new Object[]{f.numFactura, f.fecha.replace('T', ' '), f.cedula, f.nombreCliente,
                    f.formaPago, f.codCredito == null ? "" : "#" + f.codCredito + " (" + f.plazoMeses + " m)",
                    Ui.dinero(f.subtotal), Ui.dinero(f.descuento), Ui.dinero(f.total)});
            }
        });
    }
}
