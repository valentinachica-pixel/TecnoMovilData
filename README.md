# TecnoMovil Data – Procesamiento funcional de datos de transporte urbano

Caso de estudio Unidad 2 –  Ingenieria de Software y Datos, IU Digital de Antioquia.

Módulo en Java (sin frameworks externos) que procesa registros simulados de validadores y
sensores del transporte de TecnoValle usando **funciones puras, inmutabilidad, lambdas,
funciones de orden superior y Streams**.

## Requisitos

- JDK 17 o superior (probado con 17 y 21).

## Estructura

```
src/com/tecnomovil/
├── Accion.java               enum ENTRADA / SALIDA
├── RegistroTransporte.java   record inmutable (idUsuario, ruta, estacion, accion, timestamp)
├── Tramo.java                record: desplazamiento entre dos estaciones
├── EstadoRuta.java           record: resultado de sobrecarga por ruta
├── SimuladorDatos.java       generador determinista de datos simulados
├── AnalizadorTransporte.java funciones puras (tareas a–f)
└── Main.java                 ejecución y salida por consola
evidencias/salida_ejecucion.txt   salida real del programa
```

## Compilar y ejecutar

```bash
javac -encoding UTF-8 -d out src/com/tecnomovil/*.java
java -cp out com.tecnomovil.Main              # 150.000 usuarios, semilla 2026
java -cp out com.tecnomovil.Main 300000 7     # parámetros opcionales: usuarios y semilla
```

## Tareas implementadas (AnalizadorTransporte)

| Tarea | Método |
|-------|--------|
| a) Afluencia por estación | `afluenciaPorEstacion` |
| b) Horas pico | `flujoPorHora`, `horasPico` |
| c) Rutas más utilizadas | `rutasMasUtilizadas` |
| d) Patrones de viaje por usuario | `patronesPorUsuario` |
| e) Tiempo promedio entre estaciones | `tramosConsecutivos`, `tiempoPromedioEntreEstaciones` |
| f) Sobrecarga en rutas | `detectarSobrecarga` |

Cada método recibe el parámetro `paralelo` para usar `stream()` o `parallelStream()`.
