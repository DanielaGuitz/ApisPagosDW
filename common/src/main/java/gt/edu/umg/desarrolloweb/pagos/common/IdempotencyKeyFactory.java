package gt.edu.umg.desarrolloweb.pagos.common;

public final class IdempotencyKeyFactory {

    private final String prefijoEquipo;

    public IdempotencyKeyFactory(String prefijoEquipo) {
        if (prefijoEquipo == null || !prefijoEquipo.matches("^[A-Za-z0-9]{1,10}$")) {
            throw new IllegalArgumentException("prefijoEquipo debe ser alfanumérico de 1 a 10 caracteres");
        }
        this.prefijoEquipo = prefijoEquipo;
    }

    public String para(long pagoId) {
        if (pagoId <= 0) {
            throw new IllegalArgumentException("pagoId debe ser > 0");
        }
        return String.format("%s-%06d", prefijoEquipo, pagoId);
    }
}