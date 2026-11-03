package ec.edu.monster.vista;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import ec.edu.monster.controlador.ComercializadoraControlador;
import ec.edu.monster.modelo.Credito;
import ec.edu.monster.modelo.CuotaAmortizacion;
import java.math.BigDecimal;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Supplier;

/** Utilitarios de la vista móvil: controlador compartido, llamadas en segundo plano y tabla de amortización. */
final class Ui {

    /** Lo crea LoginActivity con la IP del servidor que ingresa el usuario. */
    static ComercializadoraControlador ctrl;

    private static final ExecutorService HILOS = Executors.newFixedThreadPool(2);
    private static final Handler PRINCIPAL = new Handler(Looper.getMainLooper());

    private Ui() {
    }

    /** Android no permite red en el hilo principal: la llamada REST va en otro hilo y el resultado vuelve a la UI. */
    static <T> void ejecutar(Activity a, ProgressBar progreso, Supplier<T> tarea, Consumer<T> alTerminar) {
        if (progreso != null) {
            progreso.setVisibility(View.VISIBLE);
        }
        HILOS.execute(() -> {
            T r = null;
            RuntimeException error = null;
            try {
                r = tarea.get();
            } catch (RuntimeException e) {
                error = e;
            }
            final T res = r;
            final RuntimeException err = error;
            PRINCIPAL.post(() -> {
                if (a.isFinishing()) {
                    return;
                }
                if (progreso != null) {
                    progreso.setVisibility(View.GONE);
                }
                if (err != null) {
                    error(a, err.getMessage());
                } else {
                    alTerminar.accept(res);
                }
            });
        });
    }

    static void error(Context c, String mensaje) {
        new AlertDialog.Builder(c).setTitle("Atención").setMessage(mensaje).setPositiveButton("OK", null).show();
    }

    static String dinero(BigDecimal v) {
        return v == null ? "" : String.format(Locale.US, "%,.2f", v);
    }

    /** Tabla de amortización con el formato del enunciado (# Cuota, Valor Cuota, Interés, Capital, Saldo). */
    static View tablaAmortizacion(Context c, Credito cr) {
        LinearLayout cont = new LinearLayout(c);
        cont.setOrientation(LinearLayout.VERTICAL);

        TextView resumen = new TextView(c);
        resumen.setText(String.format(Locale.US, "Crédito #%d · %s (%s)%nMonto $%s · %d cuotas · Tasa %s%% anual%n"
                + "Cuota fija $%s · Intereses $%s · Total $%s", cr.codCredito, cr.nombre, cr.estado, dinero(cr.monto),
                cr.plazoMeses, cr.tasaAnual, dinero(cr.cuotaFija), dinero(cr.totalIntereses), dinero(cr.totalPagar)));
        resumen.setTextColor(Color.parseColor("#1C2433"));
        resumen.setPadding(0, 0, 0, dp(c, 8));
        cont.addView(resumen);

        TableLayout t = new TableLayout(c);
        t.addView(fila(c, true, "#", "Vence", "Cuota", "Interés", "Capital", "Saldo"));
        t.addView(fila(c, false, "0", cr.fechaCredito, "", "", "", dinero(cr.monto)));
        for (CuotaAmortizacion q : cr.tablaAmortizacion) {
            t.addView(fila(c, false, String.valueOf(q.numCuota), q.fechaVencimiento, dinero(q.valorCuota),
                    dinero(q.interesPagado), dinero(q.capitalPagado), dinero(q.saldo)));
        }
        HorizontalScrollView hs = new HorizontalScrollView(c);
        hs.addView(t);
        cont.addView(hs);
        return cont;
    }

    private static TableRow fila(Context c, boolean cabecera, String... celdas) {
        TableRow r = new TableRow(c);
        if (cabecera) {
            r.setBackgroundColor(Color.parseColor("#E8EEFB"));
        }
        for (int i = 0; i < celdas.length; i++) {
            TextView tv = new TextView(c);
            tv.setText(celdas[i]);
            tv.setPadding(dp(c, 8), dp(c, 6), dp(c, 8), dp(c, 6));
            tv.setGravity(i < 2 ? Gravity.START : Gravity.END);
            tv.setTextColor(Color.parseColor(cabecera ? "#0B3D91" : "#1C2433"));
            if (cabecera) {
                tv.setTypeface(Typeface.DEFAULT_BOLD);
            }
            r.addView(tv);
        }
        return r;
    }

    static int dp(Context c, int v) {
        return Math.round(v * c.getResources().getDisplayMetrics().density);
    }
}
