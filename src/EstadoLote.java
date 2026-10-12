/** Indica qué operaciones se pueden realizar sobre un lote. */
public enum EstadoLote {
    // Permite agregar y retirar trabajos antes de comenzar.
    EN_PREPARACION,
    // Sus trabajos ya iniciaron y faltan resultados por registrar.
    EN_EJECUCION,
    // Todos los trabajos tienen resultado y el nodo ya quedó libre.
    CERRADO
}
