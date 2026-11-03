package ec.edu.monster.vista;

import ec.edu.monster.controlador.ComercializadoraControlador;
import ec.edu.monster.modelo.DetalleFactura;
import ec.edu.monster.modelo.EvaluacionCredito;
import ec.edu.monster.modelo.Factura;
import ec.edu.monster.modelo.SolicitudVenta;
import ec.edu.monster.modelo.Telefono;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;

/** Facturación: venta con pago en efectivo (42% de descuento) o con crédito directo BanQuito. */
class VentaPanel extends JPanel {

    private static final BigDecimal DESCUENTO_EFECTIVO = new BigDecimal("0.42");

    private final ComercializadoraControlador ctrl;
    private final JComboBox<Telefono> cboTelefono = new JComboBox<>();
    private final JSpinner spCantidad = new JSpinner(new SpinnerNumberModel(1, 1, 99, 1));
    private final DefaultTableModel carrito = Ui.modelo("Código", "Teléfono", "Cantidad", "P. Unitario", "Subtotal");
    private final JTable tablaCarrito = Ui.tabla(carrito, 2);
    private final List<Telefono> itemsTelefono = new ArrayList<>();
    private final List<Integer> itemsCantidad = new ArrayList<>();

    private final JTextField txtCedula = new JTextField(12);
    private final JTextField txtNombre = new JTextField(18);
    private final JRadioButton rbEfectivo = new JRadioButton("Efectivo (42% desc.)", true);
    private final JRadioButton rbCredito = new JRadioButton("Crédito directo BanQuito");
    private final JSpinner spPlazo = new JSpinner(new SpinnerNumberModel(6, 3, 18, 1));
    private final JLabel lblEvaluacion = new JLabel(" ");
    private final JLabel lblTotales = new JLabel(" ");

    private final JLabel lblFactura = new JLabel(" ");
    private final AmortizacionPanel.TablaAmortizacion tablaAmortizacion = new AmortizacionPanel.TablaAmortizacion();
    private final JSplitPane split;

    VentaPanel(ComercializadoraControlador ctrl) {
        super(new BorderLayout(8, 8));
        this.ctrl = ctrl;
        Ui.margen(this, 14);

        // --- selección de teléfonos
        JButton agregar = Ui.boton("Agregar", Ui.AZUL);
        JButton quitar = Ui.boton("Quitar", null);
        JButton recargar = Ui.boton("↻", null);
        JPanel seleccion = new JPanel(new FlowLayout(FlowLayout.LEFT));
        seleccion.add(new JLabel("Teléfono:"));
        seleccion.add(cboTelefono);
        seleccion.add(recargar);
        seleccion.add(new JLabel("Cantidad:"));
        seleccion.add(spCantidad);
        seleccion.add(agregar);
        seleccion.add(quitar);

        // --- datos del cliente y forma de pago
        ButtonGroup grupo = new ButtonGroup();
        grupo.add(rbEfectivo);
        grupo.add(rbCredito);
        JButton verificar = Ui.boton("Verificar crédito", null);
        JButton facturar = Ui.boton("FACTURAR", Ui.VERDE);
        JPanel datos = new JPanel(new GridBagLayout());
        datos.setBorder(BorderFactory.createTitledBorder("Cliente y forma de pago"));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 6, 4, 6);
        g.anchor = GridBagConstraints.WEST;
        int f = 0;
        celda(datos, g, 0, f, new JLabel("Cédula:"));
        celda(datos, g, 1, f, txtCedula);
        celda(datos, g, 2, f, new JLabel("Nombre (cliente nuevo en efectivo):"));
        celda(datos, g, 3, f++, txtNombre);
        celda(datos, g, 0, f, rbEfectivo);
        celda(datos, g, 1, f, rbCredito);
        celda(datos, g, 2, f, new JLabel("Plazo (meses, 3 - 18):"));
        JPanel plazo = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        plazo.add(spPlazo);
        plazo.add(new JLabel("  "));
        plazo.add(verificar);
        celda(datos, g, 3, f++, plazo);
        g.gridwidth = 3;
        celda(datos, g, 0, f, lblEvaluacion);
        g.gridwidth = 1;
        celda(datos, g, 3, f, facturar);

        JPanel superior = new JPanel(new BorderLayout(6, 6));
        superior.add(seleccion, BorderLayout.NORTH);
        superior.add(new JScrollPane(tablaCarrito), BorderLayout.CENTER);
        JPanel sur = new JPanel(new BorderLayout());
        sur.add(lblTotales, BorderLayout.NORTH);
        sur.add(datos, BorderLayout.CENTER);
        superior.add(sur, BorderLayout.SOUTH);

        JPanel inferior = new JPanel(new BorderLayout(4, 4));
        inferior.add(lblFactura, BorderLayout.NORTH);
        inferior.add(tablaAmortizacion, BorderLayout.CENTER);

