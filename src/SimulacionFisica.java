import java.math.BigDecimal;
import java.math.RoundingMode;

/** Representa simulaciones que necesitan GPU y solo se ejecutan de noche. */
public class SimulacionFisica extends Trabajo {
    // La resolución se expresa en millones de celdas.
    private final double resolucion;

    /** Valida los datos comunes, el horario y la resolución de la malla. */
    public SimulacionFisica(String codigo, String estudio, String proyecto,
                           double horasEstimadas, Horario horario, double resolucion) {
        // Primero se inicializa la parte que todas las categorías comparten.
        super(codigo, estudio, proyecto, horasEstimadas, horario);
        // Las GPU se reservan para otros servicios durante el día.
        if (horario != Horario.NOCTURNO) {
            throw new IllegalArgumentException("Una simulación física solo admite horario nocturno.");
        }
        // Los extremos 1 y 50 sí se aceptan; los valores no finitos se rechazan.
        if (!Double.isFinite(resolucion) || resolucion < 1 || resolucion > 50) {
            throw new IllegalArgumentException("La resolución debe estar entre 1 y 50 millones de celdas.");
        }
        this.resolucion = resolucion;
    }

    /** Cobra la base nocturna, las horas estimadas y el uso de GPU. */
    @Override
    public double calcularCosto() {
        // Se usa la base de Q40 acordada en el análisis. Aquí no se redondean horas.
        BigDecimal base = new BigDecimal("40.00");
        BigDecimal cobroHoras = BigDecimal.valueOf(getHorasEstimadas()).multiply(new BigDecimal("22.00"));
        BigDecimal cobroGpu = new BigDecimal("65.00");
        // Se redondea únicamente el monto final a centavos, usando decimales exactos.
        return base.add(cobroHoras).add(cobroGpu).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    /** Todas las simulaciones tienen el mismo plazo de entrega. */
    @Override
    public int calcularEntrega() {
        // El plazo no depende de la resolución de la malla.
        return 18;
    }

    /** Comprueba si el nodo tiene el recurso necesario para simular. */
    @Override
    public boolean esCompatible(Nodo nodo) {
        // Primero se verifica que exista y luego se consulta si tiene GPU.
        return nodo != null && nodo.tieneGpu();
    }

    /** Una simulación solo puede ejecutarse una vez. */
    @Override
    public int maxIntentos() {
        // Si ese intento falla, Trabajo.resolver la deja cancelada.
        return 1;
    }

    /** Devuelve el nombre usado para esta categoría. */
    @Override
    public String categoria() {
        // Permite que los reportes agrupen sus trabajos e ingresos.
        return "Simulación física";
    }
}
