package gt.edu.umg.desarrolloweb.pagos.common;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ValidadorPagoTest {
    private final ZonedDateTime ahora = ZonedDateTime.now();

    private PagoOrigen pago(BigDecimal monto, FormaPago forma, List<RubroDetalle> rubros, ZonedDateTime fecha) {
        return new PagoOrigen(1L, "CLI-0001", null, "CF", null, monto, rubros, forma, fecha, 2026, 10, null);
    }

    private List<RubroDetalle> rubroUnico(Rubro rubro, String descripcion, BigDecimal monto) {
        return List.of(new RubroDetalle(rubro, descripcion, monto));
    }

    @Test
    void pagoValidoNoTieneErrores() {
        PagoOrigen pago = pago(new BigDecimal("150.75"), FormaPago.EFECTIVO,
            rubroUnico(Rubro.AGUA_POTABLE, null, new BigDecimal("150.75")), ahora.minusDays(1));

        assertThat(ValidadorPago.validar(pago, Destino.BANCO, ahora)).isEmpty();
    }

    @Test
    void pagoConChequeNoEsValidoParaBanco() {
        PagoOrigen pago = pago(new BigDecimal("150.75"), FormaPago.CHEQUE,
            rubroUnico(Rubro.AGUA_POTABLE, null, new BigDecimal("150.75")), ahora.minusDays(1));

        assertThat(ValidadorPago.validar(pago, Destino.BANCO, ahora))
            .contains("el banco no acepta pagos con cheque");
    }

    @Test
    void sumaDeRubrosDebeCuadrarConMontoTotal() {
        PagoOrigen pago = pago(new BigDecimal("150.75"), FormaPago.EFECTIVO,
            rubroUnico(Rubro.AGUA_POTABLE, null, new BigDecimal("100.00")), ahora.minusDays(1));

        assertThat(ValidadorPago.validar(pago, Destino.ERP, ahora))
            .anySatisfy(error -> assertThat(error).contains("la suma de rubros"));
    }

    @Test
    void rubroOtrosRequiereDescripcion() {
        PagoOrigen pago = pago(new BigDecimal("150.75"), FormaPago.EFECTIVO,
            rubroUnico(Rubro.OTROS, null, new BigDecimal("150.75")), ahora.minusDays(1));

        assertThat(ValidadorPago.validar(pago, Destino.ERP, ahora))
            .contains("rubro OTROS requiere descripción");
    }
}
