package ec.edu.monster.vista;

import ec.edu.monster.controlador.ComercializadoraControlador;
import ec.edu.monster.modelo.Telefono;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

/** Mantenimiento del catálogo de teléfonos celulares (crear, editar, dar de baja). */
class CatalogoPanel extends JPanel {

    private final ComercializadoraControlador ctrl;
    private final DefaultTableModel modelo = Ui.modelo("Código", "Marca", "Modelo", "Precio ($)", "Estado");
    private final JTable tabla = Ui.tabla(modelo, 3);
    private final JCheckBox chkTodos = new JCheckBox("Mostrar dados de baja", true);
    private List<Telefono> telefonos = new ArrayList<>();

    CatalogoPanel(ComercializadoraControlador ctrl) {
        super(new BorderLayout(8, 8));
        this.ctrl = ctrl;
        Ui.margen(this, 14);

        JButton nuevo = Ui.boton("Nuevo", Ui.AZUL);
        JButton editar = Ui.boton("Editar", null);
        JButton baja = Ui.boton("Dar de baja", Ui.ROJO);
        JButton refrescar = Ui.boton("Refrescar", null);
        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        for (java.awt.Component c : new java.awt.Component[]{nuevo, editar, baja, refrescar, chkTodos}) {
            acciones.add(c);
        }

        JPanel norte = new JPanel(new BorderLayout());
        norte.add(Ui.titulo("Catálogo de teléfonos a la venta"), BorderLayout.NORTH);
        norte.add(acciones, BorderLayout.SOUTH);
        add(norte, BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        nuevo.addActionListener(e -> formulario(null));
        editar.addActionListener(e -> {
            Telefono t = seleccionado();
            if (t != null) {
                formulario(t);
            }
        });
        baja.addActionListener(e -> darDeBaja());
        refrescar.addActionListener(e -> cargar());
        chkTodos.addActionListener(e -> cargar());
        tabla.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && seleccionado() != null) {
                    formulario(seleccionado());
                }
            }
        });
        cargar();
    }

    private void cargar() {
        Ui.async(this, () -> ctrl.listarTelefonos(chkTodos.isSelected()), lista -> {
            telefonos = lista;
            modelo.setRowCount(0);
            for (Telefono t : lista) {
                modelo.addRow(new Object[]{t.codTelefono, t.marca, t.modelo, Ui.dinero(t.precio),
                    t.activo ? "A la venta" : "De baja"});
            }
        });
    }

    private Telefono seleccionado() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            Ui.info(this, "Seleccione un teléfono de la tabla");
            return null;
        }
        return telefonos.get(tabla.convertRowIndexToModel(fila));
    }

    private void formulario(Telefono t) {
        JTextField marca = new JTextField(t == null ? "" : t.marca);
        JTextField modeloTxt = new JTextField(t == null ? "" : t.modelo);
        JTextField precio = new JTextField(t == null ? "" : t.precio.toPlainString());
        JCheckBox activo = new JCheckBox("A la venta", t == null || t.activo);
        JPanel p = new JPanel(new GridLayout(0, 2, 6, 6));
        p.add(new JLabel("Marca"));
        p.add(marca);
        p.add(new JLabel("Modelo"));
        p.add(modeloTxt);
        p.add(new JLabel("Precio ($)"));
        p.add(precio);
        p.add(new JLabel("Estado"));
        p.add(activo);
        String titulo = t == null ? "Nuevo teléfono" : "Editar teléfono #" + t.codTelefono;
        if (JOptionPane.showConfirmDialog(this, p, titulo, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE)
                != JOptionPane.OK_OPTION) {
            return;
        }
        Integer cod = t == null ? null : t.codTelefono;
        Ui.async(this, () -> ctrl.guardarTelefono(cod, marca.getText(), modeloTxt.getText(), precio.getText(),
                activo.isSelected()), guardado -> {
                    Ui.info(this, "Teléfono guardado: " + guardado.marca + " " + guardado.modelo);
                    cargar();
                });
    }

    private void darDeBaja() {
        Telefono t = seleccionado();
        if (t == null) {
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "¿Dar de baja " + t.marca + " " + t.modelo + "?",
                "Confirmar", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            Ui.async(this, () -> {
                ctrl.eliminarTelefono(t.codTelefono);
                return t;
            }, x -> cargar());
        }
    }
}
