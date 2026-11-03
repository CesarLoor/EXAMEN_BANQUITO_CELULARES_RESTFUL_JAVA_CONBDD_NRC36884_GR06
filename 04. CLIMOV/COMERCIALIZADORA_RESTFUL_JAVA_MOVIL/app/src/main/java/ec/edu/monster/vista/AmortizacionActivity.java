package ec.edu.monster.vista;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import ec.edu.monster.R;
import ec.edu.monster.modelo.Credito;
import java.util.List;

/** Consulta de la tabla de amortización de los préstamos de un cliente. */
public class AmortizacionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Ui.ctrl == null) {
            finish();
            return;
        }
        setContentView(R.layout.activity_amortizacion);
        EditText cedula = findViewById(R.id.txtCedula);
        LinearLayout resultado = findViewById(R.id.contResultado);
        ProgressBar progreso = findViewById(R.id.progreso);

        findViewById(R.id.btnConsultar).setOnClickListener(v -> {
            String ced = cedula.getText().toString();
            Ui.ejecutar(this, progreso, () -> Ui.ctrl.creditosCliente(ced), (List<Credito> creditos) -> {
                resultado.removeAllViews();
                if (creditos.isEmpty()) {
                    TextView vacio = new TextView(this);
                    vacio.setText("El cliente no tiene préstamos en BanQuito.");
                    resultado.addView(vacio);
                }
                for (Credito c : creditos) {
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    lp.bottomMargin = Ui.dp(this, 20);
                    resultado.addView(Ui.tablaAmortizacion(this, c), lp);
                }
            });
        });
    }
}
