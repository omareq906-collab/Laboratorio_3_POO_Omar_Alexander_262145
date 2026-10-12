/** Agrega fotogramas y las reglas de cobro de una animación. */
public class Animacion extends Trabajo {
    // La cantidad de fotogramas no cambia después del registro.
    private final int fotogramas;

    /** Inicializa los datos comunes y valida la cantidad de fotogramas. */
    public Animacion(String codigo, String estudio, String proyecto,
                     double horasEstimadas, Horario horario, int fotogramas) {
        // Trabajo valida y guarda la parte común.
        super(codigo, estudio, proyecto, horasEstimadas, horario);
        // El tipo int ya impide decimales; aquí se impiden cero y negativos.
        if (fotogramas <= 0) {
            throw new IllegalArgumentException("Los fotogramas deben ser un entero positivo.");
        }
        this.fotogramas = fotogramas;
    }

    /** Calcula las horas que se cobran, incluyendo la fracción como hora completa. */
    protected double horasFacturables() {
        double horasPorFotogramas = fotogramas * 0.02;
        double mayor = Math.max(getHorasEstimadas(), horasPorFotogramas);
        // ceil redondea hacia arriba: por ejemplo, 12.2 pasa a ser 13.
        return Math.ceil(mayor);
    }

    /** Suma la tarifa del horario y el cobro de las horas facturables. */
    @Override
    public double calcularCosto() {
        double base = 40.00;
        // Se cambia la base únicamente cuando la animación es diurna.
        if (getHorario() == Horario.DIURNO) {
            base = 60.00;
        }
        // Este cálculo no modifica las horas estimadas que usa el nodo.
        return base + 15.00 * horasFacturables();
    }

    /** Devuelve el plazo de entrega según el horario de la animación. */
    @Override
    public int calcularEntrega() {
        // La animación diurna se entrega antes que la nocturna.
        if (getHorario() == Horario.DIURNO) {
            // Durante el día se entrega en veinticuatro horas.
            return 24;
        }
        // Durante la noche se utiliza el plazo de treinta y seis horas.
        return 36;
    }

    /** Una animación puede ejecutarse con GPU o sin ella. */
    @Override
    public boolean esCompatible(Nodo nodo) {
        // Basta con recibir un nodo existente.
        return nodo != null;
    }

    /** Permite hasta tres intentos para una animación normal. */
    @Override
    public int maxIntentos() {
        // La postproducción sobrescribe este método con otro límite.
        return 3;
    }

    /** Devuelve el nombre de esta categoría. */
    @Override
    public String categoria() {
        // Este texto sirve como grupo en los reportes.
        return "Animación";
    }
}
