package ec.edu.monster.vista;

import ec.edu.monster.controlador.ComercializadoraControlador;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

/** Ventana principal: una pestaña por funcionalidad del examen. */
public class PrincipalVista extends JFrame {

    public PrincipalVista(ComercializadoraControlador ctrl) {
        super("Comercializadora de Celulares - Crédito directo BanQuito (GR06)");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel cabecera = new JPanel(new BorderLayout());
        cabecera.setBackground(Ui.AZUL);
        JLabel t = new JLabel("Comercializadora de Celulares");
        t.setForeground(Color.WHITE);
        t.setFont(t.getFont().deriveFont(Font.BOLD, 20f));
        JLabel u = new JLabel("Usuario: MONSTER   ");
        u.setForeground(Color.WHITE);
        cabecera.add(Ui.margen(t, 12), BorderLayout.WEST);
        cabecera.add(u, BorderLayout.EAST);

        FacturasPanel facturas = new FacturasPanel(ctrl);
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Facturación", new VentaPanel(ctrl));
        tabs.addTab("Catálogo de teléfonos", new CatalogoPanel(ctrl));
        tabs.addTab("Tabla de amortización", new AmortizacionPanel(ctrl));
        tabs.addTab("Facturas", facturas);
        tabs.addChangeListener(e -> {
            if (tabs.getSelectedComponent() == facturas) {
                facturas.cargar();
            }
        });

        add(cabecera, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
        setSize(1100, 720);
        setLocationRelativeTo(null);
    }
}
