package ec.edu.monster.vista;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import ec.edu.monster.R;

public class MenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Ui.ctrl == null) { // el sistema cerró la app: volver a iniciar sesión
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        setContentView(R.layout.activity_menu);
        findViewById(R.id.btnVenta).setOnClickListener(v -> startActivity(new Intent(this, VentaActivity.class)));
        findViewById(R.id.btnCatalogo).setOnClickListener(v -> startActivity(new Intent(this, CatalogoActivity.class)));
        findViewById(R.id.btnAmortizacion).setOnClickListener(v -> startActivity(new Intent(this, AmortizacionActivity.class)));
        findViewById(R.id.btnFacturas).setOnClickListener(v -> startActivity(new Intent(this, FacturasActivity.class)));
        findViewById(R.id.btnSalir).setOnClickListener(v -> {
            Ui.ctrl = null;
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }
}
