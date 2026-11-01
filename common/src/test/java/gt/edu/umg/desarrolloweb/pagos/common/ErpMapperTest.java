package gt.edu.umg.desarrolloweb.pagos.common;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

class ErpMapperTest {

    private PagoOrigen pagoDummy() {
        return new PagoOrigen(1L, "CLI-0001", "Ana", "CF", "MTR-1",
            new BigDecimal("150.75"),
            List.of(new RubroDetalle(Rubro.AGUA_POTABLE, "Agua", new BigDecimal("150.75"))),
            FormaPago.EFECTIVO, ZonedDateTime.parse("2026-10-01T10:00:00-06:00"), 2026, 9, "Pago");
    }

    @Test
    void mapea_total_a_texto_con_dos_decimales() {
        ErpMapper.ErpRequest req = ErpMapper.mapear(pagoDummy(), "EQUIPO_A", "EQA-000001");
        assertThat(req.total).isEqualTo("150.75");
    }

    @Test
    void mapea_documento_origen() {
        ErpMapper.ErpRequest req = ErpMapper.mapear(pagoDummy(), "EQUIPO_A", "EQA-000001");
        assertThat(req.documento_origen.numero).isEqualTo("EQA-000001");
        assertThat(req.documento_origen.tipo).isEqualTo("RECIBO_CAJA");
        assertThat(req.documento_origen.fecha_emision).isEqualTo("2026-10-01");
    }

    @Test
    void mapea_periodo_fiscal_y_detalle() {
        ErpMapper.ErpRequest req = ErpMapper.mapear(pagoDummy(), "EQUIPO_A", "EQA-000001");
        assertThat(req.periodo_fiscal.anio).isEqualTo(2026);
        assertThat(req.periodo_fiscal.mes).isEqualTo(9);
        assertThat(req.detalle).hasSize(1);
        assertThat(req.detalle.get(0).monto).isEqualTo("150.75");
    }
}
