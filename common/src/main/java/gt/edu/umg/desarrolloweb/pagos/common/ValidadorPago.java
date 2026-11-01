package gt.edu.umg.desarrolloweb.pagos.common;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public final class ValidadorPago {

    private static final BigDecimal LIMITE_BANCO = new BigDecimal("25000.00");
    private static final long TOLERANCIA_SEGUNDOS = 5 * 60;

    private ValidadorPago() { /* utilidad */ }

    public static List<String> validar(PagoOrigen pago, Destino destino, ZonedDateTime ahora) {
        List<String> errores = new ArrayList<>();

        if (pago.montoTotal().signum() <= 0) {
            errores.add("montoTotal debe ser > 0");
        }
        if (pago.montoTotal().scale() != 2) {
            errores.add("montoTotal debe tener exactamente 2 decimales");
        }
        if (destino == Destino.BANCO && pago.montoTotal().compareTo(LIMITE_BANCO) > 0) {
            errores.add("montoTotal excede el límite del banco (Q25,000.00)");
        }
        if (pago.fechaPago().isAfter(ahora.plusSeconds(TOLERANCIA_SEGUNDOS))) {
            errores.add("fechaPago está en el futuro");
        }
        if (destino == Destino.BANCO && pago.formaPago() == FormaPago.CHEQUE) {
            errores.add("el banco no acepta pagos con cheque");
        }

        BigDecimal sumaRubros = pago.rubros().stream()
            .map(RubroDetalle::monto)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        if (sumaRubros.compareTo(pago.montoTotal()) != 0) {
            errores.add("la suma de rubros (" + sumaRubros + ") no cuadra con montoTotal (" + pago.montoTotal() + ")");
        }

        for (RubroDetalle r : pago.rubros()) {
            if (r.rubro() == Rubro.OTROS && (r.descripcion() == null || r.descripcion().isBlank())) {
                errores.add("rubro OTROS requiere descripción");
            }
        }

        return errores;
    }
}
