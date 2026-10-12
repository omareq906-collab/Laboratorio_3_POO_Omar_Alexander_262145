/**
 * Reúne los datos que comparten todos los trabajos.
 * Es abstracta porque no se pueden crear trabajos genéricos, solo categorías concretas y bien definidas con sus "reglas"
 */
public abstract class Trabajo {
    // Estos datos se reciben al crear el trabajo y ya no cambian
    private final String codigo;
    private final String estudio;
    private final String proyecto;
    private final double horasEstimadas;
    private final Horario horario;

    // Solo los métodos de esta clase pueden cambiar el estado y los intentos.
    private EstadoTrabajo estado;
    private int intentos;

    /** Recibe y valida los datos comunes antes de crear una categoría concreta. */
    protected Trabajo(String codigo, String estudio, String proyecto,
                      double horasEstimadas, Horario horario) {
        // trim permite detectar códigos formados únicamente por espacios.
        if (codigo == null || codigo.trim().isEmpty()) {
            throw new IllegalArgumentException("El código del trabajo no puede estar vacío.");
        }
        // El estudio y el proyecto son necesarios para identificar el encargo.
        if (estudio == null || estudio.trim().isEmpty()
                || proyecto == null || proyecto.trim().isEmpty()) {
            throw new IllegalArgumentException("Escribe el estudio y el proyecto.");
        }
        // También se rechazan infinito y NaN, que no representan horas válidas.
        if (!Double.isFinite(horasEstimadas) || horasEstimadas <= 0) {
            throw new IllegalArgumentException("Las horas estimadas deben ser positivas y finitas.");
        }
        // El horario debe ser uno de los valores del enum Horario.
        if (horario == null) {
            throw new IllegalArgumentException("Selecciona un horario válido.");
        }
        // Se guardan los datos sin espacios sobrantes en los textos.
        this.codigo = codigo.trim();
        this.estudio = estudio.trim();
        this.proyecto = proyecto.trim();
        this.horasEstimadas = horasEstimadas;
        this.horario = horario;
        // Todo trabajo empieza pendiente y sin ejecuciones realizadas.
        this.estado = EstadoTrabajo.EN_COLA;
        this.intentos = 0;
    }

    // Los getters permiten consultar los atributos privados sin modificarlos
    public String getCodigo() { return codigo; }
    public String getEstudio() { return estudio; }
    public String getProyecto() { return proyecto; }
    public double getHorasEstimadas() { return horasEstimadas; }
    public Horario getHorario() { return horario; }
    public EstadoTrabajo getEstado() { return estado; }
    public int getIntentos() { return intentos; }

    /** Cada categoría devuelve su precio de registro en quetzales */
    public abstract double calcularCosto();

    /** Cada categoría indica cuantas horas estimadas se necesita para entregar el resultado */
    public abstract int calcularEntrega();

    /** Cada categoría decide si puede ejecutarse en el nodo recibido o si no */
    public abstract boolean esCompatible(Nodo nodo);

    /** Cada categoría establece su cantidad máxima de intentos */
    public abstract int maxIntentos();

    /** Devuelve el nombre que se usa para agrupar los reportes */
    public abstract String categoria();

    /** Inicia una ejecución. Solo se llama cuando el lote ya pasó sus validaciones. */
    void iniciarIntento() {
        // Un trabajo terminado, cancelado o en ejecución no puede iniciarse de nuevo
        if (estado != EstadoTrabajo.EN_COLA || intentos >= maxIntentos()) {
            throw new IllegalStateException("El trabajo " + codigo + " no puede iniciar otro intento.");
        }
        // El intento se cuenta al ejecutar, JAMAS al asignar el trabajo a un lote.
        intentos++;
        // El trabajo deja la cola porque su ejecución ya empezo
        estado = EstadoTrabajo.EN_EJECUCION;
    }

    /** Guarda el estado que corresponde al resultado de una ejecución. */
    void resolver(boolean exito) {
        // Solo se puede resolver un intento que se encuentra en ejecución.
        if (estado != EstadoTrabajo.EN_EJECUCION) {
            throw new IllegalStateException("El trabajo " + codigo + " no está en ejecución.");
        }
        // Un resultado exitoso termina el trabajo, incluso si era su último intento.
        if (exito) {
            estado = EstadoTrabajo.COMPLETADO;
        } else if (intentos < maxIntentos()) {
            // Si falló y todavía tiene intentos, regresa a la cola.
            estado = EstadoTrabajo.EN_COLA;
        } else {
            // Si falló en su último intento, ya no se puede volver a ejecutar.
            estado = EstadoTrabajo.CANCELADO;
        }
    }
}