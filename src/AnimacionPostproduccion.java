import java.math.BigDecimal;
import java.math.RoundingMode;

/** Es una animación que también necesita composición y recursos licenciados. */
public class AnimacionPostproduccion extends Animacion {
    // Guarda el valor declarado de los modelos, texturas u otros recursos.
    private final double valorRecursos;

    /** Aprovecha el constructor de Animacion y agrega el valor de los recursos. */
    public AnimacionPostproduccion(String codigo, String estudio, String proyecto,
                                  double horasEstimadas, Horario horario,
                                  int fotogramas, double valorRecursos) {
        // Animacion llama a Trabajo y también valida los fotogramas.
        super(codigo, estudio, proyecto, horasEstimadas, horario, fotogramas);
        // El valor declarado debe ser una cantidad válida y mayor que cero.
        if (!Double.isFinite(valorRecursos) || valorRecursos <= 0) {
            throw new IllegalArgumentException("El valor de los recursos debe ser positivo y finito.");
        }
        this.valorRecursos = valorRecursos;
    }

    /** Agrega la composición y el recargo al costo de la animación normal. */
    @Override
    public double calcularCosto() {
        // super reutiliza el cálculo de fotogramas y horario de Animacion.
        double costoAnimacion = super.calcularCosto();
        // Se detiene el cálculo si las horas producen un precio demasiado grande.
        if (!Double.isFinite(costoAnimacion)) {
            throw new IllegalArgumentException("El costo de la animación supera el rango permitido.");
        }
        BigDecimal base = BigDecimal.valueOf(costoAnimacion);
        BigDecimal composicion = new BigDecimal("80.00");
        BigDecimal recargo = BigDecimal.valueOf(valorRecursos).multiply(new BigDecimal("0.05"));
        // Se suman los cargos con decimales exactos y se redondea al terminar.
        // HALF_UP hace que Q206.205 se cobre como Q206.21, sin perder un centavo.
        return base.add(composicion).add(recargo).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    /** La composición necesita doce horas adicionales de entrega. */
    @Override
    public int calcularEntrega() {
        // Se conserva el plazo de Animacion y se suma el tiempo adicional.
        return super.calcularEntrega() + 12;
    }

    /** Esta categoría solo permite dos intentos en total. */
    @Override
    public int maxIntentos() {
        // Reemplaza el límite de tres que tiene la animación normal.
        return 2;
    }

    /** Distingue la postproducción en los reportes. */
    @Override
    public String categoria() {
        // El sistema la cuenta como una categoría diferente.
        return "Animación con postproducción";
    }

    // esCompatible se hereda de Animacion porque acepta los mismos nodos.
}
