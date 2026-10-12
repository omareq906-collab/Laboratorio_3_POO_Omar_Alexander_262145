import java.util.List;
import java.util.Locale;

/**
 * Comprueba las reglas del laboratorio sin instalar un programa adicional de pruebas.
 * Está separada de src porque no forma parte del menú ni de las clases del negocio.
 */
public class PruebasRenderGT {
    private static int aprobadas = 0;

    /** Ejecuta los grupos de pruebas y termina con error si alguna comparación falla. */
    public static void main(String[] args) {
        // Cada grupo verifica un conjunto distinto de reglas del enunciado.
        probarCategorias();
        // Se verifican los centavos antes de revisar las operaciones del sistema.
        probarRedondeos();
        probarValidaciones();
        probarLotes();
        probarSistema();
        probarIntentos();
        probarReintentosJuntos();
        // Llegar aquí significa que todas las comprobaciones terminaron correctamente.
        System.out.println("TOTAL: " + aprobadas + " comprobaciones aprobadas; 0 fallidas.");
    }

    /** Revisa las tarifas, plazos, compatibilidad y límites de cada categoría. */
    private static void probarCategorias() {
        Nodo cpu = new Nodo("CPU", 20, false);
        Nodo gpu = new Nodo("GPU", 20, true);
        ImagenFija dia = new ImagenFija("ID", "Estudio", "Imagen", 1.5, Horario.DIURNO);
        ImagenFija noche = new ImagenFija("IN", "Estudio", "Imagen", 2, Horario.NOCTURNO);
        // Cada resultado esperado se escribe con el valor calculado a partir del enunciado.
        igual("Costo de imagen diurna", "1.5 h; DIURNO", 45.00, dia.calcularCosto());
        igual("Costo de imagen nocturna", "2 h; NOCTURNO", 30.00, noche.calcularCosto());
        igual("Entrega de imagen diurna", "DIURNO", 4, dia.calcularEntrega());
        igual("Entrega de imagen nocturna", "NOCTURNO", 10, noche.calcularEntrega());
        igual("Imagen sin GPU", "Nodo CPU", true, dia.esCompatible(cpu));
        igual("Imagen con GPU", "Nodo GPU", true, dia.esCompatible(gpu));
        igual("Límite de imagen", "Imagen fija", 3, dia.maxIntentos());

        Animacion animacion = new Animacion("AD", "Estudio", "Video", 9.5, Horario.DIURNO, 610);
        Animacion animacionNoche = new Animacion("AN", "Estudio", "Video", 9.5, Horario.NOCTURNO, 610);
        Animacion fraccion = new Animacion("AF", "Estudio", "Video", 2.5, Horario.NOCTURNO, 100);
        Animacion entero = new Animacion("AE", "Estudio", "Video", 2, Horario.NOCTURNO, 100);
        // 610 fotogramas representan 12.2 horas y se cobran 13.
        igual("Horas por fotogramas", "9.5 h; 610 fotogramas; DIURNO", 255.00, animacion.calcularCosto());
        igual("Base nocturna de animación", "9.5 h; 610 fotogramas; NOCTURNO", 235.00, animacionNoche.calcularCosto());
        igual("Fracción con estimación mayor", "2.5 h; 100 fotogramas; NOCTURNO", 85.00, fraccion.calcularCosto());
        igual("Hora entera sin cargo adicional", "2 h; 100 fotogramas; NOCTURNO", 70.00, entero.calcularCosto());
        igual("Horas originales conservadas", "Costo calculado para 9.5 h", 9.5, animacion.getHorasEstimadas());
        igual("Entrega de animación diurna", "DIURNO", 24, animacion.calcularEntrega());
        igual("Entrega de animación nocturna", "NOCTURNO", 36, animacionNoche.calcularEntrega());
        igual("Animación sin GPU", "Nodo CPU", true, animacion.esCompatible(cpu));
        igual("Límite de animación", "Animación normal", 3, animacion.maxIntentos());

        AnimacionPostproduccion post = new AnimacionPostproduccion("PD", "Estudio", "Anuncio",
                9.5, Horario.DIURNO, 610, 1800);
        AnimacionPostproduccion postNoche = new AnimacionPostproduccion("PN", "Estudio", "Anuncio",
                2.5, Horario.NOCTURNO, 100, 1000);
        // El ejemplo diurno debe cobrar 255 + 80 + 90 = 425.
        igual("Composición y recursos", "9.5 h; 610 fotogramas; recursos Q1800; DIURNO", 425.00, post.calcularCosto());
        igual("Postproducción nocturna", "2.5 h; 100 fotogramas; recursos Q1000; NOCTURNO", 215.00, postNoche.calcularCosto());
        igual("Entrega de postproducción diurna", "DIURNO", 36, post.calcularEntrega());
        igual("Entrega de postproducción nocturna", "NOCTURNO", 48, postNoche.calcularEntrega());
        igual("Compatibilidad heredada", "Postproducción; nodo CPU", true, post.esCompatible(cpu));
        igual("Límite de postproducción", "Postproducción", 2, post.maxIntentos());

        SimulacionFisica simulacion = new SimulacionFisica("SF", "Estudio", "Humo", 3.5, Horario.NOCTURNO, 1);
        SimulacionFisica maxima = new SimulacionFisica("SM", "Estudio", "Agua", 5, Horario.NOCTURNO, 50);
        // La simulación conserva las fracciones: 40 + 22 * 3.5 + 65 = 182.
        igual("Cobro de simulación con fracción", "3.5 h; resolución 1; NOCTURNO", 182.00, simulacion.calcularCosto());
        igual("Resolución máxima permitida", "5 h; resolución 50; NOCTURNO", 215.00, maxima.calcularCosto());
        igual("Entrega de simulación", "Simulación física", 18, simulacion.calcularEntrega());
        igual("Simulación sin GPU", "Nodo CPU", false, simulacion.esCompatible(cpu));
        igual("Simulación con GPU", "Nodo GPU", true, simulacion.esCompatible(gpu));
        igual("Límite de simulación", "Simulación física", 1, simulacion.maxIntentos());
    }

