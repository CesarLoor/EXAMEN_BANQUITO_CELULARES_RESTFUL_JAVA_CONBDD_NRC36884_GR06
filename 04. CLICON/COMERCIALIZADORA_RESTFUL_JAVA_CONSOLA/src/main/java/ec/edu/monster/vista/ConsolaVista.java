package ec.edu.monster.vista;

import ec.edu.monster.controlador.ComercializadoraControlador;
import ec.edu.monster.modelo.Credito;
import ec.edu.monster.modelo.CuotaAmortizacion;
import ec.edu.monster.modelo.DetalleFactura;
import ec.edu.monster.modelo.EvaluacionCredito;
import ec.edu.monster.modelo.Factura;
import ec.edu.monster.modelo.SolicitudVenta;
import ec.edu.monster.modelo.Telefono;
import ec.edu.monster.servicio.ComercializadoraServicio;
import ec.edu.monster.servicio.ServicioException;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

/** Cliente de consola de la Comercializadora de Teléfonos Celulares (GR06). */
public class ConsolaVista {

    private static final PrintStream out = new PrintStream(new FileOutputStream(FileDescriptor.out), true, StandardCharsets.UTF_8);
    private static final Scanner sc = new Scanner(System.in, StandardCharsets.UTF_8);
    private static ComercializadoraControlador ctrl;

    public static void main(String[] args) {
        Locale.setDefault(Locale.US); // montos en dólares con punto decimal: 1199.00
        titulo("COMERCIALIZADORA DE CELULARES - LOGIN");
        // Servidor: argumento 1 o se pregunta (Enter = misma PC). Desde otra máquina: IP de la PC servidor.
        String servidor = args.length > 0 ? args[0]
                : leer("Servidor IP:puerto [" + ComercializadoraServicio.SERVIDOR_POR_DEFECTO + "]: ");
        String url = ComercializadoraServicio.url(servidor);
        ctrl = new ComercializadoraControlador(url);

        int intentos = 0;
        while (true) {
            String usuario = leer("Usuario: ");
            String clave = leer("Contraseña: ");
            if (ctrl.login(usuario, clave)) {
                out.println("\n✔ Acceso concedido. Servidor: " + url);
                break;
            }
            if (++intentos == 3) {
                out.println("❌ Demasiados intentos. Adiós.");
                return;
            }
            out.println("❌ Usuario o contraseña incorrectos\n");
        }

        while (true) {
            titulo("MENÚ PRINCIPAL");
            out.println(" 1. Catálogo de teléfonos (mantenimiento)");
            out.println(" 2. Facturar venta (Efectivo / Crédito directo)");
            out.println(" 3. Consultar tabla de amortización de un cliente");
            out.println(" 4. Listar facturas");
            out.println(" 0. Salir");
            String op = leer("Opción: ");
            try {
                switch (op) {
                    case "1": menuCatalogo(); break;
                    case "2": facturar(); break;
                    case "3": consultarAmortizacion(); break;
                    case "4": listarFacturas(); break;
                    case "0": out.println("Hasta pronto."); return;
                    default: out.println("⚠ Opción inválida");
                }
            } catch (ServicioException e) {
                out.println("❌ " + e.getMessage());
            }
        }
    }

