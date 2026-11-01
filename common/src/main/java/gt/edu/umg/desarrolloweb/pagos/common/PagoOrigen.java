package gt.edu.umg.desarrolloweb.pagos.common;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;

public record PagoOrigen(
    long pagoId,
    String clienteCodigo,
    String clienteNombre,
    String nit,
    String contadorCodigo,
    BigDecimal montoTotal,
    List<RubroDetalle> rubros,
    FormaPago formaPago,
    ZonedDateTime fechaPago,
    int periodoAnio,
    int periodoMes,
    String concepto
) {
    public PagoOrigen {
        Objects.requireNonNull(clienteCodigo, "clienteCodigo obligatorio");
        Objects.requireNonNull(montoTotal, "montoTotal obligatorio");
        Objects.requireNonNull(rubros, "rubros obligatorio");
        Objects.requireNonNull(formaPago, "formaPago obligatorio");
        Objects.requireNonNull(fechaPago, "fechaPago obligatorio");
        if (montoTotal.scale() != 2) {
            throw new IllegalArgumentException("montoTotal debe tener 2 decimales");
        }
        if (montoTotal.signum() < 0) {
            throw new IllegalArgumentException("montoTotal no puede ser negativo");
        }
    }
}