    /** Revisa casos donde usar double directamente podía perder un centavo. */
    private static void probarRedondeos() {
        SistemaRender sistema = new SistemaRender();
        AnimacionPostproduccion post = new AnimacionPostproduccion("REDONDEO", "E", "P",
                1, Horario.DIURNO, 50, 1024.10);
        // La suma exacta es 75 + 80 + 51.205 = 206.205; a centavos queda 206.21.
        igual("Redondeo del recargo de recursos", "1 h; 50 fotogramas; DIURNO; recursos Q1024.10",
                206.21, post.calcularCosto());
        contiene("Cotización sin pérdida de centavo", "Postproducción de Q206.205",
                sistema.cotizar(post), "Costo: Q206.21");
        sistema.registrar(post);
        // La venta y la consulta deben mostrar el mismo monto que la cotización.
        contiene("Ingreso igual a la cotización", "Registrar postproducción cotizada",
                sistema.reporteIngresos(), "Por registro: Q206.21");
        contiene("Consulta conserva el precio redondeado", "Consultar REDONDEO registrada",
                sistema.consultarTrabajos(), "Costo de registro: Q206.21");

        AnimacionPostproduccion menor = new AnimacionPostproduccion("MENOR", "E", "P",
                1, Horario.DIURNO, 50, 1024.09);
        AnimacionPostproduccion mayor = new AnimacionPostproduccion("MAYOR", "E", "P",
                1, Horario.DIURNO, 50, 1024.11);
        // Los valores a ambos lados del límite ayudan a comprobar la dirección del redondeo.
        igual("Valor debajo del medio centavo", "Recursos Q1024.09; costo exacto Q206.2045",
                206.20, menor.calcularCosto());
        igual("Valor encima del medio centavo", "Recursos Q1024.11; costo exacto Q206.2055",
                206.21, mayor.calcularCosto());

        SimulacionFisica simulacion = new SimulacionFisica("DECIMAL", "E", "P",
                0.0075, Horario.NOCTURNO, 10);
        // La fórmula exacta da 40 + 0.165 + 65 = 105.165, que se cobra como 105.17.
        igual("Redondeo del costo de simulación", "0.0075 h; NOCTURNO; resolución 10",
                105.17, simulacion.calcularCosto());
        contiene("Cotización de simulación decimal", "Costo exacto Q105.165",
                sistema.cotizar(simulacion), "Costo: Q105.17");
        sistema.registrar(simulacion);
        // Se suman los montos cobrados: 206.21 + 105.17 = 311.38.
        contiene("Total suma los centavos cobrados", "Postproducción y simulación registradas",
                sistema.reporteIngresos(), "Total: Q311.38");
        contiene("Centavos por categoría", "Simulación recién registrada",
                sistema.reporteIngresos(), "Simulación física: Q105.17");
        // Un supuesto ingreso que al redondear queda en cero no se debe aceptar.
        rechaza("Ingreso menor que un centavo", "Monto Q0.001", null,
                () -> new Ingreso(post, TipoIngreso.REGISTRO, 0.001));
    }

