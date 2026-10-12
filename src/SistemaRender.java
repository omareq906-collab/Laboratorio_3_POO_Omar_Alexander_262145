import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Coordina los trabajos, nodos, lotes y cobros que utiliza el menú. */
public class SistemaRender {
    // Los mapas permiten localizar cada objeto usando su código.
    private final Map<String, Trabajo> trabajos;
    private final Map<String, Nodo> nodos;
    private final Map<String, Lote> lotes;
    // Cada ingreso se guarda una sola vez. Los reportes calculan los totales.
    private final List<Ingreso> ingresos;

    /** Prepara las colecciones y carga los ejemplos solicitados. */
    public SistemaRender() {
        this.trabajos = new LinkedHashMap<>();
        this.nodos = new LinkedHashMap<>();
        this.lotes = new LinkedHashMap<>();
        this.ingresos = new ArrayList<>();
        // La carga inicial no representa ventas, por eso no llama a registrar.
        cargarIniciales();
    }

    /** Carga dos trabajos por categoría y tres nodos sin generar ingresos. */
    private void cargarIniciales() {
        Trabajo[] ejemplos = {
            new ImagenFija("IMG01", "Estudio Norte", "Fachada", 1.5, Horario.DIURNO),
            new ImagenFija("IMG02", "Estudio Sur", "Producto", 2, Horario.NOCTURNO),
            new Animacion("ANI01", "Estudio Norte", "Recorrido", 9.5, Horario.DIURNO, 610),
            new Animacion("ANI02", "Estudio Sur", "Cortometraje", 2.5, Horario.NOCTURNO, 100),
            new AnimacionPostproduccion("POST01", "Estudio Centro", "Anuncio", 9.5,
                    Horario.DIURNO, 610, 1800),
            new AnimacionPostproduccion("POST02", "Estudio Centro", "Presentación", 2.5,
                    Horario.NOCTURNO, 100, 1000),
            new SimulacionFisica("SIM01", "Estudio Sur", "Humo", 3.5, Horario.NOCTURNO, 1),
            new SimulacionFisica("SIM02", "Estudio Norte", "Agua", 5, Horario.NOCTURNO, 50)
        };
        // Se guardan los ejemplos directamente para mantener los ingresos en cero.
        for (Trabajo trabajo : ejemplos) {
            trabajos.put(trabajo.getCodigo(), trabajo);
        }
        // Se incluyen nodos con diferentes capacidades y disponibilidad de GPU.
        registrarNodo(new Nodo("N01", 10, false));
        registrarNodo(new Nodo("N02", 20, true));
        registrarNodo(new Nodo("N03", 30, true));
    }

    /** Devuelve costo y entrega sin guardar el trabajo ni crear un ingreso. */
    public String cotizar(Trabajo trabajo) {
        // Una cotización recibe un objeto temporal con datos ya validados por su constructor.
        if (trabajo == null) {
            throw new IllegalArgumentException("Debes indicar un trabajo para cotizar.");
        }
        if (trabajos.containsKey(trabajo.getCodigo())) {
            throw new IllegalArgumentException("Ya existe un trabajo con ese código.");
        }
        double costo = trabajo.calcularCosto();
        // Evita mostrar un monto infinito si se introdujo un número demasiado grande.
        if (!Double.isFinite(costo) || costo <= 0) {
            throw new IllegalArgumentException("Los datos producen un costo fuera de rango.");
        }
        BigDecimal monto = BigDecimal.valueOf(costo).setScale(2, RoundingMode.HALF_UP);
        // Se devuelve texto; no se agrega nada a las colecciones del sistema.
        return "Categoría: " + trabajo.categoria() + "\nCosto: Q" + monto.toPlainString()
                + "\nEntrega estimada: " + trabajo.calcularEntrega() + " horas.";
    }

    /** Guarda un trabajo nuevo y el cobro completo de su registro. */
    public void registrar(Trabajo trabajo) {
        // Primero se revisan los datos que requieren conocer los registros existentes.
        if (trabajo == null) {
            throw new IllegalArgumentException("El trabajo no puede estar vacío.");
        }
        if (trabajos.containsKey(trabajo.getCodigo())) {
            throw new IllegalArgumentException("Ya existe un trabajo con ese código.");
        }
        if (trabajo.getEstado() != EstadoTrabajo.EN_COLA || trabajo.getIntentos() != 0) {
            throw new IllegalArgumentException("Solo se pueden registrar trabajos nuevos.");
        }
        // Se prepara el cobro antes de guardar, por si el monto resulta inválido.
        Ingreso ingreso = new Ingreso(trabajo, TipoIngreso.REGISTRO, trabajo.calcularCosto());
        // Ambas operaciones se realizan después de terminar las validaciones.
        trabajos.put(trabajo.getCodigo(), trabajo);
        ingresos.add(ingreso);
    }

