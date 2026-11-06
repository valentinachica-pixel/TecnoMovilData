package src.com.tecnomovil;

import java.time.LocalDateTime;
import java.util.Objects;

public record RegistroTransporte(
    String idUsuario,
    String ruta,
    String estacion,
    Accion accion,
    LocalDateTime timestamp) {

    public RegistroTransporte {
        Objects.requireNonNull(idUsuario, "idUsuario");
        Objects.requireNonNull(ruta, "ruta");
        Objects.requireNonNull(estacion, "estacion");
        Objects.requireNonNull(accion, "accion");
        Objects.requireNonNull(timestamp, "timestamp");
    }
}