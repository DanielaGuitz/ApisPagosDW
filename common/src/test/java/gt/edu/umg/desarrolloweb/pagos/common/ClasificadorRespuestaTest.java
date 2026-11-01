package gt.edu.umg.desarrolloweb.pagos.common;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class ClasificadorRespuestaTest {

    @Test
    void codigo_201_es_exito() {
        assertThat(ClasificadorRespuesta.clasificar(201, null)).isEqualTo(ClasificadorRespuesta.Accion.EXITO);
    }

    @Test
    void codigo_409_duplicado_es_exito() {
        assertThat(ClasificadorRespuesta.clasificar(409, "DUPLICATE_PAYMENT")).isEqualTo(ClasificadorRespuesta.Accion.EXITO);
        assertThat(ClasificadorRespuesta.clasificar(409, "DOCUMENTO_DUPLICADO")).isEqualTo(ClasificadorRespuesta.Accion.EXITO);
    }

    @Test
    void codigo_409_reutilizado_es_dlq() {
        assertThat(ClasificadorRespuesta.clasificar(409, "IDEMPOTENCY_KEY_REUSED")).isEqualTo(ClasificadorRespuesta.Accion.DLQ);
        assertThat(ClasificadorRespuesta.clasificar(409, "DOCUMENTO_REUTILIZADO")).isEqualTo(ClasificadorRespuesta.Accion.DLQ);
    }

    @Test
    void errores_fatales_van_a_dlq() {
        assertThat(ClasificadorRespuesta.clasificar(400, null)).isEqualTo(ClasificadorRespuesta.Accion.DLQ);
        assertThat(ClasificadorRespuesta.clasificar(403, null)).isEqualTo(ClasificadorRespuesta.Accion.DLQ);
        assertThat(ClasificadorRespuesta.clasificar(422, "TOTAL_NO_CUADRA")).isEqualTo(ClasificadorRespuesta.Accion.DLQ);
    }

    @Test
    void errores_transitorios_se_reintentan() {
        assertThat(ClasificadorRespuesta.clasificar(429, null)).isEqualTo(ClasificadorRespuesta.Accion.REINTENTAR);
        assertThat(ClasificadorRespuesta.clasificar(500, null)).isEqualTo(ClasificadorRespuesta.Accion.REINTENTAR);
        assertThat(ClasificadorRespuesta.clasificar(502, null)).isEqualTo(ClasificadorRespuesta.Accion.REINTENTAR);
        assertThat(ClasificadorRespuesta.clasificar(503, null)).isEqualTo(ClasificadorRespuesta.Accion.REINTENTAR);
        assertThat(ClasificadorRespuesta.clasificar(504, null)).isEqualTo(ClasificadorRespuesta.Accion.REINTENTAR);
    }
}
