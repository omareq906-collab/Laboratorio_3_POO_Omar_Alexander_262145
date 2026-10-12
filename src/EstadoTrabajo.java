/** Evita escribir estados distintos de los que permite el sistema. */
public enum EstadoTrabajo {
    // Está pendiente de ejecutar; puede estar reservado en un lote en preparación.
    EN_COLA,
    // Su lote ya inició y todavía no se ha registrado el resultado del trabajo.
    EN_EJECUCION,
    // La ejecución terminó con éxito.
    COMPLETADO,
    // El último intento permitido falló y ya no se puede ejecutar otra vez.
    CANCELADO
}