    /** Comprueba que los constructores rechacen los datos prohibidos. */
    private static void probarValidaciones() {
        // Las expresiones con -> guardan una operación para intentar ejecutarla en la prueba.
        rechaza("Código vacío", "Código con espacios", null,
                () -> new ImagenFija("   ", "E", "P", 1, Horario.DIURNO));
        rechaza("Horas cero", "0 horas", null,
                () -> new ImagenFija("I", "E", "P", 0, Horario.DIURNO));
        rechaza("Horas negativas", "-1 hora", null,
                () -> new ImagenFija("I", "E", "P", -1, Horario.DIURNO));
        rechaza("Horas no finitas", "NaN", null,
                () -> new Animacion("A", "E", "P", Double.NaN, Horario.DIURNO, 50));
        rechaza("Horas infinitas", "+infinito", null,
                () -> new Animacion("A", "E", "P", Double.POSITIVE_INFINITY, Horario.DIURNO, 50));
        rechaza("Límite de horas de imagen", "2.01 horas", null,
                () -> new ImagenFija("I", "E", "P", 2.01, Horario.NOCTURNO));
        rechaza("Fotogramas cero", "0 fotogramas", null,
                () -> new Animacion("A", "E", "P", 1, Horario.DIURNO, 0));
        rechaza("Fotogramas negativos", "-10 fotogramas", null,
                () -> new Animacion("A", "E", "P", 1, Horario.DIURNO, -10));
        rechaza("Recursos no positivos", "Recursos Q0", null,
                () -> new AnimacionPostproduccion("P", "E", "P", 1, Horario.DIURNO, 50, 0));
        rechaza("Recursos no finitos", "Recursos +infinito", null,
                () -> new AnimacionPostproduccion("P", "E", "P", 1, Horario.DIURNO, 50, Double.POSITIVE_INFINITY));
        rechaza("Simulación diurna", "Horario DIURNO", null,
                () -> new SimulacionFisica("S", "E", "P", 1, Horario.DIURNO, 10));
        rechaza("Resolución inferior", "0.99 millones", null,
                () -> new SimulacionFisica("S", "E", "P", 1, Horario.NOCTURNO, 0.99));
        rechaza("Resolución superior", "50.01 millones", null,
                () -> new SimulacionFisica("S", "E", "P", 1, Horario.NOCTURNO, 50.01));
        rechaza("Resolución no finita", "NaN", null,
                () -> new SimulacionFisica("S", "E", "P", 1, Horario.NOCTURNO, Double.NaN));
        rechaza("Horario vacío", "Horario null", null,
                () -> new ImagenFija("I", "E", "P", 1, null));
        rechaza("Código de nodo vacío", "Código con espacios", null, () -> new Nodo(" ", 10, true));
        rechaza("Capacidad cero", "0 horas", null, () -> new Nodo("N", 0, true));
        rechaza("Capacidad infinita", "+infinito", null, () -> new Nodo("N", Double.POSITIVE_INFINITY, true));
    }