    /** Registra un nodo cuando su código todavía no existe. */
    public void registrarNodo(Nodo nodo) {
        // El constructor ya revisó capacidad y código; aquí se comprueba la unicidad.
        if (nodo == null) {
            throw new IllegalArgumentException("El nodo no puede estar vacío.");
        }
        if (nodos.containsKey(nodo.getCodigo())) {
            throw new IllegalArgumentException("Ya existe un nodo con ese código.");
        }
        // Registrar equipos no representa un cobro.
        nodos.put(nodo.getCodigo(), nodo);
    }

    /** Crea un lote para un nodo que no tenga otro lote abierto. */
    public void crearLote(String codigo, String codigoNodo) {
        // Se comprueba el código del lote antes de usarlo como llave del mapa.
        if (codigo == null || codigo.trim().isEmpty()) {
            throw new IllegalArgumentException("El código del lote no puede estar vacío.");
        }
        if (lotes.containsKey(codigo.trim())) {
            throw new IllegalArgumentException("Ya existe un lote con ese código.");
        }
        Nodo nodo = nodos.get(codigoNodo == null ? null : codigoNodo.trim());
        // El nodo debe estar registrado para poder recibir un lote.
        if (nodo == null) {
            throw new IllegalArgumentException("No existe el nodo indicado.");
        }
        // Los lotes cerrados son historial y no ocupan el nodo.
        for (Lote lote : lotes.values()) {
            if (lote.getNodo() == nodo && lote.getEstado() != EstadoLote.CERRADO) {
                throw new IllegalStateException("El nodo ya tiene un lote sin cerrar.");
            }
        }
        Lote nuevo = new Lote(codigo, nodo);
        // Se guarda únicamente después de comprobar la disponibilidad del nodo.
        lotes.put(nuevo.getCodigo(), nuevo);
    }

    /** Impide la asignación a varios lotes y delega las demás reglas en Lote. */
    public void asignar(String codigoLote, String codigoTrabajo) {
        Lote lote = lotes.get(codigoLote == null ? null : codigoLote.trim());
        Trabajo trabajo = trabajos.get(codigoTrabajo == null ? null : codigoTrabajo.trim());
        // Se localizan los objetos reales registrados, sin crear copias del trabajo.
        if (lote == null || trabajo == null) {
            throw new IllegalArgumentException("No existe el lote o el trabajo indicado.");
        }
        // Un trabajo tampoco se puede repetir dentro del mismo lote abierto.
        for (Lote actual : lotes.values()) {
            if (actual.getEstado() != EstadoLote.CERRADO
                    && actual.getTrabajos().contains(trabajo)) {
                throw new IllegalStateException("El trabajo ya está asignado al lote " + actual.getCodigo() + ".");
            }
        }
        // Lote revisa cola, intentos, capacidad y compatibilidad antes de agregar.
        lote.asignar(trabajo);
    }

    /** Busca el lote y el trabajo y solicita retirar su asignación. */
    public void retirar(String codigoLote, String codigoTrabajo) {
        Lote lote = lotes.get(codigoLote == null ? null : codigoLote.trim());
        Trabajo trabajo = trabajos.get(codigoTrabajo == null ? null : codigoTrabajo.trim());
        // Si algún código no existe, la operación termina antes de cambiar datos.
        if (lote == null || trabajo == null) {
            throw new IllegalArgumentException("No existe el lote o el trabajo indicado.");
        }
        // El retiro no modifica los ingresos ni la cantidad de intentos.
        lote.retirar(trabajo);
    }

    /** Inicia el lote y registra los cargos de sus trabajos que se están reintentando. */
    public void iniciarLote(String codigoLote) {
        Lote lote = lotes.get(codigoLote == null ? null : codigoLote.trim());
        // Primero se confirma que el lote exista.
        if (lote == null) {
            throw new IllegalArgumentException("No existe el lote indicado.");
        }
        List<Trabajo> reintentos = new ArrayList<>();
        // Se identifica quiénes ya tenían intentos antes de iniciar el siguiente.
        for (Trabajo trabajo : lote.getTrabajos()) {
            // Tener un intento previo significa que el siguiente ya debe cobrarse.
            if (trabajo.getIntentos() > 0) {
                // Se conserva la referencia para asociarle su ingreso después del inicio.
                reintentos.add(trabajo);
            }
        }
        // Si iniciar rechaza la operación, no se ejecuta ninguna de las líneas de cobro.
        double total = lote.iniciar();
        // Se reparte el total entre los reintentos: cada uno corresponde a Q25.00.
        if (!reintentos.isEmpty()) {
            double montoPorTrabajo = total / reintentos.size();
            // Se guarda un ingreso por cada trabajo para poder agruparlo por categoría.
            for (Trabajo trabajo : reintentos) {
                ingresos.add(new Ingreso(trabajo, TipoIngreso.REINTENTO, montoPorTrabajo));
            }
        }
    }

