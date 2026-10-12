import java.math.BigDecimal;
import java.math.RoundingMode;

/** Guarda un cobro realizado. Cotizar o asignar no crea ingresos. */
public class Ingreso {
    // Los tres datos son privados y no cambian después de guardar el cobro.
    private final Trabajo trabajo;
    private final TipoIngreso tipo;
    private final double monto;

    /** Valida y guarda el trabajo, el motivo y el monto del cobro. */
    public Ingreso(Trabajo trabajo, TipoIngreso tipo, double monto) {
        // Todo ingreso debe estar asociado a un trabajo y a un motivo válido.
        if (trabajo == null || tipo == null) {
            throw new IllegalArgumentException("El ingreso necesita un trabajo y un tipo.");
        }
        // El cobro debe ser una cantidad positiva que pueda representarse.
        if (!Double.isFinite(monto) || monto <= 0) {
            throw new IllegalArgumentException("El monto del ingreso debe ser positivo y finito.");
        }
        // Se guarda el monto en centavos para sumar lo mismo que se muestra al cobrar.
        double montoRedondeado = BigDecimal.valueOf(monto).setScale(2, RoundingMode.HALF_UP).doubleValue();
        // Un monto que se vuelve cero después del redondeo tampoco es un ingreso válido.
        if (montoRedondeado <= 0) {
            throw new IllegalArgumentException("El ingreso debe ser de al menos un centavo.");
        }
        this.trabajo = trabajo;
        this.tipo = tipo;
        this.monto = montoRedondeado;
    }

    // Los reportes usan estos getters para consultar y sumar los cobros.
    public Trabajo getTrabajo() { return trabajo; }
    public TipoIngreso getTipo() { return tipo; }
    public double getMonto() { return monto; }
}