    /** Revisa capacidad, estados, retiro, resultados y validación antes del inicio. */
    private static void probarLotes() {
        Lote lote = new Lote("L", new Nodo("N", 10, false));
        Animacion animacion = new Animacion("A", "E", "P", 9.5, Horario.DIURNO, 610);
        ImagenFija imagen = new ImagenFija("I", "E", "P", 0.5, Horario.DIURNO);
        ImagenFija extra = new ImagenFija("X", "E", "P", 0.1, Horario.DIURNO);
        // Un lote vacío y un trabajo nuevo comienzan sin ejecuciones.
        igual("Estado inicial del lote", "Nuevo lote", EstadoLote.EN_PREPARACION, lote.getEstado());
        igual("Estado inicial del trabajo", "Nueva animación", EstadoTrabajo.EN_COLA, animacion.getEstado());
        igual("Intentos iniciales", "Nueva animación", 0, animacion.getIntentos());
        rechaza("Lote vacío", "Iniciar sin trabajos", null, () -> lote.iniciar());
        // Se debe aceptar aunque la animación facture 13 horas y el nodo solo permita 10.
        lote.asignar(animacion);
        igual("Capacidad con horas estimadas", "9.5 h estimadas; 13 facturables; nodo 10 h", 9.5, lote.horasAsignadas());
        lote.asignar(imagen);
        igual("Capacidad exacta", "9.5 + 0.5 en nodo de 10", 0.0, lote.capacidadDisponible());
        rechaza("Exceso de capacidad", "Agregar 0.1 h a lote lleno", null, () -> lote.asignar(extra));
        igual("Lista sin cambios por exceso", "Asignación rechazada", 2, lote.getTrabajos().size());
        rechaza("Trabajo duplicado en lote", "Asignar otra vez A", null, () -> lote.asignar(animacion));
        // Modificar la copia que devuelve el getter no puede vaciar el lote.
        List<Trabajo> copia = lote.getTrabajos();
        copia.clear();
        igual("Lista protegida", "Vaciar la copia del getter", 2, lote.getTrabajos().size());
        rechaza("Resultado antes de iniciar", "Resolver A en preparación", null,
                () -> lote.registrarResultado(animacion, true));
        // El retiro libera la capacidad y conserva la cola y los intentos.
        lote.retirar(imagen);
        igual("Capacidad después de retirar", "Retirar 0.5 h", 0.5, lote.capacidadDisponible());
        igual("Retiro conserva intentos", "Trabajo I retirado", 0, imagen.getIntentos());
        igual("Retiro conserva cola", "Trabajo I retirado", EstadoTrabajo.EN_COLA, imagen.getEstado());
        lote.asignar(imagen);
        igual("Primer inicio sin recargo", "A e I sin intentos previos", 0.0, lote.iniciar());
        igual("Inicio cuenta intento", "Primer inicio de A", 1, animacion.getIntentos());
        igual("Estado de ejecución", "Lote iniciado", EstadoLote.EN_EJECUCION, lote.getEstado());
        rechaza("Retiro durante ejecución", "Retirar I después de iniciar", null, () -> lote.retirar(imagen));
        rechaza("Asignación durante ejecución", "Asignar X después de iniciar", null, () -> lote.asignar(extra));
        rechaza("Inicio repetido", "Iniciar de nuevo el lote", null, () -> lote.iniciar());
        rechaza("Resultado de trabajo ajeno", "Resolver X", null, () -> lote.registrarResultado(extra, true));
        lote.registrarResultado(animacion, false);
        igual("Fallo con intentos disponibles", "Primer fallo de A", EstadoTrabajo.EN_COLA, animacion.getEstado());
        igual("Lote parcialmente resuelto", "A tiene resultado; I pendiente", EstadoLote.EN_EJECUCION, lote.getEstado());
        rechaza("Resultado duplicado", "Segundo resultado de A", null, () -> lote.registrarResultado(animacion, true));
        lote.registrarResultado(imagen, true);
        igual("Trabajo completado", "Resultado exitoso de I", EstadoTrabajo.COMPLETADO, imagen.getEstado());
        igual("Cierre automático", "Último resultado recibido", EstadoLote.CERRADO, lote.getEstado());

        Lote decimal = new Lote("DEC", new Nodo("ND", 0.3, false));
        // Las horas decimales deben sumar exactamente para comparar la capacidad.
        decimal.asignar(new ImagenFija("D1", "E", "P", 0.1, Horario.DIURNO));
        decimal.asignar(new ImagenFija("D2", "E", "P", 0.2, Horario.DIURNO));
        igual("Suma decimal de capacidad", "0.1 + 0.2 en nodo de 0.3", 0.0, decimal.capacidadDisponible());
        rechaza("Exceso decimal pequeño", "Agregar 0.000001 al nodo lleno", null,
                () -> decimal.asignar(new ImagenFija("D3", "E", "P", 0.000001, Horario.DIURNO)));

        Lote grupo = new Lote("GRUPO", new Nodo("NG", 5, false));
        ImagenFija primero = new ImagenFija("G1", "E", "P", 1, Horario.DIURNO);
        ImagenFija segundo = new ImagenFija("G2", "E", "P", 1, Horario.DIURNO);
        // Se provoca una condición inválida para comprobar que el primer trabajo no inicie parcialmente.
        grupo.asignar(primero);
        grupo.asignar(segundo);
        segundo.iniciarIntento();
        rechaza("Inicio validado por completo", "Segundo trabajo ya en ejecución", null, () -> grupo.iniciar());
        igual("Primer trabajo sin cambio parcial", "Inicio del grupo rechazado", 0, primero.getIntentos());
        igual("Lote sin cambio parcial", "Inicio del grupo rechazado", EstadoLote.EN_PREPARACION, grupo.getEstado());
    }

