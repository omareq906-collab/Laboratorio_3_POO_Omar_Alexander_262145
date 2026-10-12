import java.nio.charset.StandardCharsets;
import java.util.NoSuchElementException;
import java.util.Scanner;

/** Presenta el menú de consola y envía las operaciones a SistemaRender. */
public class Main {
    // El menú utiliza un solo sistema y un solo lector durante toda la sesión.
    private final SistemaRender sistema;
    private final Scanner entrada;

    /** Prepara el sistema con ejemplos y la lectura del teclado. */
    public Main() {
        this.sistema = new SistemaRender();
        this.entrada = new Scanner(System.in, StandardCharsets.UTF_8.name());
    }

    /** Es el punto donde Java empieza a ejecutar el programa. */
    public static void main(String[] args) {
        Main programa = new Main();
        // Se inicia el menú con los datos que creó el constructor.
        programa.ejecutar();
    }

    /** Mantiene el menú disponible hasta elegir salir. */
    public void ejecutar() {
        boolean continuar = true;
        // El aviso aclara por qué los trabajos iniciales no aparecen como ventas.
        System.out.println("RenderGT - Laboratorio de herencia");
        System.out.println("Se cargaron 8 trabajos en cola y 3 nodos. Ingresos iniciales: Q0.00.");
        // Al terminar una opción se vuelve a mostrar este menú.
        while (continuar) {
            // Estas líneas muestran las acciones disponibles y el número para elegirlas.
            System.out.println("\n1. Cotizar trabajo");
            System.out.println("2. Registrar trabajo");
            System.out.println("3. Registrar nodo");
            System.out.println("4. Crear lote");
            System.out.println("5. Asignar trabajo a lote");
            System.out.println("6. Retirar trabajo de lote");
            System.out.println("7. Iniciar lote");
            System.out.println("8. Registrar resultado de un trabajo");
            System.out.println("9. Consultar trabajos");
            System.out.println("10. Consultar nodos");
            System.out.println("11. Reporte de trabajos");
            System.out.println("12. Reporte de ingresos");
            System.out.println("13. Reporte de lotes abiertos");
            System.out.println("0. Salir");
            try {
                int opcion = leerEntero("Opción: ");
                // El switch elige una acción del menú, no las reglas de una categoría.
                // Cada break termina su opción; después el while vuelve a mostrar el menú.
                switch (opcion) {
                    case 1: {
                        Trabajo trabajo = leerTrabajo();
                        // Cotizar solo muestra información; el objeto no se guarda.
                        System.out.println(sistema.cotizar(trabajo));
                        // Se aclara que el precio mostrado todavía no es una venta.
                        System.out.println("La cotización no registró el trabajo ni generó ingresos.");
                        break;
                    }
                    case 2: {
                        Trabajo trabajo = leerTrabajo();
                        // Se muestra el precio antes de confirmar el cobro.
                        System.out.println(sistema.cotizar(trabajo));
                        int aceptar = leerEntero("¿Aceptar y registrar? 1 = sí, 0 = cancelar: ");
                        // Solo la confirmación 1 permite guardar el trabajo y cobrarlo.
                        if (aceptar == 1) {
                            sistema.registrar(trabajo);
                            // Este mensaje aparece únicamente si el registro terminó bien.
                            System.out.println("Trabajo registrado en cola. Se cobró el monto completo.");
                        } else if (aceptar == 0) {
                            // Cancelar la confirmación no guarda el objeto temporal.
                            System.out.println("Registro cancelado. No se realizó ningún cobro.");
                        } else {
                            // Una respuesta distinta de 0 o 1 se rechaza sin guardar ni cobrar.
                            throw new IllegalArgumentException("La confirmación debe ser 1 o 0.");
                        }
                        break;
                    }
                    case 3: {
                        // Se piden las características del nuevo nodo.
                        System.out.print("Código del nodo: ");
                        String codigo = entrada.nextLine();
                        double capacidad = leerNumero("Capacidad de horas por lote: ");
                        int gpu = leerEntero("¿Tiene GPU? 1 = sí, 0 = no: ");
                        // Se revisa la opción antes de convertirla al valor true o false.
                        if (gpu != 0 && gpu != 1) {
                            throw new IllegalArgumentException("La opción de GPU debe ser 1 o 0.");
                        }
                        // Nodo valida sus datos y SistemaRender revisa si el código se repite.
                        sistema.registrarNodo(new Nodo(codigo, capacidad, gpu == 1));
                        // Se confirma que el equipo ya está disponible en el registro.
                        System.out.println("Nodo registrado.");
                        break;
                    }
                    case 4: {
                        // La lista permite consultar cuáles nodos están libres.
                        System.out.print(sistema.consultarNodos());
                        System.out.print("Código del nuevo lote: ");
                        String codigo = entrada.nextLine();
                        System.out.print("Código del nodo: ");
                        String nodo = entrada.nextLine();
                        // El sistema comprueba que el nodo exista y no tenga otro lote abierto.
                        sistema.crearLote(codigo, nodo);
                        // Se informa que ahora se pueden asignar trabajos al lote.
                        System.out.println("Lote creado en preparación.");
                        break;
                    }
                    case 5: {
                        // Se reciben códigos para trabajar con los objetos ya registrados.
                        System.out.print("Código del lote: ");
                        String lote = entrada.nextLine();
                        System.out.print("Código del trabajo: ");
                        String trabajo = entrada.nextLine();
                        // El sistema y el lote validan disponibilidad, capacidad y GPU.
                        sistema.asignar(lote, trabajo);
                        // La asignación solo reserva un espacio; todavía no inicia la ejecución.
                        System.out.println("Trabajo asignado. No se cobró ni se contó un intento.");
                        break;
                    }
                    case 6: {
                        // Solo se podrá retirar si el lote sigue en preparación.
                        System.out.print("Código del lote: ");
                        String lote = entrada.nextLine();
                        System.out.print("Código del trabajo: ");
                        String trabajo = entrada.nextLine();
                        // Se solicita quitar la asignación, sin cambiar intentos ni ingresos.
                        sistema.retirar(lote, trabajo);
                        // Se confirma que el trabajo ya puede asignarse a otro lote.
                        System.out.println("Trabajo retirado; vuelve a estar disponible en cola.");
                        break;
                    }
                    case 7: {
                        // El inicio es la operación que cuenta intentos y cobra reintentos.
                        System.out.print("Código del lote: ");
                        String lote = entrada.nextLine();
                        // Se validan todos los trabajos antes de cambiar estados o registrar cargos.
                        sistema.iniciarLote(lote);
                        // Si no hubo error, todo el grupo ya se encuentra en ejecución.
                        System.out.println("Lote en ejecución. Se registraron los reintentos que correspondían.");
                        break;
                    }
                    case 8: {
                        // Se registra un resultado por trabajo, no uno para todo el lote.
                        System.out.print("Código del lote: ");
                        String lote = entrada.nextLine();
                        System.out.print("Código del trabajo: ");
                        String trabajo = entrada.nextLine();
                        int resultado = leerEntero("Resultado: 1 = completado, 2 = fallido: ");
                        // Solo se aceptan las dos respuestas mostradas al usuario.
                        if (resultado != 1 && resultado != 2) {
                            throw new IllegalArgumentException("El resultado debe ser 1 o 2.");
                        }
                        // La comparación convierte 1 en true y 2 en false para guardar el resultado.
                        sistema.registrarResultado(lote, trabajo, resultado == 1);
                        // El sistema decide si todavía faltan resultados o si el lote ya cerró.
                        System.out.println("Resultado guardado. El lote se cierra cuando todos tienen resultado.");
                        break;
                    }
                    case 9:
                        // Las consultas devuelven texto y no modifican los datos.
                        System.out.print(sistema.consultarTrabajos());
                        break;
                    case 10:
                        // Se muestra cada nodo con su capacidad, GPU y ocupación actual.
                        System.out.print(sistema.consultarNodos());
                        break;
                    case 11:
                        // Cada reporte se calcula con los registros que existen en ese momento.
                        System.out.print(sistema.reporteTrabajos());
                        break;
                    case 12:
                        // Se muestran los ingresos por registro, reintentos y categoría.
                        System.out.print(sistema.reporteIngresos());
                        break;
                    case 13:
                        // Se listan los lotes abiertos con las horas ocupadas y disponibles.
                        System.out.print(sistema.reporteLotes());
                        break;
                    case 0:
                        // Cambiar la bandera termina el ciclo del menú.
                        continuar = false;
                        break;
                    default:
                        // Una opción inexistente no ejecuta ninguna operación.
                        System.out.println("Selecciona una opción entre 0 y 13.");
                        break;
                }
            } catch (IllegalArgumentException | IllegalStateException error) {
                // Las validaciones explican el motivo y permiten seguir usando el menú.
                System.out.println("No se realizó la operación: " + error.getMessage());
            } catch (NoSuchElementException error) {
                // Si se termina la entrada, el programa sale sin mostrar una excepción.
                System.out.println("\nSe terminó la entrada de datos.");
                continuar = false;
            }
        }
        // El lector se cierra al salir; los datos solo existían durante esta ejecución.
        entrada.close();
        // Se avisa que el menú terminó y que la sesión ya no conserva sus datos.
        System.out.println("Programa finalizado.");
    }

