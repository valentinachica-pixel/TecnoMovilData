package src.com.tecnomovil;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public final class AnalizadorTransporte {
    
    private AnalizadorTransporte() { }
    public static final Predicate<RegistroTransporte> ES_ENTRADA = registro -> registro.accion() == Accion.ENTRADA;
    public static final Function<RegistroTransporte, String> POR_ESTACION = RegistroTransporte::estacion;
    public static final Function<RegistroTransporte, String> POR_RUTA = RegistroTransporte::ruta;
    public static final Function<RegistroTransporte, Integer> POR_HORA = registro -> registro.timestamp() .getHour();

    public static <K> Map<K, Long> contarPor (List<RegistroTransporte> registros, Predicate<RegistroTransporte> filtro, Function<RegistroTransporte, K> clasificador, boolean paralelo) {
        return Collections.unmodifiableMap (flujo (registros, paralelo) .filter (filtro) .collect (Collectors.groupingBy (clasificador, Collectors.counting ())));

    }
public static Map<String, Long> afluenciaPorEstacion (List<RegistroTransporte> registros, boolean paralelo) {
    return contarPor (registros, ES_ENTRADA, POR_ESTACION, paralelo);
}
public static Map<Integer, Long> flujoPorHora (List<RegistroTransporte> registros, boolean paralelo) {
    return contarPor (registros, ES_ENTRADA, POR_HORA, paralelo);
}
public static List<Map.Entry<Integer, Long>> horasPico(List<RegistroTransporte> registros, int n, boolean paralelo) {
    return topN(flujoPorHora(registros, paralelo), n);
}
public static List<Map.Entry<String, Long>> rutasMasUtilizadas (List<RegistroTransporte> registros, int n, boolean paralelo) {
    return topN (contarPor (registros, ES_ENTRADA, POR_RUTA, paralelo), n);
}
public static Map<String, List<String>> patronesPorUsuario (List<RegistroTransporte> registros, boolean paralelo) {
    return Collections.unmodifiableMap (flujo (registros, paralelo) .collect (Collectors.groupingBy (RegistroTransporte::idUsuario, Collectors.mapping (RegistroTransporte::estacion, Collectors.toUnmodifiableList()))));
}
public static List<Tramo> tramosConsecutivos (List<RegistroTransporte> registrosDeUsuario) {
    return IntStream.range (0, registrosDeUsuario.size() - 1) .filter (i -> registrosDeUsuario.get (i) .accion() == Accion.ENTRADA && registrosDeUsuario.get (i + 1) .accion() == Accion.SALIDA && registrosDeUsuario.get (i) .ruta() .equals (registrosDeUsuario.get (i + 1) .ruta())) .mapToObj (i -> {
        RegistroTransporte entrada = registrosDeUsuario.get (i);
        RegistroTransporte salida = registrosDeUsuario.get (i + 1);
        long minutos = java.time.Duration.between (entrada.timestamp(), salida.timestamp()) .toMinutes();
        return new Tramo (entrada.estacion(), salida.estacion(), minutos);
    }) 
    .toList();
} 
public static Map<String, Double> tiempoPromedioEntreEstaciones(List<RegistroTransporte> registros, boolean paralelo) {
        Map<String, List<RegistroTransporte>> porUsuario = flujo(registros, paralelo) .collect(Collectors.groupingBy(RegistroTransporte::idUsuario));
        Stream<List<RegistroTransporte>> listas = paralelo
                ? porUsuario.values().parallelStream()
                : porUsuario.values().stream();
        return Collections.unmodifiableMap(
                listas.flatMap(lista -> tramosConsecutivos(lista).stream()) .collect(Collectors.groupingBy(Tramo::etiqueta, Collectors.averagingLong(Tramo::minutos))));
    }
public static List<EstadoRuta> detectarSobrecarga(List<RegistroTransporte> registros, Map<String, Long> umbrales, boolean paralelo) {
        Map<String, Map<Integer, Long>> ingresosPorRutaYHora = flujo(registros, paralelo)
                .filter(ES_ENTRADA)
                .collect(Collectors.groupingBy(POR_RUTA, Collectors.groupingBy(POR_HORA, Collectors.counting())));
        return ingresosPorRutaYHora.entrySet().stream()
                .map(entrada -> estadoDeRuta(entrada.getKey(), entrada.getValue(), umbrales.getOrDefault(entrada.getKey(), Long.MAX_VALUE)))
                .sorted(Comparator.comparingDouble(EstadoRuta::ocupacionPorcentual).reversed())
                .toList();
    }
 
    private static EstadoRuta estadoDeRuta(String ruta, Map<Integer, Long> ingresosPorHora, long umbral) {
        Map.Entry<Integer, Long> pico = ingresosPorHora.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .orElseThrow();
        EstadoRuta.Nivel nivel = pico.getValue() > umbral ? EstadoRuta.Nivel.CRITICA : EstadoRuta.Nivel.NORMAL;
        return new EstadoRuta(ruta, pico.getKey(), pico.getValue(), umbral, nivel);
    }
public static <K> List<Map.Entry<K, Long>> topN(Map<K, Long> conteos, int n) {
        return conteos.entrySet().stream()
                .sorted(Map.Entry.<K, Long>comparingByValue().reversed() .thenComparing(entrada -> entrada.getKey().toString()))
                .limit(n)
                .map(entrada -> Map.entry(entrada.getKey(), entrada.getValue()))
                .toList();
    }
 
    private static Stream<RegistroTransporte> flujo(List<RegistroTransporte> registros, boolean paralelo) {
        return paralelo ? registros.parallelStream() : registros.stream();
    }
}