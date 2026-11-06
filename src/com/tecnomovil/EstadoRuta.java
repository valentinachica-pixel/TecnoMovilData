package src.com.tecnomovil;

public record EstadoRuta(
    String ruta,
    int horaPico,
    long usuariosHoraPico,
    long umbral,
    Nivel nivel) {
    
        public enum Nivel { NORMAL, CRITICA }

        public double ocupacionPorcentual() {
            return umbral == 0 ? 0.0 : (usuariosHoraPico * 100.0) / umbral;
        }
}
