package gt.edu.umg.desarrolloweb.pagos.common;

import java.math.BigDecimal;
import java.util.Objects;

public record RubroDetalle(
    Rubro rubro,
    String descripcion,
    BigDecimal monto
) {
    public RubroDetalle {
        Objects.requireNonNull(rubro, "rubro obligatorio");
        Objects.requireNonNull(monto, "monto obligatorio");
        if (monto.scale() != 2) {
            throw new IllegalArgumentException("monto del rubro debe tener 2 decimales");
        }
    }
}