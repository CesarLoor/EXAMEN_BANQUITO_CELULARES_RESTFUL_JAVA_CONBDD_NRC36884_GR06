package ec.edu.monster.vista;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import ec.edu.monster.R;
import ec.edu.monster.modelo.Telefono;
import java.util.ArrayList;
import java.util.List;

/** Mantenimiento del catálogo: tocar un teléfono para editarlo o darlo de baja. */
public class CatalogoActivity extends AppCompatActivity {

    private final List<Telefono> telefonos = new ArrayList<>();
    private final List<String> filas = new ArrayList<>();
    private ArrayAdapter<String> adaptador;
    private ProgressBar progreso;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Ui.ctrl == null) {
            finish();
            return;
        }
        setContentView(R.layout.activity_catalogo);
        progreso = findViewById(R.id.progreso);
        ListView lista = findViewById(R.id.lista);
        adaptador = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, filas);
        lista.setAdapter(adaptador);
        lista.setOnItemClickListener((p, v, pos, id) -> formulario(telefonos.get(pos)));
        findViewById(R.id.btnNuevo).setOnClickListener(v -> formulario(null));
        cargar();
    }

    private void cargar() {
        Ui.ejecutar(this, progreso, () -> Ui.ctrl.listarTelefonos(true), lista -> {
            telefonos.clear();
            telefonos.addAll(lista);
            filas.clear();
            for (Telefono t : lista) {
                filas.add("#" + t.codTelefono + "  " + t.marca + " " + t.modelo + "\n$" + Ui.dinero(t.precio)
                        + (t.activo ? "  ·  A la venta" : "  ·  DE BAJA"));
            }
            adaptador.notifyDataSetChanged();
        });
    }

    private void formulario(Telefono t) {
        View v = LayoutInflater.from(this).inflate(R.layout.dialogo_telefono, null);
        EditText marca = v.findViewById(R.id.txtMarca);
        EditText modelo = v.findViewById(R.id.txtModelo);
        EditText precio = v.findViewById(R.id.txtPrecio);
        CheckBox activo = v.findViewById(R.id.chkActivo);
        if (t != null) {
            marca.setText(t.marca);
            modelo.setText(t.modelo);
            precio.setText(t.precio.toPlainString());
            activo.setChecked(t.activo);
        }
        AlertDialog.Builder b = new AlertDialog.Builder(this)
                .setTitle(t == null ? "Nuevo teléfono" : "Editar teléfono #" + t.codTelefono)
                .setView(v)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Guardar", (d, w) -> Ui.ejecutar(this, progreso,
                        () -> Ui.ctrl.guardarTelefono(t == null ? null : t.codTelefono, marca.getText().toString(),
                                modelo.getText().toString(), precio.getText().toString(), activo.isChecked()),
                        g -> {
                            Toast.makeText(this, "Teléfono guardado", Toast.LENGTH_SHORT).show();
                            cargar();
                        }));
        if (t != null && t.activo) {
            b.setNeutralButton("Dar de baja", (d, w) -> Ui.ejecutar(this, progreso, () -> {
                Ui.ctrl.eliminarTelefono(t.codTelefono);
                return t;
            }, x -> cargar()));
        }
        b.show();
    }
}
