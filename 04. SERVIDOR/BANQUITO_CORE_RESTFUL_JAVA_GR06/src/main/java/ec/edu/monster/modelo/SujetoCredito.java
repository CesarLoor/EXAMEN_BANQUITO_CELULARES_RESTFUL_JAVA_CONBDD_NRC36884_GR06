package ec.edu.monster.modelo;

import java.util.ArrayList;
import java.util.List;

/** Respuesta del servicio que valida si una persona es sujeto de crédito. */
public class SujetoCredito {

    public String cedula;
    public Integer codCliente;
    public String nombre;
    public boolean sujetoCredito;
    /** Reglas que NO se cumplen (vacía cuando es sujeto de crédito). */
    public List<String> motivos = new ArrayList<>();
}