    /** Pide los datos del trabajo y construye la categoría que eligió el usuario. */
    private Trabajo leerTrabajo() {
        // Esta es la única selección de categoría; después se usa polimorfismo.
        System.out.println("1. Imagen fija\n2. Animación\n3. Animación con postproducción\n4. Simulación física");
        int categoria = leerEntero("Categoría: ");
        // Una categoría inexistente se rechaza antes de pedir sus datos.
        if (categoria < 1 || categoria > 4) {
            throw new IllegalArgumentException("La categoría debe estar entre 1 y 4.");
        }
        // Se leen primero los datos que comparten todas las categorías.
        System.out.print("Código del trabajo: ");
        String codigo = entrada.nextLine();
        System.out.print("Estudio: ");
        String estudio = entrada.nextLine();
        System.out.print("Proyecto: ");
        String proyecto = entrada.nextLine();
        double horas = leerNumero("Horas estimadas: ");
        int opcionHorario = leerEntero("Horario: 1 = diurno, 2 = nocturno: ");
        // Se valida la opción para no guardar un horario distinto de los permitidos.
        if (opcionHorario != 1 && opcionHorario != 2) {
            throw new IllegalArgumentException("El horario debe ser 1 o 2.");
        }
        Horario horario = opcionHorario == 1 ? Horario.DIURNO : Horario.NOCTURNO;
        // Cada constructor valida los datos particulares de su categoría.
        switch (categoria) {
            case 1:
                // La imagen utiliza únicamente los datos comunes.
                return new ImagenFija(codigo, estudio, proyecto, horas, horario);
            case 2: {
                int fotogramas = leerEntero("Fotogramas: ");
                // La animación agrega la cantidad de fotogramas.
                return new Animacion(codigo, estudio, proyecto, horas, horario, fotogramas);
            }
            case 3: {
                int fotogramas = leerEntero("Fotogramas: ");
                double recursos = leerNumero("Valor de recursos licenciados (Q): ");
                // La postproducción necesita fotogramas y el valor de los recursos.
                return new AnimacionPostproduccion(codigo, estudio, proyecto, horas, horario, fotogramas, recursos);
            }
            case 4: {
                double resolucion = leerNumero("Resolución en millones de celdas: ");
                // El constructor de simulación revisa la resolución y exige horario nocturno.
                return new SimulacionFisica(codigo, estudio, proyecto, horas, horario, resolucion);
            }
            default:
                // Protege el método si en el futuro se modifica la selección de categorías.
                throw new IllegalArgumentException("Categoría no reconocida.");
        }
    }

