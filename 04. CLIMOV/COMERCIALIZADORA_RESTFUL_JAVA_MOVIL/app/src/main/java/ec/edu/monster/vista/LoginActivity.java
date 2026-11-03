package ec.edu.monster.vista;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import ec.edu.monster.R;
import ec.edu.monster.controlador.ComercializadoraControlador;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        EditText usuario = findViewById(R.id.txtUsuario);
        EditText clave = findViewById(R.id.txtClave);
        EditText servidor = findViewById(R.id.txtServidor);
        Button ingresar = findViewById(R.id.btnIngresar);

        // Emulador: 10.0.2.2 = la PC. Celular por USB: 127.0.0.1 con "adb reverse tcp:8080 tcp:8080".
        // Por Wi-Fi se escribe la IP de la PC.
        boolean emulador = Build.FINGERPRINT.contains("generic") || Build.MODEL.contains("sdk_gphone");
        SharedPreferences prefs = getSharedPreferences("config", MODE_PRIVATE);
        servidor.setText(prefs.getString("servidor", emulador ? "10.0.2.2:8080" : "127.0.0.1:8080"));

        ingresar.setOnClickListener(v -> {
            String host = servidor.getText().toString().trim();
            ComercializadoraControlador ctrl =
                    new ComercializadoraControlador("http://" + host + "/COMERCIALIZADORA/api");
            if (ctrl.login(usuario.getText().toString().trim(), clave.getText().toString())) {
                prefs.edit().putString("servidor", host).apply();
                Ui.ctrl = ctrl;
                startActivity(new Intent(this, MenuActivity.class));
                finish();
            } else {
                Toast.makeText(this, "Usuario o contraseña incorrectos", Toast.LENGTH_SHORT).show();
                clave.setText("");
            }
        });
    }
}
