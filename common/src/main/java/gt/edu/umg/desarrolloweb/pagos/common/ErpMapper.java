package gt.edu.umg.desarrolloweb.pagos.common;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public class ErpMapper {

    /* =====================================================================
       CLASES INTERNAS DE APOYO
       Como el generador de OpenAPI nos daba problemas para instanciar los DTOs del ERP,
       creamos estas clases aquí mismo para armar el JSON exacto que pide el YAML.
       
       ===================================================================== */
       
    public static class ErpRequest {
        public String equipo;
        public DocumentoOrigen documento_origen;
        public Contribuyente contribuyente;
        public PeriodoFiscal periodo_fiscal;
        public String forma_pago;
        public String fecha_pago;
        public String total;
        public List<Detalle> detalle;
    }

    public static class DocumentoOrigen {
        public String tipo;
        public String numero;
        public String fecha_emision;
    }

    public static class Contribuyente {
        public String codigo_cliente;
        public String codigo_contador;
        public String nit;
    }

    public static class PeriodoFiscal {
        public int anio;
        public int mes;
    }

    public static class Detalle {
        public String rubro;
        public String descripcion;
        public String monto;
    }

    /* =====================================================================
       EL MÉTODO PRINCIPAL DE MAPEO
       Recibe nuestro PagoOrigen (de la base de datos), el código de equipo y la llave.
       Devuelve el ErpRequest listo para enviarse por Feign.
       ===================================================================== */
    public static ErpRequest mapear(PagoOrigen pago, String teamCode, String llaveIdempotencia) {
        ErpRequest req = new ErpRequest();
        
        // 1. EQUIPO: Asignamos el código del equipo (ej. EQUIPO_A)
        req.equipo = teamCode;

        // 2. DOCUMENTO ORIGEN: Aquí va la llave de idempotencia y la fecha de emisión
        req.documento_origen = new DocumentoOrigen();
        req.documento_origen.numero = llaveIdempotencia; // ej EQA-000123
        // Extraemos solo la fecha (sin hora) y la pasamos a String. Ej: "2026-10-01"
        req.documento_origen.fecha_emision = pago.fechaPago().toLocalDate().toString();
        
        // 3. TIPO DE DOCUMENTO Y FORMA DE PAGO
        //  El YAML del ERP exige que si es EFECTIVO mandemos "RECIBO_CAJA", 
        // si es TARJETA mandemos "VOUCHER_TARJETA", etc.
        switch (pago.formaPago()) {
            case EFECTIVO: req.documento_origen.tipo = "RECIBO_CAJA"; req.forma_pago = "EFECTIVO"; break;
            case TARJETA: req.documento_origen.tipo = "VOUCHER_TARJETA"; req.forma_pago = "TARJETA"; break;
            case TRANSFERENCIA: req.documento_origen.tipo = "BOLETA_DEPOSITO"; req.forma_pago = "TRANSFERENCIA"; break;
            case CHEQUE: req.documento_origen.tipo = "RECIBO_CAJA"; req.forma_pago = "CHEQUE"; break;
        }

        // 4. CONTRIBUYENTE: Los datos del cliente que pagó
        req.contribuyente = new Contribuyente();
        req.contribuyente.codigo_cliente = pago.clienteCodigo();
        req.contribuyente.codigo_contador = pago.contadorCodigo();
        // Regla del negocio: Si no tiene NIT, mandamos "CF" (Consumidor Final) por defecto
        req.contribuyente.nit = (pago.nit() == null || pago.nit().isBlank()) ? "CF" : pago.nit();

        // 5. PERIODO FISCAL: El ERP lo pide separado en año y mes
        req.periodo_fiscal = new PeriodoFiscal();
        req.periodo_fiscal.anio = pago.periodoAnio();
        req.periodo_fiscal.mes = pago.periodoMes();

        // 6. FECHA DE PAGO: El ERP pide ISO-8601 con zona horaria (ej. 2026-10-01T10:15:00-06:00)
        req.fecha_pago = pago.fechaPago().toOffsetDateTime().toString();

        // 7. MONTO TOTAL
        // El ERP NO usa centavos. Exige un String con exactamente 2 decimales. 
        // setScale(2) fuerza que sean 2 decimales. 
        // UNNECESSARY lanza error si el monto tiene más decimales (ej. 150.755), para evitar errores contables.
        // toPlainString() lo convierte a texto (ej. "150.75") en lugar de notación científica.
        req.total = pago.montoTotal().setScale(2, java.math.RoundingMode.UNNECESSARY).toPlainString();

        // 8. DETALLE (El desglose por rubro)
        // Usamos Streams de Java para iterar los rubros del pago y convertirlos a la clase Detalle del ERP
        req.detalle = pago.rubros().stream().map(r -> {
            Detalle d = new Detalle();
            d.rubro = r.rubro().name(); // Ej: AGUA_POTABLE o MORA
            d.descripcion = r.descripcion();
            // Igual que el total, el monto de cada rubro va como texto con 2 decimales exactos
            d.monto = r.monto().setScale(2, java.math.RoundingMode.UNNECESSARY).toPlainString();
            return d;
        }).collect(Collectors.toList());

        // Retornamos el objeto listo para que Feign lo envíe al ERP
        return req;
    }
}