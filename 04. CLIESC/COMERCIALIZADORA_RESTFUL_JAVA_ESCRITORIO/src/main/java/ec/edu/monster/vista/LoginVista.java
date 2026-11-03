package ec.edu.monster.vista;

import com.formdev.flatlaf.FlatLightLaf;
import ec.edu.monster.controlador.ComercializadoraControlador;
import ec.edu.monster.servicio.ComercializadoraServicio;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Locale;
import java.util.prefs.Preferences;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

/** Pantalla de acceso del cliente de escritorio. */
public class LoginVista extends JFrame {

    private final JTextField txtUsuario = new JTextField(18);
    private final JPasswordField txtClave = new JPasswordField(18);
    // Recuerda el último servidor usado (IP:puerto de la PC donde corre Payara)
    private static final Preferences PREFS = Preferences.userNodeForPackage(LoginVista.class);
    private final JTextField txtServidor = new JTextField(
            PREFS.get("servidor", ComercializadoraServicio.SERVIDOR_POR_DEFECTO), 18);

    public LoginVista() {
        super("Comercializadora de Celulares - Acceso");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel cabecera = new JPanel(new BorderLayout());
        cabecera.setBackground(Ui.AZUL);
        JLabel titulo = new JLabel("<html><center>Comercializadora de Celulares<br>"
                + "<span style='font-size:10px'>Crédito directo BanQuito · GR06</span></center></html>", SwingConstants.CENTER);
        titulo.setForeground(Color.WHITE);
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 18f));
        cabecera.add(Ui.margen(titulo, 18));

        JPanel form = Ui.margen(new JPanel(new GridBagLayout()), 20);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 6, 6, 6);
        g.fill = GridBagConstraints.HORIZONTAL;
        agregar(form, g, 0, "Usuario", txtUsuario);
        agregar(form, g, 1, "Contraseña", txtClave);
        agregar(form, g, 2, "Servidor (IP:puerto)", txtServidor);
        JButton btn = Ui.boton("Ingresar", Ui.AZUL);
        g.gridx = 0;
        g.gridy = 3;
        g.gridwidth = 2;
        form.add(btn, g);

        btn.addActionListener(e -> ingresar());
        getRootPane().setDefaultButton(btn);
        txtUsuario.putClientProperty("JTextField.placeholderText", "MONSTER");

        add(cabecera, BorderLayout.NORTH);
        add(form, BorderLayout.CENTER);
        pack();
        setResizable(false);
        setLocationRelativeTo(null);
    }

    private static void agregar(JPanel p, GridBagConstraints g, int fila, String etiqueta, java.awt.Component campo) {
        g.gridx = 0;
        g.gridy = fila;
        g.weightx = 0;
        p.add(new JLabel(etiqueta), g);
        g.gridx = 1;
        g.weightx = 1;
        p.add(campo, g);
    }

    private void ingresar() {
        String servidor = txtServidor.getText().trim();
        ComercializadoraControlador ctrl = new ComercializadoraControlador(ComercializadoraServicio.url(servidor));
        if (ctrl.login(txtUsuario.getText().trim(), new String(txtClave.getPassword()))) {
            PREFS.put("servidor", servidor);
            new PrincipalVista(ctrl).setVisible(true);
            dispose();
        } else {
            Ui.error(this, "Usuario o contraseña incorrectos");
            txtClave.setText("");
        }
    }

    public static void main(String[] args) {
        Locale.setDefault(Locale.US);
        FlatLightLaf.setup();
        SwingUtilities.invokeLater(() -> new LoginVista().setVisible(true));
    }
}
