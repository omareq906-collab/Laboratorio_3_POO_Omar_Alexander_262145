import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Organiza un grupo de trabajos que se ejecutan en un mismo nodo. */
public class Lote {
    // El código y el nodo se eligen al crear el lote.
    private final String codigo;
    private final Nodo nodo;
    // La lista conserva los trabajos, incluso cuando el lote ya está cerrado.
    private final List<Trabajo> trabajos;
    // Cada resultado relaciona un código con true (completado) o false (fallido).
    private final Map<String, Boolean> resultados;
    private EstadoLote estado;

    /** Crea un lote vacío que todavía permite agregar o retirar trabajos. */
    public Lote(String codigo, Nodo nodo) {
        // Se valida antes de guardar los datos del nuevo lote.
        if (codigo == null || codigo.trim().isEmpty()) {
            throw new IllegalArgumentException("El código del lote no puede estar vacío.");
        }
        if (nodo == null) {
            throw new IllegalArgumentException("El lote necesita un nodo existente.");
        }
        this.codigo = codigo.trim();
        this.nodo = nodo;
        this.trabajos = new ArrayList<>();
        this.resultados = new LinkedHashMap<>();
        this.estado = EstadoLote.EN_PREPARACION;
    }

    // Estos datos se pueden consultar, pero no se modifican desde el menú.
    public String getCodigo() { return codigo; }
    public Nodo getNodo() { return nodo; }
    public EstadoLote getEstado() { return estado; }

    /** Entrega una copia para que quitar elementos afuera no cambie el lote. */
    public List<Trabajo> getTrabajos() {
        // Se copian las referencias a los trabajos en una lista diferente.
        return new ArrayList<>(trabajos);
    }

    /** Suma las horas estimadas del lote. */
    public double horasAsignadas() {
        double total = 0.0;
        for (Trabajo trabajo : trabajos) {
            total += trabajo.getHorasEstimadas();
        }
        return total;
    }

    /** Calcula la capacidad restante del nodo. */
    public double capacidadDisponible() {
        double disponible = nodo.getCapacidad();
        for (Trabajo trabajo : trabajos) {
            disponible -= trabajo.getHorasEstimadas();
        }
        return disponible;
    }

    /** Revisa la capacidad y la compatibilidad antes de agregar el trabajo. */
    void asignar(Trabajo trabajo) {
        if (estado != EstadoLote.EN_PREPARACION) {
            throw new IllegalStateException("Solo se puede asignar a un lote en preparación.");
        }
        if (trabajo == null) {
            throw new IllegalArgumentException("El trabajo no existe.");
        }
        if (trabajo.getEstado() != EstadoTrabajo.EN_COLA
                || trabajo.getIntentos() >= trabajo.maxIntentos()) {
            throw new IllegalStateException("El trabajo no está disponible para asignarse.");
        }

        double total = trabajo.getHorasEstimadas();
        for (Trabajo actual : trabajos) {
            if (actual.getCodigo().equals(trabajo.getCodigo())) {
                throw new IllegalStateException("El trabajo ya pertenece a este lote.");
            }
            total += actual.getHorasEstimadas();
        }

        if (total > nodo.getCapacidad()) {
            throw new IllegalArgumentException("Los trabajos superan la capacidad del nodo.");
        }
        if (!trabajo.esCompatible(nodo)) {
            throw new IllegalArgumentException("El trabajo no es compatible con este nodo.");
        }

        trabajos.add(trabajo);
    }

    /** Retira un trabajo antes de iniciar el lote. */
    void retirar(Trabajo trabajo) {
        // Los lotes en ejecución o cerrados conservan su lista de trabajos.
        if (estado != EstadoLote.EN_PREPARACION) {
            throw new IllegalStateException("Solo se puede retirar de un lote en preparación.");
        }
        // Se verifica la pertenencia para no aceptar un retiro que no corresponde.
        if (trabajo == null || !trabajos.contains(trabajo)) {
            throw new IllegalArgumentException("El trabajo no pertenece a este lote.");
        }
        // El trabajo ya estaba en cola: basta con quitar su asignación.
        trabajos.remove(trabajo);
    }

    /** Inicia todos los trabajos y devuelve el cargo por reintentos. */
    double iniciar() {
        if (estado != EstadoLote.EN_PREPARACION) {
            throw new IllegalStateException("El lote ya inició o está cerrado.");
        }
        if (trabajos.isEmpty()) {
            throw new IllegalStateException("No se puede iniciar un lote vacío.");
        }

        double cargoReintentos = 0.00;
        double totalHoras = 0.0;

        for (Trabajo trabajo : trabajos) {
            if (trabajo.getEstado() != EstadoTrabajo.EN_COLA
                    || trabajo.getIntentos() >= trabajo.maxIntentos()) {
                throw new IllegalStateException("El trabajo " + trabajo.getCodigo() + " no puede iniciar.");
            }
            if (!trabajo.esCompatible(nodo)) {
                throw new IllegalStateException("Hay un trabajo incompatible con el nodo.");
            }
            totalHoras += trabajo.getHorasEstimadas();
            if (trabajo.getIntentos() > 0) {
                cargoReintentos += 25.00;
            }
        }

        if (totalHoras > nodo.getCapacidad()) {
            throw new IllegalStateException("El lote supera la capacidad del nodo.");
        }

        for (Trabajo trabajo : trabajos) {
            trabajo.iniciarIntento();
        }
        estado = EstadoLote.EN_EJECUCION;
        return cargoReintentos;
    }

    /** Registra un resultado y cierra el lote cuando ya no quedan resultados pendientes. */
    void registrarResultado(Trabajo trabajo, boolean exito) {
        // Solo los lotes en ejecución reciben resultados.
        if (estado != EstadoLote.EN_EJECUCION) {
            throw new IllegalStateException("El lote no está en ejecución.");
        }
        // Se evita resolver un trabajo que no pertenece a este lote.
        if (trabajo == null || !trabajos.contains(trabajo)) {
            throw new IllegalArgumentException("El trabajo no pertenece a este lote.");
        }
        // Un resultado ya guardado no se puede cambiar ni volver a registrar.
        if (resultados.containsKey(trabajo.getCodigo())) {
            throw new IllegalStateException("Este trabajo ya tiene resultado en el lote.");
        }
        if (trabajo.getEstado() != EstadoTrabajo.EN_EJECUCION) {
            throw new IllegalStateException("El trabajo no está en ejecución.");
        }
        // Trabajo decide si queda completado, en cola o cancelado.
        trabajo.resolver(exito);
        // El mapa conserva el resultado de este intento aunque el trabajo se reintente después.
        resultados.put(trabajo.getCodigo(), exito);
        // Al completar todos los resultados, el nodo deja de tener un lote abierto.
        if (resultados.size() == trabajos.size()) {
            estado = EstadoLote.CERRADO;
        }
    }
}
