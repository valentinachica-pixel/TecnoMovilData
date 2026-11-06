package src.com.tecnomovil;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class Main {
    public static void main(String[] args) {
        System.setOut (new PrintStream (System.out, true, StandardCharsets .UTF_8));
        int usuarios = args.length > 0 ? Integer.parseInt (args[0]) : 150_000;
        Long semilla = args.length > 1 ? Long.parseLong (args [1]) : 2026L;

        System.out.println ("=== TecnoMovil Data - Procesamiento funcional de transporte urbano ===");

        List<RegistroTransporte> registros = SimuladorDatos .simular(usuarios, semilla);
        System.out.printf("Usuarios simulados : %,d%n", usuarios);
        System.out.printf("Registros generados: %,d (semilla %d)%n", registros.size(), semilla);
        System.out.println("Ejemplo de registro: " + registros.get(0));
 
        demostrarInmutabilidad (registros);

        titulo("a) Afluencia por estación (ingresos del día)");
        Map<String, Long> afluencia = AnalizadorTransporte.afluenciaPorEstacion(registros, false);
        AnalizadorTransporte.topN(afluencia, afluencia.size())
                .forEach(e -> System.out.printf("  %-18s %,10d%n", e.getKey(), e.getValue()));

        titulo("b) Horas pico (5 horas con mayor flujo)");
        AnalizadorTransporte.horasPico (registros, 5, false)
                                .forEach(e -> System.out.printf("  %02d:00 - %02d:59 %,10d ingresos%n", e.getKey(), e.getKey(), e.getValue()));
 

        titulo("c) Rutas más utilizadas");
        AnalizadorTransporte.rutasMasUtilizadas(registros, 6, false)
                .forEach(e -> System.out.printf("  %-26s %,10d ingresos%n", e.getKey(), e.getValue()));
            
        titulo("d) Patrones de viaje por usuario (primeros 5 usuarios)");
        Map<String, List<String>> patrones = AnalizadorTransporte.patronesPorUsuario(registros, false);
        System.out.printf("  Usuarios con patrón calculado: %,d%n", patrones.size());
        patrones.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .limit(5)
                .forEach(e -> System.out.println("  " + e.getKey() + " : " + String.join(" > ", e.getValue())));
 
        titulo("e) Tiempo promedio entre estaciones (8 tramos más lentos)");
        Map<String, Double> tiempos = AnalizadorTransporte.tiempoPromedioEntreEstaciones(registros, false);
        System.out.printf("  Tramos distintos analizados: %d%n", tiempos.size());
        tiempos.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed()
                        .thenComparing(Map.Entry.comparingByKey()))
                .limit(8)
                .forEach(e -> System.out.printf("  %-38s %6.2f min%n", e.getKey(), e.getValue()));
 
        titulo("f) Detección de sobrecarga en rutas (umbral = capacidad horaria simulada)");
        Map<String, Long> umbrales = SimuladorDatos.umbrales();
        List<EstadoRuta> estados = AnalizadorTransporte.detectarSobrecarga(registros, umbrales, false);
        System.out.printf("  %-26s %-11s %10s %9s %9s  %s%n", "Ruta", "Hora pico", "Ingresos", "Umbral", "Ocupación", "Estado");
        estados.forEach(s -> System.out.printf("  %-26s %02d:00-%02d:59 %,10d %,9d %8.1f%%  %s%n",
                s.ruta(), s.horaPico(), s.horaPico(), s.usuariosHoraPico(), s.umbral(),
                s.ocupacionPorcentual(), s.nivel()));
 
        compararSecuencialYParalelo(registros, umbrales);
    }
 
    private static void demostrarInmutabilidad(List<RegistroTransporte> registros) {
        titulo("Inmutabilidad de la colección original");
        try {
            registros.add(registros.get(0));
            System.out.println("  ERROR: la lista fue modificada");
        } catch (UnsupportedOperationException e) {
            System.out.println("  registros.add(...) -> UnsupportedOperationException (la lista es inmutable)");
        }
    }
 
    private static void compararSecuencialYParalelo(List<RegistroTransporte> registros, Map<String, Long> umbrales) {
        titulo("Comparación secuencial vs. parallelStream()");
        System.out.printf("  %-34s %12s %12s  %s%n", "Tarea", "Secuencial", "Paralelo", "Resultado igual");
 
        comparar("a) Afluencia por estación",
                () -> AnalizadorTransporte.afluenciaPorEstacion(registros, false),
                () -> AnalizadorTransporte.afluenciaPorEstacion(registros, true));
        comparar("b) Horas pico",
                () -> AnalizadorTransporte.horasPico(registros, 24, false),
                () -> AnalizadorTransporte.horasPico(registros, 24, true));
        comparar("c) Rutas más utilizadas",
                () -> AnalizadorTransporte.rutasMasUtilizadas(registros, 6, false),
                () -> AnalizadorTransporte.rutasMasUtilizadas(registros, 6, true));
        comparar("d) Patrones por usuario",
                () -> AnalizadorTransporte.patronesPorUsuario(registros, false),
                () -> AnalizadorTransporte.patronesPorUsuario(registros, true));
        comparar("e) Tiempo promedio entre estaciones",
                () -> AnalizadorTransporte.tiempoPromedioEntreEstaciones(registros, false),
                () -> AnalizadorTransporte.tiempoPromedioEntreEstaciones(registros, true));
        comparar("f) Sobrecarga en rutas",
                () -> AnalizadorTransporte.detectarSobrecarga(registros, umbrales, false),
                () -> AnalizadorTransporte.detectarSobrecarga(registros, umbrales, true));
    }
 
    private static void comparar(String nombre, Supplier<Object> secuencial, Supplier<Object> paralelo) {
        long t0 = System.nanoTime();
        Object resultadoSecuencial = secuencial.get();
        long t1 = System.nanoTime();
        Object resultadoParalelo = paralelo.get();
        long t2 = System.nanoTime();
 
        System.out.printf("  %-34s %9.1f ms %9.1f ms  %s%n", nombre,
                (t1 - t0) / 1_000_000.0, (t2 - t1) / 1_000_000.0,
                resultadoSecuencial.equals(resultadoParalelo) ? "SI" : "NO");
    }
 
    private static void titulo(String texto) {
        System.out.println();
        System.out.println("--- " + texto + " ---");
    
  }
}



    
