package src.com.tecnomovil;

public record Tramo(String origen, String destino, long minutos) {
    public String etiqueta() {
        return origen + " -> " + destino;
    }
}
