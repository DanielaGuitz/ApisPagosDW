package gt.edu.umg.desarrolloweb.pagos.common;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IdempotencyKeyFactoryTest {

    @Test
    void generaClaveConPrefijoYPagoConSeisDigitos() {
        assertThat(new IdempotencyKeyFactory("equipo1").para(42))
            .isEqualTo("equipo1-000042");
    }

    @Test
    void rechazaPrefijoInvalido() {
        assertThatThrownBy(() -> new IdempotencyKeyFactory("equipo-1"))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rechazaPagoIdNoPositivo() {
        IdempotencyKeyFactory factory = new IdempotencyKeyFactory("equipo1");

        assertThatThrownBy(() -> factory.para(0))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
