package src.com.tecnomovil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public final class SimuladorDatos {
    public static final LocalDate FECHA = LocalDate.of(2026, 11, 5);

    public record Ruta(String id, List<String> estaciones, long capacidadHoraria) { }
       
    public static List<Ruta> RUTAS = List.of(
        new Ruta("T1 - Troncal Norte.Sur",
            List.of("Terminal Norte", "Plaza Mayor", "Parque Central", "Hospital", "Universidad", "Terminal Sur"), 20000),
        new Ruta("T2 - Troncal Oriente",
            List.of("Terminal Oriente", "Mercado", "Plaza Mayor", "Centro Cívico", "Estadio"), 14000),
        new Ruta("A1 - Alimentadora Loma",
            List.of("Loma Alta", "Barrio Nuevo", "Parque Central", "Biblioteca"), 9500),
        new Ruta("A2 - Alimentadora Río",
            List.of("Puente Río", "Zona Industrial", "Hospital", "Centro Cívico"), 5200),
        new Ruta("E1 - Expreso Aeropuerto",
            List.of("Aeropuerto", "Terminal Sur", "Universidad", "Plaza Mayor"), 5500),
        new Ruta("E2 - Expreso Tecnoparque",
            List.of("Tecnoparque", "Estadio", "Parque Central", "Terminal Norte"), 6500)); 
        
private static final int[] PESOS_RUTAS = {30, 25, 15, 10, 8, 12};

private static final int[] HORAS_MAÑANA = {5, 6, 7, 8};
private static final int[] PESOS_MAÑANA = {10, 25, 40, 25};
private static final int[] HORAS_MEDIODIA = {10, 11, 12, 13, 14};
private static final int[] PESOS_MEDIODIA = {15, 25, 30, 20, 15};
private static final int[] HORAS_TARDE = {16, 17, 18, 19, 20};
private static final int[] PESOS_TARDE = {15, 30, 35, 15, 5};

private SimuladorDatos() { }

public static List<RegistroTransporte> simular (int usuarios, long semilla) {
    return IntStream.range (0, usuarios)
        .mapToObj (u -> viajesDeUsuario (u, semilla))
        .flatMap (Function.identity())
        .sorted (Comparator.comparing (RegistroTransporte::timestamp)
                 .thenComparing (RegistroTransporte::idUsuario))
        .toList();
}

private static Stream<RegistroTransporte> viajesDeUsuario (int numero, long semilla) {
    Random azar = new Random (semilla + numero * 1_000_003L);
    String idUsuario = String.format ("U%07d", numero);

    Stream<RegistroTransporte> manana = viaje (azar, idUsuario, HORAS_MAÑANA, PESOS_MAÑANA, false);
    Stream<RegistroTransporte> mediodia = azar.nextInt (100) < 40
        ? viaje (azar, idUsuario, HORAS_MEDIODIA, PESOS_MEDIODIA, azar.nextBoolean())
        : Stream.empty();
    Stream<RegistroTransporte> tarde = viaje (azar, idUsuario, HORAS_TARDE, PESOS_TARDE, true);

    return Stream.of(manana, mediodia, tarde).flatMap (Function.identity());
}

private static Stream<RegistroTransporte> viaje (Random azar, String idUsuario, int[] horas, int[] pesos, boolean sentidoInverso) {
    Ruta ruta = RUTAS.get (elegirIndice (azar, PESOS_RUTAS));
    List<String> estaciones = sentidoInverso ? invertir (ruta.estaciones()) : ruta.estaciones();
    int origen = azar.nextInt (estaciones.size() - 1);
    int destino = origen + 1 + azar.nextInt (estaciones.size() - origen - 1);

    LocalDateTime inicio = LocalDateTime.of (FECHA, LocalTime.of (elegirHora (azar, horas, pesos), azar.nextInt(60), azar.nextInt(60)));
    Long minutos = (destino - origen) * 4L + 3 + azar.nextInt(5);

    return Stream.of(
        new RegistroTransporte (idUsuario, ruta.id(), estaciones.get(origen), Accion.ENTRADA, inicio),
        new RegistroTransporte (idUsuario, ruta.id(), estaciones.get(destino), Accion.SALIDA, inicio.plusMinutes (minutos)));
}

private static List<String> invertir (List<String> lista) {
    int n = lista.size();
    return IntStream.range (0, n) .mapToObj (i -> lista.get (n - 1 - i)) .toList();
}
private static int elegirHora (Random azar, int[] horas, int[] pesos) {
    return horas[elegirIndice (azar, pesos)];
}
private static int elegirIndice (Random azar, int[] pesos) {
    int punto = azar.nextInt (IntStream.of(pesos).sum());
    int acumulado = 0;
    for (int i = 0; i < pesos.length; i++) {
        acumulado += pesos[i];
        if (punto < acumulado) {
            return i;
        }
    }
    return pesos.length - 1;
}
public static Map<String, Long> umbrales() {
    return RUTAS.stream() .collect (Collectors.toUnmodifiableMap (Ruta::id, Ruta::capacidadHoraria));
}
}
