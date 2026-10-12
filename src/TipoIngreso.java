/** Permite separar el cobro inicial de los cobros por nuevos intentos. */
public enum TipoIngreso {
    // Cobro completo que se guarda al registrar un nuevo trabajo.
    REGISTRO,
    // Cargo de Q25 que se guarda al iniciar un intento adicional al primero.
    REINTENTO
}
