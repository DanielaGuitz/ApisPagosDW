package gt.edu.umg.desarrolloweb.pagos.common;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import static org.assertj.core.api.Assertions.*;

class PagoOrigenTest {
    private PagoOrigen pagoValido() {
        return new PagoOrigen(123L, "CLI-0001", "Ana López", "CF", "MTR-01",
            new BigDecimal("150.75"),
            List.of(new RubroDetalle(Rubro.AGUA_POTABLE, "Servicio", new BigDecimal("150.75"))),
            FormaPago.EFECTIVO, ZonedDateTime.now(), 2026, 9, "Servicio");
    }

    @Test
    void pagoValido_seConstruye() {
        assertThat(pagoValido().pagoId()).isEqualTo(123L);
    }

    @Test
    void montoSinDosDecimales_falla() {
        assertThatThrownBy(() -> new PagoOrigen(123L, "CLI-0001", null, "CF", null,
            new BigDecimal("150.7"), List.of(), FormaPago.EFECTIVO, ZonedDateTime.now(), 2026, 9, null))
            .isInstanceOf(IllegalArgumentException.class);
    }
}