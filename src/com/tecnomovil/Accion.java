package src.com.tecnomovil;

public enum Accion {
    ENTRADA("Entrada"),
    SALIDA("Salida");

    private final String texto;

    Accion(String texto) {
        this.texto = texto;
    }

    public String Texto() {
        return texto;
    }
  }