        split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, superior, inferior);
        split.setResizeWeight(0.62);
        add(Ui.titulo("Facturación de teléfonos celulares"), BorderLayout.NORTH);
        add(split, BorderLayout.CENTER);

        agregar.addActionListener(e -> agregar());
        quitar.addActionListener(e -> quitar());
        recargar.addActionListener(e -> cargarCatalogo());
        verificar.addActionListener(e -> verificarCredito());
        facturar.addActionListener(e -> facturar());
        rbEfectivo.addActionListener(e -> actualizarModo());
        rbCredito.addActionListener(e -> actualizarModo());
        actualizarModo();
        cargarCatalogo();
    }

    private static void celda(JPanel p, GridBagConstraints g, int x, int y, java.awt.Component c) {
        g.gridx = x;
        g.gridy = y;
        p.add(c, g);
    }

    private void cargarCatalogo() {
        Ui.async(this, () -> ctrl.listarTelefonos(false), lista -> {
            cboTelefono.removeAllItems();
            lista.forEach(cboTelefono::addItem);
        });
    }

    private void actualizarModo() {
        boolean credito = rbCredito.isSelected();
        spPlazo.setEnabled(credito);
        txtNombre.setEnabled(!credito);
        lblEvaluacion.setText(" ");
        actualizarTotales();
    }

    private void agregar() {
        Telefono t = (Telefono) cboTelefono.getSelectedItem();
        if (t == null) {
            return;
        }
        int cant = (Integer) spCantidad.getValue();
        itemsTelefono.add(t);
        itemsCantidad.add(cant);
        carrito.addRow(new Object[]{t.codTelefono, t.marca + " " + t.modelo, cant, Ui.dinero(t.precio),
            Ui.dinero(t.precio.multiply(BigDecimal.valueOf(cant)))});
        actualizarTotales();
    }

    private void quitar() {
        int fila = tablaCarrito.getSelectedRow();
        if (fila >= 0) {
            itemsTelefono.remove(fila);
            itemsCantidad.remove(fila);
            carrito.removeRow(fila);
            actualizarTotales();
        }
    }

    private BigDecimal subtotal() {
        BigDecimal s = BigDecimal.ZERO;
        for (int i = 0; i < itemsTelefono.size(); i++) {
            s = s.add(itemsTelefono.get(i).precio.multiply(BigDecimal.valueOf(itemsCantidad.get(i))));
        }
        return s;
    }

    /** Vista previa; el valor oficial lo calcula el servidor al facturar. */
    private void actualizarTotales() {
        BigDecimal sub = subtotal();
        BigDecimal desc = rbEfectivo.isSelected() ? sub.multiply(DESCUENTO_EFECTIVO) : BigDecimal.ZERO;
        lblTotales.setText(String.format("<html><div style='font-size:12px;padding:4px'>Subtotal: <b>$%s</b> · "
                + "Descuento: <b>$%s</b> · Total: <b style='color:#0B3D91'>$%s</b></div></html>",
                Ui.dinero(sub), Ui.dinero(desc), Ui.dinero(sub.subtract(desc))));
    }

    private void verificarCredito() {
        Ui.async(this, () -> ctrl.evaluarCredito(txtCedula.getText()), (EvaluacionCredito ev) -> {
            if (ev.sujetoCredito) {
                lblEvaluacion.setText("<html><span style='color:#1E8E3E'>✔ " + ev.nombre
                        + " es sujeto de crédito. Monto máximo: <b>$" + Ui.dinero(ev.montoMaximo) + "</b></span></html>");
            } else {
                lblEvaluacion.setText("<html><span style='color:#C5221F'>✘ No es sujeto de crédito: "
                        + String.join("; ", ev.motivos) + "</span></html>");
            }
        });
    }

    private void facturar() {
        List<SolicitudVenta.Item> items = new ArrayList<>();
        for (int i = 0; i < itemsTelefono.size(); i++) {
            items.add(new SolicitudVenta.Item(itemsTelefono.get(i).codTelefono, itemsCantidad.get(i)));
        }
        boolean credito = rbCredito.isSelected();
        String cedula = txtCedula.getText();
        String nombre = txtNombre.getText();
        Integer plazo = credito ? (Integer) spPlazo.getValue() : null;
        Ui.async(this, () -> ctrl.facturar(cedula, nombre, credito ? "CREDITO" : "EFECTIVO", plazo, items),
                this::mostrarFactura);
    }

    private void mostrarFactura(Factura f) {
        StringBuilder det = new StringBuilder();
        for (DetalleFactura d : f.detalles) {
            det.append(d.cantidad).append(" x ").append(d.descripcion).append(" ($").append(Ui.dinero(d.subtotal))
                    .append(")<br>");
        }
        lblFactura.setText(String.format("<html><div style='padding:6px;font-size:12px'><b style='color:#1E8E3E'>"
                + "✔ Factura %s emitida</b> · %s · %s (%s)<br>%s<b>Subtotal $%s · Descuento $%s · TOTAL $%s</b> · "
                + "Pago: %s</div></html>", f.numFactura, f.fecha.replace('T', ' '), f.nombreCliente, f.cedula, det,
                Ui.dinero(f.subtotal), Ui.dinero(f.descuento), Ui.dinero(f.total),
                f.codCredito == null ? "EFECTIVO" : "CRÉDITO #" + f.codCredito + " a " + f.plazoMeses + " meses"));
        tablaAmortizacion.mostrar(f.credito);
        split.setDividerLocation(f.credito != null ? 0.30 : 0.55); // deja ver la factura y la tabla completa
        carrito.setRowCount(0);
        itemsTelefono.clear();
        itemsCantidad.clear();
        lblEvaluacion.setText(" ");
        actualizarTotales();
    }
}