    /** Entrega al lote el resultado que escribió el usuario. */
    public void registrarResultado(String codigoLote, String codigoTrabajo, boolean exito) {
        Lote lote = lotes.get(codigoLote == null ? null : codigoLote.trim());
        Trabajo trabajo = trabajos.get(codigoTrabajo == null ? null : codigoTrabajo.trim());
        // No se permite guardar resultados para códigos inexistentes.
        if (lote == null || trabajo == null) {
            throw new IllegalArgumentException("No existe el lote o el trabajo indicado.");
        }
        // El lote valida pertenencia y estado, y se cierra cuando corresponde.
        lote.registrarResultado(trabajo, exito);
    }

    /** Muestra los trabajos, su estado y el lote que los tiene reservados. */
    public String consultarTrabajos() {
        StringBuilder texto = new StringBuilder("TRABAJOS REGISTRADOS\n");
        // Se recorren los trabajos en el orden en que fueron registrados.
        for (Trabajo trabajo : trabajos.values()) {
            String loteActivo = "ninguno";
            // Se distinguen los trabajos en cola disponibles de los ya asignados.
            for (Lote lote : lotes.values()) {
                // Los lotes cerrados no reservan trabajos para una ejecución actual.
                if (lote.getEstado() != EstadoLote.CERRADO && lote.getTrabajos().contains(trabajo)) {
                    // Se muestra el código del único lote abierto que contiene al trabajo.
                    loteActivo = lote.getCodigo();
                }
            }
            // Se incluyen las reglas calculadas por la categoría para facilitar la consulta.
            texto.append(String.format(Locale.US,
                    "%s | %s | Estudio: %s | Proyecto: %s%n"
                    + "  Horas: %s | %s | %s | Intentos: %d/%d | Lote activo: %s%n"
                    + "  Costo de registro: Q%.2f | Entrega: %d horas%n",
                    trabajo.getCodigo(), trabajo.categoria(), trabajo.getEstudio(), trabajo.getProyecto(),
                    trabajo.getHorasEstimadas(), trabajo.getHorario(), trabajo.getEstado(),
                    trabajo.getIntentos(), trabajo.maxIntentos(), loteActivo,
                    trabajo.calcularCosto(), trabajo.calcularEntrega()));
        }
        // Consultar no cambia estados ni crea cobros.
        return texto.toString();
    }

    /** Muestra la capacidad, la GPU y la ocupación de cada nodo. */
    public String consultarNodos() {
        StringBuilder texto = new StringBuilder("NODOS REGISTRADOS\n");
        // La disponibilidad se obtiene de los lotes; no se guarda otra copia de ese dato.
        for (Nodo nodo : nodos.values()) {
            String ocupacion = "Libre";
            // Se busca si este nodo está reservado por algún lote todavía abierto.
            for (Lote lote : lotes.values()) {
                if (lote.getNodo() == nodo && lote.getEstado() != EstadoLote.CERRADO) {
                    // Se reemplaza Libre por el código y el estado de su lote actual.
                    ocupacion = "Lote " + lote.getCodigo() + " (" + lote.getEstado() + ")";
                }
            }
            // El operador ? elige el texto Sí o No según el valor booleano de GPU.
            texto.append(nodo.getCodigo()).append(" | Capacidad: ").append(nodo.getCapacidad())
                    .append(" horas | GPU: ").append(nodo.tieneGpu() ? "Sí" : "No")
                    .append(" | ").append(ocupacion).append('\n');
        }
        // Se devuelve el listado completo para imprimirlo desde el menú.
        return texto.toString();
    }

