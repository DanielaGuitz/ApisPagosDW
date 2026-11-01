package gt.edu.umg.desarrolloweb.pagos.common;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

class BancoMapperTest {

    private PagoOrigen pagoDummy() {
        return new PagoOrigen(1L, "CLI-0001", "Ana", "123456", null,
            new BigDecimal("150.75"),
            List.of(new RubroDetalle(Rubro.AGUA_POTABLE, "Agua", new BigDecimal("150.75"))),
            FormaPago.EFECTIVO, ZonedDateTime.parse("2026-10-01T10:00:00-06:00"), 2026, 10, "Pago");
    }

    @Test
    void mapea_centavos_correctamente() {
        BancoMapper.BancoRequest req = BancoMapper.mapear(pagoDummy(), "EQUIPO_A");
        // Q150.75 -> 15075 centavos
        assertThat(req.amount.valueInCents).isEqualTo(15075L);
        assertThat(req.amount.currency).isEqualTo("GTQ");
    }

    @Test
    void mapea_canal_correctamente() {
        BancoMapper.BancoRequest req = BancoMapper.mapear(pagoDummy(), "EQUIPO_A");
        assertThat(req.channel).isEqualTo("CASH");
    }

    @Test
    void mapea_nit_nulo_a_cf() {
        PagoOrigen p = new PagoOrigen(1L, "CLI-1", null, null, null,
            new BigDecimal("10.00"), List.of(new RubroDetalle(Rubro.OTROS, "x", new BigDecimal("10.00"))),
            FormaPago.EFECTIVO, ZonedDateTime.now(), 2026, 1, null);
        BancoMapper.BancoRequest req = BancoMapper.mapear(p, "EQUIPO_A");
        assertThat(req.payer.taxId).isEqualTo("CF");
    }
}