    /** Revisa cotizaciones, cobros, códigos y operaciones que involucran varios lotes. */
    private static void probarSistema() {
        SistemaRender sistema = new SistemaRender();
        // La carga de ejemplos debe mostrar cero ingresos y dos trabajos de cada categoría.
        contiene("Ingresos iniciales", "Nuevo sistema", sistema.reporteIngresos(), "Total: Q0.00");
        contiene("Imágenes iniciales", "Nuevo sistema", sistema.reporteTrabajos(), "Imagen fija: EN_COLA=2");
        contiene("Animaciones iniciales", "Nuevo sistema", sistema.reporteTrabajos(), "Animación: EN_COLA=2");
        contiene("Postproducciones iniciales", "Nuevo sistema", sistema.reporteTrabajos(), "Animación con postproducción: EN_COLA=2");
        contiene("Simulaciones iniciales", "Nuevo sistema", sistema.reporteTrabajos(), "Simulación física: EN_COLA=2");
        contiene("Nodo inicial sin GPU", "N01", sistema.consultarNodos(), "N01 | Capacidad: 10.0 horas | GPU: No");
        contiene("Nodo inicial con GPU", "N02", sistema.consultarNodos(), "N02 | Capacidad: 20.0 horas | GPU: Sí");
        contiene("Tercer nodo inicial", "N03", sistema.consultarNodos(), "N03 | Capacidad: 30.0");

        ImagenFija nuevo = new ImagenFija("VENTA", "E", "P", 1, Horario.DIURNO);
        String antes = fotografia(sistema);
        String cotizacion = sistema.cotizar(nuevo);
        // Se compara todo el estado observable para demostrar que cotizar no registra nada.
        contiene("Precio de cotización", "Imagen diurna nueva", cotizacion, "Costo: Q45.00");
        igual("Cotización sin cambios", "Cotizar VENTA", true, antes.equals(fotografia(sistema)));
        sistema.cotizar(nuevo);
        igual("Repetir cotización sin cambios", "Cotizar VENTA dos veces", true, antes.equals(fotografia(sistema)));
        sistema.registrar(nuevo);
        contiene("Cobro de registro", "Registrar VENTA", sistema.reporteIngresos(), "Por registro: Q45.00");
        contiene("Ingreso por categoría", "Registrar imagen diurna", sistema.reporteIngresos(), "Imagen fija: Q45.00");
        rechaza("Código de trabajo repetido", "Registrar otra vez VENTA", sistema, () -> sistema.registrar(nuevo));
        rechaza("Cotizar código ya registrado", "Cotizar VENTA registrada", sistema, () -> sistema.cotizar(nuevo));
        rechaza("Código de nodo repetido", "Registrar N01", sistema,
                () -> sistema.registrarNodo(new Nodo("N01", 50, true)));
        rechaza("Costo fuera de rango", "Animación con Double.MAX_VALUE horas", sistema,
                () -> sistema.registrar(new Animacion("ENORME", "E", "P", Double.MAX_VALUE, Horario.DIURNO, 1)));
        // Los códigos se guardan sin espacios externos, también al revisar duplicados.
        rechaza("Código repetido con espacios", "Registrar código ' VENTA '", sistema,
                () -> sistema.registrar(new ImagenFija(" VENTA ", "E", "P", 1, Horario.DIURNO)));

        sistema.crearLote("L1", "N01");
        rechaza("Nodo ocupado", "Crear otro lote en N01", sistema, () -> sistema.crearLote("L2", "N01"));
        rechaza("Código de lote repetido", "L1 ya existe", sistema, () -> sistema.crearLote("L1", "N02"));
        rechaza("Código de lote vacío", "Código con espacios", sistema, () -> sistema.crearLote(" ", "N02"));
        rechaza("Nodo desconocido", "Crear lote en NOEXISTE", sistema, () -> sistema.crearLote("X", "NOEXISTE"));
        rechaza("Lote inexistente", "Iniciar NOEXISTE", sistema, () -> sistema.iniciarLote("NOEXISTE"));
        rechaza("Inicio de lote vacío sin cobros", "Iniciar L1 vacío", sistema, () -> sistema.iniciarLote("L1"));
        rechaza("Restricción de GPU", "SIM01 en N01 sin GPU", sistema, () -> sistema.asignar("L1", "SIM01"));
        sistema.asignar("L1", "ANI01");
        rechaza("Capacidad del sistema", "ANI01 9.5 h + IMG01 1.5 h en N01", sistema, () -> sistema.asignar("L1", "IMG01"));
        sistema.crearLote("L2", "N02");
        rechaza("Asignación a dos lotes", "ANI01 ya está en L1", sistema, () -> sistema.asignar("L2", "ANI01"));
        rechaza("Trabajo inexistente", "Asignar NOEXISTE", sistema, () -> sistema.asignar("L2", "NOEXISTE"));
        rechaza("Retirar trabajo ajeno", "Retirar IMG02 de L1", sistema, () -> sistema.retirar("L1", "IMG02"));
        String ingresos = sistema.reporteIngresos();
        // Retirar y volver a asignar siguen siendo operaciones sin cobro.
        sistema.retirar("L1", "ANI01");
        igual("Retiro sin ingreso", "Retirar ANI01", true, ingresos.equals(sistema.reporteIngresos()));
        sistema.asignar("L2", "ANI01");
        sistema.asignar("L2", "IMG01");
        sistema.iniciarLote("L2");
        igual("Primeros intentos sin ingreso adicional", "Iniciar ANI01 e IMG01", true, ingresos.equals(sistema.reporteIngresos()));
        sistema.registrarResultado("L2", "ANI01", false);
        rechaza("Fallo espera cierre anterior", "Reasignar ANI01 mientras IMG01 sigue pendiente", sistema,
                () -> sistema.asignar("L1", "ANI01"));
        rechaza("Segundo resultado sin cambios", "Repetir resultado de ANI01", sistema,
                () -> sistema.registrarResultado("L2", "ANI01", true));
        sistema.registrarResultado("L2", "IMG01", true);
        contiene("Nodo libre después de cerrar", "Último resultado de L2", sistema.consultarNodos(), "N02 | Capacidad: 20.0 horas | GPU: Sí | Libre");
        sistema.crearLote("L3", "N02");
        sistema.asignar("L1", "ANI01");
        rechaza("Completado no se reasigna", "IMG01 ya completada", sistema, () -> sistema.asignar("L3", "IMG01"));
        // Un trabajo que pasó de un lote cerrado al siguiente sigue siendo el mismo registro.
        contiene("Reporte de trabajo completado", "IMG01 completada", sistema.reporteTrabajos(), "COMPLETADO=1");
    }