    /** Cuenta los trabajos de cada categoría y muestra todos sus estados. */
    public String reporteTrabajos() {
        Map<String, int[]> conteos = new LinkedHashMap<>();
        // Cada categoría obtiene un arreglo con una casilla por estado.
        for (Trabajo trabajo : trabajos.values()) {
            String categoria = trabajo.categoria();
            // Una categoría nueva comienza con un contador en cero por cada estado.
            if (!conteos.containsKey(categoria)) {
                conteos.put(categoria, new int[EstadoTrabajo.values().length]);
            }
            // ordinal indica la posición del estado dentro del enum.
            conteos.get(categoria)[trabajo.getEstado().ordinal()]++;
        }
        StringBuilder texto = new StringBuilder("TRABAJOS POR CATEGORÍA Y ESTADO\n");
        // Los nombres se obtienen de los objetos; no hay una lista fija de categorías.
        for (String categoria : conteos.keySet()) {
            int total = 0;
            // Se escribe el nombre del grupo antes de sus cantidades.
            texto.append(categoria).append(':');
            // Se incluyen todos los estados, aunque en alguno la cantidad sea cero.
            for (EstadoTrabajo estado : EstadoTrabajo.values()) {
                int cantidad = conteos.get(categoria)[estado.ordinal()];
                // Se agrega el conteo al texto y al total de la categoría.
                texto.append(' ').append(estado).append('=').append(cantidad);
                total += cantidad;
            }
            // Se cierra la fila con la suma de los estados de esta categoría.
            texto.append(" | Total=").append(total).append('\n');
        }
        // El menú recibe un reporte listo para mostrar.
        return texto.toString();
    }

    /** Suma los cobros reales y separa registro, reintentos y categorías. */
    public String reporteIngresos() {
        BigDecimal registros = BigDecimal.ZERO;
        BigDecimal reintentos = BigDecimal.ZERO;
        Map<String, BigDecimal> porCategoria = new LinkedHashMap<>();
        // También se muestran las categorías que todavía no han generado ingresos.
        for (Trabajo trabajo : trabajos.values()) {
            porCategoria.put(trabajo.categoria(), BigDecimal.ZERO);
        }
        // Solo se suman objetos Ingreso; el precio de los ejemplos iniciales no se cobra.
        for (Ingreso ingreso : ingresos) {
            BigDecimal monto = BigDecimal.valueOf(ingreso.getMonto());
            // El motivo del ingreso decide en cuál de los dos acumulados se suma.
            if (ingreso.getTipo() == TipoIngreso.REGISTRO) {
                registros = registros.add(monto);
            } else {
                reintentos = reintentos.add(monto);
            }
            String categoria = ingreso.getTrabajo().categoria();
            // El mismo cobro también se agrega al total de su categoría.
            porCategoria.put(categoria, porCategoria.get(categoria).add(monto));
        }
        StringBuilder texto = new StringBuilder("INGRESOS\n");
        // Se muestran dos decimales y el total incluye ambos tipos de cobro.
        texto.append(String.format(Locale.US,
                "Por registro: Q%.2f%nPor reintentos: Q%.2f%nTotal: Q%.2f%n",
                registros, reintentos, registros.add(reintentos)));
        // Cada categoría suma tanto sus registros como sus reintentos.
        for (String categoria : porCategoria.keySet()) {
            texto.append(String.format(Locale.US, "%s: Q%.2f%n", categoria, porCategoria.get(categoria)));
        }
        // Se devuelve el texto; ninguno de estos cálculos crea ingresos nuevos.
        return texto.toString();
    }

    /** Lista los lotes abiertos, sus trabajos y la capacidad que todavía tienen. */
    public String reporteLotes() {
        StringBuilder texto = new StringBuilder("LOTES SIN CERRAR\n");
        boolean hayAbiertos = false;
        // Un lote cerrado no ocupa el nodo y no aparece en este reporte.
        for (Lote lote : lotes.values()) {
            // Se omiten los lotes cerrados porque solo son parte del historial.
            if (lote.getEstado() != EstadoLote.CERRADO) {
                // La bandera permite saber al final si se mostró al menos un lote.
                hayAbiertos = true;
                // Se agrega una fila con nodo, estado y uso de capacidad.
                texto.append(lote.getCodigo()).append(" | Nodo: ").append(lote.getNodo().getCodigo())
                        .append(" | ").append(lote.getEstado())
                        .append(" | Horas asignadas: ").append(lote.horasAsignadas())
                        .append(" | Capacidad disponible: ").append(lote.capacidadDisponible())
                        .append(" | Capacidad total: ").append(lote.getNodo().getCapacidad()).append('\n');
                // Los códigos ayudan a elegir los trabajos al registrar resultados.
                texto.append("  Trabajos:");
                // Se listan los códigos de los trabajos que pertenecen a ese lote.
                for (Trabajo trabajo : lote.getTrabajos()) {
                    texto.append(' ').append(trabajo.getCodigo());
                }
                // Se separa este lote del siguiente para que el reporte sea legible.
                texto.append('\n');
            }
        }
        // Se informa expresamente cuando todos los nodos están libres de lotes.
        if (!hayAbiertos) {
            texto.append("No hay lotes abiertos.\n");
        }
        // Se entrega el reporte de lotes sin alterar sus estados ni asignaciones.
        return texto.toString();
    }
}
