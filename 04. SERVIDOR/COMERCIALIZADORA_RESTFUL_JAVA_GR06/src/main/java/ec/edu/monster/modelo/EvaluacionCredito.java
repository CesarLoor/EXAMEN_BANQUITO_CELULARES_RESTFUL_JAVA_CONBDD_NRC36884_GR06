package ec.edu.monster.modelo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** Resultado combinado de los WS de BanQuito "sujeto de crédito" + "monto máximo". */
public class EvaluacionCredito {

    public String cedula;
    public Integer codCliente;
    public String nombre;
    public boolean sujetoCredito;
    public List<String> motivos = new ArrayList<>();
    public BigDecimal montoMaximo;
}