    // ================================================================ CATÁLOGO
    private static void menuCatalogo() {
        while (true) {
            titulo("CATÁLOGO DE TELÉFONOS");
            List<Telefono> lista = ctrl.listarTelefonos(true);
            imprimirCatalogo(lista);
            out.println("\n 1. Nuevo   2. Editar   3. Dar de baja   0. Volver");
            String op = leer("Opción: ");
            try {
                switch (op) {
                    case "1":
                        Telefono n = ctrl.guardarTelefono(null, leer("Marca: "), leer("Modelo: "), leer("Precio: "), true);
                        out.println("✔ Creado con código " + n.codTelefono);
                        break;
                    case "2": {
                        Telefono t = buscar(lista, leerEntero("Código a editar: "));
                        out.println("(Enter = mantener valor actual)");
                        String marca = leerOpcional("Marca [" + t.marca + "]: ", t.marca);
                        String modelo = leerOpcional("Modelo [" + t.modelo + "]: ", t.modelo);
                        String precio = leerOpcional("Precio [" + t.precio + "]: ", t.precio.toPlainString());
                        boolean activo = leerOpcional("¿Activo? (s/n) [" + (t.activo ? "s" : "n") + "]: ",
                                t.activo ? "s" : "n").equalsIgnoreCase("s");
                        ctrl.guardarTelefono(t.codTelefono, marca, modelo, precio, activo);
                        out.println("✔ Teléfono actualizado");
                        break;
                    }
                    case "3": {
                        Telefono t = buscar(lista, leerEntero("Código a dar de baja: "));
                        if (leer("¿Seguro de dar de baja " + t.marca + " " + t.modelo + "? (s/n): ").equalsIgnoreCase("s")) {
                            ctrl.eliminarTelefono(t.codTelefono);
                            out.println("✔ Teléfono dado de baja (ya no aparece en ventas)");
                        }
                        break;
                    }
                    case "0":
                        return;
                    default:
                        out.println("⚠ Opción inválida");
                }
            } catch (ServicioException e) {
                out.println("❌ " + e.getMessage());
            }
        }
    }

    private static void imprimirCatalogo(List<Telefono> lista) {
        out.printf("%-6s %-12s %-26s %10s  %s%n", "COD", "MARCA", "MODELO", "PRECIO", "ESTADO");
        for (Telefono t : lista) {
            out.printf("%-6d %-12s %-26s %10.2f  %s%n", t.codTelefono, t.marca, t.modelo, t.precio,
                    t.activo ? "A la venta" : "De baja");
        }
    }

    private static Telefono buscar(List<Telefono> lista, int cod) {
        for (Telefono t : lista) {
            if (t.codTelefono == cod) {
                return t;
            }
        }
        throw new ServicioException("No existe el teléfono " + cod);
    }

    // ================================================================ FACTURACIÓN
    private static void facturar() {
        titulo("FACTURAR VENTA");
        List<Telefono> catalogo = ctrl.listarTelefonos(false);
        imprimirCatalogo(catalogo);

        List<SolicitudVenta.Item> items = new ArrayList<>();
        do {
            Telefono t = buscar(catalogo, leerEntero("\nCódigo del teléfono: "));
            int cant = leerEntero("Cantidad: ");
            items.add(new SolicitudVenta.Item(t.codTelefono, cant));
            out.println("  + " + cant + " x " + t.marca + " " + t.modelo);
        } while (leer("¿Agregar otro teléfono? (s/n): ").equalsIgnoreCase("s"));

        String cedula = leer("Cédula del cliente: ");
        out.println("Forma de pago:  1. Efectivo (42% de descuento)   2. Crédito directo BanQuito");
        boolean credito = leer("Opción: ").equals("2");

        Factura f;
        if (credito) {
            EvaluacionCredito ev = ctrl.evaluarCredito(cedula);
            if (!ev.sujetoCredito) {
                out.println("❌ No es sujeto de crédito en BanQuito:");
                ev.motivos.forEach(m -> out.println("   - " + m));
                return;
            }
            out.println("✔ " + ev.nombre + " es sujeto de crédito. Monto máximo aprobado: $" + ev.montoMaximo);
            int plazo = leerEntero("Plazo en meses (3 - 18): ");
            f = ctrl.facturar(cedula, null, "CREDITO", plazo, items);
        } else {
            String nombre = leer("Nombre del cliente (Enter si ya es cliente): ");
            f = ctrl.facturar(cedula, nombre, "EFECTIVO", null, items);
        }
        imprimirFactura(f);
        if (f.credito != null) {
            imprimirAmortizacion(f.credito);
        }
    }

