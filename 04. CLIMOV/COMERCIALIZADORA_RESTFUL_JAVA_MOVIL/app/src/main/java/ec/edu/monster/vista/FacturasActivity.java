package ec.edu.monster.vista;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import androidx.appcompat.app.AppCompatActivity;
import ec.edu.monster.R;
import ec.edu.monster.modelo.Factura;
import java.util.ArrayList;
import java.util.List;

public class FacturasActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Ui.ctrl == null) {
            finish();
            return;
        }
        setContentView(R.layout.activity_facturas);
        List<String> filas = new ArrayList<>();
        ArrayAdapter<String> ad = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, filas);
        ((ListView) findViewById(R.id.lista)).setAdapter(ad);
        ProgressBar progreso = findViewById(R.id.progreso);
        Ui.ejecutar(this, progreso, Ui.ctrl::listarFacturas, (List<Factura> lista) -> {
            for (Factura f : lista) {
                filas.add(f.numFactura + "   $" + Ui.dinero(f.total) + "\n" + f.nombreCliente + " · "
                        + f.formaPago + (f.codCredito == null ? "" : " #" + f.codCredito + " (" + f.plazoMeses + " m)")
                        + "\n" + f.fecha.replace('T', ' '));
            }
            if (lista.isEmpty()) {
                filas.add("Aún no hay facturas");
            }
            ad.notifyDataSetChanged();
        });
    }
}