    /** Lleva cada categoría hasta su cancelación y comprueba cada cobro. */
    private static void probarIntentos() {
        Trabajo[] categorias = {
            new ImagenFija("I", "E", "P", 1, Horario.DIURNO),
            new Animacion("A", "E", "P", 1, Horario.NOCTURNO, 50),
            new AnimacionPostproduccion("P", "E", "P", 1, Horario.NOCTURNO, 50, 1000),
            new SimulacionFisica("S", "E", "P", 1, Horario.NOCTURNO, 10)
        };
        int[] limitesEsperados = {3, 3, 2, 1};
        double[] costosEsperados = {45, 55, 185, 127};
        // Los límites esperados vienen del enunciado, no de maxIntentos().
        for (int categoria = 0; categoria < categorias.length; categoria++) {
            Trabajo trabajo = categorias[categoria];
            SistemaRender sistema = new SistemaRender();
            sistema.registrar(trabajo);
            for (int intento = 1; intento <= limitesEsperados[categoria]; intento++) {
                String lote = "INT" + intento;
                sistema.crearLote(lote, "N02");
                String antes = sistema.reporteIngresos();
                sistema.asignar(lote, trabajo.getCodigo());
                igual("Asignación sin cobro de " + trabajo.categoria(), "Intento " + intento,
                        true, antes.equals(sistema.reporteIngresos()));
                // También se comprueba el retiro de una asignación que iba a ser reintento.
                sistema.retirar(lote, trabajo.getCodigo());
                igual("Retiro sin cobro de " + trabajo.categoria(), "Intento " + intento,
                        true, antes.equals(sistema.reporteIngresos()));
                sistema.asignar(lote, trabajo.getCodigo());
                sistema.iniciarLote(lote);
                igual("Conteo de " + trabajo.categoria(), "Inicio " + intento, intento, trabajo.getIntentos());
                double reintentos = (intento - 1) * 25.00;
                contiene("Cargo de " + trabajo.categoria(), "Inicio " + intento,
                        sistema.reporteIngresos(), String.format(Locale.US, "Por reintentos: Q%.2f", reintentos));
                contiene("Ingreso acumulado de " + trabajo.categoria(), "Inicio " + intento,
                        sistema.reporteIngresos(), String.format(Locale.US, "Total: Q%.2f", costosEsperados[categoria] + reintentos));
                rechaza("Inicio repetido sin doble cobro", trabajo.categoria() + "; intento " + intento,
                        sistema, () -> sistema.iniciarLote(lote));
                // Todos estos resultados son fallidos para alcanzar el límite de cada categoría.
                sistema.registrarResultado(lote, trabajo.getCodigo(), false);
                EstadoTrabajo esperado = intento == limitesEsperados[categoria]
                        ? EstadoTrabajo.CANCELADO : EstadoTrabajo.EN_COLA;
                igual("Estado por fallo de " + trabajo.categoria(), "Fallo " + intento, esperado, trabajo.getEstado());
            }
            // Una vez cancelado, ni siquiera se permite volver a asignar el trabajo.
            sistema.crearLote("FINAL", "N02");
            rechaza("Cancelado no se reasigna", trabajo.categoria(), sistema,
                    () -> sistema.asignar("FINAL", trabajo.getCodigo()));
        }

        ImagenFija ultimo = new ImagenFija("EXITO", "E", "P", 1, Horario.DIURNO);
        // Tener éxito en el tercer intento debe completar, no cancelar, el trabajo.
        ultimo.iniciarIntento();
        ultimo.resolver(false);
        ultimo.iniciarIntento();
        ultimo.resolver(false);
        ultimo.iniciarIntento();
        ultimo.resolver(true);
        igual("Éxito en el último intento", "Dos fallos y un éxito", EstadoTrabajo.COMPLETADO, ultimo.getEstado());
    }

