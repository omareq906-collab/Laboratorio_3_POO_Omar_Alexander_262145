/** Representa una sola imagen y utiliza los datos comunes de Trabajo. */
public class ImagenFija extends Trabajo {
    /** Primero inicializa Trabajo y luego revisa el límite propio de una imagen. */
    public ImagenFija(String codigo, String estudio, String proyecto,
                      double horasEstimadas, Horario horario) {
        // super llama al constructor de la clase padre.
        super(codigo, estudio, proyecto, horasEstimadas, horario);
        // Una imagen fija no puede necesitar más de dos horas de cómputo.
        if (horasEstimadas > 2) {
            throw new IllegalArgumentException("Una imagen fija admite como máximo 2 horas.");
        }
    }

    /** Cobra un precio fijo según el horario, sin multiplicar por las horas. */
    @Override
    public double calcularCosto() {
        // Durante el día se cobra la tarifa diurna.
        if (getHorario() == Horario.DIURNO) {
            return 45.00;
        }
        // El único horario restante es nocturno.
        return 30.00;
    }

    /** Devuelve el plazo de entrega de la imagen. */
    @Override
    public int calcularEntrega() {
        // Se elige el plazo que corresponde al horario guardado.
        if (getHorario() == Horario.DIURNO) {
            // La entrega diurna de una imagen tarda cuatro horas.
            return 4;
        }
        // En horario nocturno la entrega tarda diez horas.
        return 10;
    }

    /** Una imagen fija puede ejecutarse en cualquier nodo existente. */
    @Override
    public boolean esCompatible(Nodo nodo) {
        // No exige GPU; solo se necesita que el nodo exista.
        return nodo != null;
    }

    /** Permite hasta tres ejecuciones, contando la primera. */
    @Override
    public int maxIntentos() {
        // Este límite lo consulta Trabajo cuando un intento falla.
        return 3;
    }

    /** Identifica esta categoría en las consultas y reportes. */
    @Override
    public String categoria() {
        // El sistema usa este nombre sin revisar el tipo del objeto.
        return "Imagen fija";
    }
}
