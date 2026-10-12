/** Guarda las características de una computadora de la granja de renderizado. */
public class Nodo {
    // Un nodo conserva el mismo código, capacidad y GPU durante esta sesión.
    private final String codigo;
    private final double capacidad;
    private final boolean gpu;

    /** Recibe los datos del equipo y valida su capacidad. */
    public Nodo(String codigo, double capacidad, boolean gpu) {
        // Se necesita un código para localizar el nodo en el sistema.
        if (codigo == null || codigo.trim().isEmpty()) {
            throw new IllegalArgumentException("El código del nodo no puede estar vacío.");
        }
        // Se rechazan cero, negativos, infinito y NaN.
        if (!Double.isFinite(capacidad) || capacidad <= 0) {
            throw new IllegalArgumentException("La capacidad debe ser positiva y finita.");
        }
        this.codigo = codigo.trim();
        this.capacidad = capacidad;
        this.gpu = gpu;
    }

    // Los lotes y trabajos consultan estas características sin poder cambiarlas.
    public String getCodigo() { return codigo; }
    public double getCapacidad() { return capacidad; }

    /** Indica si el equipo tiene GPU para que el trabajo revise su compatibilidad. */
    public boolean tieneGpu() {
        // Solo informa una característica; cada categoría decide si la necesita.
        return gpu;
    }
}