    /** Verifica que un lote pueda mezclar primeros intentos y varios reintentos. */
    private static void probarReintentosJuntos() {
        SistemaRender sistema = new SistemaRender();
        // Se hace fallar una imagen y una animación de los datos iniciales.
        sistema.crearLote("J1", "N01");
        sistema.asignar("J1", "IMG01");
        sistema.asignar("J1", "ANI02");
        sistema.iniciarLote("J1");
        sistema.registrarResultado("J1", "IMG01", false);
        sistema.registrarResultado("J1", "ANI02", false);
        // El siguiente lote tiene dos reintentos y un trabajo que nunca se ha ejecutado.
        sistema.crearLote("J2", "N01");
        sistema.asignar("J2", "IMG01");
        sistema.asignar("J2", "ANI02");
        sistema.asignar("J2", "IMG02");
        contiene("Lote mixto antes del inicio", "Dos reintentos asignados", sistema.reporteIngresos(), "Total: Q0.00");
        sistema.iniciarLote("J2");
        contiene("Dos reintentos en un lote", "IMG01, ANI02 e IMG02", sistema.reporteIngresos(), "Por reintentos: Q50.00");
        contiene("Reintento por categoría imagen", "IMG01 segundo intento", sistema.reporteIngresos(), "Imagen fija: Q25.00");
        contiene("Reintento por categoría animación", "ANI02 segundo intento", sistema.reporteIngresos(), "Animación: Q25.00");
        contiene("Datos iniciales sin cobro de registro", "Lote mixto iniciado", sistema.reporteIngresos(), "Por registro: Q0.00");
        rechaza("Lote mixto no cobra dos veces", "Iniciar J2 otra vez", sistema, () -> sistema.iniciarLote("J2"));
        // Al resolver todos, se libera el nodo y desaparece el lote del reporte de abiertos.
        sistema.registrarResultado("J2", "IMG01", true);
        sistema.registrarResultado("J2", "ANI02", true);
        sistema.registrarResultado("J2", "IMG02", true);
        contiene("Reporte después de cerrar", "Tres resultados exitosos", sistema.reporteLotes(), "No hay lotes abiertos.");
    }

