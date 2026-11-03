package ec.edu.monster.vista;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/** Utilitarios de la vista: colores, llamadas al servidor en segundo plano y tablas. */
final class Ui {

    static final Color AZUL = new Color(0x0B3D91);
    static final Color VERDE = new Color(0x1E8E3E);
    static final Color ROJO = new Color(0xC5221F);

    private Ui() {
    }

    /** Ejecuta la llamada REST fuera del hilo de Swing y luego actualiza la vista. */
    static <T> void async(Component origen, Supplier<T> tarea, Consumer<T> alTerminar) {
        Component raiz = SwingUtilities.getRoot(origen);
        if (raiz != null) {
            raiz.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        }
        new SwingWorker<T, Void>() {
            @Override
            protected T doInBackground() {
                return tarea.get();
            }

            @Override
            protected void done() {
                if (raiz != null) {
                    raiz.setCursor(Cursor.getDefaultCursor());
                }
                try {
                    alTerminar.accept(get());
                } catch (ExecutionException e) {
                    error(origen, e.getCause().getMessage());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }.execute();
    }

    static void error(Component c, String mensaje) {
        JOptionPane.showMessageDialog(c, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }

    static void info(Component c, String mensaje) {
        JOptionPane.showMessageDialog(c, mensaje, "Información", JOptionPane.INFORMATION_MESSAGE);
    }

    static JButton boton(String texto, Color color) {
        JButton b = new JButton(texto);
        if (color != null) {
            b.setBackground(color);
            b.setForeground(Color.WHITE);
        }
        b.putClientProperty("JButton.buttonType", "roundRect");
        return b;
    }

    static JLabel titulo(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(l.getFont().deriveFont(Font.BOLD, 18f));
        l.setForeground(AZUL);
        l.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        return l;
    }

    static DefaultTableModel modelo(String... columnas) {
        return new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
    }

    /** Tabla de solo lectura; las columnas desde 'desdeColumnaNumerica' se alinean a la derecha. */
    static JTable tabla(DefaultTableModel m, int desdeColumnaNumerica) {
        JTable t = new JTable(m);
        t.setRowHeight(26);
        t.setFillsViewportHeight(true);
        t.getTableHeader().setReorderingAllowed(false);
        DefaultTableCellRenderer der = new DefaultTableCellRenderer();
        der.setHorizontalAlignment(JLabel.RIGHT);
        for (int i = desdeColumnaNumerica; i >= 0 && i < m.getColumnCount(); i++) {
            t.getColumnModel().getColumn(i).setCellRenderer(der);
        }
        return t;
    }

    static <C extends JComponent> C margen(C c, int px) {
        c.setBorder(BorderFactory.createEmptyBorder(px, px, px, px));
        return c;
    }

    static String dinero(Object v) {
        return v == null ? "" : String.format(java.util.Locale.US, "%,.2f", v);
    }
}
