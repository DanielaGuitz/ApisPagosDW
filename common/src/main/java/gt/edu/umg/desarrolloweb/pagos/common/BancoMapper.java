package gt.edu.umg.desarrolloweb.pagos.common;

import java.math.BigDecimal;

public class BancoMapper {

    public static class BancoRequest {
        public String teamCode;
        public Payer payer;
        public Amount amount;
        public String transactionDate;
        public String channel;
        public String description;
    }

    public static class Payer {
        public String customerCode;
        public String fullName;
        public String taxId;
    }

    public static class Amount {
        public long valueInCents;
        public String currency = "GTQ";
    }

    public static BancoRequest mapear(PagoOrigen pago, String teamCode) {
        BancoRequest req = new BancoRequest();
        req.teamCode = teamCode;

        req.payer = new Payer();
        req.payer.customerCode = pago.clienteCodigo();
        req.payer.fullName = pago.clienteNombre();
        req.payer.taxId = (pago.nit() == null || pago.nit().isBlank()) ? "CF" : pago.nit();

        req.amount = new Amount();
        // Convertir a centavos con longValueExact (seguro, sin double)
        req.amount.valueInCents = pago.montoTotal().multiply(new BigDecimal("100")).longValueExact();

        // El banco pide ISO-8601 (ej. 2026-10-01T10:15:00-06:00)
        req.transactionDate = pago.fechaPago().toOffsetDateTime().toString();

        // Mapear forma de pago
        switch (pago.formaPago()) {
            case EFECTIVO: req.channel = "CASH"; break;
            case TARJETA: req.channel = "CARD"; break;
            case TRANSFERENCIA: req.channel = "BANK_TRANSFER"; break;
            case CHEQUE: req.channel = "CHECK"; break;
        }

        req.description = pago.concepto() != null ? pago.concepto() : "Pago agua";

        return req;
    }
}