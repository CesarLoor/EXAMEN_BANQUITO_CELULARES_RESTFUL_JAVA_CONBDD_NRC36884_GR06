package ec.edu.monster.vista;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import ec.edu.monster.R;
import ec.edu.monster.modelo.DetalleFactura;
import ec.edu.monster.modelo.EvaluacionCredito;
import ec.edu.monster.modelo.Factura;
import ec.edu.monster.modelo.SolicitudVenta;
import ec.edu.monster.modelo.Telefono;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** Facturación: efectivo (42% de descuento) o crédito directo BanQuito. */
public class VentaActivity extends AppCompatActivity {

    private final List<Telefono> catalogo = new ArrayList<>();
    private final List<Telefono> itemsTel = new ArrayList<>();
    private final List<Integer> itemsCant = new ArrayList<>();
    private ArrayAdapter<Telefono> adCatalogo;
    private Spinner spTelefono;
    private EditText txtCantidad, txtCedula, txtNombre, txtPlazo;
    private RadioButton rbCredito;
    private TextView lblCarrito, lblTotales, lblEvaluacion, lblFactura;
    private LinearLayout contAmortizacion;
    private View bloqueCredito;
    private ProgressBar progreso;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Ui.ctrl == null) {
            finish();
            return;
        }
        setContentView(R.layout.activity_venta);
        spTelefono = findViewById(R.id.spTelefono);
        txtCantidad = findViewById(R.id.txtCantidad);
        txtCedula = findViewById(R.id.txtCedula);
        txtNombre = findViewById(R.id.txtNombre);
        txtPlazo = findViewById(R.id.txtPlazo);
        rbCredito = findViewById(R.id.rbCredito);
        lblCarrito = findViewById(R.id.lblCarrito);
        lblTotales = findViewById(R.id.lblTotales);
        lblEvaluacion = findViewById(R.id.lblEvaluacion);
        lblFactura = findViewById(R.id.lblFactura);
        contAmortizacion = findViewById(R.id.contAmortizacion);
        bloqueCredito = findViewById(R.id.bloqueCredito);
        progreso = findViewById(R.id.progreso);

        adCatalogo = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, catalogo);
        spTelefono.setAdapter(adCatalogo);

        findViewById(R.id.btnAgregar).setOnClickListener(v -> agregar());
        findViewById(R.id.btnLimpiar).setOnClickListener(v -> limpiarCarrito());
        findViewById(R.id.btnVerificar).setOnClickListener(v -> verificar());
        findViewById(R.id.btnFacturar).setOnClickListener(v -> facturar());
        findViewById(R.id.rbEfectivo).setOnClickListener(v -> modo());
        rbCredito.setOnClickListener(v -> modo());
        modo();

        Ui.ejecutar(this, progreso, () -> Ui.ctrl.listarTelefonos(false), lista -> {
            catalogo.clear();
            catalogo.addAll(lista);
            adCatalogo.notifyDataSetChanged();
        });
    }

    private void modo() {
        boolean credito = rbCredito.isChecked();
        bloqueCredito.setVisibility(credito ? View.VISIBLE : View.GONE);
        txtNombre.setEnabled(!credito);
        lblEvaluacion.setText("");
        actualizarCarrito();
    }

    private void agregar() {
        Telefono t = (Telefono) spTelefono.getSelectedItem();
        int cant;
        try {
            cant = Integer.parseInt(txtCantidad.getText().toString().trim());
        } catch (NumberFormatException e) {
            cant = 0;
        }
        if (t == null || cant < 1) {
            Ui.error(this, "Seleccione un teléfono y una cantidad mayor a 0");
            return;
        }
        itemsTel.add(t);
        itemsCant.add(cant);
        actualizarCarrito();
    }

    private void limpiarCarrito() {
        itemsTel.clear();
        itemsCant.clear();
        actualizarCarrito();
    }

    /** Vista previa; el valor oficial lo calcula el servidor. */
    private void actualizarCarrito() {
        StringBuilder sb = new StringBuilder();
        BigDecimal sub = BigDecimal.ZERO;
        for (int i = 0; i < itemsTel.size(); i++) {
            Telefono t = itemsTel.get(i);
            BigDecimal s = t.precio.multiply(BigDecimal.valueOf(itemsCant.get(i)));
            sub = sub.add(s);
            sb.append(itemsCant.get(i)).append(" x ").append(t.marca).append(' ').append(t.modelo)
                    .append("  $").append(Ui.dinero(s)).append('\n');
        }
        lblCarrito.setText(sb.length() == 0 ? "(sin teléfonos)" : sb.toString().trim());
        BigDecimal desc = rbCredito.isChecked() ? BigDecimal.ZERO : sub.multiply(new BigDecimal("0.42"));
        lblTotales.setText("Subtotal $" + Ui.dinero(sub) + "\nDescuento $" + Ui.dinero(desc)
                + "\nTOTAL $" + Ui.dinero(sub.subtract(desc)));
    }

    private void verificar() {
        String cedula = txtCedula.getText().toString();
        Ui.ejecutar(this, progreso, () -> Ui.ctrl.evaluarCredito(cedula), (EvaluacionCredito ev) -> {
            if (ev.sujetoCredito) {
                lblEvaluacion.setTextColor(ContextCompat.getColor(this, R.color.verde));
                lblEvaluacion.setText("✔ " + ev.nombre + " es sujeto de crédito.\nMonto máximo: $" + Ui.dinero(ev.montoMaximo));
            } else {
                lblEvaluacion.setTextColor(ContextCompat.getColor(this, R.color.rojo));
                StringBuilder sb = new StringBuilder("✘ No es sujeto de crédito:");
                for (String m : ev.motivos) {
                    sb.append("\n• ").append(m);
                }
                lblEvaluacion.setText(sb);
            }
        });
    }

    private void facturar() {
        List<SolicitudVenta.Item> items = new ArrayList<>();
        for (int i = 0; i < itemsTel.size(); i++) {
            items.add(new SolicitudVenta.Item(itemsTel.get(i).codTelefono, itemsCant.get(i)));
        }
        boolean credito = rbCredito.isChecked();
        String cedula = txtCedula.getText().toString();
        String nombre = txtNombre.getText().toString();
        Integer plazo = null;
        try {
            plazo = Integer.valueOf(txtPlazo.getText().toString().trim());
        } catch (NumberFormatException ignorada) {
            // el controlador valida el plazo y muestra el mensaje
        }
        Integer plazoFinal = plazo;
        Ui.ejecutar(this, progreso, () -> Ui.ctrl.facturar(cedula, nombre, credito ? "CREDITO" : "EFECTIVO",
                plazoFinal, items), this::mostrarFactura);
    }

    private void mostrarFactura(Factura f) {
        StringBuilder sb = new StringBuilder("FACTURA ").append(f.numFactura).append('\n')
                .append(f.fecha.replace('T', ' ')).append('\n')
                .append(f.nombreCliente).append(" (").append(f.cedula).append(")\n\n");
        for (DetalleFactura d : f.detalles) {
            sb.append(d.cantidad).append(" x ").append(d.descripcion).append("  $").append(Ui.dinero(d.subtotal)).append('\n');
        }
        sb.append("\nSubtotal $").append(Ui.dinero(f.subtotal))
                .append("\nDescuento $").append(Ui.dinero(f.descuento))
                .append("\nTOTAL $").append(Ui.dinero(f.total))
                .append("\nPago: ").append(f.codCredito == null ? "EFECTIVO"
                : "CRÉDITO #" + f.codCredito + " a " + f.plazoMeses + " meses");
        lblFactura.setText(sb);
        lblFactura.setVisibility(View.VISIBLE);
        contAmortizacion.removeAllViews();
        if (f.credito != null) {
            contAmortizacion.addView(Ui.tablaAmortizacion(this, f.credito));
        }
        limpiarCarrito();
        lblEvaluacion.setText("");
        lblFactura.getParent().requestChildFocus(lblFactura, lblFactura); // desplaza hasta la factura
    }
}
