package ec.edu.monster.vista;

import ec.edu.monster.controlador.ComercializadoraControlador;
import ec.edu.monster.modelo.Credito;
import ec.edu.monster.modelo.CuotaAmortizacion;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

/** Consulta de la tabla de amortización de los préstamos de un cliente (vía BanQuito). */
class AmortizacionPanel extends JPanel {

    private final ComercializadoraControlador ctrl;
    private final JTextField txtCedula = new JTextField(12);
    private final JComboBox<Credito> cboCreditos = new JComboBox<>();
    private final TablaAmortizacion tabla = new TablaAmortizacion();

    AmortizacionPanel(ComercializadoraControlador ctrl) {
        super(new BorderLayout(8, 8));
        this.ctrl = ctrl;
        Ui.margen(this, 14);

        JButton buscar = Ui.boton("Consultar", Ui.AZUL);
        JPanel filtro = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filtro.add(new JLabel("Cédula del cliente:"));
        filtro.add(txtCedula);
        filtro.add(buscar);
        filtro.add(new JLabel("   Préstamo:"));
        filtro.add(cboCreditos);

        JPanel norte = new JPanel(new BorderLayout());
        norte.add(Ui.titulo("Tabla de amortización de un préstamo"), BorderLayout.NORTH);
        norte.add(filtro, BorderLayout.SOUTH);
        add(norte, BorderLayout.NORTH);
        add(tabla, BorderLayout.CENTER);

        buscar.addActionListener(e -> consultar());
        txtCedula.addActionListener(e -> consultar());
        cboCreditos.addActionListener(e -> tabla.mostrar((Credito) cboCreditos.getSelectedItem()));
    }

    private void consultar() {
        Ui.async(this, () -> ctrl.creditosCliente(txtCedula.getText()), (List<Credito> creditos) -> {
            cboCreditos.removeAllItems();
            creditos.forEach(cboCreditos::addItem);
            if (creditos.isEmpty()) {
                tabla.mostrar(null);
                Ui.info(this, "El cliente no tiene préstamos en BanQuito");
            }
        });
    }

    /** Componente reutilizable (también lo usa la pantalla de facturación). */
    static class TablaAmortizacion extends JPanel {

        private final DefaultTableModel modelo = Ui.modelo("# Cuota", "Vence", "Valor Cuota", "Interés Pagado",
                "Capital Pagado", "Saldo");
        private final JLabel resumen = new JLabel(" ");

        TablaAmortizacion() {
            super(new BorderLayout(4, 4));
            JTable t = Ui.tabla(modelo, 2);
            add(resumen, BorderLayout.NORTH);
            add(new JScrollPane(t), BorderLayout.CENTER);
        }

        void mostrar(Credito c) {
            modelo.setRowCount(0);
            if (c == null) {
                resumen.setText(" ");
                return;
            }
            resumen.setText(String.format("<html><b>Crédito #%d</b> · %s · Monto $%s · %d cuotas · Tasa %s%% anual · "
                    + "Cuota fija <b>$%s</b> · Intereses $%s · Total $%s · <b>%s</b></html>", c.codCredito, c.nombre,
                    Ui.dinero(c.monto), c.plazoMeses, c.tasaAnual, Ui.dinero(c.cuotaFija), Ui.dinero(c.totalIntereses),
                    Ui.dinero(c.totalPagar), c.estado));
            modelo.addRow(new Object[]{0, c.fechaCredito, "", "", "", Ui.dinero(c.monto)});
            for (CuotaAmortizacion q : c.tablaAmortizacion) {
                modelo.addRow(new Object[]{q.numCuota, q.fechaVencimiento, Ui.dinero(q.valorCuota),
                    Ui.dinero(q.interesPagado), Ui.dinero(q.capitalPagado), Ui.dinero(q.saldo)});
            }
        }
    }
}