    /** Reúne las consultas para detectar cualquier cambio producido por un rechazo. */
    private static String fotografia(SistemaRender sistema) {
        // Se incluyen trabajos, nodos, lotes, estados e ingresos en una sola comparación.
        return sistema.consultarTrabajos() + sistema.consultarNodos() + sistema.reporteTrabajos()
                + sistema.reporteIngresos() + sistema.reporteLotes();
    }

    /** Compara el resultado obtenido con el valor que exige la prueba. */
    private static void igual(String nombre, String datos, Object esperado, Object obtenido) {
        // Un error detiene la prueba para evitar informar que todo pasó cuando no fue así.
        if (!esperado.equals(obtenido)) {
            throw new AssertionError(nombre + ": se esperaba " + esperado + " y se obtuvo " + obtenido);
        }
        aprobadas++;
        // Se documentan datos, esperado y obtenido para revisar la evidencia.
        System.out.println("OK " + aprobadas + " | " + nombre + "\n  Entrada: " + datos
                + "\n  Esperado: " + esperado + "\n  Obtenido: " + obtenido);
    }

    /** Revisa una línea de un reporte sin depender del resto de su formato. */
    private static void contiene(String nombre, String datos, String texto, String esperado) {
        String obtenido = "No se encontró el dato esperado";
        // Se guarda el fragmento real de la salida para mostrarlo como evidencia.
        int posicion = texto.indexOf(esperado);
        if (posicion >= 0) {
            obtenido = texto.substring(posicion, posicion + esperado.length());
        }
        igual(nombre, datos, esperado, obtenido);
    }

    /** Comprueba que una operación inválida se rechace y conserve los datos anteriores. */
    private static void rechaza(String nombre, String datos, SistemaRender sistema, Runnable operacion) {
        String antes = sistema == null ? "" : fotografia(sistema);
        boolean rechazada = false;
        String motivo = "";
        try {
            // run ejecuta la operación que recibió la prueba.
            operacion.run();
        } catch (IllegalArgumentException | IllegalStateException error) {
            rechazada = true;
            motivo = error.getMessage();
        }
        // La ausencia de excepción significaría que se aceptó una operación inválida.
        igual(nombre, datos, true, rechazada);
        System.out.println("  Mensaje: " + motivo);
        if (sistema != null) {
            // Se verifica que también estén intactos los ingresos y los intentos.
            igual(nombre + " conserva el sistema", datos, true, antes.equals(fotografia(sistema)));
        }
    }
}