    /** Lee un número decimal y vuelve a pedirlo si el formato es incorrecto. */
    private double leerNumero(String mensaje) {
        // El ciclo termina únicamente cuando se recibe un número finito.
        while (true) {
            // Se muestra la pregunta antes de leer la siguiente línea del teclado.
            System.out.print(mensaje);
            String texto = entrada.nextLine().trim().replace(',', '.');
            try {
                double numero = Double.parseDouble(texto);
                // Se rechazan NaN e infinito aunque Java permita convertir esos textos.
                if (!Double.isFinite(numero)) {
                    // Un valor no finito obliga a repetir la misma pregunta.
                    System.out.println("Escribe un número finito.");
                } else {
                    // Se entrega el número para que su clase valide las reglas de ese dato.
                    return numero;
                }
            } catch (NumberFormatException error) {
                // Se aceptan punto o coma decimal, pero no separadores de miles.
                System.out.println("Formato incorrecto. Ejemplo de número válido: 2.5.");
            }
        }
    }

    /** Lee un entero sin cerrar el programa si el usuario escribe letras o decimales. */
    private int leerEntero(String mensaje) {
        // Se repite la lectura hasta obtener un int válido.
        while (true) {
            // Se vuelve a mostrar la pregunta si la respuesta anterior no era un entero.
            System.out.print(mensaje);
            String texto = entrada.nextLine().trim();
            try {
                // parseInt rechaza decimales y valores fuera del rango de int.
                return Integer.parseInt(texto);
            } catch (NumberFormatException error) {
                // Después del aviso se repite exactamente la misma pregunta.
                System.out.println("Escribe un número entero válido.");
            }
        }
    }
}
