# RenderGT

Proyecto de Java para gestionar trabajos de render, lotes y cobros. Es una app de consola que simula el flujo de un estudio de gráficos: crear trabajos, asignarlos a nodos, iniciar procesos y consultar ingresos.

## Cómo correrlo

```bash
javac -d out src/*.java
java -cp out Main
```

La opción `0` sale del programa.

## Qué está en el proyecto

- `Trabajo.java`: clase base para todos los trabajos.
- `ImagenFija.java`: imagen estática.
- `Animacion.java`: animaciones con fotogramas y tiempo.
- `AnimacionPostproduccion.java`: animación con edición y recursos extras.
- `SimulacionFisica.java`: simulaciones con GPU y cálculo especial.
- `Nodo.java`: equipo donde se ejecutan los trabajos.
- `Lote.java`: conjunto de trabajos asignados a un nodo.
- `SistemaRender.java`: lógica principal del sistema.
- `Main.java`: menú y entrada del usuario.
- `tests/`: pruebas para verificar reglas y resultados.

## Datos iniciales

El sistema ya tiene unos trabajos de ejemplo:

- `IMG01`, `IMG02`: imágenes fijas
- `ANI01`, `ANI02`: animaciones
- `POST01`, `POST02`: postproducción
- `SIM01`, `SIM02`: simulaciones físicas

También hay nodos:

- `N01`: capacidad 10 h, sin GPU
- `N02`: capacidad 20 h, con GPU
- `N03`: capacidad 30 h, con GPU

## Reglas principales

- Cada trabajo tiene un costo y un tiempo estimado.
- Los trabajos se asignan a lotes antes de ejecutarse.
- Si falla, puede volver a intentar según la lógica del ejercicio.
- Cada intento extra cuesta `Q25.00`.
- Los ingresos solo cambian cuando una operación está bien validada.
- La lógica usa herencia y polimorfismo para calcular costos y tiempos según el tipo de trabajo.

## Pruebas

```bash
javac -encoding UTF-8 -d out src/*.java tests/*.java
java -Dfile.encoding=UTF-8 -cp out lab 3
```

Las pruebas revisan:

- costos y cobros 
- tiempos de entrega 
- capacidad de los nodos 
- uso de GPU 
- estados de trabajos y lotes 
- reintentos 
- casos inválidos 

## explicacion basica

1. Ver trabajos y nodos.
2. Crear un lote en un nodo.
3. Asignar un trabajo al lote.
4. Iniciar el lote.
5. Resolver el resultado del trabajo.
6. Revisar ingresos y estado final.