    private static void imprimirFactura(Factura f) {
        titulo("FACTURA " + f.numFactura);
        out.println("Fecha:   " + f.fecha.replace('T', ' '));
        out.println("Cliente: " + f.nombreCliente + "  (" + f.cedula + ")");
        out.println("Pago:    " + (f.codCredito != null
                ? "CRÉDITO DIRECTO BanQuito #" + f.codCredito + " a " + f.plazoMeses + " meses" : "EFECTIVO"));
        out.printf("%n%-5s %-34s %10s %11s%n", "CANT", "DESCRIPCIÓN", "P. UNIT", "SUBTOTAL");
        for (DetalleFactura d : f.detalles) {
            out.printf("%-5d %-34s %10.2f %11.2f%n", d.cantidad, d.descripcion, d.precioUnitario, d.subtotal);
        }
        out.printf("%51s %11.2f%n", "SUBTOTAL:", f.subtotal);
        out.printf("%51s %11.2f%n", "DESCUENTO:", f.descuento);
        out.printf("%51s %11.2f%n", "TOTAL:", f.total);
    }

    // ================================================================ AMORTIZACIÓN
    private static void consultarAmortizacion() {
        titulo("TABLA DE AMORTIZACIÓN");
        List<Credito> creditos = ctrl.creditosCliente(leer("Cédula del cliente: "));
        if (creditos.isEmpty()) {
            out.println("El cliente no tiene préstamos en BanQuito.");
            return;
        }
        for (Credito c : creditos) {
            imprimirAmortizacion(c);
        }
    }

    private static void imprimirAmortizacion(Credito c) {
        titulo("CRÉDITO #" + c.codCredito + " - " + c.nombre + " (" + c.estado + ")");
        out.printf("Valor préstamo: $%.2f   Cuotas: %d   Tasa anual: %.2f%%   Cuota fija: $%.2f%n",
                c.monto, c.plazoMeses, c.tasaAnual, c.cuotaFija);
        out.printf("%n%-8s %-12s %12s %12s %12s %12s%n", "# CUOTA", "VENCE", "VALOR CUOTA", "INTERÉS", "CAPITAL", "SALDO");
        out.printf("%-8d %-12s %12s %12s %12s %12.2f%n", 0, c.fechaCredito, "", "", "", c.monto);
        for (CuotaAmortizacion q : c.tablaAmortizacion) {
            out.printf("%-8d %-12s %12.2f %12.2f %12.2f %12.2f%n", q.numCuota, q.fechaVencimiento,
                    q.valorCuota, q.interesPagado, q.capitalPagado, q.saldo);
        }
        out.printf("Total intereses: $%.2f   Total a pagar: $%.2f%n", c.totalIntereses, c.totalPagar);
    }

    private static void listarFacturas() {
        titulo("FACTURAS EMITIDAS");
        out.printf("%-18s %-20s %-24s %-10s %10s%n", "NÚMERO", "FECHA", "CLIENTE", "PAGO", "TOTAL");
        for (Factura f : ctrl.listarFacturas()) {
            out.printf("%-18s %-20s %-24s %-10s %10.2f%n", f.numFactura, f.fecha.replace('T', ' '),
                    f.nombreCliente, f.formaPago, f.total);
        }
    }

    // ================================================================ utilitarios de entrada
    private static void titulo(String t) {
        out.println("\n==================================================================");
        out.println("  " + t);
        out.println("==================================================================");
    }

    private static String leer(String prompt) {
        out.print(prompt);
        return sc.hasNextLine() ? sc.nextLine().trim() : "0";
    }

    private static String leerOpcional(String prompt, String actual) {
        String v = leer(prompt);
        return v.isEmpty() ? actual : v;
    }

    private static int leerEntero(String prompt) {
        while (true) {
            String v = leer(prompt);
            try {
                return Integer.parseInt(v);
            } catch (NumberFormatException e) {
                out.println("⚠ Ingrese un número entero");
            }
        }
    }
}
